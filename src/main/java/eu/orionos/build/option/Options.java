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

import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Options {

	private ArrayList<Option> options = new ArrayList<Option>();

	public Options(String args[]) throws FileNotFoundException, IOException
	{
		int i = 0;
		if (args.length == 0)
			return;

		options.add(new OptionHelp(this));
		options.add(new OptionHelp('u', "usage", this));
		options.add(new OptionVersion());
		options.add(new OptionSilent());
		options.add(new OptionVerbose());
		options.add(new OptionClean());
		options.add(new OptionConfigure());
		options.add(new OptionConfig());
		options.add(new OptionTask());
		options.add(new OptionCflags());
		options.add(new OptionLDflags());
		options.add(new OptionAflags());
		options.add(new OptionGenModule());
		options.add(new OptionDepFile());
		options.add(new OptionGenDepfile());
		options.add(new OptionUpdateDepFile());
		options.add(new OptionAllYes());
		options.add(new OptionAllNo());
		options.add(new OptionRandom());

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
							if (op.operands() && i+1 < args.length)
								op.operand(args[++i]);
							else if (op.operands())
							{
								System.err.println("Option " + a + " expects an argument");
								System.exit(ErrorCode.OPTION_UNSPECIFIED);
							}
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
						if (op.operands() && i + 1 < args.length)
							op.operand(args[++i]);
						else if (op.operands())
						{
							System.err.println("Option " + args[i] + " expects an operand");
							System.exit(ErrorCode.OPTION_UNSPECIFIED);
						}
						op.option();
						break nextarg;
					}
				}
				System.err.println(args[i] + " Invalid option!");
				new OptionHelp(this).option();
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
