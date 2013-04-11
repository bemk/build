package eu.orionos.build.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import eu.orionos.build.CompileUnit;
import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

public class CommandRunner extends Thread {
	private boolean runnable = true;

	public CommandRunner()
	{
		super();
	}

	public void haltThread()
	{
		this.runnable = false;
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

	private void writeCmd(String[] cmd)
	{
		if (!Config.getInstance().silent() && Config.getInstance().verbose())
		{
			for (int i = 0; i < cmd.length; i++)
			{
				System.out.print(cmd[i] + " ");
			}
			System.out.println("");
		}
	}

	@Override
	public void run()
	{
		super.run();
		while (runnable)
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

					if (p.waitFor() != 0)
					{
						System.err.println("Could not compute!");
						r.halt(ErrorCode.INSTRUCTION_FAILED);
					}

					c.markComplete();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else
			{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
