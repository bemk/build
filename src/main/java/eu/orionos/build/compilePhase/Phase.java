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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import eu.orionos.build.Build;
import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIDebug;
import eu.orionos.build.ui.CLIError;
import eu.orionos.build.ui.CLIWarning;

/**
 * @author bemk
 * 
 */
public abstract class Phase {
	protected BuildPhase phaseMgr;
	protected volatile AbstractMap<String, CompileUnit> toRun;
	protected ConcurrentHashMap<String, CompileUnit> hasRun;
	protected ArrayList<String> targets;
	protected ArrayList<String> flags;
	protected String executable;
	private volatile boolean readyForMarking = false;
	private Phase previous;
	private String modName = null;

	protected static final Config config = Config.getInstance();
	protected static final CLI cli = CLI.getInstance();
	protected static final CLI debug = CLIDebug.getInstance();
	protected static final CLI error = CLIError.getInstance();
	protected static final CLI warning = CLIWarning.getInstance();
	
	protected CommandKernel kernel;

	protected Phase(BuildPhase phaseMgr) {
		kernel = CommandKernel.getInstance();
		this.phaseMgr = phaseMgr;
		this.toRun = new ConcurrentHashMap<String, CompileUnit>();
		this.hasRun = new ConcurrentHashMap<String, CompileUnit>();
		this.targets = new ArrayList<String>();
		this.flags = new ArrayList<String>();
	}

	public abstract void setExecutable();

	public abstract void setFlags();

	protected abstract void switchPhase();

	protected abstract String phaseName();

	public abstract void dependencyUpdate();

	public abstract void run();

	public void setPrevious(Phase previous) {
		this.previous = previous;
		if (!previous.toRun.isEmpty()) {
			new Exception("Illegal phase switch: " + previous.phaseName()
					+ "\nfrom module: " + phaseMgr.getModule().getName()
					+ "\nThread ID: " + Thread.currentThread().getId()
					+ "\nCurrent phase " + phaseName()).printStackTrace();

			printPreviousPhases();

			cli.kill();
			while (!cli.getDone()) {
				Thread.yield();
			}
			System.exit(9001);
		}
	}

	public void printPreviousPhases() {
		System.out.println(phaseMgr.getModule().getName() + " : " + phaseName()
				+ " : " + (toRun.isEmpty() ? "empty" : "non-empty") + " : "
				+ this.toString());
		if (previous != null) {
			previous.printPreviousPhases();
		}
	}

	public void addTarget(String target) {
		this.targets.add(target);
	}

	public void addTargets(ArrayList<String> targets) {
		this.targets.addAll(targets);
	}

	protected void sendCommands() {
		synchronized (previous) {
			AbstractMap<String, CompileUnit> oldMap = toRun;
			this.toRun = new ConcurrentHashMap<String, CompileUnit>(toRun);

			readyForMarking = true;

			Iterator<CompileUnit> i = oldMap.values().iterator();
			while (i.hasNext()) {
				CompileUnit unit = i.next();
				unit.setPhase(this);
				kernel.runCommand(unit);
			}
		}
	}
	
	public String getName() {
		if (modName == null) {
			modName = phaseMgr.getModule().getName();
		}
		return modName;
	}

	public void markComplete(CompileUnit cu) {
		synchronized (previous) {
			String key = cu.key();
			if (this.hasRun.containsKey(key)) {
				error.writeline("Command: " + key + " has already run!");
			}
			this.hasRun.put(key, cu);
			if (!readyForMarking || !this.toRun.containsKey(key)
					|| cu.getPhase() != this) {
				if (!readyForMarking) {
					error.writeline(
							"Not yet ready for marking!");
				}
				if (cu.getPhase() != this) {
					error.writeline(
							"Compile unit attributed to incorrect phase: "
									+ phaseName() + " but belongs to "
									+ cu.getPhase().phaseName());
				}
				new Exception("Unable to remove command: " + key
						+ "\nfrom module: " + phaseMgr.getModule().getName()
						+ "\nCommand did: " + cu.getCommand()[0]
						+ "\nOur module: " + phaseMgr.getModule().getName()
						+ "\nThread ID: " + Thread.currentThread().getId()
						+ "\nCurrent phase " + phaseName()).printStackTrace();

				printPreviousPhases();

				Build.panic("", 9001);
			}
			this.toRun.remove(cu.key());
			if (this.toRun.isEmpty()) {
				switchPhase();
			}
		}
	}
}
