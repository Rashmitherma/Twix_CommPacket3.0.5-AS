package com.twix;

import java.io.Serializable;

public class Dispatch implements Serializable
	{
	private static final long serialVersionUID = 3289088825594905261L;
	public int dispatchId;
	public int serviceAddressId;
	public String batchNo;
	public String jobNo;
	public String cusNo;
	public String altBillTo;
	public String contractType;
	public String dateStarted;
	public String dateEnded;
	public String dateOrdered;
	public String customerPO;
	public String requestedBy;
	public String requestedByPhone;
	public String requestedByEmail;
	public String siteContact;
	public String siteContactPhone;
	
	public String siteName;
	public String siteAddress1;
	public String siteAddress2;
	public String status;
	public String tenant;
	public String description;
	
	public String mechanic1;
	public String mechanic2;
	public String mechanic3;
	public String mechanic4;
	public String mechanic5;
	public String mechanic6;
	public String mechanic7;
	
	public String PMComments;
	public String PMEstTime;
	
	public String CallComplete;
	}