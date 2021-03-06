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
package eu.orionos.build.buildPhase;

/**
 * @author bemk
 * 
 */
public class PhaseManager {
	private Phase phase;
	private String[] cmd;
	private boolean toConfigure = false;
	private boolean toCompile = false;
	private boolean panic = false;

	public PhaseManager(String[] cmd) {
		this.cmd = cmd;
	}

	public void setToConfigure() {
		this.toConfigure = true;
	}

	public boolean getToConfigure() {
		return this.toConfigure;
	}

	public void setToCompile() {
		this.toCompile = true;
	}

	public boolean getToCompile() {
		return this.toCompile;
	}

	public String[] getCmd() {
		return this.cmd;
	}

	public Phase getPhase() {
		return this.phase;
	}

	public void switchPhases(Phase phase) {
		this.phase = phase;
		phase.run();
	}

	public void panic(boolean panic) {
		this.panic = panic;
	}
	public boolean panic() {
		return this.panic;
	}
}
