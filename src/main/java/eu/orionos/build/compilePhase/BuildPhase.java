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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.Module;
import eu.orionos.build.buildPhase.PhaseManager;
import eu.orionos.build.ui.CLIDebug;
import eu.orionos.build.ui.CLIError;

/**
 * @author bemk
 * 
 */
public class BuildPhase {
	private Phase phase;
	private Module module;

	private ConcurrentHashMap<String, Module> dependencies;
	private boolean compileDone = false;
	private boolean archiveDone = false;
	private boolean linkDone = false;
	private ArrayList<String> filesForArchive = new ArrayList<String>();

	Object lock = new Object();

	public BuildPhase(Module parentModule) {
		this.phase = new PhasePreStart(this);
		this.module = parentModule;
	}

	public Module getModule() {
		return this.module;
	}

	public synchronized void switchPhase(Phase phase) {
		synchronized (lock) {
			if (this.dependencies == null) {
				this.dependencies = new ConcurrentHashMap<String, Module>();
				ArrayList<Module> dependencies = module.calculateDependencies();
				Iterator<Module> d = dependencies.iterator();

				while (d.hasNext()) {
					this.addDependency(d.next());
				}
			}

			if (Config.getInstance().getDebug()) {
				System.out.println("Module: " + module.getName()
						+ " current state:\t" + this.phase.toString());
				System.out.println("Module: " + module.getName()
						+ " next state:\t" + phase.toString());
			}

			phase.setPrevious(this.phase);
			this.phase = phase;
			this.phase.setExecutable();
			this.phase.setFlags();
			this.phase.run();
		}
	}

	public void compileDone(boolean compileDone) {
		this.compileDone = compileDone;
	}

	public boolean compileDone() {
		return this.compileDone;
	}

	public void archiveDone(boolean archiveDone) {
		this.archiveDone = archiveDone;
	}

	public boolean archiveDone() {
		return this.archiveDone;
	}

	public void linkDone(boolean linkDone) {
		this.linkDone = linkDone;
	}

	public boolean linkDone() {
		return this.linkDone;
	}

	public void markDependencyDone(Module m) {
		synchronized (lock) {
			if (!dependencies.contains(m)) {
				CLIError.getInstance().writeline(
						"Trying to remove dependency: " + m.getName()
								+ " whilst non-existent in dependency list");
				return;
			}
			CLIDebug.getInstance().writeline(
					"Removing dependency from " + module.getName() + " : "
							+ m.getName());
			dependencies.remove(m.getName());

			if (phase == null) {
				CLIError.getInstance().writeline(
						"Null phase in module: " + module.getName());
			}
			phase.dependencyUpdate();
		}
	}

	private void addDependency(Module m) {
		CLIDebug.getInstance().writeline(
				"Adding dependency to " + module.getName() + " : "
						+ m.getName());
		dependencies.put(m.getName(), m);
		m.build();
	}

	public boolean dependenciesDone() {
		return dependencies.isEmpty();
	}

	public void markCompileUnitDone(CompileUnit unit) {
		this.phase.markComplete(unit);
	}

	public void addForArchive(String fileName) {
		filesForArchive.add(fileName);
	}

	public ArrayList<String> filesForArchive() {
		return new ArrayList<String>(filesForArchive);
	}
}
