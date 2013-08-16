/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Config {
	private JSONObject conf;
	private static final Config instance = new Config();
	private boolean updateDepFile = false;
	private boolean silent = false;
	private boolean verbose = false;
	private String buildFile = "main.build";
	private String configFile = ".config";
	private String cflags = "";
	private String ldflags = "";
	private String aflags = "";
	private boolean clean = false;
	private boolean genDepFile = false;
	private boolean genConfigFile = false;
	private boolean allyes_config = false;
	private boolean allno_config = false;
	private boolean random_config = false;
	private File buildDir = null;
	private HashMap<String, Module> modules = new HashMap<String, Module>();
	private int threads = 4;
	private String depFile = "dep.flags";
	private Random randomSource = new Random();

	private void setConfigFile(String conf) throws FileNotFoundException, IOException
	{
		this.configFile = conf;
		File f = new File(configFile);
		if (!f.exists())
		{
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			reader.close();
			this.conf = new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {
			System.err.println("File " + configFile + " can't be parsed!");
			this.conf = null;
			this.configFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Config getInstance()
	{
		return instance;
	}
	public String getConfigFile()
	{
		return this.configFile;
	}

	public synchronized static Config getInstance(String conf) throws FileNotFoundException, IOException
	{
		instance.setConfigFile(conf);
		return instance;
	}
	
	public synchronized void override(String conf) throws FileNotFoundException, IOException
	{
		this.setConfigFile(conf);
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
		return conf.optJSONObject(key);
	}
	public void setClean(boolean clean)
	{
		this.clean = clean;
	}
	public boolean getClean()
	{
		return this.clean;
	}

	public boolean getDefined(String key)
	{
		if (conf == null)
			return false;
		if (!conf.has(Semantics.GLOBAL_DEFS))
			return false;
		JSONArray a;
		
		try {
			a = conf.getJSONArray(Semantics.GLOBAL_DEFS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		for (int i = 0; i < a.length(); i++) {
			try {
				if (key.equals(a.get(i)))
					return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	public boolean getModuleDefined(String module, String key)
	{
		if (conf == null)
			return false;
		if (!conf.has(module))
			return false;
		
		JSONArray a;
		try {
			a = conf.getJSONArray(module);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		for (int i = 0; i < a.length(); i++) {
			try {
				if (key.equals(a.get(i)))
					return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public synchronized void genDepFile(boolean value)
	{
		this.genDepFile = value;
	}
	public synchronized boolean genDepFile()
	{
		return this.genDepFile;
	}

	public synchronized void genConfigFile(boolean value)
	{
		this.genConfigFile = value;
	}
	public synchronized boolean genConfigFile()
	{
		return this.genConfigFile;
	}

	public synchronized void allyes_config(boolean value)
	{
		this.allyes_config = value;
	}
	public synchronized boolean allyes_config()
	{
		return this.allyes_config;
	}
	public synchronized void allno_config(boolean value)
	{
		this.allno_config = value;
	}
	public synchronized boolean allno_config()
	{
		return this.allno_config;
	}
	public synchronized void random_config(boolean value)
	{
		this.random_config = value;
	}
	public synchronized boolean random_config()
	{
		return this.random_config;
	}
	public synchronized boolean auto_config()
	{
		return allyes_config | allno_config | random_config;
	}

	public synchronized boolean hasConf()
	{
		if (this.conf == null && this.genDepFile == false)
			return false;
		return true;
	}
	public JSONArray getGlobalFlags()
	{
		return conf.optJSONArray(Semantics.GLOBAL_DEFS);
	}
	public JSONArray getModuleFlags(String key)
	{
		return conf.optJSONArray(key);
	}
	public boolean RegisterModule(Module m)
	{
		if (modules.containsKey(m.getName()))
			return false;
		
		modules.put(m.getName(), m);
		return true;
	}
	public synchronized String getBuildDir()
	{
		if (buildDir == null)
		{
			String s = (String) conf.optString(Semantics.CONFIG_BUILD_DIR);
			if (s == null || s.equals(""))
				s = "bin";
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

	public void cflags(String cflags)
	{
		this.cflags = cflags;
	}
	public String cflags()
	{
		return this.cflags;
	}
	public void aflags(String aflags)
	{
		this.aflags = aflags;
	}
	public String aflags()
	{
		return this.aflags;
	}
	public void ldflags(String ldflags)
	{
		this.ldflags = ldflags;
	}
	public String ldflags()
	{
		return this.ldflags;
	}
	public String getDepFile()
	{
		if (depFile == null || depFile.equals(""))
			return "dep.flags";

		return depFile;
	}
	public void setDepFile(String depFile)
	{
		this.depFile = depFile;
	}

	public void updateDepFile(boolean value)
	{
		this.updateDepFile = value;
	}
	public boolean updateDepFile() {
		return this.updateDepFile;
	}
	public int getRandom()
	{
		return randomSource.nextInt();
	}
	public int getRandom(int max)
	{
		return randomSource.nextInt(max);
	}
	public int getRandom(int min, int max)
	{
		return randomSource.nextInt(max - min + 1) + min;
	}
}
