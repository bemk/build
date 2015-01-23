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
package eu.orionos.build.configGenerator;

import java.util.ArrayList;

import net.michelmegens.xterm.Color;

import org.json.JSONObject;

import eu.orionos.build.Config;
import eu.orionos.build.ui.CLI;

public abstract class Flag {
	protected String key;
	protected String info;
	protected boolean configured = false;
	protected boolean mandatory = false;
	protected DepFile depfile = null;

	public Flag(String key, DepFile depfile)
	{
		this.key = key;
		this.depfile = depfile;
		this.depfile.registerFlag(this);
	}

	public Flag(String key, String info, DepFile depfile)
	{
		this.key = key;
		this.info = info;
		this.depfile = depfile;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	public abstract void configure();
	public abstract void setEnabled();
	public abstract boolean getEnabled();
	public abstract ArrayList<String> getConfigFlags();
	public abstract JSONObject getDepFlags();

	protected boolean getBoolean(String msg)
	{
		StringBuilder question = new StringBuilder(msg);
		if (Config.getInstance().colors()) {
			question.append(Color.GREEN);
		}
		question.append(this.key);
		if (Config.getInstance().colors()) {
			question.append(Color.BLUE);
		}
		question.append(" (Yes/No/Info) ");
		if (Config.getInstance().colors()) {
			question.append(Color.DEFAULT);
		}
		boolean finished = false;
		boolean val = false;
		while (!finished)
		{
			String Answer = CLI.getInstance().readline(question.toString()).toLowerCase();
			if (Answer.equals("y") || Answer.equals("yes"))
			{
				finished = true;
				val = true;
			}
			else if (Answer.equals("n") || Answer.equals("no"))
			{
				finished = true;
				val = false;
			}
			else if (Answer.equals("i") || Answer.equals("info"))
			{
				StringBuilder infoString = new StringBuilder();
				if (Config.getInstance().colors()) {
					infoString.append(Color.YELLOW);
				}
				infoString.append(info);
				if (Config.getInstance().colors()) {
					infoString.append(Color.DEFAULT);
				}
				
				CLI.getInstance().writeline(infoString.toString());
			}
		}
		return val;
	}

	public boolean configured()
	{
		return this.configured;
	}
}
