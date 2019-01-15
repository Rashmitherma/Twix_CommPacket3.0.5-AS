package com.twix_agent;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

/*******************************************************************************************************************
 * Class: Twix_AgentTabActivity
 * 
 * Purpose: Hosts all the main tabs of Twix_Agent. The four tabs hosted are "Dispatch", "Open Tags", "Site Info", 
 *  and "Sync".
 *  	These tabs correspond to:
 *  	 - ActivityGroup1 	-> Dispatch
 *    	 - ActivityGroup2 	-> Open Tags
 *  	 - ActivityGroup3 	-> Site Info
 *  	 - SyncPage			-> Sync
 * 
 * 
 * Relevant XML: main.xml
 * 
 * @author Michael Nowak, Comp Three Inc.
 * Modified by Rashmi Kulkarni, Therma Corp.
 *
 ********************************************************************************************************************/

public class Twix_AgentTabActivity extends TabActivity
	{
	private Twix_Application app;
	private View syncIndicator;
	public void onCreate(Bundle savedInstanceState)
		{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    // Setup Application Variables
	    app = (Twix_Application) getApplication();
	    app.Twix_Theme = new Twix_AgentTheme(this);
	    app.prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
	    app.db = new Twix_SQLite(getApplicationContext(), app);
	    app.tele = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	    app.client = new Twix_Client(app);
	    
	    app.MainTabs = this;
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    //Tab 1 - Dispatch
	    intent = new Intent().setClass(this, Twix_AgentActivityGroup1.class);
	    View tabIndicator = inflater.inflate(R.layout.main_tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Dispatches");
	    spec = tabHost.newTabSpec("dispatch").setIndicator(tabIndicator).setContent(intent);
	    tabHost.addTab(spec);
	    
	    //Tab 2 - Tag Activity
	    intent = new Intent().setClass(this, Twix_AgentActivityGroup2.class);
	    tabIndicator = inflater.inflate(R.layout.main_tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Open Work");
	    spec = tabHost.newTabSpec("tags").setIndicator(tabIndicator).setContent(intent);
	    tabHost.addTab(spec);
	    
	    //Tab 3 - Site Info
	    intent = new Intent().setClass(this, Twix_AgentActivityGroup3.class);
	    tabIndicator = inflater.inflate(R.layout.main_tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Site Info");
	    spec = tabHost.newTabSpec("siteinfo").setIndicator(tabIndicator).setContent(intent);
	    tabHost.addTab(spec);
	    
	    //Tab 4 - Sync
	    intent = new Intent().setClass(this, Twix_AgentSyncPage.class);
	    syncIndicator = inflater.inflate(R.layout.main_tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)syncIndicator.findViewById(R.id.tabsText)).setText("Sync");
	    app.setIndicators(syncIndicator);
	    app.refreshTabs();
	    spec = tabHost.newTabSpec("syncPage").setIndicator(syncIndicator).setContent(intent);
	    tabHost.addTab(spec);
	    
	    //Start the TabLayout on Tab 1
	    tabHost.setCurrentTab(0);
		}
	
	public void onDestroy()
		{
		super.onDestroy();
		//Make sure the database is closed
		((Twix_Application) this.getApplication()).db.db.close();
		}
	
	public void setTabState(boolean state)
		{
		this.getTabWidget().setEnabled(state);
		}
	
	}