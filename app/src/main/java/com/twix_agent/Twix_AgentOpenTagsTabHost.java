package com.twix_agent;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentOpenTagsTabHost
 * 
 * Purpose: Hosts open tag details. Open tags can be deleted is they are not marked submit.
 * 			These tabs include:
 * 			- Tag Details: General open tag details. This includes linkage with service addresses and dispatches
 * 			- Blue: These are quotes for equipment repairs.
 * 			- Safety Checklist: The required safety check list for each job.
 * 			- Receipts: Receipts the user has taken photos of
 * 			- Signature: The customer signature or no signature reason, and customer email list
 *  
 * Relevant XML: opentags_tabhost.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentOpenTagsTabHost extends TabActivity
	{
	private Twix_Application app;
	private Context mContext;
	public int serviceTagId;
	public TextView currentSite;
	public boolean readOnly;
	
	public void onCreate(Bundle savedInstanceState)
		{
	    super.onCreate(savedInstanceState);
	    mContext = getParent();
	    app = (Twix_Application) getApplication();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.opentags_tabhost, null);
		this.setContentView( viewToLoad );
	    
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    tabHost.getTabWidget().setOrientation(LinearLayout.VERTICAL);
	    
	    serviceTagId = getIntent().getIntExtra("serviceTagId", 0);
	    
	    TextView tagNo = (TextView) findViewById(R.id.TagNo);
	    if( serviceTagId < 0 )
	    	tagNo.setText("New Tag");
	    else
	    	tagNo.setText(serviceTagId + "");
	    currentSite = (TextView) findViewById(R.id.SiteName);
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Twix_AgentOpenTag.class);
	    intent.putExtra("serviceTagId", serviceTagId );
	    View tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Tag Details");
	    spec = tabHost.newTabSpec("Tag").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentOpenTagSiteDetails.class);
	    intent.putExtra("serviceTagId", serviceTagId );
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Addresses");
	    spec = tabHost.newTabSpec("Addresses").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentBlue.class); 
	    intent.putExtra("serviceTagId", serviceTagId );
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Blue");
	    spec = tabHost.newTabSpec("Blue").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentSafetyChecklist.class);
	    intent.putExtra("serviceTagId", serviceTagId );
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Safety Checklist");
	    spec = tabHost.newTabSpec("SafetyChecklist").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentOpenTagReceipt.class);
	    intent.putExtra("serviceTagId", serviceTagId );
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Receipts");
	    spec = tabHost.newTabSpec("Receipt").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);

	    
	    
	    buildClickListeners();
	    tabHost.setCurrentTab(0);
		}
	
	public void updateFooter(int serviceTagId)
		{
		String sqlQ = "select " +
		"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
		"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.siteName) ELSE ( " +
			"select serviceAddress.siteName from serviceAddress " +
				"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
															") ELSE (serviceAddress.siteName) END AS siteName, " +
		
		"CASE WHEN openServiceTag.dispatchId = '0' THEN ( '' ) ELSE (dispatch.tenant) END AS tenant " +
		
		"from openServiceTag " + 
		"LEFT OUTER JOIN dispatch " +
			"on openServiceTag.dispatchId = dispatch.dispatchId " +
		"LEFT OUTER JOIN serviceAddress " +
			"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
		"WHERE openServiceTag.serviceTagId = " + serviceTagId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			TextView tv;
			String s;
			
			// Site Name
			s = Twix_TextFunctions.clean(cursor.getString(0));
			if( s.length() > 0)
				{
				tv = (TextView) findViewById(R.id.Title_SiteName);
				tv.setVisibility(View.VISIBLE);
				
				tv = (TextView) findViewById(R.id.SiteName);
				tv.setVisibility(View.VISIBLE);
				tv.setText( s );
				}
			
			//Tenant
			s = Twix_TextFunctions.clean(cursor.getString(1));
			if( s.length() > 0)
				{
				tv = (TextView) findViewById(R.id.Title_Tenant);
				tv.setVisibility(View.VISIBLE);
				
				tv = (TextView) findViewById(R.id.Tenant);
				tv.setVisibility(View.VISIBLE);
				tv.setText( s );
				}
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void buildClickListeners()
		{
		ImageButton del_bn = (ImageButton) findViewById(R.id.Delete);
		OnClickListener delete = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( !openTagHasBlues(serviceTagId) )
					{
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage("Are you sure you want to delete this open tag and all its content?")
					       .setCancelable(false)
					       .setPositiveButton("Yes", new DialogInterface.OnClickListener()
					    	   {
					           public void onClick(DialogInterface dialog, int id)
					        	   {
					        	   delete_callback();
						           finish();
						           }
						       })
					       .setNegativeButton("No", new DialogInterface.OnClickListener()
					    	   {
					           public void onClick(DialogInterface dialog, int id)
					        	   {
					               dialog.cancel();
						           }
						       });
					
					AlertDialog alert = builder.create();
					alert.show();
					}
				else
					Toast.makeText(mContext,
							"Cannot delete the service tag, it has completed blue units",
							Toast.LENGTH_LONG).show();
				
				}
			}
		;
		del_bn.setOnClickListener(delete);
		}
	
	private void delete_callback()
		{
		int id;
		String sqlQ = "select serviceTagUnitId from serviceTagUnit " +
				"WHERE serviceTagId = " + serviceTagId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		if ( cursor.moveToFirst() )
			{
			do
				{
				id = cursor.getInt(0);
				app.db.delete("serviceLabor", "serviceTagUnitId", id );
				app.db.delete("serviceMaterial", "serviceTagUnitId", id );
				app.db.delete("serviceRefrigerant", "serviceTagUnitId", id );
				app.db.delete("servicePhoto", "serviceTagUnitId", id );
				app.db.delete("pmCheckList", "serviceTagUnitId", id );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		
		sqlQ = "select blueId from blue " +
				"WHERE serviceTagId = '" + serviceTagId + "'";
		cursor = app.db.rawQuery(sqlQ);
		if ( cursor.moveToFirst() )
			{
			do
				{
				id = cursor.getInt(0);
				app.db.delete("blueUnit", "blueId", id );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		// Delete the FormDataValues, FormDataSignatures, FormPhotos, and FormData instances
		//  under this Service Tag
		String sql = "DELETE FROM FormDataValues WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId IN " +
					"(SELECT serviceTagUnitId FROM serviceTagUnit WHERE serviceTagId = " + serviceTagId + ") )";
		app.db.db.execSQL(sql);
		sql = "DELETE FROM FormDataSignatures WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId IN " +
					"(SELECT serviceTagUnitId FROM serviceTagUnit WHERE serviceTagId = " + serviceTagId + ") )";
		app.db.db.execSQL(sql);
		
		sql = "DELETE FROM FormPhotos WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId IN " +
					"(SELECT serviceTagUnitId FROM serviceTagUnit WHERE serviceTagId = " + serviceTagId + ") )";
		app.db.db.execSQL(sql);
		
		sql = "DELETE FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId IN " +
					"(SELECT serviceTagUnitId FROM serviceTagUnit WHERE serviceTagId = " + serviceTagId + ")";
		app.db.db.execSQL(sql);
		
		// Delete the Open Service Tag, Service Tag Unit, etc AFTER the forms have been deleted
		//  since their are deleted based on dependency
		app.db.delete("safetyTagChecklist", "serviceTagId", serviceTagId);
		app.db.delete("safetyTagChecklistItem", "serviceTagId", serviceTagId);
		app.db.delete("serviceTagUnit", "serviceTagId", serviceTagId);
		app.db.delete("openServiceTag", "serviceTagId", serviceTagId);
		app.db.delete("blue", "serviceTagId", serviceTagId);
		}
	
	private boolean openTagHasBlues(int ID)
		{
		boolean ret = false;
		String sqlQ = "select blueUnit.blueUnitId " +
				"from openServiceTag " + 
				"INNER JOIN blue " + 
					"on blue.serviceTagId = openServiceTag.serviceTagId " +
				"INNER JOIN blueUnit " + 
					"on blueUnit.blueId = blue.blueId " +
				"WHERE openServiceTag.serviceTagId = " + ID + " " + 
				"AND blueUnit.completed = 'Y'";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			ret = true;
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	}
