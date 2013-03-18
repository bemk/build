package eu.orionos.build;

import java.io.IOException;
import org.json.simple.parser.ParseException;

public class Build {
	
	private BuildUnit units;
	private Config cfg;
	
	private static final int BUILD = 1;
	private static final int CLEAN = 2;
	
	public Build(String path, int task)
	{
		try {
			this.cfg = Config.getInstance(".config");
			this.cfg.configure();
			this.units = new BuildUnit(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		if (task == BUILD)
		{
			try {
				units.compile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DisabledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Compilation failed on module: " + e.getMsg());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Compilation failed on module: " + e.getMsg());
			}
		}
		else
		{
			units.clean();
		}
	}
	
	public static void main(String args[])
	{
		if (args.length > 0)
		{
			if (args[0].equals("clean"))
			{
				new Build("main.build", CLEAN);
				System.exit(0);
			}
		}
		new Build("main.build", BUILD);
	}
}
