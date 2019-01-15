package com.twix;

import java.io.Serializable;

public class ServiceAddress implements Serializable
	{
	private static final long serialVersionUID = 5510384959885171977L;
	
	public int serviceAddressId;
	public String siteName;
	public String address1;
	public String address2;
	public String city;
	public String state;
	public String zip;
	public String buildingNo;
	public String note;
	
	public ServiceAddress()
		{
		}
	
	/**
	 * PM Checklist based on Service Address
	 */
	static public class pmAddressChecklist implements Serializable
		{
		private static final long serialVersionUID = 4706975106286457391L;
		
		public int pmChecklistId;
		public int serviceAddressId;
		public int equipmentCategoryId;
		public String itemText;
		public String itemType;
		public String	identifier;
		}
	
	static public class tenant implements Serializable
		{
		private static final long serialVersionUID = 5394675315929225585L;
		
		public int tenantId;
		public int serviceAddressId;
		public String tenant;
		}
	

	
	}