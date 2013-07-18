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
package eu.orionos.build.newModuleGenerator;

import java.io.*;

import org.json.JSONObject;

public class Module {
	String name;
	JSONObject json = new JSONObject();

	public Module(String path)
	{
		if (path == null)
			throw new NullPointerException();

		File f = new File(path);
		if (f.exists())
			parseFile(f);

		getValues();

		generateFile(f);
	}

	private void getValues()
	{
	}

	private void parseFile(File f)
	{
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			StringBuilder s = new StringBuilder();
			String tmp = "";

			while ((tmp = br.readLine()) != null)
			{
				s.append(tmp);
			}
			br.close();

			json = new JSONObject(s.toString());
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}
	}

	private void generateFile(File f)
	{
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(json.toString(8));
			fw.close();
		} catch (IOException e) {
		}
		return;
	}
}
