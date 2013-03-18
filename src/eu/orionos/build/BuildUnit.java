package eu.orionos.build;
import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class BuildUnit {
	public static int SUCCESS = 0;
	public static int NO_FILE = 1;
	public static int NO_BIN = 2;
	public static int DISABLED = 3;

	private BuildUnit parent;
	private ArrayList<BuildUnit> childUnits = new ArrayList<BuildUnit>();
	private JSONArray files;
	private String pwd;
	private String name;
	private String out;
	private FileReader unit;
	
	private String compiler;
	private String ar;
	private String linker;

	private String compilerOpts;
	private String arOpts;
	private String linkerOpts;

	private String buildType;

	private boolean compress = false;

	private JSONArray dynamic;
	private JSONArray dynamicConf;
	private JSONObject config;
	
	public String toString()
	{
		String s = "";
		s += "name:     " + name + "\n";
		s += "pwd:      " + pwd + "\n";
		s += "out:      " + out + "\n";
		s += "files:    " + files.toString() + "\n";
		s += "compress: " + compress + "\n";
		if (getCompiler() != null)
			s += "compiler: " + getCompiler() + "\n";
		if (getAr() != null)
			s += "ar:       " + getAr() + "\n";
		if (getLinker() != null)
			s += "linker:   " + getLinker() + "\n";
		if (getCompilerOpts() != null)
			s += "cflags:   " + getCompilerOpts() + "\n";
		if (getArOpts() != null)
			s += "aflags:   " + getArOpts() + "\n";
		if (getLinkerOpts() != null)
			s += "ldflags:  " + getLinkerOpts() + "\n";
		s += "buildtype:" + buildType + "\n";
		s += "dynamic:  " + dynamic.toString() + "\n";
		Iterator<BuildUnit> i = childUnits.iterator();
		while (i.hasNext())
		{
			s += "deps:     " + i.next().name + "\n";
		}
		return s;
	}
	public BuildUnit(String pwd) throws IOException, ParseException
	{
		this(pwd, null);
	}
	@SuppressWarnings("unchecked")
	public BuildUnit(String pwd, BuildUnit parent) throws IOException, ParseException
	{
		/* Set some file info */
		this.parent = parent;
		this.pwd = pwd;

		/* File point stuff */
		File f = new File(pwd);
		if (f.exists() == false)
			throw new FileNotFoundException();
		unit = new FileReader(f);
		
		this.pwd = f.getAbsolutePath();
		int len = this.pwd.lastIndexOf('/');
		this.pwd = this.pwd.substring(0, len);

		/* Parse info */
		JSONParser p = new JSONParser();
		JSONObject o = (JSONObject)p.parse(unit);

		name         = (String)    o.get("name");
		compiler     = (String)    o.get("compiler");
		ar           = (String)    o.get("ar");
		linker       = (String)    o.get("linker");
		out          = (String)    o.get("out");
		files        = (JSONArray) o.get("file");
		dynamic      = (JSONArray) o.get("dyn");
		compress     = (Boolean)   o.get("compress");

		/* Read config info */
		config = Config.getInstance().get(this.name);
		if (config != null)
		{
			buildType   = (String)config.get("type");
			dynamicConf = (JSONArray)config.get("dyn");
		}
		
		/* Resolve static dependencies */
		JSONArray deps = (JSONArray) o.get("depend");
		@SuppressWarnings("unchecked")
		Iterator<String> i = deps.iterator();
		while (i.hasNext())
		{
			String dep = i.next();
			String depPath = this.pwd + "/" + dep;
			try {
				this.childUnits.add(new BuildUnit(depPath, this));
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}

		/* Resolve dynamic dependencies */
		deps = (JSONArray) o.get("dyn");
		i = deps.iterator();
		while(i.hasNext() && dynamicConf != null)
		{
			String s = i.next();
			Iterator<String> finder = dynamicConf.iterator();
			if (finder == null)
				break;
			while(finder.hasNext())
			{
				if (s.equals(finder.next()))
				{
					String depPath = this.pwd + "/" + i.next();
					this.childUnits.add(new BuildUnit(depPath, this));
					break;
				}
				else
				{
					i.next();
				}
			}
		}

		/* Resolve dynamic configuration */
		JSONArray style = (JSONArray)o.get("type"); /* Read build file */
		if (style != null)
		{
			@SuppressWarnings("unchecked")
			Iterator<JSONObject>j = style.iterator(); /* Match the conf file */
			if (j != null)
			{
				while (j.hasNext())
				{
					JSONObject obj = j.next();
					if (((String)obj.get("type")).equals(buildType))
					{
						compilerOpts = (String)obj.get("cflags");
						linkerOpts   = (String)obj.get("ldflags");
						arOpts       = (String)obj.get("aflags");
					}
				}
			}
		}

		System.out.println(this.toString());
	}

	public String getCompiler()
	{
		if (compiler == null && parent != null)
			return parent.getCompiler();
		return compiler;
	}
	public String getCompilerOpts()
	{
		if (compilerOpts == null && parent != null)
			return parent.getCompilerOpts();
		return compilerOpts;
	}
	public String getAr()
	{
		if (ar == null && parent != null)
			return parent.getAr();
		return ar;
	}
	public String getArOpts()
	{
		if (arOpts == null && parent != null)
			return parent.getArOpts();
		return arOpts;
	}
	public String getLinker()
	{
		if (linker == null && parent != null)
			return parent.getLinker();
		return linker;
	}
	public String getLinkerOpts()
	{
		if (linkerOpts == null && parent != null)
			return parent.getLinkerOpts();
		return linkerOpts; 
	}

	public int compile() throws IOException, DisabledException, InterruptedException, FailedException
	{
		if (getCompiler() == null)
			return NO_BIN;

		if (buildType.equals("disabled"))
			throw new DisabledException(this.name);
		
		System.setProperty("user.dir", pwd);

		Iterator<BuildUnit> ib = childUnits.iterator();
		while(ib.hasNext())
		{
			BuildUnit b = ib.next();
			b.compile();
		}

		Runtime r = Runtime.getRuntime();

		@SuppressWarnings("unchecked")
		Iterator<String> f = files.iterator();
		String[] c = {getCompiler(), getCompilerOpts(), ""};
		while (f.hasNext())
		{
			String file = f.next();
			c[2] = file;
			Process p = r.exec(c);
			if (p.waitFor() != 0)
				throw new FailedException(this.name);
		}
		System.setProperty("user.dir", pwd);
		
		System.err.println("Do linking or compressing here!");
		
		return SUCCESS;
	}

	public int compress()
	{
		if (getAr() == null)
			return NO_BIN;
		return SUCCESS;
	}

	public int link()
	{
		if (getLinker() == null)
			return NO_BIN;
		return SUCCESS;
	}
}
