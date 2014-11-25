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
import eu.orionos.build.exec.CommandKernel;

/**
 * @author bemk
 * 
 */
public class PhaseArchive extends Phase {

	/**
	 * @param phaseMgr
	 */
	public PhaseArchive(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	@Override
	public void setExecutable() {
		this.executable = phaseMgr.getModule().getArchiver();
	}

	@Override
	public void setFlags() {
		String flags[] = phaseMgr.getModule().getArchiverFlags();
		for (int i = 0; i < flags.length; i++) {
			this.flags.add(flags[i]);
		}
	}

	@Override
	protected void switchPhase() {
		if (this.toRun.isEmpty()) {
			phaseMgr.archiveDone(true);
			phaseMgr.switchPhase(new PhaseSync(phaseMgr));
		}
	}

	@Override
	public synchronized void run() {
		if (phaseMgr.getModule().toArchive()) {
			ArrayList<String> command = new ArrayList<String>();

			command.add(this.executable);
			command.addAll(this.flags);
			command.add(phaseMgr.getModule().getAFile());
			command.addAll(phaseMgr.filesForArchive());

			String[] cuCommand = new String[1];
			cuCommand = command.toArray(cuCommand);

			CompileUnit cu = new CompileUnit(phaseMgr.getModule(), cuCommand,
					phaseMgr.getModule().getAFile());

			this.toRun.put(cu.key(), cu);

			sendCommands();
		} else {
			switchPhase();
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Archive";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}

}
