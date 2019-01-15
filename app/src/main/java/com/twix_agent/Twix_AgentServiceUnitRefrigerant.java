package com.twix_agent;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.twix_agent.Twix_AgentEquipmentDetail_Edit.CategoryArrayAdapter;
//import com.twix_agent.Twix_AgentEquipmentDetail_Edit.DateData;
import com.twix_agent.Twix_AgentServiceUnitLabor.MechanicArrayAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentFilters_Edit
 * 
 * Purpose: Allows the user to edit the filters on a piece of equipment.
 * 
 * Relevant XML: filters_edit.xml
 * 
 * 
 * @authorRashmi Kulkarni, Therma Corp.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitRefrigerant extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_SQLite db;
	private Context mContext;
	 private Calendar mcalendar;   
	//private LinearLayout ll;
	//private TableLayout tl;
	private Button UnitInfo,e,bref;
	
	  private int day,month,year;
	  private MechanicArrayAdapter adapter;
	  private OnItemSelectedListener dirtySelect;
	  private int currentCount = 0;
	  private int rowCount = 0;
	  private Spinner TechName,sref;
	  private ArrayAdapter<CharSequence> adapterManufacturer;
	  private TextView DateInService;
	  private EditText RefrigerantType;
	  private EditText Amount;
	  private EditText ModelNo;
	  private EditText SerialNo;
	  private EditText Cylinder;
	  private EditText CylinderSerial;
	  private EditText TransferedTo,Tech,tref;
	//  private EditText Capacity;
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
        View viewToLoad = LayoutInflater.from(getParent().getParent()).inflate(R.layout.service_refrigerant, null);
		this.setContentView(viewToLoad);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		LocalActivityManager manager2 = unitAct.getLocalActivityManager();
		//UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).spinnerUnitNo;
		UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).unitSpinner;
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        e=(Button) findViewById(R.id.e);
        DateInService	= (TextView) findViewById(R.id.Text_DateInService);
        tref	= (EditText) findViewById(R.id.tref);
        sref	= (Spinner) findViewById(R.id.sref);
        bref	= (Button) findViewById(R.id.bref);
        Tech = (EditText) findViewById(R.id.Text_Tech);
        TechName	= (Spinner) findViewById(R.id.Text_TechName);
        Amount	= (EditText) findViewById(R.id.Text_Amount);
        ModelNo	= (EditText) findViewById(R.id.Text_Model);
        Cylinder	= (EditText) findViewById(R.id.Text_Cylinder);
        SerialNo	= (EditText) findViewById(R.id.Text_SerialNo);
        TransferedTo	= (EditText) findViewById(R.id.Text_TransferedTo);
        CylinderSerial	= (EditText) findViewById(R.id.Text_CylinderSerial);
        
        buildAdapters();
        buildAdapters2();
        readSQL();
        
        DateInService.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        
    	((Twix_AgentActivityGroup2)mContext).changeDate(v);	
			
		
        }
        
        });
        
        e.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
                TechName.performClick();
				}
        });
        
        bref.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        			sref.performClick();
			       }
        });
    	
    	}
	
	
	public boolean validateSave()
		{
		boolean ret = true;
		boolean valid = true;
		boolean isEmpty = true;
		resetBGs();
		isEmpty = isEmpty();
		String s;
		valid = true;
		if( !isEmpty )
		{
			
		}
		s = DateInService.getText().toString();
		if ( s.length() < 1 )
		{
			mark(DateInService);
			valid = false;
		}
			
		s = Tech.getText().toString();
		if ( s.length() < 1 )
		{
			mark(e);
			valid = false;
		}
		s = bref.getText().toString();
		if ( s.length() < 1 )
		{
			mark(bref);
			valid = false;
		}
		s = ModelNo.getText().toString();
		if ( s.length() < 1 )
		{
			mark(ModelNo);
			valid = false;
		}
		s = SerialNo.getText().toString();
		if ( s.length() < 1 )
		{
			mark(SerialNo);
			valid = false;
		}
		s = TransferedTo.getText().toString();
		if ( s.length() < 1 )
		{
			mark(TransferedTo);
			valid = false;
		}
		s = Amount.getText().toString();
		if ( s.length() < 1 )
		{
			mark(Amount);
			valid = false;
		}
		s = Amount.getText().toString();
		if( !s.matches("^[0-9]{0,6}(\\.[0-9]{0,2}){0,1}$") )
		{
			mark(Amount);
			valid = false;
		}
		
		// Don't return, because we want to mark any incorrect fields
		if( !valid && !isEmpty)
			ret = false;
				
		return ret;
		}
	
	public boolean isEmpty()
		{
		return ( (DateInService).length() <= 0)
				&& (Amount.length() <= 0) 
				&& (ModelNo.length() <= 0)
				&& (SerialNo.length() <= 0)
				&& (bref.length() <= 0)
				&& (TransferedTo.length() <= 0);
		}
	
	private void resetBGs()
		{
			unMark(DateInService);
			unMark(TechName);
			unMark(Amount);
			unMark(bref);
			unMark(ModelNo);
			unMark(SerialNo);
			unMark(TransferedTo);
		}
	
	private void mark( View v )
		{
		v.setBackgroundColor(Twix_Theme.warnColorLight);
		}

	private void unMark(View v)
		{
		v.setBackgroundColor(Twix_Theme.editBG);
		}

	private void readSQL()
	    {
	    
		    
	    String sqlQ = "select substr(sr.transferdate, 1, 10 ), sr.techName, sr.typeOfRefrigerant, sr.amount, sr.nameOfCylinder, sr.cylinderSerialNo,sr.transferedTo, sr.modelNo,sr.serialNo, m.mechanic, m.mechanic_name, rt.RefrigerantTypeId, rt.RefrigerantType" +
					" from serviceRefrigerant sr INNER JOIN mechanic m on m.mechanic = sr.techName INNER JOIN RefrigerantType rt on rt.RefrigerantTypeId = sr.typeOfRefrigerant " +
					"where sr.serviceTagUnitId = " + unitAct.serviceTagUnitId ;

	    Cursor cursor = db.rawQuery(sqlQ);
	    RefrigerantData row = new RefrigerantData();
	    if ( cursor.moveToFirst() )
	    	{
	    	do
	    		{
	    		//row.reset();
				row.tdate	= Twix_TextFunctions.clean(cursor.getString(0));
				row.techname= Twix_TextFunctions.clean(cursor.getString(1));
				row.typeofrefrigerant	= Twix_TextFunctions.clean(cursor.getString(2));
				row.amount	= cursor.getString(3);
				row.nameofcylinder	= Twix_TextFunctions.clean(cursor.getString(4));
				row.cylinderserialno	= Twix_TextFunctions.clean(cursor.getString(5));
				row.transferedto	= Twix_TextFunctions.clean(cursor.getString(6));
				row.modelno	= Twix_TextFunctions.clean(cursor.getString(7));
				row.serialno	= Twix_TextFunctions.clean(cursor.getString(8));
				row.mechanicid = Twix_TextFunctions.clean(cursor.getString(9));
				row.mechname = Twix_TextFunctions.clean(cursor.getString(10));
				row.refid = Twix_TextFunctions.clean(cursor.getString(11));
				row.reftype = Twix_TextFunctions.clean(cursor.getString(12));
				
				applydata(row);
	    		}
	    	while (cursor.moveToNext());
	    	}
	    else {
				row.tdate	= "";
				row.techname = "";
				row.typeofrefrigerant	= "";
				row.nameofcylinder	= "";
				row.cylinderserialno	= "";
				row.transferedto	= "";
				row.modelno	= "";
				row.serialno	= "";
				row.mechanicid	= "";
				row.mechname = "";
				row.refid ="";
				row.reftype = "";
				row.amount = "";
				applydata(row);
	    	}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
}
	

