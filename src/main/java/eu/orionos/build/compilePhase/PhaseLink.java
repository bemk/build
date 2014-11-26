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
package eu.orionos.build.compilePhase;

import java.util.ArrayList;

import eu.orionos.build.CompileUnit;

/**
 * @author bemk
 * 
 */
public class PhaseLink extends Phase {

	/**
	 * @param phaseMgr
	 */
	public PhaseLink(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	@Override
	public void setExecutable() {
		this.executable = phaseMgr.getModule().getLinker();
	}

	@Override
	public void setFlags() {
		String flags[] = phaseMgr.getModule().getLinkerFlags();
		for (int i = 0; i < flags.length; i++) {
			this.flags.add(flags[i]);
		}
	}

	@Override
	protected void switchPhase() {
		if (this.toRun.isEmpty()) {
			phaseMgr.linkDone(true);
			phaseMgr.switchPhase(new PhaseDone(phaseMgr));
		}
	}

	@Override
	public synchronized void run() {
		if (!phaseMgr.getModule().toLink()) {
			switchPhase();
			return;
		}

		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(this.executable);
		cmd.addAll(this.flags);
		cmd.add("-o");
		cmd.add(phaseMgr.getModule().getLFile());
		cmd.addAll(phaseMgr.getModule().getLinkableFiles());

		String[] command = new String[1];
		command = cmd.toArray(command);

		CompileUnit cu = new CompileUnit(phaseMgr.getModule(), command,
				phaseMgr.getModule().getLFile());
		this.toRun.put(cu.key(), cu);

		sendCommands();
	}

	@Override
	protected String phaseName() {
		return "Phase-Link";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
