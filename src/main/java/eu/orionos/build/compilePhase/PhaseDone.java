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

import eu.orionos.build.exec.CommandKernel;

/**
 * @author bemk
 * 
 */
public class PhaseDone extends Phase {

	/**
	 * @param phaseMgr
	 */
	public PhaseDone(BuildPhase phaseMgr) {
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

	}

	@Override
	public synchronized void run() {
		CommandKernel.getInstance().unregisterModule(phaseMgr.getModule());
		if (phaseMgr.getModule().getParent() != null)
			phaseMgr.getModule().getParent()
					.markDependencyDone(phaseMgr.getModule());
		phaseMgr.getModule().setDone();
	}

	@Override
	protected String phaseName() {
		return "Phase-Done";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
