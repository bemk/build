/*  Build - Hopefully a simple build system
    Copyright (C) 2013 2014 - Bart Kuivenhoven

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

package eu.orionos.build;

import eu.orionos.build.compilePhase.Phase;
import eu.orionos.build.ui.CLI;
import eu.orionos.build.Config;
import net.michelmegens.xterm.Color;

public class CompileUnit {
	private String command[];
	private Phase module;
	private String object;
	private boolean silent;
	private Phase phase = null;
	private static StringBuilder prefix = null;
	private static CLI cli = CLI.getInstance();
	private static Config config = Config.getInstance();
	private String key;

	public CompileUnit(Phase module, String command[], String object) {
		this.module = module;
		this.command = command;
		this.object = object;
		this.silent = config.silent();
		if (prefix == null && !silent) {
			StringBuilder prefixBuilder = new StringBuilder();
			if (config.colors()) {
				prefixBuilder.append(Color.GREEN);
			}
			prefixBuilder.append(" [ OK ] ");
			if (config.colors()) {
				prefixBuilder.append(Color.DEFAULT);
			}
			prefix = prefixBuilder;
		}
		StringBuilder key = new StringBuilder(module.getName());
		key.append('-');
		key.append(object);
		this.key = key.toString();
	}

	public String[] getCommand() {
		return this.command;
	}

	public void markComplete() {
		module.markComplete(this);
		if (!silent) {
			StringBuilder output = new StringBuilder(prefix).append(object);
			cli.writeline(output.toString());
		}
	}

	public String getName() {
		return module.getName();
	}
	public String key() {
		return key;
	}

	public String getObject() {
		return this.object;
	}

	public void setSilent() {
		this.silent = true;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public Phase getPhase() {
		return this.phase;
	}
}
