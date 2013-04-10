package eu.orionos.build.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import eu.orionos.build.CompileUnit;
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

	private void writeCmd(String cmd)
	{
		if (!Config.getInstance().silent() && Config.getInstance().verbose())
		{
			System.out.println(cmd);
		}
	}

	@Override
	public void run()
	{
		super.run();
		while (true)
		{
			CompileUnit c = CommandKernel.getInstance().getCommand();
			if (c != null)
			{
				try {
					writeCmd(c.getCommand());
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(c.getCommand());

					writeStream(p.getErrorStream(), System.err);
					writeStream(p.getInputStream(), System.out);

					c.markComplete();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
