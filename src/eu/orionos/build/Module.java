package eu.orionos.build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

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

	public Module(String path) throws FileNotFoundException, IOException, JSONException
	{
		this(path, null);
	}

	public Module(String path, Module parent) throws FileNotFoundException, IOException, JSONException
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
		int len = this.cwd.lastIndexOf('/');
		this.cwd = this.cwd.substring(0, len);

		/* Read global stuff into local variables for easier access */
		this.globalCompiler = module.optString(Syntax.GLOBAL_COMPILER, null);
		this.globalCompilerFlags = module.optString(Syntax.GLOBAL_COMPILER_FLAGS, null);
		this.globalLinker = module.optString(Syntax.GLOBAL_LINKER, null);
		this.globalLinkerFlags = module.optString(Syntax.GLOBAL_LINKER_FLAGS, null);
		this.globalArchiver = module.optString(Syntax.GLOBAL_ARCHIVER, null);
		this.globalArchiverFlags = module.optString(Syntax.GLOBAL_ARCHIVER_FLAGS, null);

		/* Get all the modular data in place */
		this.modCompiler = module.optString(Syntax.MOD_COMPILER, null);
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
