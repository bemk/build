/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
        Steven vd Schoot   <stevenvdschoot@gmail.com> - 2013

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

import org.json.JSONException;
import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;

public class BooleanFlag extends Flag {
	private boolean value;
	private boolean ignore_autoconf;

	public BooleanFlag(String key, DepFile depfile, boolean ignore_autoconf)
	{
		super (key, depfile);
		this.ignore_autoconf = ignore_autoconf;
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
			{
				if (this.ignore_autoconf)
					value = false;
				else
					value = true;
			}
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
		try {
			o.put(Semantics.FLAG_DEP_MANDATORY, this.mandatory);
			o.put(Semantics.FLAG_DEP_INFO, this.info);
			o.put(Semantics.FLAG_DEP_IGNORE_AUTOCONF, this.ignore_autoconf);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
