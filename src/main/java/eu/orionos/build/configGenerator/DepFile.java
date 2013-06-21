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
package eu.orionos.build.configGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIError;

public class DepFile {
	private FlagSet flags = new FlagSet("base");
	
	public DepFile() throws IOException
	{
		flags.setEnabled();
		flags.setInfo("");
	}

	public void generateDepFile(Set<String> flags)
	{
		return;
	}

	public void readDepFile() throws IOException
	{
		File f = new File (Config.getInstance().getDepFile());
		if (!f.exists())
			f.createNewFile();
		if (f.isDirectory())
			return;
		FileReader depReader = new FileReader(f);

		StringBuilder b = new StringBuilder();
		BufferedReader br = new BufferedReader(depReader);
		String line = br.readLine();
		for (; line != null; line = br.readLine())
		{
			b.append(line);
		}
		br.close();

		JSONObject JSON = new JSONObject(b.toString());

		flags.parseJSON(JSON);
	}

	public ConfigFile generateConfigFile()
	{
		flags.configure();
		Iterator <String> i = flags.getConfigFlags().iterator();
		while (i.hasNext())
		{
			String s = i.next();
			CLI.getInstance().writeline(s);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return flags.toString();
	}
}
