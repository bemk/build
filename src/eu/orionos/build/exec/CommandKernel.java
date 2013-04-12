package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CompileUnit> compileCommands = new ConcurrentLinkedQueue<CompileUnit>();
	private ConcurrentHashMap<String, CompileUnit> archiveCommands;
	
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
		archiveCommands = new ConcurrentHashMap<>(Config.getInstance().threads()+1);
		for (int i = 0; i < Config.getInstance().threads(); i++)
		{
			runners.add(new CommandRunner());
		}
	}
	private void startThreads()
	{
		for (CommandRunner r : runners)
		{
			r.start();
		}
	}
	public void stopThreads()
	{
		while(!compileCommands.isEmpty() && !archiveCommands.isEmpty())
		{
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
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
		return compileCommands.poll();
	}
	
	public void runCompileCommand(CompileUnit cmd)
	{
		compileCommands.offer(cmd);
	}

	public void runWaitingCommand(CompileUnit cmd)
	{
		archiveCommands.put(cmd.getModule().getName(), cmd);
	}

	public void signalNextPhase(String module)
	{
		while (archiveCommands.containsKey(module))
		{
			compileCommands.add(archiveCommands.get(module));
			archiveCommands.remove(module);
		}
	}
}
