/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013

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

import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.option.Options;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Build {
	
	private Module modules;

	public Build(String path, String args[])
	{
		try {
			Config.getInstance();
			new Options(args);
			if (Config.getInstance().hasConf() == false)
			{
				Config.getInstance().override(".config");
				if (Config.getInstance().hasConf() == false)
				{
					System.err.println("No usable config files found!");
					System.exit(1);
				}
			}
			if (!Config.getInstance().configured())
			{
				/* \TODO: Present a nice little menu for configuring options */
			}

			this.modules = new Module(Config.getInstance().buildFile());
			modules.build();
			/* Wait untill the commads have finished running & don't bother waiting if no commands were issued */
			while (!modules.getDone() && CommandKernel.getInstance().getNoCommands() != 0)
				Thread.sleep(250);
			CommandKernel.getInstance().stopThreads();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		new Build("main.build", args);
	}
}
