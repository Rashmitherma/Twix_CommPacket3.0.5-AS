package com.twix;
import java.io.Serializable;

public class ServiceAddressContact implements Serializable
	{
	private static final long serialVersionUID = 8327006458411326501L;
	
	public int contactId;
	public int serviceAddressId;
	
	public String contactName;
	public String phone1;
	public String phone1Type;
	public String ext1;
	public String phone2;
	public String phone2Type;
	public String ext2;
	public String email;
	public String contactType;

	public String updatedBy;
	public String updatedDate;

	public String	noteid;
	}