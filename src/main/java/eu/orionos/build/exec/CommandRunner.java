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

public class CommandRunner extends Thread {
	private boolean runnable = true;

	public CommandRunner() {
		super();
	}

	public void haltThread() {
		this.runnable = false;
	}

	private void writeStream(InputStream p, CLI output, boolean prio)
			throws IOException {

		if ((prio || Config.getInstance().verbose())
				&& !Config.getInstance().silent()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(p));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.toLowerCase().contains("error"))
					CLIError.getInstance().writeline(line);
				else
					output.writeline(line);
			}
		}
	}

	private void writeCmd(String[] cmd) {
		if (!Config.getInstance().silent() && Config.getInstance().verbose()) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < cmd.length; i++) {
				s.append(cmd[i]);
				s.append(" ");
			}
			CLIInfo.getInstance().writeline(s.toString());
		}
	}

	@Override
	public void run() {
		super.run();
		while (runnable) {
			CompileUnit c = CommandKernel.getInstance().getCommand();
			if (c != null) {
				try {
					writeCmd(c.getCommand());
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(c.getCommand());

					writeStream(p.getErrorStream(), CLIWarning.getInstance(),
							true);
					writeStream(p.getInputStream(), CLIInfo.getInstance(),
							false);

					if (p.waitFor() != 0) {
						StringBuilder err = new StringBuilder();
						final String[] array = c.getCommand();
						for (int i = 0; i < array.length; i++) {
							err.append(array[i]);
							err.append(' ');
						}
						err.append("Could not compute!");
						CLIError.getInstance().writeline(err.toString());
						CommandKernel.getInstance().killThreads();
						Build.setError(ErrorCode.COMPILE_FAILED);
					}

					c.markComplete();
				} catch (IOException e) {
					CLIError.getInstance().writeline(e.getMessage());
					CommandKernel.getInstance().killThreads();
					Build.setError(ErrorCode.GENERIC);
					CLI.getInstance().kill();
					while (!CLI.getInstance().getDone()) {
						Thread.yield();
					}
					System.exit(Build.getError());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Thread.yield();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		CommandKernel.getInstance().unregisterTask(this);
	}
}
