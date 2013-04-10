package eu.orionos.build;

public class CompileUnit {
	private String command;
	private Module module;
	private String object;

	public CompileUnit(Module module, String command, String object)
	{
		this.module = module;
		this.command = command;
	}

	public String getCommand()
	{
		return this.command;
	}

	public void markComplete()
	{
		module.mark(this);
		if (!Config.getInstance().silent())
			System.out.println("[ OK ] " + object);
	}
}