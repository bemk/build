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
package eu.orionos.build.makefile;

import java.util.ArrayList;
import java.util.Iterator;

import eu.orionos.build.Config;

/**
 * @author bemk
 * 
 */
public class Target {
	protected MakeModule parent;
	protected String target;
	private String sourceFile;

	protected ArrayList<Target> dependencies = new ArrayList<Target>();
	protected ArrayList<MakeModule> modules = new ArrayList<MakeModule>();

	public Target(String target) {
		this.target = target;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String getTarget() {
		StringBuilder target = new StringBuilder();

		target.append(this.target);
		target.append(": ");
		target.append(sourceFile);
		target.append("\n\t$(");
		target.append(parent.getCC());
		target.append(") $(");
		target.append(parent.getCFLAGS());
		target.append(") ");
		target.append(this.target);
		target.append(" ");
		target.append(sourceFile);
		target.append("\n\n");

		return target.toString();
	}

	protected String getTargetDepencencies(String exec, String flags) {
		StringBuilder target = new StringBuilder();

		target.append(this.target);
		target.append(":");

		Iterator<Target> i = dependencies.iterator();
		StringBuilder tmp = new StringBuilder();
		while (i.hasNext()) {
			Target t = i.next();
			tmp.append(" ");
			tmp.append(t.getTargetName());
		}
		Iterator<MakeModule> j = modules.iterator();
		while (j.hasNext()) {
			MakeModule m = j.next();
			tmp.append(" ");
			tmp.append(m.getTarget());
		}
		target.append(tmp.toString());

		target.append("\n\t$(");
		if (!Config.getInstance().nosync()) {
			target.append("SYNC)\n\t$(");
		}
		target.append(exec);
		target.append(") $(");
		target.append(flags);
		target.append(") ");
		target.append(this.target);
		target.append(" ");
		target.append(sourceFile);
		target.append("\n\n");

		j = modules.iterator();
		while (j.hasNext()) {
			MakeModule m = j.next();
			target.append(m.getDependencies());
		}
		i = dependencies.iterator();
		while (i.hasNext()) {
			Target t = i.next();
			target.append(t.getTarget());
		}
		return target.toString();
	}

	public void setParent(MakeModule parent) {
		this.parent = parent;
	}

	public String getTargetName() {
		return this.target;
	}

	public void addDependency(Target dependency) {
		this.dependencies.add(dependency);
	}

	public void addDependency(ArrayList<Target> dependency) {
		this.dependencies.addAll(dependency);
	}

	public void addDepencency(ArrayList<MakeModule> depencency) {
		this.modules.addAll(depencency);
	}
}