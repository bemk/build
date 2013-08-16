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

import eu.orionos.build.Build;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.plexus.util.StringUtils;

public class Options {

	private ArrayList<Option> options = new ArrayList<Option>();
	//private static final int maxLongWidth = 12;

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
	
	public ArrayList<String> autocomplete(String str)
	{
		ArrayList<String> result = new ArrayList<String>();
		str = str.trim();
		
		if(str.charAt(0)=='-')
		{
			if(str.length()==1)
			{
				for (Option o : options)
				{
					result.add(String.valueOf(o.getShort()));
					result.add("-" + o.getLong());
				}
				return result;
			}
			else if(str.charAt(1) == '-')
			{
				str = str.substring(2);
				int strlen = str.length();
				for (Option o : options)
				{
					if(o.getLong().substring(0, strlen).equalsIgnoreCase(str))
						result.add(o.getLong());
				}
				return result;
			}
			else
			{
				char c = str.charAt(1);
				for (Option o : options)
				{
					if(o.getShort() == c)
						result.add(String.valueOf(o.getShort()));
				}
				return result;
			}
		}
		else
		{
			; // TODO: TBD
		}
		
		return null;
	}
	
	public String help()
	{
		StringBuilder strbuild = new StringBuilder();
		strbuild.append("OPTIONS:\n");
		String[] strings = new String[options.size()];

		int width = Build.terminalWidth();
		int maxLongWidth = (width/2) - (4+7); // The start of the description may not be more to the right than half the screen width.
		int length = 0;
		int i = 0;
		for (Option o : options)
		{
			String str = o.getLong() + " " + o.getParameters();
			
			int thisLength = str.length();
			if( (thisLength > length) && (thisLength < maxLongWidth) )
				length = thisLength;
			
			strings[i] = str;
			i++;
		}
		length++; // We want at least one space.
		
		if(length > maxLongWidth)
			length = maxLongWidth;
		
		width -= (4 + 7 + length);

		i = 0;
		int strlen;
		for (Option o : options)
		{
			strbuild.append(StringUtils.repeat(" ", 4) + ((o.getShort()==' ')?"  ":("-"+o.getShort())) + " | --" + strings[i]);
			if (strings[i].length() >= length)
				strbuild.append("\n" + StringUtils.repeat(" ", 4+7+length));
			else
				strbuild.append(StringUtils.repeat(" ", length - strings[i].length()));

			String s = o.getInfo();
			strlen = 0;
			for(char c : s.toCharArray())
			{
				if(c == '\n')
				{
					strlen = 0;
					strbuild.append("\n" + StringUtils.repeat(" ", 4+7+length));
				}
				else
				{
					if(strlen >= width)
					{
						strlen = strbuild.length();
						int place = strlen-1;
						while( (place>0) && (strbuild.charAt(place)!=' ') )
						{
							place--;
						}
						strlen = strlen-place-1;
						strbuild.insert(place, "\n" + StringUtils.repeat(" ", 4+7+length-1));
					}
					else
					{
						strlen++;
					}
					strbuild.append(c);
				}
			}
			strbuild.append('\n');

			i++;
		}
		
		return strbuild.toString();
	}
}
