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
package eu.orionos.build.configGenerator;

import eu.orionos.build.ui.CLI;

public abstract class Flag {
	protected String key;
	protected String info;
	protected boolean configured = false;
	protected boolean mandatory = false;

	public Flag(String key)
	{
		this.key = key;
	}

	public Flag(String key, String info)
	{
		this.key = key;
		this.info = info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public abstract void configure();
	public abstract void setEnabled();
	public abstract String getConfigFlags();
	public abstract String getDepFlags();

	protected boolean getBoolean(String msg)
	{
		boolean finished = false;
		boolean val = false;
		while (!finished)
		{
			String Answer = CLI.getInstance().readline(msg + this.key + "(Yes/No/Info)").toLowerCase();
			if (Answer.equals("y") || Answer.equals("yes"))
			{
				finished = true;
				val = true;
			}
			else if (Answer.equals("n") || Answer.equals("no"))
			{
				finished = true;
				val = false;
			}
			else if (Answer.equals("i") || Answer.equals("info"))
			{
				CLI.getInstance().writeline(info);
			}
		}
		return val;
	}
}
