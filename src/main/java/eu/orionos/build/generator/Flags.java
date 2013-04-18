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

import java.util.HashMap;

public class Flags extends Field {
	private String global_compiler_flags = "";
	private String global_linker_flags = "";
	private String global_archiver_flags = "";

	private String module_compililer_flags = "";
	private String module_linker_flags = "";
	private String module_archiver_flags = "";

	private HashMap<String, String> dynamic_compiler_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_linker_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_archiver_flags = new HashMap<String, String>();

	private HashMap<String, String> dynamic_module_compiler_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_module_linker_flags = new HashMap<String, String>();
	private HashMap<String, String> dynamic_module_archiver_flags = new HashMap<String, String>();

	public Flags()
	{
		System.out.print("Do you want to add to the global flags?");
		if (askBoolean())
		{
			System.out.println("You fool!");
		}
		System.out.print("Do you want to add something to the module wide flags?");
		if (askBoolean())
		{
			System.out.println("Haven't you learnt by now?");
		}
		System.out.print("Do you want to add to the global flags conditionally?");
		if (askBoolean())
		{
			System.out.println("My patience is starting to run out with you");
		}
		System.out.print("Do you want to add to the module flags conditionally?");
		if (askBoolean())
		{
			System.out.println("Ok, that's it, no more questions for you from this part of the module");
		}
	}

	@Override
	public String toJSON() {
		return "";
	}

}
