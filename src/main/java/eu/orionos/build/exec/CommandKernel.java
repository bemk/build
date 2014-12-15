/*  Build - Hopefully a simple build system
    Copyright (C) 2013 - 2014 - Bart Kuivenhoven

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.orionos.build.Build;
import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;
import eu.orionos.build.Module;
import eu.orionos.build.ui.CLIDebug;
import eu.orionos.build.ui.CLIError;

public class CommandKernel implements Runnable {
	private static CommandKernel instance;

	private List<CommandRunner> runners = new ArrayList<CommandRunner>();
	// private AtomicInteger killedThreads = new AtomicInteger(0);
	// private Queue<CompileUnit> compileCommands = new
	// ConcurrentLinkedQueue<CompileUnit>();
	private List<CompileUnit> compileCommandsList = new LinkedList<CompileUnit>();
	private ConcurrentHashMap<String, Module> modules;
	private Config c = Config.getInstance();

	private static final Object instanceLock = new Object();

	public static CommandKernel getInstance() {
		synchronized (instanceLock) {
			if (instance == null) {
				instance = new CommandKernel();
				Thread t = new Thread(instance);
				t.start();
			}
		}
		return instance;
	}

	private CommandKernel() {
		modules = new ConcurrentHashMap<String, Module>(c.threads() + 1);

	}

	private void startThreads() {
		int i = 0;
		for (CommandRunner r : runners) {
			i++;
			try {
				Thread t = new Thread(r);
				t.setName("CommandRunner-" + i);
				t.start();
			} catch (OutOfMemoryError e) {
				CLIError.getInstance().writeline("Thread count: " + i);
				Build.panic(
						"Too many threads have drained the memory resources",
						ErrorCode.GENERIC);
			}
			if (c.verbose()) {
				if ((i | 0xF) == 0 && i != 0)
					CLIDebug.getInstance().writeline(
							"Starting worker thread: " + i);
			}
		}
	}

	public void stopThreads() {
		/* Skip this step if no command was ever issued */
		while (!modules.isEmpty() && getNoCommands() != 0) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		killThreads();
		// waitThreads();
	}

	public void killThreads() {
		for (CommandRunner r : runners) {
			r.haltThread();
		}
	}

	public CompileUnit getCommand() throws InterruptedException {
		CompileUnit ret = null;

		synchronized (compileCommandsList) {
			try {
				ret = compileCommandsList.remove(0);
			} catch (IndexOutOfBoundsException e) {
				compileCommandsList.wait(5);
				try {
					ret = compileCommandsList.remove(0);
				} catch (IndexOutOfBoundsException ee) {
					ret = null;
				}
			}
		}

		return ret;
	}

	public void runCommand(CompileUnit cmd) {
		synchronized (compileCommandsList) {
			compileCommandsList.add(cmd);
			compileCommandsList.notify();
		}
	}

	public int getNoCommands() {
		synchronized (compileCommandsList) {
			return compileCommandsList.size();
		}
	}

	public void dumpCommands() {
		synchronized (compileCommandsList) {
			Iterator<CompileUnit> cs = compileCommandsList.iterator();
			while (cs.hasNext()) {
				CompileUnit cu = cs.next();
				System.err.println("Command: " + cu.getName() + " : "
						+ cu.getObject());
			}
		}
	}

	public void registerModule(Module m) {
		if (!modules.containsKey(m.getName())) {
			modules.put(m.getName(), m);
		}
	}

	public void unregisterModule(Module m) {
		if (modules.containsKey(m.getName())) {
			modules.remove(m.getName());
		}
	}

	public void unregisterTask(CommandRunner r) {
	}

	public boolean getModule(Module m) {
		return modules.containsKey(m.getName());
	}

	@Override
	public void run() {
		for (int i = 0; i < c.threads(); i++) {
			runners.add(new CommandRunner());
		}
		this.startThreads();
	}
}
