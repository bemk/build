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

package eu.orionos.build;

import eu.orionos.build.configGenerator.ConfigFile;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.option.Options;
import org.json.JSONException;



import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class Build {
	
	private Module modules;
	private static int error = ErrorCode.SUCCESS;

	public Build(String path, String args[])
	{
		try {
			Config.getInstance();
			new Options(args);
			if (Config.getInstance().hasConf() == false)
			{
				Config.getInstance().override(".config");
				if (Config.getInstance().hasConf() == false && !Config.getInstance().toConfigure())
				{
					System.err.println("No usable config files found!");
					System.exit(1);
				}
			}
			this.modules = new Module(Config.getInstance().buildFile());
			if (Config.getInstance().toConfigure())
			{
				Set<String> flags = modules.getBuildFlags();
				try {
				CLI.getInstance().writeline(Config.getInstance().getConfigFile());
				File f = new File(Config.getInstance().getConfigFile());
				if (!f.exists())
					f.createNewFile();
				FileWriter fw = new FileWriter(f);
				fw.write(new ConfigFile(flags).toString());
				fw.close();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				modules.build();
			}
			/* Wait until the commands have finished running & don't bother waiting if no commands were issued */
			while (!modules.getDone() && CommandKernel.getInstance().getNoCommands() != 0 && error == ErrorCode.SUCCESS)
				Thread.sleep(250);
			if (error == ErrorCode.SUCCESS)
				CommandKernel.getInstance().stopThreads();
			else
				CLIError.getInstance().writeline("Stopping due to error!");
			Thread.yield();
			CLI.getInstance().kill();
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(error);
	}

	public static void setError(int error)
	{
		Build.error = error;
	}
	public static void main(String args[])
	{
		new Build("main.build", args);
	}
}
