package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceTagGroup implements Serializable
	{
	private static final long serialVersionUID = 5995497216142280140L;
	
	public byte[] signature;
	public String noSignatureReason;
	public String dateCreated;
	
	public List<Integer> serviceTagXref;
	public String emailList;
	
	public ServiceTagGroup()
		{
		serviceTagXref = new ArrayList<Integer>();
		}
	}