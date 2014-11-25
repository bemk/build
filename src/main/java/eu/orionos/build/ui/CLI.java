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
package eu.orionos.build.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.orionos.build.Config;
import net.michelmegens.xterm.Color;

public class CLI extends Thread {
	private static CLI cli = null;
	protected static boolean finished = false;
	protected ArrayList<String> out = new ArrayList<String>();
	private Lock lock = new ReentrantLock(true);
	private BufferedReader r = new BufferedReader(new InputStreamReader(
			System.in));
	protected static Lock instanceLock = new ReentrantLock(true);
	protected String prefix = "";
	private ConcurrentHashMap<String, Thread> threads = new ConcurrentHashMap<String, Thread>();
	private String name = "";
	private boolean silent = false;

	public static CLI getInstance() {
		instanceLock.lock();
		if (cli == null)
			cli = new CLI("CLI");
		instanceLock.unlock();
		return cli;
	}

	protected CLI(String name) {
		this.start();
		this.name = name;
		threads.put(name, this);
		if (Config.getInstance().colors() && name.equals("CLI")) {
			prefix = Color.DEFAULT;
		} 
	}

	public void writeline(String msg) {
		this.write(msg + "\n");
	}

	public void write(String msg) {
		if (this.silent) {
			return;
		}
		this.getLock();
		this.out.add(prefix + msg);
		this.unlock();
		Thread.yield();
	}

	public String readline(String msg) {
		String ret = null;
		if (this.silent) {
			return ret;
		}
		while (ret == null) {
			this.getLock();
			if (this.out.isEmpty()) {
				try {
					System.out.print(msg);
					ret = r.readLine();
				} catch (IOException e) {
					ret = "";
				}
			} else
				Thread.yield();
			this.unlock();
		}
		return ret;
	}

	public boolean readboolean(String msg) {
		if (this.silent) {
			return false;
		}
		String ret = this.readline(msg + " [y/N] ").toLowerCase();
		if (ret.equals("y") || ret.equals("yes") || ret.equals("true")
				|| ret.equals("1")) {
			return true;
		}
		return false;
	}

	public int readint(String msg) {
		if (this.silent) {
			return 0;
		}
		String str = this.readline(msg + " [0..9] ").toLowerCase();
		while (true) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				str = this.readline("Invalid number").toLowerCase();
			}
		}
	}

	public void run() {
		while (!finished || !out.isEmpty()) {
			Thread.yield();
			this.getLock();
			Iterator<String> i = out.iterator();
			while (i.hasNext()) {
				System.out.print(i.next());
				i.remove();
			}
			this.unlock();
		}
		CLI.getInstance().markDone(name);
	}

	public void kill() {
		finished = true;
	}

	protected void getLock() {
		lock.lock();
	}

	protected void unlock() {
		lock.unlock();
	}

	protected void markDone(String name) {
		threads.remove(name);
	}

	public boolean getDone() {
		return threads.isEmpty();
	}

	protected void setSiltent() {
		this.silent = true;
	}
}
