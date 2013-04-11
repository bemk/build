package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CompileUnit> sets = new ConcurrentLinkedQueue<CompileUnit>();
	
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
		for (int i = 0; i < Config.getInstance().threads(); i++)
		{
			runners.add(new CommandRunner());
		}
	}
	private void startThreads()
	{
		for (CommandRunner r: runners)
		{
			r.start();
		}
	}
	public void stopThreads()
	{
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
		return sets.poll();
	}
	
	public void runCommand(CompileUnit set)
	{
		sets.offer(set);
	}
}
