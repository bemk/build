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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;

public class DepFile {
	private FlagSet flags = null;
	private HashMap<String, Flag> flagMap = new HashMap<String, Flag>();
	private String build_root = null;
	private String defHeader = null;

	public DepFile() throws IOException {
		flags = new FlagSet("base", this);
		flags.setEnabled();
		flags.setInfo("");
	}

	public JSONObject generateDepFile(Set<String> flags) {
		JSONObject o = new JSONObject();
		Iterator<String> i = flags.iterator();

		try {
			o.put(Semantics.DEP_BUILD_ROOT, Config.getInstance().buildFile());
			o.put(Semantics.GLOBAL_DEFINE, Config.getInstance().def_hdr());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (i.hasNext()) {
			String key = i.next();
			JSONObject value = new JSONObject();

			try {
				value.put(Semantics.FLAG_DEP_MANDATORY, false);
				value.put(Semantics.FLAG_DEP_INFO, "");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				o.put(key, value);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return o;
	}

	public JSONObject updateDepFile(Set<String> flags) {
		Iterator<String> i = flags.iterator();

		while (i.hasNext()) {
			String key = i.next();
			if (this.getFlag(key) == null) {
				this.flags.addFlag(new BooleanFlag(key, this, false));
			}
		}
		JSONObject o = this.flags.getDepFlags().optJSONObject(
				Semantics.FLAG_DEP_SET);
		try {
			o.put(Semantics.DEP_BUILD_ROOT, build_root);
			o.put(Semantics.GLOBAL_DEFINE, defHeader);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}

	public void readDepFile() throws IOException, DepfileException {
		File f = new File(Config.getInstance().getDepFile());
		if (f.isDirectory() || !f.exists())
			throw new DepfileException();
		FileReader depReader = new FileReader(f);

		StringBuilder b = new StringBuilder();
		BufferedReader br = new BufferedReader(depReader);
		String line = br.readLine();
		for (; line != null; line = br.readLine()) {
			b.append(line);
		}
		br.close();

		JSONObject JSON;
		try {
			JSON = new JSONObject(b.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		parseJSON(JSON);
	}

	public void parseJSON(JSONObject o) {
		flags.parseJSON(o);
		build_root = o.optString(Semantics.DEP_BUILD_ROOT);
		defHeader = o.optString(Semantics.GLOBAL_DEFINE);
	}

	public ConfigFile generateConfigFile() {
		flags.configure();

		ConfigFile c = new ConfigFile(flags.getConfigFlags());
		if (this.build_root != null) {
			c.setBuildRoot(build_root);
		}
		if (this.defHeader != null) {
			c.setDefHeader(this.defHeader);
		}

		return c;
	}

	public void registerFlag(Flag flag) {
		String key = flag.key;
		flagMap.put(key, flag);
	}

	public Flag getFlag(String key) {
		return flagMap.get(key);
	}

	public String getBuildRoot() {
		return this.build_root;
	}

	public String getDefHeader() {
		return this.defHeader;
	}

	@Override
	public String toString() {
		return flags.toString();
	}
}
