/*  Build - Hopefully a simple build system
    Copyright (C) 2013 - Bart Kuivenhoven

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

package eu.orionos.build.option;

import java.util.Date;
import java.util.Random;

import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

public class OptionTask extends Option {

	public OptionTask()
	{
		this('t', "tasks", true);
	}
	public OptionTask(char c, String s, boolean operands) {
		super(c, s, operands);
	}

	@Override
	public void option() {
		if (this.operand.equals("random"))
		{
			Random r = new Random(new Date().getTime());
			int tasks = r.nextInt(15) + 1;
			Config.getInstance().threads(tasks);
			System.out.println("Running with " + tasks + " worker threads");
			return;
		}
		if (this.operand.equals("cores"))
		{
			int cores = Runtime.getRuntime().availableProcessors();
			System.out.println("Running with " + cores + " worker treads");
			return;
		}
		try {
		int tasks = Integer.parseInt(this.operand);
		if (tasks == 0)
			tasks = 1;
		if (tasks > 40)
		{
			System.out.println("Are you certain you want to run " + tasks + " worker threads?(y/N)");
			String ret = System.console().readLine().toLowerCase();
			if (!(ret.equals("y") || ret.equals("yes")))
			{
				System.out.println("Setting the number of tasks to 40");
				tasks = 40;
			}
		}
		Config.getInstance().threads(tasks);
		} catch (NumberFormatException e)
		{
			System.err.println("Operand " + this.operand + " to -t or --tasks is not a valid number!");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
	}

	@Override
	public String help() {
		return "-t | --tasks [n]\n\t" +
				"-t | --tasks [random | cores]\n\t\t\t" + 
				"Specify the number of tasks to build with.\n\t\t\t" +
				"Random selects a random number of threads between 1 and 16." +
				"cores selects the number of cpu's available to build.";
	}

}
