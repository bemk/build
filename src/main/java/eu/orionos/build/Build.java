/*  Build - Hopefully a simple build system
    Copyright (C)
        Bart Kuivenhoven   <bemkuivenhoven@gmail.com> - 2013
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

import eu.orionos.build.configGenerator.ConfigFile;
import eu.orionos.build.configGenerator.DepFile;
import eu.orionos.build.configGenerator.DepfileException;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.option.Options;
import eu.orionos.build.phase.Compile;
import eu.orionos.build.phase.Configure;
import eu.orionos.build.phase.InitialPreconfigure;
import eu.orionos.build.phase.ParseOptions;
import eu.orionos.build.phase.PhaseManager;
import eu.orionos.build.phase.Preconfigure;

import org.json.JSONObject;

import eu.orionos.build.ui.CLI;
import eu.orionos.build.ui.CLIError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class Build {
	
	private Module modules;
	private static int error = ErrorCode.SUCCESS;
	private PhaseManager manager;

	public Build(String path, String args[])
	{
		manager = new PhaseManager(args);
		manager.switchPhases(new ParseOptions(manager));
		
		if (Config.getInstance().genConfigFile()) {
			manager.switchPhases(new Configure(manager));
		} else if (Config.getInstance().updateDepFile()) {
			manager.switchPhases(new Preconfigure(manager));
		} else if (Config.getInstance().genDepFile()) {
			manager.switchPhases(new InitialPreconfigure(manager));
		} else {
			manager.switchPhases(new Compile(manager));
		}

		System.exit(error);
	}

	public static void setError(int error)
	{
		Build.error = error;
	}
	public static int getError()
	{
		return Build.error;
	}
	public static void main(String args[])
	{
		new Build("main.build", args);
	}
	
	public static int terminalWidth()
	{
		String cols = System.getenv("COLUMNS");
		if(cols==null)
			return 80; // Assume 80 lines as default terminal width, if no actual with is found
		return Integer.parseInt(cols);
	}
	
	public static Version getVersion()
	{
		Version version = new Version();
		
		try {
			Enumeration<URL> en = Build.class.getClassLoader().getResources("version.properties");
			
			URL url = en.nextElement();;
			
			if(url == null)
				return version;
			
			InputStream is = url.openStream();
			
			if(is == null)
				return version;
			
			int data;
			String varName = "";
			String varVal = "";
			
			while( (data = is.read()) != -1 )
			{
				if( data == '\n' )
				{
					if(varName.equalsIgnoreCase("version.major"))
					{
						version.major = Integer.parseInt(varVal);
					}
					else if(varName.equalsIgnoreCase("version.minor"))
					{
						version.minor = Integer.parseInt(varVal);
					}
					else if(varName.equalsIgnoreCase("version.build"))
					{
						version.build = Integer.parseInt(varVal);
					}
					
					varName = "";
					varVal = "";
				}
				else if( data == '=' )
				{
					varName = varVal;
					varVal = "";
				}
				else if( data != ' ' && data != '\t' )
				{
					varVal += (char)data;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return version;
	}
	
	public static class Version
	{
		public int major = -1;
		public int minor = -1;
		public int build = -1;
	}
}
