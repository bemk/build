/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013 - 2014
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013
        Steven vd Schoot   <stevenvdschoot@gmail.com> - 2013

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

package eu.orionos.build;

import eu.orionos.build.buildPhase.Compile;
import eu.orionos.build.buildPhase.Configure;
import eu.orionos.build.buildPhase.DefHdrConfigure;
import eu.orionos.build.buildPhase.InitialPreconfigure;
import eu.orionos.build.buildPhase.ParseOptions;
import eu.orionos.build.buildPhase.PhaseManager;
import eu.orionos.build.buildPhase.Preconfigure;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class Build {

	private static int error = ErrorCode.SUCCESS;
	private static PhaseManager manager;
	Config config = Config.getInstance();

	public Build(String path, String args[]) {
		manager = new PhaseManager(args);
		manager.switchPhases(new ParseOptions(manager));

		if (config.genHeaderFile()) {
			manager.switchPhases(new DefHdrConfigure(manager));
		} else if (config.genConfigFile()) {
			manager.switchPhases(new Configure(manager));
		} else if (config.updateDepFile()) {
			manager.switchPhases(new Preconfigure(manager));
		} else if (config.genDepFile()) {
			manager.switchPhases(new InitialPreconfigure(manager));
		} else {
			manager.switchPhases(new Compile(manager));
		}

		System.exit(error);
	}

	public static void setError(int error) {
		Build.error = error;
	}

	public static int getError() {
		return Build.error;
	}

	public static void main(String args[]) {
		//CLI.getInstance().readboolean("Press enter when ready to continue");
		new Build("main.build", args);
	}

	public static int terminalWidth() {
		return jline.TerminalFactory.get().getWidth();
	}

	public static Version getVersion() {
		Version version = new Version();

		try {
			Enumeration<URL> en = Build.class.getClassLoader().getResources(
					"version.properties");

			URL url = en.nextElement();

			if (url == null)
				return version;

			InputStream is = url.openStream();

			if (is == null)
				return version;

			int data;
			String varName = "";
			String varVal = "";

			while ((data = is.read()) != -1) {
				if (data == '\n') {
					if (varName.equalsIgnoreCase("version.major")) {
						version.major = Integer.parseInt(varVal);
					} else if (varName.equalsIgnoreCase("version.minor")) {
						version.minor = Integer.parseInt(varVal);
					} else if (varName.equalsIgnoreCase("version.build")) {
						version.build = Integer.parseInt(varVal);
					}

					varName = "";
					varVal = "";
				} else if (data == '=') {
					varName = varVal;
					varVal = "";
				} else if (data != ' ' && data != '\t') {
					varVal += (char) data;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return version;
	}
	
	public static void stop(String message, CLI stream) {
		CommandKernel.getInstance().stopThreads();
		stream.writeline(message);
		CLI.getInstance().kill();
		while (CLI.getInstance().getDone()) {
			Thread.yield();
		}
	}
	
	public static void panic(String msg, int errCode) {
		Build.stop(msg, CLIError.getInstance());
		manager.panic(true);
		System.exit(errCode);
	}

	public static class Version {
		public int major = -1;
		public int minor = -1;
		public int build = -1;
	}
}
