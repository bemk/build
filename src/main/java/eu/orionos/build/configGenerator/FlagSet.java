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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import eu.orionos.build.Semantics;

public class FlagSet extends Flag {
	protected boolean enabled = false;
	protected HashMap <Integer, Flag> flags = new HashMap<Integer, Flag>();

	public FlagSet(String key) {
		super(key);
	}

	@Override
	public void configure()
	{
		if (!mandatory)
		{
			this.enabled = getBoolean("Enable group ");
		}

		if (mandatory || enabled)
		{
			Set<Entry<Integer, Flag>> entries = flags.entrySet();
			Iterator<Entry<Integer, Flag>> i = entries.iterator();
			while (i.hasNext())
			{
				Entry<Integer, Flag> o = i.next();
				o.getValue().configure();
			}
		}
		this.configured = true;
	}

	@Override
	public void setEnabled()
	{
		this.mandatory = true;
	}

	public void addFlag(Flag f)
	{
		return;
	}

	public void parseJSON(JSONObject json)
	{
		String[] keys = JSONObject.getNames(json);

		for (int i = 0; i < keys.length; i++)
		{
			JSONObject flag = json.optJSONObject(keys[i]);
			JSONObject set = flag.optJSONObject(Semantics.FLAG_DEP_SET);
			JSONObject num = flag.optJSONObject(Semantics.FLAG_DEP_ENUM);

			Flag f = null;
			
			if (set != null)
			{
				f = new FlagSet(keys[i]);
				((FlagSet)f).parseJSON(set);
			}
			else if (num != null)
			{
				f = new EnumFlag(keys[i]);
				((EnumFlag)f).parseJSON(num);
			}
			else
			{
				f = new BooleanFlag(keys[i]);
			}

			f.setInfo(flag.optString(Semantics.FLAG_DEP_INFO));
			if (flag.optBoolean(Semantics.FLAG_DEP_MANDATORY))
			{
				f.setEnabled();
			}
			flags.put(i, f);
		}

		return;
	}

	@Override
	public ArrayList<String> getConfigFlags()
	{
		ArrayList<String> list = new ArrayList<String>();
		if (this.getEnabled())
		{
			Iterator<Entry<Integer, Flag>> i = flags.entrySet().iterator();
			while (i.hasNext())
			{
				Entry<Integer, Flag> e = i.next();
				list.addAll(e.getValue().getConfigFlags());
			}
		}
		return list;
	}

	@Override
	public String getDepFlags()
	{
		return null;
	}

	@Override
	public String toString()
	{
		if (configured && !getEnabled())
			return "";
		Set<Entry<Integer, Flag>> flags = this.flags.entrySet();
		Iterator <Entry<Integer, Flag>> i = flags.iterator();

		StringBuilder s = new StringBuilder();
		s.append("Set: ");
		s.append(key);
		s.append("\ninfo: ");
		s.append(info);
		s.append(": {\n");

		while (i.hasNext())
		{
			Entry<Integer, Flag> e = i.next();
			if (!this.configured || (e.getValue().configured() && e.getValue().getEnabled()))
			{
				s.append("[");
				s.append(e.getKey().toString());
				s.append("] ");
				String str = e.getValue().toString();
				str = str.replaceAll("\n", "\n\t");
				s.append(str);
				s.append("\n");
			}
		}

		s.append("}\n");

		return s.toString(); 
	}

	@Override
	public boolean getEnabled()
	{
		return (this.mandatory || this.enabled);
	}
}
