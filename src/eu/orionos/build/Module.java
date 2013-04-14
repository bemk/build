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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import eu.orionos.build.exec.CommandKernel;

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

	public String getName()
	{
		return this.name;
	}
	public Module(String path) throws FileNotFoundException, IOException, ParseException
	{
		this(path, null);
	}

	@SuppressWarnings("unchecked")
	public Module(String path, Module parent) throws FileNotFoundException, IOException, ParseException
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
		module = (JSONObject) new JSONParser().parse(new FileReader(f));
		/* Get some paths right */
		this.cwd = f.getAbsolutePath();
		int len = 0;
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			len = this.cwd.lastIndexOf('\\');
		else
			len = this.cwd.lastIndexOf('/');
		this.cwd = this.cwd.substring(0, len);

		if (module.containsKey("name"))
			this.name = (String)module.get("name");
		else
		{
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
		if (module.containsKey(Syntax.GLOBAL_COMPILER))
		{
			this.globalCompiler = (String)module.get(Syntax.GLOBAL_COMPILER);
		}
		else if (this.getGlobalCompiler() == null)
		{
			System.err.println("A global compiler has to be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_COMPILER + "\" : \"<compiler>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.GLOBAL_COMPILER_FLAGS))
		{
			this.globalCompilerFlags = (String)module.get(Syntax.GLOBAL_COMPILER_FLAGS);
		}
		else if (this.getGlobalCompilerFlags() == null)
		{
			System.err.println("Global compiler options must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_COMPILER_FLAGS + "\" : \"<compiler flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.GLOBAL_LINKER))
		{
			this.globalLinker = (String)module.get(Syntax.GLOBAL_LINKER);
		}
		else if (this.getGlobalLinker() == null)
		{
			System.err.println("A Global linker must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_LINKER+ "\" : \"<linker>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.GLOBAL_LINKER_FLAGS))
		{
			this.globalLinkerFlags = (String)module.get(Syntax.GLOBAL_LINKER_FLAGS);
		}
		else if (this.getGlobalLinkerFlags() == null)
		{
			System.err.println("Global Linker options must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_LINKER_FLAGS +  "\" : \"<linker flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.GLOBAL_ARCHIVER))
		{
			this.globalArchiver = (String)module.get(Syntax.GLOBAL_ARCHIVER);
		}
		else if (this.getGlobalArchiver() == null)
		{
			System.err.println("A global archiver must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_ARCHIVER + "\" : \"<archiver>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.GLOBAL_ARCHIVER_FLAGS))
		{
			this.globalArchiverFlags = (String)module.get(Syntax.GLOBAL_ARCHIVER_FLAGS);
		}
		else if (this.getGlobalArchiverFlags() == null)
		{
			System.err.println("Global archiver flags must be set in the main build file");
			System.err.println("Specify: \"" + Syntax.GLOBAL_ARCHIVER_FLAGS + "\" : \"<archiver flags>\"");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}

		/* Get all the modular data in place */
		if (module.containsKey(Syntax.MOD_COMPILER))
			this.modCompiler = (String)module.get(Syntax.MOD_COMPILER);
		if (module.containsKey(Syntax.MOD_ARCHIVER_FLAGS))
			this.modArchiverFlags = (String)module.get(Syntax.MOD_ARCHIVER_FLAGS);
		if (module.containsKey(Syntax.MOD_ARCHIVER))
			this.modArchiver = (String)module.get(Syntax.MOD_ARCHIVER);
		if (module.containsKey(Syntax.MOD_ARCHIVER_FLAGS))
			this.modArchiverFlags = (String)module.get(Syntax.MOD_ARCHIVER_FLAGS);
		if (module.containsKey(Syntax.MOD_LINKER))
			this.modLinker = (String)module.get(Syntax.MOD_LINKER);
		if (module.containsKey(Syntax.MOD_LINKER_FLAGS))
			this.modLinkerFlags = (String)module.get(Syntax.MOD_LINKER_FLAGS);

		/* The dynamic flags */
		if (module.containsKey(Syntax.DYN_ARCHIVER_FLAGS))
			this.dynArchiverFlags = (JSONArray)module.get(Syntax.DYN_ARCHIVER_FLAGS);
		if (module.containsKey(Syntax.DYN_COMPILER_FLAGS))
			this.dynCompilerFlags = (JSONArray)module.get(Syntax.DYN_COMPILER_FLAGS);
		if (module.containsKey(Syntax.DYN_LINKER_FLAGS))
			this.dynLinkerFlags = (JSONArray)module.get(Syntax.DYN_LINKER_FLAGS);

		/* And the dynamic module wide flags */
		if (module.containsKey(Syntax.DYN_MOD_ARCHIVER_FLAGS))
			this.dynModArchiverFlags = (JSONArray)module.get(Syntax.DYN_MOD_ARCHIVER_FLAGS);
		if (module.containsKey(Syntax.DYN_MOD_COMPILER_FLAGS))
			this.dynModCompilerFlags = (JSONArray)module.get(Syntax.DYN_MOD_COMPILER_FLAGS);
		if (module.containsKey(Syntax.DYN_MOD_LINKER_FLAGS))
			this.dynModLinkerFlags = (JSONArray)module.get(Syntax.DYN_MOD_LINKER_FLAGS);

		/* Find all source files for this module */
		if (module.containsKey(Syntax.SOURCE))
			this.sourceFiles.addAll(((JSONArray)module.get(Syntax.SOURCE)));

		/* Determine whether or not we should link */
		if (module.containsKey(Syntax.LINK))
		{
			this.toLink = (boolean) module.get(Syntax.LINK);
		}
		else
		{
			System.err.println("Module " + name + " Did not specify linking");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		/* And determine the same for archiving */
		if (module.containsKey(Syntax.ARCHIVE))
		{
			this.toArchive = (boolean) module.get(Syntax.ARCHIVE);
		}
		else
		{
			System.err.println("Module " + name + " did not specify archiving");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		/* Determine the linked output file */
		if (module.containsKey(Syntax.LINKED))
		{
			this.linkedFile = (String) module.get(Syntax.LINKED);
		}
		else if (this.toLink)
		{
			System.err.println("Module " + name + " is to link, but no output file specified");
			System.err.println("Specify: \"" + Syntax.LINKED + "\" : \"<outputfile>\"");
			System.exit(ErrorCode.PARSE_FAILED);
		}
		/* Determine the archived output file */
		if (module.containsKey(Syntax.ARCHIVED))
		{
			this.archivedFile = (String) module.get(Syntax.ARCHIVED);
		}
		else if (this.toArchive)
		{
			System.err.println("Module " + name + " is to be archived, but no output file specified");
			System.err.println("Specify: \"" + Syntax.ARCHIVED + "\" : \"<outputfile>\"");
			System.exit(ErrorCode.PARSE_FAILED);
		}
		
		/* Get all the dependencies, dynamic or not */
		if (module.containsKey(Syntax.DEP))
		{
			JSONArray array = (JSONArray) module.get(Syntax.DEP);
			Iterator<JSONObject> i = array.iterator();
			while (i.hasNext())
			{
				JSONObject o = i.next();
				subModules.add(new Module((String) o.get(Syntax.DEP_PATH), this));
			}
		}
		if (module.containsKey(Syntax.DYN_DEP))
		{
			JSONArray array = (JSONArray) module.get(Syntax.DYN_DEP);
			Iterator<JSONObject> i = array.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				dynamicModules.put((String) o.get(Syntax.DYN_DEP_KEY), new Module((String) o.get(Syntax.DEP_PATH), this));
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
			Iterator i = dynCompilerFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getDefined(key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}
		if (dynModCompilerFlags != null)
		{
			Iterator i = dynModCompilerFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getModuleDefined(this.name, key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}

		return a;
	}
	protected String getCompilerFlags()
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

		return a;
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
			Iterator i = dynArchiverFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getDefined(key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}
		if (dynModArchiverFlags != null)
		{
			Iterator i = dynModArchiverFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getModuleDefined(this.name, key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}

		return a;
	}
	protected String getArchiverFlags()
	{
		String a = "";
		if (getGlobalArchiverFlags() == null)
			return null;
		a += getGlobalArchiverFlags();

		if (modArchiverFlags != null)
		{
			if (a.isEmpty())
				a += " ";
			a += modArchiverFlags;
		}

		String dyn = getDynArchiverFlags();
		if (!dyn.isEmpty())
		{
			if (a.isEmpty())
				a += " ";
			a += dyn;
		}

		return a;
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
			Iterator i = dynLinkerFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getDefined(key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}
		if (dynModLinkerFlags != null)
		{
			Iterator i = dynModLinkerFlags.iterator();
			while (i.hasNext())
			{
				JSONObject o = (JSONObject) i.next();
				String key = (String) o.get(Syntax.CONFIG_GLOBAL_KEY);
				if (c.getModuleDefined(this.name, key))
				{
					if (!a.isEmpty())
						a += " ";
					a += (String)o.get(Syntax.CONFIG_GLOBAL_FLAGS);
				}
			}
		}

		return a;
	}
	protected String getLinkerFlags()
	{
		String a = "";
		if (getGlobalLinkerFlags() == null)
			return null;
		a += getGlobalLinkerFlags();

		if (modLinkerFlags != null)
		{
			if (a.isEmpty())
				a += " ";
			a += modLinkerFlags;
		}

		String dyn = getDynLinkerFlags();
		if (!dyn.isEmpty())
		{
			if (a.isEmpty())
				a += " ";
			a += dyn;
		}

		return a;
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
	private void addDynamicDeps(ArrayList<Module> dependencies, Iterator<String> i)
	{
		while (i.hasNext())
		{
			String flag = i.next();
			if (dynamicModules.containsKey(flag))
			{
				Module m = dynamicModules.get(flag);
				if (!dependencies.contains(dynamicModules.get(m)))
				{
					dependencies.add(m);
				}
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
		{
			Iterator<String> i = flags.iterator();
			addDynamicDeps(dependencies, i);
		}

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
			String compilerFlags = getCompilerFlags();

			if (compiler == null || compilerFlags == null)
			{
				System.err.println("Compiler settings not read correctly");
				System.exit(ErrorCode.PARSE_FAILED);
			}

			String cmd[] = {compiler, "-c", sFile, "-o", oFile, compilerFlags};
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
		dynamicCommand.add(getArchiverFlags());
		dynamicCommand.add(getAFile());
		dynamicCommand.addAll(objectFiles);

		String cmd[] = dynamicCommand.toArray(new String[dynamicCommand.size()]);

		sendCommand(cmd, getAFile());

		return 0;
	}

	/* Also speaks for itself */
	public int link()
	{
		/* TODO: Make the command actually make sense */
		ArrayList<String> dynamicCommand = new ArrayList<String>();
		dynamicCommand.add(getLinker());
		dynamicCommand.add(getLinkerFlags());
		dynamicCommand.add("-o");
		dynamicCommand.add(linkedFile);
		dynamicCommand.addAll(getLinkableFiles());
		String cmd[] = dynamicCommand.toArray(new String[dynamicCommand.size()]);
		sendCommand(cmd, "linkedFile");

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
		return new ArrayList<String>();
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
