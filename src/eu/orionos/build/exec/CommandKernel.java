package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.orionos.build.CompileUnit;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CompileUnit> sets = new ConcurrentLinkedQueue<CompileUnit>();
	
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
	
	public CompileUnit getCommand()
	{
		return sets.poll();
	}
	
	public void runCommand(CompileUnit set)
	{
		sets.offer(set);
	}
}
