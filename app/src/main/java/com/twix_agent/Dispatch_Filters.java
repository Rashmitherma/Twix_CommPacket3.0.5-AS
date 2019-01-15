package com.twix_agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dispatch_Filters implements Serializable
	{
	private static final long	serialVersionUID	= 3172541402394879282L;
	public boolean				NoTags				= true;
	public boolean				MustReturn			= true;
	public boolean				CallComplete		= true;
	public boolean				Mech1				= true;
	public boolean				Mech2				= true;

	public List<String>			Mech1List;
	public List<String>			Mech2List;

	public Dispatch_Filters()
		{
		Mech1List = new ArrayList<String>();
		Mech2List = new ArrayList<String>();
		}

	public void reset()
		{
		NoTags = true;
		MustReturn = true;
		CallComplete = true;
		Mech1 = true;
		Mech2 = true;

		if (Mech1List == null)
			Mech1List = new ArrayList<String>();

		if (Mech2List == null)
			Mech2List = new ArrayList<String>();

		}
	
	public boolean isFiltering()
		{
		return (!NoTags || !MustReturn || !CallComplete || (Mech1 && Mech1List.size() > 0) || (Mech2 && Mech2List.size() > 0));
		}
	
	}
