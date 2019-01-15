package com.twix_agent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentRefCircuits_Edit
 * 
 * Purpose: Allows the user to edit the Refrigeration Circuits on a piece of equipment.
 * 			Refrigeration Circuits are made up of:
 * 			- Ref Circuit details
 * 			- Up to four compressors
 * 
 * Relevant XML: refcircuits_edit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentRefCircuits_Edit extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_AgentEquipmentTabHost_Edit eqAct;
	private Twix_SQLite db;
	private Context mContext;
	private LinearLayout ll;
	private TextWatcher decWatcher;
	
    static final int DATE_DIALOG_ID = 0;
    static final int REMOVE_COMPRESSOR_PROMPT = 1;
    
    static final int PROMPT_REFCIRCUIT = 3;
    static final int PROMPT_COMPRESSOR = 4;
    
    private static final int MAX_COMPRESSOR = 4; //Allows 4 Compressors per RefCircuit
    
	private List<Integer> circuitIdList;
	
	private ArrayAdapter<CharSequence> adapterCompressorManufacturer;
	private OnClickListener dropClick;
	private OnFocusChangeListener dropFocus;
	
	// Input Filters
	private InputFilter[] IF2 = new InputFilter[1];
	private InputFilter[] IF4 = new InputFilter[1];
	private InputFilter[] IF20 = new InputFilter[1];
	private InputFilter[] IF50 = new InputFilter[1];
	private InputFilter[] IF9 = new InputFilter[1];
	private final InputFilter IF2ele = new InputFilter.LengthFilter(2);
	private final InputFilter IF4ele = new InputFilter.LengthFilter(4);
	private final InputFilter IF20ele = new InputFilter.LengthFilter(20); //Also used for Reals
	private final InputFilter IF50ele = new InputFilter.LengthFilter(50);
	private final InputFilter IF9ele = new InputFilter.LengthFilter(9); //For ints: 2147483647
	
	// Click Listeners
	private OnClickListener deleteCircuit;
	private OnClickListener deleteCompressor;
	private OnClickListener addCircuit;
	private OnClickListener addCompressor;
	private OnClickListener dateClick;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
        View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.refcircuits_edit, null);
        this.setContentView( viewToLoad );
        
        eqAct = (Twix_AgentEquipmentTabHost_Edit)getParent();
        
        ll = (LinearLayout) findViewById(R.id.TableHost);
        
        app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
        
        circuitIdList = new ArrayList<Integer>();
        
        // Prevent any button actions in read only mode
        if( !eqAct.readOnly )
        	{
        	createClickListeners();
	        setupInputFilters();
	        buildAdapters();
        	}
        else
        	{
        	findViewById(R.id.New_RefCircuit).setVisibility(View.GONE);
        	findViewById(R.id.Title_New_RefCircuit).setVisibility(View.GONE);
        	}
        
        readSQLClass();
    	}
	
	private void setupInputFilters()
		{
		IF2[0] = IF2ele;
		IF4[0] = IF4ele;
		IF20[0] = IF20ele;
		IF50[0] = IF50ele;
		IF9[0] = IF9ele;
		}
	
	private void buildAdapters()
		{
		adapterCompressorManufacturer = Twix_TextFunctions.BuildAdapter(db, mContext, "Compressor Manufacturer");
		
		dropClick = new View.OnClickListener()
    		{
    		public void onClick(View v)
    			{
    			((AutoCompleteTextView)v).showDropDown();
    			}
    		};
    	
    	dropFocus = new View.OnFocusChangeListener()
			{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
				{
				if( hasFocus )
					((AutoCompleteTextView)v).showDropDown();
				}
			};
		
		buildAutoCaps();
		}
	
	private void buildAutoCaps()
		{
		/*
		autoCaps = new TextWatcher()
			{
			@Override
			public void afterTextChanged(Editable s)
				{
				int index = s.length()-1;
				if( index < 0 )
					return;
				
				char c = s.charAt(index);
				if( Character.isLowerCase(c) )
					s.replace(s.length()-1, s.length(),  new String(Character.toChars(c-32)), 0, 1);
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
				}

			};
		*/
		
		decWatcher = new TextWatcher()
			{
	        @Override
	        public void afterTextChanged(Editable arg0)
	        	{
	        	String text = arg0.toString();
	        	if( !text.matches("^[0-9]{0,6}(\\.[0-9]{0,3}){0,1}$") )
	        		{
	        		text = text.substring(0, text.length()-1);
	        		arg0.clear();
	        		arg0.append(text);
	        		}
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
	        	}
	        
			};
		}
	
    private void removeOriginalEntries()
    	{
    	db.db.execSQL("DELETE FROM Compressor WHERE CircuitId IN (SELECT CircuitId FROM RefCircuit WHERE EquipmentId = " + eqAct.equipmentId + ")");
    	db.db.execSQL("DELETE FROM RefCircuit WHERE EquipmentId = " + eqAct.equipmentId);
    	
    	/* No Need to use bound Ids, we can just use a select statement
    	int size = circuitIdList.size();
    	for( int i = 0; i < size; i++ )
    		{
    		db.delete("RefCircuit", "CircuitId", circuitIdList.get(i));
    		db.delete("Compressor", "CircuitId", circuitIdList.get(i));
    		}
    	*/
    	}
    
    /*************************************************************************
     * 
     *	Edit Redone with Classes
     *
     ************************************************************************/
    
    // Classes to handle the data
	private class RefCirData
		{
		int CircuitId;
		String CircuitNo;
		String Refrig;
		List<CompressorData> Compressors;
		
		public RefCirData()
			{
			Compressors = new ArrayList<CompressorData>();
			}
		}
    
	private class CompressorData
		{
		String CompressorNo;
		String Manufacturer;
		String Model;
		String SerialNo;
		String DateInService;
		String DateOutOfService;
		}
	
	private class RefCirRow
		{
		LinearLayout Table;
		LinearLayout Children;
		LinearLayout AddCompressor;
		EditText CircuitNo;
		EditText Refrig;
		}
    
	private class CompressorRow
		{
		EditText CompressorNo;
		AutoCompleteTextView Manufacturer;
		EditText Model;
		EditText SerialNo;
		TextView DateInService;
		TextView DateOutOfService;
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
				if( s.length() >= 10 )
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
				Log.w("twix_agent:Twix_AgentRefCircuits_Edit", "Error parsing Date Data. Input is '" + s + "'. Error: " + e.getMessage(), e);
				}
			}
		
		private boolean isBlank()
			{
			return ( (day == 1) && (month == 0) && (year == 1900) );
			}
		
		public String DBformat()
			{
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
	
	private void readSQLClass()
	    {
    	String sqlQ = "SELECT CircuitId, CircuitNo, LbsRefrigerant " + 
						"FROM RefCircuit WHERE equipmentId = " + eqAct.equipmentId;
    	Cursor cursorRefCircuit = db.rawQuery(sqlQ);
    	Cursor cursorCompressor;
    	
    	LayoutParams params = new LayoutParams();
    	params.width = LayoutParams.MATCH_PARENT;
    	params.height = LayoutParams.WRAP_CONTENT;
    	params.setMargins(3, 3, 3, 10);
    	
		if (cursorRefCircuit.moveToFirst())
			{
			RefCirData refCirData;
			RefCirRow vTable;
			CompressorData compData;
			do
				{
				refCirData = new RefCirData();
				refCirData.CircuitId	= cursorRefCircuit.getInt(0);
				refCirData.CircuitNo	= Twix_TextFunctions.clean(cursorRefCircuit.getString(1));
				refCirData.Refrig		= Twix_TextFunctions.clean(cursorRefCircuit.getString(2));
				
				sqlQ = 	"SELECT CompressorNo, DateInService, " +
						"DateOutService, Manufacturer, Model, " +
						"SerialNo " +
						"FROM Compressor WHERE CircuitId = " + refCirData.CircuitId;
				cursorCompressor = db.rawQuery(sqlQ);
				if (cursorCompressor.moveToFirst())
					{
					do
						{
						compData = new CompressorData();
						compData.CompressorNo		= Twix_TextFunctions.clean(cursorCompressor.getString(0));
						compData.DateInService		= Twix_TextFunctions.clean(cursorCompressor.getString(1));
						compData.DateOutOfService	= Twix_TextFunctions.clean(cursorCompressor.getString(2));
						compData.Manufacturer		= Twix_TextFunctions.clean(cursorCompressor.getString(3));
						compData.Model				= Twix_TextFunctions.clean(cursorCompressor.getString(4));
						compData.SerialNo			= Twix_TextFunctions.clean(cursorCompressor.getString(5));
						refCirData.Compressors.add(compData);
						}
					while (cursorCompressor.moveToNext());
					}
				if (cursorCompressor != null && !cursorCompressor.isClosed())
					{
					cursorCompressor.close();
					}
				
				// Add the id so we can delete the old records later
				circuitIdList.add(refCirData.CircuitId);
				// Create build all the rows
				vTable = createRefCircuitRow(refCirData);
				// Add the resulting table to the host table
				ll.addView(vTable.Table);
				}
			while (cursorRefCircuit.moveToNext());
			}
		if (cursorRefCircuit != null && !cursorRefCircuit.isClosed())
			{
			cursorRefCircuit.close();
			}
	    }
	
	private void createClickListeners()
		{
		deleteCircuit = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				final View bn = v;
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to delete this Refrigeration Circuit?")
						.setCancelable(true)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								eqAct.dirtyFlag = true;
								RefCirRow row = (RefCirRow) ((View)bn.getParent().getParent()).getTag();
								ll.removeView(row.Table);
								
								dialog.dismiss();
								}
							})
						.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								dialog.dismiss();
								}
							});
				AlertDialog alert = builder.create();
				alert.show();
				}
			}
		;
		
		deleteCompressor = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				final View bn = v;
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to delete this Compressor?")
						.setCancelable(true)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								eqAct.dirtyFlag = true;
								RefCirRow host = (RefCirRow) ((View)bn.getParent().getParent().getParent().getParent()).getTag();
								
								host.Children.removeView( (View) bn.getParent().getParent() );
								if( host.Children.getChildCount() < MAX_COMPRESSOR)
									host.AddCompressor.setVisibility(View.VISIBLE);
								
								dialog.dismiss();
								}
							})
						.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								dialog.dismiss();
								}
							});
				AlertDialog alert = builder.create();
				alert.show();
				}
			}
		;
		
		addCircuit = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				eqAct.dirtyFlag = true;
				RefCirData data = new RefCirData();
				data.CircuitNo = "";
				data.Refrig = "";
				
				ll.addView( createRefCircuitRow(data).Table );
				}
			}
		;
		
		addCompressor = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				eqAct.dirtyFlag = true;
				CompressorData data = new CompressorData();
				data.CompressorNo = "";
				data.Manufacturer = "";
				data.Model = "";
				data.SerialNo = "";
				data.DateInService = "";
				data.DateOutOfService = "";
				
				RefCirRow host = (RefCirRow) ((View)v.getParent().getParent()).getTag();
				
				host.Children.addView( createCompressorRow( data ) );//, host.Compressors) );
				
				if( host.Children.getChildCount() >= MAX_COMPRESSOR )
					{
					host.AddCompressor.setVisibility(View.GONE);
					}
				}
			}
		;
		
		ImageButton ib = (ImageButton)findViewById(R.id.New_RefCircuit);
		ib.setOnClickListener(addCircuit);
		
		dateClick = new OnClickListener()
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
		
		}
	
	private RefCirRow createRefCircuitRow(RefCirData data)
		{
		RefCirRow vTable	= new RefCirRow();
		vTable.CircuitNo	= createET( data.CircuitNo, true, false, 1f, IF4, InputType.TYPE_CLASS_TEXT );
		vTable.Refrig		= createET( data.Refrig, true, false, 1f, IF9, InputType.TYPE_CLASS_TEXT );
		if( !eqAct.readOnly )
			{
			//vTable.CircuitNo.addTextChangedListener(autoCaps);
			vTable.Refrig.setInputType(InputType.TYPE_CLASS_PHONE);
			vTable.Refrig.addTextChangedListener(decWatcher);
			}
		
		vTable.Table = createTable();
		vTable.Table.setTag(vTable);
		vTable.Table.setBackgroundColor(Twix_Theme.tableBG);
		((LinearLayout.LayoutParams)vTable.Table.getLayoutParams()).setMargins(3,6,3,6);
		
		LinearLayout row = createRow();
		row.setBackgroundColor(Twix_Theme.headerBG);
		if( !eqAct.readOnly )
			{
			ImageButton bn = new ImageButton(mContext);
			LinearLayout.LayoutParams bnParams = new LinearLayout.LayoutParams( 35, 35 );
			bnParams.gravity = Gravity.CENTER;
			bn.setLayoutParams(bnParams);
			bn.setOnClickListener(deleteCircuit);
			bn.setImageResource(R.drawable.minus);
			bn.setBackgroundColor(0x00000000);
			row.addView(bn);
			}
			
			row.addView(createTV( "Circuit No: ", true, true, 1f ));
			row.addView(vTable.CircuitNo);
			row.addView(createTV( "Lbs of Refrigerant: ", true, true, 1f ));
			row.addView(vTable.Refrig);
		vTable.Table.addView(row);
		
		vTable.Children = createTable();
		int size = data.Compressors.size();
		for( int i = 0; i < size; i++ )
			{
			vTable.Children.addView( createCompressorRow(data.Compressors.get(i)) );
			}
		vTable.Table.addView( vTable.Children );
		
		if( !eqAct.readOnly )
			{
			vTable.AddCompressor = createAddCompressor();
			vTable.Table.addView(vTable.AddCompressor);
			if( vTable.Children.getChildCount() >= MAX_COMPRESSOR || eqAct.readOnly )
				vTable.AddCompressor.setVisibility(View.GONE);
			}
		
		return vTable;
		}
	
	private LinearLayout createCompressorRow(CompressorData data)
		{
		LinearLayout ret = createTable();
		((LinearLayout.LayoutParams)ret.getLayoutParams()).setMargins(8,3,8,3);
		ret.setBackgroundColor(Twix_Theme.tableBG2);
		
		LinearLayout row;
		CompressorRow vRow = new CompressorRow();
		DateData dateData;
		
		vRow.CompressorNo = createET(data.CompressorNo, false, false, 1f, IF4, InputType.TYPE_CLASS_TEXT );
		//	vRow.CompressorNo.addTextChangedListener(autoCaps);
		vRow.Manufacturer = createAT(data.Manufacturer, false, adapterCompressorManufacturer, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
		vRow.Model = createET(data.Model, false, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
		vRow.Model.addTextChangedListener(eqAct.upShiftText);
		vRow.SerialNo = createET(data.SerialNo, false, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
		vRow.SerialNo.addTextChangedListener(eqAct.upShiftText);
		
		vRow.DateInService = createTV("", false, false, 1f );
			vRow.DateInService.setTextColor(Twix_Theme.headerValue);
			dateData = new DateData(data.DateInService);
			vRow.DateInService.setText(dateData.NormalFormat());
			if( !eqAct.readOnly )
				{
				vRow.DateInService.setTag(dateData);
				vRow.DateInService.setOnClickListener(dateClick);
				vRow.DateInService.setBackgroundResource(R.drawable.editbox);
				}
			
		vRow.DateOutOfService = createTV("", false, false, 1f );
			dateData = new DateData(data.DateOutOfService);
			vRow.DateOutOfService.setTextColor(Twix_Theme.headerValue);
			vRow.DateOutOfService.setText(dateData.NormalFormat());
			if( !eqAct.readOnly )
				{
				vRow.DateOutOfService.setTag(dateData);
				vRow.DateOutOfService.setOnClickListener(dateClick);
				vRow.DateOutOfService.setBackgroundResource(R.drawable.editbox);
				}
		
		row = createRow();
			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
		row.addView( createTV("Compressor No: ", false, false, 1f ) );
		row.addView( vRow.CompressorNo );
		row.addView( createTV("Manufacturer: ", false, false, 1f ) );
		row.addView( vRow.Manufacturer );
		ret.addView( row );
		
		row = createRow();
		if( !eqAct.readOnly )
			{
			ImageButton bn = new ImageButton(mContext);
			LinearLayout.LayoutParams bnParams = new LinearLayout.LayoutParams( 35, 35 );
			bnParams.gravity = Gravity.CENTER;
			bn.setLayoutParams(bnParams);
			bn.setOnClickListener(deleteCompressor);
			bn.setImageResource(R.drawable.minus);
			bn.setBackgroundColor(0x00000000);
			row.addView(bn);
			}
		else
			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
		row.addView( createTV("Model: ", false, true, 1f ) );
		row.addView( vRow.Model );
		row.addView( createTV("Serial No: ", false, true, 1f ) );
		row.addView( vRow.SerialNo );
		ret.addView( row );
		
		row = createRow();
			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
		row.addView( createTV("Date In Service: ", false, true, 1f ) );
		row.addView( vRow.DateInService );
		row.addView( createTV("Date Out of Service: ", false, true, 1f ) );
		row.addView( vRow.DateOutOfService );
		ret.addView( row );
		
		ret.setTag(vRow);
		return ret;
		}
	
	private AutoCompleteTextView createAT(String text, boolean header,
			ArrayAdapter<CharSequence> adapter, boolean wrap,
			float weight, InputFilter[] inputFilter, int inputType )
		{
		AutoCompleteTextView at = new AutoCompleteTextView(mContext);
		LinearLayout.LayoutParams params;
		if( wrap )
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
		params.weight = weight;
		params.setMargins(3, 3, 3, 3);
		at.setLayoutParams(params);
		
		at.setText(text);
		if( header )
			{
			at.setTextSize(Twix_Theme.headerSizeLarge);
			at.setTextColor(Twix_Theme.headerValue);
			}
		else
			{
			at.setTextSize(Twix_Theme.subSize);
			at.setTextColor(Twix_Theme.sub1Value);
			}
		
    	at.setPadding(3, 3, 3, 3);
    	at.setMaxLines(1);
    	at.setEnabled( !eqAct.readOnly );
    	if( !eqAct.readOnly )
	    	{
	    	at.setThreshold(0);
	    	at.setInputType(inputType);
	    	at.setAdapter(adapter);
	    	at.setOnClickListener(dropClick);
	    	at.setOnFocusChangeListener(dropFocus);
		    at.setFilters(inputFilter);
		    at.addTextChangedListener(eqAct.setDirtyFlag);
		    at.setBackgroundResource(R.drawable.editbox);
	    	}
    	else
    		{
    		at.setEnabled( false );
    		if( header )
    			at.setBackgroundColor(Twix_Theme.headerBG);
    		else
    			at.setBackgroundColor(0x00000000);
    		}
		
		return at;
		}
	
	private EditText createET(String text, boolean header,
			boolean wrap, float weight, InputFilter[] inputFilter, int inputType )
		{
		EditText et = new EditText(mContext);
		LinearLayout.LayoutParams params;
		if( wrap )
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
		params.weight = weight;
		params.setMargins(3, 3, 3, 3);
		et.setLayoutParams(params);
		
		et.setText(text);
		if( header )
			{
			et.setTextSize(Twix_Theme.headerSizeLarge);
			et.setTextColor(Twix_Theme.headerValue);
			}
		else
			{
			et.setTextSize(Twix_Theme.subSize);
			et.setTextColor(Twix_Theme.sub1Value);
			}
    	et.setPadding(3, 3, 3, 3);
    	et.setMaxLines(1);
    	et.setEnabled( !eqAct.readOnly );
    	if( !eqAct.readOnly )
	    	{
	    	et.setInputType(inputType);
		    et.setFilters(inputFilter);
		    et.addTextChangedListener(eqAct.setDirtyFlag);
		    et.setBackgroundResource(R.drawable.editbox);
	    	}
    	else
    		{
    		et.setEnabled( false );
    		if( header )
    			et.setBackgroundColor(Twix_Theme.headerBG);
    		else
    			et.setBackgroundColor(0x00000000);
    		}
    	
		return et;
		}
	
	private TextView createTV(String text, boolean header, boolean wrap, float weight )
		{
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams params;
		if( wrap )
			params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
		params.weight = weight;
		params.setMargins(3, 3, 3, 3);
		tv.setLayoutParams(params);
		
		tv.setText(text);
		if( header )
			{
			tv.setTextSize(Twix_Theme.headerSizeLarge);
			tv.setTextColor(Twix_Theme.headerText);
			}
		else
			{
			tv.setTextSize(Twix_Theme.subSize);
			tv.setTextColor(Twix_Theme.headerText);
			}
    	tv.setPadding(3, 3, 3, 3);
    	tv.setMaxLines(1);
    	
		return tv;
		}
	
	private ImageButton createIB(int res, OnClickListener click)
		{
		ImageButton bn = new ImageButton(mContext);
		LinearLayout.LayoutParams bnParams = new LinearLayout.LayoutParams( 35, 35 );
		bnParams.gravity = Gravity.CENTER;
		bn.setLayoutParams(bnParams);
		bn.setOnClickListener(click);
		bn.setImageResource(res);
		bn.setBackgroundColor(0x00000000);
		
		return bn;
		}
	
	private LinearLayout createAddCompressor()
		{
		LinearLayout ret = createRow();
		((LinearLayout.LayoutParams) ret.getLayoutParams()).leftMargin = 50;
		ret.addView(createIB(R.drawable.plus, addCompressor));
		ret.addView(createTV("Add Compressor", false, true, 1));
		
		return ret;
		}
	
	private LinearLayout createRow()
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.HORIZONTAL);
		
		return ret;
		}
	
	private LinearLayout createTable()
		{
		LinearLayout ret = new LinearLayout(mContext);
		
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		
		return ret;
		}
	
	public void updateDBClass()
		{
		removeOriginalEntries();
		
		ContentValues cv = new ContentValues();
		int size = ll.getChildCount();
		int newId;
		
		RefCirRow rRow;
		CompressorRow cRow;
		DateData dateData;
		int size2 = 0;
		for( int i = size-1; i >= 0; i-- )
			{
			rRow = (RefCirRow) ll.getChildAt(i).getTag();
			newId = db.newNegativeId("refcircuit", "circuitId");
			cv.put("CircuitId",		newId);
			cv.put("EquipmentId",	eqAct.equipmentId);
			cv.put("CircuitNo",		rRow.CircuitNo.getText().toString());
			cv.put("LbsRefrigerant",rRow.Refrig.getText().toString());
			
			db.db.insertOrThrow("RefCircuit", null, cv);
			cv.clear();
			
			size2 = rRow.Children.getChildCount();
			for( int j = 0; j < size2; j++ )
				{
				cRow = (CompressorRow) rRow.Children.getChildAt(j).getTag();
				cv.put("CircuitId",		newId);
				cv.put("CompressorNo",	cRow.CompressorNo.getText().toString());
				cv.put("Manufacturer",	cRow.Manufacturer.getText().toString());
				cv.put("Model",			cRow.Model.getText().toString());
				cv.put("SerialNo",		cRow.SerialNo.getText().toString());
				
				dateData = (DateData) cRow.DateInService.getTag();
				cv.put("DateInService",	dateData.DBformat());
				
				dateData = (DateData) cRow.DateOutOfService.getTag();
				cv.put("DateOutService",dateData.DBformat());
				
				db.db.insertOrThrow("Compressor", null, cv);
				cv.clear();
				}
			}
		
		}
	
	/**
	 * Force the activity to use the activity group's provided back functionality
	 */
	@Override
	public void onBackPressed()
		{
		((Twix_TabActivityGroup)mContext).onBackPressed();
		}
	}