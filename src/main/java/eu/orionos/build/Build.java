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
import eu.orionos.build.configGenerator.DepFile;
import eu.orionos.build.configGenerator.DepfileException;
import eu.orionos.build.exec.CommandKernel;
import eu.orionos.build.option.Options;

import org.json.JSONException;
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

	public Build(String path, String args[])
	{
		try {
			Config.getInstance();
			new Options(args);
			if (Config.getInstance().hasConf() == false)
			{
				Config.getInstance().override(".config");
				if (Config.getInstance().hasConf() == false && !Config.getInstance().genDepFile() && !Config.getInstance().genConfigFile())
				{
					System.err.println("No usable config files found!");
					System.exit(1);
				}
			}
			this.modules = new Module(Config.getInstance().buildFile());
			if (Config.getInstance().genDepFile())
			{
				Set<String> flags = modules.getBuildFlags();
				try {
					File f = new File (Config.getInstance().getDepFile());
					if (!f.exists())
						f.createNewFile();
					if (f.isDirectory())
						throw (new Exception());
					FileWriter fw = new FileWriter(f);

					DepFile d = new DepFile();
					fw.write(d.generateDepFile(flags).toString(8));

					fw.close();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
				} catch (Exception e) {
				}
			}
			else if (Config.getInstance().updateDepFile())
			{
				DepFile d = new DepFile();
				try {
					d.readDepFile();

					Set<String> flags = modules.getBuildFlags();
					JSONObject o = d.updateDepFile(flags);

					File f = new File(Config.getInstance().getDepFile());
					if (!f.exists() || f.isDirectory())
						throw (new Exception());
					FileWriter fw = new FileWriter(f);
					fw.write(o.toString(8) + "\n");
					fw.close();
				}
				catch (DepfileException e)
				{
					CLIError.getInstance().writeline("Missing depfile!");
					CLIError.getInstance().writeline("Run with --gen-depfile first!");
				}
				catch (Exception e)
				{
				}

			}
			else if (Config.getInstance().genConfigFile())
			{
				DepFile d = new DepFile();
				try {
					d.readDepFile();
				}
				catch(DepfileException e)
				{
					d.parseJSON(d.generateDepFile(modules.getBuildFlags()));
				}
				ConfigFile c = d.generateConfigFile();
				try {
					c.write();
				} catch (IOException e)
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