private class RefrigerantData
	{
	String tdate = "";
	String techname = "";
	String typeofrefrigerant = "";
	String amount;
	String nameofcylinder = "";
	String cylinderserialno = "";
	String transferedto = "";
	String modelno = "";
	String serialno = "";
	String mechanicid ="";
	String mechname = "";
	String refid = "";
	String reftype = "";
	
	public void reset()
		{
		tdate = "";
		techname = "";
		typeofrefrigerant = "";
		nameofcylinder = "";
		cylinderserialno = "";
		transferedto = "";
		modelno = "";
		serialno = "";
		mechanicid = "";
		mechname ="";
		refid = "";
		reftype = "";
		amount ="";
	
		}
	
}

private class MechanicData
{
String mechanic;
String name;
boolean refDept;
}

private void buildAdapters()
		{
		buildEquipmentCategory();
		}

private void buildAdapters2()
	{
	buildRefs();
	}

private void buildEquipmentCategory()
	{
	String sqlQ = "SELECT mechanic.mechanic, mechanic.mechanic_name, " +
			"CASE WHEN dept = 'REF' THEN 0 ELSE 1 END as deptSort " +
		"FROM mechanic " +
			"WHERE Terminated != 'Y' " +
		"ORDER BY deptSort asc, mechanic.mechanic_name asc";
	Cursor cursor = db.rawQuery(sqlQ);
	ArrayList<EquipmentCategory> Categories = new ArrayList<EquipmentCategory>();
	while (cursor.moveToNext())
		Categories.add( new EquipmentCategory(cursor.getString(0), cursor.getString(1)) );
	
	if (cursor != null && !cursor.isClosed())
		cursor.close();
	
	CategoryArrayAdapter adapter = new CategoryArrayAdapter(mContext, Categories);
	TechName.setAdapter(adapter);
	}

