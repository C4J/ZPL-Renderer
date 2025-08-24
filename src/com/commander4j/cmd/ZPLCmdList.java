package com.commander4j.cmd;

import java.util.LinkedList;

public class ZPLCmdList
{

	private LinkedList<ZPLCmd> zpllist = new LinkedList<ZPLCmd>();
	private String uuid = "";
	
	public ZPLCmdList(String uuid)
	{
		this.uuid = uuid;
	}
	
	public LinkedList<ZPLCmd> getCommands()
	{
		return zpllist;
	}
	
	public void clearCommands()
	{
		zpllist.clear();
	}
	
	public void addCommand(ZPLCmd cmd)
	{
		zpllist.addLast(cmd);	
	}
	
	public Integer getCommandCount()
	{
		return zpllist.size();
	}
	
	public ZPLCmd getCommand(Integer seq)
	{
		ZPLCmd result = new ZPLCmd(uuid);
	
		
		if (seq <= getCommandCount())
		{
			Integer index = seq - 1;
			result = zpllist.get(index);
		}
		else
		{
			result.setCommand("beyond end of list");
		}
		
		return result;
		

	}
	
}
