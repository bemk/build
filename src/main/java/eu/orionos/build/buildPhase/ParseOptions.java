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
package eu.orionos.build.buildPhase;

import java.io.FileNotFoundException;
import java.io.IOException;

import eu.orionos.build.option.Options;

/**
 * @author bemk
 * This class outlines the state when parsing the commandline options
 */
public class ParseOptions extends Phase {

	/**
	 * @param manager
	 */
	public ParseOptions(PhaseManager manager) {
		super(manager);
	}

	/**
	 * @see eu.orionos.build.buildPhase.Phase#run()
	 */
	@Override
	public void run() {
		try {
			new Options(this.manager.getCmd());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