private void buildRefs()
	{
	String sqlQ = "SELECT RefrigerantTypeId, RefrigerantType from RefrigerantType order by RefrigerantType";
	Cursor cursor = db.rawQuery(sqlQ);
	ArrayList<RefCategory> Refs = new ArrayList<RefCategory>();
	while (cursor.moveToNext())
		Refs.add( new RefCategory(cursor.getString(0), cursor.getString(1)) );
	
	if (cursor != null && !cursor.isClosed())
		cursor.close();
	
	RefArrayAdapter adapter2 = new RefArrayAdapter(mContext, Refs);
	sref.setAdapter(adapter2);
	}
	private class EquipmentCategory
	{
	String MechanicId;
	String MechName;

		public EquipmentCategory(String id, String Name)
			{
			MechanicId = id;
			MechName = Name;
			}
	}

	private class RefCategory
	{
	String RefId;
	String RefName;

	public RefCategory(String id, String Name)
	{
		RefId = id;
		RefName = Name;
	}
}

public class CategoryArrayAdapter extends ArrayAdapter<EquipmentCategory>
	{
	private List<EquipmentCategory>	items;
	private Context			mContext;
	private LinearLayout.LayoutParams layoutParams;
	
	public CategoryArrayAdapter(Context c, List<EquipmentCategory> items)
		{
		super(c, R.layout.spinner_popup, items);
		this.items = items;
		this.mContext = c;
		this.layoutParams = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
	
	// The View displayed for selecting in the list
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
		EquipmentCategory data = items.get(position);
		TextView tv = (TextView) super.getView(position, convertView, parent);
		
		// If the layout inflater fails/DropDown not set
		if (tv == null)
			{
			tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
			//tv = new TextView(mContext);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(Twix_Theme.headerSize);
			}
		
		tv.setTextColor(Twix_Theme.headerValue);
		tv.setText(data.MechName + "-" + data.MechanicId);
		
		
		TechName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         //   String selectedItemText = (String) parent.getItemAtPosition(position);
        EquipmentCategory data = items.get(position);
            // Notify the selected item text
            Toast.makeText
                    (getApplicationContext(), "Selected : " + data.MechName + " - " + data.MechanicId, Toast.LENGTH_SHORT)
                    .show();
           
           Tech.setText(data.MechanicId);
            e.setText(data.MechName);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    });
	return tv;
	
	}

	@Override
	public EquipmentCategory getItem(int position)
		{
		return items.get(position);
		}
	
	// Views displayed AFTER selecting
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
		{
		TextView tv = (TextView) convertView;
		EquipmentCategory data = items.get(position);
		
		if (tv == null)
			{
			tv = new TextView(mContext);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(Twix_Theme.headerSize);
			}
		
		tv.setTextColor(Twix_Theme.headerValue);
		
		tv.setText(data.MechName);
		tv.setPadding(4, 4, 4, 4);
		
		return tv;
		}
	
	public int size()
		{
		return items.size();
		}
	
	public List<EquipmentCategory> getItemList()
		{
		return items;
		}
	
	public Context getContext()
		{
		return this.mContext;
		}
	
	/*public int getIndexOfId(int find)
		{
		int ret = 0;
		int size = items.size();
		for( int i = 0; i < size; i++ )
			{
			if( find == items.get(i).MechanicId )
				{
				ret = i;
				break;
				}
			}
		
		return ret;
		}*/
	}

