package com.commander4j.cmd;

public class ZPLCmdInfo
{

	public String description = "";
	public String syntax = "";
	public boolean prints = false;
	
	public ZPLCmdInfo(String desc,String syn,boolean output)
	{
		this.description=desc;
		this.syntax=syn;
		this.prints=output;
	}
	
}
