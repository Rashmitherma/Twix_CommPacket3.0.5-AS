package com.twix_agent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentActivityGroup3
 * 
 * Purpose: Hosts the Site Info tab's activities. Paging functionality for the tab is controlled by this class.
 * 			This class also drives many of the onClick listeners from XML files. This activity group is used in
 * 			underlying activities as a primary context to launch new intents. Often referred to as mContext in
 * 			child activities.
 * 
 * Relevant XML: see Twix_AgentTab3
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentActivityGroup3 extends Twix_TabActivityGroup
	{
    Twix_Application app;
	
    static final int LOGIN = 0;
    static final int SCAN_BARCODE = 1;
    static final int SCAN_BARCODE_READ = 2;
    static final int DOWNLOAD_PARSE = 3;
    
    static final int DATE_DIALOG_ID = 0;
    static final int REMOVE_COMPRESSOR_PROMPT = 1;
	
    static final int EQUIPMENT = 0;
    static final int FAN = 1;
    static final int FILTER = 2;
    static final int REFCIRCUIT = 3;
    static final int PROMPT = 4;
    static final int DELETE = 5;
    
    static final int PROMPT_FAN = 0;
    static final int PROMPT_SHEAVE = 1;
    static final int PROMPT_FILTER = 2;
    static final int PROMPT_REFCIRCUIT = 3;
    static final int PROMPT_COMPRESSOR = 4;
    
    static final int CANCEL_NEW_EQUIPMENT = 0;
    static final int CANCEL_CHANGED_EQUIPMENT = 1;
    
    static final String CANCEL_NEW_EQ = "Are you sure you want to delete your new peice of Equipment?";
    static final String CANCEL_EQ_CHANGES = "Are you sure you want to cancel your Changes?";
    
    Class<? extends Activity> topClass;
    
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        
        app = (Twix_Application) getApplication();
        
        startChildActivity("Twix_AgentTab3", new Intent(this,Twix_AgentTab3.class));
    	}
	
    /** 
     * Handles the canceling of saves for Equipment Edit Tabhost
     * 
     * @param x - type of prompt
     * @param msg - the message
     */
    public void generalPrompt( final int x, String msg)
    	{
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	generalHandler( x );
			            break;
		
			        case DialogInterface.BUTTON_NEGATIVE:
			            //Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
    	}
	
    private void generalHandler( int x )
	    {
	    LocalActivityManager manager = getLocalActivityManager();
	    Twix_AgentEquipmentTabHost_Edit actTab = (Twix_AgentEquipmentTabHost_Edit) manager.getActivity("Twix_AgentEquipmentTabHost_Edit");
		LocalActivityManager manager2 = actTab.getLocalActivityManager();
		Twix_AgentEquipmentDetail_Edit act;
		if( actTab.dirtyFlag )
			{
			switch(x)
				{
				case CANCEL_NEW_EQUIPMENT:
					act = (Twix_AgentEquipmentDetail_Edit)manager2.getActivity("EquipmentDetail_Edit");
					if( act == null )
						{
						return;
						}
					act.deleteThis();
					break;
				case CANCEL_CHANGED_EQUIPMENT:
					act = (Twix_AgentEquipmentDetail_Edit)manager2.getActivity("EquipmentDetail_Edit");
					if( act == null )
						{
						return;
						}
					finishActivity();
				break;
				}
			}
	    }
    
    public void gotoSite(View v)
		{
		LocalActivityManager manager = getLocalActivityManager();
		Twix_AgentSiteSummary act;
		act = (Twix_AgentSiteSummary)manager.getActivity("Twix_AgentSiteSummary");
		int which = v.getId();
		switch( which )
			{
			case R.id.gotoHistory:
				act.gotoHistory();
			break;
			case R.id.gotoContacts:
				act.gotoContacts();
			break;
			case R.id.gotoEquipment:
				act.gotoEquipment();
				break;
			}
		
		}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    	{
    	if( requestCode == SCAN_BARCODE_READ )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			Twix_AgentEquipment act;
    			act = (Twix_AgentEquipment) getLocalActivityManager().getActivity("Twix_AgentEquipment");
    			act.scanResult( data.getStringExtra("la.droid.qr.result") );
    			}
    		else if ( resultCode == RESULT_CANCELED )
    			Toast.makeText(this, "Scanner Cancelled", Toast.LENGTH_SHORT).show();
    		}
    	
    	if( requestCode == SCAN_BARCODE )
    		{
    		if( resultCode == RESULT_OK)
    			{
    			Twix_AgentEquipmentTabHost_Edit actHost;
    			Twix_AgentEquipmentDetail_Edit act;
    			actHost = (Twix_AgentEquipmentTabHost_Edit) getLocalActivityManager().getActivity("Twix_AgentEquipmentTabHost_Edit");
    			act = (Twix_AgentEquipmentDetail_Edit) actHost.getLocalActivityManager().getActivity("EquipmentDetail_Edit");
    			
    			String barcode = data.getStringExtra("la.droid.qr.result");
    			if( barcode != null && barcode.length() > 0 )
    				act.setBarCode( data.getStringExtra("la.droid.qr.result") );
    			else
    				Toast.makeText(this, "Error reading barcode. Barcode App returned no result.", Toast.LENGTH_LONG).show();
    			}
    		else if ( resultCode == RESULT_CANCELED )
    			Toast.makeText(this, "Scanner Cancelled", Toast.LENGTH_SHORT).show();
    		}
    	}
    
    // Prompts for deleting a peice of equipment
    private boolean checkEquipmentUsed()
    	{
    	boolean ret = false;
    	
    	LocalActivityManager manager = getLocalActivityManager();
    	Twix_AgentEquipmentTabHost_Edit act = ((Twix_AgentEquipmentTabHost_Edit)manager.getActivity("Twix_AgentEquipmentTabHost_Edit"));
    	
    	String sqlQ = "select su.equipmentId from serviceTagUnit as su " +
    					"WHERE su.equipmentId = " + act.equipmentId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			ret = true;
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
    	}
    
    public void deleteEquipment(View v)
    	{
    	if( checkEquipmentUsed() )
    		{
    		Toast.makeText(this, "Cannot delete the equipment. It is currently used in an open tag.", Toast.LENGTH_LONG).show();
    		return;
    		}
    	
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	deleteEquipmentCallback();
			            break;
		
			        case DialogInterface.BUTTON_NEGATIVE:
			        	// Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this equipment?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
    	}
    
    private void deleteEquipmentCallback()
    	{
    	LocalActivityManager manager = getLocalActivityManager();
		LocalActivityManager manager2 = ((TabActivity) manager.getActivity("Twix_AgentEquipmentTabHost_Edit")).getLocalActivityManager();
		Twix_AgentEquipmentDetail_Edit act1 = ((Twix_AgentEquipmentDetail_Edit)manager2.getActivity("EquipmentDetail_Edit"));
        
        if( act1 != null)
        	{
        	act1.deleteEquipment();
        	finishActivity();
        	}
    	}
    
    // Override the before back function so we can check for the equipment dirty flag
    @Override
    public boolean beforeBack()
    	{
		LocalActivityManager manager = getLocalActivityManager();
		Twix_AgentEquipmentTabHost_Edit eqhost = ((Twix_AgentEquipmentTabHost_Edit) manager.getActivity("Twix_AgentEquipmentTabHost_Edit"));
		
		if( eqhost != null )
			{
			if( eqhost.dirtyFlag )
				{
			    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			    String msg;
			    if( eqhost.serviceAddressId > 0 )
			    	msg = CANCEL_NEW_EQ;
			    else
			    	msg = CANCEL_EQ_CHANGES;
			    
			    builder.setMessage(msg)
			           .setCancelable(false)
			           .setPositiveButton("Yes", new DialogInterface.OnClickListener()
			        	   {
			               public void onClick(DialogInterface dialog, int id)
			            	   {
			    	           Twix_AgentActivityGroup3.this.finishActivity();
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
			    
			    return true;
				}
			}
    	return false;
		
    	}
	}
