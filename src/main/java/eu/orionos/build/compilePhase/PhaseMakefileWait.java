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

/**
 * @author bemk
 * 
 */
public class PhaseMakefileWait extends Phase {

	protected PhaseMakefileWait(BuildPhase phaseMgr) {
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
		if (phaseMgr.dependenciesDone()) {
			phaseMgr.switchPhase(new PhaseMakefileArchive(phaseMgr));
		}
	}

	@Override
	protected String phaseName() {
		return "Phase-Makefile-Wait";
	}

	@Override
	public void dependencyUpdate() {
		switchPhase();
	}

	@Override
	public void run() {
		switchPhase();
	}

}
