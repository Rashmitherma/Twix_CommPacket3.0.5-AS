package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentEquipmentTabHost_Edit
 * 
 * Purpose: Contains the equipment tabs for edit mode.
 * 			These tabs include:
 * 			- Equipment: General equipment details
 * 			- Fans: Fans included on the piece of equipment.
 * 			- Filters: Filters included on the piece of equipment.
 * 			- Ref Circuits: Refrigeration Circuits included on the piece of equipment. 
 * 
 * Relevant XML: equipment_tabhost_edit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentEquipmentTabHost_Edit extends TabActivity
	{
	private Twix_Application app;
	private Context mContext;
	public TextView equipmentDetail;
	public boolean dirtyFlag = false;
	public TextWatcher setDirtyFlag;
	public TextWatcher upShiftText;
	public boolean readOnly = true;
	
	public int equipmentId;
	public int serviceAddressId;
	
	public void onCreate(Bundle savedInstanceState)
		{
	    super.onCreate(savedInstanceState);
	    mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.equipment_tabhost_edit, null);
		this.setContentView( viewToLoad );
		
		app = (Twix_Application) getApplication();
		readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
		
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    setupClickListeners();
	    buildDirtyFlag();
	    int curPage = getIntent().getIntExtra("current_page", 0);
	    serviceAddressId = getIntent().getIntExtra("serviceAddressId", 0);
	    equipmentId = getIntent().getIntExtra("equipmentId", 0);
	    String siteName = getIntent().getStringExtra("siteName");
	    ((TextView)findViewById(R.id.SiteName)).setText(siteName);
	    equipmentDetail = (TextView) findViewById(R.id.EquipmentDetail);
	    
	   
	    if( equipmentId > 0 )
	    	findViewById(R.id.Delete).setVisibility(View.GONE);
	    
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    tabHost.getTabWidget().setOrientation(LinearLayout.VERTICAL);
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Twix_AgentEquipmentDetail_Edit.class);
	    View tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Equipment");
	    spec = tabHost.newTabSpec("EquipmentDetail_Edit").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentFans_Edit.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Fans/Belts");
	    spec = tabHost.newTabSpec("Fans_Edit").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentFilters_Edit.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Filters");
	    spec = tabHost.newTabSpec("Filters_Edit").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentRefCircuits_Edit.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Refrigeration\nCircuits");
	    spec = tabHost.newTabSpec("RefCircuits_Edit").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(curPage);
		}
	
	/**
	 * Sets up the click listeners for tab-level buttons. Also hides unusable buttons
	 * 	Buttons:
	 * 		- Save
	 * 		- Delete
	 */
	private void setupClickListeners()
		{
		ImageButton save = (ImageButton) findViewById(R.id.Save);
		save.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				saveChanges();
				}
			});
		
		ImageButton delete = (ImageButton) findViewById(R.id.Delete);
		if( !readOnly )
			{
			delete.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					deleteButton();
					}
				});
			}
		else
			{
			delete.setVisibility(View.GONE);
			}
		
		Button EquipmentHistory = (Button) findViewById(R.id.EquipmentHistory);
		EquipmentHistory.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				EquipmentHistory();
				}
			});
		}
	
	/**
	 * Save changes button function call. This asks the user if they want to save the changes
	 * 	and saves them on a "Yes" answer
	 */
	public void saveChanges()
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	if( writeChanges(true).length() > 0 )
			        		{
			        		dialog.dismiss();
			        		}
			            break;
		
			        case DialogInterface.BUTTON_NEGATIVE:
			        	// Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Are you sure you want to save changes?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		}
	
	/**
	 * Writes out the changes to the peice of equipment to the database
	 * 
	 * @param showError - Whether or not to "toast" the error message
	 * @return - An error message as a String
	 */
	public String writeChanges(boolean showError)
    	{
    	// Don't bother validating if the system is in read only mode
    	if( readOnly )
    		return "";
    	
    	LocalActivityManager manager = getLocalActivityManager();
		
		Twix_AgentEquipmentDetail_Edit act1 = ((Twix_AgentEquipmentDetail_Edit)manager.getActivity("EquipmentDetail_Edit"));
		Twix_AgentFans_Edit act2 = ((Twix_AgentFans_Edit)manager.getActivity("Fans_Edit"));
		Twix_AgentFilters_Edit act3 = ((Twix_AgentFilters_Edit)manager.getActivity("Filters_Edit"));
		Twix_AgentRefCircuits_Edit act4 = ((Twix_AgentRefCircuits_Edit)manager.getActivity("RefCircuits_Edit"));
		boolean canUpdate = true;
		
		List<String> act1List = new ArrayList<String>();
		String error = "";
		
        canUpdate = act1.validateSave(act1List);
        
        /* Can be used if validation is necessary for fans, filters, and ref circuits
	    if( act2 != null )
	    	canUpdate = act2.validateSave();
	    
		if( act3 != null )
			canUpdate = act3.validateSave();
	    
        if( act4 != null )
        	canUpdate = act4.validateSave();
        */
		if( canUpdate )
			{
	        equipmentId = act1.updateDB();
			
		    if( act2 != null )
		    	act2.updateDBClass();
			
			if( act3 != null )
				act3.updateDBClass();
	    	
	        if( act4 != null )
	       		act4.updateDBClass();
	        
	        ((Twix_AgentActivityGroup3)mContext).finishActivity();
			}
		else
			{
			int size = act1List.size();
			for( int i = 0; i < size; i++ )
				{
				error += act1List.get(i);
				if( i < size-1 )
					error += ", ";
				if( i == size-2 )
					{
					if( size < 2)
						error += " ";
					error += "and ";
					}
				}
			error += " must be properly filled out before you can save!";
			if( showError )
				Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			}
        
		return error;
    	}
	
	/**
	 * Checks whether or not the piece of equipment exists. This is useful for new pieces of equipment
	 * 	that could potentially be a service unit. We should not be able to delete these
	 * 
	 * @return	true - The equipment exists in service units
	 * 			false - The equipment does not exist in service units
	 */
	private boolean checkEquipmentUsed()
    	{
    	boolean ret = false;
    	String sqlQ = "select su.equipmentId from serviceTagUnit as su " +
    					"WHERE su.equipmentId = '" + equipmentId + "'";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret = true;
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
    	}
	
	/**
	 * Deletes the piece of equipment and all its fans, filters, refCircuits, etc
	 */
	public void deleteEquipment()
		{
		if( equipmentId == 0 )
			return;
		
		app.db.delete("Equipment", "equipmentId", equipmentId);
    	
		String sqlQ = "SELECT Fan.FanId FROM Fan " +
				"WHERE Fan.EquipmentId = '" + equipmentId + "' ";

		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				app.db.delete( "Belt", "FanId", cursor.getInt(0) );
				app.db.delete( "Sheave", "FanId", cursor.getInt(0) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		app.db.delete("Fan", "EquipmentId", equipmentId);
		app.db.delete("Filter", "EquipmentId", equipmentId);
    	
    	sqlQ = "SELECT RefCircuit.CircuitId FROM RefCircuit " +
				"WHERE RefCircuit.EquipmentId = '" + equipmentId + "' ";

		cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				app.db.delete( "Compressor", "CircuitId", cursor.getInt(0) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
		app.db.delete("RefCircuit", "EquipmentId", equipmentId);
		}
	
	/**
	 * Called when the delete button is pressed
	 */
	public void deleteButton()
    	{
    	if( checkEquipmentUsed() )
    		{
    		Toast.makeText(this, "Cannot delete the equipment. It is currently used in an open tag.", Toast.LENGTH_LONG).show();
    		return;
    		}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Are you sure you want to delete this equipment?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
			    @Override
			    public void onClick(DialogInterface dialog, int which)
			    	{
			    	deleteEquipment();
				    ((Twix_AgentActivityGroup3)mContext).finishActivity();
			    	}
				})
		    .setNegativeButton("No", new DialogInterface.OnClickListener()
				{
			    @Override
			    public void onClick(DialogInterface dialog, int which)
			    	{
			        // Do nothing on "No"
			    	}
				})
		    .show();
    	}
	
	/**
	 * Builds the dirty flag textWatcher
	 */
	private void buildDirtyFlag()
		{
		setDirtyFlag = new TextWatcher()
			{
			@Override
			public void afterTextChanged(Editable s)
				{
				}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after)
				{
				}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count)
				{
				dirtyFlag = true;
				}
			};
		
		upShiftText = new TextWatcher()
			{
			@Override
			public void afterTextChanged(Editable s)
				{
				String curString = s.toString();
				if( curString.matches(".*[a-z]+.*") )
					s.replace(0, s.length(), curString.toUpperCase() );
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after)
				{}
			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count)
				{}
			};
		}
	
	/**
	 * Deletes the piece of equipment, NOT fans, filters, refCirctuits.
	 * 	Useful when the peice of equipment is new
	 */
	public void deleteThis()
		{
		if( serviceAddressId != 0 )
			{
			app.db.delete("Equipment", "EquipmentId", equipmentId);
			((Twix_AgentActivityGroup3)mContext).finishActivity();
			}
		}
	
	/**
	 * Creates the EquipmentHistory Pop-up
	 */
	public void EquipmentHistory()
		{
		new Twix_AgentEquipment_History( (Twix_Application)getApplication(), mContext, equipmentId);
		}
	}
