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

package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.Module;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CompileUnit> compileCommands = new ConcurrentLinkedQueue<CompileUnit>();
	private ConcurrentHashMap<String, Module> modules;
	
	public static CommandKernel getInstance()
	{
		if (instance == null)
		{
			instance = new CommandKernel();
			instance.startThreads();
		}
		return instance;
	}

	private CommandKernel()
	{
		modules = new ConcurrentHashMap<>(Config.getInstance().threads()+1);
		for (int i = 0; i < Config.getInstance().threads(); i++)
		{
			runners.add(new CommandRunner());
		}
	}

	private void startThreads()
	{
		int i = 0;
		for (CommandRunner r : runners)
		{
			r.start();
			if (i%16 == 0 && i != 0)
				System.err.println("Starting worker thread: " + i);
			i++;
		}
	}

	public void stopThreads()
	{
		while(!modules.isEmpty())
		{
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		for (CommandRunner r : runners)
		{
			try {
				r.haltThread();
				r.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public CompileUnit getCommand()
	{
		return compileCommands.poll();
	}
	
	public void runCommand(CompileUnit cmd)
	{
		compileCommands.offer(cmd);
	}


	public void registerModule(Module m)
	{
		if (!modules.containsKey(m.getName()))
		{
			modules.put(m.getName(), m);
		}
	}

	public void unregisterModule(Module m)
	{
		if (modules.containsKey(m.getName()))
		{
			modules.remove(m.getName());
		}
	}

	public boolean getModule(Module m)
	{
		return modules.containsKey(m.getName());
	}
}
