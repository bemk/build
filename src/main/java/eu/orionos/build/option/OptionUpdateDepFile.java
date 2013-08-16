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
package eu.orionos.build.option;

import eu.orionos.build.Config;

public class OptionUpdateDepFile extends Option {

	private OptionUpdateDepFile(char c, String s, boolean operands) {
		super(c, s, operands, "", "Add newly added build flags to the build file. (This does will likely not put them in the right place so manual editing will remain necessary.)");
	}

	public OptionUpdateDepFile()
	{
		this(' ', "update-depfile", false);
	}

	@Override
	public void option() {
		Config.getInstance().updateDepFile(true);
	}

}
