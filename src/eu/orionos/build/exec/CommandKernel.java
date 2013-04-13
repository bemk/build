package eu.orionos.build.exec;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.Module;

public class CommandKernel {
	private static CommandKernel instance;
	
	private ArrayList<CommandRunner> runners = new ArrayList<CommandRunner>();
	private Queue<CompileUnit> compileCommands = new ConcurrentLinkedQueue<CompileUnit>();
	private ConcurrentHashMap<String, Module> modules;
	
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
		modules = new ConcurrentHashMap<>(Config.getInstance().threads()+1);
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
		while(!modules.isEmpty())
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
	
	public void runCommand(CompileUnit cmd)
	{
		compileCommands.offer(cmd);
	}


	public void registerModule(Module m)
	{
		if (!modules.containsKey(m.getName()))
		{
			modules.put(m.getName(), m);
		}
	}

	public void unregisterModule(Module m)
	{
		if (modules.containsKey(m.getName()))
		{
			modules.remove(m.getName());
		}
	}

	public boolean getModule(Module m)
	{
		return modules.containsKey(m.getName());
	}
}
