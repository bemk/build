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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.orionos.build.Build;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.Module;
import eu.orionos.build.makefile.MakeModule;

/**
 * @author bemk
 * 
 */
public class PhaseMakefileWrite extends Phase {

	private boolean done = false;

	protected PhaseMakefileWrite(BuildPhase phaseMgr) {
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
			phaseMgr.switchPhase(new PhaseDone(phaseMgr));
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Makefile-Write";
	}

	@Override
	public void dependencyUpdate() {

	}

	@Override
	public void run() {
		Module parent = phaseMgr.getModule().getParent();
		if (parent != null) {
			parent.getMakefile().addDepencency(
					phaseMgr.getModule().getMakefile());
			done = true;
			switchPhase();
			return;
		}

		try {
			MakeModule m = phaseMgr.getModule().getMakefile();
			File makefile = new File(Config.getInstance().MakefilePath());
			
			if (makefile.exists() && makefile.isDirectory()) {
				Build.panic("File at " + Config.getInstance().MakefilePath()
						+ " already exists and is directory!", -9004);
			}
			if (makefile.exists()) {
				makefile.createNewFile();
			}
			FileWriter fw = new FileWriter(makefile);
			fw.write(m.getMakefile());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			Build.panic("Makefile writing went wrong", ErrorCode.FILE_NOT_FOUND);
		}

		done = true;
		switchPhase();
	}

}
