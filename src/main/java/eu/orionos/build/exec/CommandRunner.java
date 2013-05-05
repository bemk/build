/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
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

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

import java.io.*;

import ui.CLI;
import ui.CLIInfo;
import ui.CLIWarning;

public class CommandRunner extends Thread {
	private boolean runnable = true;

	public CommandRunner()
	{
		super();
	}

	public void haltThread()
	{
		this.runnable = false;
	}

	private void writeStream(InputStream p, CLI output, boolean prio) throws IOException
	{
		
		if ((prio || Config.getInstance().verbose()) && !Config.getInstance().silent())
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(p));
			String line;
			while ((line = in.readLine()) != null)
			{
				output.writeline(line);
			}
		}
	}

	private void writeCmd(String[] cmd)
	{
		if (!Config.getInstance().silent() && Config.getInstance().verbose())
		{
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < cmd.length; i++)
			{
				s.append(cmd[i]);
				s.append(" ");
			}
			CLIInfo.getInstance().writeline(s.toString());
		}
	}

	@Override
	public void run()
	{
		super.run();
		while (runnable)
		{
			CompileUnit c = CommandKernel.getInstance().getCommand();
			if (c != null)
			{
				try {
					writeCmd(c.getCommand());
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(c.getCommand());

					writeStream(p.getErrorStream(), CLIInfo.getInstance(), true);
					writeStream(p.getInputStream(), CLIWarning.getInstance(), false);

					if (p.waitFor() != 0)
					{
						final String[] array = c.getCommand();
						for (int i = 0; i < array.length; i++) {
							System.err.print(array[i]);
							System.err.print(' ');
						}
						System.err.println("Could not compute!");
						r.halt(ErrorCode.INSTRUCTION_FAILED);
					}

					c.markComplete();
				}
				catch (IOException e)
				{
					e.printStackTrace();
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
