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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import eu.orionos.build.ErrorCode;
import eu.orionos.build.Syntax;

public class Flags extends Field {
	private String global_compiler_flags = "";
	private String global_linker_flags = "";
	private String global_archiver_flags = "";

	private String global_override_compiler_flags = "";
	private String global_override_linker_flags = "";
	private String global_override_archiver_flags = "";

	private String module_compililer_flags = "";
	private String module_linker_flags = "";
	private String module_archiver_flags = "";

	private HashMap<String, String> dynamic_compiler_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_linker_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_archiver_flags = new HashMap<String, String>();

	private HashMap<String, String> dynamic_module_compiler_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_module_linker_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_module_archiver_flags = new HashMap<String, String>();

	private void setFlags(HashMap<String, String> map, String type) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true)
		{
			System.out.println("Set the key for the dymamic " + type + " flag");
			System.out.println("Leave field blank and return to go to the next item");
			String key = in.readLine();
			if (key.equals(""))
				break;
			System.out.print("Set the flag for " + type + "-" + key);
			String flag = askString();
			map.put(key, flag);
		}
	}

	public Flags() throws IOException
	{
		System.out.print("Do you want to override the global flags for this module?");
		if (askBoolean())
		{
			System.out.println("Give the global overriding compiler flags");
			System.out.print("Leave blank and hit return to skip this option");
			global_override_compiler_flags = askString();
			System.out.println("Give the global overriding linker flags");
			System.out.print("Leave blank and hit return to skip this option");
			global_override_linker_flags = askString();
			System.out.println("Give the global overriding archiver flags");
			System.out.print("Leave blank and hit return to skip this option");
			global_override_archiver_flags = askString();
		}
		System.out.print("Do you want to add to the global flags?");
		if (askBoolean())
		{
			System.out.print("Give global compiler flags");
			global_compiler_flags = askString();
			System.out.print("Give global linker flags");
			global_linker_flags = askString();
			System.out.print("Give global archiver flags");
			global_archiver_flags = askString();
		}
		System.out.print("Do you want to add something to the module wide flags?");
		if (askBoolean())
		{
			System.out.print("Give module wide compiler flags");
			module_compililer_flags = askString();
			System.out.print("Give module wide linker flags");
			module_linker_flags = askString();
			System.out.print("Give module wide archiver flags");
			module_archiver_flags = askString();
		}
		System.out.print("Do you want to add to the global flags conditionally?");
		if (askBoolean())
		{
			try {
				setFlags(dynamic_compiler_flags, "global compiler");
				setFlags(dynamic_linker_flags, "global linker");
				setFlags(dynamic_archiver_flags, "global archiver");
			} catch (IOException e) {
				System.err.println("Something went wrong in capturing input");
				System.exit(ErrorCode.GENERIC);
			}
		}
		System.out.print("Do you want to add to the module flags conditionally?");
		if (askBoolean())
		{
			try {
				setFlags(dynamic_module_compiler_flags, "module wide compiler");
				setFlags(dynamic_module_linker_flags, "module wide linker");
				setFlags(dynamic_module_archiver_flags, "module wide archiver");
			} catch (IOException e) {
				System.err.println("Something went wrong in capturing input");
				System.exit(ErrorCode.GENERIC);
			}
		}
	}

	private String parseMap(Map<String,String> map)
	{
		String ret = "";
		boolean first = true;
		for (Map.Entry<String, String> e : map.entrySet())
		{
			if (!first)
				ret += ",";
			else
				first = true;
			ret += "{\"" + Syntax.CONFIG_GLOBAL_KEY+ "\" : \"" + e.getKey() + "\",";
			ret += "\"" + Syntax.CONFIG_GLOBAL_FLAGS + "\" : \"" + e.getValue() + "\"}";
		}
		return ret;
	}

	@Override
	public String toJSON() {
		String json = "";

		json += "\"" + Syntax.GLOBAL_COMPILER_FLAGS + "\" : \"" + global_compiler_flags + "\"";
		json += ",\n\"" + Syntax.GLOBAL_LINKER_FLAGS + "\" : \"" + global_linker_flags + "\"";
		json += ",\n\"" + Syntax.GLOBAL_ARCHIVER_FLAGS + "\" : \"" + global_archiver_flags + "\"";

		if (!module_compililer_flags.equals(""))
			json += ",\"\n" + Syntax.MOD_COMPILER_FLAGS + "\" : \"" + module_compililer_flags + "\"";
		if (!module_linker_flags.equals(""))
			json += ",\n\"" + Syntax.MOD_LINKER_FLAGS + "\" : \"" + module_linker_flags + "\"";
		if (!module_archiver_flags.equals(""))
			json += ",\n\"" + Syntax.MOD_ARCHIVER_FLAGS + "\" : \"" + module_archiver_flags + "\"";

		if (!global_override_compiler_flags.equals(""))
			json += ",\n\"" + Syntax.GLOBAL_COMPILER_OVERRIDE_FLAGS + "\" : \"" + global_override_compiler_flags + "\"";
		if (!global_override_linker_flags.equals(""))
			json += ",\n\"" + Syntax.GLOBAL_LINKER_OVERRIDE_FLAGS + "\" : \"" + global_override_linker_flags + "\"";
		if (!global_override_archiver_flags.equals(""))
			json += ",\n\"" + Syntax.GLOBAL_ARCHIVER_OVERRIDE_FLAGS + "\" : \"" + global_override_archiver_flags + "\"";

		String dcf = parseMap(dynamic_compiler_flags);
		String dlf = parseMap(dynamic_linker_flags);
		String daf = parseMap(dynamic_archiver_flags);
		if (!dcf.equals(""))
			json += ",\n\"" + Syntax.DYN_COMPILER_FLAGS + "\" : [" + dcf + "]";
		if (!dlf.equals(""))
			json += ",\n\"" + Syntax.DYN_LINKER_FLAGS + "\" : [" + dlf + "]";
		if (!daf.equals(""))
			json += ",\n\"" + Syntax.DYN_ARCHIVER_FLAGS + "\" : [" + daf + "]";

		String dmcf = parseMap(dynamic_module_compiler_flags);
		String dmlf = parseMap(dynamic_module_linker_flags);
		String dmaf = parseMap(dynamic_module_archiver_flags);
		if (!dmcf.equals(""))
			json += ",\n\"" + Syntax.DYN_MOD_COMPILER_FLAGS + "\" : [" + dmcf + "]";
		if (!dmlf.equals(""))
			json += ",\n\"" + Syntax.DYN_MOD_LINKER_FLAGS + "\" : [" + dmlf + "]";
		if (!dmaf.equals(""))
			json += ",\n\"" + Syntax.DYN_MOD_ARCHIVER_FLAGS + "\" : [" + dmaf + "]";

		return json;
	}

}
