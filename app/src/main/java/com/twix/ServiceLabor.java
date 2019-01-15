package com.twix;

import java.io.Serializable;

public class ServiceLabor implements Serializable
	{
	private static final long serialVersionUID = 726934369017994073L;
	
	public int serviceLaborId;
	public int serviceTagUnitId;
	
	public String serviceDate;
	public float regHours;
	public float thHours;
	public float dtHours;
	public String mechanic;
	public String rate;
	}