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
import java.util.Iterator;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.ui.CLIDebug;

/**
 * @author bemk
 * 
 */
public class PhaseCompile extends Phase {

	public PhaseCompile(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	private CompileUnit buildCompileUnit(String target) {
		String objectName = phaseMgr.getModule().getOFile(target);

		target = phaseMgr.getModule().getCWD() + "/" + target;

		ArrayList<String> commandList = new ArrayList<String>();

		commandList.add(this.executable);
		commandList.addAll(this.flags);
		commandList.add(target);
		commandList.add("-o");
		commandList.add(objectName);

		phaseMgr.addForArchive(objectName);

		String[] command = new String[1];
		command = commandList.toArray(command);

		return new CompileUnit(this.phaseMgr.getModule(), command, objectName);
	}

	@Override
	protected synchronized void switchPhase() {
		if (this.toRun.isEmpty()) {
			CLIDebug.getInstance().writeline(
					"Going to sync for module: "
							+ phaseMgr.getModule().getName());
			phaseMgr.compileDone(true);
			phaseMgr.switchPhase(new PhaseSync(phaseMgr));
		} else {
			CLIDebug.getInstance().writeline(
					"Not yet switching phases in "
							+ phaseMgr.getModule().getName());
			CLIDebug.getInstance().writeline(
					phaseMgr.getModule().getName() + "Still waiting for: ");
			Iterator<CompileUnit> i = toRun.values().iterator();
			while (i.hasNext()) {
				CLIDebug.getInstance().writeline("\t" + i.next().key());
			}
		}
	}

	private void getTargets() {
		this.targets = phaseMgr.getModule().getSourceFiles();
	}

	@Override
	public synchronized void run() {
		CLIDebug.getInstance().writeline(
				"Running compile in module " + phaseMgr.getModule().getName());
		/*
		 * A double itteration has been chosen, because the command runner could
		 * theoretically be quicker than the rate at which new commands are
		 * entered into the list, causing the switchPhase method to run before
		 * its time.
		 */

		getTargets();
		Iterator<String> target = targets.iterator();
		while (target.hasNext()) {
			String t = target.next();
			CompileUnit u = buildCompileUnit(t);
			toRun.put(u.key(), u);
		}

		sendCommands();
		/*
		 * This call exist, because if there are no targets, this method won't
		 * be called otherwise. One could also opt for a mock command, but this
		 * is a lot simpler.
		 */
		switchPhase();
	}

	@Override
	public void setExecutable() {
		this.executable = phaseMgr.getModule().getCompiler();
	}

	@Override
	public void setFlags() {
		String ccFlags[] = phaseMgr.getModule().getCompilerFlags();
		for (int i = 0; i < ccFlags.length; i++) {
			flags.add(ccFlags[i]);
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Compile";
	}

	@Override
	public void dependencyUpdate() {
		return; // Not at all relevant for this stage
	}
}
