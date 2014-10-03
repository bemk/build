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

import eu.orionos.build.Build;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.ui.CLI;
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

	/* (non-Javadoc)
	 * @see eu.orionos.build.phase.Phase#run()
	 */
	@Override
	public void run() {
		/* Wait until the commands have finished running & don't bother waiting if no commands were issued */
		try {
		while ((modules == null || !modules.getDone()) && CommandKernel.getInstance().getNoCommands() != 0 && Build.getError() == ErrorCode.SUCCESS) {
			Thread.sleep(250);
		}
		if (Build.getError() == ErrorCode.SUCCESS)
			CommandKernel.getInstance().stopThreads();
		else
			CLIError.getInstance().writeline("Stopping due to error!");
		Thread.yield();
		CLI.getInstance().kill();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(Build.getError());
		
	}

}
