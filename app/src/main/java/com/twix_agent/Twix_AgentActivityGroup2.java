package com.twix_agent;

import java.sql.Date;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;


/*******************************************************************************************************************
 * Class: Twix_AgentActivityGroup2
 * 
 * Purpose: Hosts the Open Tags tab's activities. Paging functionality for the tab is controlled by this class.
 * 			This class also drives many of the onClick listeners from XML files. This activity group is used in
 * 			underlying activities as a primary context to launch new intents. Often referred to as mContext in
 * 			child activities.
 * 
 * Relevant XML: see Twix_AgentTab2
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentActivityGroup2 extends Twix_TabActivityGroup
	{
	private Twix_Application app;
	private static final int	DIALOG_DATE_ID	= 0;
	private Twix_SQLite db;
	private LocalActivityManager manager;
	
	private View dateView; 
	public static final int PHOTO = 4;
	public static final int TAKE_PIC = 0;
	public static final int TAKE_RECEIPT = 1;
	public static final int SCAN_BARCODE_READ = 2;
	public static final int SCAN_BARCODE_BLUE = 3;
	
	public boolean ButtonPressed = false;
	
	/**
     * Initializes all the pointers from Twix_Application
     */
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        startChildActivity("Twix_AgentTab2", new Intent(this,Twix_AgentTab2.class));
        
        app = (Twix_Application) getApplication();
        db = app.db;
        manager = getLocalActivityManager();
    	}
	
	/**
	 * Used by Service Unit pages to prevent the user from deleting their changes without saving.
	 * 
	 * Pops up a menu so the user can choose if they want to save or not.
	 */
	@Override
	public boolean beforeBack()
		{
		Twix_AgentOpenTagsTabHost host = ((Twix_AgentOpenTagsTabHost) manager.getActivity("Twix_AgentOpenTagsTabHost"));
		Twix_AgentServiceUnitTabHost unithost = ((Twix_AgentServiceUnitTabHost) manager.getActivity("Twix_AgentServiceUnitTabHost"));
		if( host != null  && unithost != null)
			{
			if( unithost.dirtyFlag )
				{
			    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    builder.setMessage("Are you sure you want to cancel all changes?")
			           .setCancelable(false)
			           .setPositiveButton("Yes", new DialogInterface.OnClickListener()
			        	   {
			               public void onClick(DialogInterface dialog, int id)
			            	   {
			    	           Twix_AgentActivityGroup2.this.finishActivity();
			    	           }
			    	       })
			           .setNegativeButton("No", new DialogInterface.OnClickListener()
			        	   {
			               public void onClick(DialogInterface dialog, int id)
			            	   {
			                   dialog.cancel();
			                   ButtonPressed = false;
			                   app.MainTabs.setTabState(!ButtonPressed);
			    	           }
			    	       });
			    
			    AlertDialog alert = builder.create();
			    alert.show();
			    
			    return true;
				}
			}
    	return false;
		}
	
	@Override
	protected void backPressed()
		{
		if( !ButtonPressed || app.MainTabs.getCurrentActivity() != this )
			{
			ButtonPressed = true;
			app.MainTabs.setTabState(true);
			super.backPressed();
			//ButtonPressed = false;
			}
		}
	
	@Override
	public boolean finishActivity()
		{
		ButtonPressed = false;
		app.MainTabs.setTabState(true);
		return super.finishActivity();
		}
	
	// Open Tags Buttons
	/**
	 * Button function that creates a new open tag
	 * 
	 * Source: Twix_AgentTab2
	 * 
	 * @param v
	 */
	public void newOpenTag()
		{
		newOpenTag(0, 0);
    	}
	
	/**
	 * Creates a new open tag with the provided dispatchId and serviceAddressId. This is used as a
	 *  convenience function to create an unlinked open tag, or linked to a dispatch and/or service address
	 * 
	 * @param dispatchId- dispatch id as a string
	 * @param serviceAddressId - service address id as a string
	 */
	public void newOpenTag( int dispatchId, int serviceAddressId )
		{
		ContentValues cv = new ContentValues();
		int newId = db.newNegativeId("openServiceTag", "serviceTagId");
		
		cv.put("serviceTagId",		newId);
		cv.put("dispatchId",		dispatchId);
		cv.put("serviceAddressId",	serviceAddressId);
		cv.put("empno",				app.empno);
		cv.put("serviceDate",		Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT));
		cv.put("completed",			"N");
		
    	db.db.insertOrThrow("openServiceTag", null, cv );
    	
    	Intent intent = new Intent(getParent(), Twix_AgentOpenTagsTabHost.class);
    	intent.putExtra("serviceTagId", newId);
        startChildActivity("Twix_AgentOpenTagsTabHost", intent);
    	}
	
	// Open Tags -> Blue Units
	/**
	 * Button function that adds a new blue to a service tag
	 * 
	 * Source: Twix_Agentblue
	 * 
	 * @param v
	 */
	public void addBlue(View v)
		{
		LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
	   	Twix_AgentBlue act = ((Twix_AgentBlue)manager2.getActivity("Blue"));
		act.addBlue();
		}
	
	// Open Tags -> Signature
	/**
	 * Button function that creates the customer signature pop up on the signature and summary page
	 * 
	 * Twix_AgentSignature
	 * 
	 * @param v
	 */
	public void CustomerSignature(View v)
		{
		LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		Twix_AgentSignature act = ((Twix_AgentSignature)manager2.getActivity("Signature"));
		act.sigPopup();
		}
	
	// Open Tags -> Receipts
	/**
	 * Button function that takes a photo for a receipt
	 * 
	 * Source: Twix_AgentOpenTagReceipt
	 * 
	 * @param v
	 */
	public void takeReceipt(View v)
		{
		if( !ButtonPressed || app.MainTabs.getCurrentActivity() != this )
			{
			ButtonPressed = true;
			app.MainTabs.setTabState(!ButtonPressed);
			
			TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentOpenTagsTabHost"));
			if( tabhost != null )
				{
				LocalActivityManager manager2 = tabhost.getLocalActivityManager();
				Twix_AgentOpenTagReceipt act = ((Twix_AgentOpenTagReceipt)manager2.getActivity("Receipt"));
				if( act != null )
					act.takePhoto();
				else
					{
					Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					ButtonPressed = false;
					app.MainTabs.setTabState(!ButtonPressed);
					}
				}
			else
				{
				Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
				ButtonPressed = false;
				app.MainTabs.setTabState(!ButtonPressed);
				}
			}
		}
	
			// Dispatch
	// ServiceUnit -> Material Buttons
	public void addMaterial(View v)
		{
		LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost")).getLocalActivityManager();
    	Twix_AgentServiceUnitMaterial act = ((Twix_AgentServiceUnitMaterial)manager2.getActivity("Material"));
		act.addMaterial(null);
		}
	
	// ServiceUnit -> Labor Buttons
	public void addLabor(View v)
		{
		LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost")).getLocalActivityManager();
    	Twix_AgentServiceUnitLabor act = ((Twix_AgentServiceUnitLabor)manager2.getActivity("Labor"));
		act.addLabor(null);
		}
	
	// ServiceUnit -> Unit Photos
	public void takePhoto(View v)
		{
		if( !ButtonPressed || app.MainTabs.getCurrentActivity() != this )
			{
			ButtonPressed = true;
			app.MainTabs.setTabState(!ButtonPressed);
			TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost"));
			if( tabhost != null )
				{
				LocalActivityManager manager2 = tabhost.getLocalActivityManager();
				Twix_AgentServiceUnitPhoto act = ((Twix_AgentServiceUnitPhoto)manager2.getActivity("Photo"));
				if( act != null )
					act.takePhoto();
				else
					{
					Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					ButtonPressed = false;
					app.MainTabs.setTabState(!ButtonPressed);
					}
				}
			else
				{
				Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
				ButtonPressed = false;
				app.MainTabs.setTabState(!ButtonPressed);
				}
			}
		}
	public void takePhoto2(View v)
		{
		if( !ButtonPressed || app.MainTabs.getCurrentActivity() != this )
			{
			ButtonPressed = true;
			app.MainTabs.setTabState(!ButtonPressed);
			TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost"));
			if( tabhost != null )
				{
				LocalActivityManager manager2 = tabhost.getLocalActivityManager();
				Twix_AgentServiceUnitPhoto act = ((Twix_AgentServiceUnitPhoto)manager2.getActivity("Photo"));
				if( act != null )
					act.takePhoto2();
				else
					{
					Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					ButtonPressed = false;
					app.MainTabs.setTabState(!ButtonPressed);
					}
				}
			else
				{
				Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
				ButtonPressed = false;
				app.MainTabs.setTabState(!ButtonPressed);
				}
			}
		}
	
	// Date Dialog for Service Labor
	public void changeDate(View v)
		{
		dateView = v;
		String s = ((TextView)v).getText().toString();
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH),
			month = c.get(Calendar.MONTH)+1,
			year = c.get(Calendar.YEAR);
		
		if( s.matches("^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}$") )//s.length() > 9 )
			{
			year = Integer.parseInt(s.substring( 0, 4 ));
			month = Integer.parseInt(s.substring( 5, 7 ));
			day = Integer.parseInt(s.substring( 8, 10 ));
			}
		
		Bundle b = new Bundle();
		b.putInt("day", day);
		b.putInt("month", month-1);
		b.putInt("year", year);
			
		showDialog(DIALOG_DATE_ID, b);
		}
	// The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
    		new DatePickerDialog.OnDateSetListener()
    	{
    	public void onDateSet(DatePicker view, int Year, int monthOfYear, int dayOfMonth)
    		{
    		String day = "", month = "", year = "";
    		if( dayOfMonth < 10 )
    			day = "0";
    		day += dayOfMonth;
    		
    		if( monthOfYear+1 < 10 )
    			month = "0";
    		month += (monthOfYear+1);
    		
    		year += Year;
    		
    		((TextView)dateView).setText( year + "-" + month + "-" + day );
    		removeDialog(DIALOG_DATE_ID);
    		}
    	};
    /*
	public void changeDate(View v)
		{
		dateView = v;
		String s = ((TextView)v).getText().toString();
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH),
			month = c.get(Calendar.MONTH)+1,
			year = c.get(Calendar.YEAR);
		
		if( s.matches("^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}$") )//s.length() > 9 )
			{
			year = Integer.parseInt(s.substring( 0, 4 ));
			month = Integer.parseInt(s.substring( 5, 7 ));
			day = Integer.parseInt(s.substring( 8, 10 ));
			}
		
		Bundle b = new Bundle();
		b.putInt("day", day);
		b.putInt("month", month-1);
		b.putInt("year", year);
			
		showDialog(DIALOG_DATE_ID, b);
		}
	// The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
    		new DatePickerDialog.OnDateSetListener()
    	{
    	public void onDateSet(DatePicker view, int Year, int monthOfYear, int dayOfMonth)
    		{
    		String day = "", month = "", year = "";
    		if( dayOfMonth < 10 )
    			day = "0";
    		day += dayOfMonth;
    		
    		if( monthOfYear+1 < 10 )
    			month = "0";
    		month += (monthOfYear+1);
    		
    		year += Year;
    		Calendar cal = Calendar.getInstance(); 
      		 int dayc = cal.get(Calendar.DAY_OF_MONTH);
      		  int monthc = cal.get(Calendar.MONTH)+1;
      		  int yearc = cal.get(Calendar.YEAR);
    		if(( Integer.parseInt(year) > yearc ))
    			{
    			AlertDialog alertDialog = new AlertDialog.Builder(Twix_AgentActivityGroup2.this).create();
					alertDialog.setTitle("invalid Year");
					alertDialog.setMessage("Date cannot be greater than today");
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
    			}});alertDialog.show();}
					else if( Integer.parseInt(year) >= yearc  && (Integer.parseInt(month) > monthc) )
						{
						AlertDialog alertDialog = new AlertDialog.Builder(Twix_AgentActivityGroup2.this).create();
						alertDialog.setTitle("Invalid month");
						alertDialog.setMessage("Date cannot be greater than today");
						alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
						new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
	    			}});	alertDialog.show();	
						}
					else if ( Integer.parseInt(year) >= yearc  && (Integer.parseInt(month) >= monthc) && (Integer.parseInt(day) >dayc))
					{
					
					AlertDialog alertDialog = new AlertDialog.Builder(Twix_AgentActivityGroup2.this).create();
					alertDialog.setTitle("Invalid date");
					alertDialog.setMessage("Date cannot be greater than today");
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					}});	alertDialog.show();}
					else
						{
    		//String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    		//((TextView)dateView).setText( mydate );
    		//removeDialog(DIALOG_DATE_ID);
      		((TextView)dateView).setText( year + "-" + month + "-" + day );
	    		removeDialog(DIALOG_DATE_ID);
						}
    		
    	}};
    		
    
    */
    @Override
    protected Dialog onCreateDialog(int id, Bundle b)
    	{
    	switch (id)
    		{
    		case DIALOG_DATE_ID:
        		int month = 0, day = 0, year = 0;
        		month = b.getInt("month");
        		day = b.getInt("day");
        		year = b.getInt("year");
        		if( month == 0 && year == 0 && day == 0 )
        			{
        			Calendar c = Calendar.getInstance();
        			c.setTimeInMillis(System.currentTimeMillis());
        		    day = c.get(Calendar.DAY_OF_MONTH);
        	        month = c.get(Calendar.MONTH);
        	        year = c.get(Calendar.YEAR);
        			}
    			return new DatePickerDialog(this, mDateSetListener, year, month, day);
    			
    		}
    	return null;
    	}
	// End Dialog for Service Labor
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
		
	
		
		if (requestCode == TAKE_PIC)
			{
			if (resultCode == RESULT_OK)
				{
				TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost"));
				
				if( tabhost != null )
					{
					LocalActivityManager manager2 = tabhost.getLocalActivityManager();
					Twix_AgentServiceUnitPhoto act = ((Twix_AgentServiceUnitPhoto)manager2.getActivity("Photo"));
					if( act != null )
						act.photoListAdd();
					else
						Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					}
				else
					Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
				}
			else if (resultCode == RESULT_CANCELED)
				Toast.makeText(this, "Picture was Cancelled", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
			}
		
		else if (requestCode == TAKE_RECEIPT)
			{
			
			if (resultCode == RESULT_OK)
				{
				TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentOpenTagsTabHost"));
				if( tabhost != null )
					{
					LocalActivityManager manager2 = tabhost.getLocalActivityManager();
					Twix_AgentOpenTagReceipt act = ((Twix_AgentOpenTagReceipt)manager2.getActivity("Receipt"));
					if( act != null )
						act.photoListAdd();
					else
						Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					}
				else
					Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
				}
			else if (resultCode == RESULT_CANCELED)
				Toast.makeText(this, "Picture was Cancelled", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
			
			}
		else if( requestCode == SCAN_BARCODE_READ )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost")).getLocalActivityManager();
    			Twix_AgentServiceTagUnit act = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit"));
    			act.scanResult( data.getStringExtra("la.droid.qr.result") );
    			}
    		else if ( resultCode == RESULT_CANCELED )
    			Toast.makeText(this, "Scanner Cancelled", Toast.LENGTH_SHORT).show();
    		
    		}
		else if( requestCode == SCAN_BARCODE_BLUE )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
    			Twix_AgentBlue act = ((Twix_AgentBlue)manager2.getActivity("Blue"));
    			act.scanResult( data.getStringExtra("la.droid.qr.result") );
    			}
    		else if ( resultCode == RESULT_CANCELED )
    			Toast.makeText(this, "Scanner Cancelled", Toast.LENGTH_SHORT).show();
    		}
		else if( requestCode == 100 )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			FragmentManager fm = this.getFragmentManager();
    			Fragment f = fm.findFragmentById(0);
    			if( f != null )
    				((Twix_AgentFormDisplay)f).onActivityResult(requestCode, resultCode, data);
    			}
    		else if ( resultCode == RESULT_CANCELED )
    			Toast.makeText(this, "Scanner Cancelled", Toast.LENGTH_SHORT).show();
    		}
		else if( requestCode == PHOTO )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			TabActivity tabhost = ((TabActivity) manager.getActivity("Twix_AgentServiceUnitTabHost"));
				
				if( tabhost != null )
					{
					 String curFileName = data.getStringExtra("GetFileName");
					  Toast.makeText(this, "File Name is "+curFileName, Toast.LENGTH_SHORT).show();
					LocalActivityManager manager2 = tabhost.getLocalActivityManager();
					Twix_AgentServiceUnitPhoto act = ((Twix_AgentServiceUnitPhoto)manager2.getActivity("Photo"));
					if( act != null )
					act.getpic(curFileName);
						//Toast.makeText(this, "Fetch the File and convert to picture", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(this, "Error 2: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
					}
				else
					Toast.makeText(this, "Error 1: Twix lost connection with the camera.", Toast.LENGTH_SHORT).show();
    		}}
		else
			
			super.onActivityResult(requestCode, resultCode, data);
		
		ButtonPressed = false;
		app.MainTabs.setTabState(!ButtonPressed);
		}
	
    @Override
    public void onResume()
    	{
    	super.onResume();
    	ButtonPressed = false;
    	}
    
    @Override
    public void onPause()
    	{
    	super.onPause();
    	ButtonPressed = true;
    	}
    	}
