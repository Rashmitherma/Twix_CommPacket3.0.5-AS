package com.twix_agent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentEquipmentDetail_Edit
 * 
 * Purpose: Lists all the static equipment details and allows the user to edit them. Validation is done on key
 * 			fields. This is the main page for equipment details.
 * 
 * Relevant XML: equipment_detail_edit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentEquipmentDetail_Edit extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Twix_AgentEquipmentTabHost_Edit eqAct;
	private OnItemSelectedListener dirtySelect;
	private OnCheckedChangeListener dirtyCheck;
	
	private Context mContext;
	static final int DATE_DIALOG_ID = 0;
	private static final int	REQUEST_PATH	= 1;
	private ArrayAdapter<CharSequence> adapterCategory;
	private ArrayAdapter<CharSequence> adapterRefrigerant;
	private ArrayAdapter<CharSequence> adapterCapacity;
	private ArrayAdapter<CharSequence> adapterVoltage;
	private ArrayAdapter<CharSequence> adapterPMType;
	private ArrayAdapter<CharSequence> adapterManufacturer;
	
	private OnClickListener dropClick;
	private OnClickListener attachphoto;
	private OnFocusChangeListener dropFocus;
	
	public TextView equipmentDetail;
	private TextWatcher updateFooter;
	private TextView unitNo;
	private int currentCategory = -1;
	private int currentRefrigerant = -1;
	private int currentPMFreq = -1;
	private boolean init = false;
	
	// Screen Fields
	private Spinner Category;
	private EditText UnitNo;
	private EditText AreaServed;
	private EditText Economizer;
	private Spinner RefrigerantType;
	private AutoCompleteTextView Manufacturer;
	private EditText Model;
	private Spinner PMFrequency;
	private AutoCompleteTextView Voltage;
	private EditText Capacity;
	private AutoCompleteTextView CapacityUnits;
	private EditText SerialNo;
	private EditText MfgYear;
	private TextView DateInService;
	private TextView DateOutService;
	private EditText BarCode;
	private CheckBox Verified;
	private EditText Notes;
	
	private TextView LastVerifiedBy;
	
	ArrayList<View> FieldList;
	
	private class EquipmentData
		{
		@SuppressWarnings("unused")
		String EquipmentCategory;
		int equipmentCategoryId;
		String unitNo;
		String AreaServed;
		String Economizer;
		String RefrigerantType;
		String Manufacturer;
		String Model;
		String AdditionalInfo = "NOT PM EQUIPMENT"; // Default the PM Frequency
		String Voltage;	
		float Capacity;
		String CapacityUnit;
		String SerialNo;
		String MfgYear;
		String DateInService;
		String DateOutOfService;
		String BarCode;
		String Notes;
		
		String Mechanic;
		boolean Verified;
		}
	
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(getParent().getParent()).inflate(R.layout.equipment_detail_edit, null);
		this.setContentView(viewToLoad);
		
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        eqAct = (Twix_AgentEquipmentTabHost_Edit)getParent();
		
        equipmentDetail = ((Twix_AgentEquipmentTabHost_Edit)getParent()).equipmentDetail;
        unitNo = (TextView) findViewById(R.id.Text_UnitNo);
        
        FieldList = new ArrayList<View>();
        
        Category		= (Spinner) findViewById(R.id.Text_EquipmentCategory);
        UnitNo			= (EditText) findViewById(R.id.Text_UnitNo);
        AreaServed		= (EditText) findViewById(R.id.Text_AreaServed);
        Economizer		= (EditText) findViewById(R.id.Text_Economizer);
        RefrigerantType	= (Spinner) findViewById(R.id.Text_RefrigerantType);
        Manufacturer	= (AutoCompleteTextView) findViewById(R.id.Text_Manufacturer);
        Model			= (EditText) findViewById(R.id.Text_Model);
        PMFrequency		= (Spinner) findViewById(R.id.Text_AdditionalInfo);
        Voltage			= (AutoCompleteTextView) findViewById(R.id.Text_Voltage);
        Capacity		= (EditText) findViewById(R.id.Text_Capacity);
        CapacityUnits	= (AutoCompleteTextView) findViewById(R.id.Auto_Capacity);
        SerialNo		= (EditText) findViewById(R.id.Text_SerialNo);
        MfgYear			= (EditText) findViewById(R.id.Text_MfgYear);
        DateInService	= (TextView) findViewById(R.id.Text_DateInService);
        DateOutService	= (TextView) findViewById(R.id.Text_DateOutOfService);
        BarCode			= (EditText) findViewById(R.id.Text_BarCode);
        Verified		= (CheckBox) findViewById(R.id.Verified);
        Notes			= (EditText) findViewById(R.id.Text_Notes);
        
        LastVerifiedBy	= (TextView) findViewById(R.id.LastVerified);
        
        FieldList.add(Category);
		FieldList.add(UnitNo);
		FieldList.add(AreaServed);
		FieldList.add(Economizer);
		FieldList.add(RefrigerantType);
		FieldList.add(Manufacturer);
		FieldList.add(Model);
		FieldList.add(PMFrequency);
		FieldList.add(Voltage);
		FieldList.add(Capacity);
		FieldList.add(CapacityUnits);
		FieldList.add(SerialNo);
		FieldList.add(MfgYear);
		FieldList.add(DateInService);
		FieldList.add(DateOutService);
		FieldList.add(BarCode);
		FieldList.add(Verified);
		FieldList.add(Notes);
        
		setClickListeners();
		
		buildAdapters();
		
		if( !eqAct.readOnly )
			{
			Verified.setOnCheckedChangeListener(dirtyCheck);
			createBarCodeOnClick();
			}
		else
			{
			Verified.setEnabled(false);
			findViewById(R.id.Verified).setEnabled(false);
			findViewById(R.id.Button_Scanner).setEnabled(false);
			}
		
		readSQL();
		
		int size = FieldList.size();
		for( int i = 0; i < size; i++ )
			{
			View v = FieldList.get(i);
			if( v instanceof EditText )
				((EditText)v).addTextChangedListener(eqAct.setDirtyFlag);
			else if( v instanceof AutoCompleteTextView )
				{
				((AutoCompleteTextView)v).addTextChangedListener(eqAct.setDirtyFlag);
				((AutoCompleteTextView)v).setThreshold(0);
				((AutoCompleteTextView)v).setOnFocusChangeListener(dropFocus);
				((AutoCompleteTextView)v).setOnClickListener(dropClick);
				}
			else if( v instanceof Spinner )
				((Spinner)v).setOnItemSelectedListener(dirtySelect);
				
			v.setEnabled( !eqAct.readOnly );
			}
		
		SerialNo.addTextChangedListener(eqAct.upShiftText);
		Model.addTextChangedListener(eqAct.upShiftText);
		}
	
	public void createBarCodeOnClick()
		{
		findViewById(R.id.Button_Scanner).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	// QR Droid Barcode Scanner
            	Intent intent = new Intent( "qr.code.barcode.reader.scanner" );
            	intent.setPackage("qr.code.barcode.reader.scanner");
            	intent.putExtra( "qr.code.barcode.reader.scanner.complete", true);
            	try
            		{
            		((Twix_TabActivityGroup)mContext).startActivityForResult( intent, Twix_AgentActivityGroup3.SCAN_BARCODE );
            		}
            	catch (ActivityNotFoundException e)
            		{
            		Toast.makeText(mContext,
            				"Cannot Find Bar Code scanner ggggg. Please install QR Scanner from the Android Market and try again",
            				Toast.LENGTH_LONG).show();
            		}
                }
        	});
		}
	
	public void setBarCode( String s )
		{
		((EditText)findViewById(R.id.Text_BarCode)).setText(s);
		}
	
	private void buildAdapters()
		{
		// Setup the listeners
		dropClick = new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				((AutoCompleteTextView)v).showDropDown();
				
				}
    		};
		dropFocus = new View.OnFocusChangeListener()
    		{
			@Override
			public void onFocusChange(View v,
					boolean hasFocus)
				{
				if( hasFocus )
					((AutoCompleteTextView)v).showDropDown();
				}
    		};
		
    	attachphoto =  new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				openGallery();
				
				}
    		};
    		
    		
    		
    		
		CapacityUnits.setAdapter(Twix_TextFunctions.BuildAdapter(db, mContext, "Capacity Units"));
		PMFrequency.setAdapter(Twix_TextFunctions.BuildAdapter(db, mContext, "PM Frequency"));
		Voltage.setAdapter(Twix_TextFunctions.BuildAdapter(db, mContext, "Voltages"));
		RefrigerantType.setAdapter(Twix_TextFunctions.BuildAdapter(db, mContext, "Refrigerant Type"));
		Manufacturer.setAdapter(Twix_TextFunctions.BuildAdapter(db, mContext, "Equipment Manufacturer"));
		
		// Capacity Adapter
		CapacityUnits.setThreshold(0);
		CapacityUnits.setOnFocusChangeListener(dropFocus);
		CapacityUnits.setOnClickListener(dropClick);
		
		// Voltage Adapter
		Voltage.setThreshold(0);
		Voltage.setOnFocusChangeListener(dropFocus);
		Voltage.setOnClickListener(dropClick);
		
		// Manufacturer Adapter
		Manufacturer.setOnFocusChangeListener(dropFocus);
		Manufacturer.setOnClickListener(dropClick);
		
		// Build more adapters
		buildEquipmentCategory();
		
		
		//buildAutoCaps();
		buildUpdateFooter();
		buildDirtySelector();
		}
	
	private void buildEquipmentCategory()
		{
    	String sqlQ = "SELECT equipmentCategoryId, categoryDesc FROM equipmentCategory ORDER BY categoryDesc";
    	Cursor cursor = db.rawQuery(sqlQ);
    	ArrayList<EquipmentCategory> Categories = new ArrayList<EquipmentCategory>();
    	while (cursor.moveToNext())
    		Categories.add( new EquipmentCategory(cursor.getInt(0), cursor.getString(1)) );
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		CategoryArrayAdapter adapter = new CategoryArrayAdapter(mContext, Categories);
    	Category.setAdapter(adapter);
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
	
	private void setClickListeners()
		{
		OnClickListener dateClick = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				final TextView tv = (TextView) v;
				final DateData dateData = (DateData) tv.getTag();
				
				Dialog dialog;
				
				if( !dateData.isBlank() )
					{
					dialog = new DatePickerDialog(mContext,
							new DatePickerDialog.OnDateSetListener()
						{
						public void onDateSet(DatePicker view, int y, int m, int d)
							{
							eqAct.dirtyFlag = true;
							dateData.year = y;
							dateData.month = m;
							dateData.day = d;
							
							tv.setText(dateData.NormalFormat());
							}
						},
						dateData.year, dateData.month, dateData.day);
					}
				else
					{
					Calendar c = Calendar.getInstance();
					dialog = new DatePickerDialog(mContext,
							new DatePickerDialog.OnDateSetListener()
						{
						public void onDateSet(DatePicker view, int y, int m, int d)
							{
							eqAct.dirtyFlag = true;
							dateData.year = y;
							dateData.month = m;
							dateData.day = d;
							
							tv.setText(dateData.NormalFormat());
							}
						},
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH) );
					}
				
				
				dialog.show();
				
				}
			}
		;
		
		DateInService.setOnClickListener(dateClick);
		DateOutService.setOnClickListener(dateClick);
		
		Button clearDateInService = (Button) findViewById(R.id.DateInService_Clear);
		clearDateInService.setTag(DateInService);
		Button clearDateOutService = (Button) findViewById(R.id.DateOutOfService_Clear);
		clearDateOutService.setTag(DateOutService);
		
		OnClickListener clearDate = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				TextView tv = (TextView)v.getTag();
				tv.setText("Not Entered");
				
				DateData dateData = (DateData) tv.getTag();
				dateData.clear();
				eqAct.dirtyFlag = true;
				}
			}
		;	
		clearDateInService.setOnClickListener(clearDate);
		clearDateOutService.setOnClickListener(clearDate);
		}
	private void openGallery() {
	 
	 Intent intent1 = new Intent(this, filechooser.class);
	 startActivityForResult(intent1,REQUEST_PATH);
	 
	 }
	
	private void buildUpdateFooter()
		{
		updateFooter = new TextWatcher()
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
				equipmentDetail.setText( ((EquipmentCategory)Category.getSelectedItem()).CategoryDesc + " - " + s.toString());
				}

			};
		
		unitNo.addTextChangedListener(updateFooter);
		}
	
	private void buildDirtySelector()
		{
		dirtySelect = new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
				{
				
				if( init )
					{
					if( arg0.getAdapter() == adapterCategory )
						{
						if( (currentCategory != arg2) && (currentCategory != -1) )
							{
							eqAct.dirtyFlag = true;
							equipmentDetail.setText( ((String)arg0.getSelectedItem()) + " - " + unitNo.getText() );
							}
						currentCategory = arg2;
						}
					else if( arg0.getAdapter() == adapterRefrigerant )
						{
						if( (currentRefrigerant != arg2) && (currentRefrigerant != -1) )
							{
							eqAct.dirtyFlag = true;
							}
						currentRefrigerant = arg2;
						}
					else if( arg0.getAdapter() == adapterPMType )
						{
						if( (currentPMFreq != arg2) && (currentPMFreq != -1) )
							{
							eqAct.dirtyFlag = true;
							}
						currentPMFreq = arg2;
						}
					}
				
				}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				}
			
			};
		
		dirtyCheck = new OnCheckedChangeListener()
			{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
				{
				if( init )
					eqAct.dirtyFlag = true;
				}
			
			};
		}
	
	private void readSQL()
	    {
	    if( eqAct.equipmentId == 0 && eqAct.serviceAddressId == 0 )
	    	{
			DateData datedata = new DateData("");
			DateInService.setTag(datedata);
			DateInService.setText(datedata.NormalFormat());
			
			datedata = new DateData("");
			DateInService.setTag(datedata);
			DateInService.setText(datedata.NormalFormat());
			
	    	return;
	    	}
	    
    	String sqlQ = "SELECT	ec.CategoryDesc, e.equipmentCategoryId, e.UnitNo, e.AreaServed, e.Economizer, " + 
							"e.RefrigerantType, e.Manufacturer, e.Model, e.ProductIdentifier, " + 
							"e.Voltage, e.Capacity, e.CapacityUnits, e.SerialNo, e.MfgYear, " +
							"e.DateInService, e.DateOutService, e.BarCodeNo, e.Notes, " +
							"m.mechanic_name, e.verified " + 
		
							"FROM Equipment as e " +
								"LEFT OUTER JOIN EquipmentCategory as ec " +
									"on e.EquipmentCategoryId = ec.EquipmentCategoryId " +
								"LEFT OUTER JOIN mechanic as m " +
									"on m.mechanic = e.verifiedByEmpno " + 
							"WHERE e.equipmentId = " + eqAct.equipmentId;
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	String s = "";
    	DateData datedata;
    	
    	EquipmentData data;
    	int index = 0;
		if (cursor.moveToFirst())
			{
			data = new EquipmentData();
			data.EquipmentCategory = Twix_TextFunctions.clean(cursor.getString(0));
			data.equipmentCategoryId = cursor.getInt(1);
			data.unitNo = cursor.getString(2);
			data.AreaServed = cursor.getString(3);
			data.Economizer = cursor.getString(4);
			data.RefrigerantType = Twix_TextFunctions.clean(cursor.getString(5));
			data.Manufacturer = cursor.getString(6);
			data.Model = cursor.getString(7);
			s = Twix_TextFunctions.clean(cursor.getString(8));
			if( s.length() <= 0 )
				data.AdditionalInfo = "NOT PM EQUIPMENT";
			else
			data.AdditionalInfo = s;
			data.Voltage = cursor.getString(9);
			data.Capacity = cursor.getFloat(10);
			data.CapacityUnit = cursor.getString(11);
			data.SerialNo = cursor.getString(12);
			data.MfgYear = cursor.getString(13);
			data.DateInService = cursor.getString(14);
			data.DateOutOfService = cursor.getString(15);
			data.BarCode = cursor.getString(16);
			data.Notes = cursor.getString(17);
			
			data.Mechanic = cursor.getString(18);
			s = cursor.getString(19);
			data.Verified = (s != null && s.contentEquals(app.empno));
			
			//Apply the SQL Data
			ApplyData(data);
			}
		else
			{
			datedata = new DateData("");
			DateInService.setTag(datedata);
			DateInService.setText(datedata.NormalFormat());
			
			datedata = new DateData("");
			DateOutService.setTag(datedata);
			DateOutService.setText(datedata.NormalFormat());
			}
		
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		init = true;
	    }
	
	private void ApplyData(EquipmentData data)
		{
		Category.setSelection( ((CategoryArrayAdapter)Category.getAdapter()).getIndexOfId(data.equipmentCategoryId) );
		UnitNo.setText(data.unitNo);
		AreaServed.setText(data.AreaServed);
		Economizer.setText(data.Economizer);
		RefrigerantType.setSelection(GetAdapterIndex(RefrigerantType.getAdapter(), data.RefrigerantType));
		Manufacturer.setText(data.Manufacturer);
		Model.setText(data.Model);
		PMFrequency.setSelection(GetAdapterIndex(PMFrequency.getAdapter(), data.AdditionalInfo));
		Voltage.setText(data.Voltage);
		Capacity.setText(data.Capacity+"");
		CapacityUnits.setText(data.CapacityUnit);
		SerialNo.setText(data.SerialNo);
		MfgYear.setText(data.MfgYear);
		
		DateData datedata = new DateData(data.DateInService);
		DateInService.setTag(datedata);
		DateInService.setText(datedata.NormalFormat());
		
		datedata = new DateData(data.DateOutOfService);
		DateOutService.setTag(datedata);
		DateOutService.setText(datedata.NormalFormat());
		
		BarCode.setText(data.BarCode);
		Verified.setChecked(data.Verified);
		Notes.setText(data.Notes);
		
		LastVerifiedBy.setText(data.Mechanic);
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
	
	public boolean validateSave(List<String> error)
		{
		boolean valid = true;
		if( ((EquipmentCategory)Category.getSelectedItem()).EquipmentCategoryId < 1 )
			{
			valid = false;
			error.add("Category");
			((TextView) findViewById(R.id.Title_EquipmentCategory)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView) findViewById(R.id.Title_EquipmentCategory)).setTextColor(Twix_Theme.headerText);
			}
		
		if( UnitNo.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("Unit No");
			((TextView)findViewById(R.id.Title_UnitNo)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_UnitNo)).setTextColor(Twix_Theme.headerText);
			}
		
		/*
		tv = ((TextView)findViewById(R.id.Text_Manufacturer));
		if( tv.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("Manufacturer");
			((TextView)findViewById(R.id.Title_Manufacturer)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_Manufacturer)).setTextColor(Twix_Theme.headerText);
			}
		
		tv = ((TextView)findViewById(R.id.Text_Model));
		if( tv.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("Model");
			((TextView)findViewById(R.id.Title_Model)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_Model)).setTextColor(Twix_Theme.headerText);
			}
			
		tv = ((TextView)findViewById(R.id.Text_SerialNo));
		if( tv.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("Serial No");
			((TextView)findViewById(R.id.Title_SerialNo)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_SerialNo)).setTextColor(Twix_Theme.headerText);
			}
		
		tv = ((TextView)findViewById(R.id.Text_MfgYear));
		if( tv.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("Manufacturer Year");
			((TextView)findViewById(R.id.Title_MfgYear)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_MfgYear)).setTextColor(Twix_Theme.headerText);
			}
		*/
		//tv = ((TextView)findViewById(R.id.Text_Capacity));
		if( !Capacity.getText().toString().matches("^([0-9]{1,7}([.][0-9]{0,3}){0,1}){0,1}$") && Capacity.getText().length() > 0 )
			{
			valid = false;
			error.add("Capcity");
			((TextView)findViewById(R.id.Title_Capacity)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)findViewById(R.id.Title_Capacity)).setTextColor(Twix_Theme.headerText);
			}
		return valid;
		}
	
	public int updateDB()
		{
		ContentValues cv = new ContentValues();
		EquipmentData eq = new EquipmentData();
		
		int newId = 0;
		boolean isInsert = eqAct.equipmentId == 0;
		
		if( isInsert )
			{
			newId = db.newNegativeId("equipment", "equipmentId");
			cv.put("equipmentId", 		newId );
			cv.put("serviceAddressId",	eqAct.serviceAddressId );
			}
		else
			{
			cv.put("equipmentId",	eqAct.equipmentId );
			newId = eqAct.equipmentId;
			}
		
		String s;
		DateData dateData;
		eq.equipmentCategoryId	= ((EquipmentCategory)Category.getSelectedItem()).EquipmentCategoryId;
		eq.unitNo				= UnitNo.getText().toString();
		eq.AreaServed			= AreaServed.getText().toString();
		eq.Economizer			= Economizer.getText().toString();
		eq.RefrigerantType		= (String) RefrigerantType.getSelectedItem();
		eq.Manufacturer			= Manufacturer.getText().toString();
		eq.Model				= Model.getText().toString();
		eq.AdditionalInfo		= (String) PMFrequency.getSelectedItem();
		eq.Voltage				= Voltage.getText().toString();
		s = Capacity.getText().toString();
		if( s.length() > 0 )
			eq.Capacity			= Float.parseFloat( s );
		else
			eq.Capacity			= 0f;
		eq.CapacityUnit			= CapacityUnits.getText().toString();
		eq.SerialNo				= SerialNo.getText().toString();
		eq.MfgYear				= MfgYear.getText().toString();
		
		
		dateData = (DateData) DateInService.getTag();
		eq.DateInService = dateData.DBformat();
		
		dateData = (DateData) DateOutService.getTag();
		eq.DateOutOfService = dateData.DBformat();
		
		eq.BarCode				= BarCode.getText().toString();
		eq.Notes				= Notes.getText().toString();
		
		cv.put("equipmentCategoryId",	eq.equipmentCategoryId );
		cv.put("unitNo",				eq.unitNo );
		cv.put("areaServed",			eq.AreaServed );
		cv.put("economizer",			eq.Economizer );
		cv.put("refrigerantType",		eq.RefrigerantType );
		cv.put("manufacturer",			eq.Manufacturer );
		cv.put("model",					eq.Model );
		cv.put("productIdentifier",		eq.AdditionalInfo );
		cv.put("voltage",				eq.Voltage );
		cv.put("capacity",				eq.Capacity );
		cv.put("capacityUnits",			eq.CapacityUnit );
		cv.put("serialNo",				eq.SerialNo );
		cv.put("mfgYear",				eq.MfgYear );
		cv.put("dateInService",			eq.DateInService );
		cv.put("dateOutService",		eq.DateOutOfService );
		cv.put("barCodeNo",				eq.BarCode );
		cv.put("notes",					eq.Notes );
		cv.put("modified",				"Y" );
		
		if( ((CheckBox)findViewById(R.id.Verified)).isChecked() )
			cv.put("verified",	"Y" );
		else
			cv.put("verified",	"N" );
		
		// Explode if update or insert fails, no try catch
		if( isInsert )
			db.db.insertOrThrow("equipment", null, cv);
		else
			{
			db.update("equipment", cv, "equipmentId", eqAct.equipmentId );
			}
		
		return newId;
		}
	
	/**
	 * DO NOT call this function without knowing the consequences. It will delete the current equipment!
	 */
	public void deleteThis()
		{
		if( eqAct.serviceAddressId != 0 )
			{
			db.delete("Equipment", "EquipmentId", eqAct.equipmentId);
			((Twix_AgentActivityGroup3)mContext).finishActivity();
			}
		}
	
	public void deleteEquipment()
		{
		if( eqAct.equipmentId == 0 )
			return;
		
		db.delete("Equipment", "equipmentId", eqAct.equipmentId);
    	
		String sqlQ = "SELECT Fan.FanId FROM Fan " +
				"WHERE Fan.EquipmentId = " + eqAct.equipmentId + " ";

		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				db.delete( "Belt", "FanId", cursor.getString(0) );
				db.delete( "Sheave", "FanId", cursor.getString(0) );
				}
				while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		db.delete("Fan", "EquipmentId", eqAct.equipmentId);
    	db.delete("Filter", "EquipmentId", eqAct.equipmentId);
    	
    	sqlQ = "SELECT RefCircuit.CircuitId FROM RefCircuit " +
				"WHERE RefCircuit.EquipmentId = " + eqAct.equipmentId;

		cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				db.delete( "Compressor", "CircuitId", cursor.getString(0) );
				}
				while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
    	db.delete("RefCircuit", "EquipmentId", eqAct.equipmentId);
		}
	
	/**
	 * Force the activity to use the activity group's provided back functionality
	 */
	@Override
	public void onBackPressed()
		{
		((Twix_TabActivityGroup)mContext).onBackPressed();
		}
	
	private class EquipmentCategory
		{
		int EquipmentCategoryId;
		String CategoryDesc;
		
		public EquipmentCategory(int id, String desc)
			{
			EquipmentCategoryId = id;
			CategoryDesc = desc;
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
			tv.setText(data.CategoryDesc);
			
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
			
			tv.setText(data.CategoryDesc);
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
		
		public int getIndexOfId(int find)
			{
			int ret = 0;
			int size = items.size();
			for( int i = 0; i < size; i++ )
				{
				if( find == items.get(i).EquipmentCategoryId )
					{
					ret = i;
					break;
					}
				}
			
			return ret;
			}
		}
	}
