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

import java.util.Iterator;
import eu.orionos.build.CompileUnit;

/**
 * @author bemk
 * 
 */
public class PhaseClean extends Phase {

	/**
	 * @param phaseMgr
	 */
	public PhaseClean(BuildPhase phaseMgr) {
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
		if (toRun.isEmpty()) {
			phaseMgr.compileDone(true);
			phaseMgr.archiveDone(true);
			phaseMgr.linkDone(true);
			phaseMgr.switchPhase(new PhaseSync(phaseMgr));
		}
	}

	private void buildCommand(String[] cmd, String obj) {
		CompileUnit cu = new CompileUnit(phaseMgr.getModule(), cmd, obj);
		this.toRun.put(cu.key(), cu);
	}

	private void getTargets() {
		this.targets = phaseMgr.getModule().getSourceFiles();
	}

	@Override
	public synchronized void run() {
		getTargets();
		if (targets.isEmpty()
				&& !(phaseMgr.getModule().toArchive() || phaseMgr.getModule()
						.toLink())) {
			switchPhase();
			return;
		}

		Iterator<String> i = targets.iterator();

		while (i.hasNext()) {
			String sFile = i.next();
			String obj = phaseMgr.getModule().getOFile(sFile);
			String cmd[] = { "rm", "-f", obj };

			buildCommand(cmd, obj);
		}
		if (phaseMgr.getModule().toArchive()) {
			String archive[] = { "rm", "-f", phaseMgr.getModule().getAFile() };
			buildCommand(archive, phaseMgr.getModule().getAFile());
		}
		if (phaseMgr.getModule().toLink()) {
			String linked[] = { "rm", "-f", phaseMgr.getModule().getLFile() };
			buildCommand(linked, phaseMgr.getModule().getLFile());
		}

		sendCommands();

		this.switchPhase();
	}

	@Override
	protected String phaseName() {
		return "Phase-Clean";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
