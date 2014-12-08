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

import eu.orionos.build.makefile.MakeModule;
import eu.orionos.build.makefile.Target;
import eu.orionos.build.ui.CLIInfo;

/**
 * @author bemk
 * 
 */
public class PhaseMakefileCompile extends Phase {

	private boolean done = false;

	protected PhaseMakefileCompile(BuildPhase phaseMgr) {
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
		if (done) {
			phaseMgr.switchPhase(new PhaseMakefileWait(phaseMgr));
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Makefile-Compile";
	}

	@Override
	public void dependencyUpdate() {
		return;
	}

	private String convertFlags(String flags[]) {
		StringBuilder f = new StringBuilder();

		// f.append("\"");
		for (int i = 0; i < flags.length; i++) {
			f.append(flags[i]);
			if (i != flags.length - 1) {
				f.append(" ");
			}
		}
		// f.append("\"");

		return f.toString();
	}

	private void setVariables() {
		MakeModule m = phaseMgr.getModule().getMakefile();

		String baseKey = phaseMgr.getModule().getName();
		m.setTarget(baseKey);

		String cc = phaseMgr.getModule().getCompiler();
		String cflags = convertFlags(phaseMgr.getModule().getCompilerFlags());
		m.setCC(baseKey + "-CC", cc);
		m.setCFLAGS(baseKey + "-CFLAGS", cflags + " -o");

		if (phaseMgr.getModule().toArchive()) {
			String arflags = convertFlags(phaseMgr.getModule()
					.getArchiverFlags());
			String ar = phaseMgr.getModule().getArchiver();
			m.setAR(baseKey + "-AR", ar);
			m.setARFLAGS(baseKey + "-ARFLAGS", arflags);
		}

		if (phaseMgr.getModule().toLink()) {
			String ldflags = convertFlags(phaseMgr.getModule().getLinkerFlags());
			String ld = phaseMgr.getModule().getLinker();
			m.setLD(baseKey + "-LD", ld);
			m.setLDFLAGS(baseKey + "-LDFLAGS", ldflags + " -o");
		}
	}

	@Override
	public void run() {
		ArrayList<String> srcFiles = phaseMgr.getModule().getSourceFiles();
		ArrayList<Target> targets = new ArrayList<Target>();

		setVariables();

		Iterator<String> s = srcFiles.iterator();
		while (s.hasNext()) {
			String src = s.next();

			String target = phaseMgr.getModule().getOFile(src);
			StringBuilder source = new StringBuilder();

			source.append(phaseMgr.getModule().getCWD());
			source.append("/");
			source.append(src);

			Target t = new Target(target);
			t.setSourceFile(source.toString());
			t.setParent(phaseMgr.getModule().getMakefile());

			phaseMgr.addForArchive(target);
			targets.add(t);
		}

		phaseMgr.compileTargets(targets);

		if (!phaseMgr.getModule().toLink() && !phaseMgr.getModule().toArchive()) {
			phaseMgr.getModule().getMakefile().addDependency(targets);
		}

		done = true;
		switchPhase();
	}

}
