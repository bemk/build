package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CommandSet> sets = new ConcurrentLinkedQueue<CommandSet>();
	
	public static CommandKernel getInstance()
	{
		if (instance == null)
			instance = new CommandKernel();
		return instance;
	}
	private CommandKernel()
	{
		System.err.println("Initialise the runners here!");
		for (CommandRunner r: runners)
		{
			r.start();
		}
	}
	
	public CommandSet getCommand()
	{
		return sets.poll();
	}
	
	public void runCommandSet(CommandSet set)
	{
		sets.offer(set);
	}
}
