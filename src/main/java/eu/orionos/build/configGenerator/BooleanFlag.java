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

import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;

public class BooleanFlag extends Flag {
	private boolean value;

	public BooleanFlag(String key, DepFile depfile)
	{
		super (key, depfile);
	}

	public void configure()
	{
		if (this.mandatory)
			value = true;
		else
		{
			if (Config.getInstance().allno_config())
				value = false;
			else if (Config.getInstance().allyes_config())
				value = true;
			else if (Config.getInstance().random_config())
				value = (Config.getInstance().getRandom(0, 1) == 0) ? false : true;
			else
				value = getBoolean("Enable flag ");
		}

		this.configured = true;
		return;
	}

	public void setEnabled()
	{
		this.configured = true;
		this.value = true;
		this.mandatory = true;
	}

	public ArrayList<String> getConfigFlags()
	{
		ArrayList<String> list = new ArrayList<String>();
		if (this.getEnabled())
			list.add(key);
		return list;
	}

	public JSONObject getDepFlags()
	{
		JSONObject o = new JSONObject();
		o.put(Semantics.FLAG_DEP_MANDATORY, this.mandatory);
		o.put(Semantics.FLAG_DEP_INFO, this.info);
		return o;
	}

	@Override
	public String toString()
	{
		return "Key: " + this.key + ", mandator: " + Boolean.toString(mandatory);
	}

	public boolean getEnabled()
	{
		return (this.mandatory || this.value);
	}
}
