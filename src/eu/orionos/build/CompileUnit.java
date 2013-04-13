package eu.orionos.build;

public class CompileUnit {
	private String command[];
	private Module module;
	private String object;

	public CompileUnit(Module module, String command[], String object)
	{
		this.module = module;
		this.command = command;
		this.object = object;
	}

	public String[] getCommand()
	{
		return this.command;
	}

	public void markComplete()
	{
		module.markCompileUnitDone(this);
		if (!Config.getInstance().silent())
			System.out.println("[ OK ] " + object);
	}

	public String key()
	{
		return module.getName() + object;
	}

	public Module getModule()
	{
		return module;
	}

	public String getObject()
	{
		return this.object;
	}
}
