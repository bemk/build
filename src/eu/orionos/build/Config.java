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
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {
	private JSONObject conf;
	private static Config instance;
	private boolean silent = false;
	private boolean verbose = false;
	private String buildFile = "main.build";
	private String configFile = null;
	private boolean clean = false;
	private boolean configured = false;
	private File buildDir = null;
	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private int threads = 4;

	private void setConfigFile(String conf) throws FileNotFoundException, IOException, ParseException
	{
		this.configFile = conf;
		File f = new File(configFile);
		if (!f.exists())
		{
			return;
		}
		try {
			FileReader fr = new FileReader(f);
			this.conf = (JSONObject) new JSONParser().parse(fr);
			fr.close();
		} catch (ParseException e) {
			System.err.println("File " + configFile + " can't be parsed!");
			this.conf = null;
			this.configFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Config getInstance()
	{
		if (instance == null)
			instance = new Config();
		return instance;
	}

	public static Config getInstance(String conf) throws FileNotFoundException, IOException, ParseException
	{
		if (instance == null)
			instance = new Config();
		instance.setConfigFile(conf);
		return instance;
	}
	
	public void override(String conf) throws FileNotFoundException, IOException, ParseException
	{
		this.setConfigFile(conf);
	}

	private Config()
	{
	}

	public void configure()
	{
		System.err.println("Configuration options should be set here!!!");
	}

	public boolean silent()
	{
		return this.silent;
	}
	public void silent(boolean silent)
	{
		this.silent = silent;
	}
	public boolean verbose()
	{
		return this.verbose;
	}
	public void verbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	public void buildFile(String buildFile)
	{
		this.buildFile = buildFile;
	}
	public String buildFile()
	{
		return this.buildFile;
	}

	public JSONObject get(String key)
	{
		if (conf.containsKey(key))
			return (JSONObject)conf.get(key);
		return null;
	}
	public void setClean()
	{
		this.clean = true;
	}
	public boolean getClean()
	{
		return this.clean;
	}

	public boolean getDefined(String key)
	{
		if (!conf.containsKey(Syntax.GLOBAL_DEFS))
			return false;

		JSONArray a = (JSONArray) conf.get(Syntax.GLOBAL_DEFS);
		@SuppressWarnings("rawtypes")
		Iterator i = a.iterator();

		while (i.hasNext())
		{
			String s = (String) i.next();
			if (s.equals(key))
				return true;
		}
		return false;
	}
	public boolean getModuleDefined(String module, String key)
	{
		if (!conf.containsKey(module))
			return false;

		JSONArray a = (JSONArray) conf.get(module);
		@SuppressWarnings("rawtypes")
		Iterator i = a.iterator();

		while (i.hasNext())
		{
			String s = (String) i.next();
			if (s.equals(key))
				return true;
		}
		return false;
	}

	public void configured(boolean configured)
	{
		this.configured = configured;
	}
	public boolean configured()
	{
		return this.configured;
	}
	public boolean hasConf()
	{
		if (this.conf == null)
			return false;
		return true;
	}
	public JSONArray getGlobalFlags()
	{
		return (JSONArray) conf.get(Syntax.GLOBAL_DEFS);
	}
	public JSONArray getModuleFlags(String key)
	{
		return (JSONArray) conf.get(key);
	}
	public boolean RegisterModule(Module m)
	{
		if (modules.containsKey(m.getName()))
			return false;
		
		modules.put(m.getName(), m);
		return true;
	}
	public String getBuildDir()
	{
		if (buildDir == null)
		{
			String s = (String) conf.get(Syntax.CONFIG_BUILD_DIR);
			buildDir = new File(s);
		}
		if (!buildDir.exists())
		{
			buildDir.mkdir();
		}
		if (!buildDir.isDirectory())
		{
			System.err.println("The build directory specified is not a directory!");
			System.exit(ErrorCode.FILE_NOT_FOUND);
		}
		return buildDir.getAbsolutePath();
	}
	public int threads()
	{
		return this.threads;
	}
	public void threads(int threads)
	{
		this.threads = threads;
	}
}
