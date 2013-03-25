package eu.orionos.build.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import eu.orionos.build.Config;

public class CommandRunner extends Thread {
	public CommandRunner()
	{
		super();
	}
	
	private void writeStream(InputStream p, PrintStream out) throws IOException
	{
		if (Config.getInstance().verbose() && !Config.getInstance().silent())
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(p));
			String line;
			while ((line = in.readLine()) != null)
			{
				out.println(line);
			}
		}
	}

	private void writeCmd(String cmd[])
	{
		if (!Config.getInstance().silent())
		{
			for (String s : cmd)
			{
				System.out.print(s + " ");
			}
			System.out.println("");
		}
	}
	
	@Override
	public void run()
	{
		super.run();
		while (true)
		{
			CommandSet c = CommandKernel.getInstance().getCommand();
			Command cmd;
			if (c != null)
			{
				try {
					while ((cmd = c.getCmd()) != null)
					{
						Runtime r = Runtime.getRuntime();
						Process p = r.exec(cmd.command());

						writeCmd(cmd.command());
						writeStream(p.getErrorStream(), System.err);
						writeStream(p.getInputStream(), System.out);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
