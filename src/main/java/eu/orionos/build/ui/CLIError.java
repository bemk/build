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
package eu.orionos.build.ui;

import eu.orionos.build.Config;

import net.michelmegens.xterm.Color;

public class CLIError extends CLI {
	private static CLIError instance = null;

	public static CLIError getInstance()
	{
		instanceLock.lock();
		if (instance == null)
			instance = new CLIError();
		instanceLock.unlock();
		return instance;
	}
	
	private CLIError()
	{
		super("CLIError");
		if(Config.getInstance().colors())
			this.prefix = Color.RED + "[ ERROR ] " + Color.DEFAULT;
		else
			this.prefix = "[ ERROR ] ";
	}
}
