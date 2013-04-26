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
import java.util.concurrent.atomic.AtomicInteger;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.Module;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private AtomicInteger killedThreads = new AtomicInteger(0);
	private int commandsRegistered = 0;
	private Queue<CompileUnit> compileCommands = new ConcurrentLinkedQueue<CompileUnit>();
	private ConcurrentHashMap<String, Module> modules;
	
	public synchronized static CommandKernel getInstance()
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
		modules = new ConcurrentHashMap<String, Module>(Config.getInstance().threads()+1);
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
			try {
			r.start();
			} catch (OutOfMemoryError e) {
				System.err.println("Too many threads drained the memory resources.");
				System.err.println("Thread count: " + i);
				Runtime.getRuntime().halt(ErrorCode.GENERIC);
			}
			if (Config.getInstance().verbose())
			{
				if (i%16 == 0 && i != 0)
					System.err.println("Starting worker thread: " + i);
				i++;
			}
		}
	}

	public synchronized void stopThreads()
	{
		/* Skip this step if no command was ever issued */
		while(!modules.isEmpty() && getNoCommands() != 0)
		{
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		for (CommandRunner r : runners)
		{
			r.haltThread();
		}
		while (this.killedThreads.get() < this.runners.size())
		{
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public CompileUnit getCommand()
	{
		return compileCommands.poll();
	}
	
	public synchronized void runCommand(CompileUnit cmd)
	{
		compileCommands.offer(cmd);
		commandsRegistered ++;
	}

	public synchronized int getNoCommands()
	{
		return commandsRegistered;
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
	public void unregisterTask(CommandRunner r)
	{
		killedThreads.incrementAndGet();
	}

	public boolean getModule(Module m)
	{
		return modules.containsKey(m.getName());
	}
}
