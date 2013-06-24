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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import eu.orionos.build.Semantics;
import eu.orionos.build.ui.CLI;

public class EnumFlag extends FlagSet {
	int choice = 0;

	public EnumFlag(String key, DepFile depfile) {
		super(key, depfile);
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
			while (true)
			{
				CLI.getInstance().writeline("Enum info: " + this.info);
				Set<Entry<Integer, Flag>> entries = flags.entrySet();
				Iterator<Entry<Integer, Flag>> i = entries.iterator();
				while (i.hasNext())
				{
					Entry<Integer, Flag> e = i.next();
					CLI.getInstance().writeline("Index: " + e.getKey() + " : " + e.getValue().key);
				}
				String answer = CLI.getInstance().readline("({0 .. n},info {0 .. n})").toLowerCase();
				try {
					choice = Integer.parseInt(answer);
					if (flags.get(new Integer(choice)) == null)
						continue;
					else
					{
						flags.get(new Integer(choice)).setEnabled();
						break;
					}
				}
				catch (NumberFormatException e)
				{
					if (answer.startsWith("info") || answer.startsWith("i"))
					{
						answer = answer.replaceFirst("info", "");
						answer = answer.replaceFirst("i", "");
						answer = answer.replaceAll(" ", "");
						if (!answer.equals(""))
						{
							try {
								int n = Integer.parseInt(answer);
								CLI.getInstance().writeline("Option info: " + flags.get(n).info);
							}
							catch (NumberFormatException ee)
							{
								CLI.getInstance().writeline("Illegal answer format (2)");
							}
						}
					}
					else
					{
						CLI.getInstance().writeline("Illegal answer format");
					}
				}
			}
		}
		configured = true;

		return;
	}

	@Override
	public ArrayList<String> getConfigFlags()
	{
		ArrayList<String> list = new ArrayList<String>();
		if (this.getEnabled() && this.configured())
			list.addAll(flags.get(new Integer(choice)).getConfigFlags());
		return list;
	}

	@Override
	public JSONObject getDepFlags()
	{
		JSONObject o = new JSONObject();
		JSONObject set = new JSONObject();

		o.put(Semantics.FLAG_DEP_MANDATORY, this.mandatory);
		o.put(Semantics.FLAG_DEP_INFO, this.info);
		o.put(Semantics.FLAG_DEP_ENUM, set);

		Set<Integer> keys = flags.keySet();
		Iterator<Integer> i = keys.iterator();
		while (i.hasNext())
		{
			Integer key = i.next();
			Flag f = flags.get(key);
			set.put(f.key, f.getDepFlags());
		}
		return o;
	}

	@Override
	public String toString()
	{
		if (configured && !enabled)
			return "";
		Set<Entry<Integer, Flag>> flags = this.flags.entrySet();
		Iterator <Entry<Integer, Flag>> i = flags.iterator();

		StringBuilder s = new StringBuilder();
		s.append("Enum: ");
		s.append(key);
		s.append("\ninfo: ");
		s.append(info);
		s.append(": {\n");

		if (!configured)
		{	
			while (i.hasNext())
			{
				Entry<Integer, Flag> e = i.next();
				s.append("[");
				s.append(e.getKey().toString());
				s.append("] ");
				String str = e.getValue().toString();
				str = str.replaceAll("\n", "\n\t");
				s.append(str);
				s.append("\n");
			}
		}
		else
		{
			s.append("[");
			s.append(choice);
			s.append("] ");
			String str = this.flags.get(new Integer(choice)).toString();
			str = str.replaceAll("\n", "\n\t");
			s.append(str);
		}

		s.append("}\n");

		return s.toString(); 
	}
}
