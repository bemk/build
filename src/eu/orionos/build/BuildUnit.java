package eu.orionos.build;
import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class BuildUnit {
	public static final int SUCCESS = 0;
	public static final int NO_FILE = 1;
	public static final int NO_BIN = 2;
	public static final int DISABLED = 3;

	private BuildUnit parent;
	private ArrayList<BuildUnit> childUnits = new ArrayList<BuildUnit>();
	private JSONArray files;
	private JSONArray ldfiles;
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
		if (files != null)
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
		ldfiles      = (JSONArray) o.get("ldfile");
		dynamic      = (JSONArray) o.get("dyn");

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
						if (!obj.get("type").equals("disabled"))
						{
							compilerOpts = (String)obj.get("cflags");
							linkerOpts   = (String)obj.get("ldflags");
							arOpts       = (String)obj.get("aflags");
							compress     = (boolean)obj.get("compress");
						}
					}
				}
			}
		}

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

		Iterator<BuildUnit> ib = childUnits.iterator();
		while(ib.hasNext())
		{
			BuildUnit b = ib.next();
			b.compile();
		}

		Runtime r = Runtime.getRuntime();

		if (files != null)
		{
			@SuppressWarnings("unchecked")
			Iterator<String> f = files.iterator();
			String[] c = {getCompiler(), getCompilerOpts(), "-o", "", ""};
			String[] a = {getAr(), getArOpts(), out, ""};
			while (f.hasNext())
			{
				String file = f.next();
				String ofile = this.pwd + "/" + file.substring(0, file.lastIndexOf('.')) + ".o";
				c[c.length-2] = ofile;
				c[c.length-1] = this.pwd + "/" + file;
				Process p = r.exec(c);
				if (p.waitFor() != 0)
					throw new FailedException(ofile);

				if (compress)
				{
					a[a.length-1] = ofile;
					p = r.exec(a);
					if (p.waitFor() != 0)
						throw new FailedException(ofile);
				}
			}
		}
		if (!compress)
			return link();

		return SUCCESS;
	}

	public int link() throws FailedException, IOException, InterruptedException
	{
		if (getLinker() == null || getLinkerOpts() == null)
			return NO_BIN;

		ArrayList<String> cmdDyn = new ArrayList<String>();
		cmdDyn.add(getLinker());
		if (getLinkerOpts() != null && !getLinkerOpts().equals(""))
		{
			cmdDyn.add(getLinkerOpts());
		}
		cmdDyn.add("-o");
		cmdDyn.add(out);

		@SuppressWarnings("unchecked")
		Iterator<String> i = ldfiles.iterator();
		while (i.hasNext())
			cmdDyn.add(i.next());

		String[] cmd = new String[cmdDyn.size()];
		Iterator<String> I = cmdDyn.iterator();
		int idx = 0;
		while (I.hasNext())
		{
			cmd[idx] = (String) I.next();
			idx++;
		}
		Process p = Runtime.getRuntime().exec(cmd);
		if (p.waitFor() != 0)
			throw new FailedException(out);

		return SUCCESS;
	}

	public int clean()
	{
		Iterator<BuildUnit> i = childUnits.iterator();
		while (i.hasNext())
		{
			i.next().clean();
		}
		Runtime r = Runtime.getRuntime();
		if (files != null)
		{
			Iterator<String> j = files.iterator();
			while (j.hasNext())
			{
				String file = j.next();
				String ofile = this.pwd + "/" + file.substring(0, file.lastIndexOf('.')) + ".o";
				String cmd[] = {"rm", "-fv", ofile};
				try {
					r.exec(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String c[] = {"rm", "-fv", out};
		try {
			r.exec(c);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return SUCCESS;
	}
}
