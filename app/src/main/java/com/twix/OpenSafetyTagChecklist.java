package com.twix;

import java.io.Serializable;
import java.util.ArrayList;

public class OpenSafetyTagChecklist implements Serializable
	{
	private static final long serialVersionUID = -286741272119954607L;
	
	public int serviceTagId;
	public String checkListDate;
	public String comments;
	public ArrayList<ChecklistItem> items;
	
	public OpenSafetyTagChecklist()
		{
		}
	
	}