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

import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

import eu.orionos.build.Build;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.Module;
import eu.orionos.build.ui.CLIInfo;

/**
 * @author bemk
 * 
 */
public class MakeModule {
	private Variable CC;
	private Variable LD;
	private Variable AR;

	private Variable CFLAGS;
	private Variable LDFLAGS;
	private Variable ARFLAGS;

	private Module module;

	private String target;

	ArrayList<MakeModule> dependencies = new ArrayList<MakeModule>();
	ArrayList<Target> targets = new ArrayList<Target>();

	public MakeModule(String target, Module m) {
		this.target = target;
		this.module = m;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void addDepencency(MakeModule m) {
		dependencies.add(m);
	}

	public void addDepencency(Target t) {
		targets.add(t);
	}

	public void addDependency(ArrayList<Target> t) {
		targets.addAll(t);
	}

	public ArrayList<MakeModule> getModules() {
		return this.dependencies;
	}

	public void clearModules() {
		this.dependencies = new ArrayList<MakeModule>();
	}

	private String getVariables() {
		StringBuilder vars = new StringBuilder();

		if (CC == null) {
			Build.panic("CC = null in " + target, ErrorCode.GENERIC);
		}
		vars.append(CC.getVarDef());
		if (LD != null)
			vars.append(LD.getVarDef());
		if (AR != null)
			vars.append(AR.getVarDef());
		vars.append("\n");

		vars.append(CFLAGS.getVarDef());
		if (LDFLAGS != null)
			vars.append(LDFLAGS.getVarDef());
		if (ARFLAGS != null)
			vars.append(ARFLAGS.getVarDef());

		vars.append("\n");

		return vars.toString();
	}

	public String getDependencies() {
		StringBuilder deps = new StringBuilder();

		deps.append("SYNC:=sync\n");
		deps.append(getVariables());
//		deps.append("\n\n");

		deps.append(".PHONY: ");
		deps.append(target);
		deps.append("\n");
		deps.append(target);
		deps.append(": ");

		StringBuilder targets = new StringBuilder();

		Iterator<MakeModule> d = dependencies.iterator();
		while (d.hasNext()) {
			MakeModule tmp = d.next();
			deps.append(tmp.getTarget());
			deps.append(" ");
			targets.append(tmp.getDependencies());
		}

		targets.append("\n\n");

		Iterator<Target> t = this.targets.iterator();
		while (t.hasNext()) {
			Target tmp = t.next();
			deps.append(tmp.getTargetName());
			deps.append(" ");
			targets.append(tmp.getTarget());
		}

		deps.append("\n");
		deps.append(targets.toString());
		//deps.append("\n\n");

		return deps.toString();
	}

	public String getMakefile() {
		StringBuilder makefile = new StringBuilder();

		makefile.append(getDependencies());

		return makefile.toString();
	}

	public String getTarget() {
		return target;
	}

	public void setCC(String key, String value) {
		CC = new Variable(key, value);
	}

	public String getCC() {
		return CC.getKey();
	}

	public void setLD(String key, String value) {
		LD = new Variable(key, value);
	}

	public String getLD() {
		return LD.getKey();
	}

	public void setAR(String key, String value) {
		AR = new Variable(key, value);
	}

	public String getAR() {
		return AR.getKey();
	}

	public void setCFLAGS(String key, String value) {
		CFLAGS = new Variable(key, value);
	}

	public String getCFLAGS() {
		return CFLAGS.getKey();
	}

	public void setLDFLAGS(String key, String value) {
		LDFLAGS = new Variable(key, value);
	}

	public String getLDFLAGS() {
		return LDFLAGS.getKey();
	}

	public void setARFLAGS(String key, String value) {
		ARFLAGS = new Variable(key, value);
	}

	public String getARFLAGS() {
		return ARFLAGS.getKey();
	}
}
