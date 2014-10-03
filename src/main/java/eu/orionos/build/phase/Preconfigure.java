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

import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.configGenerator.DepFile;
import eu.orionos.build.configGenerator.DepfileException;
import eu.orionos.build.ui.CLIError;

/**
 * @author bemk
 * This class is responsible for the --update-depfile option
 */
public class Preconfigure extends Phase {

	/**
	 * @param manager
	 */
	public Preconfigure(PhaseManager manager) {
		super(manager);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see eu.orionos.build.phase.Phase#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			DepFile d = new DepFile();
			d.readDepFile();

			Set<String> flags = modules.getBuildFlags();
			JSONObject o = d.updateDepFile(flags);

			File f = new File(Config.getInstance().getDepFile());
			if (!f.exists() || f.isDirectory())
				throw (new Exception());
			FileWriter fw = new FileWriter(f);
			fw.write(o.toString(8) + "\n");
			fw.close();
		}
		catch (DepfileException e)
		{
			CLIError.getInstance().writeline("Missing depfile!");
			CLIError.getInstance().writeline("Run with --gen-depfile first!");
		}
		catch (Exception e)
		{
		}
		manager.switchPhases(new Complete(manager));
	}

}
