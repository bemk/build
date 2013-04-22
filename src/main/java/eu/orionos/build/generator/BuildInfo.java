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

import java.io.IOException;
import java.util.ArrayList;

import eu.orionos.build.Syntax;

public class BuildInfo extends Field {
	private ArrayList<String> sFiles = new ArrayList<String>();
	private String lFile = "";
	private String aFile = "";
	private boolean toLink = false;
	private boolean toArchive = false;

	public BuildInfo() throws IOException
	{
		System.out.print("Do you wish to link this module?");
		toLink = askBoolean();
		if (toLink)
		{
			System.out.print("Give the name of the linked output file");
			lFile = askString();
		}
		System.out.print("Should this module get archived?");
		toArchive = askBoolean();
		if (toArchive)
		{
			System.out.print("Give the name of the archived output file");
			aFile = askString();
		}
		while (true)
		{
			System.out.println("Give the name of the next source file");
			System.out.print("Press enter with a blank line to continue to the next part");
			String s = askString();
			if (s.equals(""))
				break;
			else
				sFiles.add(s);
		}
	}

	@Override
	public String toJSON() {
		String s = "\"" + Syntax.LINK + "\" : " + ((toLink) ? "true" : "false") + ",\n";
		s += "\"" + Syntax.ARCHIVE + "\" : " + ((toArchive) ? "true" : "false") + ",\n";
		if (toLink)
			s += "\"" + Syntax.LINKED + "\" : \"" + lFile + "\",\n";
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