public class RefArrayAdapter extends ArrayAdapter<RefCategory>
	{
	private List<RefCategory>	items;
	private Context			mContext;
	private LinearLayout.LayoutParams layoutParams;
	
	public RefArrayAdapter(Context c, List<RefCategory> items)
		{
		super(c, R.layout.spinner_popup, items);
		this.items = items;
		this.mContext = c;
		this.layoutParams = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
	
	// The View displayed for selecting in the list
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
		RefCategory data = items.get(position);
		TextView tv = (TextView) super.getView(position, convertView, parent);
		
		// If the layout inflater fails/DropDown not set
		if (tv == null)
			{
			tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
			//tv = new TextView(mContext);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(Twix_Theme.headerSize);
			}
		
		tv.setTextColor(Twix_Theme.headerValue);
		tv.setText(data.RefName);
		
		
		sref.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         //   String selectedItemText = (String) parent.getItemAtPosition(position);
        RefCategory data = items.get(position);
            // Notify the selected item text
            Toast.makeText
                    (getApplicationContext(), "Selected : " + data.RefName + " - " + data.RefId, Toast.LENGTH_SHORT)
                    .show();
           //TechName.setAdapter(null);
           tref.setText(data.RefId);
            bref.setText(data.RefName);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    });
	return tv;
	
	}

	@Override
	public RefCategory getItem(int position)
		{
		return items.get(position);
		}
	
	// Views displayed AFTER selecting
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
		{
		TextView tv = (TextView) convertView;
		RefCategory data = items.get(position);
		
		if (tv == null)
			{
			tv = new TextView(mContext);
			tv.setLayoutParams(layoutParams);
			tv.setTextSize(Twix_Theme.headerSize);
			}
		
		tv.setTextColor(Twix_Theme.headerValue);
		
		tv.setText(data.RefName);
		tv.setPadding(4, 4, 4, 4);
		
		return tv;
		}
	
	public int size()
		{
		return items.size();
		}
	
	public List<RefCategory> getItemList()
		{
		return items;
		}
	
	public Context getContext()
		{
		return this.mContext;
		}
	
	/*public int getIndexOfId(int find)
		{
		int ret = 0;
		int size = items.size();
		for( int i = 0; i < size; i++ )
			{
			if( find == items.get(i).MechanicId )
				{
				ret = i;
				break;
				}
			}
		
		return ret;
		}*/
	}
