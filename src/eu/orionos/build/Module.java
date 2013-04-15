/*  Build - Hopefully a simple build system
    Copyright (C) 2013 - Bart Kuivenhoven

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. 

    A version of the licence can also be found at http://gnu.org/licences/
*/

package eu.orionos.build;

import eu.orionos.build.exec.CommandKernel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Module {
	private ConcurrentHashMap<String, CompileUnit> toRun;
	private ConcurrentHashMap<String, CompileUnit> ran;
	private ConcurrentHashMap<String, Module> waiting;

	private ArrayList<Module> subModules = new ArrayList<Module>();
	private HashMap<String, Module> dynamicModules = new HashMap<String, Module>();
	private ArrayList<String> sourceFiles = new ArrayList<String>();
	private ArrayList<String> objectFiles = new ArrayList<String>();
	private String linkedFile;
	private String archivedFile;
	private String cwd;
	private String name;
	private Module parent;
	private JSONObject module;

	private CompilePhase phase = CompilePhase.COMPILING;

	private boolean toLink = false;
	private boolean toArchive = false;

	private String globalCompiler;
	private String globalLinker;
	private String globalArchiver;

	private String modCompiler;
	private String modLinker;
	private String modArchiver;

	private String globalCompilerFlags;
	private String globalLinkerFlags;
	private String globalArchiverFlags;

	private String modCompilerFlags;
	private String modLinkerFlags;
	private String modArchiverFlags;

	private JSONArray dynArchiverFlags;
	private JSONArray dynCompilerFlags;
	private JSONArray dynLinkerFlags;
	
	private JSONArray dynModArchiverFlags;
	private JSONArray dynModCompilerFlags;
	private JSONArray dynModLinkerFlags;

	public Module(String path) throws FileNotFoundException, IOException, JSONException
	{
		this(path, null);
	}

	public String getName()
	{
		return this.name;
	}

	public Module(String path, Module parent) throws FileNotFoundException, IOException, JSONException
	{
		/* Get some verbosity out of our system */
		if (Config.getInstance().verbose())
		{
			System.out.println("Parsing " + path);
		}
		this.parent = parent;
		/* Get the actual module file */
		File f = new File(path);
		if (!f.exists())
		{
			System.err.println("Module at " +  path +  " can not be found");
			System.exit(ErrorCode.FILE_NOT_FOUND);
		}
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append('\n');
		}
		module = new JSONObject(stringBuilder.toString());
		/* Get some paths right */
		this.cwd = f.getAbsolutePath();
		int len = 0;
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			len = this.cwd.lastIndexOf('\\');
		else
			len = this.cwd.lastIndexOf('/');
		this.cwd = this.cwd.substring(0, len);

		try {
			this.name = module.getString("name");
		} catch (JSONException e) {
			System.err.println("Module in " + cwd + "referenced by " + path + " does not have a name field!");
			System.err.println("Modules have to have a name field!");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (Config.getInstance().RegisterModule(this) == false)
		{
			System.err.println("Module with name: " + this.name + " conflicts with another module of the same name");
			System.exit(ErrorCode.NAME_CONFLICT);
		}

		/* Read global stuff into local variables for easier access */
		this.globalCompiler = module.optString(Syntax.GLOBAL_COMPILER, null);
		if (this.getGlobalCompiler() == null) {
			System.err.println("A global compiler has to be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_COMPILER + "\" : \"<compiler>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalCompilerFlags = module.optString(Syntax.GLOBAL_COMPILER_FLAGS, null);
		if (this.getGlobalCompilerFlags() == null) {
			System.err.println("Global compiler options must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_COMPILER_FLAGS + "\" : \"<compiler flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalLinker = module.optString(Syntax.GLOBAL_LINKER, null);
		if (this.getGlobalLinker() == null) {
			System.err.println("A Global linker must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_LINKER+ "\" : \"<linker>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalLinkerFlags = module.optString(Syntax.GLOBAL_LINKER_FLAGS, null);
		if (this.getGlobalLinkerFlags() == null) {
			System.err.println("Global Linker options must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_LINKER_FLAGS +  "\" : \"<linker flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalArchiver = module.optString(Syntax.GLOBAL_ARCHIVER, null);
		if (this.getGlobalArchiver() == null) {
			System.err.println("A global archiver must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_ARCHIVER + "\" : \"<archiver>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		this.globalArchiverFlags = module.optString(Syntax.GLOBAL_ARCHIVER_FLAGS, null);
		if (this.getGlobalArchiverFlags() == null) {
			System.err.println("Global archiver flags must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_ARCHIVER_FLAGS + "\" : \"<archiver flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}

		/* Get all the modular data in place */
		this.modCompiler = module.optString(Syntax.MOD_COMPILER, null);
		this.modCompilerFlags = module.optString(Syntax.MOD_COMPILER_FLAGS, null);
		this.modArchiverFlags = module.optString(Syntax.MOD_ARCHIVER_FLAGS, null);
		this.modArchiver = module.optString(Syntax.MOD_ARCHIVER, null);
		this.modArchiverFlags = module.optString(Syntax.MOD_ARCHIVER_FLAGS, null);
		this.modLinker = module.optString(Syntax.MOD_LINKER, null);
		this.modLinkerFlags = module.optString(Syntax.MOD_LINKER_FLAGS, null);

		/* The dynamic flags */
		this.dynArchiverFlags = module.optJSONArray(Syntax.DYN_ARCHIVER_FLAGS);
		this.dynCompilerFlags = module.optJSONArray(Syntax.DYN_COMPILER_FLAGS);
		this.dynLinkerFlags = module.optJSONArray(Syntax.DYN_LINKER_FLAGS);
		
		/* And the dynamic module wide flags */
		this.dynModArchiverFlags = module.optJSONArray(Syntax.DYN_MOD_ARCHIVER_FLAGS);
		this.dynModCompilerFlags = module.optJSONArray(Syntax.DYN_MOD_COMPILER_FLAGS);
		this.dynModLinkerFlags = module.optJSONArray(Syntax.DYN_MOD_LINKER_FLAGS);
		/* Find all source files for this module */
		JSONArray sources = module.optJSONArray(Syntax.SOURCE);
		for (int i = 0; i < sources.length(); i++) {
			final String source = sources.optString(i, null);
			if (source != null)
				this.sourceFiles.add(source);
		}
		/* Determine whether or not we should link */
		try {
			this.toLink = module.getBoolean(Syntax.LINK);
		} catch (JSONException e) {
			System.err.println("Module " + name + " Did not specify linking");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		/* And determine the same for archiving */
		try {
			this.toArchive = module.getBoolean(Syntax.ARCHIVE);
		} catch (JSONException e) {
			System.err.println("Module " + name + " did not specify archiving");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		/* Determine the linked output file */
		if (this.toLink) {
			try {
				this.linkedFile = module.getString(Syntax.LINKED);
			} catch (JSONException e) {
				System.err.println("Module " + name + " is to link, but no output file specified");
				System.err.println("Specify: \"" + Syntax.LINKED + "\" : \"<outputfile>\"");
				System.exit(ErrorCode.PARSE_FAILED);
			}
		}
		if (this.toArchive) {
			try {
				this.archivedFile = module.getString(Syntax.ARCHIVED);
			} catch (JSONException e) {
				System.err.println("Module " + name + " is to be archived, but no output file specified");
				System.err.println("Specify: \"" + Syntax.ARCHIVED + "\" : \"<outputfile>\"");
				System.exit(ErrorCode.PARSE_FAILED);
			}
		}
		
		/* Get all the dependencies, dynamic or not */
		JSONArray array = module.optJSONArray(Syntax.DEP);
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				try {
					final JSONObject o = array.getJSONObject(i);
					subModules.add(new Module(o.getString(Syntax.DEP_PATH), this));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		array = module.optJSONArray(Syntax.DYN_DEP);
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				try {
					JSONObject o = array.getJSONObject(i);
					dynamicModules.put(o.getString(Syntax.DYN_DEP_KEY), new Module(o.getString(Syntax.DEP_PATH), this));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* These functions retrieve the global values only. These are helper
	 * functions and should only be called by the functions that determine the
	 * actual values */
	protected String getGlobalArchiver()
	{
		if (parent != null && this.globalArchiver == null)
			return parent.getGlobalArchiver();
		return globalArchiver;
	}
	protected String getGlobalCompiler()
	{
		if (parent != null && globalCompiler == null)
			return parent.getGlobalCompiler();
		return globalCompiler;
	}
	protected String getGlobalLinker()
	{
		if (parent != null && globalLinker == null)
			return parent.getGlobalLinker();
		return globalLinker;
	}
	
	protected String getGlobalArchiverFlags()
	{
		String a = "";
		if (parent != null)
			a = parent.getGlobalArchiverFlags();
		if (!a.isEmpty() && globalArchiverFlags != null)
			a += " " + globalArchiverFlags;
		else if (globalArchiverFlags != null)
			a += globalArchiverFlags;

		return a;
	}
	protected String getGlobalCompilerFlags()
	{
		String a = "";
		if (parent != null)
			a = parent.getGlobalCompilerFlags();
		if (!a.isEmpty() && globalCompilerFlags != null)
			a += " " + globalCompilerFlags;
		else if (globalCompilerFlags != null)
			a += globalCompilerFlags;

		return a;
	}
	protected String getGlobalLinkerFlags()
	{
		String a = "";
		if (parent != null)
			a = parent.getGlobalLinkerFlags();
		if (!a.isEmpty() && globalLinkerFlags != null)
			a += " " + globalLinkerFlags;
		else if (globalCompilerFlags != null)
			a += globalLinkerFlags;

		return a;
	}

	/* And get the actual strings to instert into the commands */
	protected String getCompiler()
	{
		if (modCompiler == null)
			return getGlobalCompiler();
		return modCompiler;
	}
	@SuppressWarnings("rawtypes")
	private String getDynCompilerFlags()
	{
		String a = "";
		Config c = Config.getInstance();

		if (dynCompilerFlags != null)
		{
			for (int i = 0; i < dynCompilerFlags.length(); i++) {
				try {
					final JSONObject o = dynCompilerFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getDefined(key) && o.has(Syntax.CONFIG_GLOBAL_KEY))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (dynModCompilerFlags != null)
		{
			for (int i = 0; i < dynModCompilerFlags.length(); i++) {
				try {
					final JSONObject o = dynModCompilerFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key) && o.has(Syntax.CONFIG_GLOBAL_FLAGS))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return a;
	}
	protected String[] getCompilerFlags()
	{
		String a = "";
		if (getGlobalCompilerFlags() == null)
			return null;

		a += getGlobalCompilerFlags();
		if (modCompilerFlags != null)
		{
			if (!a.isEmpty())
				a += " ";
			a += modCompilerFlags;
		}

		String dyn = getDynCompilerFlags();
		if (!dyn.isEmpty())
		{
			if (!a.isEmpty())
				a += " ";
			a += dyn;
		}

		return a.split(" ");
	}

	protected String getArchiver()
	{
		if (modArchiver == null)
			return getGlobalArchiver();
		return modArchiver;
	}
	@SuppressWarnings("rawtypes")
	private String getDynArchiverFlags()
	{
		String a = "";
		Config c = Config.getInstance();

		if (dynArchiverFlags != null)
		{
			for (int i = 0; i < dynArchiverFlags.length(); i++) {
				try {
					final JSONObject o = dynArchiverFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getDefined(key) && o.has(Syntax.CONFIG_GLOBAL_FLAGS))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (dynModArchiverFlags != null)
		{
			for (int i = 0; i < dynModArchiverFlags.length(); i++) {
				try {
					final JSONObject o = dynModArchiverFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key) && o.has(Syntax.CONFIG_GLOBAL_FLAGS))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return a;
	}
	protected String[] getArchiverFlags()
	{
		String a = "";
		if (getGlobalArchiverFlags() == null)
			return null;
		a += getGlobalArchiverFlags();

		if (modArchiverFlags != null)
		{
			if (!a.isEmpty())
				a += " ";
			a += modArchiverFlags;
		}

		String dyn = getDynArchiverFlags();
		if (!dyn.isEmpty())
		{
			if (!a.isEmpty())
				a += " ";
			a += dyn;
		}

		return a.split(" ");
	}

	protected String getLinker()
	{
		if (modLinker == null)
			return getGlobalLinker();
		return modLinker;
	}
	@SuppressWarnings("rawtypes")
	private String getDynLinkerFlags()
	{
		String a = "";
		Config c = Config.getInstance();

		if (dynLinkerFlags != null)
		{
			for (int i = 0; i < dynLinkerFlags.length(); i++) {
				try {
					final JSONObject o = dynLinkerFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key) && o.has(Syntax.CONFIG_GLOBAL_FLAGS))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (dynModLinkerFlags != null)
		{
			for (int i = 0; i < dynModLinkerFlags.length(); i++) {
				try {
					final JSONObject o = dynModLinkerFlags.getJSONObject(i);
					final String key = o.getString(Syntax.CONFIG_GLOBAL_KEY);
					if (c.getModuleDefined(this.name, key) && o.has(Syntax.CONFIG_GLOBAL_FLAGS))
					{
						if (!a.isEmpty())
							a += " ";
						a += o.getString(Syntax.CONFIG_GLOBAL_FLAGS);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		return a;
	}
	protected String[] getLinkerFlags()
	{
		String a = "";
		if (getGlobalLinkerFlags() == null)
			return null;
		a += getGlobalLinkerFlags();

		if (modLinkerFlags != null)
		{
			if (!a.isEmpty())
				a += " ";
			a += modLinkerFlags;
		}

		String dyn = getDynLinkerFlags();
		if (!dyn.isEmpty())
		{
			if (!a.isEmpty())
				a += " ";
			a += dyn;
		}

		return a.split(" ");
	}

	/* Turn an input file name into the name of a unique output file */
	private String getOFile(String inFile)
	{
		String a = "";

		inFile = inFile.substring(0, inFile.lastIndexOf("."));
		inFile = inFile.replace('\\', '_');
		inFile = inFile.replace('/', '-');

		a += Config.getInstance().getBuildDir() + "/" + this.name + "-" + inFile + ".o";

		return a;
	}

	private String getAFile()
	{
		return Config.getInstance().getBuildDir() + "/" + archivedFile;
	}

	private String getLFile()
	{
		return Config.getInstance().getBuildDir() + "/" + linkedFile;
	}

	/* helper function for determining the actual dependencies */
	private void addDynamicDeps(ArrayList<Module> dependencies, JSONArray array)
	{
		for (int i = 0; i < array.length(); i++) {
			try {
				final String flag = array.getString(i);
				if (dynamicModules.containsKey(flag)) {
					Module m = dynamicModules.get(flag);
					if (!dependencies.contains(m))
						dependencies.add(m);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/* Use this to calculate the dependencies to wait for, and to make build */
	@SuppressWarnings("unchecked")
	private ArrayList<Module> calculateDependencies()
	{
		ArrayList<Module> dependencies = new ArrayList<Module>(subModules);
		JSONArray flags = Config.getInstance().getGlobalFlags();
		if (flags != null)
			addDynamicDeps(dependencies, flags);
		return dependencies;
	}

	/* Initialise build phase, make all dependencies compile, and then do the same */
	public void build() throws InterruptedException
	{
		CommandKernel.getInstance().registerModule(this);
		ran = new ConcurrentHashMap<String, CompileUnit>(Config.getInstance().threads()+1);
		toRun = new ConcurrentHashMap<String, CompileUnit>(Config.getInstance().threads()+1);
		waiting = new ConcurrentHashMap<String, Module>(Config.getInstance().threads()+1);

		ArrayList<Module> deps = calculateDependencies();
		Iterator<Module> i = deps.iterator();
		while (i.hasNext())
		{
			Module m = i.next();
			waiting.put(m.getName(), m);
			m.build();
		}

		if (this.compile() != 0)
			System.exit(ErrorCode.COMPILE_FAILED);
	}

	/* get the absolute location of a file */
	private String sFileLocation(String sFile)
	{
		return this.cwd + "/" + sFile;
	}

	private void sendCommand(String cmd[], String object)
	{
		CompileUnit c = new CompileUnit(this, cmd, object);
		toRun.put(c.key(), c);
		CommandKernel.getInstance().runCommand(c);
	}

	/* Does this need more explanation? */
	public int compile()
	{
		if (Config.getInstance().getClean())
			return this.clean();

		Iterator<String> i = sourceFiles.iterator();
		while (i.hasNext())
		{
			String sFileName = i.next();
			String sFile = sFileLocation(sFileName);
			String oFile = getOFile(sFileName);
			objectFiles.add(oFile);
			String compiler = getCompiler();
			String[] compilerFlags = getCompilerFlags();

			if (compiler == null || compilerFlags == null)
			{
				System.err.println("Compiler settings not read correctly");
				System.exit(ErrorCode.PARSE_FAILED);
			}

			ArrayList<String> command = new ArrayList<String>();
			command.add(compiler);
			command.add("-c");
			command.add(sFile);
			command.add("-o");
			command.add(oFile);
			for (String s : compilerFlags)
				command.add(s);
			
			String cmd[] = command.toArray(new String[command.size()]);
			sendCommand(cmd, oFile);
		}

		return 0;
	}

	/* Generally this should call the ar command */
	public int compress()
	{
		/*
		 * We don't need to care about other modules finishing their build process.
		 * We just need to get the archiving done.
		 */

		ArrayList<String> dynamicCommand = new ArrayList<String>();
		dynamicCommand.add(getArchiver());
		for (String s : getArchiverFlags())
			dynamicCommand.add(s);
		dynamicCommand.add(getAFile());
		dynamicCommand.addAll(objectFiles);

		String cmd[] = dynamicCommand.toArray(new String[dynamicCommand.size()]);

		sendCommand(cmd, getAFile());

		return 0;
	}

	/* Also speaks for itself */
	public int link()
	{
		ArrayList<String> dynamicCommand = new ArrayList<String>();
		dynamicCommand.add(getLinker());
		for (String s : getLinkerFlags())
			dynamicCommand.add(s);
		dynamicCommand.add("-o");
		dynamicCommand.add(getLFile());
		dynamicCommand.addAll(getLinkableFiles());
		String cmd[] = dynamicCommand.toArray(new String[dynamicCommand.size()]);
		sendCommand(cmd, getLFile());

		return 0;
	}

	/* Doesn't need much more explanation */
	public int clean()
	{
		this.phase = CompilePhase.CLEANING;
		Iterator<String> i = sourceFiles.iterator();
		while (i.hasNext())
		{
			String sFile = i.next();
			String obj = getOFile(sFile);
			String cmd[] = {"rm", "-fv", obj};
			
			sendCommand(cmd, obj);
		}
		if (this.toArchive)
		{
			String archive[] = {"rm", "-fv", getAFile()};
			sendCommand(archive, getAFile());
		}
		if (this.toLink)
		{
			String linked[] = {"rm", "-fv", getLFile()};
			sendCommand(linked, getLFile());
		}

		return 0;
	}

	/* Generate a list of output files to be fed into the linker */
	public ArrayList<String> getLinkableFiles()
	{
		/* TODO: generate a list of all files to be linked */
		ArrayList<String> ret = new ArrayList<String>();
		ArrayList<Module> deps = calculateDependencies();
		if (this.toArchive)
		{
			ret.add(getAFile());
		}
		else
		{
			ret.addAll(sourceFiles);
		}
		for (Module m : deps)
		{
			ret.addAll(m.getLinkableFiles());
		}

		return ret;
	}

	/* Mark a dependency as completed */
	public void markDone(Module m)
	{
		if (waiting.containsKey(m.getName()))
		{
			waiting.remove(m.name);
			switchPhases();
		}
	}

	/* Make an attempt at moving on to the next build phase */
	private void switchPhases()
	{
		if (toRun.isEmpty())
		{
			switch (phase)
			{
			case COMPILING:
				if (toArchive)
				{
					phase = CompilePhase.ARCHIVING;
					this.compress();
				}
				else
				{
					phase = CompilePhase.WAITING_FOR_DEPS;
					switchPhases();
				}
				break;
			case ARCHIVING:
				phase = CompilePhase.WAITING_FOR_DEPS;
				switchPhases();
			case WAITING_FOR_DEPS:
				if (waiting.isEmpty())
				{
					if (toLink)
					{
						phase = CompilePhase.LINKING;
						this.link();
					}
					else
					{
						phase = CompilePhase.DONE;
						this.switchPhases();
					}
				}
				break;
			case LINKING:
				/* And we're done! */
				CommandKernel.getInstance().unregisterModule(this);
				phase = CompilePhase.DONE;
				switchPhases();
				break;
			case DONE:
				CommandKernel.getInstance().unregisterModule(this);
				if (parent != null)
					parent.markDone(this);
				break;
			case CLEANING:
				phase = CompilePhase.DONE;
				switchPhases();
				break;
			default:
				/* Not sure how we'll ever get here, but having a default option
				 * is considered good practice ...
				 */
				break;
			}
		}
	}

	/* Is to run each time a compile unit is done */
	public void markCompileUnitDone(CompileUnit unit)
	{
		if (toRun.containsKey(unit.key()))
		{
			toRun.remove(unit.key());
			ran.put(unit.key(), unit);
		}
		switchPhases();
	}

	/* Allows parent objects to check whether we're done or not */
	public boolean getDone()
	{
		return waiting.isEmpty();
	}
}
