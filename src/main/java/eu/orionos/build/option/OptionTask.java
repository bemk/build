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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;

import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

public class OptionTask extends Option {
	private static final int max_factor = 8;

	private OptionTask(char c, String s, boolean operands) {
		super(c, s, operands, "[n | random | cores]", "Specify the number of tasks to build with. Random selects a random number of threads between 1 and 16. Cores selects the number of cpu's available to build.");
	}

	public OptionTask()
	{
		this('t', "tasks", true);
	}

	@Override
	public void option() {
		int cores = Runtime.getRuntime().availableProcessors();
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
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
			System.out.println("Running with " + cores + " worker treads");
			Config.getInstance().threads(cores);
			return;
		}
		try {
			int tasks = Integer.parseInt(this.operand);
			if (tasks == 0)
				tasks = 1;
			else if (tasks > cores * max_factor)
			{
				System.out.println("You are attempting to run " + max_factor + " times more threads than you have cores available");
				System.out.println("Are you certain you don't want to scale down to " + (cores * max_factor) + " threads? (y/N)");
				String ret = input.readLine().toLowerCase();
				if (!(ret.equals("y") || ret.equals("yes")))
				{
					System.out.println("Setting the number of tasks to " + (cores * max_factor));
					tasks = cores * max_factor;
				}
			}
			Config.getInstance().threads(tasks);
		} catch (NumberFormatException e)
		{
			System.err.println("Operand " + this.operand + " to -t or --tasks is not a valid number!");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		} catch (IOException e) {
			System.err.println("System input broke somehow ...");
			System.exit(ErrorCode.GENERIC);
		}
	}

}
