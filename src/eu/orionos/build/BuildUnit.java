package eu.orionos.build;
import java.io.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class BuildUnit {
	public static int NO_FILE = 1;
	public static int NO_BIN = 2;
	public static int NO_FUNCTION = 3;

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

	private boolean compress = false;

	private JSONArray dynamic;
	
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
		this.pwd = pwd;
		File f = new File(pwd);
		pwd = f.getAbsolutePath();
		unit = new FileReader(new File(pwd));

		if (unit == null)
			throw new FileNotFoundException();

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

		JSONArray deps = (JSONArray) o.get("depend");
		@SuppressWarnings("unchecked")
		Iterator<String> i = deps.iterator();
		while (i.hasNext())
		{
			String dep = i.next();
			try {
				this.childUnits.add(new BuildUnit(dep));
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}

		JSONArray style = (JSONArray)o.get("type");
		if (style != null)
		{
			@SuppressWarnings("unchecked")
			Iterator<JSONObject>j = style.iterator();
			while (j.hasNext())
			{
				JSONObject obj = j.next();
				if (((String)obj.get("type")).equals("static"))
				{
					compilerOpts = (String)obj.get("cflags");
					linkerOpts   = (String)obj.get("ldflags");
					arOpts       = (String)obj.get("aflags");
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

	public int compile() throws IOException
	{
		if (getCompiler() == null)
			return NO_BIN;

		Iterator<BuildUnit> ib = childUnits.iterator();
		while(ib.hasNext())
		{
			BuildUnit b = ib.next();
			b.compile();
		}

		Runtime r = Runtime.getRuntime();

		Iterator<String> f = files.iterator();
		String[] c = {getCompiler(), getCompilerOpts(), ""};
		while (f.hasNext())
		{
			String file = f.next();
			c[2] = file;
			r.exec(c);
		}
		
		return NO_FUNCTION;
	}

	public int compress()
	{
		if (getAr() == null)
			return NO_BIN;
		return NO_FUNCTION;
	}

	public int link()
	{
		if (getLinker() == null)
			return NO_BIN;
		return NO_FUNCTION;
	}
}
