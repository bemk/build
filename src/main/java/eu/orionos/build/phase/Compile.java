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
package eu.orionos.build.phase;

import java.io.FileNotFoundException;
import java.io.IOException;

import eu.orionos.build.Module;

/**
 * @author bemk
 * This class takes care of the actual compiling of the code
 */
public class Compile extends Phase {

	public Compile(PhaseManager manager)
	{
		super(manager);
	}
	/* (non-Javadoc)
	 * @see eu.orionos.build.phase.Phase#run()
	 */
	@Override
	public void run() {
		try {
			if (configuration.hasConf() == false)
			{
				configuration.override(".config");
				if (configuration.hasConf() == false)
				{
					System.err.println("No usable config files found!");
					System.err.println("Use build --configure to configure the project ");
					System.err.println("Or use the --config <config file> option to specify a config file");
					System.exit(1);
				}
			}
			modules = new Module(configuration.buildFile());
			this.modules.build();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		manager.switchPhases(new Complete(manager));
	}

}
