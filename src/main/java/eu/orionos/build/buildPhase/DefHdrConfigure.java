/*  Build - Hopefully a simple build system
    Copyright (C) 2015 - Bart Kuivenhoven

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

import java.io.IOException;

import eu.orionos.build.Config;
import eu.orionos.build.configGenerator.DefHeader;
import eu.orionos.build.configGenerator.DepFile;
import eu.orionos.build.configGenerator.DepfileException;

/**
 * @author bemk
 *
 */
public class DefHdrConfigure extends Phase {

	/**
	 * @param manager
	 */
	public DefHdrConfigure(PhaseManager manager) {
		super(manager);
	}

	@Override
	public void run() {
		try {
			DepFile d = new DepFile();
			d.readDepFile();

			Config.getInstance().clearModules();
			Config.getInstance(Config.getInstance().getConfigFile());
			modules = null;
			this.setModules(d);
			DefHeader def_hdr = new DefHeader(modules, d);
			def_hdr.write();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DepfileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		manager.switchPhases(new Complete(manager));
	}

}
