package eu.orionos.build.exec;

public class Command {
	public String cmd[];
	int ret = 0;

	public Command(String cmd[])
	{
		this.cmd = cmd;
	}
	
	public void ret(int ret)
	{
		this.ret = ret;
	}
	public int ret()
	{
		return this.ret;
	}
	public String[] command()
	{
		return this.cmd;
	}
}
