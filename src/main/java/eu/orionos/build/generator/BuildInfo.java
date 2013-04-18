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

import java.util.ArrayList;

import eu.orionos.build.Syntax;

public class BuildInfo extends Field {
	private ArrayList<String> sFiles = new ArrayList<String>();
	private String lFile = "";
	private String aFile = "";
	private boolean toLink = false;
	private boolean toArchive = false;

	public BuildInfo()
	{
		System.out.println("\tHigh level compile info");
		System.out.print("Do you wish to link this module?");
		toLink = askBoolean();
		if (toLink)
		{
			System.out.print("What should be the linker output file name?");
			lFile = askString();
		}
		System.out.print("Should this module get archived?");
		toArchive = askBoolean();
		if (toArchive)
		{
			System.out.print("What should be the archiver output file name?");
			aFile = askString();
		}
		while (true)
		{
			System.out.println("Give the name of the next source file");
			System.out.print("leave blank for last source file");
			String s = askString();
			if (s.equals(""))
				break;
			else
				sFiles.add(s);
		}
		System.out.println("\tHigh level compile info done");
	}

	@Override
	public String toJSON() {
		String s = "\"" + Syntax.LINK + "\" : " + ((toLink) ? "true" : "false") + ",\n";
		if (toLink)
			s += "\"" + Syntax.LINKED + "\" : \"" + lFile + "\",\n";
		s += "\"" + Syntax.ARCHIVE + "\" : " + ((toArchive) ? "true" : "false") + ",\n";
		if (toArchive)
			s += "\"" + Syntax.ARCHIVED + "\" : \"" + aFile + "\",\n";
		s += "\"" + Syntax.SOURCE + "\" : [";
		String a[] = sFiles.toArray(new String[sFiles.size()]);
		for (int i = 0; i < sFiles.size(); i++)
		{
			s += "\"" + a[i] + "\"";
			if (i+1 < sFiles.size())
				s+= ", ";
		}
		s += "]";

		return s;
	}
}
