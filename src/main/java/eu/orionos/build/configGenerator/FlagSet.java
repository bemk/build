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

import org.json.JSONException;
import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;

public class FlagSet extends Flag {
	protected boolean enabled = false;
	protected boolean ignore_autoconf;
	protected HashMap <Integer, Flag> flags = new HashMap<Integer, Flag>();
	int mapKey = 0;

	public FlagSet(String key, DepFile depfile) {
		super(key, depfile);
	}

	public void configure()
	{
		if (!mandatory)
		{
			if (Config.getInstance().auto_config())
			{
				if (this.ignore_autoconf)
					this.enabled = false;
				else if (Config.getInstance().allyes_config())
					this.enabled = true;
				else if (Config.getInstance().random_config())
					this.enabled = Config.getInstance().getRandom(0, 1) == 0 ? true : false;
			}
			else
				this.enabled = getBoolean("Enable group ");
		}

		if (mandatory || enabled)
		{
			if (Config.getInstance().allno_config())
			{
				this.configured = true;
				return;
			}
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

	public void setEnabled()
	{
		this.mandatory = true;
		this.enabled = true;
	}

	public synchronized void addFlag(Flag f)
	{
		int key = mapKey++;

		flags.put(key, f);

		return;
	}

	public synchronized void parseJSON(JSONObject json)
	{
		String[] keys = JSONObject.getNames(json);
		this.ignore_autoconf = json.optBoolean(Semantics.FLAG_DEP_IGNORE_AUTOCONF);

		for (; mapKey < keys.length; mapKey++)
		{
			JSONObject flag = json.optJSONObject(keys[mapKey]);
			JSONObject set = flag.optJSONObject(Semantics.FLAG_DEP_SET);
			JSONObject num = flag.optJSONObject(Semantics.FLAG_DEP_ENUM);

			Flag f = null;
			
			if (set != null)
			{
				f = new FlagSet(keys[mapKey], this.depfile);
				((FlagSet)f).parseJSON(set);
			}
			else if (num != null)
			{
				f = new EnumFlag(keys[mapKey], this.depfile);
				((EnumFlag)f).parseJSON(num);
			}
			else
			{
				boolean autoconf_ignore = flag.optBoolean(Semantics.FLAG_DEP_IGNORE_AUTOCONF);
				f = new BooleanFlag(keys[mapKey], this.depfile, autoconf_ignore);
			}

			f.setInfo(flag.optString(Semantics.FLAG_DEP_INFO));
			if (flag.optBoolean(Semantics.FLAG_DEP_MANDATORY))
			{
				f.setEnabled();
			}
			flags.put(mapKey, f);
		}

		return;
	}

	public ArrayList<String> getConfigFlags()
	{
		ArrayList<String> list = new ArrayList<String>();
		if (this.getEnabled())
		{
			list.add(this.key);
			Iterator<Entry<Integer, Flag>> i = flags.entrySet().iterator();
			while (i.hasNext())
			{
				Entry<Integer, Flag> e = i.next();
				list.addAll(e.getValue().getConfigFlags());
			}
		}
		return list;
	}

	public JSONObject getDepFlags()
	{
		JSONObject o = new JSONObject();
		JSONObject set = new JSONObject();

		try {
			o.put(Semantics.FLAG_DEP_MANDATORY, this.mandatory);
			o.put(Semantics.FLAG_DEP_INFO, this.info);
			o.put(Semantics.FLAG_DEP_SET, set);
			o.put(Semantics.FLAG_DEP_IGNORE_AUTOCONF, this.ignore_autoconf);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<Integer> keys = flags.keySet();
		Iterator<Integer> i = keys.iterator();
		while (i.hasNext())
		{
			Integer key = i.next();
			Flag f = flags.get(key);
			try {
				set.put(f.key, f.getDepFlags());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return o;
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

	public boolean getEnabled()
	{
		return (this.mandatory || this.enabled);
	}
}
