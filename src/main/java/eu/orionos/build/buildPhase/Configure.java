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

import java.io.IOException;

import eu.orionos.build.Config;
import eu.orionos.build.Module;
import eu.orionos.build.configGenerator.ConfigFile;
import eu.orionos.build.configGenerator.DepFile;
import eu.orionos.build.configGenerator.DepfileException;

/**
 * @author bemk
 * This class should take care of generating a config file
 */
public class Configure extends Phase {

	public Configure(PhaseManager manager)
	{
		super(manager);
	}


	/* (non-Javadoc)
	 * @see eu.orionos.build.phase.Phase#run()
	 */
	@Override
	public void run() {
		DepFile d = null;
		try {
			d = new DepFile();
			d.readDepFile();
		}
		catch(DepfileException e)
		{
			manager.setToConfigure();
			manager.switchPhases(new InitialPreconfigure(manager));
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (modules == null) {
				if (d.getBuildRoot() != null && !Config.getInstance().buildFileOverride()) {
					modules = new Module(d.getBuildRoot());
				} else {
					modules = new Module(Config.getInstance().buildFile());
				}
			}

			ConfigFile c = d.generateConfigFile();
			c.write();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		manager.switchPhases(new Complete(manager));
	}


}
