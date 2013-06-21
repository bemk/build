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
package eu.orionos.build.moduleGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eu.orionos.build.Semantics;

public class Dependencies extends Field {
	private ArrayList<String> dependencies = new ArrayList<String>();
	private HashMap<String, String> dynamic_dependencies = new HashMap<String, String>();

	private void setDynDeps(HashMap<String, String> map) throws IOException
	{
		while (true)
		{
			System.out.println("Set the key for the dymamic dependency");
			System.out.println("Leave field blank and return to go to the next item");
			String key = askString();
			if (key.equals(""))
				break;
			System.out.print("Set the dependency for: " + key);
			String dep = askString();
			map.put(key, dep);
		}
	}

	private void setDeps(ArrayList<String> list) throws IOException
	{
		while (true)
		{
			System.out.println("Set the relative path to the next dependency");
			System.out.println("Press enter on a blank line to go on to the next stage");
			String s = askString();
			if (s.equals(""))
				break;
			list.add(s);
		}
	}

	public Dependencies() throws IOException
	{
		System.out.print("Do you want to set static dependencies?");
		if (askBoolean())
		{
			setDeps(dependencies);
		}
		System.out.print("Do you want to set dynamic dependencies?");
		if (askBoolean())
		{
			setDynDeps(dynamic_dependencies);
		}
	}

	private String parseArray(ArrayList<String> array)
	{
		String json = "";
		boolean first = true;
		for (String s : array.toArray(new String[array.size()]))
		{
			if (first)
				first = false;
			else
				json += ", ";
			json += "{\"" + Semantics.DEP_PATH + "\" : \"" + s + "\"}";
		}
		return json;
	}

	private String parseMap(Map<String, String> map)
	{
		String json = "";
		boolean first = true;
		for (Map.Entry<String, String> e : map.entrySet())
		{
			if (first)
				first = false;
			else
				json += ", ";
			json += "{\"" + Semantics.CONFIG_GLOBAL_KEY + "\" : \"" + e.getKey() + "\", ";
			json +=  "\"" + Semantics.DEP_PATH + "\" : \"" + e.getValue() + "\"}";
		}
		return json;
	}

	@Override
	public String toJSON() {
		String json = "";

		if (!dependencies.isEmpty())
		{
			json += "\"" + Semantics.DEP + "\" : [" + parseArray(dependencies) +"]";
		}
		if (!dynamic_dependencies.isEmpty())
		{
			if (!dependencies.isEmpty())
				json += ",\n";
			json += "\"" + Semantics.DYN_DEP + "\" : [" + parseMap(dynamic_dependencies) + "]";
		}

		return json;
	}

}
