package eu.orionos.build.exec;

import java.util.ArrayList;

public class CommandSet {
	private ArrayList<Command> commands = new ArrayList<Command>();
	int idx = 0;

	public CommandSet()
	{
		
	}

	public CommandSet(String cmds[][])
	{
		for (String[] i : cmds)
		{
			commands.add(new Command(i));
		}
	}

	public void addCommand(String[] cmd)
	{
		commands.add(new Command(cmd));
	}

	public Command getCmd()
	{
		if (idx < commands.size())
		{
			return commands.get(idx++);
		}
		return null;
	}
}
