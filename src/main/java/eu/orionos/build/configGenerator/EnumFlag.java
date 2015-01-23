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

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import net.michelmegens.xterm.Color;

import org.json.JSONException;
import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.Semantics;
import eu.orionos.build.ui.CLI;

public class EnumFlag extends FlagSet {
	int choice = 0;
	CLI cli = CLI.getInstance();

	public EnumFlag(String key, DepFile depfile) {
		super(key, depfile);
	}

	@Override
	public void configure() {
		if (!mandatory) {
			if (Config.getInstance().allyes_config()
					|| Config.getInstance().allno_config()
					|| Config.getInstance().random_config()) {
				if (this.ignore_autoconf) {
					this.enabled = false;
				} else {
					this.enabled = true;
				}
			} else {
				String question = "Enable ";
				if (Config.getInstance().colors()) {
					question = new StringBuilder(Color.BLUE).append(question).append(Color.DEFAULT).toString();
				}
				this.enabled = getBoolean(question);
			}
		}

		if (mandatory || enabled) {
			if (Config.getInstance().allyes_config()
					|| Config.getInstance().allno_config()
					|| Config.getInstance().random_config()) {
				/* This keeps the current choice the first one */
				this.configured = true;
				if (Config.getInstance().random_config())
					this.choice = Config.getInstance().getRandom(flags.size());
				flags.get(new Integer(choice)).setEnabled();
				return;
			}
			while (true) {
				String enumInfo = "Enum info: " + this.info;
				if (Config.getInstance().colors()) {
					enumInfo = new StringBuilder(Color.YELLOW).append(enumInfo)
							.append(Color.DEFAULT).toString();
				}
				cli.writeline(enumInfo);
				Set<Entry<Integer, Flag>> entries = flags.entrySet();
				Iterator<Entry<Integer, Flag>> i = entries.iterator();
				while (i.hasNext()) {
					Entry<Integer, Flag> e = i.next();
					StringBuilder tmp = new StringBuilder();
					if (Config.getInstance().colors()) {
						tmp.append(Color.GREEN);
					}
					tmp.append("Option ").append(e.getKey()).append(": ");
					if (Config.getInstance().colors()) {
						tmp.append(Color.DEFAULT);
					}
					tmp.append(e.getValue().key);
					cli.writeline(tmp.toString());
				}
				String question = "({0 .. n},info {0 .. n}) ";
				if (Config.getInstance().colors()) {
					question = new StringBuilder(Color.BLUE).append(question)
							.append(Color.DEFAULT).toString();
					;
				}
				String answer = CLI.getInstance().readline(question)
						.toLowerCase();
				try {
					choice = Integer.parseInt(answer);
					if (flags.get(new Integer(choice)) == null)
						continue;
					else {
						flags.get(new Integer(choice)).setEnabled();
						break;
					}
				} catch (NumberFormatException e) {
					if (answer.startsWith("info") || answer.startsWith("i")) {
						answer = answer.replaceFirst("info", "");
						answer = answer.replaceFirst("i", "");
						answer = answer.replaceAll(" ", "");
						if (!answer.equals("")) {
							try {
								int n = Integer.parseInt(answer);
								if (flags.containsKey(new Integer(n))) {
									StringBuilder out = new StringBuilder(
											"Option info: ").append(flags
											.get(n).info);
									if (Config.getInstance().colors()) {
										out = new StringBuilder(Color.YELLOW)
												.append(out).append(
														Color.DEFAULT);
									}
									cli.writeline(out.toString());
								} else {
									String output = "Requested information not available";
									if (Config.getInstance().colors()) {
										output = new StringBuilder(Color.YELLOW)
												.append(output)
												.append(Color.DEFAULT)
												.toString();
									}
									cli.writeline(output);
								}
							} catch (NumberFormatException ee) {
								String out = "Illegal answer format (2)";
								if (Config.getInstance().colors()) {
									out = new StringBuilder(Color.RED)
											.append(out).append(Color.DEFAULT)
											.toString();
								}
								cli.writeline(out);
							}
						}
					} else {
						String out = "Illegal answer format";
						if (Config.getInstance().colors()) {
							out = new StringBuilder(Color.RED).append(out)
									.append(Color.DEFAULT).toString();
						}
						CLI.getInstance().writeline(out);
					}
				}
			}
		}
		configured = true;

		return;
	}

	@Override
	public ArrayList<String> getConfigFlags() {
		ArrayList<String> list = new ArrayList<String>();
		if (this.getEnabled() && this.configured()) {
			list.add(this.key);
			list.addAll(flags.get(new Integer(choice)).getConfigFlags());
		}
		return list;
	}

	@Override
	public JSONObject getDepFlags() {
		JSONObject o = new JSONObject();
		JSONObject set = new JSONObject();

		try {
			o.put(Semantics.FLAG_DEP_MANDATORY, this.mandatory);
			o.put(Semantics.FLAG_DEP_INFO, this.info);
			o.put(Semantics.FLAG_DEP_ENUM, set);
			o.put(Semantics.FLAG_DEP_IGNORE_AUTOCONF, this.ignore_autoconf);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<Integer> keys = flags.keySet();
		Iterator<Integer> i = keys.iterator();
		while (i.hasNext()) {
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
	public String toString() {
		if (configured && !enabled)
			return "";
		Set<Entry<Integer, Flag>> flags = this.flags.entrySet();
		Iterator<Entry<Integer, Flag>> i = flags.iterator();

		StringBuilder s = new StringBuilder();
		s.append("Enum: ");
		s.append(key);
		s.append("\ninfo: ");
		s.append(info);
		s.append(": {\n");

		if (!configured) {
			while (i.hasNext()) {
				Entry<Integer, Flag> e = i.next();
				s.append("[");
				s.append(e.getKey().toString());
				s.append("] ");
				String str = e.getValue().toString();
				str = str.replaceAll("\n", "\n\t");
				s.append(str);
				s.append("\n");
			}
		} else {
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
