package com.commander4j.zpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.commander4j.cmd.ZPLCmd;
import com.commander4j.cmd.ZPLCmdList;

public class ZPLParser
{

	String zplBytes = "";
	String uuid = "";
	
	public ZPLParser(String uuid)
	{
		this.uuid = uuid;
	}

	public String readFromFile(String filename)
	{
		String result = "";

		File file = new File(filename);

		try
		{
			result = new String(Files.readAllBytes(file.toPath()));
			result = result.replace("\n", "");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public ZPLCmdList parseBytes(String data)
	{
		ZPLCmdList result = new ZPLCmdList(uuid);
		
		String command = "";
		String arguments = "";
		
		data=data+"^";

		if (data.length() > 0)
		{

			String sequence = "";

			for (int x = 0; x < data.length(); x++)
			{
				String onechar = data.substring(x, x + 1);
				

				if ( (onechar.equals("^") || (onechar.equals("~"))))
				{
					
					sequence = onechar;
					
					if (command.equals("") == false)
					{
						ZPLCmd cmd = new ZPLCmd(uuid);
						
						cmd.setCommand(command);
						
						if (arguments.length() > 0)
						{
							String[] arglist;
							if (command.equals("^FD") == true)
							{
								arglist = new String[] {arguments};
							}
							else
							{
								arglist = arguments.split(ZPLCommon.config.get(uuid).state.delimiter);
							}
							
							for (int z = 0; z < arglist.length; z++)
							{
								cmd.addArgument(arglist[z]);
							}
						}
						
						if (cmd.getCommand().equals("^CD") || cmd.getCommand().equals("~CD"))
						{
							if (arguments.length() > 0)
							{
								ZPLCommon.config.get(uuid).state.delimiter=cmd.getArgument(0);
							}
						}
						
						result.addCommand(cmd);
						
						command = "";
						arguments = "";

					}
				}
				else
				{
					sequence = sequence + onechar;
				}

				if (sequence.length() <= 3)
				{

					command = command + onechar;
					arguments = "";

				}
				else
				{
					arguments = arguments + onechar;
				}

			}
		}

		return result;
	}

}
