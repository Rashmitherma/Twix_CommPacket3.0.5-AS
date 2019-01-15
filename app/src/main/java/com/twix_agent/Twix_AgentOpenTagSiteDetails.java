package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/*******************************************************************************************************************
 * Class: Twix_AgentOpenTagSiteDetails
 * 
 * Purpose: Displays the site details for an open tag. This includes the address and the billing/alternate billing
 * 			information.
 * 
 * Note:	After an open tag is marked to be submitted, the tag is put into a read only mode. Further changes cannot
 * 			be made to the tag. This prevents later validation errors.
 *  
 * Relevant XML: open_tag.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentOpenTagSiteDetails extends Activity
	{
	private Twix_Application app;
	private Twix_SQLite db;
	private Context mContext;
	private Twix_AgentOpenTag act;
	private SQLdata SQLData;
	
	public void onCreate(Bundle savedInstanceState)
    	{
    	super.onCreate(savedInstanceState);
    	
        mContext = getParent().getParent();
        View viewToLoad = LayoutInflater.from(getParent().getParent()).inflate(R.layout.open_tag_site_details, null);
		this.setContentView(viewToLoad);
		
		LocalActivityManager manager = ((Twix_AgentActivityGroup2)mContext).getLocalActivityManager();
		LocalActivityManager manager2 = ((Twix_AgentOpenTagsTabHost)manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		act = (Twix_AgentOpenTag) manager2.getActivity("Tag");
		
        app = (Twix_Application) getApplication();
        db = app.db;
        //Twix_Theme = app.Twix_Theme;
        SQLData = new SQLdata();
        
		readOnlySetup();
        setClickListeners();
    	}
	
	private void setClickListeners()
		{
		((ImageButton)findViewById(R.id.AltBilling)).setOnClickListener( new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				changeBilling();
				}
			});
		}
	
	private void readOnlySetup()
		{
		if( act.tagReadOnly )
			{
			findViewById(R.id.AltBilling).setVisibility(View.INVISIBLE);
			}
		}
	
	private void readSQL()
		{
		String sqlQ = "SELECT openServiceTag.serviceAddressId, openServiceTag.dispatchId, " +
		    	
	    			"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
					"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.siteName) ELSE ( " +
						"select serviceAddress.siteName from serviceAddress " +
							"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																		") ELSE (serviceAddress.siteName) END AS siteName, " +
					
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.address1) ELSE ( " +
							"select serviceAddress.address1 from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.address1) END AS address1, " +
																			
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.address2) ELSE ( " +
							"select serviceAddress.address2 from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.address2) END AS address2, " +
																			
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.city) ELSE ( " +
							"select serviceAddress.city from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.city) END AS city, " +
																			
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.state) ELSE ( " +
							"select serviceAddress.state from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.state) END AS state, " +
																			
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.zip) ELSE ( " +
							"select serviceAddress.zip from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.zip) END AS zip, " +
					
					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
						"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.buildingNo) ELSE ( " +
							"select serviceAddress.buildingNo from serviceAddress " +
								"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																			") ELSE (serviceAddress.buildingNo) END AS buildingNo, " +
												
					"openServiceTag.billTo, openServiceTag.billAttn, " +
					"openServiceTag.billAddress1, openServiceTag.billAddress2, " +
					"openServiceTag.billAddress3, openServiceTag.billAddress4,  " +
					"billing.Name, " +
					"billing.Address1, " +
					"billing.Address2, " +
					"billing.Address3, " +
					"billing.Address4, " +
					
					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( '' ) ELSE (dispatch.tenant) END AS tenant " +
					
				"FROM	openServiceTag " +
					"LEFT OUTER JOIN dispatch " +
						"on openServiceTag.dispatchId = dispatch.dispatchId " +
					"LEFT OUTER JOIN serviceAddress " +
						"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
					"LEFT OUTER JOIN billing " +
						"ON billing.CustomerID = dispatch.cusNo " +
							"AND (TRIM(billing.altBillId) = TRIM(dispatch.altBillTo) OR (billing.altBillId IS NULL AND TRIM(dispatch.altBillTo) = '')) " +
/*
					"LEFT OUTER JOIN billing as altBilling " +
						"ON altBilling.CustomerID = dispatch.altBillTo AND dispatch.altBillTo != '' " +
*/
				
				"WHERE openServiceTag.serviceTagId = " + act.serviceTagId;
		
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			/**
			 * Manually set the lists for easier management in the future.
			 */
			SQLData.siteName 			= Twix_TextFunctions.clean(cursor.getString(2));
			SQLData.add1 				= Twix_TextFunctions.clean(cursor.getString(3));
			SQLData.add2 				= Twix_TextFunctions.clean(cursor.getString(4));
			SQLData.city 				= Twix_TextFunctions.clean(cursor.getString(5));
			SQLData.state 				= Twix_TextFunctions.clean(cursor.getString(6));
			SQLData.zip 				= Twix_TextFunctions.clean(cursor.getString(7));
			SQLData.buildingNo 			= Twix_TextFunctions.clean(cursor.getString(8));
			SQLData.billTo 				= Twix_TextFunctions.clean(cursor.getString(9));
			SQLData.billAttn 			= Twix_TextFunctions.clean(cursor.getString(10));
			SQLData.billAdd1 			= Twix_TextFunctions.clean(cursor.getString(11));
			SQLData.billAdd2 			= Twix_TextFunctions.clean(cursor.getString(12));
			SQLData.billAdd3 			= Twix_TextFunctions.clean(cursor.getString(13));
			SQLData.billAdd4 			= Twix_TextFunctions.clean(cursor.getString(14));
			
			if( SQLData.altEmpty() )
				{
				((TextView)findViewById(R.id.AltBillingHeader)).setText("Billing Information");
				SQLData.billTo		= Twix_TextFunctions.clean(cursor.getString(15));
				SQLData.billAdd1 	= Twix_TextFunctions.clean(cursor.getString(16));
				SQLData.billAdd2 	= Twix_TextFunctions.clean(cursor.getString(17));
				SQLData.billAdd3 	= Twix_TextFunctions.clean(cursor.getString(18));
				SQLData.billAdd4 	= Twix_TextFunctions.clean(cursor.getString(19));
				}
			else
				{
				((TextView)findViewById(R.id.AltBillingHeader)).setText("Alternate Billing Information");
				}
			
			SQLData.tenant			= Twix_TextFunctions.clean(cursor.getString(20));
			
			setData();
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	private class SQLdata
		{
		String siteName;
		String tenant;
		String add1;
		String add2;
		String city;
		String state;
		String zip;
		String buildingNo;
		String billTo;
		String billAttn;
		String billAdd1;
		String billAdd2;
		String billAdd3;
		String billAdd4;
		
		public boolean altEmpty()
			{
			if( 
				(billTo.length() > 0 )
			||	(billAttn.length() > 0 )
			||	(billAdd1.length() > 0 )
			||	(billAdd2.length() > 0 )
			||	(billAdd3.length() > 0 )
			||	(billAdd4.length() > 0 )
										)
				return false;
			
			return true;
			}
		}
	
	private void setData()
		{
		TextView tv = (TextView)findViewById(R.id.Text_SiteName);
		tv.setText(SQLData.siteName);
		if( SQLData.tenant.length() > 0 )
			{
			((LinearLayout.LayoutParams)tv.getLayoutParams()).weight = 2;
			tv = (TextView)findViewById(R.id.Text_Tenant);
			tv.setText(SQLData.tenant);
			tv.setVisibility(View.VISIBLE);
			findViewById(R.id.Title_Tenant).setVisibility(View.VISIBLE);
			}
		((TextView)findViewById(R.id.Text_Address1))	.setText(SQLData.add1);
		((TextView)findViewById(R.id.Text_Address2))	.setText(SQLData.add2);
		((TextView)findViewById(R.id.Text_City))		.setText(SQLData.city);
		((TextView)findViewById(R.id.Text_State))		.setText(SQLData.state);
		((TextView)findViewById(R.id.Text_Zip))			.setText(SQLData.zip);
		((TextView)findViewById(R.id.Text_BuildingNo))	.setText(SQLData.buildingNo);
		
		((TextView)findViewById(R.id.AltName))			.setText(SQLData.billTo);
		((TextView)findViewById(R.id.AltAttn))			.setText(SQLData.billAttn);
		((TextView)findViewById(R.id.AltAddress1))		.setText(SQLData.billAdd1);
		((TextView)findViewById(R.id.AltAddress2))		.setText(SQLData.billAdd2);
		((TextView)findViewById(R.id.AltAddress3))		.setText(SQLData.billAdd3);
		((TextView)findViewById(R.id.AltAddress4))		.setText(SQLData.billAdd4);
		}
	
	private void changeBilling()
		{
		View viewToLoad = LayoutInflater.from(this).inflate(R.layout.altbilling_popup, null);
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		final List<EditText> fields = new ArrayList<EditText>();
		fields.add((EditText)viewToLoad.findViewById(R.id.AltName));
		fields.add((EditText)viewToLoad.findViewById(R.id.AltAttn));
		fields.add((EditText)viewToLoad.findViewById(R.id.AltAddress1));
		fields.add((EditText)viewToLoad.findViewById(R.id.AltAddress2));
		fields.add((EditText)viewToLoad.findViewById(R.id.AltAddress3));
		fields.add((EditText)viewToLoad.findViewById(R.id.AltAddress4));
		
		fields.get(0).setText( SQLData.billTo );
		fields.get(1).setText( SQLData.billAttn );
		fields.get(2).setText( SQLData.billAdd1 );
		fields.get(3).setText( SQLData.billAdd2 );
		fields.get(4).setText( SQLData.billAdd3 );
		fields.get(5).setText( SQLData.billAdd4 );
		
		dialog.setContentView(viewToLoad);
		dialog.show();
		
		((Button)dialog.findViewById(R.id.Save)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				ContentValues cv = new ContentValues();
				
				cv.put("billTo", fields.get(0).getText().toString());
				cv.put("billAddress1", fields.get(1).getText().toString());
				cv.put("billAddress2", fields.get(2).getText().toString());
				cv.put("billAddress3", fields.get(3).getText().toString());
				cv.put("billAddress4", fields.get(4).getText().toString());
				cv.put("billAttn", fields.get(5).getText().toString());
				
		        db.update("openServiceTag", cv, "serviceTagId", act.serviceTagId);
		        act.setupTagDetails();
		        
				dialog.dismiss();
				
				readSQL();
				}
			});
		
		((Button)dialog.findViewById(R.id.Clear)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				int size = fields.size();
				for( int i = 0; i < size; i++ )
					fields.get(i).setText("");
				}
			});
		
		
		((Button)dialog.findViewById(R.id.Cancel)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				dialog.dismiss();
				}
			});
		}
	
	public void onResume()
		{
		readSQL();
		
		super.onResume();
		}
	
	public void onDestroy()
		{
		// Only modify the database if the blue is editable
		if( !act.tagReadOnly )
			updateDB();
		
		super.onDestroy();
		}
	
	private void updateDB()
		{
		//Currently does nothing. Functionality is still optional.
		}
	
	}
