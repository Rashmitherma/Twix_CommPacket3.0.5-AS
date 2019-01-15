package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteSearch implements Serializable
	{
	private static final long serialVersionUID = -7527640248458964557L;
	
	public List<Integer> knownServiceAddressIds;
	public String siteName;
	public String address;
	public String city;
	public String buildingNo;
	
	public SiteSearch()
		{
		knownServiceAddressIds = new ArrayList<Integer>();
		}
	
	public boolean isEmpty()
		{
		boolean hasNull = (siteName == null ||
							 address == null ||
							 city == null ||
							 buildingNo == null);
		boolean empty = false;
		
		if( !hasNull )
			empty = !(siteName.length() > 0 ||
					 address.length() > 0 ||
					 city.length() > 0 ||
					 buildingNo.length() > 0);
		
		return empty;
		}
	}
