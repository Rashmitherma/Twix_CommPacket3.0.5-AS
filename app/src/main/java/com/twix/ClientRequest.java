package com.twix;
import java.io.Serializable;
import java.util.ArrayList;


public class ClientRequest implements Serializable
	{
	/**
	 * Client Request Serialize
	 */
	private static final long serialVersionUID = 2328685545493889305L;
	public final static int LOGIN = 0;
	public final static int SYNC_UPLOAD = 1;
	public final static int SYNC_DOWNLOAD = 2;
	public final static int SYNC_TIME = 5;
	public final static int SITE_SEARCH = 3;
	public final static int SITE_DOWNLOAD = 4;
	public final static int DOWNLOAD_UPDATE = 10;
	public final static int ASSIGN_MECHANIC = 100;
	
	public final static String version = "0.9.5";
	
	public String MEID;
	public String username;
	public String password;
	public int action = -1;
	public String app_version;
	
	public boolean init;
	
	public Package_Upload pkg;
	public FormDataPackage formDataPkg;
	public SiteSearch search;
	public ArrayList<Integer> siteRequest;
	
	// Client Request V2 Changes
	public String SecretKey;
	public Object Package;
	public LatestDates latestDates;
	
	
	public ClientRequest( String ID, int a, Package_Upload pack, String version )
		{
		MEID = ID;
		action = a;
		pkg = pack;
		init = false;
		app_version = version;
		
		latestDates = new LatestDates();
		}
	
	public ClientRequest(int a, String version)
		{
		action = a;
		app_version = version;
		switch(a)
			{
			case SITE_SEARCH:
				break;
			case SITE_DOWNLOAD:
				siteRequest = new ArrayList<Integer>();
			}
		}
	
	public static class LatestDates implements Serializable
		{
		private static final long serialVersionUID = 3518926333341524057L;
		
		public String DispatchPriority;
		public String All_LatestDate;
		}
	
	}
