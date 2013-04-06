package eu.orionos.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Module {
	private ArrayList<Module> subModules = new ArrayList<Module>();
	private String cwd;
	private Module parent;
	private JSONObject module;

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

	public Module(String path) throws FileNotFoundException, IOException, ParseException
	{
		this(path, null);
	}

	public Module(String path, Module parent) throws FileNotFoundException, IOException, ParseException
	{
		/* Get some verbosity out of our system */
		if (Config.getInstance().verbose())
		{
			System.out.println("Parsing " + path);
		}
		/* Get the actual module file */
		File f = new File(path);
		if (!f.exists())
		{
			System.err.println("Module at " +  path +  " can not be found");
			System.exit(1);
		}
		module = (JSONObject) new JSONParser().parse(new FileReader(f));
		/* Get some paths right */
		this.cwd = f.getAbsolutePath();
		int len = this.cwd.lastIndexOf('/');
		this.cwd = this.cwd.substring(0, len);

		/* Read global stuff into local variables for easier access */
		if (module.containsKey(Syntax.GLOBAL_COMPILER))
			this.globalCompiler = (String)module.get(Syntax.GLOBAL_COMPILER);
		if (module.containsKey(Syntax.GLOBAL_COMPILER_FLAGS))
			this.globalCompilerFlags = (String)module.get(Syntax.GLOBAL_COMPILER_FLAGS);
		if (module.containsKey(Syntax.GLOBAL_LINKER))
			this.globalLinker = (String)module.get(Syntax.GLOBAL_LINKER);
		if (module.containsKey(Syntax.GLOBAL_LINKER_FLAGS))
			this.globalLinkerFlags = (String)module.get(Syntax.GLOBAL_LINKER_FLAGS);
		if (module.containsKey(Syntax.GLOBAL_ARCHIVER))
			this.globalArchiver = (String)module.get(Syntax.GLOBAL_ARCHIVER);
		if (module.containsKey(Syntax.GLOBAL_ARCHIVER_FLAGS))
			this.globalArchiverFlags = (String)module.get(Syntax.GLOBAL_ARCHIVER_FLAGS);

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
			a = parent.getGlobalArchiverFlags();
		return a + " " + globalArchiverFlags;
	}
	protected String getGlobalCompilerFlags()
	{
		String a = "";
		if (parent != null)
			a = parent.getGlobalCompilerFlags();
		return a + " " + globalCompilerFlags;
	}
	protected String getGlobalLinkerFlags()
	{
		String a = "";
		if (parent != null)
			a = parent.getGlobalLinkerFlags();
		return a + " " + globalLinkerFlags;
	}

	protected String getCompiler()
	{
		if (modCompiler == null)
			return getGlobalCompiler();
		return modCompiler;
	}
	protected String getCompilerFlags()
	{
		String a = "";
		if (getGlobalCompilerFlags() == null)
			return null;

		a += getGlobalCompilerFlags();
		if (modCompilerFlags != null)
			a += " " + modCompilerFlags;

		return a;
	}

	protected String getArchiver()
	{
		if (modArchiver == null)
			return getGlobalArchiver();
		return modArchiver;
	}
	protected String getArchiverFlags()
	{
		String a = "";
		if (getGlobalArchiverFlags() == null)
			return null;
		a += getGlobalArchiverFlags();

		if (modArchiverFlags != null)
			a += " " + modArchiverFlags;

		return a;
	}

	protected String getLinker()
	{
		if (modLinker == null)
			return getGlobalLinker();
		return modLinker;
	}
	protected String getLinkerFlags()
	{
		String a = "";
		if (getGlobalLinkerFlags() == null)
			return null;
		a += getGlobalLinkerFlags();

		if (modLinkerFlags != null)
			a += " " + modLinkerFlags;

		return a;
	}

	public void compile()
	{
	}

	public void compress()
	{
	}

	public void link()
	{
	}
}
