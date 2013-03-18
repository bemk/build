package eu.orionos.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Config {
	private static JSONObject conf;
	private static Config instance;

	public static Config getInstance()
	{
		return instance;
	}

	public static Config getInstance(String conf) throws FileNotFoundException, IOException, ParseException
	{
		if (instance == null)
			instance = new Config(conf);
		return instance;
	}

	private Config(String conf) throws FileNotFoundException, IOException, ParseException
	{
		Config.conf = (JSONObject)(new JSONParser()).parse(new FileReader(new File(conf)));
	}

	public void configure()
	{

	}

	public String get(String key)
	{
		return (String)conf.get(key);
	}
}
