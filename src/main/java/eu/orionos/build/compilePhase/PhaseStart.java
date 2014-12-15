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

import eu.orionos.build.Config;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.ui.CLIDebug;
import eu.orionos.build.ui.CLIError;

/**
 * @author bemk
 * 
 */
public class PhaseStart extends Phase {

	/**
	 * @param phaseMgr
	 */
	public PhaseStart(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	@Override
	public void setExecutable() {

	}

	@Override
	public void setFlags() {

	}

	@Override
	protected void switchPhase() {
		debug.writeline(new StringBuilder("Switching to start or clean in ")
				.append(phaseMgr.getModule().getName()).toString());
		if (config.genMakefile()) {
			phaseMgr.switchPhase(new PhaseMakefileCompile(phaseMgr));
		} else if (Config.getInstance().getClean()) {
			phaseMgr.switchPhase(new PhaseClean(phaseMgr));
		} else {
			phaseMgr.switchPhase(new PhaseCompile(phaseMgr));
		}
	}

	@Override
	public synchronized void run() {
		kernel.registerModule(phaseMgr.getModule());
		debug.writeline(new StringBuilder("Module ").append(phaseMgr.getModule().getName()).append(" registered!").toString());
		this.switchPhase();
	}

	@Override
	protected String phaseName() {
		return "Phase-Start";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
