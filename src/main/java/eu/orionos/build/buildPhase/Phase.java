/*  Build - Hopefully a simple build system
    Copyright (C) 2014 - Bart Kuivenhoven

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
package eu.orionos.build.buildPhase;

import eu.orionos.build.*;
import eu.orionos.build.configGenerator.DepFile;

/**
 * @author bemk
 *
 */
public abstract class Phase {
	protected static Module modules;
	protected static Config configuration = Config.getInstance();
	protected PhaseManager manager;

	public Phase(PhaseManager manager) {
		this.manager = manager;
	}

	protected void setModules(DepFile d) throws Exception {
		if (modules == null) {
			if (d.getBuildRoot() != null
					&& !Config.getInstance().buildFileOverride()) {
				modules = new Module(d.getBuildRoot());
			} else {
				modules = new Module(Config.getInstance().buildFile());
			}
		}
	}

	/**
	 * @fn run
	 * @author bemk Start running this phase of the build process.
	 */
	public abstract void run();
}
