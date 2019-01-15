package com.twix;

import java.io.Serializable;

public class PMChecklist implements Serializable
	{
	private static final long serialVersionUID = -8856904972496825370L;
	
	public int pmChecklistId;
	public int serviceTagUnitId;
	
	public String itemText;
	public String itemType;
	public String itemValue;
	public String itemComment;
	public String identifier;
	}