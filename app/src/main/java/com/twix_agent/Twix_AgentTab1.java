package com.twix_agent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.twix.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentTab1
 * 
 * Purpose: Highest level activity for Tab 1 (Dispatches). This allows the user to select from a list of dispatches
 * 			and view their details. From those details they can create an open service tag.
 * 
 * Relevant XML: tab1.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentTab1 extends Activity
	{
	private boolean readOnly;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private LinearLayout tl;
	private Context mContext;
	private Twix_SQLite db;
	
	private int curCol = 0;
	private String CurrentSearch;
	private boolean desc = false;
	
	static final int PROGRESS_DIALOG = 0;
	private OnClickListener dispatchPopup;
	private OnClickListener colorCodePopup;
	private OnClickListener safetyapp;
	
	private OnClickListener assignMechanicPopup;
	private OnClickListener assignMechanicSelection;
	private OnClickListener navigationClick;
	
	// Summary Variables
	private Dialog SummaryDialog;
	private Dialog AssignDialog;
	
	// Filtering Variables
	private Dispatch_Filters filters;
	private Dialog filterDialog;
	private Dialog mechanicDialog;
	private TextView filterWarning;
	private TreeMap<Integer, String> ColorCoding;
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab1);
        
        ColorCoding = new TreeMap<Integer, String>();
        tl = (LinearLayout) findViewById(R.id.WorkFlowBuild);
        filterWarning = (TextView) findViewById(R.id.Warning_Filters);
        mContext = getParent();
        
        app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
    	clearBgs();
    	setClickListeners();
    	
    	Update_WorkFlow();
    	}
    
    private class DispatchData
	    {
	    int dispatchId;
	    String siteName;
	    String tenant;
	    String batchNo;
	    String jobNo;
	    String dateStarted;
	    String mech1;
	    String mech2;
	    String desc;
	    String disposition;
	    
	    Date dateOrdered;
	    }
    
    public void readSQL()
	    {
	    filters = readFilterPreferences();
	    if( filters.isFiltering() )
	    	filterWarning.setVisibility(View.VISIBLE);
	    else
	    	filterWarning.setVisibility(View.INVISIBLE);
    	tl.removeAllViews();
    	
    	// TODO: Change dateOrdered to dateStarted
    	String sqlQ = "SELECT dispatch.dispatchId, " +
				"CASE WHEN ( dispatch.serviceAddressId > 0 ) " +
					"THEN ( serviceAddress.siteName) " +
					"ELSE ( dispatch.siteName ) END as siteName, " +
				"dispatch.tenant, " +
				"dispatch.batchNo, substr(dispatch.jobNo, 5), " +
				"substr(dispatch.dateStarted, 1, 10), " +
				"m1.mechanic_name, m2.mechanic_name, " +
				"dispatch.description, " +
				
				"CASE WHEN ( EXISTS ( select 'Y' from serviceTag where (serviceTag.disposition = 'C') AND (serviceTag.dispatchId = dispatch.dispatchId) ) ) " +
					"THEN ( 'C' ) " +
				"ELSE ( " +
					"CASE WHEN ( EXISTS ( select 'Y' from serviceTag where (serviceTag.disposition = 'R') AND (serviceTag.dispatchId = dispatch.dispatchId) ) )  " +
						"THEN ( 'R' ) " +
					"ELSE ( 'N' ) END ) " +
				"END AS status, " +
				"dispatch.dateStarted " +
				
			"from dispatch " +
				"LEFT OUTER JOIN serviceAddress " +
					"on serviceAddress.serviceAddressId = dispatch.serviceAddressId " +
				"LEFT OUTER JOIN mechanic as m1 " +
					"on dispatch.mechanic1 = m1.mechanic " +
				"LEFT OUTER JOIN mechanic as m2 " +
					"on dispatch.mechanic2 = m2.mechanic ";
    	
    	if( filters != null )
    		{
    		sqlQ += "WHERE 1=1 ";
    		
    		if( !filters.NoTags )
    			sqlQ += "AND status != 'N' ";
    		
    		if( !filters.CallComplete )
    			sqlQ += "AND status != 'C' ";
    		
    		if( !filters.MustReturn )
    			sqlQ += "AND status != 'R' ";
    		
    		String mechanicList = "";
    		int size = filters.Mech1List.size();
    		if( filters.Mech1 && size > 0 )
    			{
    			for( int i = 0; i < size; i++ )
    				{
    				mechanicList += "'" + filters.Mech1List.get(i) + "'";
    				if( i+1 < size )
    					{
    					mechanicList += ", ";
    					}
    				}
    			
    			sqlQ += "AND dispatch.mechanic1 IN (" + mechanicList + ") ";
    			}
    		
    		mechanicList = "";
    		size = filters.Mech2List.size();
    		if( filters.Mech2 && size > 0 )
    			{
    			for( int i = 0; i < size; i++ )
    				{
    				mechanicList += "'" + filters.Mech2List.get(i) + "'";
    				if( i+1 < size )
    					{
    					mechanicList += ", ";
    					}
    				}
    			
    			sqlQ += "AND dispatch.mechanic2 IN (" + mechanicList + ") ";
    			}
    		
    		}
    	
    	sqlQ += "ORDER BY " + CurrentSearch;
    	if( !desc )
    		sqlQ += " asc";
    	else
    		sqlQ += " desc";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	DispatchData data;
    	int index;
		if (cursor.moveToFirst())
			{
			float weight[] = { 1.3f, 0.8f, 0.7f, 0.5f, 0.8f, 1.3f, 2f };
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			do
				{
				index = 0;
				data = new DispatchData();
				data.dispatchId			= cursor.getInt(0);
				data.siteName			= cursor.getString(1);
				data.tenant				= cursor.getString(2);
				data.batchNo			= cursor.getString(3);
				data.jobNo				= cursor.getString(4);
				data.dateStarted		= Twix_TextFunctions.DBtoNormal(cursor.getString(5));
				data.mech1				= formatMechName(cursor.getString(6));
				data.mech2				= formatMechName(cursor.getString(7));
				data.desc				= cursor.getString(8);
				data.disposition		= cursor.getString(9);
				try
					{
					data.dateOrdered = formatter.parse(cursor.getString(10));
					}
				catch ( Exception e )
					{
					data.dateOrdered = new Date();
					}
				
				createRow( data, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	    }
    
    public void SetupColorCoding()
    	{
    	ColorCoding.clear();
    	String sqlQ = "SELECT DaysLate, RGBColor " +
    			"FROM dispatchPriority ";
		Cursor cursor = db.rawQuery(sqlQ);
		
		while (cursor.moveToNext())
			ColorCoding.put(cursor.getInt(0), cursor.getString(1));
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
    	}
    
    private String formatMechName(String mech)
    	{
    	if( mech == null )
    		return "";
    	
    	int subindex, endindex, length;
    	subindex = mech.indexOf(",");
		if( subindex > 0 )
			{
			length = mech.length();
			String lastName = mech.substring(0, subindex);
			if( subindex+5 <= length )
				endindex = subindex+5;
			else
				endindex = length;
			String firstName = mech.substring(subindex+2, endindex);
			mech = lastName + ", " + firstName;
			}
		
		return mech;
    	}
    
    private void noResults()
    	{
    	LinearLayout row = new LinearLayout(this);
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 10, 10, 10);
		params.weight = 1;
		
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setGravity(Gravity.LEFT);
		tv.setText("No Dispatches Available. Try Syncing.");
		tv.setTextSize(Twix_Theme.headerSizeLarge);
		tv.setTextColor(Twix_Theme.headerValue);
		tv.setBackgroundColor(Twix_Theme.sub2BG);
		tv.setTypeface(Typeface.MONOSPACE);
		tv.setPadding(10, 10, 10, 10);
		row.addView(tv);
    	
		tl.addView(row);
    	}
    
    public void createRow(DispatchData data, float[] weight)
	    {
	    LinearLayout tl = (LinearLayout) findViewById(R.id.WorkFlowBuild);
	    LinearLayout row = new LinearLayout(this);
	    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
	    		LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    row.setLayoutParams(rowParams);
	    row.setOrientation(LinearLayout.HORIZONTAL);
    	row.setTag(data);
    	row.setBackgroundResource(R.drawable.clickable_bg);
    	
    	// Color Codings
    	boolean callComplete = (data.disposition != null) && (data.disposition.contentEquals("C"));
    	long timeOpen = System.currentTimeMillis() - data.dateOrdered.getTime();
    	int days = (int) (timeOpen / (1000*60*60*24));
    	Entry<Integer, String> entry = ColorCoding.floorEntry(days);
    	int bgColor = Twix_Theme.headerBG;
    	if( entry != null )
    		{
	    	String bgColorString = entry.getValue();
	    	try
	    		{
	    		bgColor = Integer.parseInt(bgColorString, 16)+0xFF000000;
	    		}
	    	catch( Exception e)
	    		{
	    		bgColor = Twix_Theme.headerBG;
	    		Log.e("twix_agent:Twix_AgentTab1", "Failed to parse Dispatch Date Ordered Background Color.");
	    		}
    		}
    	
    	row.addView(createTextView( data.siteName,		weight[0], true, callComplete, bgColor ));
    	row.addView(createTextView( data.tenant,		weight[1], false, callComplete, bgColor ));
    	row.addView(createTextView( data.batchNo,		weight[2], false, callComplete, bgColor ));
    	row.addView(createTextView( data.jobNo,			weight[3], false, callComplete, bgColor ));
    	row.addView(createTextView( data.dateStarted,	weight[4], false, callComplete, bgColor ));
    	
    	String mechs = "";
    	if( (data.mech1 != null) && (data.mech1.length() > 0) )
    		mechs += data.mech1;
    	if( (data.mech2 != null) && (data.mech2.length() > 0) )
    		{
    		if( mechs.length() > 0 )
    			mechs += "\n";
    		mechs += data.mech2;
    		}
    	row.addView(createTextView( mechs,				weight[5], false, callComplete, bgColor ));
    	row.addView(createTextView( data.desc,			weight[6], false, callComplete, bgColor ));
    	
    	row.setOnClickListener(dispatchPopup);
    	
		tl.addView(row);
	    }
    
    private TextView createTextView(String text, float weight, boolean firstCol, boolean callComplete, int bgColor)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			0, LayoutParams.FILL_PARENT);
    	params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setPadding(5, 5, 5, 5);
		if(callComplete)
			{
			tv.setTextColor(Twix_Theme.disabledColor);
    		tv.setBackgroundColor(Twix_Theme.disabledColorBG);
			}
		else
			{
			tv.setTextColor(Twix_Theme.headerValue);
			if( firstCol )
				{
				tv.setBackgroundColor(BrightenColor(bgColor, 1.2f));
				}
			else
				tv.setBackgroundColor(bgColor);
    		}
    	return tv;
    	}
    
    private int BrightenColor(int c, float multi)
    	{
    	int a = Color.alpha(c);
		int r = Color.red(c);
		int b = Color.blue(c);
		int g = Color.green(c);
		int def = (int) (multi*10) - 10;
		
		if (r == 0 && b == 0 && g == 0)
			return Color.argb(a, def, def, def);
		
		// Red
		if (r < 3 && r != 0)
			{
			r = def;
			}
		else
			{
			r = (int) (r * multi);
			r = (r > 255) ? 255 : r;
			}
		
		// Blue
		if (b < 3 && b != 0)
			{
			b = def;
			}
		else
			{
			b = (int) (b * multi);
			b = (b > 255) ? 255 : b;
			}
		
		// Green
		if (g < 3 && g != 0)
			{
			g = def;
			}
		else
			{
			g = (int) (g * multi);
			g = (g > 255) ? 255 : g;
			}

	    return Color.argb(a, r, g, b);
    	}
    
    private TextView createText( String s )
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.weight = 1;
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(s);
    	tv.setTextSize(25);
    	tv.setTextColor( Twix_Theme.sub1Value );
    	
    	return tv;
    	}
    
    private TextView createText( String s, float weight )
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.weight = weight;
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(s);
    	tv.setTextSize(25);
    	tv.setTextColor( Twix_Theme.sub1Value );
    	
    	return tv;
    	}
    
    private TextView createTextNoWidth( String s, float weight )
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			0, LayoutParams.WRAP_CONTENT);
    	params.weight = weight;
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(s);
    	tv.setTextSize(25);
    	tv.setTextColor( Twix_Theme.sub1Value );
    	
    	return tv;
    	}
    
    public void Update_WorkFlow()
    	{
    	readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
    	clearBgs();
    	CurrentSearch = "siteName";
    	curCol = 0;
    	findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortAsc);
    	SetupColorCoding();
    	readSQL();
    	}
    
    public void SQLQuery(View v)
    	{
    	Intent intent = new Intent(getParent(), Twix_AgentEquipmentOld.class);
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)getParent();
        parentActivity.startChildActivity("Twix_AgentEquipmentOld", intent);
    	}
    
    /**
     * Returns the total service labor hours on a dispatch, including the user's current open tags.
     * 	This data is only as accurate as the last sync.
     * 
     * @param dispatchId
     * @return Total Dispatch labor hours, including tablet open tags
     */
    private float getDispatchHours(int dispatchId)
    	{
    	float ret = 0;
    	
    	String sqlQ = "SELECT SUM(sl.regHours), SUM(sl.thHours), SUM(sl.dtHours) " +
				"FROM serviceLabor AS sl " +
					"LEFT OUTER JOIN serviceTagUnit AS su " +
						"ON su.serviceTagUnitId = sl.serviceTagUnitId " +
					"LEFT OUTER JOIN serviceTag AS st " +
						"ON st.serviceTagId = su.serviceTagId " +
					"LEFT OUTER JOIN openServiceTag AS ost " +
						"ON ost.serviceTagId = su.serviceTagId " +
				"WHERE st.dispatchId = " + dispatchId + " OR ost.dispatchId = " + dispatchId;
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			// Corrected to not multiply hours
			ret += cursor.getFloat(0);
			ret += cursor.getFloat(1);
			ret += cursor.getFloat(2);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
    	
    	return ret;
    	}
    
	//Sort Functions
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Tenant).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_BatchNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_CallDate).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Mechanic).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Description).setBackgroundColor(Twix_Theme.sortNone);
    	}
	
    private class DispatchPopupData
    	{
    	int DispatchId;
    	int ServiceAddressId;
    	
    	String batchNo;
    	String jobNo;
    	String dateOrdered;
    	String siteContact;
    	String siteContactPhone;
    	String requestedBy;
    	String requestedByPhone;
    	String requestedByEmail;
    	String cusNo;
    	String altBillTo;
    	
    	String siteName;
    	String tenant;
    	String address1;
    	String address2;
    	String city;
    	String state;
    	String zip;
    	
    	String description;
    	Mechanic[] mech;
    	
    	String billingName;
    	String billingAddress1;
    	String billingAddress2;
    	String billingAddress3;
    	String billingAddress4;
    	
    	String PMComments;
    	String PMEstTime;
    	
    	String contractType;
    	String customerPO;
    	
    	public DispatchPopupData(int dId, Cursor cursor)
    		{
    		DispatchId = dId;
    		
    		mech = new Mechanic[7];
    		for( int i = 0; i < mech.length; i++ )
    			{
    			mech[i] = new Mechanic();
    			}
    		
    		
    		int index = 0;
			ServiceAddressId	= cursor.getInt(index++);
			batchNo				= cursor.getString(index++);
			jobNo				= cursor.getString(index++);
			dateOrdered			= cursor.getString(index++);
			siteContact			= cursor.getString(index++);
			siteContactPhone	= cursor.getString(index++);
			requestedBy			= cursor.getString(index++);
			requestedByPhone	= cursor.getString(index++);
			requestedByEmail	= cursor.getString(index++);
			cusNo				= cursor.getString(index++);
			altBillTo			= cursor.getString(index++);
			
			siteName			= cursor.getString(index++);
			address1			= cursor.getString(index++);
			address2			= cursor.getString(index++);
			city				= cursor.getString(index++);
			state				= cursor.getString(index++);
			zip					= cursor.getString(index++);
			
			description			= cursor.getString(index++);
			
			// Assign the mechanic objects
			for( int i = 0; i < mech.length; i++ )
				{
				mech[i].Empno		= cursor.getString(index++);
				mech[i].Name		= cursor.getString(index++);
				}
			
			billingName = cursor.getString(index++);
			billingAddress1	= Twix_TextFunctions.clean(cursor.getString(index++));
			billingAddress2	= Twix_TextFunctions.clean(cursor.getString(index++));
			billingAddress3	= Twix_TextFunctions.clean(cursor.getString(index++));
			billingAddress4	= Twix_TextFunctions.clean(cursor.getString(index++));
			
			tenant				= cursor.getString(index++);
			PMComments			= cursor.getString(index++);
			PMEstTime			= cursor.getString(index++);
			contractType		= cursor.getString(index++);
			customerPO			= cursor.getString(index++);
    		}
    	
    	class Mechanic
	    	{
	    	String Empno;
	    	String Name;
	    	
	    	public boolean IsEmpty()
	    		{
	    		boolean ret = (Empno != null) && (Name != null);
	    		if( ret )
	    			ret = (Empno.length() > 0) && (Name.length() > 0);
	    		
	    		return !ret;
	    		}
	    	}
    	}
    
    private class DispatchPopup
	    {
	    TextView BatchNo;
	    TextView JobNo;
	    TextView CallDate;
	    LinearLayout ContactResults;
	    LinearLayout RequestResults;
	    TextView BillTo;
	    TextView BillAddress1;
	    TextView BillAddress2;
	    TextView SiteName;
	    TextView TenantTitle;
	    TextView Tenant;
	    TextView Address1;
	    TextView Address2;
	    TextView City;
	    TextView State;
	    TextView Zip;
	    
	    TextView Description;
	    LinearLayout MechanicBuild;
	    Button AssignMechanic;
	    
	    TextView PMComTitle;
	    LinearLayout PMComments;
	    TextView PMEstTime;
	    
	    TextView DispatchTime;
	    TextView CustomerPO;
	    
	    LinearLayout Navigation;
	    
	    DispatchPopupData data;
	    
	    public DispatchPopup(View viewToLoad)
		    {
		    BatchNo				= (TextView) viewToLoad.findViewById(R.id.Text_BatchNo);
			JobNo				= (TextView) viewToLoad.findViewById(R.id.Text_JobNo);
			CallDate			= (TextView) viewToLoad.findViewById(R.id.Text_CallDate);
			ContactResults		= (LinearLayout) viewToLoad.findViewById(R.id.ContactResults);
			RequestResults		= (LinearLayout) viewToLoad.findViewById(R.id.RequestResults);
			BillTo				= (TextView) viewToLoad.findViewById(R.id.Text_BillTo);
			BillAddress1		= (TextView) viewToLoad.findViewById(R.id.Text_BillAddress1);
			BillAddress2		= (TextView) viewToLoad.findViewById(R.id.Text_BillAddress2);
			SiteName			= (TextView) viewToLoad.findViewById(R.id.Text_SiteName);
			TenantTitle			= (TextView) viewToLoad.findViewById(R.id.Title_Tenant);
			Tenant				= (TextView) viewToLoad.findViewById(R.id.Text_Tenant);
			Address1			= (TextView) viewToLoad.findViewById(R.id.Text_Address1);
			Address2			= (TextView) viewToLoad.findViewById(R.id.Text_Address2);
			City				= (TextView) viewToLoad.findViewById(R.id.Text_City);
			State				= (TextView) viewToLoad.findViewById(R.id.Text_State);
			Zip					= (TextView) viewToLoad.findViewById(R.id.Text_Zip);
			Description			= (TextView) viewToLoad.findViewById(R.id.Text_Description);
			MechanicBuild		= (LinearLayout) viewToLoad.findViewById(R.id.MechanicBuild);
			AssignMechanic 		= (Button) viewToLoad.findViewById(R.id.AssignMechanic);
			PMComTitle			= (TextView) viewToLoad.findViewById(R.id.PMComTitle);
			PMComments			= (LinearLayout) viewToLoad.findViewById(R.id.PMComments);
			PMEstTime			= (TextView) viewToLoad.findViewById(R.id.PMEstTime);
			DispatchTime		= (TextView) viewToLoad.findViewById(R.id.TotalDispatchHours);
			CustomerPO			= (TextView) viewToLoad.findViewById(R.id.Text_CustomerPO);
			
			Navigation			= (LinearLayout) viewToLoad.findViewById(R.id.Navigation);
		    }
	    
	    public void AssignData(DispatchPopupData pData)
	    	{
	    	data = pData;
	    	
			BatchNo.setText( pData.batchNo );
			JobNo.setText( pData.jobNo );
			CallDate.setText( Twix_TextFunctions.DBtoNormal(pData.dateOrdered) );
			
			// SiteContact
			ContactResults.addView( createText(pData.siteContact) );
			if( pData.siteContactPhone != null && pData.siteContactPhone.length() > 9)
				{
				// Format the Phone Number
				pData.siteContactPhone = "(" + pData.siteContactPhone.substring(0,3) + ") " +
											pData.siteContactPhone.substring(3,6) + "-" +
											pData.siteContactPhone.substring(6,10);
				ContactResults.addView( createText(pData.siteContactPhone) );
				}
			
			// RequestedBy
			RequestResults.addView( createText(pData.requestedBy) );
			if( pData.requestedByPhone != null && pData.requestedByPhone.length() > 9 )
				{
				pData.requestedByPhone = "(" + pData.requestedByPhone.substring(0,3) + ") " +
						pData.requestedByPhone.substring(3,6) + "-" +
						pData.requestedByPhone.substring(6,10);
				RequestResults.addView( createText(pData.requestedByPhone) );
				}
			
			RequestResults.addView( createText(pData.requestedByEmail) );
			
			
			// Set Billing or Alt Billing
			BillTo.setText( pData.billingName );
			BillAddress1.setText( pData.billingAddress1 + " " + pData.billingAddress2 );
			BillAddress2.setText( pData.billingAddress3 + " " + pData.billingAddress4 );
			
			// Service Address through description
			SiteName.setText( pData.siteName );
			if( pData.tenant != null && pData.tenant.length() > 0 )
				{
				Tenant.setText( pData.tenant );
				Tenant.setVisibility(View.VISIBLE);
				TenantTitle.setVisibility(View.VISIBLE);
				}
			
			Address1	.setText( pData.address1 );
			Address2	.setText( pData.address2 );
			City		.setText( pData.city );
			State		.setText( pData.state );
			Zip			.setText( pData.zip );
			Description	.setText( pData.description );
			
			// Mechanic Build
			boolean hasOpenSlot = false;
			ArrayList<String> MechanicList = new ArrayList<String>();
			DispatchPopupData.Mechanic mech;
			LinearLayout row = null;
			LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
		    		LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			int mechCnt = 0;
			for( int i = 0; i < pData.mech.length; i++ )
				{
				mech = pData.mech[i];
				if( mech.IsEmpty() )
					hasOpenSlot = true;
				else
					{
					MechanicList.add(mech.Empno);
					if( mechCnt % 2 == 0)
						{
						row = new LinearLayout(mContext);
					    row.setLayoutParams(rowParams);
					    row.setOrientation(LinearLayout.HORIZONTAL);
						MechanicBuild.addView(row);
						}
					row.addView( createTextNoWidth( mech.Name, 1 ) );
					mechCnt++;
					}
				}
			
			if( !hasOpenSlot )
				AssignMechanic.setVisibility(View.INVISIBLE);
			else
				{
				Object[] mechTag = new Object[2];
				mechTag[0] = pData;
				mechTag[1] = MechanicList;
				AssignMechanic.setTag(mechTag);
				AssignMechanic.setOnClickListener(assignMechanicPopup);
				}
			
			// PM Comments & Time
			if( pData.contractType != null && pData.contractType.contentEquals("PM"))
				{
				String PMComm = pData.PMComments;
				
				if( PMComments != null )
					{
					String com1 = "";
					String com2 = "";
					String com3 = "";
					String com4 = "";
					
					try
						{
						if( PMComm.length() >= 60 )
							com1 = PMComm.substring(0, 60).trim();
						else
							com1 = PMComm.substring(0).trim();
						
						if( PMComm.length() >= 120 )
							com2 = PMComm.substring(60, 120).trim();
						else if( PMComm.length() > 60 )
							com2 = PMComm.substring(60).trim();
							
						if( PMComm.length() >= 180 )
							com3 = PMComm.substring(120, 180).trim();
						else if( PMComm.length() > 120 )
							com3 = PMComm.substring(120).trim();
						
						if( PMComm.length() > 180 )
							com4 = PMComm.substring(180).trim();
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
						
					if( com1.length() > 0 || com2.length() > 0 || com3.length() > 0 || com4.length() > 0 )
						{
						PMComTitle.setVisibility(View.VISIBLE);
						PMComments.setVisibility(View.VISIBLE);
						if( com1.length() > 0 )
							PMComments.addView( createText(com1, 0f) );
						if( com2.length() > 0 )
							PMComments.addView( createText(com2, 0f) );
						if( com3.length() > 0 )
							PMComments.addView( createText(com3, 0f) );
						if( com4.length() > 0 )
							PMComments.addView( createText(com4, 0f) );
						}
					}
				
				if( pData.PMEstTime != null && pData.PMEstTime.length() > 0 )
					{
					PMEstTime.setVisibility(View.VISIBLE);
					PMEstTime.setText("PM Estimated Time: " + pData.PMEstTime);
					}	
				}
			
			CustomerPO.setText( pData.customerPO );
			Navigation.setTag( pData.address1 + " " + pData.address2 + " " + pData.city + ", " + pData.state + " " + pData.zip );
	    	}
	    }
    
    
    private void setClickListeners()
	    {
	    findViewById(R.id.Sort_SiteName).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 0 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 0;
            		}
            	CurrentSearch = "siteName";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Tenant).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 1 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 1;
            		}
            	CurrentSearch = "dispatch.tenant";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_BatchNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 2 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 2;
            		}
            	CurrentSearch = "batchNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_JobNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 3 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 3;
            		}
            	CurrentSearch = "jobNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_CallDate).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 4 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 4;
            		}
            	CurrentSearch = "dateStarted";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Mechanic).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 5 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 5;
            		}
            	CurrentSearch = "m1.mechanic_name ";
            	if( !desc )
            		CurrentSearch += "asc";
            	else
            		CurrentSearch += "desc";
            	CurrentSearch += ", m2.mechanic_name";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Description).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 6 )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		curCol = 6;
            		}
            	CurrentSearch = "description";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.FilterDispatches).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	inflateFilter();
                }
        	});
	    
	    dispatchPopup = new OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	final DispatchData data = (DispatchData)v.getTag();
            	BuildSummary(data.dispatchId);
            	}
        	}
	    ;
	    
        colorCodePopup = new OnClickListener()
        	{
			@Override
			public void onClick(View v)
				{
				ColorKeyPopup();
				}
        	}
        ;
        findViewById(R.id.ColorCodeInfo).setOnClickListener(colorCodePopup);
        
       safetyapp = new OnClickListener()
        	{
			@Override
			public void onClick(View v)
				{
				AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
				alertDialog.setTitle("SDS BinderWorks Mobile App");
				alertDialog.setMessage("Please use 'therma/msds' as 'username/password' for SDS Binderworks application if you need to login.\n");
				
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
				    new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) {
				        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.trilixgroup.android.sdsbinderworks");
						
						   
	                	if (launchIntent != null) { 
					    startActivity(launchIntent);//null pointer check in case package name was not found
	                	}else{
	               
	                	Toast.makeText(mContext,
	                			"Cannot find the app. " +
	                			"Please install SDS Binderworks Mobile app from the Android Market and try again",
	                			Toast.LENGTH_LONG).show();
	                	
				}
				        }
				    });
				alertDialog.show();
				}
				
        	}
        ;
       
        findViewById(R.id.safetyapp).setOnClickListener(safetyapp);
        
        assignMechanicPopup = new OnClickListener()
        	{
			@Override
			public void onClick(View v)
				{
				Object[] params = (Object[]) v.getTag();
				DispatchPopupData pData = (DispatchPopupData)params[0];
				@SuppressWarnings("unchecked")
				ArrayList<String> MechanicList = (ArrayList<String>)params[1];
				
				AssignMechanicPopup( pData.DispatchId, MechanicList );
				}
        	}
        ;
 
        
        assignMechanicSelection = new OnClickListener()
        	{
			@Override
			public void onClick(View v)
				{
				Object[] params = (Object[]) v.getTag();
				final String empno = (String) params[0];
				final int dispatchId = (Integer) params[1];
				
				AlertDialog alert = new AlertDialog.Builder(mContext).create();
		    	alert.setTitle("Confirm");
		    	alert.setMessage("Are you sure you want to assign this mechanic?");
		    	
		    	LinearLayout list = new LinearLayout(mContext);
		    	list.setOrientation(LinearLayout.VERTICAL);
		    	list.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) );
		    	
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
					{
					@Override
					public void onClick(DialogInterface dialog, int which)
						{
						switch (which)
							{
							case DialogInterface.BUTTON_POSITIVE:
								// Send the Request...
								DispatchRequest dRequest = new DispatchRequest();
								dRequest.DispatchId = dispatchId;
								dRequest.Empno = empno;
								app.AssignDispatch(false, dRequest);
								break;

							case DialogInterface.BUTTON_NEGATIVE:
								// Just Dismiss the Dialog
								break;
							}
						}
					};
		    	alert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", dialogClickListener);
		    	alert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", dialogClickListener);
		    	alert.show();
				}
        	}
        ;
        // End Click Listeners
       
	    navigationClick = new OnClickListener()
	    	{
			@Override
			public void onClick(View v)
				{
				String NavigationAddress = (String) v.getTag();
				try
					{
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse("http://maps.google.com/maps?daddr=" + NavigationAddress) );
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(intent);
					}
				catch (Exception e)
					{
					Toast.makeText(Twix_AgentTab1.this,
							"Error attempting to open Navigate. Please make sure Google Maps is installed.",
							Toast.LENGTH_LONG).show();
					}
				}
	    	};
	    }
    
    private void BuildSummary(int dispatchId)//DispatchData data)
    	{
    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.dispatch_summary, null);
        
    	final Dialog dialog = new Dialog(mContext);
    	SummaryDialog = dialog;
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Populate the Dispatch Popup Object
		DispatchPopup views = new DispatchPopup(viewToLoad);
		
		views.Navigation.setOnClickListener(navigationClick);
		
		String sqlQ = "select dispatch.serviceAddressId, " +
						"dispatch.batchNo, substr(dispatch.jobNo, 5), dispatch.dateOrdered, " +
        				"dispatch.siteContact, dispatch.siteContactPhone, " +
        				"dispatch.requestedBy, dispatch.requestedByPhone, dispatch.requestedByEmail, " +
        				"dispatch.cusNo, dispatch.altBillTo, " +
        				"serviceAddress.siteName, serviceAddress.address1, serviceAddress.address2, " +
        				"serviceAddress.city, serviceAddress.state, serviceAddress.zip, " +
        				"dispatch.description, " +
        				"m1.mechanic, m1.mechanic_name, " +
        				"m2.mechanic, m2.mechanic_name, " +
        				"m3.mechanic, m3.mechanic_name, " +
        				"m4.mechanic, m4.mechanic_name, " +
        				"m5.mechanic, m5.mechanic_name, " +
        				"m6.mechanic, m6.mechanic_name, " +
        				"m7.mechanic, m7.mechanic_name, " +
        				"CASE WHEN dispatch.altBillTo = '' THEN ( billing.Name ) ELSE ( altBilling.Name ) END AS billingName, " +
    					"CASE WHEN dispatch.altBillTo = '' THEN ( billing.Address1 ) ELSE ( altBilling.Address1 ) END AS billingAddress1, " +
    					"CASE WHEN dispatch.altBillTo = '' THEN ( billing.Address2 ) ELSE ( altBilling.Address2 ) END AS billingAddress2, " +
    					"CASE WHEN dispatch.altBillTo = '' THEN ( billing.Address3 ) ELSE ( altBilling.Address3 ) END AS billingAddress3, " +
    					"CASE WHEN dispatch.altBillTo = '' THEN ( billing.Address4 ) ELSE ( altBilling.Address4 ) END AS billingAddress4, " +
    					"dispatch.tenant, dispatch.PMComments, dispatch.PMEstTime, dispatch.contractType, dispatch.customerPO " +
    				"from dispatch " +
						"LEFT OUTER JOIN serviceAddress " +
							"on serviceAddress.serviceAddressId = dispatch.serviceAddressId " +
						// Left Outer Join the mechanics
						"LEFT OUTER JOIN mechanic as m1 " +
							"on m1.mechanic = dispatch.mechanic1 " +
						"LEFT OUTER JOIN mechanic as m2 " +
							"on m2.mechanic = dispatch.mechanic2 " +
						"LEFT OUTER JOIN mechanic as m3 " +
							"on m3.mechanic = dispatch.mechanic3 " +
						"LEFT OUTER JOIN mechanic as m4 " +
							"on m4.mechanic = dispatch.mechanic4 " +
						"LEFT OUTER JOIN mechanic as m5 " +
							"on m5.mechanic = dispatch.mechanic5 " +
						"LEFT OUTER JOIN mechanic as m6 " +
							"on m6.mechanic = dispatch.mechanic6 " +
						"LEFT OUTER JOIN mechanic as m7 " +
							"on m7.mechanic = dispatch.mechanic7 " +
						// Left Outer Join the billing formation
						"LEFT OUTER JOIN billing as billing " +
							"ON billing.CustomerID = dispatch.cusNo AND dispatch.altBillTo = '' " +
						"LEFT OUTER JOIN billing as altBilling " +
							"ON altBilling.CustomerID = dispatch.altBillTo AND dispatch.altBillTo != '' " +
					"WHERE dispatch.dispatchId = " + dispatchId + " ";
		Cursor cursor = db.rawQuery(sqlQ);
		
		if( cursor.moveToFirst())
			{
			DispatchPopupData pData = new DispatchPopupData(dispatchId, cursor);
			
			views.AssignData(pData);
			}
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		dialog.setContentView(viewToLoad);
		dialog.show();
		Button bn = ((Button)dialog.findViewById(R.id.OpenTag));
		if( readOnly )
			{
			bn.setVisibility(View.INVISIBLE);
			bn.setEnabled(false);
			}
		else
			{
			Object[] tag = new Object[2];
			tag[0] = dispatchId;
			tag[1] = views.data.ServiceAddressId;
			bn.setTag(tag);
			bn.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v2)
					{
					dialog.dismiss();
					Object[] params = (Object[]) v2.getTag();
					int dispatchId = (Integer) params[0];
					int serviceAddressId = (Integer) params[1];	    					
					
					if( checkPages() )
						{
						newTag( dispatchId, serviceAddressId );
						}
					else
						{
						promptPages(dispatchId, serviceAddressId);
						}
					}
				});
			}
		ImageButton ib = ((ImageButton)dialog.findViewById(R.id.close));
		ib.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				dialog.dismiss();
				}
			});
    	
    	views.DispatchTime.setText( getDispatchHours(dispatchId)+"" );
    	}
    
    private Dispatch_Filters readFilterPreferences()
    	{
    	Dispatch_Filters ret = null;
    	
    	try
    		{
	    	FileInputStream fis = openFileInput(app.PREFS_FILE);
	    	ObjectInputStream in = new ObjectInputStream(fis);
	    	ret = (Dispatch_Filters) in.readObject();
	    	in.close();
    		}
    	catch( IOException e )
    		{
    		Log.i(Twix_AgentTab1.this.getClass().toString(),
    				"IO EXCEPTION: Failed to read the user preferences file. Reason: " + e.getMessage() );
    		}
    	catch( ClassNotFoundException e )
    		{
    		Log.e(Twix_AgentTab1.this.getClass().toString(),
    				"CLASS NOT FOUND EXCEPTION: Failed to read the user preferences file. Reason: " + e.getMessage() );
    		}
    	
    	if( ret == null )
    		ret = new Dispatch_Filters();
    	
    	return ret;
    	}
    
    private void writeFilterPreferences(Dispatch_Filters filters)
    	{
    	try
    		{
	    	FileOutputStream fos = openFileOutput(app.PREFS_FILE, Context.MODE_PRIVATE);
	    	ObjectOutputStream out = new ObjectOutputStream(fos);
	    	out.writeObject(filters);
	    	out.close();
    		}
    	catch( IOException e )
    		{
    		Log.e(Twix_AgentTab1.this.getClass().toString(),
    				"Failed to write the user preferences file. Reason: " + e.getMessage() );
    		}
    	}
    
    public void inflateFilter()
    	{
    	filterDialog = new Dialog(mContext);
    	filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.dispatch_filter, null);
    	
    	final LinearLayout ll_Mech1 = (LinearLayout) viewToLoad.findViewById(R.id.ListView_Mechanic1);
    	final LinearLayout ll_Mech2 = (LinearLayout) viewToLoad.findViewById(R.id.ListView_Mechanic2);
    	
    	final Button bn_Apply = (Button) viewToLoad.findViewById(R.id.Button_Apply);
    	final Button bn_Cancel = (Button) viewToLoad.findViewById(R.id.Button_Cancel);
    	final Button bn_Mech1 = (Button) viewToLoad.findViewById(R.id.Button_AddMechanic1);
    	final Button bn_Mech2 = (Button) viewToLoad.findViewById(R.id.Button_AddMechanic2);
    	final Button bn_ALL = (Button) viewToLoad.findViewById(R.id.Button_AllCall);
    	
    	final CheckBox ck_NoTags = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_NoTags);
    	final CheckBox ck_MustReturn = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_MustReturn);
    	final CheckBox ck_CallComplete = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_CallComplete);
    	
    	final CheckBox ck_Mech1 = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_Mechanic1);
    	final CheckBox ck_Mech2 = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_Mechanic2);
    	
    	bn_ALL.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				ck_NoTags.setChecked(true);
				ck_MustReturn.setChecked(true);
				ck_CallComplete.setChecked(true);
				}
    		});
    	bn_Mech1.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
		    	createMechanicChoices(ll_Mech1);
				}
    		});
    	bn_Mech2.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
		    	createMechanicChoices(ll_Mech2);
				}
    		});
    	
    	bn_Apply.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				filters.NoTags			= ck_NoTags.isChecked();
				filters.MustReturn		= ck_MustReturn.isChecked();
				filters.CallComplete	= ck_CallComplete.isChecked();
				filters.Mech1			= ck_Mech1.isChecked();
				filters.Mech2			= ck_Mech2.isChecked();
				filters.Mech1List.clear();
				filters.Mech2List.clear();
				
				int size = ll_Mech1.getChildCount();
				View row;
				String s;
				for( int i = 0; i < size; i++ )
					{
					row = ll_Mech1.getChildAt(i);
					s = (String) row.getTag();
					if( s.length() > 0 )
						filters.Mech1List.add( s );
					}
				
				size = ll_Mech2.getChildCount();
				for( int i = 0; i < size; i++ )
					{
					row = ll_Mech2.getChildAt(i);
					s = (String) row.getTag();
					if( s.length() > 0 )
						filters.Mech2List.add( s );
					}
				
				writeFilterPreferences(filters);
				
				filterDialog.dismiss();
				readSQL();
				}
    		});
    	bn_Cancel.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				filterDialog.dismiss();
				}
    		});
    	
    	// Setup the saved preferences
    	ck_NoTags		.setChecked(filters.NoTags);
    	ck_MustReturn	.setChecked(filters.MustReturn);
    	ck_CallComplete	.setChecked(filters.CallComplete);
    	ck_Mech1		.setChecked(filters.Mech1);
    	ck_Mech2		.setChecked(filters.Mech2);
    	
    	
    	for( int i = 0; i < filters.Mech1List.size(); i++ )
    		{
			ll_Mech1.addView(inflateMechanic(filters.Mech1List.get(i)));
    		}
    	
    	for( int i = 0; i < filters.Mech2List.size(); i++ )
    		{
    		ll_Mech2.addView(inflateMechanic(filters.Mech2List.get(i)));
    		}
    	
    	// Finally display the dialog
    	filterDialog.setContentView(viewToLoad);
    	filterDialog.getWindow().setLayout(1400	, LayoutParams.WRAP_CONTENT);
    	filterDialog.show();
    	}
    
    private void createMechanicChoices(LinearLayout target)
    	{
    	mechanicDialog = new Dialog(mContext);
    	mechanicDialog.setTitle("Select a Mechanic to filter by");
    	mechanicDialog.setContentView(R.layout.dispatch_mechanic_list);
    	mechanicDialog.getWindow().setLayout(600, 700);
    	
    	LinearLayout host = (LinearLayout) mechanicDialog.findViewById(R.id.MechHost);
    	
    	String sqlQ = "SELECT mechanic.mechanic, mechanic.mechanic_name, " +
				"CASE WHEN dept = 'REF' THEN 0 ELSE 1 END as deptSort " +
			"FROM mechanic " +
				"WHERE Terminated != 'Y' " +
			"ORDER BY deptSort asc, mechanic.mechanic_name asc";
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			do
				{
				host.addView(createTextView(cursor.getString(0), cursor.getString(1), target ));
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		Button bn_Cancel = (Button) mechanicDialog.findViewById(R.id.Cancel);
		bn_Cancel.setOnClickListener( new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( mechanicDialog != null )
					if( mechanicDialog.isShowing() )
						mechanicDialog.dismiss();
				}
			});
		
    	mechanicDialog.show();
    	}
    
    private TextView createTextView(String mechName, String mechId, final LinearLayout mech)
    	{
    	TextView tv = new TextView(mContext);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	params.setMargins(3, 3, 3, 3);
    	tv.setLayoutParams(params);
    	tv.setPadding(5, 5, 5, 5);
    	tv.setTextSize(Twix_Theme.headerSize);
    	tv.setTextColor(Twix_Theme.headerText);
    	tv.setText(mechName);
    	tv.setTag(mechId);
    	tv.setOnClickListener(new OnClickListener()
    		{
    		@Override
			public void onClick(View v)
				{
				if( mechanicDialog != null)
					{
					if( mechanicDialog.isShowing() )
						{
						TextView tv = (TextView) v;
						mech.addView( inflateMechanic( (String)tv.getTag() ));
						mechanicDialog.dismiss();
						}
					}
				}
    		});
    	
    	return tv;
    	}
   
    private View inflateMechanic(String mechId)
    	{
    	String mechName = null;
    	String sqlQ = "select mechanic_name, mechanic from mechanic " +
    			"WHERE mechanic = '" + mechId + "' " +
				"ORDER BY mechanic_name";
		Cursor cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			do
				{
				mechName = Twix_TextFunctions.clean(cursor.getString(0));
				}
		    while (cursor.moveToNext());
			}
		else
			mechName = "Employee Not Found";
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
    	
    	View v = LayoutInflater.from(mContext).inflate(R.layout.dispatch_mechanic_item, null);
		((TextView)v.findViewById(R.id.Item_Text)).setText(mechName);
		((ImageButton)v.findViewById(R.id.Item_Delete)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				((LinearLayout)v.getParent().getParent()).removeView((View) v.getParent());
				}
			});
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		v.setLayoutParams(params);
		
		v.setTag(mechId);
		
		return v;
    	}
    
    @Override
	public void onBackPressed ()
		{
		//if( !http.cancelHTTP() )
			super.onBackPressed();
		}
	
	/**
	 * Saving all Pages
	 */
	
	private void newTag( int dispatchId, int serviceAddressId )
		{
		Twix_AgentTabActivity tabActivity = (Twix_AgentTabActivity)((Twix_AgentActivityGroup1)mContext).getParent();
		tabActivity.getTabHost().setCurrentTab(1);
		
		LocalActivityManager manager = tabActivity.getLocalActivityManager();
		Twix_AgentActivityGroup2 act = (Twix_AgentActivityGroup2) manager.getActivity("tags");
		act.newOpenTag( dispatchId, serviceAddressId );
		}
	
	private void promptPages(final int dispatchId, final int serviceAddressId )
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	String error = saveAllPages();
			        	if( error.length() < 1)
			        		{
			        		newTag(dispatchId, serviceAddressId);
			        		}
			        	else
			        		{
			        		AlertDialog alert = new AlertDialog.Builder(mContext).create();
			            	alert.setTitle("Error Saving Pages:");
			            	alert.setMessage( error );
			            	alert.setButton("Ok", new DialogInterface.OnClickListener()
			            		{  
			            		public void onClick(DialogInterface dialog, int which)
			            			{
			            			return;  
			            			}
			            		});
			            	alert.show();
			        		}
			            break;
		
			        case DialogInterface.BUTTON_NEGATIVE:
			        	// Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Would you like to save your current open tag?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		}
	
	private boolean checkPages()
		{
		TabActivity host = (TabActivity) ((Twix_TabActivityGroup)mContext).getParent();
		LocalActivityManager manager = host.getLocalActivityManager();

		Twix_TabActivityGroup group = (Twix_TabActivityGroup) manager.getActivity("tags");
		if (group != null)
			{
			if (group.activityExists("Twix_AgentOpenTagsTabHost"))
				return false;
			}
		
		return true;
		}
	
	private String saveAllPages()
		{
		if( filterDialog != null && filterDialog.isShowing() )
			filterDialog.dismiss();
		
		if( mechanicDialog != null && mechanicDialog.isShowing() )
			mechanicDialog.dismiss();
		
		return "";
		}
	
	private void ColorKeyPopup()
		{
		// Alert
		AlertDialog alert = new AlertDialog.Builder(mContext).create();
    	alert.setTitle("Dispatch Color Key");
    	
    	LinearLayout list = new LinearLayout(mContext);
    	list.setOrientation(LinearLayout.VERTICAL);
    	list.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) );
    	
    	Set<Entry<Integer, String>> ColorSet = ColorCoding.entrySet();
    	Entry<Integer, String> entry = null;
    	int bgColor = 0xFFFFFFFF;
    	for( Iterator<Entry<Integer, String>> i = ColorSet.iterator(); i.hasNext(); )
    		{
    		entry = i.next();
        	if( entry != null )
        		{
    	    	String bgColorString = entry.getValue();
    	    	try
    	    		{
    	    		bgColor = Integer.parseInt(bgColorString, 16)+0xFF000000;
    	    		}
    	    	catch( Exception e)
    	    		{
    	    		bgColor = Twix_Theme.headerBG;
    	    		Log.e("twix_agent:Twix_AgentTab1", "Failed to parse Dispatch Date Ordered Background Color.");
    	    		}
    	    	
    	    	list.addView( createColorItem("Dispatch Date > " + entry.getKey() + " Days", bgColor) );
        		}
    		}
    	
    	list.addView( createColorItem("Call Complete", Twix_Theme.disabledColorBG) );
    	
    	alert.setView(list);
    	alert.setButton("Ok", new DialogInterface.OnClickListener()
    		{  
    		public void onClick(DialogInterface dialog, int which)
    			{
    			return;  
    			}
    		});
    	alert.show();
		}
	
	private LinearLayout createColorItem( String s, int bgColor )
    	{
    	LinearLayout row = new LinearLayout(mContext);
    	LinearLayout.LayoutParams holderParam = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	holderParam.setMargins(2, 2, 2, 2);
    	row.setLayoutParams(holderParam);
    	row.setOrientation(LinearLayout.HORIZONTAL);
    	row.setBackgroundColor(Twix_Theme.headerBG);
    	
    	
    	LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(
    			0, LayoutParams.WRAP_CONTENT);
    	textParam.weight = 1f;
    	textParam.setMargins(2, 2, 2, 2);
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(textParam);
    	tv.setText(s);
    	tv.setTextSize(20);
    	tv.setTextColor( Twix_Theme.sub1Value );
    	tv.setPadding(2, 2, 2, 2);
    	row.addView(tv);
    	
    	LinearLayout sampleBorder = new LinearLayout(mContext);
    	LinearLayout.LayoutParams sampleBorderParam = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	sampleBorderParam.setMargins(2, 2, 2, 2);
    	sampleBorder.setLayoutParams(sampleBorderParam);
    	sampleBorder.setPadding(2, 2, 2, 2);
    	sampleBorder.setBackgroundColor(0xFF000000);
    	
    	LinearLayout sample = new LinearLayout(mContext);
    	LinearLayout.LayoutParams sampleParam = new LinearLayout.LayoutParams(25, 25);
    	sample.setLayoutParams(sampleParam);
    	sample.setBackgroundColor(bgColor);
    	
    	sampleBorder.addView(sample);
    	row.addView(sampleBorder);
    	
    	return row;
    	}
	
	// Dispatch Assignment Functions
	private void AssignMechanicPopup(int dispatchId, ArrayList<String> MechanicList)
		{
		// Build the query of assignable mechanics
		String sqlQ = "SELECT mechanic.mechanic, mechanic.mechanic_name, mechanic.dept," +
				"CASE WHEN dept = 'REF' THEN 0 ELSE 1 END as deptSort  " +
			"FROM mechanic " +
				"WHERE Terminated != 'Y' ";
			
		int size = MechanicList.size();
		if( size > 0 )
			sqlQ += " AND mechanic NOT IN ( ";
			
		for( int i = 0; i < size; i++ )
			{
			sqlQ += "'" + MechanicList.get(i) + "'";
			if( i < size-1 )
				sqlQ += ", ";
			}
		if( size > 0 )
			sqlQ += " )";
		sqlQ += "ORDER BY deptSort asc, mechanic.mechanic_name asc";
		Cursor cursor = db.rawQuery(sqlQ);
		
		
		ArrayList<Twix_SelectionDialog.RowInfo> rowInfo = new ArrayList<Twix_SelectionDialog.RowInfo>();
		Twix_SelectionDialog.RowInfo info;
		TextView tv;
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);
		Object[] tag;
		if (cursor.moveToFirst())
			{
			do
				{
				info = new Twix_SelectionDialog.RowInfo();
				tag = new Object[3];
				tag[0] = cursor.getString(0);
				tag[1] = dispatchId;
				tag[2]=cursor.getString(2);
				info.tag = tag;
				
				tv = new TextView(mContext);
				tv.setLayoutParams(params);
				tv.setText(cursor.getString(1));
				tv.setTextSize(20);
				tv.setTextColor(Twix_Theme.sub1Value);
				
				if (tag[2].equals("REF"))
					{
					tv.setBackgroundResource(R.drawable.clickable_bg2);
					}
					else
						{
					tv.setBackgroundColor(Twix_Theme.disabledColorBG);
						}
				tv.setPadding(4, 4, 4, 4);
				
				info.contents = tv;
				
				rowInfo.add(info);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		Twix_SelectionDialog dialog = new Twix_SelectionDialog(mContext, assignMechanicSelection, rowInfo, "Select One");
		AssignDialog = dialog.generateDialog();
		AssignDialog.getWindow().setLayout(600, 550);
		AssignDialog.show();
		}
	
	// Post Results
	public void refreshDialogs(int dispatchId)
		{
		if( AssignDialog != null && AssignDialog.isShowing() )
			{
			AssignDialog.dismiss();
			}
		
		if( SummaryDialog != null && SummaryDialog.isShowing() )
			{
			SummaryDialog.dismiss();
			BuildSummary(dispatchId);
			}
		}
	}