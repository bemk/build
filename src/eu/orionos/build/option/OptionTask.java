package eu.orionos.build.option;

import java.util.Date;
import java.util.Random;

import eu.orionos.build.Config;
import eu.orionos.build.ErrorCode;

public class OptionTask extends Option {

	public OptionTask()
	{
		this('t', "tasks", true);
	}
	public OptionTask(char c, String s, boolean operands) {
		super(c, s, operands);
	}

	@Override
	public void option() {
		if (this.operand.equals("random"))
		{
			Random r = new Random(new Date().getTime());
			int tasks = r.nextInt(15) + 1;
			Config.getInstance().threads(tasks);
			System.out.println("Running with " + tasks +" worker threads");
			return;
		}
		try {
		int tasks = Integer.parseInt(this.operand);
		if (tasks == 0)
			tasks = 1;
		Config.getInstance().threads(tasks);
		} catch (NumberFormatException e)
		{
			System.err.println("Operand " + this.operand + " to -t or --tasks is not a valid number!");
			System.exit(ErrorCode.OPTION_UNSPECIFIED);
		}
	}

	@Override
	public String help() {
		return " -t | --tasks [n]\t Specify the number of tasks to use";
	}

}
