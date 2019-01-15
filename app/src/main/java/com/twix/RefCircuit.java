package com.twix;

import java.io.Serializable;
import java.util.ArrayList;

public class RefCircuit implements Serializable
	{
	private static final long serialVersionUID = 9089226793740715456L;
	
	public int equipmentId;
	public int circuitId;
	public String circuitNo;
	public float lbsRefrigerant;	
	
	public ArrayList<Compressor> compressors;
	
	public RefCircuit()
		{
		}
	
	}