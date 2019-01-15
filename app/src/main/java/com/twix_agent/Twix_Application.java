package com.twix_agent;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.twix.ClientRequest;
import com.twix.DispatchRequest;
import com.twix.ServerResponse;
import com.twix.SiteSearch;

import android.app.AlertDialog;
import android.app.Application;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_Application
 * 
 * Purpose: Overrides the android Application class so it can contain some global classes. Also provides
 * 			functionality to refresh the highest level tabs.
 * 
 * Relevant XML: none
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_Application extends Application
	{
	//final long LOGIN_TIMEOUT = 3600000;
	final String PREFS_FILE = "dispatch_filters.data";
	String version = "5.0.3";
	String changelog = "";
	
	Twix_SQLite db;
	Twix_Client client;
	
	String empno;
	String techName;
	String techEmail;
	String SecretKey;
	
	long last_connection = 0;
	String username;
	String password;
	String serviceAddressId;
	String last_sync;
	
	Twix_AgentTheme Twix_Theme;
	SharedPreferences prefs;
	TelephonyManager tele;
	private View syncIndicator;
	public View.OnLongClickListener copyListener;
	public Twix_AgentTabActivity MainTabs;
	
	public Twix_Login login;
	public ProgressDialog mProgressDialog;
	
	public Twix_Application()
		{
		super();
		empno = "";
		serviceAddressId = "";
		
		createListeners();
		setChangelog();
		}
	
	public String getDeviceId()
		{
		String deviceId = tele.getDeviceId();
		if( deviceId == null )
			deviceId = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		
		if( deviceId == null )
			{
			WifiManager wm = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
			if( wm != null )
				{
				WifiInfo wi = wm.getConnectionInfo();
				if( wi != null )
					deviceId = wi.getMacAddress();
				}
			}
		
		return deviceId;
		}
	
	public boolean checkTempDir()
		{
		boolean ret = true;
		File tmp = Environment.getExternalStorageDirectory();
		tmp = new File( tmp.getAbsolutePath() + "/.temp/" );
		if (!tmp.exists())
			{
			ret = tmp.mkdir();
			}
		
		return ret;
		}
	
	public String clean(String s)
		{
		if( s == null )
			return "";
		else if ( s.contentEquals("NULL") )
			return "";
		else
			return s;
		}
	
	public void setIndicators(View v4)
		{
		syncIndicator = v4;
		}
	
	public void refreshPages()
		{
		LocalActivityManager manager = MainTabs.getLocalActivityManager();
		
		Twix_TabActivityGroup group = (Twix_TabActivityGroup) manager
				.getActivity("dispatch");
		if (group != null)
			{
			group.finishAll();
			((Twix_AgentTab1) group.getLocalActivityManager().getActivity(
					"Twix_AgentTab1")).Update_WorkFlow();
			}

		group = (Twix_TabActivityGroup) manager.getActivity("tags");
		if (group != null)
			{
			group.finishAll();
			((Twix_AgentTab2) group.getLocalActivityManager().getActivity(
					"Twix_AgentTab2")).readSQL();
			}

		group = (Twix_TabActivityGroup) manager.getActivity("siteinfo");
		if (group != null)
			{
			group.finishAll();
			((Twix_AgentTab3) group.getLocalActivityManager().getActivity(
					"Twix_AgentTab3")).onSearch();
			}
		
		Twix_AgentSyncPage syncPage = (Twix_AgentSyncPage) manager
				.getActivity("syncPage");
		if (syncPage != null)
			{
			syncPage.updateText();
			syncPage.readSQL();
			syncPage.ResetReInit();
			}
		}
	
	public void refreshTabs()
		{
		// Set the Tech Name
		techName = getEmpName();
		
		RelativeLayout rel = (RelativeLayout) syncIndicator.findViewById(R.id.tabsLayout);
		if( prefs.getBoolean("reqUpdate", true) || prefs.getBoolean("data_dirty", true) || prefs.getBoolean("sync_dirty", true))
	    	{
	    	boolean hasIcon = false;
	    	int size = rel.getChildCount();
	    	for( int i = 0; i < size; i++ )
	    		{
	    		if( rel.getChildAt(i).getClass() == ImageView.class )
	    			{
	    			hasIcon = true;
	    			break;
	    			}
	    		}
	    	
	    	// Only add the icon when it does not already exist
	    	if( !hasIcon )
	    		{
			    ImageView im = new ImageView(this);
			    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			    params.addRule(RelativeLayout.RIGHT_OF, R.id.tabsText);
			    params.addRule(RelativeLayout.CENTER_VERTICAL);
			    im.setLayoutParams(params);
			    im.setImageResource(R.drawable.icon_alert);
			    rel.addView(im, 0);
	    		}
	    	}
		else
			{
			View v;
			int size = rel.getChildCount();
			int i = 0;
			while( i < size )
				{
				v = rel.getChildAt(i);
				i++;
				if( v.getClass() == ImageView.class )
					{
					rel.removeView(v);
					i = 0;
					size = rel.getChildCount();
					}
				}
			}
		}
	
	private void createListeners()
		{
		copyListener = new View.OnLongClickListener()
			{
			@Override
			public boolean onLongClick(View v)
				{
				if( v.getClass() == EditText.class || v.getClass() == TextView.class )
					{
					ClipboardManager clipboard = (ClipboardManager)
					        getSystemService(Context.CLIPBOARD_SERVICE);
					
					ClipData clip = ClipData.newPlainText( "Twix Copied Text", ((TextView)v).getText() );
					
					clipboard.setPrimaryClip(clip);
					
					return true;
					}
				return false;
				}
			
			};
		}
	
	
	private void setChangelog()
		{
		changelog =

				"\t 1. XOI Integration .\n" ;
			//	"\t 2. Added labor rate under tags history.\n";
				//"\t 3. Access SDS BinderWorks App from our Application - Dispatch tab";
				
				
				
				
				
				
		}
	public boolean loggedIn()
		{
		return (username != null) && !(prefs.getBoolean("offline", true));
		//return ( (username != null) && (password != null) && (last_connection > (System.currentTimeMillis() - LOGIN_TIMEOUT) ) );
		}
	
	
	private String getEmpName()
		{
		String empName = "";
		
		String sqlQ = "select mechanic_name from mechanic where mechanic = '" + empno + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			empName = Twix_TextFunctions.clean( cursor.getString(0) );
		if (cursor != null && !cursor.isClosed())
			cursor.close();
				
		return empName;
		}
	
	// One way of hashing the application username and password. This is used to allow logins without connectivity.
	public String oneWayHash(String arg)
		{
		String ret = null;
		MessageDigest md = null;
		try
			{
			md = MessageDigest.getInstance( "MD5" );
			ret = Base64.encodeToString(md.digest(arg.getBytes()), Base64.NO_WRAP);
			}
		catch( NoSuchAlgorithmException nsae )
			{
			nsae.printStackTrace();
			}
		
		return ret;
		}
	
	// Application Level Sync
	public ProgressDialog generateProgressDialog(String s)
		{
		mProgressDialog = new ProgressDialog(MainTabs);
		mProgressDialog.setMessage(s);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.show();
		
		return mProgressDialog;
		}
	
	public Handler handler = new Handler()
		{
		@Override
		public void handleMessage(Message msg)
			{
			switch(msg.what)
				{
				case Twix_Client.POST_ERROR_MESSAGE:
					if( msg.obj != null && (msg.obj instanceof String) )
						{
						AlertDialog alert = new AlertDialog.Builder(MainTabs).create();
				    	alert.setTitle("Error Message Received From Server" );
				    	alert.setMessage((String)msg.obj);
				    	alert.setButton("Ok", new DialogInterface.OnClickListener()
				    		{  
				    		public void onClick(DialogInterface dialog, int which)
				    			{
				    			return;  
				    			}
				    		});
				    	alert.show();
						}
					break;
				
				case Twix_Client.POST_USER_MESSAGE:
					if( msg.obj != null && (msg.obj instanceof String) )
						{
						AlertDialog alert = new AlertDialog.Builder(MainTabs).create();
				    	alert.setTitle("Message Received From Server" );
				    	alert.setMessage((String)msg.obj);
				    	alert.setButton("Ok", new DialogInterface.OnClickListener()
				    		{  
				    		public void onClick(DialogInterface dialog, int which)
				    			{
				    			return;  
				    			}
				    		});
				    	alert.show();
						}
					break;
				
				case Twix_Client.UPDATE_PROGRESS:
					if( msg.obj == null )
						mProgressDialog.dismiss();
					else
						mProgressDialog.setMessage((String) msg.obj);
					break;
				}
			// End Switch
			}
		}
	;
	
	public void Sync(boolean isLogin, boolean changeUser, boolean ReInit)
		{
		if( (loggedIn() && !changeUser) || isLogin )
			{
			generateProgressDialog("Building upload package...");
			new SocketConnectionTask().execute(ReInit);
			}
		else
			login = new Twix_Login(MainTabs, this, ClientRequest.SYNC_UPLOAD, changeUser, null, ReInit);
		}
	
	public void SiteSearch(boolean isLogin, SiteSearch search)
		{
		if( loggedIn() || isLogin )
			{
			generateProgressDialog("Connecting to Server...");
			new SocketConnectionTask_Search().execute(search);
			}
		else
			login = new Twix_Login(MainTabs, this, ClientRequest.SITE_SEARCH, false, search, false);
		}
	
	@SuppressWarnings("unchecked")
	public void SiteDownload(boolean isLogin, ArrayList<Integer> serviceAddressIds)
		{
		if( loggedIn() || isLogin )
			{
			generateProgressDialog("Connecting to Server...");
			new SocketConnectionTask_Download().execute(serviceAddressIds);
			}
		else
			login = new Twix_Login(MainTabs, this, ClientRequest.SITE_DOWNLOAD, false, serviceAddressIds, false);
		}
	
	public void UpdateDownload(boolean isLogin)
		{
		if( loggedIn() || isLogin )
			{
			generateProgressDialog("Connecting to Server...");
			new SocketConnectionTask_UpdateDownload().execute();
			}
		else
			login = new Twix_Login(MainTabs, this, ClientRequest.DOWNLOAD_UPDATE, false, null, false);
		}
	
	public void AssignDispatch(boolean isLogin, DispatchRequest req)
		{
		if( loggedIn() || isLogin )
			{
			generateProgressDialog("Connecting to Server...");
			new SocketConnectionTask_AssignDispatch().execute(req);
			}
		else
			login = new Twix_Login(MainTabs, this, ClientRequest.ASSIGN_MECHANIC, false, req, false);
		}
	
	public void Login()
		{
		login = new Twix_Login(MainTabs, this, ClientRequest.LOGIN, false, null, false);
		}
	
	private void updateAlert()
		{
		last_connection = System.currentTimeMillis();
		if( login != null )
			{
			login.dismiss();
			login = null;
			}
		
		// Put the last version in the shared preferences. This will cause a change log to pop-up when the new version is installed
		prefs.edit().putString("last_version", version ).commit();
		
		AlertDialog alert = new AlertDialog.Builder(MainTabs).create();
    	alert.setTitle("Twix Mobile Update" );
    	alert.setMessage(
    			"Twix Mobile requires an update to proceed. " +
    			"This may take a few minutes. Please make sure " +
    			"you have a good connection while downloading the update.");
    	alert.setButton("Start Update", new DialogInterface.OnClickListener()
    		{  
    		public void onClick(DialogInterface dialog, int which)
    			{
    			UpdateDownload(false);
    			dialog.dismiss(); 
    			}
    		});
    	alert.show();
		}
	
	// Sync Socket Connection Thread
	private class SocketConnectionTask extends AsyncTask<Boolean, Void, Integer>
		{
		boolean ReInit = false;
	    protected Integer doInBackground(Boolean... reinit)
	    	{
	    	int result = ServerResponse.LOGIN_FAILED;
	    	ReInit = reinit[0];
	    	try
	    		{
	    		result = client.Connect_Sync(handler, ReInit);
	    		}
	    	catch(Exception e)
	    		{
	    		result = ServerResponse.TRANSACTION_FAILED;
	    		handler.sendMessage(new Message());
	    		e.printStackTrace();
	    		}
	    	
	        return result;
	    	}
	
	    protected void onPostExecute(Integer result)
	    	{
	    	try
				{
				refreshTabs();
				refreshPages();
				}
			catch( Exception e )
				{
				Toast.makeText(MainTabs, "Refresh Pages Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				Log.e("twix_agent:" + this.getClass().getName(), "Error page refresh: " + e.getMessage(), e);
				}
	    	
	    	switch( result )
		    	{
		    	case ServerResponse.SUCCESS:
			    	last_connection = System.currentTimeMillis();
		    		if( login != null )
		    			{
		    			login.dismiss();
		    			login = null;
		    			}
		    		break;
		    	case ServerResponse.REQ_UPDATE:
			    	updateAlert();
		    		break;
		    	case ServerResponse.LOGIN_FAILED:
		    		if( (login == null) || (login.dialog == null) ||(!login.dialog.isShowing()) )
		    			login = new Twix_Login(MainTabs, Twix_Application.this, ClientRequest.SYNC_UPLOAD, false, null, ReInit);
			    	else
			    		Toast.makeText(MainTabs, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TRANSACTION_FAILED:
		    		Toast.makeText(MainTabs,
	    				"Sync Transaction Failed. Please contact your local administrator.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TIMEOUT_EXCEPTION:
		    		Toast.makeText(MainTabs,
	    				"Failed to connect to server. Please check your data connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.IOEXCEPTION:
		    	case ServerResponse.NO_ROUTE_TO_HOST:
		    		Toast.makeText(MainTabs,
	    				"Twix Mobile Service is offline. Please try again later.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case Twix_Client.CONNECTION_NOT_AVAILABLE:
		    		Toast.makeText(MainTabs,
	    				"Internet Connectivity not available. Please check your device's internet connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	default:
		    		Toast.makeText(MainTabs,
	    				"An Unknown error has ocurred. Please contact your local administrator. Code = '" + result + "'",
	    				Toast.LENGTH_LONG).show();
		    	}
	    	}
		}
	
	// Site Download Socket Connection Thread
	/**
	 * 
	 * Parameters:
	 * 			 - SiteSearch Object... contains an ArrayList 
	 * 
	 * @author MikeLaptop
	 *
	 */
	private class SocketConnectionTask_Search extends AsyncTask<SiteSearch, Void, Integer>
		{
		private SiteSearch search;
		private ArrayList<ServerResponse.SearchData> resultData;
		
		protected Integer doInBackground(SiteSearch... args)
	    	{
	    	search = args[0];
	    	resultData = new ArrayList<ServerResponse.SearchData>();
	    	
	    	int result = ServerResponse.LOGIN_FAILED;
	    	try
	    		{
	    		result = client.Connect_Sync(handler, search, resultData);
	    		}
	    	catch(Exception e)
	    		{
	    		resultData = null;
	    		result = ServerResponse.TRANSACTION_FAILED;
	    		handler.sendMessage(new Message());
	    		e.printStackTrace();
	    		}
	    	
	        return result;
	    	}
	
	    protected void onPostExecute(Integer result)
	    	{
	    	switch( result )
		    	{
		    	case ServerResponse.SUCCESS:
			    	if( resultData != null )
			    		{
			    		LocalActivityManager manager = MainTabs.getLocalActivityManager();
			    		Twix_AgentActivityGroup3 actGroup = (Twix_AgentActivityGroup3) manager.getActivity("siteinfo");
			    		manager = actGroup.getLocalActivityManager();
			    		Twix_AgentSiteSearch searchAct = (Twix_AgentSiteSearch) manager.getActivity("Twix_AgentSiteSearch");
			    		searchAct.postResults(resultData);
			    		}
			    	
			    	last_connection = System.currentTimeMillis();
		    		if( login != null )
		    			{
		    			login.dismiss();
		    			login = null;
		    			}
		    		break;
		    	
		    	case ServerResponse.LOGIN_FAILED:
		    		if( (login == null) || (login.dialog == null) ||(!login.dialog.isShowing()) )
		    			login = new Twix_Login(MainTabs, Twix_Application.this, ClientRequest.SITE_SEARCH, false, search, false);
		    		else
		    			Toast.makeText(MainTabs, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TRANSACTION_FAILED:
		    		Toast.makeText(MainTabs,
	    				"Sync Transaction Failed. Please contact your local administrator.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TIMEOUT_EXCEPTION:
		    		Toast.makeText(MainTabs,
	    				"Failed to connect to server. Please check your data connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.NO_ROUTE_TO_HOST:
		    		Toast.makeText(MainTabs,
	    				"Twix Mobile Service is offline. Please try again later.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case Twix_Client.CONNECTION_NOT_AVAILABLE:
		    		Toast.makeText(MainTabs,
	    				"Internet Connectivity not available. Please check your device's internet connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	default:
		    		Toast.makeText(MainTabs,
	    				"An Unknown error has ocurred. Please contact your local administrator. Code = '" + result + "'",
	    				Toast.LENGTH_LONG).show();
		    	}
	    	}
	    
		}
	
	// Site Download Socket Connection Thread
	/**
	 * 
	 * Parameters:
	 * 			 - Array List of Integers - This list is of serviceAddressIds requested for download
	 * 
	 * @author MikeLaptop
	 *
	 */
	private class SocketConnectionTask_Download extends AsyncTask<ArrayList<Integer>, Void, Integer>
		{
		private ArrayList<Integer> idList;
		
		protected Integer doInBackground(ArrayList<Integer>... args)
	    	{
	    	idList = args[0];
	    	
	    	int result = ServerResponse.LOGIN_FAILED;
	    	try
	    		{
	    		result = client.Connect_Sync(handler, idList);
	    		}
	    	catch(Exception e)
	    		{
	    		result = ServerResponse.TRANSACTION_FAILED;
	    		handler.sendMessage(new Message());
	    		e.printStackTrace();
	    		}
	    	
	        return result;
	    	}
	
	    protected void onPostExecute(Integer result)
	    	{
	    	switch( result )
		    	{
		    	case ServerResponse.SUCCESS:
		    		// Finish the Site Search Activity
			    	LocalActivityManager manager = MainTabs.getLocalActivityManager();
			    	Twix_AgentActivityGroup3 actGroup = (Twix_AgentActivityGroup3) manager.getActivity("siteinfo");
			    	manager = actGroup.getLocalActivityManager();
			    	Twix_AgentSiteSearch searchAct = (Twix_AgentSiteSearch) manager.getActivity("Twix_AgentSiteSearch");
			    	searchAct.finish();
			    	
			    	last_connection = System.currentTimeMillis();
		    		if( login != null )
		    			{
		    			login.dismiss();
		    			login = null;
		    			}
		    		break;
		    	
		    	case ServerResponse.LOGIN_FAILED:
		    		if( (login == null) || (login.dialog == null) ||(!login.dialog.isShowing()) )
		    			login = new Twix_Login(MainTabs, Twix_Application.this, ClientRequest.SITE_DOWNLOAD, false, idList, false);
			    	else
			    		Toast.makeText(MainTabs, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TRANSACTION_FAILED:
		    		Toast.makeText(MainTabs,
	    				"Sync Transaction Failed. Please contact your local administrator.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TIMEOUT_EXCEPTION:
		    		Toast.makeText(MainTabs,
	    				"Failed to connect to server. Please check your data connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.NO_ROUTE_TO_HOST:
		    		Toast.makeText(MainTabs,
	    				"Twix Mobile Service is offline. Please try again later.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case Twix_Client.CONNECTION_NOT_AVAILABLE:
		    		Toast.makeText(MainTabs,
	    				"Internet Connectivity not available. Please check your device's internet connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	default:
		    		Toast.makeText(MainTabs,
	    				"An Unknown error has ocurred. Please contact your local administrator. Code = '" + result + "'",
	    				Toast.LENGTH_LONG).show();
		    	}
	    	}
	    
		}
	
	// Update Download Socket Connection Thread
	private class SocketConnectionTask_UpdateDownload extends AsyncTask<Void, Void, Integer>
		{
		protected Integer doInBackground(Void... args)
	    	{
	    	int result = ServerResponse.LOGIN_FAILED;
	    	try
	    		{
	    		result = client.Connect_Update(handler);
	    		}
	    	catch(Exception e)
	    		{
	    		result = ServerResponse.TRANSACTION_FAILED;
	    		handler.sendMessage(new Message());
	    		e.printStackTrace();
	    		}
	    	
	    	return result;
	    	}
	
	    protected void onPostExecute(Integer result)
	    	{
	    	switch( result )
		    	{
		    	case ServerResponse.SUCCESS:
		    		// Finish the Site Search Activity
			    	Intent intent = new Intent(Intent.ACTION_VIEW);
			    	intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/.temp/" + "Twix_Agent.apk")), "application/vnd.android.package-archive");
			    	MainTabs.startActivity(intent);  
			    	
			    	last_connection = System.currentTimeMillis();
		    		if( login != null )
		    			{
		    			login.dismiss();
		    			login = null;
		    			}
		    		break;
		    	
		    	case ServerResponse.LOGIN_FAILED:
		    		if( (login == null) || (login.dialog == null) ||(!login.dialog.isShowing()) )
		    			login = new Twix_Login(MainTabs, Twix_Application.this, ClientRequest.DOWNLOAD_UPDATE, false, null, false);
			    	else
			    		Toast.makeText(MainTabs, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TRANSACTION_FAILED:
		    		Toast.makeText(MainTabs,
	    				"Sync Transaction Failed. Please contact your local administrator.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TIMEOUT_EXCEPTION:
		    		Toast.makeText(MainTabs,
	    				"Failed to connect to server. Please check your data connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.NO_ROUTE_TO_HOST:
		    		Toast.makeText(MainTabs,
	    				"Twix Mobile Service is offline. Please try again later.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.IOEXCEPTION:
	    			Toast.makeText(MainTabs,
    				"Failed to open directory '" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp/'" +
    						". Please check the permissions for this directory.",
    				Toast.LENGTH_LONG).show();
	    		break;
		    	
		    	case Twix_Client.CONNECTION_NOT_AVAILABLE:
		    		Toast.makeText(MainTabs,
	    				"Internet Connectivity not available. Please check your device's internet connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	default:
		    		Toast.makeText(MainTabs,
	    				"An Unknown error has ocurred. Please contact your local administrator. Code = '" + result + "'",
	    				Toast.LENGTH_LONG).show();
		    	}
	    	}
		}
	
	private class SocketConnectionTask_AssignDispatch extends AsyncTask<DispatchRequest, Void, Integer>
		{
		DispatchRequest dRequest;
		
		protected Integer doInBackground(DispatchRequest... args)
	    	{
	    	int result = ServerResponse.LOGIN_FAILED;
	    	try
	    		{
	    		dRequest = args[0];
	    		result = client.Connect_DispatchRequest(handler, dRequest);
	    		}
	    	catch(Exception e)
	    		{
	    		result = ServerResponse.TRANSACTION_FAILED;
	    		handler.sendMessage(new Message());
	    		e.printStackTrace();
	    		}
	    	
	    	return result;
	    	}
	
	    protected void onPostExecute(Integer result)
	    	{
	    	switch( result )
		    	{
		    	case ServerResponse.DISPATCH_ALREADY_ASSIGNED:
		    		Toast.makeText(MainTabs,
	    				"This dispatch already has that mechanic assigned.", Toast.LENGTH_LONG).show();
		    	case ServerResponse.DISPATCH_SLOT_NOT_AVAILABLE:
		    		if( result == ServerResponse.DISPATCH_SLOT_NOT_AVAILABLE )
			    		Toast.makeText(MainTabs,
							"No mechanic slots are available on this dispatch.", Toast.LENGTH_LONG).show();
		    	case ServerResponse.SUCCESS:
		    		// Finish the Dispatch Request
			    	LocalActivityManager manager = MainTabs.getLocalActivityManager();
		    		Twix_AgentActivityGroup1 actGroup = (Twix_AgentActivityGroup1) manager.getActivity("dispatch");
		    		manager = actGroup.getLocalActivityManager();
		    		Twix_AgentTab1 tab1 = (Twix_AgentTab1) manager.getActivity("Twix_AgentTab1");
		    		tab1.refreshDialogs(dRequest.DispatchId);
		    	
			    	last_connection = System.currentTimeMillis();
		    		if( login != null )
		    			{
		    			login.dismiss();
		    			login = null;
		    			}
		    		break;
		    	
		    	case ServerResponse.LOGIN_FAILED:
		    		if( (login == null) || (login.dialog == null) ||(!login.dialog.isShowing()) )
		    			login = new Twix_Login(MainTabs, Twix_Application.this, ClientRequest.ASSIGN_MECHANIC, false, dRequest, false);
			    	else
			    		Toast.makeText(MainTabs, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TRANSACTION_FAILED:
		    		Toast.makeText(MainTabs,
	    				"Sync Transaction Failed. Please contact your local administrator.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.TIMEOUT_EXCEPTION:
		    		Toast.makeText(MainTabs,
	    				"Failed to connect to server. Please check your data connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.NO_ROUTE_TO_HOST:
		    		Toast.makeText(MainTabs,
	    				"Twix Mobile Service is offline. Please try again later.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	case ServerResponse.IOEXCEPTION:
	    			Toast.makeText(MainTabs,
    				"An Unknown error has ocurred. Please contact your local administrator. Error: IOEXCEPTION",
    				Toast.LENGTH_LONG).show();
	    		break;
	    			
		    	case Twix_Client.CONNECTION_NOT_AVAILABLE:
		    		Toast.makeText(MainTabs,
	    				"Internet Connectivity not available. Please check your device's internet connection.", Toast.LENGTH_LONG).show();
		    		break;
		    	
		    	default:
		    		Toast.makeText(MainTabs,
	    				"An Unknown error has ocurred. Please contact your local administrator. Code = '" + result + "'",
	    				Toast.LENGTH_LONG).show();
		    	}
	    	}
		
		}
	}