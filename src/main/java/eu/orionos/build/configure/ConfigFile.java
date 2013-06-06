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
package eu.orionos.build.configure;

import java.util.Iterator;
import java.util.Set;

import eu.orionos.build.Syntax;
import eu.orionos.build.ui.CLI;

public class ConfigFile {
	private String globalFlags[][];
	private String binDir = null;
	private boolean parsed = false;

	private static final int FLAGFIELD = 0;
	private static final int VALUEFIELD = 1;

	public ConfigFile(Set<String> flags)
	{
		globalFlags = new String[flags.size()][2];
		Iterator<String> i = flags.iterator();
		int j = 0;
		while (i.hasNext())
		{
			String flag = i.next();
			globalFlags[j][FLAGFIELD] = flag;
			globalFlags[j][VALUEFIELD] = "false";
			j++;
		}
		this.ask();
	}

	private void ask()
	{
		parsed = true;
		int i = 0;
		binDir = CLI.getInstance().readline("Set the binary output directory: ");
		for (; i < globalFlags.length; i++)
		{
			globalFlags[i][VALUEFIELD] = CLI.getInstance().readboolean("Set flag " + globalFlags[i][FLAGFIELD] + "?") ? "true" : "false";
		}
	}

	public String toString()
	{
		if (!parsed)
			return null;

		StringBuilder json = new StringBuilder("{\n\"global\" : [");
		boolean firstField = true;
		for (int i = 0; i < globalFlags.length; i++)
		{
			if (globalFlags[i][VALUEFIELD].equals("true")){
				if (!firstField)
					json.append(", ");
				else
					firstField = false;
				json.append("\"");
				json.append(globalFlags[i][FLAGFIELD]);
				json.append("\"");
			}
		}
		json.append("],\n");
		json.append("\"");
		json.append(Syntax.CONFIG_BUILD_DIR);
		json.append("\" : \"");
		json.append(binDir);
		json.append("\"\n");
		json.append("}\n");
		return json.toString();
	}
}
