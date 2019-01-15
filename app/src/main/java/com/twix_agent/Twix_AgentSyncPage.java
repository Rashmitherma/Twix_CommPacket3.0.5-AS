package com.twix_agent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentSyncPage
 * 
 * Purpose: Provides the user information for their next sync. This activity provides functions to sync, change users,
 * 			and exit the application entirely. Separate functions are called to list off each of the changes to be
 * 			made on sync.
 * 
 * Relevant XML: sync_page.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSyncPage extends Activity
	{
	private Twix_Application			app;
	private LinearLayout				ll;
	private LinearLayout.LayoutParams	params;
	public boolean button = false;
	
	// View Pointers
	private CheckBox ReInit;
	
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_page);

		ll = (LinearLayout) findViewById(R.id.SyncBuild);
		params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);

		app = (Twix_Application) this.getApplication();
		
		if (!app.prefs.getBoolean("debug", false))
			((Button) findViewById(R.id.DebugButton)).setText("Debug OFF");
		else
			((Button) findViewById(R.id.DebugButton)).setText("Debug ON");
		
		ReInit = (CheckBox) findViewById(R.id.ReInit);
		SetClickListeners();
		
		updateText();
		}
	
	public void SetClickListeners()
		{
		OnCheckedChangeListener ReInitChk = new OnCheckedChangeListener()
			{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
				if( isChecked )
					ReInitDialog((CheckBox)buttonView).show();
				}
			}
		;
		ReInit.setOnCheckedChangeListener(ReInitChk);
		}
	
	public Dialog ReInitDialog(final CheckBox cb)
		{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage("Are you sure you want to re-initialize your data? Syncing will take longer than usual. " +
				"Only use this function when there are issues with your tablet's data.")
				.setPositiveButton("Ok",
					new DialogInterface.OnClickListener()
						{
						public void onClick(DialogInterface dialog, int id)
							{
							cb.setChecked(true);
							}
						})
				.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener()
						{
						public void onClick(DialogInterface dialog, int id)
							{
							cb.setChecked(false);
							}
						});
		
		return builder.create();
		}
	
	public void closeApp(View v)
		{
		if (checkPages())
			{
			Twix_AgentTabActivity act = (Twix_AgentTabActivity) getParent();
			act.finish();
			}
		else
			Toast.makeText(
					this,
					"You have open pages. Please finish all your work and try again",
					Toast.LENGTH_LONG).show();
		}
	
	public void changeUser(View v)
		{
		synchronized(this)
			{
			if( !button )
				{
				button = true;
				if (checkPages())
					{
					app.Sync(false, true, ReInit.isChecked());
					}
				else
					promptPages(true);
				button = false;
				}
			}
		}
	
	public void updateSync(View v)
		{
		synchronized(this)
			{
			if( !button )
				{
				button = true;
				if (checkPages())
					{
					app.Sync(false, false, ReInit.isChecked());
					}
				else
					promptPages(false);
				button = false;
				}
			}
		}
	
	public void updateText()
		{
		// Get the employee's actual name from the mechanics table
		((TextView) findViewById(R.id.LoginName))
				.setText( app.techName );
		
		if( app.last_sync != null )
			((TextView) findViewById(R.id.LoginDate)).setText( app.last_sync );
		
		}
	
	/*
	private String getEmpName(String empID)
		{
		String empName = "";
		
		String sqlQ = "select mechanic_name from mechanic where mechanic ='" + empID + "'";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			empName = cursor.getString(0);
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
				
		return empName;
		}
	*/
	
	// Check if there are pages open
	private boolean checkPages()
		{
		TabActivity host = ((TabActivity) getParent());
		LocalActivityManager manager = host.getLocalActivityManager();

		Twix_TabActivityGroup group = (Twix_TabActivityGroup) manager.getActivity("tags");
		if (group != null)
			{
			if (group.activityExists("Twix_AgentOpenTagsTabHost"))
				return false;
			}

		group = (Twix_TabActivityGroup) manager.getActivity("siteinfo");
		if (group != null)
			{
			if (group.activityExists("Twix_AgentEquipmentTabHost_Edit"))
				return false;
			}

		return true;
		}

	private void promptPages(final boolean changeUser)
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	String error = saveAllPages();
			        	
			        	if( error.length() < 1)
			        		{
			        		app.Sync(false, changeUser, ReInit.isChecked());
			        		}
			        	else
			        		{
			        		AlertDialog alert = new AlertDialog.Builder(Twix_AgentSyncPage.this).create();
			            	alert.setTitle("Error Saving Pages:");
			            	alert.setMessage( error );
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
		
			        case DialogInterface.BUTTON_NEGATIVE:
			        	// Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Would you like to save all your open pages?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		}
	
	private String saveAllPages()
		{
		TabActivity host = ((TabActivity) getParent());
		LocalActivityManager manager = host.getLocalActivityManager();
		
		Twix_AgentActivityGroup2 actgroup2 = (Twix_AgentActivityGroup2) manager.getActivity("tags");
		LocalActivityManager manager2 = null;
		if( actgroup2 != null )
			manager2 = actgroup2.getLocalActivityManager();
		
		String errorMessage = "";
		
		if( (actgroup2 != null) && (manager2 != null) )
			{
			Twix_AgentOpenTagsTabHost openTagTabs =
					(Twix_AgentOpenTagsTabHost) manager2.getActivity("Twix_AgentOpenTagsTabHost");
			
			Twix_AgentServiceUnitTabHost serviceUnitTabs =
					(Twix_AgentServiceUnitTabHost) manager2.getActivity("Twix_AgentServiceUnitTabHost");
			// Close the service Units if they exist. Also let the caller known if they saved correctly
			if( serviceUnitTabs != null )
				{
				if( !serviceUnitTabs.save_call(this) )
					errorMessage += "Error Saving Service Tag Units. See Alert.";
					//errorMessage += actgroup2.saveServiceUnit_Call(false);
				}
			
			if( openTagTabs != null && (errorMessage.length() < 1) )
				openTagTabs.finish();
			
			if( errorMessage.length() > 0 )
				{
				errorMessage += "\n";
				}
			}
		
		Twix_AgentActivityGroup3 actgroup3 = (Twix_AgentActivityGroup3) manager.getActivity("siteinfo");
		LocalActivityManager manager3 = null;
		if( actgroup3 != null )
			manager3 = actgroup3.getLocalActivityManager();
		
		if( (actgroup3 != null) && (manager3 != null))
			{
			Twix_AgentEquipmentTabHost_Edit equipmentTabs =
					(Twix_AgentEquipmentTabHost_Edit) manager3.getActivity("Twix_AgentEquipmentTabHost_Edit");
			
			if( equipmentTabs != null )
				{
				String equipmentError = equipmentTabs.writeChanges(false);
				if( equipmentError.length() > 0 )
					{
					if( errorMessage.length() > 0 )
						errorMessage += "\n";
					errorMessage += "\tEquipment Errors:\n\t - " + equipmentError;
					}
				}
			}
		
		return errorMessage;
		}
	
	// Refresh the Sync Screen
	public void readSQL()
		{
		ll.removeAllViews();
		
		boolean reqUpdate = app.prefs.getBoolean("reqUpdate", true);
		boolean data_dirty = app.prefs.getBoolean("data_dirty", true);
		boolean sync_dirty = app.prefs.getBoolean("sync_dirty", true);
		
		if( !reqUpdate && !data_dirty && !sync_dirty)
			{
			getEquipmentChanges();
			getContactChanges();
			getNotesChanges();
			getClosedTags();
			getSubmittedBlues();
			}
		else
			{
			if( data_dirty || sync_dirty)
				ll.addView(createTextView(" - Error during last sync. Restoring data next sync with no data loss."));
			else
				ll.addView(createTextView(" - Download and Install a Twix Mobile Update"));
			}
		}

	private void getEquipmentChanges()
		{
		String sqlQ = "SELECT e.equipmentId, "
				+ "( ec.categoryDesc || ' - ' || e.unitNo ), "
				+ "sa.siteName, e.verified "
				+ "FROM equipment as e "
				+ "LEFT OUTER JOIN equipmentCategory as ec on e.equipmentCategoryId = ec.equipmentCategoryId "
				+ "LEFT OUTER JOIN serviceAddress as sa on e.serviceAddressId = sa.serviceAddressId "
				+ "WHERE e.modified = 'Y'";
		Cursor cursor = app.db.rawQuery(sqlQ);

		String s;
		if (cursor.moveToFirst())
			{
			do
				{
				s = " - " + cursor.getString(1);

				if (cursor.getString(0).charAt(0) == '-')
					s += " added to ";
				else
					s += " modified from ";

				s += cursor.getString(2);
				
				if (cursor.getString(3).contentEquals("Y"))
					s += " and Verified";
				
				ll.addView(createTextView(s));

				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}

	private void getContactChanges()
		{
		String sqlQ = "SELECT c.contactId, c.contactName, sa.siteName "
				+ "FROM serviceAddressContact as c "
				+ "LEFT OUTER JOIN serviceAddress as sa on c.serviceAddressId = sa.serviceAddressId "
				+ "WHERE c.modified = 'Y'";
		Cursor cursor = app.db.rawQuery(sqlQ);

		String s;
		if (cursor.moveToFirst())
			{
			do
				{
				s = " - Contact ";
				if (cursor.getString(0).charAt(0) == '-')
					s += "added to ";
				else
					s += "modified from ";

				s += cursor.getString(2) + ": " + cursor.getString(1);

				ll.addView(createTextView(s));
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void getNotesChanges()
		{
		String sqlQ = "SELECT n.noteid, n.serviceaddressid, n.notes "
				+ "FROM notes as n "
				+ "LEFT OUTER JOIN serviceAddress as sa on n.serviceAddressId = sa.serviceAddressId "
		+ "WHERE n.modified = 'Y'";
				
		Cursor cursor = app.db.rawQuery(sqlQ);

		String s;
		if (cursor.moveToFirst())
			{
			do
				{
				s = " - Notes ";
				if (cursor.getString(0).charAt(0) == '-')
					s += "added to ";
				else
					s += "modified from ";

				s += cursor.getString(1) + ": " + cursor.getString(2);

				ll.addView(createTextView(s));
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}

	private void getClosedTags()
		{
		String sqlQ = "SELECT "
				+ "CASE WHEN tag.serviceAddressId = '0' THEN ( "
				+ "CASE WHEN tag.dispatchId = '0' THEN (tag.siteName) ELSE "
				+ "(select siteName from serviceAddress "
				+ "where serviceAddress.serviceAddressId = d.serviceAddressId) END "
				+ ") ELSE (sa.siteName) END AS siteName, "
				+ "CASE WHEN tag.dispatchId = '0' THEN ( tag.batchNo ) ELSE ( d.batchNo ) END AS batchNo, "
				+ "CASE WHEN tag.dispatchId = '0' THEN ( tag.jobNo ) ELSE ( d.jobNo ) END AS jobNo, "
				+ "CASE WHEN tag.dispatchId = '0' THEN ( tag.serviceType ) ELSE ( d.contractType ) END AS serviceType "
				+

				"FROM openServiceTag as tag "
				+ "LEFT OUTER JOIN serviceAddress as sa on tag.serviceAddressId = sa.serviceAddressId "
				+ "LEFT OUTER JOIN dispatch as d on tag.dispatchId = d.dispatchId "
				+ "WHERE tag.completed = 'Y'";
		Cursor cursor = app.db.rawQuery(sqlQ);

		String s;
		if (cursor.moveToFirst())
			{
			do
				{
				s = " - Submitting Tag for " + cursor.getString(0) + " as a "
						+ cursor.getString(3) + " contract " + " with BatchNo "
						+ cursor.getString(1) + " with JobNo "
						+ cursor.getString(2);

				ll.addView(createTextView(s));
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void getSubmittedBlues()
		{
		String sqlQ = "SELECT b.blueId, sa.siteName, st.serviceTagId "
				//+ "( ec.categoryDesc || ' - ' || e.unitNo ), "
				+ "FROM blue as b "
				+ "LEFT OUTER JOIN openServiceTag as st on b.serviceTagId = st.serviceTagId "
				+ "LEFT OUTER JOIN serviceAddress as sa on st.serviceAddressId = sa.serviceAddressId "
				+ "WHERE b.completed = 'M'";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			String s, u; String blueId;
			TextView blueTV;
			int blueCount;
			// Blue Selection Loop
			do
				{
				blueCount = 0;
				blueId = cursor.getString(0);
				s = " - Submitting blue for Tag: " + cursor.getString(2) + " at Site: " + cursor.getString(1);
				
				blueTV = createTextView("");
				
				ll.addView(blueTV);
				
				sqlQ = "SELECT ( ec.categoryDesc || ' - ' || e.unitNo )  "
						+ "FROM blueUnit as bu "
						+ "LEFT OUTER JOIN equipment as e on e.equipmentId = bu.equipmentId "
						+ "LEFT OUTER JOIN equipmentCategory as ec on ec.equipmentCategoryId = e.equipmentCategoryId "
						+ "WHERE bu.blueId = '" + blueId + "'";
				Cursor unitCursor = app.db.rawQuery(sqlQ);
				if (unitCursor.moveToFirst())
					{
					// Blue Unit Selection Loop
					do
						{
						u = "\t - Blue Unit: " + unitCursor.getString(0);
						ll.addView(createTextView(u));
						blueCount++;
						}
					while (unitCursor.moveToNext());
					}
				if (unitCursor != null && !unitCursor.isClosed())
					{
					unitCursor.close();
					}
				
				s += " with " + blueCount + " units quoted.";
				blueTV.setText(s);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private TextView createTextView(String s)
		{
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setText(s);
		tv.setPadding(5, 5, 5, 5);
		tv.setTextSize(app.Twix_Theme.headerSize);
		tv.setTextColor(app.Twix_Theme.sub1Header);
		tv.setBackgroundColor(app.Twix_Theme.tableBG);

		return tv;
		}

	@Override
	public void onResume()
		{
		readSQL();

		super.onResume();
		}

	public void about(View v)
		{
		// Alert
		AlertDialog alert = new AlertDialog.Builder(this).create();
    	alert.setTitle("Twix Mobile v" + app.version );
    	alert.setMessage(
    			"Therma Web Information Exchange - Mobile\n\n" +
    			"Author: Therma LLC .\n\n" +
    			"This software is owned by: Therma LLC\n\n" +
    			"Copyright© Therma, Inc   2012         All rights reserved\n\n\n" +
    			"Warning: This application is protected by copyright\n" +
    			"\tlaw. Unauthorized reproduction or distribution of this\n" +
    			"\tprogram, or any portion of it, may result in civil and\n" +
    			"\tcriminal penalties.\n\n\nChangeLog:\n\n" + app.changelog);
    	alert.setButton("Ok", new DialogInterface.OnClickListener()
    		{  
    		public void onClick(DialogInterface dialog, int which)
    			{
    			return;  
    			}
    		});
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	alert.show();
		}
	
	public void ResetReInit()
		{
		if( ReInit != null )
			ReInit.setChecked(false);
		}
	}