private class DateData
	{
	public int day = 1;
	public int month = 0;
	public int year = 1900;
	
	public DateData(String s)
		{
		try
			{
			if( s != null && s.length() >= 10 )
				{
				year = Integer.parseInt(s.substring(0, 4));
    			month = Integer.parseInt(s.substring(5, 7));
    			month--;
    			day = Integer.parseInt(s.substring(8, 10));
				}
			}
		catch (Exception e)
			{
			Calendar c = Calendar.getInstance();
			day = c.get(Calendar.DAY_OF_MONTH);
			month = c.get(Calendar.MONTH);
			year = c.get(Calendar.YEAR);
			Log.w("twix_agent:Twix_AgentEquipmentDetail_Edit", "Error parsing Date Data. Input is '" + s + "'. Error: " + e.getMessage(), e);
			}
		}
	
	private boolean isBlank()
		{
		return ( (day == 1) && (month == 0) && (year == 1900) );
		}
	
	public void clear()
		{
		day = 1;
		month = 0;
		year = 1900;
		}
	
	public String DBformat()
		{
		if( isBlank() )
			return null;
		
		String date = year + "-";
			
    	if( (month+1) < 10 )
    		date += "0";
    	date += (month+1) + "-";
    	
    	if( day < 10 )
    		date += "0";
    	date += day;
		
		return date;
		}
	
	public String NormalFormat()
		{
		String date = "";
		
		if( !isBlank() )
			{
			if( (month+1) < 10 )
    			date += "0";
    		date += (month+1) + "/";
    		
    		if( day < 10 )
    			date += "0";
    		date += day + "/";
    		
    		date += year;
			}
		else
			date += "Not Entered";
		
		return date;
		}
	}
	
private void buildDirty()
	{
	dirtySelect = new OnItemSelectedListener()
		{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int arg2, long arg3)
			{
			if( rowCount <= currentCount )
				unitAct.dirtyFlag = true;
			else
				currentCount++;
			
			arg0.invalidate();
			}
		@Override
		public void onNothingSelected(AdapterView<?> arg0)
			{
			arg0.invalidate();
			}
		};
	}
public void updateDB()
	{
	db.delete("serviceRefrigerant", "serviceTagUnitId", unitAct.serviceTagUnitId );
	ContentValues cv = new ContentValues();
	RefrigerantData rd = new RefrigerantData();
	
	
	
	
	if( !isEmpty() )
		{
	cv.put("serviceRefrigerantId",	 db.newNegativeId("serviceRefrigerant", "serviceRefrigerantId") );
	cv.put("serviceTagUnitId",	unitAct.serviceTagUnitId );
	cv.put("transferDate",			DateInService.getText().toString() );
	cv.put("techName",			 Tech.getText().toString());
	cv.put("typeOfRefrigerant",	tref.getText().toString() );
	cv.put("amount",			Amount.getText().toString());
	cv.put("nameOfCylinder",			Cylinder.getText().toString());
	cv.put("cylinderSerialNo",		CylinderSerial.getText().toString());
	cv.put("transferedTo",				TransferedTo.getText().toString());
	cv.put("serialNo",			SerialNo.getText().toString() );
	cv.put("modelNo",			ModelNo.getText().toString() );
	
	db.db.insertOrThrow("serviceRefrigerant", null, cv);
	cv.clear();
		}
	   
}

public void onResume()
	{
	//((TextView)findViewById(R.id.UnitInfo)).setText( (String) UnitInfo.getSelectedItem() );
	((TextView)findViewById(R.id.UnitInfo)).setText( UnitInfo.getText() );
	
	super.onResume();
	}

/**
 * Force the activity to use the activity group's provided back functionality
 */
@Override
public void onBackPressed()
	{
	((Twix_TabActivityGroup)mContext).onBackPressed();
	}



