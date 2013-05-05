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
package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CLI extends Thread {
	private static CLI cli = null;
	private boolean finished = false;
	private ArrayList<String> out = new ArrayList<String>();
	private Lock lock = new ReentrantLock(true);
	private BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

	public static CLI getInstance()
	{
		if (cli == null)
			cli = new CLI();
		return cli;
	}
	private CLI()
	{
		this.start();
	}

	public void writeline(String msg)
	{
		msg += "\n";
		lock.lock();
		this.out.add(msg);
		lock.unlock();
	}
	public void write(String msg)
	{
		lock.lock();
		this.out.add(msg);
		lock.unlock();
	}
	public String readline(String msg)
	{
		String ret = null;
		lock.lock();
		try {
			System.out.print(msg);
			ret = r.readLine();
		} catch (IOException e) {
			ret = "";
		}
		lock.unlock();
		return ret;
	}
	
	public void run()
	{
		while (!finished)
		{
			lock.lock();
			for (String s : out)
			{
				System.out.print(s);
			}
			lock.unlock();
		}
	}
	public void kill()
	{
		try {
			if (this.isAlive())
			{
				this.finished = true;
				this.join();
			}
		} catch (InterruptedException e){
		}
	} 
}
