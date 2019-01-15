/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;

/*******************************************************************************************************************
 * Class: Twix_AgentSignature
 * 
 * Purpose: Provides a summary page of services done and tag details. Also allows the user to input a customer
 * 			signature. If the customer isn't available, the user can select from a predefined list. Also
 * 			allows the user to specify email addresses to send the service tag to.
 * 
 * Note:	Signatures are captured in a pop-up that contains a drawable canvas-bitmap.
 * 
 * Relevant XML: signature.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSignature extends Activity
	{
	private Twix_Application app;
	private Twix_AgentOpenTag act;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext = this;
	private ArrayAdapter<CharSequence> adapter;
	private ArrayAdapter<CharSequence> adapterEmails;
	private String serviceTagId;
	private List<TextView> tvs;
	private Spinner spinner;
	private LinearLayout ll;
	private TableLayout bill_table;
	private EditText emailList;
	private boolean hideLabor = false;
	
	public Bitmap currSig;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.signature, null);
		this.setContentView( viewToLoad );
		emailList = (EditText)findViewById(R.id.EmailList);
		emailList.setSingleLine(false);
		
		bill_table = (TableLayout) findViewById(R.id.AltBillingTable);
		ll = (LinearLayout) findViewById(R.id.ServiceUnitBuild);
		spinner = (Spinner) findViewById(R.id.NoSignatureReason);
		buildAdapters();
		
		serviceTagId = getIntent().getExtras().getString("serviceTagId");
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		LocalActivityManager manager = ((Twix_AgentActivityGroup2)mContext).getLocalActivityManager();
		LocalActivityManager manager2 = ((Twix_AgentOpenTagsTabHost)manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		act = (Twix_AgentOpenTag) manager2.getActivity("Tag");
		
		tvs = new ArrayList<TextView>();
		tvs.add( (TextView)findViewById(R.id.Text_SiteName)		);
		tvs.add( (TextView)findViewById(R.id.Text_BatchNo)		);
		tvs.add( (TextView)findViewById(R.id.Text_JobNo)		);
		tvs.add( (TextView)findViewById(R.id.Text_ServiceType)	);
		tvs.add( (TextView)findViewById(R.id.Text_Disposition)	);
		tvs.add( (TextView)findViewById(R.id.Text_Completed)	);
		tvs.add( (TextView)findViewById(R.id.Text_Address1)		);
		tvs.add( (TextView)findViewById(R.id.Text_Address2)		);
		tvs.add( (TextView)findViewById(R.id.Text_City)			);
		tvs.add( (TextView)findViewById(R.id.Text_State)		);
		tvs.add( (TextView)findViewById(R.id.Text_Zip)			);
		
		readOnlySetup();
		
		readServiceTag();
		currSig = readSQL();
		((ImageView)findViewById(R.id.SignatureHolder)).setImageBitmap(currSig);
		}
	
	private void readOnlySetup()
		{
		if( act.tagReadOnly )
			{
			((Spinner)findViewById(R.id.NoSignatureReason)).setEnabled(false);
			((EditText)findViewById(R.id.EmailList)).setEnabled(false);
			((Spinner)findViewById(R.id.Spinner_EmailList)).setEnabled(false);
			}
		}
	
	private void buildAdapters()
		{
		adapter = new ArrayAdapter<CharSequence>(getParent().getParent(), R.layout.spinner_layout);
		adapter.setDropDownViewResource(R.layout.spinner_popup);
		adapter.add("");
		adapter.add("Customer not available");
		adapter.add("Off site management");
		adapter.add("Confirmed via phone");
		adapter.add("Left voicemail for customer");
		adapter.add("Progress service tag");
		
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
				{
				if( (arg2 > 0) || act.tagReadOnly )
					{
					findViewById(R.id.SignatureHolder).setVisibility(View.GONE);
					findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
			        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.GONE);
			        currSig = null;
					}
				else
					{
					//findViewById(R.id.SignatureHolder).setVisibility(View.VISIBLE);
					// Check if the tag ID is null, that way we know the selection is the first call
					if( serviceTagId != null )
						{
						if( currSig == null )
							{
							findViewById(R.id.SignatureHolder).setVisibility(View.GONE);
							findViewById(R.id.CustomerSignature).setVisibility(View.VISIBLE);
					        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.GONE);
							}
						else
							{
							findViewById(R.id.SignatureHolder).setVisibility(View.VISIBLE);
							findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
					        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.VISIBLE);
							}
						}
					}
				}
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				//Do Nothing
				}
			});
		
		adapterEmails = new ArrayAdapter<CharSequence>(getParent().getParent(), R.layout.spinner_layout);
		adapterEmails.setDropDownViewResource(R.layout.spinner_popup);
		
		Spinner sp = (Spinner)findViewById(R.id.Spinner_EmailList);
		sp.setAdapter(adapterEmails);
		sp.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0,
					View arg1, int arg2, long arg3)
				{
				if( arg2 > 0 )
					{
					String s = (String)arg0.getSelectedItem();
					String cur = emailList.getText().toString();
					arg0.setSelection(0);
					if( cur.length() > 0 )
						s = emailList.getText() + ", " + s;
					emailList.setText(s);
					}
				}
			
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				// Do Nothing...
				}
			});
		}
	
	private void buildEmailList()
		{
		adapterEmails.clear();
		adapterEmails.add("");
		String sqlQ = "SELECT email FROM serviceAddressContact where serviceAddressId = '" + act.serviceAddressId + "'" +
					" AND email IS NOT NULL";
		Cursor cursor = db.rawQuery(sqlQ);
		String s;
		if (cursor.moveToFirst())
			{
			do
				{
				s = cursor.getString(0);
				if( s.length() > 0 )
					adapterEmails.add(s);
				}
		    	while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void readServiceTag()
		{
		String sqlQ = "SELECT " +
		    	
	    			"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
					"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.siteName) ELSE ( " +
						"select serviceAddress.siteName from serviceAddress " +
							"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																		") ELSE (serviceAddress.siteName) END AS siteName, " +
						
					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.batchNo ) ELSE ( dispatch.batchNo ) END AS batchNo, " +
					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.jobNo ) ELSE ( substr(dispatch.jobNo, 5) ) END AS jobNo, " +
					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.serviceType ) ELSE ( dispatch.contractType ) END AS serviceType, " +
					
    				"CASE WHEN openServiceTag.disposition = 'C' THEN ('Call Complete') ELSE (" +
						"CASE WHEN openServiceTag.disposition = 'R' THEN ('Must Return') ELSE (openServiceTag.disposition) END) END AS disposition, " +
					
					"CASE WHEN openServiceTag.completed = 'Y' THEN ('Marked to Submit') ELSE ('Not Complete') END AS completed, " +
					
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
																	
					"openServiceTag.billTo, openServiceTag.billAddress1, openServiceTag.billAddress2, " +
					"openServiceTag.billAddress3, openServiceTag.billAddress4, " +
					"openServiceTag.billAttn, openServiceTag.emailList " +
					
				"FROM	openServiceTag " +
					"LEFT OUTER JOIN dispatch " +
						"on openServiceTag.dispatchId = dispatch.dispatchId " +
					"LEFT OUTER JOIN serviceAddress " +
						"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
				
				"WHERE openServiceTag.serviceTagId = '" + serviceTagId + "' ";
		
		Cursor cursor = db.rawQuery(sqlQ);
		List<String> list = new ArrayList<String>();
		int size = cursor.getColumnCount();
		if (cursor.moveToFirst())
			{
			do
				{
				int i = 0;
				int tvsSize = tvs.size();
				for(; i < tvsSize; i++ )
					{
					tvs.get(i).setText( Twix_TextFunctions.clean(cursor.getString(i)) );
					}
				
				// Optional Alternate Bill To Info
				String s = Twix_TextFunctions.clean( cursor.getString(i++) );
				
				if( s.length() > 0)
					{
					list.add( s );
					for(; i < size-1; i++ )
						{
						list.add( Twix_TextFunctions.clean( cursor.getString(i) ) );
						}
					
					createBillTo(list);
					}
				
				emailList.setText(cursor.getString(size-1));
				
				}
		    	while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	
		}
	
	private void createBillTo(List<String> list)
		{
		bill_table.removeAllViews();
		bill_table.setBackgroundColor(Twix_Theme.tableBG);
		TableRow row = new TableRow(this);
		
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.weight = 1;
		params.width = 0;
		
		TableRow.LayoutParams paramsLeft = new TableRow.LayoutParams();
		paramsLeft.weight = 1;
		paramsLeft.width = 0;
		
		TableRow.LayoutParams paramsRight = new TableRow.LayoutParams();
		paramsRight.weight = 1;
		paramsRight.width = 0;
		
		TableRow.LayoutParams paramsNoWeight = new TableRow.LayoutParams();
		paramsNoWeight.weight = 0;
		paramsNoWeight.width = LayoutParams.WRAP_CONTENT;
		
		TableRow.LayoutParams params2Weight = new TableRow.LayoutParams();
		params2Weight.weight = 2;
		params2Weight.width = 0;
		
		int[] bg = { Twix_Theme.headerBG };
		
		row.addView( createTextView( "Alternate Billing", Twix_Theme.headerText, params, bg) );
		bill_table.addView(row);
		
		row = new TableRow(this);
		
		row.addView( createTextView( "Bill to: ", Twix_Theme.headerText, params) );
		row.addView( createTextView( list.get(0), Twix_Theme.headerValue, params ) );
		row.addView( createTextView( "Address: ", Twix_Theme.headerText, params ) );
		row.addView( createTextView( " ", 0x00000000, params ) );
		
		bill_table.addView(row);
		row = new TableRow(this);
		
		row.addView( createTextView( "Attention Reason: ", Twix_Theme.headerText, params ) );
		row.addView( createTextView( list.get(5), Twix_Theme.headerValue, params ) );
		
		String s = list.get(1);
		if( list.get(2).length() > 0 )
			s += " " + list.get(2);
		s += " " + list.get(3) + ", " + list.get(4);
		
		row.addView( createTextView( s, Twix_Theme.headerValue, params2Weight ) );
		
		bill_table.addView(row);
		}
	
	private void readServiceUnit()
		{
		ll.removeAllViews();
		String sqlQ = "SELECT serviceTagUnit.serviceTagUnitId, " +
				"equipmentCategory.categoryDesc || ' - ' || equipment.UnitNo, " +
				"equipmentCategory.equipmentCategoryId, equipment.manufacturer, equipment.serialNo, " +
				"equipment.Model, serviceTagUnit.servicePerformed, serviceTagUnit.comments, " +
				"equipment.serviceAddressId " + 
			"FROM serviceTagUnit " +
				"LEFT OUTER JOIN equipment on equipment.equipmentId = serviceTagUnit.equipmentId " +
				"LEFT OUTER JOIN equipmentCategory on equipmentCategory.equipmentCategoryId = equipment.equipmentCategoryId " +
			"WHERE serviceTagUnit.serviceTagId = '" + serviceTagId + "'";
		List<String> listUnit = new ArrayList<String>();
		List<List<String>> unitLabor;
		List<List<String>> unitMaterial;
		Cursor cursor = db.rawQuery(sqlQ);
		int size = cursor.getColumnCount();
		String s = "";
		
		if (cursor.moveToFirst())
			{
			do
				{
				for( int i = 0; i < size; i++)
					{
					s = Twix_TextFunctions.clean( cursor.getString(i) );
					if( i == 1)
						{
						if( s.contentEquals("") || s.contentEquals("0") )
							s = "No Category";
						}
					listUnit.add(s);
					}
				
				unitLabor = getLabor( cursor.getString(0) );
				unitMaterial = getMaterials( cursor.getString(0) );
				
				buildServiceUnit(listUnit, unitLabor, unitMaterial);
				listUnit.clear();
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private List<List<String>> getLabor( String serviceTagUnitId )
		{
		List<List<String>> ret = new ArrayList<List<String>>();
		List<String> list;
		String sqlQ = "SELECT serviceLabor.serviceDate, serviceLabor.regHours, " +
					"serviceLabor.thHours, serviceLabor.dtHours, mechanic.mechanic_name " +
				"FROM serviceLabor " +
					"LEFT OUTER JOIN mechanic on mechanic.mechanic = serviceLabor.mechanic " +
				"WHERE serviceLabor.serviceTagUnitId = '" + serviceTagUnitId + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		int size = cursor.getColumnCount();
		
		if (cursor.moveToFirst())
			{
			do
				{
				list = new ArrayList<String>();
				for( int i = 0; i < size; i++)
					{
					list.add( Twix_TextFunctions.clean(cursor.getString(i)) );
					}
				ret.add(list);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private List<List<String>> getMaterials( String serviceTagUnitId )
		{
		List<List<String>> ret = new ArrayList<List<String>>();
		List<String> list;
		String sqlQ = "SELECT serviceMaterial.quantity, serviceMaterial.materialDesc " +
				"FROM serviceMaterial " +
				"WHERE serviceMaterial.serviceTagUnitId = '" + serviceTagUnitId + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		int size = cursor.getColumnCount();
		
		if (cursor.moveToFirst())
			{
			do
				{
				list = new ArrayList<String>();
				for( int i = 0; i < size; i++)
					{
					list.add( Twix_TextFunctions.clean(cursor.getString(i)) );
					}
				ret.add(list);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private void buildServiceUnit(List<String> list, List<List<String>> unitLabor, List<List<String>> unitMaterial)
		{
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.width = LayoutParams.FILL_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.setMargins(3, 3, 3, 3);
		
    	TableLayout tl = new TableLayout(this);
    	tl.setLayoutParams(params);
		tl.setBackgroundColor(Twix_Theme.tableBG);
		
		TableRow row = new TableRow(this);
		
		params = new TableRow.LayoutParams();
		params.weight = 1;
		params.height = LayoutParams.FILL_PARENT;
		params.width = 0;
		
		TableRow.LayoutParams paramsLeft = new TableRow.LayoutParams();
		paramsLeft.weight = 1;
		paramsLeft.height = LayoutParams.FILL_PARENT;
		paramsLeft.width = 0;
		
		TableRow.LayoutParams paramsRight = new TableRow.LayoutParams();
		paramsRight.weight = 1;
		paramsRight.height = LayoutParams.FILL_PARENT;
		paramsRight.width = 0;
		
		TableRow.LayoutParams paramsNoWeight = new TableRow.LayoutParams();
		paramsNoWeight.weight = 0;
		paramsNoWeight.height = LayoutParams.FILL_PARENT;
		paramsNoWeight.width = LayoutParams.WRAP_CONTENT;
		
		TableRow.LayoutParams paramsBn = new TableRow.LayoutParams();
		paramsBn.height = 38;
		paramsBn.width = 38;
		paramsBn.setMargins(10, 10, 10, 10);
		
		TableRow.LayoutParams params2Weight = new TableRow.LayoutParams();
		params2Weight.weight = 2;
		params2Weight.height = LayoutParams.FILL_PARENT;
		params2Weight.width = 0;
		
		int[] bg = { Twix_Theme.headerBG };
		
		row.addView( createTextView( "Equipment: ", Twix_Theme.headerText, params, bg) );
		
		String eqCat = list.get(2);
		
		if( (!eqCat.contentEquals("0")) && (eqCat.length() > 0) )
			{
			row.addView( createTextView( list.get(1), Twix_Theme.headerValue, params, bg) );
			row.addView( createTextView( "Serial No: ", Twix_Theme.headerText, params, bg ) );
			row.addView( createTextView( list.get(3), Twix_Theme.headerValue, params, bg) );
			
			tl.addView(row);
			row = new TableRow(this);
	
			// End edit button
			row.addView( createTextView( "", 0x00000000, paramsNoWeight) );
			row.addView( createTextView( "Manufacturer: ", Twix_Theme.headerText, params) );
			row.addView( createTextView( list.get(3), Twix_Theme.headerValue, params) );
			row.addView( createTextView( "Model: ", Twix_Theme.headerText, params) );
			row.addView( createTextView( list.get(5), Twix_Theme.headerValue, params) );
			
			tl.addView(row);
			}
		else
			{
			row.addView( createTextView( "Not Specified", Twix_Theme.headerValue, params, bg) );
			row.addView( createTextView( "", Twix_Theme.headerValue, params, bg) );
			row.addView( createTextView( "", Twix_Theme.headerValue, params, bg) );
			tl.addView(row);
			}
		
		row = new TableRow(this);
		
		row.addView( createTextView( "", 0x00000000, paramsNoWeight) );
		row.addView( createTextView( "Service Performed: ", Twix_Theme.headerText, params2Weight) );
		row.addView( createTextView( "Comments: ", Twix_Theme.headerText, params2Weight) );
		
		tl.addView(row);
		row = new TableRow(this);
		
		row.addView( createTextView( "", 0x00000000, paramsNoWeight) );
		row.addView( createTextView( list.get(6), Twix_Theme.headerValue, params2Weight) );
		row.addView( createTextView( list.get(7), Twix_Theme.headerValue, params2Weight) );
		
		tl.addView(row);
		
		ll.addView(tl);
		
		TableLayout details = laborTable(unitLabor);
		if( details != null )
			tl.addView( details );
		
		details = materialTable(unitMaterial);
		if( details != null )
			tl.addView( details );
		
		}
	
	private TableLayout laborTable(List<List<String>> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		int size2 = list.get(0).size();
		if( size2 < 1 )
			return null;
		
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
		params.setMargins(12, 3, 12, 3);
		
		TableLayout tl = new TableLayout(this);
		tl.setLayoutParams(params);
		tl.setBackgroundColor(Twix_Theme.tableBG2); // TODO - Create a Twix_Theme for table color 2
		
		
		TableRow row = new TableRow(this);
		
		TableRow.LayoutParams params2 = new TableRow.LayoutParams();
		params2.weight = 1;
		params2.height = LayoutParams.FILL_PARENT;
		params2.width = LayoutParams.WRAP_CONTENT;
		
		row.addView( createTextView( "Date", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
		if( !hideLabor )
			{
			row.addView( createTextView( "Regular Hours", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
			row.addView( createTextView( "Time and a Half Hours", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
			row.addView( createTextView( "Double Time Hours", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
			}
		row.addView( createTextView( "Mechanic", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
		tl.addView(row);
		for( int i = 0; i < size; i++ )
			{
			row = new TableRow(this);
			for( int j = 0; j < size2; j++ )
				{
				if( !hideLabor || (j == 0 || j == 4) )
					row.addView( createTextView( list.get(i).get(j) , Twix_Theme.headerValue, params2) );
				}
			tl.addView(row);
			}
		
		return tl;
		}
	
	private TableLayout materialTable(List<List<String>> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		int size2 = list.get(0).size();
		if( size2 < 1 )
			return null;
		
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
		params.setMargins(12, 3, 12, 3);
		
		TableLayout tl = new TableLayout(this);
		tl.setLayoutParams(params);
		tl.setBackgroundColor(Twix_Theme.tableBG2); // TODO - Create a Twix_Theme for table color 2
		
		TableRow row = new TableRow(this);
		
		TableRow.LayoutParams params2 = new TableRow.LayoutParams();
		params2.weight = 1;
		params2.height = LayoutParams.FILL_PARENT;
		params2.width = LayoutParams.WRAP_CONTENT;
		
		row.addView( createTextView( "Material Quantity", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
		row.addView( createTextView( "Material Description", Twix_Theme.headerValue, params2, Twix_Theme.headerBG) );
		tl.addView(row);
		for( int i = 0; i < size; i++ )
			{
			row = new TableRow(this);
			for( int j = 0; j < size2; j++ )
				row.addView( createTextView( list.get(i).get(j) , Twix_Theme.headerValue, params2) );
			tl.addView(row);
			}
		
		return tl;
		}
	
	private TextView createTextView(String s, int color, TableRow.LayoutParams params, int... bg )
		{
		TextView tv = new TextView(this);
		
		tv.setLayoutParams(params);
		tv.setTextSize(Twix_Theme.headerSize);
		tv.setText(s);
		tv.setTextColor(color);
		tv.setPadding(5, 5, 5, 5);
		if( bg.length > 0 )
			{
			tv.setBackgroundColor(bg[0]);
			}
		
		return tv;
		}
	
	public void sigPopup()
		{
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.signature_popup, null);
    	final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		final Panel p = new Panel(mContext, currSig);
		((LinearLayout)viewToLoad.findViewById(R.id.SignatureHolder)).addView(p);
		
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialog.setContentView(viewToLoad);
		dialog.show();
		
		
		((Button)dialog.findViewById(R.id.Save)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				currSig = p.mBitmap;
				ImageView iv = (ImageView)findViewById(R.id.SignatureHolder);
				iv.setImageBitmap(currSig);
				iv.setVisibility(View.VISIBLE);
		        updateDB();
		        findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
		        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.VISIBLE);
				dialog.dismiss();
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
		
		((Button)dialog.findViewById(R.id.ClearSig)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				p.clear();
				}
			});
		}
	
	private Bitmap readSQL()
		{
		String nosig = "";
		Bitmap bmp = null;
		byte[] bytes;
    	String sqlQ = "SELECT openServiceTag.signature, openServiceTag.nosignatureReason " +
    				"FROM openServiceTag " +
    			"WHERE openServiceTag.serviceTagId = '" + serviceTagId + "'";
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
			{
			do
				{
				bytes = cursor.getBlob(0);
				if( (bytes != null) && (spinner.getSelectedItemPosition() == 0) )
					{
					findViewById(R.id.SignatureHolder).setVisibility(View.VISIBLE);
					bmp = BitmapFactory.decodeByteArray( bytes, 0, bytes.length  );
					
					if( !act.tagReadOnly )
						{
						findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
				        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.VISIBLE);
						}
					else
						{
						findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
				        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.GONE);
						}
					}
				else
					{
					findViewById(R.id.SignatureHolder).setVisibility(View.GONE);
					nosig = cursor.getString(1);
					
					if( !act.tagReadOnly )
						{
						findViewById(R.id.CustomerSignature).setVisibility(View.VISIBLE);
				        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.GONE);
						}
					else
						{
						findViewById(R.id.CustomerSignature).setVisibility(View.GONE);
				        findViewById(R.id.CustomerSignature_Edit).setVisibility(View.GONE);
						}
					}
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		if( nosig != null )
			{
			if( nosig.length() > 0)
				{
				int size = adapter.getCount();
				for( int i = 0; i < size; i++ )
					{
					if( ((String)adapter.getItem(i)).contentEquals(nosig) )
						spinner.setSelection(i);
					}
				
				return null;
				}
			}
		
		return bmp;
		}
	
	class Panel extends SurfaceView implements SurfaceHolder.Callback
		{
		private ViewThread mThread;
		public Bitmap mBitmap;
        private Canvas mCanvas;
        private final Rect mRect = new Rect();
        private final Paint mPaint;
        private float mCurX;
        private float mCurY;
        private float pCurX;
        private float pCurY;
        
		Paint paint = new Paint();
		
		public Panel(Context context, Bitmap bmp)
			{
			super(context);
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			
			getHolder().addCallback(this);
			mThread = new ViewThread(this);
			this.setFocusable(true);
			this.setLayoutParams(params);
			
			mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            
            pCurX = -1;
            pCurY = -1;
            
            if( bmp != null )
				{
				mBitmap = bmp;
				}
			}
		
		public void doDraw(Canvas canvas)
			{
			if (mBitmap != null)
				{
				canvas.drawBitmap(mBitmap, 0, 0, null);
				}
			}
	
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
			{
			// TODO Auto-generated method stub
			}
	
		@Override
		public void surfaceCreated(SurfaceHolder holder)
			{
			if (!mThread.isAlive())
				{
				mThread = new ViewThread(this);
				mThread.setRunning(true);
				mThread.start();
				}
			}
	
		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
			{
			if (mThread.isAlive())
				{
				mThread.setRunning(false);
				}
			}
		
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh)
			{
			Bitmap newBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
			newBitmap.eraseColor(Color.WHITE);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (mBitmap != null)
				{
				newCanvas.drawBitmap(mBitmap, 0, 0, null);
				}
			
			mBitmap = newBitmap;
			mCanvas = newCanvas;
			}

		@Override
		protected void onDraw(Canvas canvas)
			{
			if (mBitmap != null)
			 {
			 mPaint.setARGB(255, 0, 0, 0);
			 canvas.drawBitmap(mBitmap, 0, 0, null);
			 }
			}
		
		public void clear()
			{
	        if (mCanvas != null)
	        	{
	            mPaint.setARGB(0xff, 255, 255, 255);
	            mCanvas.drawPaint(mPaint);
	            invalidate();
	        	}
			}

		@Override
		public boolean onTouchEvent(MotionEvent event)
			{
			int action = event.getActionMasked();
            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL)
            	{
            	int N = event.getHistorySize();
            	int P = event.getPointerCount();
            	for (int i = 0; i < N; i++)
            		{
            		for (int j = 0; j < P; j++)
            			{
            			mCurX = event.getHistoricalX(j, i);
            			mCurY = event.getHistoricalY(j, i);
            			drawPoint(mCurX, mCurY);
	                    }
            		}
            	for (int j = 0; j < P; j++)
            		{
            		mCurX = event.getX(j);
            		mCurY = event.getY(j);
            		drawPoint(mCurX, mCurY);
            		}
            	}
            if( action == MotionEvent.ACTION_UP )
            	{
            	pCurX = -1;
            	pCurY = -1;
            	}
            return true;
			}
		
		private void drawPoint(float x, float y)
			{
		    if (mBitmap != null)
		    	{
		    	float radius = 1.5f;
		    	mPaint.setARGB(255, 0, 0, 0);
		    	mCanvas.drawCircle(x, y, radius, mPaint);
		    	if( pCurX >= 0  && pCurY >= 0)
		    		{
			    	mCanvas.drawLine(pCurX, pCurY, x, y, mPaint);
			    	mCanvas.drawLine(pCurX-1, pCurY, x-1, y, mPaint);
			    	mCanvas.drawLine(pCurX+1, pCurY, x+1, y, mPaint);
			    	mCanvas.drawLine(pCurX, pCurY-1, x, y-1, mPaint);
			    	mCanvas.drawLine(pCurX, pCurY+1, x, y+1, mPaint); 	
		    		}
			    pCurX = x;
			    pCurY = y;
			    	
		    	mRect.set((int) (x - radius - 2), (int) (y - radius - 2),
		    			(int) (x + radius + 2), (int) (y + radius + 2));
		    	invalidate(mRect);
		    	}
			}
		
		}
	
	@Override
	public void onResume()
		{
		super.onResume();
		
		buildEmailList();
		
		String contract = ((TextView)act.tvs.get(3)).getText().toString();
		hideLabor = ( (contract.contentEquals("PM")) || (contract.contentEquals("CONT")) );
		
		readServiceTag();
		readServiceUnit();
		}
	
	public void onDestroy()
		{
		if( !act.tagReadOnly )
			updateDB();
		
		super.onDestroy();
		}
	
	public void updateDB()
		{
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		if( spinner.getSelectedItemPosition() > 0 )
			{
			l1.add("nosignatureReason");	l2.add( (String)spinner.getSelectedItem() );
			l1.add("signature");			l2.add( null );
			db.update( "openServiceTag", l1, l2, "serviceTagId", serviceTagId );
			}
		else if( currSig != null )
			{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			currSig.compress(CompressFormat.PNG, 0, bos); 
			byte[] bytes = bos.toByteArray();
			db.update( "openServiceTag", "signature", bytes, "serviceTagId", serviceTagId );
			
			l1.add("nosignatureReason");	l2.add("");
			db.update( "openServiceTag", l1, l2, "serviceTagId", serviceTagId );
			}
		else
			{
			l1.add("nosignatureReason");	l2.add( "" );
			l1.add("signature");			l2.add( null );
			db.update( "openServiceTag", l1, l2, "serviceTagId", serviceTagId );
			}
		
		l1.clear(); l2.clear();
		l1.add("emailList"); l2.add( emailList.getText().toString() );
		
		db.update( "openServiceTag", l1, l2, "serviceTagId", serviceTagId );
		}
	
	class ViewThread extends Thread
		{
		private Panel mPanel;
		private SurfaceHolder mHolder;
		private boolean mRun = false;
		
		public ViewThread(Panel panel)
			{
			mPanel = panel;
			mHolder = mPanel.getHolder();
			}

		public void setRunning(boolean run)
			{
			mRun = run;
			}
		
		@Override
		public void run()
			{
			Canvas canvas = null;
			while (mRun)
				{
				canvas = mHolder.lockCanvas();
				if (canvas != null)
					{
					mPanel.doDraw(canvas);
					mHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	
	}