private void applydata(RefrigerantData row)
{
//Category.setSelection( ((CategoryArrayAdapter)Category.getAdapter()).getIndexOfId(data.equipmentCategoryId) );

DateInService.setText(row.tdate);
e.setText(row.mechname);
if(row.mechname != "")
	{
	e.setText(row.mechname);
	Tech.setText(row.mechanicid);

	}
else
	{
	String S = "Select mechanic, mechanic_name from mechanic where mechanic ='" + app.empno + "'";
	 Cursor cursor = db.rawQuery(S);
	  if ( cursor.moveToFirst() )
		  {
		  
		  String emp = Twix_TextFunctions.clean(cursor.getString(0));
		  String ename = Twix_TextFunctions.clean(cursor.getString(1));
		  e.setText(ename);
		  Tech.setText(emp);
		  }
	  else
		  {
		  
		  }
	
	}

	bref.setText(row.reftype);
	tref.setText(row.refid);
	Amount.setText(row.amount);
	ModelNo.setText(row.modelno);
	SerialNo.setText(row.serialno);
	Cylinder.setText(row.nameofcylinder);
	CylinderSerial.setText(row.cylinderserialno);
	TransferedTo.setText(row.transferedto);
		
}
private int GetAdapterIndex(SpinnerAdapter adapter, String s)
	{
	int size = adapter.getCount();
	for( int i = 0; i < size; i++ )
		{
		if( s.contentEquals( (String)adapter.getItem(i) ) )
			return i;
		}
	
	return 0;
	}

public class MechanicArrayAdapter extends ArrayAdapter<MechanicData>
{
		private List<MechanicData>	items;
		private Context			mContext;
		private LinearLayout.LayoutParams layoutParams;
		
		public MechanicArrayAdapter(Context c, List<MechanicData> items)
			{
			super(c, R.layout.spinner_popup, items);
			this.items = items;
			this.mContext = c;
			this.layoutParams = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			}
		
		// The View displayed for selecting in the list
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
			MechanicData data = items.get(position);
			TextView tv = (TextView) super.getView(position, convertView, parent);
			
			// If the layout inflater fails/DropDown not set
			if (tv == null)
				{
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				//tv = new TextView(mContext);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(Twix_Theme.headerSize);
				}
			
			tv.setTextColor(Twix_Theme.headerValue);
			tv.setText(data.name);
			if( data.refDept )
				tv.setBackgroundColor(Twix_Theme.editBG);
			else
				tv.setBackgroundColor(Twix_Theme.disabledColorBG);
			
			return tv;
}

		
private class DateData
	{
	public int day = 1;
	public int month = 0;
	public int year = 1900;
	
	public DateData(String s)
		{
		try
			{
			if( s != null && s.length() >= 10 )
				{
				year = Integer.parseInt(s.substring(0, 4));
    			month = Integer.parseInt(s.substring(5, 7));
    			month--;
    			day = Integer.parseInt(s.substring(8, 10));
				}
			}
		catch (Exception e)
			{
			Calendar c = Calendar.getInstance();
			day = c.get(Calendar.DAY_OF_MONTH);
			month = c.get(Calendar.MONTH);
			year = c.get(Calendar.YEAR);
			Log.w("twix_agent:Twix_AgentEquipmentDetail_Edit", "Error parsing Date Data. Input is '" + s + "'. Error: " + e.getMessage(), e);
			}
		}
	
	private boolean isBlank()
		{
		return ( (day == 1) && (month == 0) && (year == 1900) );
		}
	
	public void clear()
		{
		day = 1;
		month = 0;
		year = 1900;
		}
	
	public String DBformat()
		{
		if( isBlank() )
			return null;
		
		String date = year + "-";
			
    	if( (month+1) < 10 )
    		date += "0";
    	date += (month+1) + "-";
    	
    	if( day < 10 )
    		date += "0";
    	date += day;
		
		return date;
		}
	
	public String NormalFormat()
		{
		String date = "";
		
		if( !isBlank() )
			{
			if( (month+1) < 10 )
    			date += "0";
    		date += (month+1) + "/";
    		
    		if( day < 10 )
    			date += "0";
    		date += day + "/";
    		
    		date += year;
			}
		else
			date += "Not Entered";
		
		return date;
		}
}

}}
	
		
	
	