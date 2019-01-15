package com.twix;

import java.io.Serializable;

public class ChecklistItem implements Serializable
	{
	private static final long serialVersionUID = -3421591261353626598L;
	
	public int serviceTagId;
	public int safetyChecklistId;
	public String itemRequired;
	public String itemValue;
	}