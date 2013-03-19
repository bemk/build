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

package eu.orionos.build;

import java.util.ArrayList;
import java.util.Iterator;

public class Options {

	private ArrayList<Option> options = new ArrayList<Option>();

	public Options(String args[])
	{
		int i = 0;
		if (args.length == 0)
			return;
		
		for (; i < args.length; i++)
		{
			Iterator<Option> o = options.iterator();
			while (o.hasNext())
			{
				Option op = o.next();
				char c[] = {op.getShort()};
				if (args[i].equals(op.getLong()) || args[i].equals(new String(c)))
					op.option();
			}
		}
	}
}
