package eu.orionos.build.option;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class OptionConfig extends Option {

	public OptionConfig(char c, String s, boolean operands) {
		super(c, s, operands);
	}

	public OptionConfig()
	{
		this('\0', "config", true);
	}

	@Override
	public void option() {
		try {
			eu.orionos.build.Config.getInstance().override(this.operand);
		} catch (IOException | ParseException e) {
			System.err.println("Something went wrong in switching config files!");
		}
	}

	@Override
	public String help() {
		return "   | --config [config file]\t Select an alternative config file";
	}

}
