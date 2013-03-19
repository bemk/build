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

package eu.orionos.build;

import java.io.IOException;
import org.json.simple.parser.ParseException;

public class Build {
	
	private BuildUnit units;
	private Config cfg;

	private static final int BUILD = 1;
	private static final int CLEAN = 2;

	public Build(String path, int task)
	{
		try {
			this.cfg = Config.getInstance(".config");
				if (task == BUILD)
			this.cfg.configure();
			this.units = new BuildUnit(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		if (task == BUILD)
		{
			try {
				units.compile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DisabledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Compilation failed on module: " + e.getMsg());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Compilation failed on module: " + e.getMsg());
			}
		}
		else
		{
			units.clean();
		}
	}
	
	public static void main(String args[])
	{
		if (args.length > 0)
		{
			if (args[0].equals("clean"))
			{
				new Build("main.build", CLEAN);
				System.exit(0);
			}
		}
		new Build("main.build", BUILD);
	}
}
