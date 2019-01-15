package com.twix;
import java.io.Serializable;
import java.util.ArrayList;

public class Equipment implements Serializable
	{
	private static final long serialVersionUID = 466702742315171579L;
	
	// Only used for uploading. Downloading has a parent relationship
	public int serviceAddressId;
	
	public int equipmentId;
	public int equipmentCategoryId;
	public String unitNo;
	public String barCodeNo;
	public String manufacturer;
	public String model;
	public String productIdentifier;
	public String serialNo;
	public String voltage;
	public String economizer;
	public float capacity;
	public String capacityUnits;
	public String refrigerantType;
	public String areaServed;
	public String mfgYear;
	public String dateInService;
	public String dateOutService;
	public String notes;
	public String verifiedByEmpno;
	public byte[] pic;
	
	public ArrayList<Fan> fans;
	public ArrayList<Filter> filters;
	public ArrayList<RefCircuit> refCircuits;
	
	public Equipment()
		{
		}

	}