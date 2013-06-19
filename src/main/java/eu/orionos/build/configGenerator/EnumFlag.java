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

import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;

import eu.orionos.build.ui.CLI;

public class EnumFlag extends FlagSet {
	int choice = 0;

	public EnumFlag(String key) {
		super(key);
	}

	@Override
	public void configure()
	{
		if (!mandatory)
		{
			this.enabled = getBoolean("Enable ");
		}

		if (mandatory || enabled)
		{
			while (!configured)
			{
				Set<Entry<Integer, Flag>> entries = flags.entrySet();
				Iterator<Entry<Integer, Flag>> i = entries.iterator();
				while (i.hasNext())
				{
					Entry<Integer, Flag> e = i.next();
					CLI.getInstance().writeline("Index: " + e.getKey() + " : " + e.getValue().key);
				}
			}
			String answer = CLI.getInstance().readline("({0 .. n},info {0 .. n})").toLowerCase();
			try {
				choice = Integer.parseInt(answer);
				configured = true;
			}
			catch (NumberFormatException e)
			{
				if (answer.startsWith("info ") || answer.startsWith("i "))
				{
					answer = answer.replaceFirst("info ", "");
					answer = answer.replaceFirst("i ", "");
					try {
						int n = Integer.parseInt(answer);
						CLI.getInstance().writeline(flags.get(n).info);
					}
					catch (NumberFormatException ee)
					{
						CLI.getInstance().writeline("Illegal answer format");
					}
				}
				else
				{
					CLI.getInstance().writeline("Illegal answer format");
				}
			}
		}

		return;
	}

	@Override
	public void addFlag(Flag f)
	{
		
	}

	@Override
	public String getConfigFlags()
	{
		return null;
	}

	@Override
	public String getDepFlags()
	{
		return null;
	}

}
