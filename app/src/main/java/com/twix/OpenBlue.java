package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OpenBlue implements Serializable
	{
	private static final long serialVersionUID = -8856904972496825370L;
	
	public int blueId;
	public int serviceTagId;
	public String dateCreated;
	
	public List<OpenBlueUnit> units;
	
	public OpenBlue()
		{
		units = new ArrayList<OpenBlueUnit>();
		}
	
	}