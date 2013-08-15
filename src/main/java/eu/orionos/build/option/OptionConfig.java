/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013

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

import java.io.IOException;

import org.json.JSONException;

public class OptionConfig extends Option {

	public OptionConfig(char c, String s, boolean operands) {
		super(c, s, operands);
	}

	public OptionConfig()
	{
		this('\0', "config", true);
	}

	@Override
	public void option() {
		try {
			eu.orionos.build.Config.getInstance().override(this.operand);
		} catch (IOException e) {
			System.err.println("Something went wrong in switching config files!");
		} catch (JSONException e) {
			System.err.println("Something went wrong in switching config files!");
		}
	}

	@Override
	public String help() {
		return "   | --config [config file]\n\t\t\tSelect an alternative config file";
	}

}
