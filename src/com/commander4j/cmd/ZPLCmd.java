package com.commander4j.cmd;

import java.util.LinkedList;

import com.commander4j.util.ZPLUtility;
import com.commander4j.zpl.ZPLCommon;

public class ZPLCmd
{
	private String command = "";
	private LinkedList<String> arguments = new LinkedList<String>();
	private ZPLUtility utils = new ZPLUtility();
	private String uuid = "";
	
	public ZPLCmd(String uuid)
	{
		this.uuid = uuid;
	}
	
	public void setCommand(String cmd)
	{
		command = cmd;
	}

	public String getCommand()
	{
		return command;
	}

	public void setArguments(LinkedList<String> args)
	{
		arguments = args;
	}
	
	public void updateArgument(Integer seq,String value)
	{
		if (seq <= getArgumentCount())
		{
			arguments.set(seq, value);
		}
	}

	public void addArgument(String arg)
	{
		arguments.addLast(arg);
	}

	public Integer getArgumentCount()
	{
		return arguments.size();
	}
	
	public String displayArguments()
	{
		String result = "";
		String one = "";
		
		for (int x=0;x<arguments.size();x++)
		{
			one = arguments.get(x);
			
			if (one.equals("")==false)
			{
				if (result.equals(""))
				{
					result = result + one;
				}
				else
				{
					result = result + ","+ one;
				}
			}
		}
		
		return "{"+result+"}";
	}

	public String getArgument(Integer seq)
	{

		String result = "";
		
		if (seq <= getArgumentCount())
		{
			result = arguments.get(seq);
		}
		else
		{
			result = "";
		}
		
		return result;
	}
	
	public String toString()
	{
		String result = getCommand();
		

		String arguments = "";
		
		
		if (getArgumentCount() > 0 )
		{
			for (int x = 0;x<getArgumentCount();x++)
			{
				arguments = arguments + getArgument(x);
				
				if (x<getArgumentCount()-1)
				{
					arguments = arguments + ZPLCommon.config.get(uuid).state.delimiter;	
				}
			}
		}

		arguments = utils.padString(arguments, true, 60, " ");
		
		ZPLCmdInfo desc;
		
		if (ZPLCommon.config.get(uuid).zplindex.zplDescription.containsKey(getCommand()))
		{
			desc = ZPLCommon.config.get(uuid).zplindex.zplDescription.get(getCommand());
		}
		else
		{
			 desc = new ZPLCmdInfo("Unknown Command "+getCommand(),"",false) ;
		}
		
		String printdesc = utils.padString(desc.description, true, 60, " ");
		
		result = result +  " "+arguments+ " "+printdesc + " "+uuid;
		
		return result;
	}

}
