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
package eu.orionos.build.buildPhase;

import eu.orionos.build.Build;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIDebug;
import eu.orionos.build.ui.CLIError;

/**
 * @author bemk
 * 
 */
public class Complete extends Phase {

	/**
	 * @param manager
	 */
	public Complete(PhaseManager manager) {
		super(manager);
	}

	/*
	 * !(modules.getDone() || manager.getToConfigure()) ||
	 * !manager.getToConfigure() || CommandKernel.getInstance().getNoCommands()
	 * != 0)
	 */

	/**
	 *
	 */
	@Override
	public void run() {
		/*
		 * Wait until the commands have finished running & don't bother waiting
		 * if no commands were issued
		 */
		try {
			if (modules == null)
				throw new Exception();
			boolean buildDone = false;
			while (!buildDone) {
				if (!manager.getToCompile()) {
					buildDone = true;
				} else {
					buildDone = modules.getDone();
					if (!buildDone) {
						//CLIDebug.getInstance().writeline("Waiting for modules!");
					}
				}
				if (CommandKernel.getInstance().getNoCommands() != 0) {
					buildDone = false;
					//CLIDebug.getInstance().writeline("Waiting for command kernel!");
				}
				/*CLIDebug.getInstance().writeline(
						"Main thread waiting for completion");
				CLIDebug.getInstance().writeline(
						"Modules.getDone: " + modules.getDone());
				CLIDebug.getInstance().writeline(
						"manager.getToCompile: " + manager.getToCompile());
				CLIDebug.getInstance().writeline(
						"CommandKernel.getNoCommands: "
								+ CommandKernel.getInstance().getNoCommands());*/
				Thread.sleep(100);
			}
			if (Build.getError() == ErrorCode.SUCCESS)
				CommandKernel.getInstance().stopThreads();
			else
				CLIError.getInstance().writeline("Stopping due to error!");
			while (CLI.getInstance().getDone())
				Thread.sleep(100);
			CLI.getInstance().kill();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(Build.getError());

	}
}
