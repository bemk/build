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
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.orionos.build.Config;
import net.michelmegens.xterm.Color;

public class CLI implements Runnable {
	private static CLI cli = null;
	protected static boolean finished = false;
	protected ConcurrentLinkedQueue<String> out = new ConcurrentLinkedQueue<String>();
	private BufferedReader r = new BufferedReader(new InputStreamReader(
			System.in));
	protected static Lock instanceLock = new ReentrantLock(true);
	protected String prefix = "";
	private ConcurrentHashMap<String, Thread> threads = new ConcurrentHashMap<String, Thread>();
	private String name = "";
	private boolean silent = false;

	private static Object synchronizedObject = new Object();

	private static String numberFormat = " [0 .. 9] ";
	private static String booleanFormat = " [y/N] ";

	public static CLI getInstance() {
		synchronized (synchronizedObject) {
			if (cli == null) {
				cli = new CLI("CLI");
			}
		}
		return cli;
	}

	protected CLI(String name) {
		Thread t = new Thread(this);
		t.setName("CLI-" + name);
		t.start();
		this.name = name;
		threads.put(name, t);
		if (Config.getInstance().colors() && name.equals("CLI")) {
			prefix = Color.DEFAULT;

			numberFormat = new StringBuilder(Color.BLUE).append(numberFormat)
					.append(Color.DEFAULT).toString();
			booleanFormat = new StringBuilder(Color.BLUE).append(booleanFormat)
					.append(Color.DEFAULT).toString();
		}
	}

	public void writeline(String msg) {
		this.write(msg, true);
	}

	public void write(String msg) {
		write(msg, false);
	}

	public void write(String msg, boolean endline) {
		if (this.silent) {
			return;
		}
		StringBuilder message = new StringBuilder(prefix);
		message.append(msg);
		if (endline) {
			message.append("\n");
		}
		synchronized (out) {
			this.out.add(message.toString());
			out.notifyAll();
		}
	}

	public String readline(String msg) {
		String ret = null;
		if (this.silent) {
			return ret;
		}
		while (ret == null) {
			if (this.out.isEmpty()) {
				try {
					this.write(msg);
					ret = r.readLine();
				} catch (IOException e) {
					ret = "";
				}
			} else {
				Thread.yield();
			}
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
			try {
				StringBuilder str = new StringBuilder();
				synchronized (out) {
					for (String next = out.poll(); next != null; next = out
							.poll()) {
						str.append(next);
					}
					out.wait(100);
				}
				System.out.print(str.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		CLI.getInstance().markDone(name);
	}

	public void kill() {
		finished = true;
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
