package com.commander4j.barcode;

import com.commander4j.memory.ZPLMemory;

public class ZPL_GS1_Interpreter
{

	public String OKIBarCode128FromZPL(ZPLMemory memory, String input)
	{
		String result = "";
		String infeed = input;

		String mode = "appid"; // ai // data
		String appid = "";
		String data = "";
		boolean done = false;

		if (input.length() > 0)
		{
			while (infeed.length() > 0)
			{
				String first1 = left(infeed, 1);

				switch (mode)
				{
				case "appid":
				{
					appid = appid + first1;
					if (memory.app_id_data.containsKey(appid))
					{
						mode = "data";
					}

					break;
				}
				case "data":
				{
					// fnc1?
					if (first1.equals("\u00f1"))
					{
						done = true;
					}
					else
					{
						data = data + first1;
						if (data.length() == memory.app_id_data.get(appid).max_length)
						{
							done = true;
						}
					}

					break;
				}
				}

				infeed = infeed.substring(1);

				if (infeed.length() == 0)
				{
					done = true;
				}

				if (done)
				{
					if ((appid.equals("") == false) && (data.equals("") == false))
					{
						System.out.println("Found GS1 App ID "+appid + " with data " + data);
						result = result + "[" + appid + "]" + data;
						done = false;
						mode = "appid";
						appid = "";
						data = "";
					}

				}

			}
		}

		System.out.println(result);
		return result;
	}

	private String left(String in, int reqd)
	{
		String result = "";
		if (in.length() >= reqd)
		{
			result = in.substring(reqd - 1, reqd);
		}
		else
		{
			result = "";
		}
		return result;
	}

}
