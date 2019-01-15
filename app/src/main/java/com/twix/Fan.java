package com.twix;

import java.io.Serializable;
import java.util.ArrayList;

public class Fan implements Serializable
	{
	private static final long serialVersionUID = 3022128950415357574L;
	
	// Fan Details
	public int fanId;
	public int equipmentId;
	public String partType;
	public String number;
	
	// Belt Details
	public String beltSize;
	public int beltQty;
	
	// UPLOAD DATA ONLY
	public ArrayList<Sheave> sheaves;
	
	public Fan()
		{
		beltSize = "";
		beltQty = 0;
		}
	
	}