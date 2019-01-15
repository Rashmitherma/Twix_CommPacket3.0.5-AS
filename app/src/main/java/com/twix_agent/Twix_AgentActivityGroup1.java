package com.twix_agent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/*******************************************************************************************************************
 * Class: Twix_AgentActivityGroup1
 * 
 * Purpose: Hosts the Dispatch tab's activities. Paging functionality for the tab is controlled by this class.
 * 
 * 
 * Note:	On creation, this activity forces the user to log in. If the user cancels the login, the application
 * 			is minimized.
 * 
 * Relevant XML: see Twix_AgentTab1
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/

public class Twix_AgentActivityGroup1 extends Twix_TabActivityGroup
	{
	// Twix Application - Contains necessary pointers to other classes
	private Twix_Application app;
	// Progress dialog to run on the UI thread while parsing
    public ProgressDialog mProgressDialog;
    
    
    /**
     * Initializes all the pointers from Twix_Application
     */
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        
        app = (Twix_Application) this.getApplication();
        startChildActivity("Twix_AgentTab1", new Intent(this,Twix_AgentTab1.class));
        
        if( savedInstanceState != null )
        	{
        	app.empno = savedInstanceState.getString("app.empno");
        	}
        
        // Forces the user to log in before any functionality is available. Since it 
        //  is called from activity group 1, the login will not close, it will only
        //  minimize the application.
        if( (app.empno == null) || (app.empno.length() <= 0) )
			app.Login();
        
        if( !app.prefs.getString("last_version", "").contentEquals(app.version) )
        	{
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
				.setTitle("Twix Mobile v" + app.version + " Changelog")
				.setMessage(app.changelog)
			    .setNeutralButton("Ok", new DialogInterface.OnClickListener()
				    {
				    @Override
					public void onClick(DialogInterface dialog, int which)
						{
						app.prefs.edit().putString("last_version", app.version ).commit();
						if( dialog != null )
							dialog.dismiss();
						}
				    }
			    ).show();
        	}
    	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
		{
		super.onSaveInstanceState(savedInstanceState);
		
		if( savedInstanceState == null )
			savedInstanceState = new Bundle();
		
		savedInstanceState.putString("app.empno", app.empno);
		}
	
	}
