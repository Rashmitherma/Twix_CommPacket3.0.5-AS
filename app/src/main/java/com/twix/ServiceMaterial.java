package com.twix;

import java.io.Serializable;

public class ServiceMaterial implements Serializable
	{
	private static final long serialVersionUID = -7730071038817604108L;
	
	public int serviceMaterialId;
	public int serviceTagUnitId;
	
	public float quantity;
	public String materialDesc;
	public float cost;
	public String refrigerantAdded;
	public String source;
	}