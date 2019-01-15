package com.twix_agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

/*******************************************************************************************************************
 * Class: Twix_AgentOpenTag
 * 
 * Purpose: The primary data page for open tags. All changes made here are immediately posted to the database.
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
public class Twix_AgentOpenTag extends Activity
	{
	private Twix_AgentActivityGroup2 ActGroup;
	public boolean tagReadOnly = true;
	public boolean isPM = false;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Twix_AgentOpenTagsTabHost tabhost;
	public TextView currentSite;
	public int serviceTagId;
	public int serviceAddressId;
	public int dispatchId;
	public List<View> tvs;
	private Context mContext;
	public LinearLayout ll;
	public List<String> billing;
	public List<String> altBilling;
	
	private LayoutParams mParams;
	
	public ArrayAdapter<CharSequence> serviceTypeAdapter;
	public ArrayList<String> serviceTypeMirror;
	public ArrayAdapter<CharSequence> dispositionAdapter;
	public ArrayList<String> siteDetails;
	
	private OnClickListener ModifyRow;
	private OnClickListener AddForm;
	private OnMenuItemClickListener NewForm;
	private OnClickListener SubmitForm;
	
	// Current Form Displayed -> Prevents Multiples from opening
	private Twix_AgentFormDisplay FormDisplayed;
	
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		mContext = getParent().getParent();
		ActGroup = ((Twix_AgentActivityGroup2)mContext);
		tabhost = (Twix_AgentOpenTagsTabHost) getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.open_tag, null);
		this.setContentView( viewToLoad );
		
		currentSite = ((Twix_AgentOpenTagsTabHost)getParent()).currentSite;
		
		billing = new ArrayList<String>();
		altBilling = new ArrayList<String>();
		
		
		// Standard Layout Params
		mParams = new LayoutParams();
		mParams.width = LayoutParams.WRAP_CONTENT;
		mParams.height = LayoutParams.MATCH_PARENT;
		mParams.weight = 1;
		
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		siteDetails = new ArrayList<String>();
		
		buildAdapters();
		
		tvs = new ArrayList<View>();
		tvs.add( findViewById(R.id.Text_SiteName)	); //0
		tvs.add( findViewById(R.id.Text_BatchNo)	); //1
		tvs.add( findViewById(R.id.Text_JobNo)		); //2
		tvs.add( findViewById(R.id.Text_ServiceType)); //3
		tvs.add( findViewById(R.id.Text_Disposition)); //4
		tvs.add( findViewById(R.id.Text_Completed)	); //5
		tvs.add( findViewById(R.id.Text_Description)); //6
		tvs.add( findViewById(R.id.Text_Tenant)		); //7
		tvs.add( findViewById(R.id.Text_Address)		); //8
		//tvs.add( findViewById(R.id.Text_Xoi));
		ll = ( LinearLayout) findViewById(R.id.SectionBuild);
		
		serviceTagId = getIntent().getIntExtra("serviceTagId", 0);
		
		buildClickListeners();
		
		readSQL();
		
		readOnlySetup();
		}
	
	private void readOnlySetup()
		{
		if( tagReadOnly )
			{
			findViewById(R.id.TagButtons).setVisibility(View.GONE);
			((Spinner)findViewById(R.id.Text_Disposition)).setEnabled(false);
			findViewById(R.id.AddServiceUnit).setVisibility(View.INVISIBLE);
			
			}
		}
	
	private void buildAdapters()
		{
		serviceTypeAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_layout);
		serviceTypeAdapter.setDropDownViewResource(R.layout.spinner_popup);
		serviceTypeMirror = new ArrayList<String>();
		
		serviceTypeAdapter.add("");		serviceTypeMirror.add("");
		serviceTypeAdapter.add("SPEC");	serviceTypeMirror.add("SPEC");
		serviceTypeAdapter.add("PM");	serviceTypeMirror.add("PM");
		serviceTypeAdapter.add("CONT");	serviceTypeMirror.add("CONT");
		serviceTypeAdapter.add("T&M");	serviceTypeMirror.add("T&M");
		serviceTypeAdapter.add("NTE");	serviceTypeMirror.add("NTE");
		
		dispositionAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_layout);
		dispositionAdapter.setDropDownViewResource(R.layout.spinner_popup);
		dispositionAdapter.add("");
		dispositionAdapter.add("Must Return");
		dispositionAdapter.add("Call Complete");
		
		Spinner sp = ((Spinner)findViewById(R.id.Text_Disposition));
		sp.setAdapter(dispositionAdapter);
		sp.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
				{
				ContentValues cv = new ContentValues();
				
				
				switch(arg2)
					{
					case 0:
						cv.put("disposition", "");
						
						
						//cv.put("xoi_flag", xoi.getText().toString());
						break;
					case 1:
						cv.put("disposition", "R");
						
						//cv.put("xoi_flag", xoi.getText().toString());
						break;
					case 2:
						cv.put("disposition", "C");
						
						//cv.put("xoi_flag", xoi.getText().toString());
						break;
					}
				
				db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
				}
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				//Do Nothing
				}
			
			});
		}

	public void readSQL()
	    {
	    ll.removeAllViews();
	    setupTagDetails();
	    setupTagUnits();
	    tabhost.updateFooter(serviceTagId);
	    }
	
	public void setupTagDetails()
		{
		if( serviceTagId == 0 )
	    	{
	    	return;
	    	}
	    
		/**
		 * Select Output
		 * 0 - serviceAddressId		8 - dispatch description
		 * 1 - dispatchId			9 - address1
		 * 2 - siteName				10- address2
		 * 3 - batchNo				11- city
		 * 4 - jobNo				12- state
		 * 5 - serviceType			13- zip
		 * 6 - disposition			14- tenant
		 * 7 - completed
		 * 
		 * OLD:
		 * 
		 * Select Output
		 * 0 - serviceAddressId		13- buildingNo
		 * 1 - dispatchId			14- open tag billTo
		 * 2 - siteName				15- open tag billAddress1
		 * 3 - batchNo				16- open tag billAddress2
		 * 4 - jobNo				17- open tag billAddress3
		 * 5 - serviceType			18- open tag billAddress4
		 * 6 - disposition			19- open tag billAttn
		 * 7 - completed			20- bill Name
		 * 8 - address1				21- bill Address1
		 * 9 - address2				22- bill Address2
		 * 10- city					23- bill Address3
		 * 11- state				24- bill Address4
		 * 12- zip					//25- completed
		 * 
		 */
		
    	String sqlQ = "SELECT openServiceTag.serviceAddressId, openServiceTag.dispatchId, " +
    	
	    			"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
					"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.siteName) ELSE ( " +
						"select serviceAddress.siteName from serviceAddress " +
							"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
																		") ELSE (serviceAddress.siteName) END AS siteName, " +
					
    				"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.batchNo ) ELSE ( dispatch.batchNo ) END AS batchNo, " +
    				"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.jobNo ) ELSE ( substr(dispatch.jobNo, 5) ) END AS jobNo, " +
    				"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.serviceType ) ELSE ( dispatch.contractType ) END AS serviceType, " +
    				"openServiceTag.disposition, openServiceTag.completed, dispatch.description, " +
    				
					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.tenant ) " +
						"ELSE ( dispatch.tenant ) END AS tenant, " +
    				
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
																		") ELSE (serviceAddress.zip) END AS zip, openServiceTag.xoi_flag as xoi_flag " +
					
				"FROM	openServiceTag " +
					"LEFT OUTER JOIN dispatch " +
						"on openServiceTag.dispatchId = dispatch.dispatchId " +
					"LEFT OUTER JOIN serviceAddress " +
						"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
					"LEFT OUTER JOIN billing as billing " +
						"ON billing.CustomerID = dispatch.cusNo AND dispatch.altBillTo = '' " +
					"LEFT OUTER JOIN billing as altBilling " +
						"ON altBilling.CustomerID = dispatch.altBillTo AND dispatch.altBillTo != '' " +
				"WHERE openServiceTag.serviceTagId = '" + serviceTagId + "' ";
    	
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	String s = "", temp;
    	
		if (cursor.moveToFirst())
			{
			do
				{
				if( cursor.getString(7) == null || cursor.getString(7).contentEquals("N") )
					tabhost.readOnly = tagReadOnly = ( false || app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true) );
				
				serviceAddressId = cursor.getInt(0);
				dispatchId = cursor.getInt(1);
				setupButtons( serviceAddressId, dispatchId );
				String a = cursor.getString(10);
				 String b = cursor.getString(15);
				// siteDetails is used to pass on to the next tabhost, so we know
				//  the context of the service unit as we edit/add it
				siteDetails.add( Twix_TextFunctions.clean( cursor.getString(2) ) ); // Site Name
				siteDetails.add( Twix_TextFunctions.clean( cursor.getString(4) ) ); // JobNo
				siteDetails.add( Twix_TextFunctions.clean( cursor.getString(3) ) ); // BatchNo
				siteDetails.add( Twix_TextFunctions.clean( cursor.getString(10) ) ); // address1
				siteDetails.add( ( cursor.getString(15) ) ); // xoi
				currentSite.setText( siteDetails.get(0) );
			//	TextView xoi = (TextView)findViewById(R.id.Text_Xoi);
				//xoi.setText(b);
				s =  Twix_TextFunctions.clean( cursor.getString(9) );
				
				temp = Twix_TextFunctions.clean( cursor.getString(10) );
				if( temp.length() > 0 )
					s += " " + temp;
				
				temp = Twix_TextFunctions.clean( cursor.getString(11) );
				if( temp.length() > 0 )
					s += " " + temp;
				
				temp = Twix_TextFunctions.clean( cursor.getString(12) );
				if( temp.length() > 0 )
					s += " " + temp;
				
				temp = Twix_TextFunctions.clean( cursor.getString(13) );
				if( temp.length() > 0 )
					s += " " + temp;
				
				
				
				siteDetails.add(s); // Address:[ Address1][ Address2][ City][, State][ ZIP]
				
				// Static Tag Header Info
				int size = tvs.size();
				for( int i = 0; i < size; i++)
					{
					s = Twix_TextFunctions.clean( cursor.getString(i+2) );
					if( i == 3 )
						{
						isPM = s.contentEquals("PM");
						}
					if( i == 4 )
						{
						if( s.contentEquals("C") )
							((Spinner)tvs.get(i)).setSelection(2);
						else if( s.contentEquals("R") )
							((Spinner)tvs.get(i)).setSelection(1);
						else
							((Spinner)tvs.get(i)).setSelection(0);
						}
					else if( i == 5 )
						{
						if( s.contentEquals("Y") )
							((TextView)tvs.get(i)).setText("Marked to Submit");
						else
							((TextView)tvs.get(i)).setText("Not Complete");
							
						}
					else
						((TextView)tvs.get(i)).setText( s );
					}
				
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void setupButtons(int serviceAddressId, int dispatchId)
		{
		boolean sa = serviceAddressId == 0;
		boolean ds = dispatchId == 0;
		
		if( !sa )
			{
			((Button)findViewById(R.id.LinkServiceAddress)).setText("Unlink Site");
			}
		else
			((Button)findViewById(R.id.LinkServiceAddress)).setText("Link Site");
		
		if( !ds )
			{
			// Dispatch Button
			Button b = (Button)findViewById(R.id.LinkDispatch);
			b.setText("Unlink Dispatch");
			
			// Service Address Button
			b = (Button)findViewById(R.id.LinkServiceAddress);
			b.setTextColor(Twix_Theme.disabledColor);
			b.setBackgroundResource(R.drawable.button_bg_disabled);
			b.setEnabled(false);
			
			// Manual Button
			b = (Button)findViewById(R.id.LinkManual);
			b.setTextColor(Twix_Theme.disabledColor);
			b.setBackgroundResource(R.drawable.button_bg_disabled);
			b.setEnabled(false);
			
			
			}
		else
			{
			// Dispatch Button
			Button b = (Button)findViewById(R.id.LinkDispatch);
			b.setText("Link Dispatch");
			
			// Service Address Button
			b = (Button)findViewById(R.id.LinkServiceAddress);
			b.setTextColor(Twix_Theme.headerText);
			b.setBackgroundResource(R.drawable.button_bg);
			b.setEnabled(true);
			
			// Manual Button
			b = (Button)findViewById(R.id.LinkManual);
			b.setTextColor(Twix_Theme.headerText);
			b.setBackgroundResource(R.drawable.button_bg);
			b.setEnabled(true);
			
			b = (Button)findViewById(R.id.Xoi);
			b.setTextColor(Twix_Theme.disabledColor);
			b.setBackgroundResource(R.drawable.button_bg_disabled);
			b.setEnabled(false);
			
			
			}
		
		}
	
	private void setupTagUnits()
		{
		String sqlQ = "select * from (SELECT serviceTagUnit.serviceTagUnitId, " +
				"(equipmentCategory.categoryDesc || ' - ' || equipment.UnitNo) as unitDesc, " +
				"equipment.equipmentId, equipment.manufacturer, equipment.serialNo, " +
				"equipment.Model, serviceTagUnit.servicePerformed, serviceTagUnit.comments, " +
				"equipment.serviceAddressId " + 
			"FROM serviceTagUnit " +
				"LEFT OUTER JOIN equipment on equipment.equipmentId = serviceTagUnit.equipmentId " +
				"LEFT OUTER JOIN equipmentCategory on equipmentCategory.equipmentCategoryId = equipment.equipmentCategoryId " +
			"WHERE serviceTagUnit.serviceTagId = " + serviceTagId + " " +
					") ORDER BY serviceTagUnitId desc, unitDesc asc";
		Cursor cursor = db.rawQuery(sqlQ);
		String s = "";
		ServiceUnit unit;
		Map<Integer, ServiceUnit> UnitMap = new HashMap<Integer, ServiceUnit>();
		if (cursor.moveToFirst())
			{
			do
				{
				unit = new ServiceUnit();
				unit.serviceUnitId			= cursor.getInt(0);
				s = Twix_TextFunctions.clean( cursor.getString(1) );
				if( s.length() > 0 )
					{
					unit.unitDesc			= s;
					unit.noUnit 			= false;
					}
				else
					unit.unitDesc			= "Not Selected";
				
				unit.equipmentId			= cursor.getInt(2);
				unit.manufacturer			= Twix_TextFunctions.clean( cursor.getString(3) );
				unit.serialNo				= Twix_TextFunctions.clean( cursor.getString(4) );
				unit.model					= Twix_TextFunctions.clean( cursor.getString(5) );
				unit.servicePerformed		= Twix_TextFunctions.clean( cursor.getString(6) );
				unit.comments				= Twix_TextFunctions.clean( cursor.getString(7) );
				
				UnitMap.put(unit.serviceUnitId, unit);
				//buildServiceUnit(unit);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// TODO: Add Back in ", fd.DateEntered" and change fd.equipmentId = fd.serviceTagUnitId
		sqlQ = "SELECT fd.ParentId, fd.LinkId, fd.FormId, fd.FormDataId, f.Description, fd.DateEntered, fd.Completed " +
					"FROM FormData as fd " +
					"LEFT OUTER JOIN Form as f " +
						"ON f.FormId = fd.FormId " +
				"WHERE fd.ParentTable = 'ServiceTagUnit' AND fd.LinkTable = 'Equipment' AND fd.ParentId IN " +
				"(SELECT serviceTagUnitId FROM serviceTagUnit WHERE serviceTagId = " + serviceTagId + ")";
		cursor = db.rawQuery(sqlQ);
		int index;
		while (cursor.moveToNext())
			{
			index = 0;
			unit = UnitMap.get( cursor.getInt(0) );
			if( unit != null )
				unit.forms.add( new FormInfo(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
						cursor.getLong(3), cursor.getString(4),
						cursor.getString(5), cursor.getString(6)) );
			else
				Toast.makeText(mContext, "Error: Forms exists beyond listed service units. ID: '" +
						cursor.getInt(0) + "'", Toast.LENGTH_LONG);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		for( Iterator<Entry<Integer, ServiceUnit>> i = UnitMap.entrySet().iterator(); i.hasNext(); )
			{
			buildServiceUnit(i.next().getValue());
			}
		
		}
	
	private class ServiceUnit
		{
		int serviceUnitId = 0;
		int equipmentId = 0;
		String unitDesc = "";
		String manufacturer = "";
		String serialNo = "";
		String model = "";
		String servicePerformed = "";
		String comments = "";
		boolean noUnit = true;
		List<FormInfo> forms;
		
		public ServiceUnit()
			{
			forms = new ArrayList<FormInfo>();
			}
		}
	
	private class FormInfo
		{
		int FormId;
		long FormDataId;
		int serviceTagUnitId;
		int equipmentId;
		String DisplayText;
		String Date;
		String Completed;
		
		public FormInfo(int serviceTagUnitId, int equipmentId, int FormId, long FormDataId,
				String DisplayText, String Date, String Completed)
			{
			this.serviceTagUnitId = serviceTagUnitId;
			this.FormId = FormId;
			this.FormDataId = FormDataId;
			this.DisplayText = DisplayText;
			this.Date = Date;
			this.Completed = Completed;
			}
		}
	
	private void buildServiceUnit(ServiceUnit unit)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(3, 3, 3, 3);
		
		LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT );
		subParams.weight = 1;
		
		LinearLayout.LayoutParams paramsBN = new LinearLayout.LayoutParams(
				40, 40 );
		paramsBN.setMargins(10, 10, 10, 10);
		
		
		LinearLayout main = new LinearLayout(this);
		main.setLayoutParams(params);
		main.setBackgroundColor(Twix_Theme.tableBG);
		main.setOrientation(LinearLayout.VERTICAL);
		
		// Row 1
		LinearLayout sub = new LinearLayout(this);
		sub.setLayoutParams(params);
		sub.setBackgroundColor(Twix_Theme.headerBG);
		sub.setOrientation(LinearLayout.HORIZONTAL);
		
		if( !unit.noUnit )
			{
			sub.addView( createTextView( "Equipment: ",	Twix_Theme.headerText,	subParams) );
			sub.addView( createTextView( unit.unitDesc,	Twix_Theme.headerValue,	subParams) );
			sub.addView( createTextView( "Serial No: ",	Twix_Theme.headerText,	subParams) );
			sub.addView( createTextView( unit.serialNo,	Twix_Theme.headerValue,	subParams) );
			}
		else
			{
			LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
			
			sub.addView( createTextView( "Equipment: ",	Twix_Theme.headerText,	wrapParams) );
			sub.addView( createTextView( unit.unitDesc,	Twix_Theme.headerValue,	subParams) );
			}
		
		main.addView(sub);
		
		// Row 2
		sub = new LinearLayout(this);
		sub.setLayoutParams(params);
		sub.setOrientation(LinearLayout.HORIZONTAL);
		
		// ------ Element 1
		ImageButton bn = new ImageButton(this);
		bn.setImageResource(R.drawable.icon_edit);
		bn.setLayoutParams(paramsBN);
		bn.setBackgroundResource(R.drawable.button_bg);
		
		bn.setTag( unit );
		bn.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	Intent intent = new Intent(mContext, Twix_AgentServiceUnitTabHost.class);
            	
            	ServiceUnit unit = (ServiceUnit) v.getTag();
            	
            	intent.putExtra("serviceTagUnitId", unit.serviceUnitId );
            	intent.putExtra("pm", isPM );
            	Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)mContext;
                parentActivity.startChildActivity("Twix_AgentServiceUnitTabHost", intent);
                }
        	});
		sub.addView(bn);
		
		// ------ Element 2
		
    	TableLayout tl = new TableLayout(this);
    	tl.setLayoutParams(subParams);
		TableRow row = new TableRow(this);
		
		TableRow.LayoutParams rowParams2 = new TableRow.LayoutParams();
		rowParams2.weight = 2;
		rowParams2.height = LayoutParams.WRAP_CONTENT;
		rowParams2.width = 0;
		
		if( !unit.noUnit )
			{
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
			rowParams.weight = 1;
			rowParams.height = LayoutParams.WRAP_CONTENT;
			rowParams.width = 0;
			
			row = new TableRow(this);
			row.addView( createTextView( "Manufacturer: ", Twix_Theme.headerText, rowParams) );
			row.addView( createTextView( unit.manufacturer, Twix_Theme.headerValue, rowParams) );
			row.addView( createTextView( "Model: ", Twix_Theme.headerText, rowParams) );
			row.addView( createTextView( unit.model, Twix_Theme.headerValue, rowParams) );
			tl.addView(row);
			}
		
		row = new TableRow(this);
		row.addView( createTextView( "Service Performed: ", Twix_Theme.headerText, rowParams2) );
		row.addView( createTextView( "Comments: ", Twix_Theme.headerText, rowParams2) );
		tl.addView(row);
		row = new TableRow(this);
		row.addView( createTextView( unit.servicePerformed, Twix_Theme.headerValue, rowParams2) );
		row.addView( createTextView( unit.comments, Twix_Theme.headerValue, rowParams2) );
		tl.addView(row);
		
		sub.addView(tl);
		
		main.addView(sub);
		
		// Form ServiceTagUnit Handling
		sub = new LinearLayout(this);
		sub.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		sub.setOrientation(LinearLayout.VERTICAL);
		sub.setPadding(150, 0, 0, 0);
		
		
		// Add Button Row
		if( !tagReadOnly )
			sub.addView(CreateAddForm(unit));
		
		int size = unit.forms.size();
		for( int i = 0; i < size; i++ )
			{
			sub.addView( CreateFormEdit(unit.forms.get(i)) );
			}
		
		main.addView(sub);
		
		// Finally add the view to the Service Tag Unit listing
		ll.addView(main);
    	}
	
	private LinearLayout CreateAddForm(ServiceUnit unit)
		{
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(3, 3, 3, 3);
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setBackgroundResource(R.drawable.clickable_bg2);
		row.setPadding(4, 4, 4, 4);
		row.setTag(unit);
		row.setOnClickListener(AddForm);
		
		// Add the Image that looks like a button
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.plus);
		iv.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
		//row.addView(iv);
		
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		textParams.weight = 1;
	//	row.addView( createTextView( "Add a Form",	Twix_Theme.headerText,	textParams) );
		
		return row;
		}
	
	private LinearLayout CreateFormEdit(FormInfo fInfo)
		{
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(3, 3, 3, 3);
		
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setBackgroundResource(R.drawable.clickable_bg2);
		row.setPadding(4, 0, 4, 0);
		row.setTag(fInfo);
		row.setOnClickListener(ModifyRow);
		
		// Image that looks like the button. The whole row is clickable
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.icon_edit);
		iv.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
		iv.setBackgroundResource(R.drawable.button_bg);
		row.addView(iv);
		
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		textParams.weight = 1;
		TextView tv = createTextView( Twix_TextFunctions.ComplexToNormal(fInfo.Date) +
				" - " + fInfo.DisplayText,	Twix_Theme.headerText,	textParams);
		tv.setPadding(0, 4, 0, 4);
		row.addView(tv);
		
		// Create the Save/Submit Button
		LinearLayout submitButton = new LinearLayout(this);
		submitButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 30) );
		submitButton.setBackgroundResource(R.drawable.button_bg);
		submitButton.setGravity(Gravity.CENTER_VERTICAL);
		
		iv = null;
		if( fInfo.Completed == null )
			fInfo.Completed = "N";
		if( fInfo.Completed.contentEquals("N") )
			{
			iv = new ImageView(this);
			iv.setImageResource(R.drawable.icon_check);
			iv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
			tv = createTextView( "Submit", Twix_Theme.headerText, textParams);
			tv.setPadding(0, -5, 0, 0);
			}
		else if( fInfo.Completed.contentEquals("M") )
			{
			iv = new ImageView(this);
			iv.setImageResource(R.drawable.icon_redx);
			iv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
			tv = createTextView( "Cancel Submission", Twix_Theme.headerText, textParams);
			tv.setPadding(0, -5, 0, 0);
			}
		else
			{
			// TODO: Error Case
			}
		if( iv != null )
			submitButton.addView(iv);
		submitButton.addView(tv);
		submitButton.setTag(fInfo);
		if(!tagReadOnly)
			submitButton.setOnClickListener(SubmitForm);
		else
			submitButton.setEnabled(!tagReadOnly);
		
		row.addView(submitButton);
		
		return row;
		}
	
	private TextView createTextView(String s, int color, ViewGroup.LayoutParams params, int... bg )
		{
		TextView tv = new TextView(this);
		
		tv.setLayoutParams(params);
		tv.setTextSize(Twix_Theme.headerSize);
		tv.setText(s);
		tv.setTextColor(color);
		tv.setPadding(3, 3, 3, 3);
		if( bg.length > 0 )
			{
			tv.setBackgroundColor(bg[0]);
			}
		
		return tv;
		}
	
	private class ManualData
		{
		int serviceAddressId = 0;
		int dispatchId = 0;
		
		String siteName = "";
		String tenant = "";
		String serviceType = "";
		String batchNo = "";
		String jobNo = "";
		String address1 = "";
		String address2 = "";
		String city = "";
		String state = "";
		String zip = "";
		}
	
	private void buildClickListeners()
		{
		Button dispatch_bn = (Button) findViewById(R.id.LinkDispatch);
		Button Xoi_bn = (Button) findViewById(R.id.Xoi);

		Button serviceAddress_bn = (Button) findViewById(R.id.LinkServiceAddress);
		Button manual_bn = (Button) findViewById(R.id.LinkManual);
		ImageButton addUnit_bn = (ImageButton) findViewById(R.id.AddServiceUnitButton);
		
		OnClickListener dispatch = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( dispatchId == 0 )
					{
					View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.generic_table_list, null);
					
					final Dialog dialog = new Dialog(mContext);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					
					float weight[] = { 2f, 1.5f, 1f, 1f, 1f, 3f, 2f, 1f };
					
					LinearLayout header = (LinearLayout) viewToLoad.findViewById(R.id.HeaderLine);
					header.addView( createTV("Site Name", weight[0]) );
					header.addView( createTV("Tenant", weight[1]) );
					header.addView( createTV("BatchNo", weight[2]) );
					header.addView( createTV("JobNo", weight[3]) );
					header.addView( createTV("Date", weight[4]) );
					header.addView( createTV("Description", weight[5]) );
					header.addView( createTV("Address", weight[6]) );
					header.addView( createTV("Xoi", weight[7]) );
					addDispatchRows( (LinearLayout) viewToLoad.findViewById(R.id.DataList), dialog, weight );
					((Button)viewToLoad.findViewById(R.id.Cancel)).setOnClickListener(new OnClickListener()
						{
						@Override
						public void onClick(View v)
							{
							dialog.dismiss();
							}
						});
					
					
					dialog.setContentView(viewToLoad);
					dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					dialog.show();
					}
				else
					{
					ContentValues cv = new ContentValues();
					cv.put("dispatchId", 0);
					
					db.update("openServiceTag", cv, "serviceTagId", serviceTagId );
					readSQL();
					}
				}
			}
		;
		dispatch_bn.setOnClickListener(dispatch);
		
		OnClickListener serviceAddress = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( serviceAddressId == 0 )
					{
					View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.generic_table_list, null);
					
					final Dialog dialog = new Dialog(mContext);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					
					float weight[] = { 25, 25, 16, 20 };
					
					LinearLayout header = (LinearLayout) viewToLoad.findViewById(R.id.HeaderLine);
					header.addView( createTV("Site Name", weight[0]) );
					header.addView( createTV("Address", weight[1]) );
					header.addView( createTV("City", weight[2]) );
					header.addView( createTV("Building No", weight[3]) );
					
					addServiceAddressRows( (LinearLayout)viewToLoad.findViewById(R.id.DataList), dialog, weight );
					
					((Button)viewToLoad.findViewById(R.id.Cancel)).setOnClickListener(new OnClickListener()
						{
						@Override
						public void onClick(View v)
							{
							dialog.dismiss();
							}
						});
					
					dialog.setContentView(viewToLoad);
					dialog.show();
					}
				else
					{
					// Check if the tag has service units
					String sqlQ = "select serviceTagUnit.equipmentId from serviceTagUnit " +
		    				"WHERE serviceTagUnit.serviceTagId = " + serviceTagId + " " +
		    				"AND serviceTagUnit.equipmentId <> 0";
		    		Cursor cursor = db.rawQuery(sqlQ);
		    		
		    		boolean hasUnits = cursor.moveToFirst();
		    		if (cursor != null && !cursor.isClosed())
		    			cursor.close();
		    		
		    		// Check if the tag has blue units
		    		boolean hasBlue = false;
		    		Twix_AgentBlue blueAct = ((Twix_AgentBlue)tabhost.getLocalActivityManager().getActivity("Blue"));
		    		if( blueAct == null )
		    			{
			    		sqlQ = "select blue.blueId from blue " +
			    					"LEFT OUTER JOIN blueUnit on blueUnit.blueId = blue.blueId " +
			    				"WHERE blue.serviceTagId = " + serviceTagId + " " +
			    				"AND blueUnit.equipmentId <> 0";
			    		cursor = db.rawQuery(sqlQ);
			    		
			    		hasBlue = cursor.moveToFirst();
			    		
			    		if (cursor != null && !cursor.isClosed())
			    			{
			    			cursor.close();
			    			}
		    			}
		    		else
		    			{
		    			hasBlue = blueAct.hasEquipment();
		    			}
		    		
		    		
		    		
					if( hasUnits && hasBlue )
						{
						Toast.makeText(mContext, "Cannot Unlink. This open tag has service unit and blue information", Toast.LENGTH_LONG).show();
						}
					else if( hasUnits )
						{
						Toast.makeText(mContext, "Cannot Unlink. This open tag has service unit information", Toast.LENGTH_LONG).show();
						}
					else if( hasBlue )
						{
						Toast.makeText(mContext, "Cannot Unlink. This open tag has blue information", Toast.LENGTH_LONG).show();
						}
					else
						{
		        		ContentValues cv = new ContentValues();
		        		cv.put("serviceAddressId", 0);
		        		db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
		        		readSQL();
						}
					}
				}
			}
		;
		serviceAddress_bn.setOnClickListener(serviceAddress);
		
		OnClickListener manual = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				String sqlQ = "SELECT openServiceTag.serviceAddressId, openServiceTag.dispatchId, " +
				    	
			    			"openServiceTag.siteName, openServiceTag.tenant, openServiceTag.serviceType, " +
			    			"openServiceTag.batchNo, openServiceTag.jobNo, " +
			    			"openServiceTag.address1, openServiceTag.address2, " +
			    			"openServiceTag.city, openServiceTag.state, openServiceTag.zip " +
							
						"FROM	openServiceTag " +
							"LEFT OUTER JOIN dispatch " +
								"on openServiceTag.dispatchId = dispatch.dispatchId " +
							"LEFT OUTER JOIN serviceAddress " +
								"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
						"WHERE openServiceTag.serviceTagId = " + serviceTagId;
				
				Cursor cursor = db.rawQuery(sqlQ);
				ManualData data = new ManualData();
				
				int index;
				if (cursor.moveToFirst())
					{
					index = 0;
					data.serviceAddressId	= cursor.getInt(0);
					data.dispatchId			= cursor.getInt(1);
					data.siteName			= cursor.getString(2);
					data.tenant				= cursor.getString(3);
					data.serviceType		= cursor.getString(4);
					data.batchNo			= cursor.getString(5);
					data.jobNo				= cursor.getString(6);
					data.address1			= cursor.getString(7);
					data.address2			= cursor.getString(8);
					data.city				= cursor.getString(9);
					data.state				= cursor.getString(10);
					data.zip				= cursor.getString(11);
					}
				if (cursor != null && !cursor.isClosed())
					{
					cursor.close();
					}
				
				changeTagDetails(data);
				}
			}
		;
		manual_bn.setOnClickListener(manual);
		
		OnClickListener Xoi = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				//Integer Xoi_Val = 1;
				ServiceAddressData data = new ServiceAddressData();
				final TextView jobno = (TextView) findViewById(R.id.Text_JobNo);
				final TextView sitename = (TextView) findViewById(R.id.Text_SiteName);
				final TextView batchno = (TextView) findViewById(R.id.Text_BatchNo);
				final TextView address = (TextView) findViewById(R.id.Text_Address);
				 //final TextView Xoi_val = (TextView) findViewById(R.id.Text_Xoi);
				 
				 
				 AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage("Do you have photos and videos to share with Customer through Xoi?")
					       .setCancelable(false)
					       .setPositiveButton("Yes", new DialogInterface.OnClickListener()
					    	   {
					           public void onClick(DialogInterface dialog, int id)
					        	   {
					        	   //Xoi_val.setText("Y");	
									//buildAdapters();
					        	   ContentValues cv = new ContentValues();
					        	   //TextView xoi = (TextView)findViewById(R.id.Text_Xoi);
					        	   cv.put("xoi_flag", "Y");
					        	   db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
									String a = address.getText().toString();
									String j = jobno.getText().toString();
									String b = batchno.getText().toString();
									String s = sitename.getText().toString();
									  Intent intent = new Intent(getBaseContext(),WebLinks.class);
									  Bundle extras = new Bundle();
									  extras.putString("jobNo", j );
									  extras.putString("batchNo", b );
									  extras.putString("address1", a );
									  extras.putString("sitename", s );
									  intent.putExtras(extras);
									  startActivity(intent);
						          // finish();
						           }
						       })
					       .setNegativeButton("No", new DialogInterface.OnClickListener()
					    	   {
					           public void onClick(DialogInterface dialog, int id)
					        	   {
					        	 //  ContentValues cv = new ContentValues();
					        	  
					        	//   cv.put("xoi_flag", "N");
					        	//   db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
					               dialog.cancel();
						           }
						       });
					
					AlertDialog alert = builder.create();
					alert.show();
				 
				 
				
				 
				 
				 
				 
				/* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							getBaseContext());

						// set title
						alertDialogBuilder.setTitle("Your Title");

						// set dialog message
						alertDialogBuilder
							.setMessage("Click yes to exit!")
							.setCancelable(false)
							.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, close
									// current activity
								
								}
							  })
							.setNegativeButton("No",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									dialog.cancel();
								}
							});

							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();

							// show it
							alertDialog.show();
				  
							
				*/
				
				// intent.putExtra("jobNo", j );
				// intent.putExtra("batchNo", b );
				// intent.putExtra("serviceTagId", serviceTagId );
		        //    startActivity(intent);
				  
				 
				}
			}
		;
		Xoi_bn.setOnClickListener(Xoi);
		
		
		/*OnClickListener No_Xoi = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				 final TextView Xoi_val = (TextView) findViewById(R.id.Text_Xoi);
				 Xoi_val.setText("N");
				 ContentValues cv = new ContentValues();
	        	   TextView xoi = (TextView)findViewById(R.id.Text_Xoi);
	        	   cv.put("xoi_flag", "N");
	        	   db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
				}
			}
		;
		No_Xoi_bn.setOnClickListener(No_Xoi);
		*/
		
		
		OnClickListener addServiceUnit = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				addServiceUnit();
				}
			}
		;
		addUnit_bn.setOnClickListener(addServiceUnit);
		
		final Twix_AgentFormDisplay.ActivityCallback callback = new Twix_AgentFormDisplay.ActivityCallback()
			{
			@Override
			public void Refresh()
				{
				ActGroup.ButtonPressed = false;
				app.MainTabs.setTabState(!ActGroup.ButtonPressed);
				readSQL();
				}
			}
		;
		
		ModifyRow = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( ActGroup.ButtonPressed || app.MainTabs.getCurrentActivity() != ActGroup )
					return;
				ActGroup.ButtonPressed = true;
				app.MainTabs.setTabState(!ActGroup.ButtonPressed);
				
				if( FormDisplayed != null && FormDisplayed.isVisible())
					return;
				FormInfo fInfo = (FormInfo)v.getTag();
				
				Map<String, String> AttrFindIds = new HashMap<String, String>();
				//AttrFindIds.put("equipment", fInfo.equipmentId);
				//AttrFindIds.put("dispatch", dispatchId);
				
				FormDisplayed = new Twix_AgentFormDisplay();
				FormDisplayed.Setup(fInfo.FormId, AttrFindIds, fInfo.FormDataId,
						"ServiceTagUnit", fInfo.serviceTagUnitId, "Equipment", fInfo.equipmentId, 
						mContext, app, callback, (tagReadOnly || !fInfo.Completed.contentEquals("N") ));
				
				FormDisplayed.show(((Activity)mContext).getFragmentManager(), "FormDisplay");
				}
			}
		;
		
		AddForm = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( ActGroup.ButtonPressed || app.MainTabs.getCurrentActivity() != ActGroup )
					return;
				ServiceUnit unit = (ServiceUnit)v.getTag();
				BuildFormOptions(v, unit.serviceUnitId, unit.equipmentId);
				}
			}
		;
		
		NewForm = new OnMenuItemClickListener()
			{
			@Override
			public boolean onMenuItemClick(MenuItem item)
				{
				if( FormDisplayed != null && FormDisplayed.isVisible())
					return false;
				
				View v = item.getActionView();
				if( v != null )
					{
					if( ActGroup.ButtonPressed || app.MainTabs.getCurrentActivity() != ActGroup )
						return false;
					ActGroup.ButtonPressed = true;
					app.MainTabs.setTabState(!ActGroup.ButtonPressed);
					
					FormItem fItem = (FormItem) v.getTag();
					
					Map<String, String> AttrFindIds = new HashMap<String, String>();
					AttrFindIds.put("equipment", fItem.equipmentId+"");
					AttrFindIds.put("dispatch", dispatchId+"");
					AttrFindIds.put("serviceAddress", serviceAddressId+"");
					AttrFindIds.put("users", app.empno);
					
					FormDisplayed = new Twix_AgentFormDisplay();
					FormDisplayed.Setup(fItem.FormId, AttrFindIds, 0L,
							"ServiceTagUnit", fItem.serviceTagUnitId, "Equipment", fItem.equipmentId, 
							mContext, app, callback, tagReadOnly);
					
					FormDisplayed.show(((Activity)mContext).getFragmentManager(), "FormDisplay");
					}
				
				return false;
				}
			};
		
		SubmitForm = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( ActGroup.ButtonPressed || app.MainTabs.getCurrentActivity() != ActGroup )
					return;
				ActGroup.ButtonPressed = true;
				app.MainTabs.setTabState(!ActGroup.ButtonPressed);
				if( FormDisplayed != null && FormDisplayed.isVisible())
					return;
				
				FormInfo fInfo = (FormInfo) v.getTag();
				ContentValues cv = new ContentValues();
				TextView tv = (TextView) ((LinearLayout)v).getChildAt(1);
				ImageView iv = (ImageView) ((LinearLayout)v).getChildAt(0);
				boolean MarkedComplete = fInfo.Completed.contentEquals("M");
				if( !fInfo.Completed.contentEquals("Y") )
					{
					if( MarkedComplete)
						{
						cv.put("Completed", "N");
						iv.setImageResource(R.drawable.icon_check);
						fInfo.Completed = "N";
						tv.setText("Submit");
						}
					else
						{
						cv.put("Completed", "M");
						iv.setImageResource(R.drawable.icon_redx);
						fInfo.Completed = "M";
						tv.setText("Cancel Submission");
						}
					
					db.update("FormData", cv, "FormDataId", fInfo.FormDataId);
					}
				ActGroup.ButtonPressed = false;
				app.MainTabs.setTabState(!ActGroup.ButtonPressed);
				}
			}
		;
		}
	
	// Dispatch Linking
	private class DispatchData
		{
		int dispatchId;
		int serviceAddressId;
		
		String siteName;
		String tenant;
		String batchNo;
		String jobNo;
		String dateStarted;
		String description;
		String address1;
		String xoi_flag;
		}
	
	private void addDispatchRows( LinearLayout tl, final Dialog dialog, float weight[] )
		{
		String sqlQ = "SELECT dispatch.dispatchId, dispatch.serviceAddressId, " +
				"CASE WHEN ( dispatch.serviceAddressId > 0 ) " +
					"THEN ( serviceAddress.siteName) " +
					"ELSE ( dispatch.siteName ) END as siteName, " +
				"dispatch.tenant, " +
				"dispatch.batchNo, substr(dispatch.jobNo, 5), dispatch.dateStarted, " +
				"dispatch.description,dispatch.siteAddress1 as address1 " + 
				
			"from dispatch " +
				"LEFT OUTER JOIN serviceAddress " +
					"on serviceAddress.serviceAddressId = dispatch.serviceAddressId";
		
		if( !(serviceAddressId == 0) )
			sqlQ +=	" WHERE dispatch.serviceAddressId = " + serviceAddressId;

		
		
		Cursor cursor = db.rawQuery(sqlQ);
		if ( cursor.moveToFirst() )
			{
			do
				{
				DispatchData data = new DispatchData();
				data.dispatchId			= cursor.getInt(0);
				data.serviceAddressId	= cursor.getInt(1);
				data.siteName			= cursor.getString(2);
				data.tenant				= cursor.getString(3);
				data.batchNo			= cursor.getString(4);
				data.jobNo				= cursor.getString(5);
				data.dateStarted		= cursor.getString(6);
				data.description		= cursor.getString(7);
				data.address1			= cursor.getString(8);
				tl.addView( createDispatchRow(data, dialog, weight) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	private LinearLayout createDispatchRow( DispatchData data, final Dialog dialog, float weight[] )
		{
		LinearLayout.LayoutParams rowParams;
		rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		
		row.setTag(data);
		
		// Set the clickable background
		row.setBackgroundResource(R.drawable.clickable_bg);
		
		row.addView(createTV(data.siteName, weight[0] ));
		row.addView(createTV(data.tenant, weight[1] ));
		row.addView(createTV(data.batchNo, weight[2] ));
		row.addView(createTV(data.jobNo, weight[3] ));
		row.addView(createTV( Twix_TextFunctions.DBtoNormal(data.dateStarted), weight[4]) );
		row.addView(createTV(data.description, weight[5] ));
		
		row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	DispatchData data = (DispatchData) v.getTag();
            	dispatchId = data.dispatchId;
            	
        		ContentValues cv = new ContentValues();
        		cv.put("dispatchId", data.dispatchId);
        		cv.put("serviceAddressId", data.serviceAddressId);
        		
            	db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
            	dialog.dismiss();
            	readSQL();
                }
        	});
		
		return row;
		}
	
	// Service Address Linking
	private class ServiceAddressData
		{
		int serviceAddressId;
		String siteName;
		String address;
		String city;
		String buildingNo;
		}
	
	private void addServiceAddressRows( LinearLayout tl, final Dialog dialog, float weight[] )
		{
		String sqlQ = "SELECT serviceAddressId, " +
				"siteName, ( Address1 || ' ' || Address2 ) as address, " +
				"City, buildingNo " + 
				
			"from serviceAddress";
	
		Cursor cursor = db.rawQuery(sqlQ);
		
		if ( cursor.moveToFirst() )
			{
			ServiceAddressData data;
			do
				{
				data = new ServiceAddressData();
				data.serviceAddressId	= cursor.getInt(0);
				data.siteName			= cursor.getString(1);
				data.address			= cursor.getString(2);
				data.city				= cursor.getString(3);
				data.buildingNo			= cursor.getString(4);
				
				tl.addView( createServiceAddressRow(data, serviceTagId, weight, dialog) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	private LinearLayout createServiceAddressRow( ServiceAddressData data, final int serviceTagId, float weight[], final Dialog dialog )
		{
		LinearLayout.LayoutParams rowParams;
		rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		
		row.setTag( data );
		row.setBackgroundResource(R.drawable.clickable_bg);
		
		row.addView( createTV(data.siteName, weight[0]) );
		row.addView( createTV(data.address, weight[1]) );
		row.addView( createTV(data.city, weight[2]) );
		row.addView( createTV(data.buildingNo, weight[3]) );
		
		row.setOnClickListener(new  OnClickListener()
	    	{
	        @Override
	        public void onClick(View v)
	        	{
	        	ServiceAddressData data = (ServiceAddressData) v.getTag();
	        	serviceAddressId = data.serviceAddressId;
	        	
	        	ContentValues cv = new ContentValues();
	        	cv.put("serviceAddressId", data.serviceAddressId);
	        	
	        	db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
	        	dialog.dismiss();
	        	
	        	readSQL();
	            }
	    	});
		
		return row;
		}
	
	// Manual Enter change details
	public void changeTagDetails( final ManualData data )
		{
    	final boolean min = data.serviceAddressId == 0;
    	View viewToLoad;
    	
    	if( min )
    		viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_edit, null);
    	else
    		viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_edit_min, null);
    	
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		String pmVal = Twix_TextFunctions.clean(data.serviceType);
		
		int size = serviceTypeMirror.size(), i = 0;
		for( ; i < size; i++ )
			{
			if( serviceTypeMirror.get(i).contentEquals( pmVal ) )
				break;
			}
		
		if( min )
			{
			((EditText)viewToLoad.findViewById(R.id.Text_SiteName)).setText(data.siteName);
			((EditText)viewToLoad.findViewById(R.id.Text_Tenant)).setText(data.tenant);
			((Spinner)viewToLoad.findViewById(R.id.Text_ServiceType)).setAdapter(serviceTypeAdapter);
			((Spinner)viewToLoad.findViewById(R.id.Text_ServiceType)).setSelection(i);
			((EditText)viewToLoad.findViewById(R.id.Text_BatchNo)).setText(data.batchNo);
			((EditText)viewToLoad.findViewById(R.id.Text_JobNo)).setText(data.jobNo);
			((EditText)viewToLoad.findViewById(R.id.Text_Address1)).setText(data.address1);
			((EditText)viewToLoad.findViewById(R.id.Text_Address2)).setText(data.address2);
			((EditText)viewToLoad.findViewById(R.id.Text_City)).setText(data.city);
			((EditText)viewToLoad.findViewById(R.id.Text_State)).setText(data.state);
			((EditText)viewToLoad.findViewById(R.id.Text_Zip)).setText(data.zip);
			}
		else
			{
			((Spinner)viewToLoad.findViewById(R.id.Text_ServiceType)).setAdapter(serviceTypeAdapter);
			((Spinner)viewToLoad.findViewById(R.id.Text_ServiceType)).setSelection(i);
			((EditText)viewToLoad.findViewById(R.id.Text_BatchNo)).setText(data.batchNo);
			((EditText)viewToLoad.findViewById(R.id.Text_JobNo)).setText(data.jobNo);
			((EditText)viewToLoad.findViewById(R.id.Text_Tenant)).setText(data.tenant);
			}
		
		
		dialog.setContentView(viewToLoad);
		dialog.show();
		if( min )
			dialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, 700);
		else
			dialog.getWindow().setLayout(800, 600);
		
		((Button)dialog.findViewById(R.id.Save)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
		        View v = (View) v2.getParent().getParent();
		        
		        ManualData up_data = new ManualData();
		        up_data.serviceAddressId = data.serviceAddressId;
		        up_data.dispatchId = data.dispatchId;
		        
		        up_data.serviceType		= (String)((Spinner)v.findViewById(R.id.Text_ServiceType)).getSelectedItem();
		        up_data.batchNo			= ((EditText)v.findViewById(R.id.Text_BatchNo)).getText().toString();
		        up_data.jobNo			= ((EditText)v.findViewById(R.id.Text_JobNo)).getText().toString();
		        up_data.tenant			= ((EditText)v.findViewById(R.id.Text_Tenant)).getText().toString();
				if( min )
					{
					up_data.siteName	= ((EditText)v.findViewById(R.id.Text_SiteName)).getText().toString();
					up_data.address1	= ((EditText)v.findViewById(R.id.Text_Address1)).getText().toString();
					up_data.address2	= ((EditText)v.findViewById(R.id.Text_Address2)).getText().toString();
					up_data.city		= ((EditText)v.findViewById(R.id.Text_City)).getText().toString();
					up_data.state		= ((EditText)v.findViewById(R.id.Text_State)).getText().toString();
					up_data.zip			= ((EditText)v.findViewById(R.id.Text_Zip)).getText().toString();
					}
				
				updateTagDetails( up_data, min );
		    	setupTagDetails();
		        
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
		
		}
	
	private void updateTagDetails( ManualData data, boolean min )
		{
		ContentValues cv = new ContentValues();
		cv.put("serviceType", data.serviceType);
		cv.put("batchNo", data.batchNo);
		cv.put("jobNo", data.jobNo);
		cv.put("tenant", data.tenant);
		
		if( data.serviceAddressId == 0 )
			{
			cv.put("siteName", data.siteName);
			cv.put("address1", data.address1);
			cv.put("address2", data.address2);
			cv.put("city", data.city);
			cv.put("state", data.state);
			cv.put("zip", data.zip);
			}
		
		db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
		}
	
	// Add Service Unit 
	private void addServiceUnit()
		{
		int newId =  db.newNegativeId("serviceTagUnit", "serviceTagUnitId");
		
    	ContentValues cv = new ContentValues();
    	cv.put("serviceTagUnitId", newId );
    	cv.put("serviceTagId", serviceTagId );
    	cv.put("equipmentId", 0 );
		db.db.insertOrThrow("serviceTagUnit", null, cv );
		
	    Intent intent = new Intent(getParent(), Twix_AgentServiceUnitTabHost.class);
	    intent.putExtra("serviceTagUnitId", newId);
	    ((Twix_TabActivityGroup)mContext).startChildActivity("Twix_AgentServiceUnitTabHost", intent);
		}
	
	// Other Funcs
	private TextView createTV(String s, float weight)
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				0, LayoutParams.FILL_PARENT);
		params.setMargins(2, 2, 2, 2);
		params.weight = weight;
		
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setText(s);
		tv.setTextSize(Twix_Theme.subSize);
		tv.setTextColor(Twix_Theme.sub1Value);
		tv.setBackgroundColor(Twix_Theme.sub2BG);
		tv.setTypeface(Typeface.MONOSPACE);
		tv.setPadding(3, 10, 3, 10);
		tv.setSingleLine();
		
		return tv;
		}
	
	// Form Selection Classes & Functions
	private class MenuData
		{
		String DisplayText;
		List<FormItem> FormOptions;
		
		public MenuData(String display)
			{
			DisplayText = display;
			FormOptions = new ArrayList<FormItem>();
			}
		}
	
	private class FormItem
		{
		int FormId;
		int serviceTagUnitId;
		int equipmentId;
		String Description;
		
		public FormItem(int FormId, String Description, int serviceTagUnitId, int equipmentId)
			{
			this.FormId = FormId;
			this.Description = Description;
			this.serviceTagUnitId = serviceTagUnitId;
			this.equipmentId = equipmentId;
			}
		}
	
	public void BuildFormOptions(View v, int serviceTagUnitId, int equipmentId)
		{
		String sql = "SELECT f.FormId, f.Type, f.Description, ft.Description " +
						"FROM Form AS f " +
					"LEFT OUTER JOIN FormType AS ft " +
						"ON ft.FormType = f.Type " +
				"WHERE f.EquipmentCategoryId IN " +
				"(SELECT COALESCE(e.equipmentCategoryId, 0) FROM serviceTagUnit as su " +
					"LEFT OUTER JOIN equipment as e " +
						"ON e.equipmentId = su.equipmentId " +
					"WHERE su.serviceTagUnitId = " + serviceTagUnitId +") " +
				"AND f.VersionNum = (SELECT Max(VersionNum) FROM Form WHERE VersionId = f.VersionId)";
		Cursor cursor = app.db.rawQuery(sql);
		Map<String, MenuData> FormMenu = new HashMap<String, MenuData>();
		MenuData menuData;
		String Type;
		while( cursor.moveToNext() )
			{
			Type = cursor.getString(1);
			menuData = FormMenu.get(Type);
			if( menuData == null )
				{
				menuData = new MenuData(cursor.getString(3));
				FormMenu.put(Type, menuData);
				}
			menuData.FormOptions.add(new FormItem(cursor.getInt(0), cursor.getString(2), serviceTagUnitId, equipmentId) );
			}
		
		// Generate the Popup Menu Views
		PopupMenu popup = new PopupMenu(mContext, v);
		Menu menu = popup.getMenu();
		
		MenuItem menuItem;
		SubMenu subMenu;
		FormItem item;
		int size;
		Entry<String, MenuData> entry;
		View hackV;
		for( Iterator<Entry<String, MenuData>> i = FormMenu.entrySet().iterator(); i.hasNext(); )
			{
			entry = i.next();
			menuData = entry.getValue();
			subMenu = menu.addSubMenu(0, 0, 0, menuData.DisplayText);
			size = menuData.FormOptions.size();
			for( int j = 0; j < size; j++ )
				{
				item = menuData.FormOptions.get(j);
				menuItem = subMenu.add(0, 0, 0, item.Description);
				hackV = new View(mContext);
				hackV.setTag(item);
				menuItem.setActionView(hackV);
				}
			}
		
		if( menu.size() > 0 )
			{
		    MenuInflater inflater = popup.getMenuInflater();
		    inflater.inflate(R.menu.form_menu, popup.getMenu());
		    popup.setOnMenuItemClickListener(NewForm);
		    popup.show();
			}
		else
			{
			AlertDialog alert = new AlertDialog.Builder(mContext).create();
	    	alert.setTitle("No Forms are available." );
	    	alert.setMessage("Sorry, there are no Forms available for this Unit's Equipment Category." +
	    						" You can try syncing to download the latest Forms.");
	    	alert.setButton("Ok", new DialogInterface.OnClickListener()
	    		{  
	    		public void onClick(DialogInterface dialog, int which)
	    			{
	    			return;  
	    			}
	    		});
	    	alert.show();
			}
		}
	}
