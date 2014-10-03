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

import java.io.File;
import java.io.FileWriter;
import java.util.Set;

import eu.orionos.build.Config;
import eu.orionos.build.configGenerator.DepFile;

/**
 * @author bemk
 * This class runs the --gen-depfile option
 */
public class InitialPreconfigure  extends Phase {

	/**
	 * @param manager
	 */
	public InitialPreconfigure(PhaseManager manager) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see eu.orionos.build.phase.Phase#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Set<String> flags = modules.getBuildFlags();
		try {
			File f = new File (Config.getInstance().getDepFile());
			if (!f.exists())
				f.createNewFile();
			if (f.isDirectory())
				throw (new Exception());
			FileWriter fw = new FileWriter(f);

			DepFile d = new DepFile();
			fw.write(d.generateDepFile(flags).toString(8));

			fw.close();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		} catch (Exception e) {
		}
		if (manager.getToConfigure())
			manager.switchPhases(new Configure(manager));
		else
			manager.switchPhases(new Complete(manager));
	}

}
