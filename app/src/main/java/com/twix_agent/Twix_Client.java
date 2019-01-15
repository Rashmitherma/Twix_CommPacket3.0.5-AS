package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.twix.*;

public class Twix_Client
	{
	// Production Socket
	public static final int HOST_PORT = 2600;
	//public static final int HOST_PORT = 2600;
	
	public static final int CONNECTION_NOT_AVAILABLE = -1;
		// Therma IP
	//private static final String TWIX_CONNECTION = "24.104.112.72";
	
		// Comp Three Office
	//private static final String TWIX_CONNECTION = "173.11.86.85";
		// Comp Three Office DNS
	//private static final String TWIX_CONNECTION = "compthree.dyndns.org";
	
		// Internal
	//private static final String TWIX_CONNECTION = "192.168.1.144";
	//private static final String TWIX_CONNECTION = "10.0.6.55";
		// Twix Production
	private static final String TWIX_CONNECTION = "twix.therma.com";
	
		// Twix - Toad
	//private static final String TWIX_CONNECTION = "24.104.112.81";
	
		// Therma IP2
	//private static final String TWIX_CONNECTION = "24.104.112.70";
	
	// Message Handler Constants
	public static final int UPDATE_PROGRESS = 0;
	public static final int POST_ERROR_MESSAGE = 1;
	public static final int POST_USER_MESSAGE = 2;
	
	private static final int TIMEOUT_MS = 60000;
	
	public ClientRequest request;
	
	private Twix_Application app;
	private Twix_SQLite db;
	
	public Twix_Client(Twix_Application a)
		{
		app = a;
		db = app.db;
		}
	
	private boolean isOnline()
		{
		ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = cm.getActiveNetworkInfo();
		if( net != null )
			{
			if( net.isAvailable() && net.isConnected() )
				{
				return true;
				}
			}
		
		return false;
		}
	
	private Socket BuildSocket() throws UnknownHostException, IOException
		{
		Socket socket = new Socket(TWIX_CONNECTION, HOST_PORT);
		socket.setSoTimeout(TIMEOUT_MS);
		socket.setKeepAlive(true);
		socket.setSoLinger(true, 0);
		return socket;
		}
	
	private ClientRequest.LatestDates GetLatestDates()
		{
		ClientRequest.LatestDates ret = new ClientRequest.LatestDates();
		String sqlQ = "SELECT " +
				"( SELECT MAX(DateChanged) FROM DispatchPriority )";
		
		Cursor cursor = db.rawQuery(sqlQ);
		int index = 0;
		if (cursor.moveToFirst())
			{
			ret.DispatchPriority = cursor.getString(0);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		
		ret.All_LatestDate = db.GetLatestDate();
		return ret;
		}
	
	private void CreateErrorMessage(Handler handler, String message)
		{
		if( message == null )
			return;
		Message mes = new Message();
		mes.what = POST_ERROR_MESSAGE;
		mes.obj = message;
		handler.sendMessage(mes);
		}
	
	private void CreateUserMessage(Handler handler, String message)
		{
		if( message == null )
			return;
		Message mes = new Message();
		mes.what = POST_USER_MESSAGE;
		mes.obj = message;
		handler.sendMessage(mes);
		}
	
	private void UpdateProgress(Handler handler, String message)
		{
		Message mes = new Message();
		mes.what = UPDATE_PROGRESS;
		mes.obj = message;
		handler.sendMessage(mes);
		}
	
	public int Connect_Sync(Handler handler, boolean ReInit)
		{
		if( !isOnline() )
			{
			UpdateProgress(handler, null);
			return CONNECTION_NOT_AVAILABLE;
			}
		
		int return_status = ServerResponse.LOGIN_FAILED;
		
		String deviceId = app.getDeviceId();
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket = null;
		ServerResponse response = null;
		int action;
		boolean download_only = (app.prefs.getBoolean("firstStart", false))
								||  (app.prefs.getBoolean("reqUpdate", false))
								||  (app.prefs.getBoolean("sync_dirty", false));
		
		if( download_only )
			action = ClientRequest.SYNC_DOWNLOAD;
		else
			action = ClientRequest.SYNC_UPLOAD;
		
		try
			{
			/***************************************
			 * Setup the Client Request
			 ***************************************/
			request = new ClientRequest(deviceId, action, null, app.version );
			request.username = app.username;
			request.password = app.password;
			request.SecretKey = app.SecretKey;
			if( ReInit )
				request.latestDates = null;
			else
				request.latestDates = GetLatestDates();
			request.init = download_only || ReInit;
			
			if( action == ClientRequest.SYNC_UPLOAD )
				generate_UploadPackage();
			
			// open a socket connection
			Log.i("twix_agent:Twix_Client", "Creating a client socket to server.\n" );
			UpdateProgress(handler, "Connecting to Server...");
			
			/***************************************
			 * Socket Connection
			 ***************************************/
			socket = BuildSocket();
			// open I/O streams for objects
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			
			//***************************************
			//* Request 1 - Can be Download or Upload
			//***************************************
			UpdateProgress(handler, "Transfering Data...");
			Log.i("twix_agent:Twix_Client", "Sending initial data to server.\n" );
			oos.writeObject(request);
			//app.password = null;
			Log.i("twix_agent:Twix_Client", "Waiting for server response...\n" );
			
			response = keepAliveLooper(oos, ois);
			//response = (ServerResponse) ois.readObject();
			if( response.result == ServerResponse.SUCCESS && app.password != null )
				{
				app.prefs.edit()
					.putString("hash_user", app.oneWayHash(app.username) )
					.putString("hash_pw", app.oneWayHash(app.password) )
					.putBoolean("offline", false )
					.commit();
				}
			app.password = null;
			
			return_status = response.result;
			if( return_status == ServerResponse.SUCCESS )
				{
				// Set the application Employee No and Employee Email
				app.empno = response.EmpNo;
				app.techEmail = response.email;
				app.SecretKey = response.SecretKey;
				
				app.prefs.edit()
					.putString("app.empno", app.empno )
					.putString("app.techEmail", app.techEmail )
					.commit();
				
				// Set the Dirty Flags. The server is now the data master
				app.prefs.edit().putBoolean("sync_dirty", true).commit();
				
				//***************************************
				//* Request 2 - Download Data Only
				//***************************************
				Log.i("twix_agent:Twix_Client", "Sending Data Request\n" );
				if( action == ClientRequest.SYNC_UPLOAD )
					{
					request = new ClientRequest(deviceId, ClientRequest.SYNC_DOWNLOAD, null, app.version);
					request.username = app.username;
					request.password = app.password;
					request.SecretKey = app.SecretKey;
					if( ReInit )
						request.latestDates = null;
					else
						request.latestDates = GetLatestDates();
					request.init = download_only || ReInit;
					oos.writeObject(request);
					
					Log.i("twix_agent:Twix_Client", "Waiting for Response...\n" );
					UpdateProgress(handler, "Retrieving Response...");
					//response = (ServerResponse) ois.readObject();
					response = keepAliveLooper(oos, ois);
					
					return_status = response.result;
					}
				
				// Server Successful Transaction
				if( return_status == ServerResponse.SUCCESS )
					{
					Log.i("twix_agent:Twix_Client", "Processing the Response Data\n" );
					db.db.beginTransaction();
					// Delete all the tablet data
					app.db.deleteAll();
					
					// Tell the progress dialog to change text
					UpdateProgress(handler, "Processing Results");
					
					// Set the Data Dirty Flag
					app.prefs.edit().putBoolean("sync_dirty", true).commit();
					
					// Got the Response, now start processing. Received data is now dirty
					app.prefs.edit().putBoolean("data_dirty", true).commit();
					// Process the Response
					processResponse( response );
					ProcessFormPackage(response.FormPackage, response.FormDataPackage, ReInit);
					ProcessAttributeStructure(response.AttrPackage, ReInit);
					
					// Set the Data Clean Flag. Successful processing
					app.prefs.edit().putBoolean("firstStart", false)
									.putBoolean("sync_dirty", false)
									.putBoolean("data_dirty", false)
									.putBoolean("reqUpdate", false)
									.commit();
					
					Log.i("twix_agent:Twix_Client", "Processing Complete\n" );
					db.db.setTransactionSuccessful();
					Log.i("twix_agent:Twix_Client", "Transaction Complete. Tablet ready.\n" );
					
					}
				else
					Log.i("twix_agent:Twix_Client", "Server Transaction Failed.\n" );
				}
			else if ( response.result == ServerResponse.REQ_UPDATE )
				{
				app.prefs.edit().putBoolean("reqUpdate", true).commit();
				}
			else
				Log.i("twix_agent:Twix_Client", "User Error: Login Failed..." );
			
			CreateErrorMessage(handler, response.ErrorMessage);
			CreateUserMessage(handler, response.UserMessage);
			
			// Close the OutputStreams
			oos.close();
			ois.close();
			}
		catch (SocketTimeoutException se)
			{
			return_status = ServerResponse.TIMEOUT_EXCEPTION;
			}
		catch (UnknownHostException e3)
			{
			return_status = ServerResponse.NO_ROUTE_TO_HOST;
			}
		catch (IOException e2)
			{
			return_status = ServerResponse.IOEXCEPTION;
			}
		catch (Exception e)
			{
			// TODO: Create a special return fail here
			Log.e("twix_agent:Twix_Client", "Error Attempting to Sync", e );
			e.printStackTrace();
			}
		finally
			{
			if( return_status == ServerResponse.SUCCESS )
				db.db.endTransaction();
			}
		
		// Dismiss the Progress Dialog
		UpdateProgress(handler, null);
		
		// Close Everything for the Socket
		try{socket.shutdownInput();}catch(Exception e){}
		try{socket.shutdownOutput();}catch(Exception e){}
		try{socket.close();}catch(Exception e){}
		
		return return_status;
		}
	
	public int Connect_Sync(Handler handler, SiteSearch search, ArrayList<ServerResponse.SearchData> results )
		{
		if( !isOnline() )
			{
			UpdateProgress(handler, null);
			return CONNECTION_NOT_AVAILABLE;
			}
		
		int return_status = ServerResponse.LOGIN_FAILED;
		
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket = null;
		ServerResponse response = null;
		
		try
			{
			/***************************************
			 * Setup the Client Request
			 ***************************************/
			request = new ClientRequest( ClientRequest.SITE_SEARCH, app.version );
			request.username = app.username;
			request.password = app.password;
			request.SecretKey = app.SecretKey;
			request.search = search;
			
			// open a socket connection
			Log.i("twix_agent:Twix_Client", "Creating a client socket to server.\n" );
			UpdateProgress(handler, "Connecting to Server...");
			
			/***************************************
			 * Socket Connection
			 ***************************************/
			socket = BuildSocket();
			// open I/O streams for objects
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			//***************************************
			//* Send the Search Criteria
			//***************************************
			UpdateProgress(handler, "Transfering Data...");
			Log.i("twix_agent:Twix_Client", "Sending search query to Server.\n" );
			oos.writeObject(request);
			Log.i("twix_agent:Twix_Client", "Waiting for server response...\n" );
			response = (ServerResponse) ois.readObject();
			if( response.result == ServerResponse.SUCCESS && app.password != null )
				{
				app.prefs.edit()
					.putString("hash_user", app.oneWayHash(app.username) )
					.putString("hash_pw", app.oneWayHash(app.password) )
					.putBoolean("offline", false )
					.commit();
				}
			app.password = null;
			
			return_status = response.result;
			if( return_status == ServerResponse.SUCCESS )
				{
				app.empno = response.EmpNo;
				app.techEmail = response.email;
				app.SecretKey = response.SecretKey;
				results.addAll(response.searchResponse);
				}
			else
				{
				results = null;
				Log.i("twix_agent:Twix_Client", "User Error: Login Failed..." );
				}
			
			CreateErrorMessage(handler, response.ErrorMessage);
			CreateUserMessage(handler, response.UserMessage);
			
			// Close the OutputStreams
			oos.close();
			ois.close();
			}
		catch (SocketTimeoutException se)
			{
			return_status = ServerResponse.TIMEOUT_EXCEPTION;
			}
		catch (UnknownHostException e3)
			{
			return_status = ServerResponse.NO_ROUTE_TO_HOST;
			}
		catch (IOException e2)
			{
			return_status = ServerResponse.IOEXCEPTION;
			}
		catch (Exception e)
			{
			Log.e("twix_agent:Twix_Client", "Error Attempting to Sync", e );
			e.printStackTrace();
			}
		
		// Dismiss the Progress Dialog
		UpdateProgress(handler, null);
		
		// Close Everything for the Socket
		try{socket.shutdownInput();}catch(Exception e){}
		try{socket.shutdownOutput();}catch(Exception e){}
		try{socket.close();}catch(Exception e){}
		
		return return_status;
		}
	
	public int Connect_Sync(Handler handler, ArrayList<Integer> fetchSAIds)
		{
		if( !isOnline() )
			{
			UpdateProgress(handler, null);
			return CONNECTION_NOT_AVAILABLE;
			}
		
		int return_status = ServerResponse.LOGIN_FAILED;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket = null;
		ServerResponse response = null;
		int action = ClientRequest.SITE_DOWNLOAD;
		
		try
			{
			/***************************************
			 * Setup the Client Request
			 ***************************************/
			request = new ClientRequest(action, app.version );
			request.username = app.username;
			request.password = app.password;
			request.SecretKey = app.SecretKey;
			request.siteRequest = fetchSAIds;
			
			// open a socket connection
			Log.i("twix_agent:Twix_Client", "Creating a client socket to server.\n" );
			UpdateProgress(handler, "Connecting to Server...");
			
			/***************************************
			 * Socket Connection
			 ***************************************/
			socket = BuildSocket();
			// open I/O streams for objects
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			
			//***************************************
			//* Request 1 - Can be Download or Upload
			//***************************************
			UpdateProgress(handler, "Transfering Data...");
			Log.i("twix_agent:Twix_Client", "Sending initial data to server.\n" );
			oos.writeObject(request);
			Log.i("twix_agent:Twix_Client", "Waiting for server response...\n" );
			response = (ServerResponse) ois.readObject();
			if( response.result == ServerResponse.SUCCESS && app.password != null )
				{
				app.prefs.edit()
					.putString("hash_user", app.oneWayHash(app.username) )
					.putString("hash_pw", app.oneWayHash(app.password) )
					.putBoolean("offline", false )
					.commit();
				}
			app.password = null;
			
			// If successful, add all the data to the tablet database
			return_status = response.result;
			if( return_status == ServerResponse.SUCCESS )
				{
				app.empno = response.EmpNo;
				app.techEmail = response.email;
				app.SecretKey = response.SecretKey;
				UpdateProgress(handler, "Processing Response...");
				
				db.db.beginTransaction();
				
				// Process Service Address
				processServiceAddress(response.pkg.serviceAddress);
				processTenants(response.pkg.tenants);
				processContacts(response.pkg.contacts);
				processNotes(response.pkg.notes);
				processServiceAddressPMChecklist(response.pkg.pmAddressChecklist);
				
				// Process the resulting equipment
				processEquipment(response.pkg.equipment);
				processFans(response.pkg.fan, response.pkg.sheave);
				processFilters(response.pkg.filter);
				processRefCircuits(response.pkg.refcircuit, response.pkg.compressor);
				processClosedBlues(response.pkg.closedBlue, response.pkg.closedBlueUnit);
				
				// Process the resulting Closed Tags
				processClosedTags(response.pkg.serviceTag);
				processServiceUnit(response.pkg.serviceTagUnit, false);
				processServiceLabor(response.pkg.serviceLabor, false);
				processServiceRefrigerant(response.pkg.serviceRefrigerant, false);
				db.db.setTransactionSuccessful();
				}
			
			CreateErrorMessage(handler, response.ErrorMessage);
			CreateUserMessage(handler, response.UserMessage);
			
			// Close the OutputStreams
			oos.close();
			ois.close();
			}
		catch (SocketTimeoutException se)
			{
			return_status = ServerResponse.TIMEOUT_EXCEPTION;
			}
		catch (UnknownHostException e3)
			{
			return_status = ServerResponse.NO_ROUTE_TO_HOST;
			}
		catch (IOException e2)
			{
			return_status = ServerResponse.IOEXCEPTION;
			}
		catch (Exception e)
			{
			Log.e("twix_agent:Twix_Client", "Error Attempting to Sync", e );
			e.printStackTrace();
			}
		finally
			{
			if( return_status == ServerResponse.SUCCESS )
				db.db.endTransaction();
			}
		
		// Dismiss the Progress Dialog
		UpdateProgress(handler, null);
		
		// Close Everything for the Socket
		try{socket.shutdownInput();}catch(Exception e){}
		try{socket.shutdownOutput();}catch(Exception e){}
		try{socket.close();}catch(Exception e){}
		
		return return_status;
		}
	
	public int Connect_Update(Handler handler)
		{
		if( !isOnline() )
			{
			UpdateProgress(handler, null);
			return CONNECTION_NOT_AVAILABLE;
			}
		
		int return_status = ServerResponse.LOGIN_FAILED;
		
		String deviceId = app.getDeviceId();
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket = null;
		ServerResponse response = null;
		int action = ClientRequest.DOWNLOAD_UPDATE;
		
		try
			{
			/***************************************
			 * Setup the Client Request
			 ***************************************/
			request = new ClientRequest(deviceId, action, null, app.version );
			request.username = app.username;
			request.password = app.password;
			request.SecretKey = app.SecretKey;
			
			// open a socket connection
			Log.i("twix_agent:Twix_Client", "Creating a client socket to server.\n" );
			UpdateProgress(handler, "Connecting to Server...");
			
			/***************************************
			 * Socket Connection
			 ***************************************/
			socket = BuildSocket();
			// open I/O streams for objects
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			//***************************************
			//* Request 1 - Can be Download or Upload
			//***************************************
			UpdateProgress(handler, "Transfering Data...");
			Log.i("twix_agent:Twix_Client", "Sending initial data to server.\n" );
			oos.writeObject(request);
			Log.i("twix_agent:Twix_Client", "Waiting for server response...\n" );
			response = (ServerResponse) ois.readObject();
			if( response.result == ServerResponse.SUCCESS && app.password != null )
				{
				app.prefs.edit()
					.putString("hash_user", app.oneWayHash(app.username) )
					.putString("hash_pw", app.oneWayHash(app.password) )
					.putBoolean("offline", false )
					.commit();
				}
			app.password = null;
			
			return_status = response.result;
			if( response.result == ServerResponse.SUCCESS && response.UpdateFile != null )
				{
				// If we can't read or create the .temp directory, then don't bother downloading.
				if( app.checkTempDir() )
					{
					FileOutputStream fos = null;
					try
						{
						File f = new File(Environment.getExternalStorageDirectory() + "/.temp", "Twix_Agent.apk");
						fos = new FileOutputStream(f);
						fos.write(response.UpdateFile);
						}
					catch (IOException ioe)
						{
						Log.e("twix_agent:Twix_Client", "Error writing Twix Agent Update to SD Card.\n" );
						ioe.printStackTrace();
						}
					finally
						{
						if( fos != null )
							fos.close();
						}
					}
				else
					{
					Log.e("twix_agent", "Error creating or reading the '.temp' directory. Aborting update install.");
					return_status = ServerResponse.IOEXCEPTION;
					}
				
				}
			else
				Log.i("twix_agent:Twix_Client", "User Error: Login Failed..." );
			
			CreateErrorMessage(handler, response.ErrorMessage);
			CreateUserMessage(handler, response.UserMessage);
			
			// Close the OutputStreams
			oos.close();
			ois.close();
			}
		catch (SocketTimeoutException se)
			{
			return_status = ServerResponse.TIMEOUT_EXCEPTION;
			}
		catch (UnknownHostException e3)
			{
			return_status = ServerResponse.NO_ROUTE_TO_HOST;
			}
		catch (IOException e2)
			{
			return_status = ServerResponse.IOEXCEPTION;
			}
		catch (Exception e)
			{
			Log.e("twix_agent:Twix_Client", "Error Attempting to Sync", e );
			e.printStackTrace();
			}
		
		// Dismiss the Progress Dialog
		UpdateProgress(handler, null);
		
		// Close Everything for the Socket
		try{socket.shutdownInput();}catch(Exception e){}
		try{socket.shutdownOutput();}catch(Exception e){}
		try{socket.close();}catch(Exception e){}
		
		return return_status;
		}
	
	public int Connect_DispatchRequest(Handler handler, DispatchRequest dRequest)
		{
		if( !isOnline() )
			{
			UpdateProgress(handler, null);
			return CONNECTION_NOT_AVAILABLE;
			}
		
		int return_status = ServerResponse.LOGIN_FAILED;
		
		String deviceId = app.getDeviceId();
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket = null;
		ServerResponse response = null;
		int action = ClientRequest.ASSIGN_MECHANIC;
		
		try
			{
			/***************************************
			 * Setup the Client Request
			 ***************************************/
			request = new ClientRequest(deviceId, action, null, app.version );
			request.username = app.username;
			request.password = app.password;
			request.SecretKey = app.SecretKey;
			request.Package = dRequest;
			
			// open a socket connection
			Log.i("twix_agent:Twix_Client", "Creating a client socket to server.\n" );
			UpdateProgress(handler, "Connecting to Server...");
			
			/***************************************
			 * Socket Connection
			 ***************************************/
			socket = BuildSocket();
			// open I/O streams for objects
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			//***************************************
			//* Send the Request
			//***************************************
			UpdateProgress(handler, "Transfering Data...");
			Log.i("twix_agent:Twix_Client", "Sending initial data to server.\n" );
			oos.writeObject(request);
			Log.i("twix_agent:Twix_Client", "Waiting for server response...\n" );
			
			//***************************************
			//* Receive the Response
			//***************************************
			response = (ServerResponse) ois.readObject();
			return_status = response.result;
			if( return_status == ServerResponse.SUCCESS )
				{
				app.prefs.edit().putString("hash_user", app.oneWayHash(app.username) )
					.putBoolean("offline", false );
				if( app.password != null )
					app.prefs.edit().putString("hash_pw", app.oneWayHash(app.password) );
				app.prefs.edit().commit();
					
				
				// Set the application Employee No and Employee Email
				app.empno = response.EmpNo;
				app.techEmail = response.email;
				app.SecretKey = response.SecretKey;
				
				
				//else
				//	return_status = ServerResponse.DISPATCH_SLOT_NOT_AVAILABLE;
				}
			
			if( response.pkg != null )
				{
				int size = response.pkg.dispatch.size();
				if( size == 1 )
					{
					Dispatch d = response.pkg.dispatch.get(0);
					app.db.delete("dispatch", "dispatchId", d.dispatchId);
					
					processDispatch(response.pkg.dispatch);
					}
				}
			
			app.password = null;
			
			CreateErrorMessage(handler, response.ErrorMessage);
			CreateUserMessage(handler, response.UserMessage);
			
			// Close the OutputStreams
			oos.close();
			ois.close();
			}
		catch (SocketTimeoutException se)
			{
			return_status = ServerResponse.TIMEOUT_EXCEPTION;
			}
		catch (UnknownHostException e3)
			{
			return_status = ServerResponse.NO_ROUTE_TO_HOST;
			}
		catch (IOException e2)
			{
			return_status = ServerResponse.IOEXCEPTION;
			}
		catch (Exception e)
			{
			Log.e("twix_agent:Twix_Client", "Error Attempting to Sync", e );
			e.printStackTrace();
			}
		
		// Dismiss the Progress Dialog
		UpdateProgress(handler, null);
		
		// Close Everything for the Socket
		try{socket.shutdownInput();}catch(Exception e){}
		try{socket.shutdownOutput();}catch(Exception e){}
		try{socket.close();}catch(Exception e){}
		
		return return_status;
		}
	
	/*
	 ************************* Generate the Upload Package ******************************
	 */
	
	private void generate_UploadPackage()
		{
		Log.i("twix_agent:Twix_Client", "Starting Package Generation");
		request.pkg = new Package_Upload();
		
		getOpenTags();
		getEquipment();
		getContacts();
		getNotes();
		getGroups();
		
		GetAndBuildFormData();
		}
	
	private void getOpenTags()
		{
		Log.i("twix_agent:Twix_Client", " - Generating Open Tags");
		OpenServiceTag tag;
		
		String sqlQ =
			"SELECT serviceTagId, serviceAddressId, dispatchId, " +
				"serviceType, serviceDate, billTo, billAddress1, billAddress2, billAddress3, billAddress4, " +
				"billAttn, siteName, tenant, address1, address2, city, state, zip, buildingNo, note, " +
				"batchNo, jobNo, empno, disposition, completed, xoi_flag " +
			"FROM openServiceTag ";
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				
				tag = new OpenServiceTag();
				tag.serviceTagId		= cursor.getInt(0);
				tag.serviceAddressId	= cursor.getInt(1);
				tag.dispatchId			= cursor.getInt(2);
				tag.serviceType			= cursor.getString(3);
				tag.serviceDate			= cursor.getString(4);
				tag.billTo				= cursor.getString(5);
				tag.billAddress1		= cursor.getString(6);
				tag.billAddress2		= cursor.getString(7);
				tag.billAddress3		= cursor.getString(8);
				tag.billAddress4		= cursor.getString(9);
				tag.billAttn			= cursor.getString(10);
				tag.siteName			= cursor.getString(11);
				tag.tenant				= cursor.getString(12);
				tag.address1			= cursor.getString(13);
				tag.address2			= cursor.getString(14);
				tag.city				= cursor.getString(15);
				tag.state				= cursor.getString(16);
				tag.zip					= cursor.getString(17);
				tag.buildingNo			= cursor.getString(18);
				tag.note				= cursor.getString(19);
				tag.batchNo				= cursor.getString(20);
				tag.jobNo				= cursor.getString(21);
				tag.empno				= cursor.getString(22);
				tag.disposition			= cursor.getString(23);
				tag.xoi_flag			=cursor.getString(25);
				
				// Set the submission status of the tag
				tag.submit = Twix_TextFunctions.clean( cursor.getString(24) ).contentEquals("Y");
				
				tag.units = getServiceTagUnits(tag.serviceTagId, tag.submit);
				tag.blue = getOpenBlue(tag.serviceTagId);
				tag.safetyChecklist = getOpenSafetyChecklist(tag.serviceTagId);
				if( tag.submit )
					tag.receipts = getServiceReceipts(tag.serviceTagId);
				
				// Add to the upload package
				request.pkg.openTags.add(tag);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void getEquipment()
		{
		Log.i("twix_agent:Twix_Client", " - Generating Equipment");
		Equipment eq;
		
		String sqlQ =
				"SELECT equipmentId, equipmentCategoryId," +
					"serviceAddressId, unitNo, barCodeNo, manufacturer, model, " +
					"productIdentifier, serialNo, voltage, economizer, capacity, " +
					"capacityUnits, refrigerantType, areaServed, mfgYear, " +
					"dateInService, dateOutService, notes, verified, verifiedByEmpno " +
				"FROM equipment " +
				"WHERE modified == 'Y'";
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				
				eq = new Equipment();
				
				eq.equipmentId			= cursor.getInt(0);
				eq.equipmentCategoryId	= cursor.getInt(1);
				eq.serviceAddressId		= cursor.getInt(2);
				eq.unitNo				= cursor.getString(3);
				eq.barCodeNo			= cursor.getString(4);
				eq.manufacturer			= cursor.getString(5);
				eq.model				= cursor.getString(6);
				eq.productIdentifier	= cursor.getString(7);
				eq.serialNo				= cursor.getString(8);
				eq.voltage				= cursor.getString(9);
				eq.economizer			= cursor.getString(10);
				eq.capacity				= cursor.getFloat(11);
				eq.capacityUnits		= cursor.getString(12);
				eq.refrigerantType		= cursor.getString(13);
				eq.areaServed			= cursor.getString(14);
				eq.mfgYear				= cursor.getString(15);
				eq.dateInService		= cursor.getString(16);
				eq.dateOutService		= cursor.getString(17);
				eq.notes				= cursor.getString(18);
				if( Twix_TextFunctions.clean(cursor.getString(19)).contentEquals("Y") )
					eq.verifiedByEmpno	= app.empno;
				else
					eq.verifiedByEmpno	= cursor.getString(20);
				
				eq.fans = getFans(eq.equipmentId);
				eq.filters =  getFilters(eq.equipmentId);
				eq.refCircuits = getRefCircuits(eq.equipmentId);
				
				// Add the equipment to the upload package
				request.pkg.equipment.add(eq);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		}
	
	private void getContacts()
		{
		Log.i("twix_agent:Twix_Client", " - Generating Site Contacts");
		ServiceAddressContact contact;
		
		String sqlQ =
				"SELECT contactId, serviceAddressId, " +
					"contactName, phone1, phone1Type, phone2, phone2Type, " +
					"email, contactType, ext1, ext2 " +
				"FROM serviceAddressContact " +
				"WHERE modified = 'Y'";
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			request.pkg.contact = new ArrayList<ServiceAddressContact>();
			do
				{
				index = 0;
				
				contact = new ServiceAddressContact();
				contact.contactId			= cursor.getInt(0);
				contact.serviceAddressId	= cursor.getInt(1);
				contact.contactName			= cursor.getString(2);
				contact.phone1				= cursor.getString(3);
				contact.phone1Type			= cursor.getString(4);
				contact.phone2				= cursor.getString(5);
				contact.phone2Type			= cursor.getString(6);
				contact.email				= cursor.getString(7);
				contact.contactType			= cursor.getString(8);
				contact.ext1				= cursor.getString(9);
				contact.ext2				= cursor.getString(10);
				
				request.pkg.contact.add(contact);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void getNotes()
		{
		Log.i("twix_agent:Twix_Client", " - Generating Site Notes");
		ServiceAddressNotes notes;
		
		String sqlQ =
				"SELECT noteid, serviceaddressid, " +
					"notes " +
				"FROM notes " +
				"WHERE modified = 'Y'";
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			request.pkg.notes = new ArrayList<ServiceAddressNotes>();
			do
				{
				index = 0;
				
				notes = new ServiceAddressNotes();
				notes.noteId			= cursor.getInt(0);
				notes.serviceAddressId	= cursor.getInt(1);
				notes.notes			= cursor.getString(2);
				
				
				
				request.pkg.notes.add(notes);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	
	private void getGroups()
		{
		Log.i("twix_agent:Twix_Client", " - Generating Groups and Xref");
		ServiceTagGroup group;
		int groupId;
		
		String sqlQ =
				"SELECT groupId, signature, noSignatureReason, dateCreated, emailTo " +
				"FROM serviceTagGroup";
		Cursor cursor = db.rawQuery(sqlQ);
		Cursor cursor2;
		
		// Signature Compressing Variables
		byte[] sig;
		Bitmap temp;
		ByteArrayOutputStream os;
		
		if (cursor.moveToFirst())
			{
			do
				{
				groupId = cursor.getInt(0);
				
				group = new ServiceTagGroup();
				sig = cursor.getBlob(1);
				if( sig != null )
					{
					temp = BitmapFactory.decodeByteArray(sig, 0, sig.length);
					temp = Bitmap.createScaledBitmap(temp, temp.getWidth()/3, temp.getHeight()/3, false);
					os = new ByteArrayOutputStream();
					temp.compress(CompressFormat.JPEG, 35, os);
					sig = os.toByteArray();
					temp.recycle();
					}
				group.signature			= sig;
				group.noSignatureReason	= cursor.getString(2);
				group.dateCreated		= cursor.getString(3);
				group.emailList			= cursor.getString(4);
				
				sqlQ = "SELECT serviceTagId " +
						"FROM serviceTagGroupXref " +
						"WHERE groupId = " + groupId;
				cursor2 = db.rawQuery(sqlQ);
				if( cursor2.moveToFirst() )
					{
					do
						{
						group.serviceTagXref.add(cursor2.getInt(0));
						}
					while( cursor2.moveToNext() );
					}
				if (cursor2 != null && !cursor2.isClosed())
					{
					cursor2.close();
					}
				
				request.pkg.groups.add(group);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
		// Equipment Fan
	private ArrayList<Fan> getFans(int equipmentId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Fans for EquipmentId = '" + equipmentId + "'");
		
		ArrayList<Fan> list = new ArrayList<Fan>();
		Fan fan; Sheave sheave;
		
		String sqlQ =
				"SELECT fanId, number, partType " +
				"FROM fan " +
					"WHERE equipmentId = " + equipmentId;
		Cursor cursor = db.rawQuery(sqlQ);
		Cursor cursor2;
		if (cursor.moveToFirst())
			{
			do
				{
				fan = new Fan();
				fan.fanId		= cursor.getInt(0);
				fan.number		= cursor.getString(1);
				fan.partType	= cursor.getString(2);
				fan.sheaves = new ArrayList<Sheave>();
				
				sqlQ = "SELECT beltSize, quantity " +
						"FROM belt " +
							"WHERE fanId = " + fan.fanId;
				cursor2 = db.rawQuery(sqlQ);
				if (cursor2.moveToFirst())
					{
					fan.beltSize	= cursor2.getString(0);
					fan.beltQty		= cursor2.getInt(1);
					}
				if (cursor2 != null && !cursor2.isClosed())
					{
					cursor2.close();
					}
				
				sqlQ = "SELECT type, number, manufacturer " +
						"FROM sheave " +
							"WHERE fanId = " + fan.fanId;
				cursor2 = db.rawQuery(sqlQ);
				if (cursor2.moveToFirst())
					{
					do
						{
						sheave = new Sheave();
						sheave.type			= cursor2.getString(0);
						sheave.number		= cursor2.getString(1);
						sheave.manufacturer	= cursor2.getString(2);
						
						fan.sheaves.add(sheave);
						}
					while(cursor2.moveToNext());
					}
				if (cursor2 != null && !cursor2.isClosed())
					{
					cursor2.close();
					}
				
				list.add(fan);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
				
		return list;
		}
	
		// Equipment Filters
	private ArrayList<Filter> getFilters(int equipmentId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Filters for EquipmentId = '" + equipmentId + "'");
		ArrayList<Filter> list = new ArrayList<Filter>();
		Filter fil;
		
		String sqlQ =
				"SELECT type, quantity, filterSize " +
				"FROM filter " +
					"WHERE equipmentId = " + equipmentId;
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			do
				{
				fil = new Filter();
				fil.type		= cursor.getString(0);
				fil.quantity	= cursor.getInt(1);
				fil.filterSize	= cursor.getString(2);
				
				list.add(fil);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
				
		return list;
		}
	
		// Equipment RefCircuits
	private ArrayList<RefCircuit> getRefCircuits(int equipmentId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating RefCircuits for EquipmentId = '" + equipmentId + "'");
		
		ArrayList<RefCircuit> list = new ArrayList<RefCircuit>();
		RefCircuit ref; Compressor comp;
		
		String sqlQ =
				"SELECT circuitId, circuitNo, lbsRefrigerant " +
				"FROM refCircuit " +
					"WHERE equipmentId = " + equipmentId;
		Cursor cursor = db.rawQuery(sqlQ);
		Cursor cursor2;
		if (cursor.moveToFirst())
			{
			do
				{
				ref = new RefCircuit();
				ref.circuitId		= cursor.getInt(0);
				ref.circuitNo		= cursor.getString(1);
				ref.lbsRefrigerant	= cursor.getFloat(2);
				ref.compressors = new ArrayList<Compressor>();
				
				sqlQ = "SELECT compressorNo, manufacturer, model, serialNo, dateInService, dateOutService " +
						"FROM compressor " +
							"WHERE circuitId = " + ref.circuitId;
				cursor2 = db.rawQuery(sqlQ);
				if (cursor2.moveToFirst())
					{
					do
						{
						comp = new Compressor();
						comp.compressorNo	= cursor2.getString(0);
						comp.manufacturer	= cursor2.getString(1);
						comp.model			= cursor2.getString(2);
						comp.serialNo		= cursor2.getString(3);
						comp.dateInService	= cursor2.getString(4);
						comp.dateOutService	= cursor2.getString(5);
						
						ref.compressors.add(comp);
						}
					while(cursor2.moveToNext());
					}
				if (cursor2 != null && !cursor2.isClosed())
					{
					cursor2.close();
					}
				
				list.add(ref);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
				
		return list;
		}
	
	// Open Service Tag Units
	private ArrayList<ServiceTagUnit> getServiceTagUnits(int serviceTagId, boolean submit)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Service Units for ServiceTagId = '" + serviceTagId + "'");
		ArrayList<ServiceTagUnit> units = new ArrayList<ServiceTagUnit>();
		ServiceTagUnit unit;
		
		String sqlQ =
				"SELECT serviceTagUnitId, equipmentId, servicePerformed, comments " +
				"FROM serviceTagUnit " +
				"WHERE serviceTagId = " + serviceTagId + "";
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				unit = new ServiceTagUnit();
				unit.serviceTagUnitId	= cursor.getInt(0);
				unit.equipmentId		= cursor.getInt(1);
				unit.servicePerformed	= cursor.getString(2);
				unit.comments			= cursor.getString(3);
				
				unit.labor = getServiceLabor(unit.serviceTagUnitId);
				unit.material = getServiceMaterial(unit.serviceTagUnitId);
				unit.refrigerant = getServiceRefrigerant(unit.serviceTagUnitId);
				unit.pmChecklist = getPMChecklist(unit.serviceTagUnitId);
				if( submit )
					unit.photos = getUnitPhotos(unit.serviceTagUnitId);
				
				units.add(unit);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return units;
		}
	
	// Generic ServiceTagUnit details
	private ArrayList<ServiceLabor> getServiceLabor(int serviceTagUnitId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating ServiceLabor for serviceTagUnitId = '" + serviceTagUnitId + "'");
		ArrayList<ServiceLabor> list = new ArrayList<ServiceLabor>();
		ServiceLabor item;
		
		String sqlQ =
				"SELECT serviceDate, regHours, thHours, dtHours, mechanic, rate " +
				"FROM serviceLabor " +
					"WHERE serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				item = new ServiceLabor();
				item.serviceDate		= cursor.getString(0);
				item.regHours			= cursor.getFloat(1);
				item.thHours			= cursor.getFloat(2);
				item.dtHours			= cursor.getFloat(3);
				item.mechanic			= cursor.getString(4);
				item.rate				= cursor.getString(5);
				list.add(item);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return list;
		}
	
	private ArrayList<ServiceMaterial> getServiceMaterial(int serviceTagUnitId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating ServiceMaterial for serviceTagUnitId = '" + serviceTagUnitId + "'");
		ArrayList<ServiceMaterial> list = new ArrayList<ServiceMaterial>();
		ServiceMaterial item;
		
		String sqlQ =
				"SELECT quantity, materialDesc, cost, refrigerantAdded, source " +
				"FROM serviceMaterial " +
					"WHERE serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				item = new ServiceMaterial();
				item.quantity			= cursor.getFloat(0);
				item.materialDesc		= cursor.getString(1);
				item.cost				= cursor.getFloat(2);
				item.refrigerantAdded	= cursor.getString(3);
				item.source				= cursor.getString(4);
				
				list.add(item);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return list;
		}
	
	private ArrayList<ServiceRefrigerant> getServiceRefrigerant(int serviceTagUnitId)
	{
	Log.i("twix_agent:Twix_Client", " - Generating ServiceRefrigerant for serviceTagUnitId = '" + serviceTagUnitId + "'");
	ArrayList<ServiceRefrigerant> list = new ArrayList<ServiceRefrigerant>();
	ServiceRefrigerant item;
	
	String sqlQ =
			"SELECT transferDate, techName, typeOfRefrigerant, amount, nameOfCylinder, cylinderSerialNo, transferedTo, modelNo, serialNo " +
			"FROM serviceRefrigerant " +
				"WHERE serviceTagUnitId = " + serviceTagUnitId;
	Cursor cursor = db.rawQuery(sqlQ);
	int index;
	if (cursor.moveToFirst())
		{
		do
			{
			index = 0;
			item = new ServiceRefrigerant();
			item.transferDate	= cursor.getString(0);
			item.techName	= cursor.getString(1);
			item.typeOfRefrigerant			= cursor.getString(2);
			item.amount	= cursor.getFloat(3);
			item.nameOfCylinder			= cursor.getString(4);
			item.cylinderSerialNo		= cursor.getString(5);
			item.transferedTo		= cursor.getString(6);
			item.modelNo			= cursor.getString(7);
			item.serialNo			= cursor.getString(8);
			list.add(item);
			}
	    while (cursor.moveToNext());
		}
	if (cursor != null && !cursor.isClosed())
		{
		cursor.close();
		}
		
	return list;
	}

	
	private ArrayList<PMChecklist> getPMChecklist(int serviceTagUnitId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating PMChecklist for serviceTagUnitId = '" + serviceTagUnitId + "'");
		ArrayList<PMChecklist> list = new ArrayList<PMChecklist>();
		PMChecklist item;
		
		String sqlQ =
				"SELECT itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
					"WHERE serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				item = new PMChecklist();
				item.itemText		= cursor.getString(0);
				item.itemType		= cursor.getString(1);
				item.itemValue		= cursor.getString(2);
				item.itemComment	= cursor.getString(3);
				item.identifier     = cursor.getString(4);
				list.add(item);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return list;
		}
	
	private ArrayList<Photo> getUnitPhotos(int serviceTagUnitId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Unit Photos for serviceTagUnitId = '" + serviceTagUnitId + "'");
		
		ArrayList<Photo> list = new ArrayList<Photo>();
		Photo item;
		
		String sqlQ =
				"SELECT photoDate, photo, comments " +
				"FROM servicePhoto " +
					"WHERE serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				item = new Photo();
				item.photoDate	= cursor.getString(0);
				item.photo		= cursor.getBlob(1);
				item.comments	= cursor.getString(2);
				
				list.add(item);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return list;
		}
	
	// Blues
	private OpenBlue getOpenBlue(int serviceTagId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Blues for serviceTagId = '" + serviceTagId + "'");
		
		OpenBlue blue = null;
		OpenBlueUnit unit;
		
		String sqlQ =
				"SELECT blueId, dateCreated " +
				"FROM blue " +
					"WHERE serviceTagId = " + serviceTagId;
		
		Cursor cursorUnit;
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			blue = new OpenBlue();
			blue.dateCreated	= cursor.getString(1);
			
			sqlQ = "SELECT equipmentId, description, materials, laborHours, tradesmenhrs, otherhrs, notes, cost, completed " +
					"FROM blueUnit " +
					"WHERE blueId = " + cursor.getInt(0);
			cursorUnit = db.rawQuery(sqlQ);
			if (cursorUnit.moveToFirst())
				{
				do
					{
					unit = new OpenBlueUnit();
					unit.equipmentId	= cursorUnit.getInt(0);
					unit.description	= cursorUnit.getString(1);
					unit.materials		= cursorUnit.getString(2);
					unit.laborHours		= cursorUnit.getFloat(3);
					unit.tradesmenhrs		= cursorUnit.getFloat(4);
					unit.otherhrs		= cursorUnit.getFloat(5);
					unit.notes			= cursorUnit.getString(6);
					unit.cost			= cursorUnit.getFloat(7);
					unit.completed		= cursorUnit.getString(8);
					
					blue.units.add(unit);
					}
			    while (cursorUnit.moveToNext());
				}
			if (cursorUnit != null && !cursorUnit.isClosed())
				{
				cursorUnit.close();
				}
			
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return blue;
		}
	
	// SafetyChecklist
	private OpenSafetyTagChecklist getOpenSafetyChecklist(int serviceTagId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating SafetyChecklist for serviceTagId = '" + serviceTagId + "'");
		
		OpenSafetyTagChecklist list = null;
		ChecklistItem item;
		
		String sqlQ =
				"SELECT checklistDate, comments " +
				"FROM safetyTagChecklist " +
					"WHERE serviceTagId = " + serviceTagId;
		
		Cursor cursorItem;
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			list = new OpenSafetyTagChecklist();
			list.items = new ArrayList<ChecklistItem>();
			list.checkListDate	= cursor.getString(0);
			list.comments		= cursor.getString(1);
			
			sqlQ = "SELECT safetyChecklistId, itemRequired, itemValue " +
					"FROM safetyTagChecklistItem " +
						"WHERE serviceTagId = " + serviceTagId;
			cursorItem = db.rawQuery(sqlQ);
			if (cursorItem.moveToFirst())
				{
				do
					{
					item = new ChecklistItem();
					item.safetyChecklistId	= cursorItem.getInt(0);
					item.itemRequired		= cursorItem.getString(1);
					item.itemValue			= cursorItem.getString(2);
					
					list.items.add(item);
					}
			    while (cursorItem.moveToNext());
				}
			if (cursorItem != null && !cursorItem.isClosed())
				{
				cursorItem.close();
				}
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return list;
		}
	
	// Receipts
	private ArrayList<Receipt> getServiceReceipts(int serviceTagId)
		{
		Log.i("twix_agent:Twix_Client", " - Generating Service Receipts for serviceTagId = '" + serviceTagId + "'");
		
		ArrayList<Receipt> list = new ArrayList<Receipt>();
		Receipt item;
		
		String sqlQ =
				"SELECT photoDate, photo, comments " +
				"FROM serviceReceipt " +
					"WHERE serviceTagId = " + serviceTagId;
		Cursor cursor = db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				item = new Receipt();
				item.photoDate	= cursor.getString(0);
				item.photo		= cursor.getBlob(1);
				item.comments	= cursor.getString(2);
				
				list.add(item);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
			
		return list;
		}
	
	/************************** Process the Download Package *******************************/
	private void processResponse( ServerResponse response )
		{
		// Open Tags
		processOpenTags(response.pkg.openServiceTag);
		processServiceUnit(response.pkg.openServiceTagUnit, true);
		processServiceLabor(response.pkg.openServiceLabor, true);
		processServiceMaterial(response.pkg.openServiceMaterial, true);
		processServiceRefrigerant(response.pkg.openServiceRefrigerant, true);
		processPMChecklist(response.pkg.openPMChecklist, true);
		processOpenBlue(response.pkg.openBlue, response.pkg.openBlueUnit);
		processOpenSafetyChecklist(response.pkg.SafetyTagChecklist, response.pkg.SafetyTagChecklistItem);
		
		
		// Closed Tags
		processClosedTags(response.pkg.serviceTag);
		processServiceUnit(response.pkg.serviceTagUnit, false);
		processServiceLabor(response.pkg.serviceLabor, false);
		processServiceMaterial(response.pkg.serviceMaterial, false);
		processServiceRefrigerant(response.pkg.serviceRefrigerant, false);
		//processServiceRefrigerant(response.pkg.serviceRefrigerant, false);
		
		// Dispatches and Site Details
		processDispatch(response.pkg.dispatch);
		
		// Equipment and Details
		processEquipment(response.pkg.equipment);
		processFans(response.pkg.fan, response.pkg.sheave);
		processFilters(response.pkg.filter);
		processRefCircuits(response.pkg.refcircuit, response.pkg.compressor);
		processClosedBlues(response.pkg.closedBlue, response.pkg.closedBlueUnit);
		
		// Service Address and Details
		processServiceAddress(response.pkg.serviceAddress);
		processTenants(response.pkg.tenants);
		processContacts(response.pkg.contacts);
		processNotes(response.pkg.notes);
		processServiceAddressPMChecklist(response.pkg.pmAddressChecklist);
		
		// Static Tables
		processStaticTables(response);
		
		// Process Dispatch Billing
		processBilling(response.pkg.billing);
		
		// Process Dispatch Priority
		processDispatchPriority(response.pkg.dispatchPriority);
		
		// Process the new Service Tag Ids and Service Unit Ids. These resolve IDs so photos and receipts are still attached
		//processMaps(response.pkg.serviceTagId_map, response.pkg.serviceTagUnitId_map);
		ResolveIntIds("ServiceReceipt", "ServiceTagId", response.pkg.ResolvedServiceTagIds, null, null);
		ResolveIntIds("ServicePhoto", "ServiceTagUnitId", response.pkg.ResolvedServiceTagUnitIds, null, null);
		ResolveIntIds("FormData", "ParentId", response.pkg.ResolvedServiceTagUnitIds, "ParentTable", "'ServiceTagUnit'");
		
		// Process Picklists
		try
			{
			ProcessArrayList("PickList", response.pkg.PickList);
			ProcessArrayList("PickListItem", response.pkg.PickListItem);
			}
		catch ( IllegalArgumentException e )
			{ e.printStackTrace(); }
		catch ( IllegalAccessException e )
			{ e.printStackTrace(); }
		
		// Set the new Sync Time
		app.last_sync = response.syncTime;
		}
	
	// Process Open Tags
	private void processOpenTags(List<OpenServiceTag> tags)
		{
		ContentValues cv = new ContentValues();
		OpenServiceTag tag;
		
		int size = tags.size();
		
		for( int i = 0; i < size; i++ )
			{
			tag = tags.get(i);
			cv.put("serviceTagId",		tag.serviceTagId);
			cv.put("serviceAddressId",	tag.serviceAddressId);
			cv.put("dispatchId",		tag.dispatchId);
			cv.put("serviceType",		tag.serviceType);
			cv.put("serviceDate",		tag.serviceDate);
			cv.put("billTo",			tag.billTo);
			cv.put("billAddress1",		tag.billAddress1);
			cv.put("billAddress2",		tag.billAddress2);
			cv.put("billAddress3",		tag.billAddress3);
			cv.put("billAddress4",		tag.billAddress4);
			cv.put("billAttn",			tag.billAttn);
			cv.put("siteName",			tag.siteName);
			cv.put("tenant",			tag.tenant);
			cv.put("address1",			tag.address1);
			cv.put("address2",			tag.address2);
			cv.put("city",				tag.city);
			cv.put("state",				tag.state);
			cv.put("zip",				tag.zip);
			cv.put("buildingNo",		tag.buildingNo);
			cv.put("note",				tag.note);
			cv.put("batchNo",			tag.batchNo);
			cv.put("jobNo",				tag.jobNo);
			cv.put("customerPO",		tag.customerPO);
			cv.put("requestedBy",		tag.requestedBy);
			cv.put("requestedByPhone",	tag.requestedByPhone);
			cv.put("requestedByEmail",	tag.requestedByEmail);
			cv.put("description",		tag.description);
			cv.put("empno",				tag.empno);
			cv.put("disposition",		tag.disposition);
			cv.put("xoi_flag",          tag.xoi_flag);
			if( db.db.insertOrThrow("openServiceTag", null, cv) == -1)
				throw new SQLiteException("Failed to insert OpenService Tag");
			cv.clear();
			}
		
		}
	
	private void processServiceUnit(List<ServiceTagUnit> units, boolean open)
		{
		ContentValues cv = new ContentValues();
		ServiceTagUnit unit;
		int size = units.size();
		
		for( int i = 0; i < size; i++ )
			{
			unit = units.get(i);
			
			cv.put("serviceTagId",			unit.serviceTagId);
			if( open )
				unit.serviceTagUnitId = unit.serviceTagUnitId * -1;
			cv.put("serviceTagUnitId",		unit.serviceTagUnitId);
			cv.put("equipmentId",			unit.equipmentId);
			cv.put("servicePerformed",		unit.servicePerformed);
			cv.put("comments",				unit.comments);
			db.db.insertOrThrow("serviceTagUnit", null, cv);
			cv.clear();
			}
		}
	
	private void processServiceLabor( List<ServiceLabor> list, boolean open )
		{
		ServiceLabor labor;
		int size = list.size();
		ContentValues cv = new ContentValues();
		
		for( int i = 0; i < size; i++ )
			{
			labor = list.get(i);
			
			if( open )
				{
				labor.serviceLaborId = labor.serviceLaborId * -1;
				labor.serviceTagUnitId = labor.serviceTagUnitId * -1;
				}
			cv.put("serviceLaborId",	labor.serviceLaborId);
			cv.put("serviceTagUnitId",	labor.serviceTagUnitId);
			cv.put("serviceDate",		labor.serviceDate);
			cv.put("regHours",			labor.regHours);
			cv.put("thHours",			labor.thHours);
			cv.put("dtHours",			labor.dtHours);
			cv.put("mechanic",			labor.mechanic);
			cv.put("rate",			labor.rate);
			db.db.insertOrThrow("serviceLabor", null, cv);
			cv.clear();
			}
		}
	
	private void processServiceMaterial( List<ServiceMaterial> list, boolean open )
		{
		ServiceMaterial material;
		int size = list.size();
		ContentValues cv = new ContentValues();
		
		for( int i = 0; i < size; i++ )
			{
			material = list.get(i);
			
			if( open )
				{
				material.serviceMaterialId = material.serviceMaterialId * -1;
				material.serviceTagUnitId = material.serviceTagUnitId * -1;
				}
			cv.put("serviceMaterialId",	material.serviceMaterialId);
			cv.put("serviceTagUnitId",	material.serviceTagUnitId);
			cv.put("quantity",			material.quantity);
			cv.put("materialDesc",		material.materialDesc);
			cv.put("refrigerantAdded",	material.refrigerantAdded);
			cv.put("source",			material.source);
			cv.put("cost",				material.cost);
			
			db.db.insertOrThrow("serviceMaterial", null, cv);
			cv.clear();
			}
		}
	
	private void processServiceRefrigerant( List<ServiceRefrigerant> list, boolean open )
		{
		ServiceRefrigerant refrigerant;
		int size = list.size();
		ContentValues cv = new ContentValues();
		
		for( int i = 0; i < size; i++ )
			{
			refrigerant = list.get(i);
			
			if( open )
				{
				refrigerant.serviceRefrigerantId = refrigerant.serviceRefrigerantId * -1;
				refrigerant.serviceTagUnitId = refrigerant.serviceTagUnitId * -1;
				}
			cv.put("serviceRefrigerantId",	refrigerant.serviceRefrigerantId);
			cv.put("serviceTagUnitId",	refrigerant.serviceTagUnitId);
			cv.put("transferDate",			refrigerant.transferDate);
			cv.put("techName",		refrigerant.techName);
			cv.put("typeOfRefrigerant",	refrigerant.typeOfRefrigerant);
			cv.put("amount",			refrigerant.amount);
			cv.put("nameOfCylinder",				refrigerant.nameOfCylinder);
			cv.put("cylinderSerialNo",				refrigerant.cylinderSerialNo);
			cv.put("transferedTo",				refrigerant.transferedTo);
			cv.put("modelNo",				refrigerant.modelNo);
			cv.put("serialNo",				refrigerant.serialNo);
			
			db.delete("serviceRefrigerant", "serviceRefrigerantId", refrigerant.serviceRefrigerantId );
			db.db.insertOrThrow("serviceRefrigerant", null, cv);
			cv.clear();
			}
		}
	private void processPMChecklist( List<PMChecklist> list, boolean open )
		{
		PMChecklist pmChecklist;
		int size = list.size();
		ContentValues cv = new ContentValues();
		
		for( int i = 0; i < size; i++ )
			{
			pmChecklist = list.get(i);
			
			if( open )
				{
				pmChecklist.pmChecklistId = pmChecklist.pmChecklistId * -1;
				pmChecklist.serviceTagUnitId = pmChecklist.serviceTagUnitId * -1;
				}
			cv.put("pmChecklistId",		pmChecklist.pmChecklistId);
			cv.put("serviceTagUnitId",	pmChecklist.serviceTagUnitId);
			cv.put("itemText",			pmChecklist.itemText);
			cv.put("itemType",			pmChecklist.itemType);
			cv.put("itemValue",			pmChecklist.itemValue);
			cv.put("itemComment",		pmChecklist.itemComment);
			cv.put("identifier",		pmChecklist.identifier);
			db.db.insertOrThrow("pmChecklist", null, cv);
			cv.clear();
			}
		}
	
	private void processOpenBlue(List<OpenBlue> blueList, List<OpenBlueUnit> unitList)
		{
		int size = blueList.size();
		ContentValues cv = new ContentValues();
		OpenBlue blue;
		OpenBlueUnit unit;
		
		// Loop the Blue Headers
		for( int i = 0; i < size; i++ )
			{
			blue = blueList.get(i);
			
			cv.put("blueId",			blue.blueId);
			cv.put("serviceTagId",		blue.serviceTagId);
			cv.put("dateCreated",		blue.dateCreated);
			if( db.db.insertOrThrow("blue", null, cv) == -1 )
				throw new SQLiteException("Failed to insert Open Blue");
			cv.clear();
			}
		
		// Loop the Blue Units
		size = unitList.size();
		for( int i = 0; i < size; i++ )
			{
			unit = unitList.get(i);
			
			cv.put("blueUnitId",		unit.blueUnitId);
			cv.put("blueId",			unit.blueId);
			cv.put("description",		unit.description);
			cv.put("completed",			unit.completed);
			cv.put("materials",			unit.materials);
			cv.put("notes",				unit.notes);
			cv.put("cost",				unit.cost);
			cv.put("equipmentId",		unit.equipmentId);
			cv.put("laborHours",		unit.laborHours);
			cv.put("tradesmenhrs",		unit.tradesmenhrs);
			cv.put("otherhrs",		unit.otherhrs);
			
			db.db.insertOrThrow("blueUnit", null, cv);
			cv.clear();
			}
		
		}
	
	private void processOpenSafetyChecklist(List<OpenSafetyTagChecklist> headerlist, List<ChecklistItem> itemlist)
		{
		ContentValues cv = new ContentValues();
		OpenSafetyTagChecklist list;
		ChecklistItem item;
		
		int size = headerlist.size();
		for( int i = 0; i < size; i++)
			{
			list = headerlist.get(i);
			
			cv.put("serviceTagId",		list.serviceTagId);
			cv.put("checkListDate",		list.checkListDate);
			cv.put("comments",			list.comments);
			
			if( db.db.insertOrThrow("safetyTagChecklist", null, cv) == -1 )
				throw new SQLiteException("Failed to insert SafetyChecklist");
			cv.clear();
			}
		
		size = itemlist.size();
		for( int i = 0; i < size; i++ )
			{
			item = itemlist.get(i);
			
			cv.put("serviceTagId",		item.serviceTagId);
			cv.put("itemRequired",		item.itemRequired);
			cv.put("itemValue",			item.itemValue);
			cv.put("safetyChecklistId",	item.safetyChecklistId);
			
			db.db.insertOrThrow("safetyTagChecklistItem", null, cv);
			cv.clear();
			}
		
		}
	
	// Process Closed Tags
	private void processClosedTags(List<ServiceTag> tags)
		{
		ContentValues cv = new ContentValues();
		ServiceTag tag;
		
		int size = tags.size();
		
		for( int i = 0; i < size; i++ )
			{
			tag = tags.get(i);
			cv.put("serviceTagId",		tag.serviceTagId);
			cv.put("serviceAddressId",	tag.serviceAddressId);
			cv.put("dispatchId",		tag.dispatchId);
			cv.put("serviceType",		tag.serviceType);
			cv.put("serviceDate",		tag.serviceDate);
			cv.put("billTo",			tag.billTo);
			cv.put("billAddress1",		tag.billAddress1);
			cv.put("billAddress2",		tag.billAddress2);
			cv.put("billAddress3",		tag.billAddress3);
			cv.put("billAddress4",		tag.billAddress4);
			cv.put("billAttn",			tag.billAttn);
			cv.put("tenant",			tag.tenant);
			/*
			 * This data should be attached to the service address. No need to process needless data
			cv.put("siteName",			tag.siteName);
			cv.put("address1",			tag.address1);
			cv.put("address2",			tag.address2);
			cv.put("city",				tag.city);
			cv.put("state",				tag.state);
			cv.put("zip",				tag.zip);
			cv.put("buildingNo",		tag.buildingNo);
			cv.put("note",				tag.note);
			*/
			cv.put("batchNo",			tag.batchNo);
			cv.put("jobNo",				tag.jobNo);
			cv.put("customerPO",		tag.customerPO);
			cv.put("requestedBy",		tag.requestedBy);
			cv.put("requestedByPhone",	tag.requestedByPhone);
			cv.put("requestedByEmail",	tag.requestedByEmail);
			cv.put("description",		tag.description);
			cv.put("empno",				tag.empno);
			cv.put("disposition",		tag.disposition);
			cv.put("description",		tag.dispatchDescription);
			cv.put("xoi_flag",			tag.xoi_flag);
			db.db.insertOrThrow("serviceTag", null, cv);
			cv.clear();
			}
		}
	
	// Process Service Address
	private void processServiceAddress(List<ServiceAddress> list)
		{
		ContentValues cv = new ContentValues();
		int size = list.size();
		ServiceAddress sa;
		for( int i = 0; i < size; i++ )
			{
			sa = list.get(i);
			
			cv.put("serviceAddressId",	sa.serviceAddressId);
			cv.put("siteName",			sa.siteName);
			cv.put("address1",			sa.address1);
			cv.put("address2",			sa.address2);
			cv.put("buildingNo",		sa.buildingNo);
			cv.put("city",				sa.city);
			cv.put("state",				sa.state);
			cv.put("zip",				sa.zip);
			cv.put("note",				sa.note);
			
			db.db.insertOrThrow("serviceAddress", null, cv);
			cv.clear();
			}
		}
	
	private void processServiceAddressPMChecklist(List<ServiceAddress.pmAddressChecklist> pmList)
		{
		ServiceAddress.pmAddressChecklist pmItem;
		ContentValues cv = new ContentValues();
		int size = pmList.size();
		for( int i = 0; i < size; i++ )
			{
			pmItem = pmList.get(i);
			
			cv.put("pmChecklistId",			pmItem.pmChecklistId);
			cv.put("serviceAddressId",		pmItem.serviceAddressId);
			cv.put("equipmentCategoryId",	pmItem.equipmentCategoryId);
			cv.put("itemType",				pmItem.itemType);
			cv.put("itemText",				pmItem.itemText);
			cv.put("identifier", pmItem.identifier);
			
			db.db.insertOrThrow("pmAddressChecklist", null, cv);
			cv.clear();
			}
		}
	
	private void processTenants(List<ServiceAddress.tenant> tenantList)
		{
		ContentValues cv = new ContentValues();
		ServiceAddress.tenant tenant;
		int size = tenantList.size();
		for( int i = 0; i < size; i++ )
			{
			tenant = tenantList.get(i);
			
			cv.put("tenantId",			tenant.tenantId);
			cv.put("serviceAddressId",	tenant.serviceAddressId);
			cv.put("tenant",			tenant.tenant);
			
			db.db.insertOrThrow("serviceAddressTenant", null, cv);
			cv.clear();
			}
		}
	
	private void processContacts(List<ServiceAddressContact> contactList)
		{
		ContentValues cv = new ContentValues();
		int size = contactList.size();
		ServiceAddressContact contact;
		for( int i = 0; i < size; i++ )
			{
			contact = contactList.get(i);
			cv.put("serviceAddressId",	contact.serviceAddressId);
			cv.put("contactId",		contact.contactId);
			
			cv.put("contactName",	contact.contactName);
			cv.put("contactType",	contact.contactType);
			cv.put("email",			contact.email);
			
			cv.put("phone1",		contact.phone1);
			cv.put("phone1Type",	contact.phone1Type);
			cv.put("ext1",			contact.ext1);
			
			cv.put("phone2",		contact.phone2);
			cv.put("phone2Type",	contact.phone2Type);
			cv.put("ext2",			contact.ext2);
			
			db.db.insertOrThrow("serviceAddressContact", null, cv);
			cv.clear();
			}
		}
	private void processNotes(List<ServiceAddressNotes> notesList)
		{
		//String sql = "Delete from notes";
		//Cursor cursor = db.rawQuery(sql);
		db.db.delete("notes", null, null);
		ContentValues cv = new ContentValues();
		int size = notesList.size();
		ServiceAddressNotes notes;
		for( int i = 0; i < size; i++ )
			{
			notes = notesList.get(i);
			cv.put("serviceaddressid",	notes.serviceAddressId);
			cv.put("noteid",		notes.noteId);
			cv.put("notes",			notes.notes);
			
			db.db.insertOrThrow("notes", null, cv);
			cv.clear();
			}
		}
	/*private void processNotes(List<ServiceAddress.notes> notesList)
		{
		ContentValues cv = new ContentValues();
		ServiceAddress.notes notes;
		int size = notesList.size();
		for( int i = 0; i < size; i++ )
			{
			notes = notesList.get(i);
			
			cv.put("noteid",			notes.noteId);
			cv.put("serviceAddressId",	notes.serviceAddressId);
			cv.put("notes",			notes.notes);
			
			db.db.insertOrThrow("notes", null, cv);
			cv.clear();
			}
		}
	*/
	private void processEquipment(List<Equipment> list)
		{
		ContentValues cv = new ContentValues();
		int size = list.size();
		Equipment eq;
		for( int i = 0; i < size; i++ )
			{
			eq = list.get(i);
			
			cv.put("serviceAddressId",	eq.serviceAddressId);
			cv.put("equipmentId",		eq.equipmentId);
			cv.put("unitNo",			eq.unitNo);
			cv.put("equipmentCategoryId",			eq.equipmentCategoryId);
			
			cv.put("areaServed",		eq.areaServed);
			cv.put("barCodeNo",			eq.barCodeNo);
			cv.put("capacity",			eq.capacity);
			cv.put("capacityUnits",		eq.capacityUnits);
			cv.put("dateInService",		eq.dateInService);
			cv.put("dateOutService",	eq.dateOutService);
			cv.put("economizer",		eq.economizer);
			cv.put("manufacturer",		eq.manufacturer);
			cv.put("model",				eq.model);
			cv.put("mfgYear",			eq.mfgYear);
			cv.put("notes",				eq.notes);
			cv.put("productIdentifier",	eq.productIdentifier);
			cv.put("refrigerantType",	eq.refrigerantType);
			cv.put("serialNo",			eq.serialNo);
			cv.put("verifiedByEmpno",	eq.verifiedByEmpno);
			cv.put("voltage",			eq.voltage);
			
			db.db.insertOrThrow("equipment", null, cv);
			cv.clear();
			}
		}
	
	private void processFans(List<Fan> fanlist, List<Sheave> sheavelist)
		{
		ContentValues cv = new ContentValues();
		
		Fan fan;
		int size = fanlist.size();
		for( int i = 0; i < size; i++ )
			{
			fan = fanlist.get(i);
			
			cv.put("fanId",			fan.fanId);
			cv.put("equipmentId",	fan.equipmentId);
			cv.put("number",		fan.number);
			cv.put("partType",		fan.partType);
			if( db.db.insertOrThrow("fan", null, cv) == -1 )
				throw new SQLiteException("Failed to insert Fan");
			cv.clear();
			
			cv.put("fanId",		fan.fanId);
			cv.put("beltSize",	fan.beltSize);
			cv.put("quantity",	fan.beltQty);
			db.db.insertOrThrow("belt", null, cv);
			cv.clear();
			}
		
		Sheave sheave;
		size = sheavelist.size();
		for( int i = 0; i < size; i++ )
			{
			sheave = sheavelist.get(i);
			
			cv.put("fanId",			sheave.fanId);
			cv.put("manufacturer",	sheave.manufacturer);
			cv.put("number",		sheave.number);
			cv.put("type",			sheave.type);
			db.db.insertOrThrow("sheave", null, cv);
			cv.clear();
			}
		
		}
	
	private void processFilters(List<Filter> list)
		{
		ContentValues cv = new ContentValues();
		int size = list.size();
		Filter fil;
		for( int i = 0; i < size; i++ )
			{
			fil = list.get(i);
			
			cv.put("equipmentId",	fil.equipmentId);
			cv.put("type",			fil.type);
			cv.put("quantity",		fil.quantity);
			cv.put("filterSize",	fil.filterSize);
			db.db.insertOrThrow("filter", null, cv);
			cv.clear();
			}
		}
	
	private void processRefCircuits(List<RefCircuit> rlist, List<Compressor> clist)
		{
		ContentValues cv = new ContentValues();
		
		RefCircuit ref;
		int size = rlist.size();
		for( int i = 0; i < size; i++ )
			{
			ref = rlist.get(i);
			
			cv.put("circuitId",		ref.circuitId);
			cv.put("equipmentId",	ref.equipmentId);
			cv.put("circuitNo",		ref.circuitNo);
			cv.put("lbsRefrigerant",ref.lbsRefrigerant);
			db.db.insertOrThrow("refCircuit", null, cv);
			cv.clear();
			}
		
		Compressor comp;
		size = clist.size();
		for( int i = 0; i < size; i++ )
			{
			comp = clist.get(i);
			
			cv.put("circuitId",		comp.circuitId);
			cv.put("compressorNo",	comp.compressorNo);
			cv.put("model",			comp.model);
			cv.put("manufacturer",	comp.manufacturer);
			cv.put("serialNo",		comp.serialNo);
			cv.put("dateInService",	comp.dateInService);
			cv.put("dateOutService",comp.dateOutService);
			db.db.insertOrThrow("compressor", null, cv);
			cv.clear();
			}
			
		}
	
	private void processClosedBlues(List<OpenBlue> closedBlues, List<OpenBlueUnit> closedUnits)
		{
		int size = closedBlues.size();
		ContentValues cv = new ContentValues();
		OpenBlue blue;
		OpenBlueUnit unit;
		
		// Loop the Blue Headers
		for( int i = 0; i < size; i++ )
			{
			blue = closedBlues.get(i);
			
			cv.put("blueId",			blue.blueId);
			cv.put("serviceTagId",		blue.serviceTagId);
			cv.put("dateCreated",		blue.dateCreated);
			if( db.db.insertOrThrow("closedBlue", null, cv) == -1 )
				throw new SQLiteException("Failed to insert Closed Blue");
			cv.clear();
			}
		
		// Loop the Blue Units
		size = closedUnits.size();
		for( int i = 0; i < size; i++ )
			{
			unit = closedUnits.get(i);
			
			cv.put("blueUnitId",		unit.blueUnitId);
			cv.put("blueId",			unit.blueId);
			cv.put("equipmentId",		unit.equipmentId);
			cv.put("description",		unit.description);
			cv.put("materials",			unit.materials);
			cv.put("laborHours",		unit.laborHours);
			cv.put("tradesmenhrs",		unit.tradesmenhrs);
			cv.put("otherhrs",			unit.otherhrs);
			cv.put("notes",				unit.notes);
			cv.put("cost",				unit.cost);
			
			db.db.insertOrThrow("closedBlueUnit", null, cv);
			cv.clear();
			}
		}
	
	// Process Dispatches
	private void processDispatch(List<Dispatch> list)
		{
		ContentValues cv = new ContentValues();
		int size = list.size();
		Dispatch d;
		for( int i = 0; i < size; i++ )
			{
			d = list.get(i);
			
			cv.put("dispatchId",		d.dispatchId);
			cv.put("serviceAddressId",	d.serviceAddressId);
			cv.put("batchNo",			d.batchNo);
			cv.put("jobNo",				d.jobNo);
			
			cv.put("siteName",			d.siteName);
			cv.put("tenant",			d.tenant);
			
			cv.put("altBillTo",			d.altBillTo);
			cv.put("contractType",		d.contractType);
			cv.put("cusNo",				d.cusNo);
			cv.put("customerPO",		d.customerPO);
			cv.put("dateEnded",			d.dateEnded);
			cv.put("dateOrdered",		d.dateOrdered);
			cv.put("dateStarted",		d.dateStarted);
			cv.put("description",		d.description);
			cv.put("mechanic1",			d.mechanic1);
			cv.put("mechanic2",			d.mechanic2);
			cv.put("mechanic3",			d.mechanic3);
			cv.put("mechanic4",			d.mechanic4);
			cv.put("mechanic5",			d.mechanic5);
			cv.put("mechanic6",			d.mechanic6);
			cv.put("mechanic7",			d.mechanic7);
			cv.put("requestedBy",		d.requestedBy);
			cv.put("requestedByPhone",	d.requestedByPhone);
			cv.put("requestedByEmail",	d.requestedByEmail);
			cv.put("siteAddress1",		d.siteAddress1);
			cv.put("siteAddress2",		d.siteAddress2);
			cv.put("siteContact",		d.siteContact);
			cv.put("siteContactPhone",	d.siteContactPhone);
			cv.put("PMComments",		d.PMComments);
			cv.put("PMEstTime",			d.PMEstTime);
			
			db.db.insertOrThrow("dispatch", null, cv);
			cv.clear();
			}
		}
	
	private void processStaticTables(ServerResponse response)
		{
		int size;
		ContentValues cv = new ContentValues();
		
		if( response.pkg.equipmentCategory != null )
			{
			db.db.delete("equipmentCategory", null, null);
			
			Package_Download.EquipmentCategory ec;
			size = response.pkg.equipmentCategory.size();
			for( int i = 0; i < size; i++ )
				{
				ec = response.pkg.equipmentCategory.get(i);
				cv.put("equipmentCategoryId", ec.equipmentCategoryId);
				cv.put("categoryDesc", ec.categoryDesc);
				
				db.db.insertOrThrow("equipmentCategory", null, cv);
				cv.clear();
				}
			}
		
		if( response.pkg.servicelaborrate != null )
			{
			db.db.delete("serviceLaborRate", null, null);
			
			Package_Download.serviceLaborRate slr;
			size = response.pkg.servicelaborrate.size();
			for( int i = 0; i < size; i++ )
				{
				slr = response.pkg.servicelaborrate.get(i);
				cv.put("rateId", slr.rateId);
				cv.put("rate", slr.rate);
				cv.put("rateDesc", slr.rateDesc);
				
				db.db.insertOrThrow("serviceLaborRate", null, cv);
				cv.clear();
				}
			}
		/*if( response.pkg.notes != null )
			{
			db.db.delete("notes", null, null);
			
			Package_Download.Notes nt;
			size = response.pkg.notes.size();
			for( int i = 0; i < size; i++ )
				{
				nt = response.pkg.notes.get(i);
				cv.put("noteid", nt.noteid);
				cv.put("serviceaddressid", nt.serviceaddressid);
				cv.put("notes", nt.notes);
				
				db.db.insertOrThrow("notes", null, cv);
				cv.clear();
				}
			}*/
		/*if( response.pkg.jobdoc != null )
			{
			db.db.delete("JobDoc", null, null);
			
			Package_Download.JobDoc slr;
			size = response.pkg.jobdoc.size();
			for( int i = 0; i < size; i++ )
				{
				slr = response.pkg.jobdoc.get(i);
				cv.put("jobno", slr.jobno);
				cv.put("documentContents", slr.documentContents);
				cv.put("documentName", slr.documentName);
				cv.put("documentTitle", slr.documentTitle);
				cv.put("dataSubmitted", slr.dataSubmitted);
				cv.put("jobsite", slr.jobsite);
				
				db.db.insertOrThrow("jobdoc", null, cv);
				cv.clear();
				}
			}*/
		if( response.pkg.filterSize != null )
			{
			db.db.delete("filterSize", null, null);
			
			Package_Download.FilterSize fil;
			size = response.pkg.filterSize.size();
			for( int i = 0; i < size; i++ )
				{
				fil = response.pkg.filterSize.get(i);
				cv.put("filterSizeId", fil.filterSizeId);
				cv.put("filterSize", fil.filterSize);
				
				db.db.insertOrThrow("filterSize", null, cv);
				cv.clear();
				}
			}
		
		if( response.pkg.filterType != null )
			{
			db.db.delete("filterType", null, null);
			
			Package_Download.FilterType filType;
			size = response.pkg.filterType.size();
			for( int i = 0; i < size; i++ )
				{
				filType = response.pkg.filterType.get(i);
				cv.put("filterTypeId", filType.filterTypeId);
				cv.put("filterType", filType.filterType);
				
				db.db.insertOrThrow("filterType", null, cv);
				cv.clear();
				}
			}
		
		if( response.pkg.mechanic != null )
			{
			db.db.delete("mechanic", null, null);
			
			Package_Download.Mechanic mech;
			size = response.pkg.mechanic.size();
			for( int i = 0; i < size; i++ )
				{
				mech = response.pkg.mechanic.get(i);
				cv.put("mechanic", mech.empno);
				cv.put("mechanic_name", mech.name);
				cv.put("terminated", mech.terminated);
				cv.put("dept", mech.dept);
				
				db.db.insertOrThrow("mechanic", null, cv);
				cv.clear();
				}
			}
		
		
		if( response.pkg.pmStdChecklist != null )
			{
			db.db.delete("pmStdChecklist", null, null);
			
			Package_Download.pmStdChecklist pmlist;
			size = response.pkg.pmStdChecklist.size();
			for( int i = 0; i < size; i++ )
				{
				pmlist = response.pkg.pmStdChecklist.get(i);
				cv.put("pmChecklistId", pmlist.pmChecklistId);
				cv.put("equipmentCategoryId", pmlist.equipmentCategoryId);
				cv.put("itemType", pmlist.itemType);
				cv.put("itemText", pmlist.itemText);
				cv.put("identifier",pmlist.identifier);
				db.db.insertOrThrow("pmStdChecklist", null, cv);
				cv.clear();
				}
			}
		
		
		if( response.pkg.refrigerantType != null )
			{
			db.db.delete("refrigerantType", null, null);
			
			Package_Download.RefrigerantType ref;
			size = response.pkg.refrigerantType.size();
			for( int i = 0; i < size; i++ )
				{
				ref = response.pkg.refrigerantType.get(i);
				cv.put("refrigerantTypeId", ref.refrigerantTypeId);
				cv.put("refrigerantType", ref.refrigerantType);
				
				db.db.insertOrThrow("refrigerantType", null, cv);
				cv.clear();
				}
			}
		
		
		if( response.pkg.safetyChecklist != null )
			{
			db.db.delete("safetyChecklist", null, null);
			
			Package_Download.SafetyChecklist clist;
			size = response.pkg.safetyChecklist.size();
			for( int i = 0; i < size; i++ )
				{
				clist = response.pkg.safetyChecklist.get(i);
				cv.put("safetyChecklistId", clist.safetyChecklistId);
				cv.put("sortOrder", clist.sortOrder);
				cv.put("LOTO", clist.LOTO);
				cv.put("itemType", clist.itemType);
				cv.put("itemText", clist.itemText);
				cv.put("itemTextBold", clist.itemTextBold);
				
				db.db.insertOrThrow("safetyChecklist", null, cv);
				cv.clear();
				}
			}
		
		if( response.pkg.serviceDescription != null )
			{
			db.db.delete("serviceDescription", null, null);
			
			Package_Download.ServiceDescription desc;
			size = response.pkg.serviceDescription.size();
			for( int i = 0; i < size; i++ )
				{
				desc = response.pkg.serviceDescription.get(i);
				cv.put("descriptionID", desc.descriptionID);
				cv.put("description", desc.description);
				
				db.db.insertOrThrow("serviceDescription", null, cv);
				cv.clear();
				}
			}
		
		}
	
		// Process Dispatch Billing
	private void processBilling(List<Billing> billing)
		{
		Billing bill;
		ContentValues cv = new ContentValues();
		int size = billing.size();
		for( int i = 0; i < size; i++ )
			{
			bill = billing.get(i);
			cv.put("CustomerID",	bill.CustomerId);
			cv.put("Name",			bill.Name);
			cv.put("AltBillId",		bill.altBillTo);
			cv.put("Address1",		bill.Address1);
			cv.put("Address2",		bill.Address2);
			cv.put("Address3",		bill.Address3);
			cv.put("Address4",		bill.Address4);
			
			db.db.insertOrThrow("billing", null, cv);
			cv.clear();
			}
		}
	
		// Process Dispatch Priority
	private void processDispatchPriority( List<DispatchPriority> dispatchPriority )
		{
		int size = dispatchPriority.size();
		ContentValues cv = new ContentValues();
		DispatchPriority dp;
		for( int i = 0; i < size; i++ )
			{
			dp = dispatchPriority.get(i);
			
			// Insert
			if( dp.DaysLate >= 0 )
				{
				cv.put("PriorityId",	dp.PriorityId);
				cv.put("RGBColor",		dp.RGBColor);
				cv.put("DaysLate",		dp.DaysLate);
				cv.put("DateChanged",	dp.DateChanged);
				
				//if( db.db.insertOrThrow("dispatchPriority", null, cv) == -1 )
				db.db.insertWithOnConflict( "dispatchPriority", null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
				cv.clear();
				}
			else
				{
				db.delete("dispatchPriority", "priorityId", dp.PriorityId);
				}
			}
		}
	
	// Resolve old Photo and Receipt Ids
	private void processMaps(List<Pair> serviceTagId_map, List<Pair> serviceTagUnitId_map )
		{
		Pair pair;
		String sql;
		int size;
		
		if( serviceTagId_map != null )
			{
			size = serviceTagId_map.size();
			if( size > 0 )
				{
				sql = "UPDATE serviceReceipt SET serviceTagId = CASE serviceTagId ";
				for( int i = 0; i < size; i++ )
					{
					pair = serviceTagId_map.get(i);
					sql += "WHEN " + pair.id + " THEN " + pair.newid + " ";
					}
				sql += "END WHERE serviceTagId IN ( ";
				for( int i = 0; i < size; i++ )
					{
					pair = serviceTagId_map.get(i);
					sql += pair.id;
					if( i < size-1)
						sql += ", ";
					}
				sql += " )";
				db.db.execSQL(sql);
				}
			}
		
		if( serviceTagUnitId_map != null )
			{
			size = serviceTagUnitId_map.size();
			if( size > 0 )
				{
				sql = "UPDATE servicePhoto SET serviceTagUnitId = CASE serviceTagUnitId ";
				for( int i = 0; i < size; i++ )
					{
					pair = serviceTagUnitId_map.get(i);
					sql += "WHEN " + pair.id + " THEN -" + pair.newid + " ";
					}
				sql += "END WHERE serviceTagUnitId IN ( ";
				for( int i = 0; i < size; i++ )
					{
					pair = serviceTagUnitId_map.get(i);
					sql += pair.id;
					if( i < size-1)
						sql += ", ";
					}
				sql += " )";
				db.db.execSQL(sql);
				}
			}
		
		}
	
	/****************************
	 * Form Processing Functions
	 ****************************/
	
	private void ProcessFormPackage(FormPackage FormPkg, FormDataPackage FormDataPkg, boolean ReInit)
		{
		if( FormPkg == null || FormDataPkg == null)
			return;
		
		if( ReInit )
			{
			db.db.delete("Form", null, null);
			db.db.delete("FormSection", null, null);
			db.db.delete("FormSecXRef", null, null);
			db.db.delete("FormMatrix", null, null);
			db.db.delete("FormMatrixXRef", null, null);
			db.db.delete("FormOptions", null, null);
			db.db.delete("FormSecHeights", null, null);
			db.db.delete("FormSecWidths", null, null);
			db.db.delete("FormType", null, null);
			}
		
		try
			{
			ProcessArrayList("Form", FormPkg.Form);
			ProcessArrayList("FormSection", FormPkg.FormSection);
			ProcessArrayList("FormSecXRef", FormPkg.FormSecXRef);
			ProcessArrayList("FormMatrix", FormPkg.FormMatrix);
			ProcessArrayList("FormMatrixXRef", FormPkg.FormMatrixXRef);
			ProcessArrayList("FormOptions", FormPkg.FormOptions);
			ProcessArrayList("FormSecHeights", FormPkg.FormSecHeights);
			ProcessArrayList("FormSecWidths", FormPkg.FormSecWidths);
			//db.db.delete("FormType", null, null);
			ProcessArrayList("FormType", FormPkg.FormType);
			db.db.delete("FormData", null, null);
			ProcessArrayList("FormData", FormDataPkg.FormData);
			db.db.delete("FormDataValues", null, null);
			ProcessArrayList("FormDataValues", FormDataPkg.FormDataValues);
			
			ResolveBigIntIds("FormPhotos", "FormDataId", FormDataPkg.ResolvedFormDataIds, null, null);
			ResolveBigIntIds("FormDataSignatures", "FormDataId", FormDataPkg.ResolvedFormDataIds, null, null);
			}
		catch ( IllegalArgumentException e )
			{
			e.printStackTrace();
			}
		catch ( IllegalAccessException e )
			{
			e.printStackTrace();
			}
		
		}
	
	private void ProcessAttributeStructure(AttributePackage AttrPkg, boolean ReInit)
		{
		if( AttrPkg == null )
			return;
		
		if( ReInit )
			{
			db.db.delete("AttrDef", null, null);
			db.db.delete("AttrXRef", null, null);
			}
		
		try
			{
			ProcessArrayList("AttrDef", AttrPkg.AttrDef);
			ProcessArrayList("AttrXRef", AttrPkg.AttrXRef);
			if( AttrPkg.CategoryAttrLink.size() > 0 )
				{
				db.db.delete("CategoryAttrLink", null, null);
				ProcessArrayList("CategoryAttrLink", AttrPkg.CategoryAttrLink);
				}
			}
		catch ( IllegalArgumentException e )
			{
			e.printStackTrace();
			}
		catch ( IllegalAccessException e )
			{
			e.printStackTrace();
			}
		}
	
	private void ProcessArrayList(String tableName, List<Object> list) throws IllegalArgumentException, IllegalAccessException
		{
		int size = list.size();
		if( size > 0 )
			{
			ContentValues cv = new ContentValues();
			Object o;
			Object attr;
			o = list.get(0);
			Field[] fields = o.getClass().getFields();
			for( int i = 0; i < size; i++ )
				{
				o = list.get(i);
				for( int j = 0; j < fields.length; j++ )
					{
					attr = fields[j].get(o);
					if( attr instanceof Integer )
						cv.put(fields[j].getName(), (Integer) attr);
					
					else if( attr instanceof Long )
						cv.put(fields[j].getName(), (Long) attr);
					
					else if( attr instanceof String )
						cv.put(fields[j].getName(), (String) attr);
					
					else if( attr instanceof Float )
						cv.put(fields[j].getName(), (Float) attr);
					
					else if( attr instanceof Double )
						cv.put(fields[j].getName(), (Double) attr);
					else if( attr instanceof byte[] )
						cv.put(fields[j].getName(), (byte[]) attr);
					}
				
				db.db.insertWithOnConflict(tableName, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
				cv.clear();
				}
			}
		
		}
	
	private void GetAndBuildFormData()
		{
		request.formDataPkg = new FormDataPackage();
		String MEID = app.getDeviceId();
		
		String sql = "SELECT fd.FormDataId, fd.FormId, fd.ParentTable, fd.ParentId, fd.LinkTable, fd.LinkId, " +
							"fd.InputByEmpno, fd.Completed, fd.DateEntered, " +
							"st.completed " +
				"FROM FormData as fd " +
				"LEFT OUTER JOIN serviceTagUnit as su " +
					"ON su.serviceTagUnitId = fd.ParentId AND fd.ParentTable = 'ServiceTagUnit' " +
				"LEFT OUTER JOIN openServiceTag as st " +
					"ON st.serviceTagId = su.serviceTagId";
		Cursor cursor = db.rawQuery(sql);
		FormDataPackage.FormData formData;
		FormDataPackage.FormDataValues formDataValues;
		int index;
		String Completed;
		while( cursor.moveToNext() )
			{
			index = 0;
			formData = new FormDataPackage.FormData();
			formData.FormDataId = cursor.getLong(0);
			formData.FormId = cursor.getInt(1);
			formData.ParentTable = cursor.getString(2);
			formData.ParentId = cursor.getInt(3);
			formData.LinkTable = cursor.getString(4);
			formData.LinkId = cursor.getInt(5);
			formData.InputByEmpno = cursor.getString(6);
			formData.Completed = cursor.getString(7);
			formData.DateEntered = cursor.getString(8);
			formData.tabletMEID = MEID;
			if( formData.ParentTable.contentEquals("ServiceTagUnit") )
				{
				Completed = Twix_TextFunctions.clean(cursor.getString(9));
				if( Completed.contentEquals("Y") )
					formData.Completed = "Y";
				}
			else
				if( formData.Completed.contentEquals("M") )
					formData.Completed = "Y";
			
			request.formDataPkg.FormData.add(formData);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		
		sql = "SELECT FormDataId, XRefId, MatrixTrail, Value " +
				"FROM FormDataValues WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE Completed != 'Y' )";
		cursor = db.rawQuery(sql);
		while( cursor.moveToNext() )
			{
			index = 0;
			formDataValues = new FormDataPackage.FormDataValues();
			formDataValues.FormDataId = cursor.getLong(0);
			formDataValues.XRefId = cursor.getLong(1);
			formDataValues.MatrixTrail = cursor.getString(2);
			formDataValues.Value = cursor.getString(3);
			
			request.formDataPkg.FormDataValues.add(formDataValues);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		FormDataPackage.FormDataSignatures formDataSignatures;
		sql = "SELECT fs.FormDataId, fs.XRefId, fs.MatrixTrail, fs.Value " +
				"FROM FormDataSignatures as fs " +
				"INNER JOIN FormData as fd " +
					"ON fs.FormDataId = fd.FormDataId " +
				"LEFT OUTER JOIN serviceTagUnit as su " +
					"ON su.serviceTagUnitId = fd.ParentId AND fd.ParentTable = 'ServiceTagUnit' " +
				"LEFT OUTER JOIN openServiceTag as st " +
					"ON st.serviceTagId = su.serviceTagId " + 
			"WHERE st.Completed = 'Y' OR (st.Completed IS NULL AND fd.Completed = 'M')";//COALESCE(st.Completed, fd.Completed) = 'M'";
		cursor = db.rawQuery(sql);
		while( cursor.moveToNext() )
			{
			index = 0;
			formDataSignatures = new FormDataPackage.FormDataSignatures();
			formDataSignatures.FormDataId = cursor.getLong(0);
			formDataSignatures.XRefId = cursor.getLong(1);
			formDataSignatures.MatrixTrail = cursor.getString(2);
			formDataSignatures.Value = cursor.getBlob(3);
			
			request.formDataPkg.FormDataSignatures.add(formDataSignatures);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		FormDataPackage.FormPhotos formPhotos;
		sql = "SELECT fp.FormPhotoId, fp.FormDataId, fp.Photo, fp.DateCreated, fp.Comments " +
				"FROM FormPhotos as fp " +
					"INNER JOIN FormData as fd " +
						"ON fp.FormDataId = fd.FormDataId " +
					"LEFT OUTER JOIN serviceTagUnit as su " +
						"ON su.serviceTagUnitId = fd.ParentId AND fd.ParentTable = 'ServiceTagUnit' " +
					"LEFT OUTER JOIN openServiceTag as st " +
						"ON st.serviceTagId = su.serviceTagId " + 
			"WHERE st.Completed = 'Y' OR (st.Completed IS NULL AND fd.Completed = 'M')";//COALESCE(st.Completed, fd.Completed) = 'M'";
		cursor = db.rawQuery(sql);
		while( cursor.moveToNext() )
			{
			index = 0;
			formPhotos = new FormDataPackage.FormPhotos();
			formPhotos.FormPhotoId = cursor.getLong(0);
			formPhotos.FormDataId = cursor.getLong(1);
			formPhotos.Photo = cursor.getBlob(2);
			formPhotos.DateCreated = cursor.getString(3);
			formPhotos.Comments = cursor.getString(4);
			
			request.formDataPkg.FormPhotos.add(formPhotos);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	/**
	 * Loops sending and receiving a response to and from the server. A keep alive packet (empty) is sent to the server while
	 * 	it processes, and when processing is complete it sends the response with the ServerResponse class and the keep alive
	 * 	session ends.
	 * 
	 * @param oos - The socket Object Output Stream
	 * @param ois - The socket Object Input Stream
	 * 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws OptionalDataException 
	 * 
	 */
	private ServerResponse keepAliveLooper(ObjectOutputStream oos, ObjectInputStream ois) throws OptionalDataException, ClassNotFoundException, IOException
		{
		int counter = 0;
		Object curResponse;
		curResponse = ois.readObject();
		while( curResponse instanceof KeepAlivePacket )
			{
			counter++;
			oos.writeObject( new KeepAlivePacket() );
			curResponse = ois.readObject();
			}
		Log.i("twix_agent:Twix_Client", "Keep Alive Iterations: " + counter);
		return (ServerResponse) curResponse;
		}
	
	private void ResolveIntIds(String TableName, String ColumnName, Map<Integer,Integer> ResolvedMap,
			String additionalWhere, String additionalClause)
		{
		ContentValues cv = new ContentValues();
		Entry<Integer,Integer> entry;
		String whereclause = ColumnName+"=?";
		if( additionalWhere != null && additionalClause != null)
			whereclause += " AND " + additionalWhere + "=" + additionalClause;
		for( Iterator<Entry<Integer,Integer>> i = ResolvedMap.entrySet().iterator(); i.hasNext(); )
			{
			entry = i.next();
			cv.put(ColumnName, entry.getValue());
			app.db.db.update(TableName, cv, whereclause, new String[] {entry.getKey()+""} );
			cv.clear();
			}
		}
	
	private void ResolveBigIntIds(String TableName, String ColumnName, Map<Long,Long> ResolvedMap,
			String additionalWhere, String additionalClause)
		{
		ContentValues cv = new ContentValues();
		Entry<Long,Long> entry;
		String whereclause = ColumnName+"=?";
		if( additionalWhere != null && additionalClause != null)
			whereclause += " AND " + additionalWhere + "=" + additionalClause;
		for( Iterator<Entry<Long,Long>> i = ResolvedMap.entrySet().iterator(); i.hasNext(); )
			{
			entry = i.next();
			cv.put(ColumnName, entry.getValue());
			app.db.db.update(TableName, cv, whereclause, new String[] {entry.getKey()+""} );
			cv.clear();
			}
		}
	}
