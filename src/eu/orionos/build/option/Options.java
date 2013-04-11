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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.parser.ParseException;

import eu.orionos.build.Config;

public class Options {

	private ArrayList<Option> options = new ArrayList<Option>();

	public Options(String args[]) throws FileNotFoundException, IOException, ParseException
	{
		int i = 0;
		if (args.length == 0)
			return;

		options.add(new OptionHelp(this));
		options.add(new OptionHelp('u', "usage", this));
		options.add(new OptionSilent());
		options.add(new OptionVerbose());
		options.add(new OptionClean());
		options.add(new OptionConfig());
		options.add(new OptionTask());

		for (; i < args.length; i++)
		{
			nextarg:
			if (args[i].startsWith("-") && !args[i].startsWith("--"))
			{
				String arg = args[i].substring(1);
				for (char a : arg.toCharArray())
				{
					Iterator<Option> o = options.iterator();
					while (o.hasNext())
					{
						Option op = o.next();
						if (a == op.getShort())
						{
							if (op.operands())
								op.operand(args[++i]);
							op.option();
							if (op.operands())
								break nextarg;
						}
					}
				}
			}
			else if (args[i].startsWith("--"))
			{
				Iterator<Option> o = options.iterator();
				while (o.hasNext())
				{
					Option op = o.next();
					String l = "--" + op.getLong();
					if (args[i].equals(l))
					{
						if (op.operands())
							op.operand(args[++i]);
						op.option();
						break;
					}
				}
			}
			else if (args[i].endsWith(".build"))
			{
				Config.getInstance().buildFile(args[i]);
			}
			else
			{
				System.err.println(args[i] + " Invalid option!");
				new OptionHelp(this).option();
			}
		}
	}
	
	public void help()
	{
		System.out.println("OPTIONS");
		for (Option o : options)
		{
			String s = o.help();
			if (s.length() > 0);
				System.out.println("\t" + s);
		}
	}
}
