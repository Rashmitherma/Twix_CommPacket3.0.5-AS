package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentSiteHistory_TagDetail
 * 
 * Purpose: Displays service tag details for a selected service tag in site history. The service tag can contain
 * 			service units, and their children. No photos, including signatures, are review-able.
 * 
 * Relevant XML: sitehistory_tagdetail.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSiteHistory_TagDetail extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext = this;
	private int serviceTagId;
	private LinearLayout ll;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.sitehistory_tagdetail, null);
		this.setContentView( viewToLoad );
		
		serviceTagId = getIntent().getExtras().getInt("serviceTagId");
		((TextView)findViewById(R.id.TagNo)).setText(" Tag No: " + serviceTagId);
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		ll = (LinearLayout)findViewById( R.id.TagBuild );
				
		readServiceTag();
		readServiceUnit();
		}
	
	private class TagDetailData
		{
		String siteName;
		String tenant;
		String batchNo;
		String jobNo;
		String dispatchDesc;
		String serviceType;
		String disposition;
		String serviceDate;
		String address1;
		String address2;
		String city;
		String state;
		String zip;
		
		String billTo;
		String billAddress1;
		String billAddress2;
		String billAddress3;
		String billAddress4;
		String billAttn;
		
		public boolean hasBilling()
			{
			return (billTo != null && billTo.length() > 0) ||
					(billAddress1 != null && billAddress1.length() > 0) ||
					(billAddress2 != null && billAddress2.length() > 0) ||
					(billAddress3 != null && billAddress3.length() > 0) ||
					(billAddress4 != null && billAddress4.length() > 0) ||
					(billAttn != null && billAttn.length() > 0);
			}
		
		public String getBillingAddress()
			{
			String ret = "";
			if( billAddress1 != null && billAddress1.length() > 0 )
				ret += billAddress1;
			if( billAddress2 != null && billAddress2.length() > 0 )
				ret += " " + billAddress2;
			if( billAddress3 != null && billAddress3.length() > 0 )
				ret += " " + billAddress3;
			if( billAddress4 != null && billAddress4.length() > 0 )
				ret += ", " + billAddress4;
			
			return ret;
			}
		}
	
	private void readServiceTag()
		{
		String sqlQ = "SELECT sa.siteName, st.tenant, " +
						
    				"st.batchNo, st.jobNo, st.description, st.serviceType, " +
					"st.disposition, st.serviceDate, " +
					
					"sa.address1, sa.address2, sa.city, sa.state, sa.zip, " +
																	
					"st.billTo, st.billAddress1, st.billAddress2, " +
					"st.billAddress3, st.billAddress4, " +
					"st.billAttn " +
					
				"FROM serviceTag as st " +
					"LEFT OUTER JOIN serviceAddress as sa " +
						"ON sa.serviceAddressId = st.serviceAddressId " +
				
				"WHERE st.serviceTagId = " + serviceTagId;
		
		Cursor cursor = db.rawQuery(sqlQ);
		LinearLayout tagContainer = null;
		
		TagDetailData data;
		int index = 0;
		if (cursor.moveToFirst())
			{
			data = new TagDetailData();
			data.siteName		= cursor.getString(0);
			data.tenant			= cursor.getString(1);
			data.batchNo		= cursor.getString(2);
			data.jobNo			= Twix_TextFunctions.clean(cursor.getString(3)).replaceAll("(TTCA)", "");
			data.dispatchDesc	= cursor.getString(4);
			data.serviceType	= cursor.getString(5);
			// Change
			data.disposition	= Twix_TextFunctions.clean(cursor.getString(6));
			if( data.disposition.contentEquals("C") )
				data.disposition = "Call Complete";
			else
				data.disposition = "Must Return";
			data.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(7));
			data.address1		= cursor.getString(8);
			data.address2		= cursor.getString(9);
			data.city			= cursor.getString(10);
			data.state			= cursor.getString(11);
			data.zip			= cursor.getString(12);
			data.billTo			= cursor.getString(13);
			data.billAddress1	= cursor.getString(14);
			data.billAddress2	= cursor.getString(15);
			data.billAddress3	= cursor.getString(16);
			data.billAddress4	= cursor.getString(17);
			data.billAttn		= cursor.getString(18);
			
			tagContainer = buildTagDetails( data );
			
			if( data.hasBilling() )
				createBillTo(data, tagContainer);
			
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	private LinearLayout createTable(Integer... args)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	if( args.length > 0 )
    		params.setMargins(args[0], 5, args[0], 5);
    	
     	LinearLayout row = new LinearLayout(mContext);
     	row.setOrientation(LinearLayout.VERTICAL);
     	row.setLayoutParams(params);
     	
     	return row;
    	}
	
	private LinearLayout createRow(Integer... args)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	if( args.length > 0 )
    		params.setMargins(args[0], 0, args[0], 0);
    	
     	LinearLayout row = new LinearLayout(mContext);
     	row.setOrientation(LinearLayout.HORIZONTAL);
     	row.setLayoutParams(params);
     	
     	return row;
    	}
    
    private TextView createTextView(String text, float weight, boolean value, boolean header)
    	{
    	LinearLayout.LayoutParams params =
    			new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
    	params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	if( value )
    		tv.setTextColor(Twix_Theme.headerValue);
    	else
    		tv.setTextColor(Twix_Theme.headerText);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setPadding(4, 4, 4, 4);
		if( header )
			tv.setBackgroundColor(Twix_Theme.headerBG);
		
    	return tv;
    	}
	
	private LinearLayout buildTagDetails( TagDetailData data )
		{
		LinearLayout tl = createTable();
		tl.setBackgroundColor(Twix_Theme.tableBG2);
		
		LinearLayout row = createRow();
		
		row.addView( createTextView( "SiteName: ",		1f, false, true) );
		row.addView( createTextView( data.siteName,		1f, true, true) );
		row.addView( createTextView( "Tenant: ",		1f, false, true) );
		row.addView( createTextView( data.tenant,		1f, true, true) );
		row.addView( createTextView( "Service Type: ",	1f, false, true) );
		row.addView( createTextView( data.serviceType,	1f, true, true) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "BatchNo: ",	1f, false, false) );
		row.addView( createTextView( data.batchNo,	1f, true, false) );
		row.addView( createTextView( "JobNo: ",		1f, false, false) );
		row.addView( createTextView( data.jobNo,	1f, true, false) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "Dispatch Description: ",	1f, false, false) );
		row.addView( createTextView( data.dispatchDesc,	3f, true, false) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "Disposition: ",	1f, false, false) );
		row.addView( createTextView( data.disposition,	1f, true, false) );
		row.addView( createTextView( "ServiceDate: ",	1f, false, false) );
		row.addView( createTextView( data.serviceDate,	1f, true, false) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "Address 1: ",	1f, false, false) );
		row.addView( createTextView( data.address1,	1f, true, false) );
		row.addView( createTextView( "City: ",		1f, false, false) );
		row.addView( createTextView( data.city,		1f, true, false) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "Address 2: ",	1f, false, false) );
		row.addView( createTextView( data.address2,	1f, true, false) );
		row.addView( createTextView( "State: ",		1f, false, false) );
		row.addView( createTextView( data.state,	1f, true, false) );
		
		tl.addView( row );
		row = createRow(10);
		
		row.addView( createTextView( "",			2f, false, false) );
		row.addView( createTextView( "Zip: ",		1f, false, false) );
		row.addView( createTextView( data.zip,	1f, true, false) );
		
		tl.addView( row );
		ll.addView( tl );
		
		return tl;
		}
	
	private void createBillTo(TagDetailData data, LinearLayout tagContainer)
		{
		LinearLayout tl = createTable();
		tl.setBackgroundColor(Twix_Theme.tableBG);
		
		LinearLayout row = createRow();
		
		row.addView( createTextView( "Alternate Billing", 1f, false, true) );
		tl.addView(row);
		
		row = createRow();
		
		row.addView( createTextView( "Bill to: ",		1f, false, false) );
		row.addView( createTextView( data.billTo,		2f, false, false) );
		row.addView( createTextView( "Bill Attention: ",1f, false, false ) );
		row.addView( createTextView( data.billAttn,		2f, false, false) );
		
		tl.addView(row);
		row = createRow();
		
		row.addView( createTextView( "Bill Address: ",			1f, false, false ) );
		row.addView( createTextView( data.getBillingAddress(),	3f, false, false ) );
		tl.addView(row);
		
		tagContainer.addView(tl);
		}
	
	// Service Unit Details
	public class UnitDetailData
		{
		int serviceTagUnitId;
		int equipmentCategoryId;
		int equipmentId;
		
		String title;
		String manufacturer;
		String model;
		String serialNo;
		String servicePerformed;
		String comments;
		
		List<LaborData> laborData;
		List<MaterialData> materialData;
		}
	
	private void readServiceUnit()
		{
		String sqlQ = "SELECT su.serviceTagUnitId, eq.equipmentId, ec.equipmentCategoryId, " +
				"ec.categoryDesc || ' - ' || eq.UnitNo, " +
				"eq.manufacturer, eq.serialNo, " +
				"eq.Model, su.servicePerformed, su.comments " +
			"FROM serviceTagUnit as su " +
				"LEFT OUTER JOIN equipment as eq " +
					"ON eq.equipmentId = su.equipmentId " +
				"LEFT OUTER JOIN equipmentCategory as ec " +
					"ON ec.equipmentCategoryId = eq.equipmentCategoryId " +
			"WHERE su.serviceTagId = '" + serviceTagId + "'";
		
		Cursor cursor = db.rawQuery(sqlQ);
		UnitDetailData data;
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				data = new UnitDetailData();
				data.serviceTagUnitId		= cursor.getInt(0);
				data.equipmentId			= cursor.getInt(1);
				data.equipmentCategoryId	= cursor.getInt(2);
				data.title				= cursor.getString(3);
							
				data.manufacturer			= cursor.getString(4);
				data.model					= cursor.getString(5);
				data.serialNo				= cursor.getString(6);
				data.servicePerformed		= cursor.getString(7);
				data.comments				= cursor.getString(8);
				
				data.laborData		= getLabor( data.serviceTagUnitId );
				data.materialData	= getMaterials( data.serviceTagUnitId );
				
				buildServiceUnit(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private class LaborData
		{
		String serviceDate;
		float regHours;
		float thHours;
		float dtHours;
		String mechanic;
		String rate;
		}
	
	private List<LaborData> getLabor( int serviceTagUnitId )
		{
		List<LaborData> ret = new ArrayList<LaborData>();
		String sqlQ =
				"SELECT sl.serviceDate, sl.regHours, sl.thHours, sl.dtHours, m.mechanic_name,sl.rate " +
				"FROM serviceLabor as sl " +
					"LEFT OUTER JOIN mechanic as m ON " +
						"m.mechanic = sl.mechanic " +
				"WHERE sl.serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		LaborData data;
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				data = new LaborData();
				data.serviceDate	= Twix_TextFunctions.DBtoNormal( cursor.getString(0) );
				data.regHours		= cursor.getFloat(1);
				data.thHours		= cursor.getFloat(2);
				data.dtHours		= cursor.getFloat(3);
				data.mechanic		= cursor.getString(4);
				data.rate		= cursor.getString(5);
				ret.add(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	private class MaterialData
		{
		float quantity;
		String desc;
		}
	
	private List<MaterialData> getMaterials( int serviceTagUnitId )
		{
		List<MaterialData> ret = new ArrayList<MaterialData>();
		String sqlQ =
				"SELECT sm.quantity, sm.materialDesc " +
				"FROM serviceMaterial as sm " +
				"WHERE sm.serviceTagUnitId = '" + serviceTagUnitId + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		MaterialData data;
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				data = new MaterialData();
				data.quantity	= cursor.getFloat(0);
				data.desc		= cursor.getString(1);
				ret.add(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	// Builds the Actual LinearLayours and adds them to the ViewGroup
	private void buildServiceUnit(UnitDetailData data)
		{
    	LinearLayout tl = createTable(10);
		tl.setBackgroundColor(Twix_Theme.tableBG);
    	
		LinearLayout row = createRow();
		
		// Header Row
		
		
		if( data.equipmentId > 0 )
			{
			row.addView( createTextView( "Equipment: ", 1f, false, true) );
			row.addView( createTextView( data.title, 1f, true, true) );
			row.addView( createTextView( "Serial No: ", 1f, false, true ) );
			row.addView( createTextView( data.serialNo, 1f, true, true) );
			
			tl.addView(row);
			row = createRow(10);
			
			// Sub Rows
			row.addView( createTextView( "Manufacturer: ", 1f, false, false) );
			row.addView( createTextView( data.manufacturer, 1f, true, false) );
			row.addView( createTextView( "Model: ", 1f, false, false) );
			row.addView( createTextView( data.model, 1f, true, false) );
			
			tl.addView(row);
			row = createRow(10);
			}
		else
			{
			row.addView( createTextView( "Equipment: ", 1f, false, true) );
			row.addView( createTextView( "No Category", 1f, true, true) );
			row.addView( createTextView( "", 2f, false, true) );
			tl.addView(row);
			row = createRow(10);
			}
		
		
		
		row.addView( createTextView( "Service Performed: ", 1f, false, false) );
		row.addView( createTextView( "Comments: ", 1f, false, false) );
		
		tl.addView(row);
		row = createRow(10);
		
		row.addView( createTextView( data.servicePerformed, 1f, true, false) );
		row.addView( createTextView( data.comments, 1f, true, false) );
		
		tl.addView(row);
		
		if( data.equipmentId > 0 )
			{
			row = createRow(10);
			Button ib = new Button(mContext);
			LinearLayout.LayoutParams paramsBN = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, 80);
			ib.setLayoutParams(paramsBN);
			ib.setText( "All Equipment History" );
			ib.setTextColor( Twix_Theme.headerText );
			ib.setTextSize( 20 );
			ib.setBackgroundResource(R.drawable.button_bg);
			ib.setTag( data.equipmentId );
			ib.setOnClickListener(new  OnClickListener()
	        	{
	            @Override
	            public void onClick(View v)
	            	{
	            	EquipmentHistory( (Integer)v.getTag() );
	                }
	        	});
			
			row.addView(ib);
			tl.addView(row);
			}
		
		LinearLayout details = laborTable(data.laborData);
		if( details != null )
			tl.addView( details );
		
		details = materialTable(data.materialData);
		if( details != null )
			tl.addView( details );
		
		ll.addView(tl);
		}
	
	private LinearLayout laborTable(List<LaborData> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		LinearLayout tl = createTable(12);
		tl.setBackgroundColor(Twix_Theme.tableBG2);
		
		LinearLayout row = createRow();
		
		row.addView( createTextView( "Date", 1f, false, true) );
		row.addView( createTextView( "Regular Hours", 1f, false, true) );
		row.addView( createTextView( "Time and a Half Hours", 1f, false, true) );
		row.addView( createTextView( "Double Time Hours", 1f, false, true) );
		row.addView( createTextView( "Rate", 1f, false, true) );
		row.addView( createTextView( "Mechanic", 1f, false, true) );
		tl.addView(row);
		
		LaborData data;
		for( int i = 0; i < size; i++ )
			{
			data = list.get(i);
			row = createRow();
			row.addView( createTextView( data.serviceDate, 1f, true, false) );
			row.addView( createTextView( data.regHours+"", 1f, true, false) );
			row.addView( createTextView( data.thHours+"", 1f, true, false) );
			row.addView( createTextView( data.dtHours+"", 1f, true, false) );
			if(data.rate == null)
				data.rate = "";
			row.addView( createTextView( data.rate+"", 1f, true, false) );
			row.addView( createTextView( data.mechanic, 1f, true, false) );
			tl.addView(row);
			}
		
		return tl;
		}
	
	private LinearLayout materialTable(List<MaterialData> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		LinearLayout tl = createTable(12);
		tl.setBackgroundColor(Twix_Theme.tableBG2);
		
		LinearLayout row = createRow();
		row.addView( createTextView( "Material Quantity", 1f, false, true) );
		row.addView( createTextView( "Material Description", 1f, false, true) );
		tl.addView(row);
		
		MaterialData data;
		for( int i = 0; i < size; i++ )
			{
			data = list.get(i);
			row = createRow();
			row.addView( createTextView( data.quantity+"",	1f, true, false) );
			row.addView( createTextView( data.desc,			1f, true, false) );
			tl.addView(row);
			}
		
		return tl;
		}
	
	// Equipment History
	public void EquipmentHistory(int equipmentId)
		{
		new Twix_AgentEquipment_History( app, mContext, equipmentId);
		}
	}