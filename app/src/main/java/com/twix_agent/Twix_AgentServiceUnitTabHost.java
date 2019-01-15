package com.twix_agent;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceUnitTabHost
 * 
 * Purpose: Hosts Service Unit details. Service units can be deleted, and the button resides at the tabhost level.
 * 			These tabs include:
 * 			- Unit: General Service Unit details. This includes the piece of equipment serviced and services performed.
 * 			- Labor: Labor records for the services performed on the service unit.
 * 			- Material Checklist: Material records for the service unit.
 * 			- Photo: Photos of the serviced unit.
 * 			- PM Checklist: Only available if the service tag has the service type "PM"
 *  
 * Relevant XML: serviceunit_tabhost.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitTabHost extends TabActivity
	{
	private Twix_Application app;
	public Twix_AgentOpenTag tag;
	
	private Context mContext;
	public boolean dirtyFlag = false;
	public TextWatcher setDirtyFlag;
	
	public int serviceTagUnitId;
	public int EquipmentId;
	
	public void onCreate(Bundle savedInstanceState)
		{
	    super.onCreate(savedInstanceState);
	    mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.serviceunit_tabhost, null);
		this.setContentView( viewToLoad );
		
		app = (Twix_Application) getApplication();
		
	    buildDirtyFlag();
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    tabHost.getTabWidget().setOrientation(LinearLayout.VERTICAL);
	    
	    LocalActivityManager manager = ((Twix_AgentActivityGroup2)mContext).getLocalActivityManager();
	    tag = (Twix_AgentOpenTag) ((Twix_AgentOpenTagsTabHost) manager.getActivity("Twix_AgentOpenTagsTabHost"))
	    		.getLocalActivityManager().getActivity("Tag");
	    
	    serviceTagUnitId = getIntent().getIntExtra("serviceTagUnitId", 0);
	    
	    if( tag.tagReadOnly )
	    	{
	    	findViewById(R.id.Save).setVisibility(View.GONE);
	    	findViewById(R.id.Delete).setVisibility(View.GONE);
	    	}
	    
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Twix_AgentServiceTagUnit.class);
	    View tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Unit");
	    spec = tabHost.newTabSpec("Unit").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    
	    intent = new Intent().setClass(this, Twix_AgentServiceUnitLabor.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Labor");
	    spec = tabHost.newTabSpec("Labor").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentServiceUnitMaterial.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Material");
	    spec = tabHost.newTabSpec("Material").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentServiceUnitRefrigerant.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Refrigerant");
	    spec = tabHost.newTabSpec("Refrigerant").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentServiceUnitPhoto.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("Unit Photos");
	    spec = tabHost.newTabSpec("Photo").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Twix_AgentServiceUnitPMChecklist.class);
	    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
	    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("PM Checklist");
	    spec = tabHost.newTabSpec("PMChecklist").setIndicator( tabIndicator ).setContent(intent);
	    tabHost.addTab(spec);
	    
	  /*  if( tag.isPM )
	    	{
		    intent = new Intent().setClass(this, Twix_AgentServiceUnitPMChecklist.class);
		    tabIndicator = inflater.inflate(R.layout.tabs_bg, tabHost.getTabWidget(), false);
		    ((TextView)tabIndicator.findViewById(R.id.tabsText)).setText("PM Checklist");
		    spec = tabHost.newTabSpec("PMChecklist").setIndicator( tabIndicator ).setContent(intent);
		    tabHost.addTab(spec);
	    	}
	    */
	    buildClickListeners();
	    
	    tabHost.setCurrentTab(0);
	    
	    setupFooter( serviceTagUnitId );
		}
	
	private class FooterDetails
		{
		String	siteName,
				tenant,
				jobNo,
				batchNo,
				tagNo;
		}
	
	private void setupFooter(int serviceTagUnitId)
		{
		FooterDetails footerDetails = new FooterDetails();
		
		String sqlQ = "SELECT serviceTagUnit.serviceTagId, " +
		    	
	    		"CASE WHEN openServiceTag.serviceAddressId = 0 THEN ( " +
					"CASE WHEN openServiceTag.dispatchId = 0 THEN (openServiceTag.siteName) ELSE ( " +
						"select serviceAddress.siteName from serviceAddress " +
							"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																		") ELSE (serviceAddress.siteName) END AS siteName, " +
					
				"CASE WHEN openServiceTag.dispatchId = 0 THEN ( '' ) ELSE ( dispatch.tenant ) END AS tenant, " +
				"CASE WHEN openServiceTag.dispatchId = 0 THEN ( openServiceTag.batchNo ) ELSE ( dispatch.batchNo ) END AS batchNo, " +
				"CASE WHEN openServiceTag.dispatchId = 0 THEN ( openServiceTag.jobNo ) ELSE ( dispatch.jobNo ) END AS jobNo " +
				
					
				
				"FROM	serviceTagUnit " +
					"LEFT OUTER JOIN openServiceTag " +
						"on openServiceTag.serviceTagId = serviceTagUnit.serviceTagId " +
					"LEFT OUTER JOIN dispatch " +
						"on openServiceTag.dispatchId = dispatch.dispatchId " +
					"LEFT OUTER JOIN serviceAddress " +
						"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
				"WHERE serviceTagUnit.serviceTagUnitId = " + serviceTagUnitId;

		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			footerDetails.tagNo = Twix_TextFunctions.clean( cursor.getString(0) );
			
			footerDetails.siteName	= Twix_TextFunctions.clean( cursor.getString(1) );
			if( footerDetails.siteName.length() > 0 )
				findViewById(R.id.Title_SiteName).setVisibility(View.VISIBLE);
			
			footerDetails.tenant	= Twix_TextFunctions.clean( cursor.getString(2) );
			if( footerDetails.tenant.length() > 0 )
				findViewById(R.id.Title_Tenant).setVisibility(View.VISIBLE);
			
			footerDetails.batchNo	= Twix_TextFunctions.clean( cursor.getString(3) );
			if( footerDetails.batchNo.length() > 0 )
				findViewById(R.id.Title_BatchNo).setVisibility(View.VISIBLE);
			
			footerDetails.jobNo 	= Twix_TextFunctions.clean( cursor.getString(4) ).replaceAll("(TTCA)", "");
			if( footerDetails.jobNo.length() > 0 )
				findViewById(R.id.Title_JobNo).setVisibility(View.VISIBLE);
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		// Set the Site and Tenant
		((TextView)findViewById(R.id.SiteName)).setText(footerDetails.siteName);
		((TextView)findViewById(R.id.Tenant)).setText(footerDetails.tenant);
		
		// Set the Job and Batch No
		((TextView)findViewById(R.id.JobNo)).setText(footerDetails.jobNo);
		((TextView)findViewById(R.id.BatchNo)).setText(footerDetails.batchNo);
		
		// Set the Tag No
		String s = footerDetails.tagNo;
		if( s.charAt(0) == '-' )
			s = "New Tag";
		((TextView)findViewById(R.id.TagNo)).setText(s);
		}
	
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
		}
	
	private void buildClickListeners()
		{
		ImageButton delete_bn = (ImageButton) findViewById(R.id.Delete);
		OnClickListener DeleteUnit = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to delete this service unit and all its content?")
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
			}
		;
		delete_bn.setOnClickListener(DeleteUnit);
		
		ImageButton save_bn = (ImageButton) findViewById(R.id.Save);
		OnClickListener SaveUnit = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				
					save_call(mContext);
				}
				
			}
		;
		save_bn.setOnClickListener(SaveUnit);
		
		}
	
	public boolean save_call(Context context)
		{
		// Don't bother validating if the tag is read only
		if( tag.tagReadOnly )
			return true;
		
		String error = "Service Unit Errors:\n";
		
    	boolean canSave = true;
    	boolean tempCanSave = true;
		TabWidget tw = getTabWidget();
    	ColorStateList textColor = getResources().getColorStateList(R.drawable.tab_text_selector);
		
    	LocalActivityManager manager = getLocalActivityManager();
    	
    	Twix_AgentServiceTagUnit unit = ((Twix_AgentServiceTagUnit)manager.getActivity("Unit"));
		Twix_AgentServiceUnitMaterial material = ((Twix_AgentServiceUnitMaterial)manager.getActivity("Material"));
		Twix_AgentServiceUnitRefrigerant refrigerant = ((Twix_AgentServiceUnitRefrigerant)manager.getActivity("Refrigerant"));
		Twix_AgentServiceUnitLabor labor = ((Twix_AgentServiceUnitLabor)manager.getActivity("Labor"));
		Twix_AgentServiceUnitPhoto photo = ((Twix_AgentServiceUnitPhoto)manager.getActivity("Photo"));
		Twix_AgentServiceUnitPMChecklist pmCheckList = ((Twix_AgentServiceUnitPMChecklist)manager.getActivity("PMChecklist"));
		
		if( material != null )
			{
			tempCanSave = canSave = material.validateSave();
			
			if( !tempCanSave )
				{
				error += "\n\t";
				error += "Service Material:\n\t\t";
				error += "You must provide a quantity, description, and format total cost and refrigerant added correctly.";
				((TextView)((RelativeLayout)tw.getChildAt(2)).getChildAt(0)).setTextColor(app.Twix_Theme.warnColor);
				}
			else
				((TextView)((RelativeLayout)tw.getChildAt(2)).getChildAt(0)).setTextColor(textColor);
			}
		
		if( refrigerant != null )
			{
			tempCanSave = canSave = refrigerant.validateSave();
			
			if( !tempCanSave )
				{
				error += "\n\t";
				error += "Service Refrigerant:\n\t\t";
				error += "You must provide a Type,Amount,Model,SerialNo of Refrigerant along with Cylinder Name, SerialNo and Transfered To details.";
				((TextView)((RelativeLayout)tw.getChildAt(3)).getChildAt(0)).setTextColor(app.Twix_Theme.warnColor);
				}
			else
				((TextView)((RelativeLayout)tw.getChildAt(3)).getChildAt(0)).setTextColor(textColor);
			}
		
		if( labor != null )
			{
			tempCanSave = labor.validateSave();
			if( canSave )
				canSave = tempCanSave;
			
			if( !tempCanSave )
				{
				error += "\n\t";
				error += "Service Labor:\n\t\t";
				error += "You must select a date, mechanic, and format all hours correctly.";
				((TextView)((RelativeLayout)tw.getChildAt(1)).getChildAt(0)).setTextColor(app.Twix_Theme.warnColor);
				}
			else
				((TextView)((RelativeLayout)tw.getChildAt(1)).getChildAt(0)).setTextColor(textColor);
			}
		/*if (pmCheckList == null)
			{
			AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Have you reviewed your PM Checklist. Please Review the PM checklist");
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
			}*/
		 if( canSave )
			{
			
			
			unit.updateDB();
			
			if( material != null )
				material.updateDB();
			
			if( refrigerant != null )
				refrigerant.updateDB();
			
			if( labor != null )
				labor.updateDB();
			
			if( pmCheckList != null )
				pmCheckList.updateDB();
			
			if( photo != null )
				photo.updateDB();
			
			finish();
			}
		else
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(error)
			       .setCancelable(false)
			       .setNeutralButton("Ok", new DialogInterface.OnClickListener()
			    	   {
			           public void onClick(DialogInterface dialog, int id)
			        	   {
			               dialog.dismiss();
				           }
				       });
			
			AlertDialog alert = builder.create();
			alert.show();
			}
		
		return canSave;
		}
	
	private void delete_callback()
		{
		app.db.delete("serviceTagUnit", "serviceTagUnitId", serviceTagUnitId);
		app.db.delete("serviceLabor", "serviceTagUnitId", serviceTagUnitId);
		app.db.delete("serviceMaterial", "serviceTagUnitId", serviceTagUnitId);
		app.db.delete("serviceRefrigerant", "serviceTagUnitId", serviceTagUnitId);
		app.db.delete("servicePhoto", "serviceTagUnitId", serviceTagUnitId);
		app.db.delete("pmCheckList", "serviceTagUnitId", serviceTagUnitId);
		
		// Delete the FormDataValues, FormDataSignatures, FormPhotos, and FormData instances
		//  under this Service Unit
		String sql = "DELETE FROM FormDataValues WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId = " + serviceTagUnitId + ")";
		app.db.db.execSQL(sql);
		sql = "DELETE FROM FormDataSignatures WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId = " + serviceTagUnitId + ")";
		app.db.db.execSQL(sql);
		
		sql = "DELETE FROM FormPhotos WHERE FormDataId IN " +
				"(SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId = " + serviceTagUnitId + ")";
		app.db.db.execSQL(sql);
		
		sql = "DELETE FROM FormData WHERE ParentTable = 'ServiceTagUnit' " +
					"AND ParentId = " + serviceTagUnitId;
		app.db.db.execSQL(sql);
		}
	
	}