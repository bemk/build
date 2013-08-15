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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;

public class ConfigFile {
	private JSONObject configFile;

	public ConfigFile(Iterable<String> flags)
	{
		configFile = new JSONObject();

		try {
			configFile.put(Semantics.GLOBAL_DEFS, flags);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void write() throws IOException
	{
		File f = new File(Config.getInstance().getConfigFile());
		if (!f.exists())
			f.createNewFile();
		if (f.isDirectory())
			return;

		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);

		try {
			bw.write(this.configFile.toString(8));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw.close();
		fw.close();
	}
}
