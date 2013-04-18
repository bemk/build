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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.orionos.build.ErrorCode;

public class ModuleGenerator {
	public ModuleGenerator(String path)
	{
		Module m = new Module();
		String s = m.getJSON();

		System.out.println("Generating module");

		File f = new File(path);
		FileWriter fw;
		try {
			fw = new FileWriter(f);
			fw.write(s);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(ErrorCode.GENERIC);
		}
		System.out.println("[ OK ] " + path);
	}
}
