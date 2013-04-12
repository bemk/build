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

	private ArrayList<Module> subModules = new ArrayList<Module>();
	private HashMap<String, Module> dynamicModules = new HashMap<String, Module>();
	private ArrayList<String> sourceFiles = new ArrayList<String>();
	private String linkedFile;
	private String archivedFile;
	private String cwd;
	private String name;
	private Module parent;
	private JSONObject module;

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

		if (module.containsKey(Syntax.SOURCE))
			this.sourceFiles.addAll(((JSONArray)module.get(Syntax.SOURCE)));

		if (module.containsKey(Syntax.LINK))
		{
			this.toLink = (boolean) module.get(Syntax.LINK);
		}
		else
		{
			System.err.println("Module " + name + " Did not specify linking");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
		if (module.containsKey(Syntax.ARCHIVE))
		{
			this.toArchive = (boolean) module.get(Syntax.ARCHIVE);
		}
		else
		{
			System.err.println("Module " + name + " did not specify archiving");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
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
			a = parent.getGlobalCompilerFlags();
		if (!a.isEmpty() && globalCompilerFlags != null)
			a += " " + globalCompilerFlags;
		else
			a += globalCompilerFlags;

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

	private String getOFile(String inFile)
	{
		String a = "";

		inFile = inFile.substring(0, inFile.lastIndexOf("."));
		inFile = inFile.replace('\\', '_');
		inFile = inFile.replace('/', '-');

		a += Config.getInstance().getBuildDir() + "/" + this.name + "-" + inFile + ".o";

		return a;
	}

	private void buildSubModules(Iterator<String>i) throws InterruptedException
	{
		while (i.hasNext())
		{
			String flag = i.next();
			if (dynamicModules.containsKey(flag))
			{
				dynamicModules.get(flag).build();
			}
		}
	}

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

	public void build() throws InterruptedException
	{
		ran = new ConcurrentHashMap<String, CompileUnit>(Config.getInstance().threads()+1);
		toRun = new ConcurrentHashMap<String, CompileUnit>(Config.getInstance().threads());

		ArrayList<Module> deps = calculateDependencies();
		Iterator<Module> i = deps.iterator();
		while (i.hasNext())
		{
			Module m = i.next();
			m.build();
		}

		if (this.compile() != 0)
			System.exit(ErrorCode.COMPILE_FAILED);

		while (!toRun.isEmpty()){
			Thread.sleep(50);
		}
	}

	private String sFileLocation(String sFile)
	{
		return this.cwd + "/" + sFile;
	}

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
			String compiler = getCompiler();
			String compilerFlags = getCompilerFlags();

			if (compiler == null || compilerFlags == null)
			{
				System.err.println("Compiler settings not read correctly");
				System.exit(ErrorCode.PARSE_FAILED);
			}

			String cmd[] = {compiler, "-c", sFile, "-o", oFile, compilerFlags};
			CompileUnit c = new CompileUnit(this, cmd, oFile);
			toRun.put(c.key(), c);
			CommandKernel.getInstance().runCommand(c);
		}

		return 0;
	}

	public int compress()
	{
		System.out.println("Compressing");
		/* \TODO: prepare the archiving command */
		return 0;
	}

	public int link()
	{
		System.out.println("Linking");
		/* \TODO: prepare the linking command */
		return 0;
	}

	public int clean()
	{
		Iterator<String> i = sourceFiles.iterator();
		while (i.hasNext())
		{
			String sFile = i.next();
			String obj = getOFile(sFile);
			String cmd[] = {"rm", "-fv", obj};
			
			CompileUnit u = new CompileUnit(this, cmd, obj);
			toRun.put(u.key(), u);
			CommandKernel.getInstance().runCommand(u);
		}

		return 0;
	}

	private ArrayList<String> getCompilerObjects()
	{
		return null;
	}
	public ArrayList<String> getObjectFiles()
	{
		ArrayList<String> ret = new ArrayList<String>();
		/*
		 * These output files can be the object files put out by the compiler.
		 * If a linker is set however, it is chosen as the only output files.
		 * If an archiver is set, its output is chosen over both linker and object files.
		 */

		if (this.toArchive)
			ret.add(this.archivedFile);
		else if (this.toLink)
			ret.add(this.linkedFile);
		else
			ret.addAll(getCompilerObjects());

		return ret;
	}

	public void mark(CompileUnit unit)
	{
		if (toRun.containsKey(unit.key()))
		{
			toRun.remove(unit.key());
			ran.put(unit.key(), unit);
		}
		if (toRun.isEmpty())
		{
			CommandKernel.getInstance().signalDone(name);
		}
	}
}
