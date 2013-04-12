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
	private Queue<CompileUnit> commands = new ConcurrentLinkedQueue<CompileUnit>();
	private ConcurrentHashMap<String, CompileUnit> waiting;
	
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
		waiting = new ConcurrentHashMap<>(Config.getInstance().threads()+1);
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
		while(!commands.isEmpty() && !waiting.isEmpty())
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
		return commands.poll();
	}
	
	public void runCommand(CompileUnit cmd)
	{
		commands.offer(cmd);
	}

	public void runWaitingCommand(CompileUnit cmd)
	{
		waiting.put(cmd.getModule().getName(), cmd);
	}

	public void signalDone(String module)
	{
		while (waiting.containsKey(module))
		{
			commands.add(waiting.get(module));
			waiting.remove(module);
		}
	}
}
