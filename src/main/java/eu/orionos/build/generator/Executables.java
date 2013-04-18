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
package eu.orionos.build.generator;

import eu.orionos.build.Syntax;

public class Executables extends Field {
	private String global_compiler = "";
	private String module_compiler = "";
	private String global_linker = "";
	private String module_linker = "";
	private String global_archiver = "";
	private String module_archiver = "";

	public Executables()
	{
		System.out.print("Do you wish to set the global compiler fields?");
		if (askBoolean())
		{
			System.out.print("Please set the compiler (leave blank to ignore field)");
			global_compiler = askString();
			System.out.print("Please set the linker (leave blank to ignore field)");
			global_linker = askString();
			System.out.print("Please set the archiver (leave blank to ignore field)");
			global_archiver = askString();
		}
		System.out.print("Do you wish to override the compiler fields for this module?");
		if (askBoolean())
		{
			System.out.print("Please set the module wide compiler (leave blank to ignore field)");
			module_compiler = askString();
			System.out.print("Please set the module wide linker (leave blank to ignore field)");
			module_linker = askString();
			System.out.print("Please set hte module wide archiver (leave blank to ignore field)");
			module_archiver = askString();
		}
	}
	@Override
	public String toJSON() {
		String json = "";
		if (!global_compiler.equals(""))
			json += "\"" + Syntax.GLOBAL_COMPILER + "\" : \"" + global_compiler + "\"";
		if (!global_linker.equals(""))
			json += ",\n\"" + Syntax.GLOBAL_LINKER + "\" : \"" + global_linker + "\"";
		if (!global_archiver.equals(""))
			json += ",\n\"" + Syntax.GLOBAL_ARCHIVER + "\" : \"" + global_archiver + "\"";
		if (!module_compiler.equals(""))
			json += ",\n\"" + Syntax.MOD_COMPILER + "\" : \"" + module_compiler + "\"";
		if (!module_linker.equals(""))
			json += ",\n\"" + Syntax.MOD_LINKER + "\" : \"" + module_linker + "\"";
		if (!module_archiver.equals(""))
			json += ",\n\"" + Syntax.MOD_ARCHIVER + "\" : \"" + module_archiver + "\"";

		return json;
	}

}
