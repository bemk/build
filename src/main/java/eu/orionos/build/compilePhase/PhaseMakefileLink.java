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

import eu.orionos.build.makefile.LDTarget;
import eu.orionos.build.makefile.MakeModule;

/**
 * @author bemk
 * 
 */
public class PhaseMakefileLink extends Phase {

	private boolean done = false;

	protected PhaseMakefileLink(BuildPhase phaseMgr) {
		super(phaseMgr);
	}

	public void setExecutable() {

	}

	@Override
	public void setFlags() {

	}

	@Override
	protected void switchPhase() {
		if (done) {
			phaseMgr.switchPhase(new PhaseMakefileWrite(phaseMgr));
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Makefile-Link";
	}

	@Override
	public void dependencyUpdate() {
		return;
	}

	@Override
	public void run() {
		if (!phaseMgr.getModule().toLink()) {
			done = true;
			switchPhase();
			return;
		}

		LDTarget target = new LDTarget(phaseMgr.getModule().getLFile());
		StringBuilder sources = new StringBuilder();

		Iterator<String> i = phaseMgr.getModule().getLinkableFiles().iterator();
		while (i.hasNext()) {
			if (sources.length() != 0) {
				sources.append(" ");
			}
			sources.append(i.next());
		}
		target.setSourceFile(sources.toString());

		if (phaseMgr.getModule().toArchive()) {
			target.addDependency(phaseMgr.arTarget());
		} else {
			target.addDependency(phaseMgr.compileTargets());
		}

		ArrayList<MakeModule> modules = phaseMgr.getModule().getMakefile()
				.getModules();
		target.addDepencency(modules);
		phaseMgr.getModule().getMakefile().clearModules();

		target.setParent(phaseMgr.getModule().getMakefile());
		phaseMgr.getModule().getMakefile().addDepencency(target);

		done = true;
		switchPhase();
	}

}
