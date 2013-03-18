package eu.orionos.build;

import java.io.IOException;
import org.json.simple.parser.ParseException;

public class Build {
	
	private BuildUnit units;
	
	public Build(String path)
	{
		try {
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
	}
	
	public static void main(String args[])
	{
		new Build("main.build");
	}
}
