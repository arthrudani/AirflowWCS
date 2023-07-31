package com.daifukuamerica.wrxj.util;

public class ManifestCommands extends Commands
{
	private String[] switches = 
	{
		"logf",
		"env",
    "inc",
    "man",
	};

	public ManifestCommands(String[] args)
	{
		super(args, null); 		
		setSwitches(switches);

		try
		{
			parseCommandLine();
		} catch (IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
			help();
			System.exit(-1);
		}
		if (switchPresent("help"))
		{
			help();
			System.exit(-1);
		}
	}

	public void help()
	{
		System.out.println("\njava <class> [commands]");
		System.out.println("where commands are:\n");
		System.out.println(" -help"  + "\t\tDisplay this.");
		System.out.println(" -logf:" + "<file>\tlog file name");
		System.out.println(" -env:" + "<path>\tpath (with file) to configuration properties file");
		System.out.println(" -inc:" + "causes build number to increment");
		System.out.println(" -man:" + "<path>\tcreate manifest file at given path");
		System.out.println("\nall switches are case sensitive");
	}
}
