/*  Build - Hopefully a simple build system
    Copyright (C)
        Michel Megens  <dev@michelmegens.net>

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

public class OptionColors extends Option
{
	public OptionColors(char shorthand, String operator)
	{
		super(shorthand, operator, false, "", "Enable XTerm colours in the terminal output.");
	}

	public OptionColors()
	{
		super('l', "colors", false, "", "Enable XTerm colors in the terminal output.");
	}

	@Override
	public void option()
	{
		Config.getInstance().setColors(true);
	}
}
