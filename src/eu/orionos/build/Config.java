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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Config {
	private static JSONObject conf;
	private static Config instance;
	private boolean silent = false;
	private boolean verbose = false;
	private String buildFile = "main.build";
	private boolean clean = false;
	
	public static Config getInstance()
	{
		return instance;
	}

	public static Config getInstance(String conf) throws FileNotFoundException, IOException, JSONException
	{
		if (instance == null)
			instance = new Config(conf);
		return instance;
	}

	private Config(String conf) throws FileNotFoundException, IOException, JSONException
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(conf)));
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append('\n');
		}
		Config.conf = new JSONObject(stringBuilder.toString());
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
		if (conf != null)
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
}
