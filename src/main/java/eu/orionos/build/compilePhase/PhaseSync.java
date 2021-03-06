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
package eu.orionos.build.compilePhase;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ui.CLIDebug;

/**
 * @author bemk
 * 
 */
public class PhaseSync extends Phase {

	/**
	 * @param phaseMgr
	 */
	protected PhaseSync(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	@Override
	protected synchronized void switchPhase() {
		if (config.getDebug()) {
			debug.writeline(phaseMgr.getModule().getName()
					+ " switching away from sync in thread: "
					+ Thread.currentThread().getId());
			debug.writeline("Leaving sync in " + phaseMgr.getModule().getName());
		}

		if (config.getClean()) {
			phaseMgr.switchPhase(new PhaseDone(phaseMgr));
		} else {
			phaseMgr.switchPhase(new PhaseArchive(phaseMgr));
		}
	}

	@Override
	public synchronized void run() {
		if (config.getDebug()) {
			debug.writeline("Switching to sync in module "
					+ phaseMgr.getModule().getName() + " in thread: "
					+ Thread.currentThread().getId());
		}
		if (config.nosync()) {
			this.switchPhase();
			return;
		}
		String command[] = new String[1];
		command[0] = "sync";
		CompileUnit unit = new CompileUnit(this, command, "Syncing "
				+ phaseMgr.getModule().getName());

		unit.setSilent();

		toRun.put(unit.key(), unit);

		sendCommands();
	}

	@Override
	public void setExecutable() {

	}

	@Override
	public void setFlags() {

	}

	@Override
	protected String phaseName() {
		return "Phase-Sync";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
