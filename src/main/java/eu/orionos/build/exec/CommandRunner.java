/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013 2014
        Toon Schoenmakers  <nighteyes1993@gmail.com>  - 2013

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

package eu.orionos.build.exec;

import eu.orionos.build.Build;
import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

import java.io.*;

import eu.orionos.build.ui.*;

public class CommandRunner implements Runnable {
	private boolean runnable = true;
	private Config config = Config.getInstance();
	private CLIError error = CLIError.getInstance();
	private CLIWarning warning = CLIWarning.getInstance();
	private CLIInfo info = CLIInfo.getInstance();
	private CLI cli = CLI.getInstance();
	private CommandKernel kernel = null;
	private int errorState = 0;
	private boolean silent = false;

	private CompileUnit next = null;
	private ProcessBuilder builder = null;

	public CommandRunner() {
		super();
		silent = config.silent();
	}

	public void haltThread() {
		this.runnable = false;
	}

	private void writeStream(InputStream p, CLI output, boolean prio)
			throws IOException {

		if ((prio || config.verbose()) && !silent) {
			BufferedReader in = new BufferedReader(new InputStreamReader(p));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.toLowerCase().contains("error"))
					error.writeline(line);
				else
					output.writeline(line);
			}
		}
	}

	private void writeCmd(String[] cmd) {
		if (!config.silent() && config.verbose()) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < cmd.length; i++) {
				s.append(cmd[i]);
				s.append(" ");
			}
			info.writeline(s.toString());
		}
	}

	private void getCommand() throws InterruptedException {
		next = kernel.getCommand();
		if (next != null) {
			builder = new ProcessBuilder(next.getCommand());
		}
	}

	private CompileUnit waitTask(Process p) throws InterruptedException {
		getCommand();
		/*
		 * CompileUnit next = kernel.getCommand(); if (next != null) { builder =
		 * new ProcessBuilder(next.getCommand()); }
		 */
		errorState = p.waitFor();
		return next;
	}

	@Override
	public void run() {
		kernel = CommandKernel.getInstance();
		// Runtime r = Runtime.getRuntime();

		CompileUnit c = null;
		while (runnable) {
			try {
				if (next == null) {
					getCommand();
				}
				c = next;
				next = null;
				if (c != null) {
					writeCmd(c.getCommand());
					Process p = builder.start();
					// Process p = r.exec(c.getCommand());

					writeStream(p.getErrorStream(), warning, true);
					writeStream(p.getInputStream(), info, false);

					next = waitTask(p);
					if (errorState != 0) {
						StringBuilder err = new StringBuilder();
						final String[] array = c.getCommand();
						for (int i = 0; i < array.length; i++) {
							err.append(array[i]);
							err.append(' ');
						}
						err.append("Could not compute!");
						Build.panic(err.toString(), ErrorCode.COMPILE_FAILED);
					}
					p.destroy();

					c.markComplete();
				} else {
					Thread.yield();
				}
			} catch (IOException e) {
				error.writeline(e.getMessage());
				kernel.killThreads();
				Build.setError(ErrorCode.GENERIC);
				cli.kill();
				while (!cli.getDone()) {
					Thread.yield();
				}
				System.exit(Build.getError());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*
			 * try { Thread.sleep(10); } catch (InterruptedException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 */
		}
		kernel.unregisterTask(this);
	}
}
