package com.twix;
import java.io.Serializable;
import java.util.ArrayList;


public class ServerResponse implements Serializable
	{
	/**
	 * Server Response Serialization
	 */
	private static final long serialVersionUID = -5462001019012189992L;
	// Action Types
	public final static int LOGIN = 0;
	public final static int SYNC = 1;
	public final static int SITE_SEARCH = 2;
	public final static int SITE_DOWNLOAD = 3;
	
	// Result Types
	public final static int SUCCESS = 0;
	public final static int LOGIN_FAILED = 1;
	public final static int TRANSACTION_FAILED = 2;
	public final static int TIMEOUT_EXCEPTION = 3;
	public final static int NO_ROUTE_TO_HOST = 4;
	public final static int IOEXCEPTION = 5;
	public final static int REQ_UPDATE = 10;
	
	// Dispatch Assignment Result Types
	public final static int DISPATCH_ALREADY_ASSIGNED = 100;
	public final static int DISPATCH_SLOT_NOT_AVAILABLE = 101;
	
	public int result = 0;
	
	public String EmpNo;
	public String email;
	public String syncTime;
	public String SecretKey;
	public int responseType = -1;
	
	public Package_Download pkg;
	public ArrayList<SearchData> searchResponse;
	public byte[] UpdateFile;
	
	// Specialty Fields
	public String ErrorMessage;
	public String UserMessage;
	
	// Form Package
	public FormPackage FormPackage;
	public FormDataPackage FormDataPackage;
	public AttributePackage AttrPackage;
	
	/**
	 * 
	 * Creates a Twix_Server response to the Twix Client
	 * 
	 * @param emp	-	Employee Number: either the employee number found or null
	 * @param a		-	The response action. Can be successful, login failed, or transaction failed.
	 * @param pack	-	The download package associated for the tablet to process
	 */
	public ServerResponse(String emp, int a, Package_Download pack )
		{
		EmpNo = emp;
		result = a;
		pkg = pack;
		}
	
	static public class SearchData implements Serializable
	    {
		private static final long serialVersionUID = 8806842827674814522L;
		
		public int serviceAddressId;
	    public String siteName;
	    public String address;
	    public String city;
	    public String state;
	    public String zip;
	    public String buildingNo;
	    }
	}
