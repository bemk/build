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
package ui;

import java.util.Iterator;

public class CLIWarning extends CLI {
	private static CLIWarning instance = null;

	public static CLIWarning getInstance()
	{
		instanceLock.lock();
		if (instance == null)
			instance = new CLIWarning();
		instanceLock.unlock();
		return instance;
	}

	private CLIWarning()
	{
		super();
		this.prefix = "[ WARNING ] ";
	}

	/** @0verride */
	public void run()
	{
		while (!finished)
		{
			this.getLock();
			Iterator<String> i = out.iterator();
			while (i.hasNext())
			{
				System.err.print(i.next());
				i.remove();
			}
			this.unlock();
		}
	}
}
