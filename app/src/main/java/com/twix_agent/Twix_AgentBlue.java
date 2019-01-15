package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentBlue
 * 
 * Purpose: Allows technicians to create Blues (Equipment Service Quotes). Blues are based on a set of units.
 * 			When a blue is marked complete, it will be uploaded next sync, regardless of the service tag
 * 			completeness. When a blue is complete, blue units cannot be added to blue anymore. The blue is
 * 			read only when complete.
 * 
 * Note:	It is possible for the serviceTagId link to be broken on the server if the service tag is deleted on
 * 			the tablet after the blue is marked complete. The blue will still exist and exist with all the
 * 			necessary data regardless.
 * 
 * Relevant XML: blue.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentBlue extends Activity
	{
	private Twix_Application app;
	private Twix_SQLite db;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private LinearLayout BlueList;
	private Twix_AgentOpenTag act;
	private int LastServiceAddressId;
	
	private boolean HasCompletedBlues = false;
	
	private EquipmentArrayAdapter EquipmentAdapter;
	private ArrayAdapter<CharSequence> adapterRepair;
	
	private TextWatcher DecimalWatcher;
	private Spinner currentScan;
	
	// Input Filters
	private InputFilter[] max5k;
	private InputFilter[] max2k;
	private InputFilter[] max15;
	
	public void onCreate(Bundle savedInstanceState)
    	{
    	super.onCreate(savedInstanceState);
    	
        mContext = getParent().getParent();
        View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.blue, null);
		this.setContentView(viewToLoad);
		
		LocalActivityManager manager = ((Twix_AgentActivityGroup2)mContext).getLocalActivityManager();
		LocalActivityManager manager2 = ((Twix_AgentOpenTagsTabHost)manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		act = (Twix_AgentOpenTag) manager2.getActivity("Tag");
		
		BlueList = (LinearLayout) findViewById(R.id.TableHost_Blue);
		
        app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
		readOnlySetup();
        buildAdapters();
        HasCompletedBlues = false;
        readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( act.tagReadOnly )
			{
			findViewById(R.id.AddBlueUnit).setVisibility(View.INVISIBLE);
			}
		}
	
	private void buildAdapters()
		{
		max5k = new InputFilter[] {new InputFilter.LengthFilter(5000)};
		max2k = new InputFilter[] {new InputFilter.LengthFilter(2000)};
		max15 = new InputFilter[] {new InputFilter.LengthFilter(15)};
		
		adapterRepair = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
		adapterRepair.setDropDownViewResource(R.layout.spinner_popup);
		adapterRepair.add("");
		String sqlQ = "select serviceDescription.description from serviceDescription " +
				"ORDER BY serviceDescription.description";
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				adapterRepair.add(cursor.getString(0));
				}
		    	while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		BuildEquipmentAdapter();
		
		DecimalWatcher = new TextWatcher()
			{
	        @Override
	        public void afterTextChanged(Editable arg0)
	        	{
	        	String text = arg0.toString();
	        	if( !text.matches("^[0-9]{0,12}(\\.[0-9]{0,2}){0,1}$") )
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
	
	private void BuildEquipmentAdapter()
		{
		String sqlQ = "SELECT e.equipmentId, e.unitNo, ec.categoryDesc " +
				"FROM equipment AS e " + 
				"INNER JOIN equipmentCategory AS ec " + 
					"on e.equipmentCategoryId = ec.equipmentCategoryId " +
				"WHERE e.serviceAddressId = " + act.serviceAddressId + " " +
					" AND ( (e.DateOutService IS NULL OR e.DateOutService = '' OR e.DateOutService LIKE '1900-01-01%') " +
						"OR (e.equipmentId IN (SELECT equipmentId FROM blueUnit WHERE blueId IN (SELECT blueId FROM blue WHERE serviceTagId = " + act.serviceTagId + ")) ) )" + 
					"ORDER BY ec.equipmentCategoryId";
		/*
		String sqlQ = "select equipment.equipmentId, equipment.unitNo, equipmentCategory.categoryDesc " +
				"from equipment " + 
				"INNER JOIN equipmentCategory " + 
					"on equipment.equipmentCategoryId = equipmentCategory.equipmentCategoryId " +
					"WHERE equipment.serviceAddressId = '" + act.serviceAddressId + "' " + 
					"ORDER BY equipmentCategory.equipmentCategoryId";
		*/
		Cursor cursor = db.rawQuery(sqlQ);
		ArrayList<EquipmentItem> eqItems = new ArrayList<EquipmentItem>();
		EquipmentItem item = new EquipmentItem(0, "Not Listed");
		eqItems.add(item);
		while (cursor.moveToNext())
			{
			item = new EquipmentItem( cursor.getInt(0), cursor.getString(1) + " - " + cursor.getString(2) );
			eqItems.add(item);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		EquipmentAdapter = new EquipmentArrayAdapter(mContext, eqItems);
		}
	
	private void readSQL()
		{
		// Set the Service Address Id
		LastServiceAddressId = act.serviceAddressId;
		
		String sqlQ = "select blueUnit.blueUnitId, blueUnit.equipmentId, blueUnit.description, " +
						"blueUnit.materials, blueUnit.notes, " +
						"blueUnit.cost, blueUnit.laborHours, blueUnit.tradesmenhrs, blueUnit.otherhrs, blueUnit.completed " +
				"from blue " + 
				"LEFT OUTER JOIN blueUnit " + 
					"on blue.blueId = blueUnit.blueId " +
				"WHERE blue.serviceTagId = " + act.serviceTagId;
		Cursor cursor = db.rawQuery(sqlQ);
		
		//int size = cursor.getColumnCount();
		//List<String> list = new ArrayList<String>();
		BlueData data;
		int index;
		while (cursor.moveToNext())
			{
			index = 0;
			data = new BlueData();
			data.BlueUnitId = cursor.getInt(0);
			data.EquipmentId = cursor.getInt(1);
			data.Repair = cursor.getString(2);
			data.Materials = cursor.getString(3);
			data.Notes = cursor.getString(4);
			data.MaterialsCost = cursor.getString(5);
			data.LaborHours = cursor.getString(6);
			data.Tradesmenhrs = cursor.getString(7);
			data.Otherhrs = cursor.getString(8);
			data.Completed = cursor.getString(7);
			BlueList.addView(createBlueRow(data));
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		}
	
	public void addBlue()
		{
		BlueList.addView( createBlueRow(new BlueData()) );
		}
	
	private class BlueData
		{
		@SuppressWarnings("unused")
		int BlueUnitId = 0;
		
		int EquipmentId = 0;
		String Repair = "";
		String Materials = "";
		String Notes = "";
		String MaterialsCost = "0";
		String LaborHours = "0";
		String Tradesmenhrs = "0";
		String Otherhrs = "0";
		String Completed = "N";
		}
	
	public LinearLayout createBlueRow(BlueData data)
	    {
	    BlueUnitRow UnitRow = new BlueUnitRow();
	    UnitRow.Completed = data.Completed.contentEquals("Y");
	    if( UnitRow.Completed )
	    	HasCompletedBlues = true;
	    
	    boolean editable = (!UnitRow.Completed && !act.tagReadOnly );
	    boolean hasServiceAddress = !(act.serviceAddressId == 0);
	    
	    LinearLayout.LayoutParams tableParams = new LinearLayout.LayoutParams(
	    		LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    tableParams.setMargins(3, 3, 3, 10);
	    
	    LinearLayout BlueHost = new LinearLayout(this);
	    BlueHost.setOrientation(LinearLayout.VERTICAL);
	    BlueHost.setLayoutParams(tableParams);
	    BlueHost.setBackgroundColor(Twix_Theme.tableBG);
	    BlueHost.setPadding(0, 0, 0, 10);
	    BlueHost.setDrawingCacheEnabled(false);
	    BlueHost.setTag(UnitRow);
	    
		LinearLayout.LayoutParams paramsL = new LinearLayout.LayoutParams(
	    		0, LayoutParams.WRAP_CONTENT);
		paramsL.setMargins(35, 2, 5, 2);
		paramsL.weight = 1;
		
		LinearLayout.LayoutParams paramsR = new LinearLayout.LayoutParams(
	    		0, LayoutParams.WRAP_CONTENT);
		paramsR.setMargins(5, 2, 35, 2);
		paramsR.weight = 1;
		
		LinearLayout.LayoutParams paramsC = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		paramsC.setMargins(2, 2, 35, 2);
		paramsC.weight = 4;
		
		// Used on Text Boxes
		LinearLayout.LayoutParams paramsNoWeight = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		paramsNoWeight.setMargins(2, 2, 2, 2);
		
		
		LinearLayout row = CreateRow();
		row.setGravity(Gravity.CENTER_VERTICAL);
		
		TextView tv;
		EditText et;
		
		// Delete Blue Button -  Only available when editable
		if( editable )
			row.addView(CreateDeleteButton(BlueHost));
			
		row.setBackgroundColor(Twix_Theme.headerBG);
		
		//Equipment title
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		paramsNoWeight.setMargins(2, 2, 2, 2);
		tv = createText("Equipment: ", titleParams);
		tv.setTypeface(Typeface.DEFAULT_BOLD);
		row.addView(tv);
		
		// Create the Spinner
		row.addView(CreateEquipmentSpinner(data.EquipmentId, editable && hasServiceAddress, UnitRow));
		
		if( editable )
			{
			// Add the Scanner Button
			LinearLayout scanner = CreateScanner(UnitRow); 
			if( !hasServiceAddress )
				scanner.setVisibility(View.GONE);
			row.addView(scanner);
			}
		// Add the Complete Check Box
		row.addView(CreateBlueComplete(data, UnitRow, editable));
		
		// Add the Row including: Close Box, Title, Equipment Spinner, Scanner Button, and Complete Checkbox
		BlueHost.addView(row);
		
		
		row = CreateRow();
		row.setBackgroundColor(Twix_Theme.tableBG);
		
		// Repair Input
		// Instantiate the EditText so it can be a target for the spinner
		et = createEdit( data.Repair, paramsC, editable );
		if( !editable )
			row.addView( createText("Repair: ", paramsL) );
		else
			{
			LinearLayout col = new LinearLayout(this);
			col.setLayoutParams(paramsL);
			col.setOrientation(LinearLayout.VERTICAL);
			
			col.addView(createText("Repair: ", paramsNoWeight));
			col.addView(createSpinner(adapterRepair, paramsNoWeight, et));
			row.addView(col);
			}
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		et.setSingleLine(false);
		et.setLines(3);
		et.setFilters(max5k);
		row.addView(et);
		if( editable )
			UnitRow.repair = et;
		
		BlueHost.addView(row);
		row = CreateRow();
		row.setBackgroundColor(Twix_Theme.tableBG);
		
		// Materials Input
		row.addView(createText("Materials: ", paramsL));
		et = createEdit( data.Materials, paramsC, editable );
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		et.setSingleLine(false);
		et.setLines(3);
		et.setFilters(max2k);
		row.addView(et);
		if( editable )
			UnitRow.materials = et;
		
		BlueHost.addView(row);
		
		row = CreateRow();
		row.setBackgroundColor(Twix_Theme.tableBG);
		row.addView( createText("Notes: ", paramsL) );
		
		// Notes Input
		et = createEdit( data.Notes, paramsC, editable );
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		et.setSingleLine(false);
		et.setLines(3);
		et.setFilters(max5k);
		row.addView(et);
		if( editable )
			UnitRow.notes = et;
		
		
		// Material and Labor Titles
		BlueHost.addView(row);
		row = CreateRow();
		row.setBackgroundColor(Twix_Theme.tableBG);
		row.addView( createText("Estimated Material Cost: ", paramsL) );
		row.addView( createText("Estimated Plum Hours: ", paramsR) );
		row.addView( createText("Estimated Tradesmen Hours: ", paramsR) );
		row.addView( createText("Estimated Other Hours: ", paramsR) );
		
		// Material and Labor Input Fields
		BlueHost.addView(row);	
		row = CreateRow();
		row.setBackgroundColor(Twix_Theme.tableBG);
		
		et = createEdit( data.MaterialsCost, paramsL, editable );
		et.setMaxLines(1);
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_CLASS_PHONE);
		et.setFilters(max15);
		row.addView(et);
		if( editable )
			{
			et.addTextChangedListener(DecimalWatcher);
			UnitRow.cost = et;
			}
		
		et = createEdit( data.LaborHours, paramsR, editable );
		et.setMaxLines(1);
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_CLASS_PHONE);
		et.setFilters(max15);
		row.addView(et);
		if( editable )
			{
			et.addTextChangedListener(DecimalWatcher);
			UnitRow.labor = et;
			}
		
		et = createEdit( data.Tradesmenhrs, paramsR, editable );
		et.setMaxLines(1);
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_CLASS_PHONE);
		et.setFilters(max15);
		row.addView(et);
		if( editable )
			{
			et.addTextChangedListener(DecimalWatcher);
			UnitRow.tradesmen= et;
			}
		et = createEdit( data.Otherhrs, paramsR, editable );
		et.setMaxLines(1);
		et.setEnabled(editable);
		et.setInputType(InputType.TYPE_CLASS_PHONE);
		et.setFilters(max15);
		row.addView(et);
		if( editable )
			{
			et.addTextChangedListener(DecimalWatcher);
			UnitRow.other= et;
			}
		BlueHost.addView(row);
		
		return BlueHost;
	    }
	
	private TextView createText(String s, ViewGroup.LayoutParams params)
		{
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setText( s );
		tv.setTextSize(Twix_Theme.headerSizeLarge);
		tv.setPadding(3, 3, 3, 3);
		tv.setGravity(Gravity.LEFT);
		tv.setTextColor(Twix_Theme.sub1Header);
		
		return tv;
		}
	
	private EditText createEdit(String s, ViewGroup.LayoutParams params, boolean editable)
		{
		EditText et = new EditText(this);
		et.setLayoutParams(params);
		et.setText( s );
		et.setTextSize(Twix_Theme.headerSizeLarge);
		et.setPadding(3, 3, 3, 3);
		et.setGravity(Gravity.LEFT);
		et.setTextColor(Twix_Theme.sub1Value);
		et.setBackgroundResource(R.drawable.editbox);
		et.setEnabled(editable);
		
		return et;
		}
	
	private RelativeLayout createSpinner( ArrayAdapter<CharSequence> adapter,
			LinearLayout.LayoutParams params, EditText SpinnerTarget )
		{
		RelativeLayout rl = new RelativeLayout(this);
		rl.setLayoutParams(params);
		
			Spinner sp = new Spinner(mContext);
			sp.setTag(SpinnerTarget);
			RelativeLayout.LayoutParams paramsSpinner = new RelativeLayout.LayoutParams(
						LayoutParams.FILL_PARENT, 45); // Hard coded because it was being a pain in the ass
				paramsSpinner.setMargins(2, 2, 2, 2);
				sp.setLayoutParams(paramsSpinner);
				sp.setAdapter(adapter);
				sp.setBackgroundResource(R.drawable.editbox2);
				sp.setOnItemSelectedListener(new OnItemSelectedListener()
					{
					@Override
					public void onItemSelected(AdapterView<?> arg0,
							View arg1, int arg2, long arg3)
						{
						if( arg2 > 0 )
							{
							String s = (String)arg0.getSelectedItem();
							arg0.setSelection(0);
							EditText et = (EditText) arg0.getTag();
							String text = et.getText().toString();
							if( text.length() > 0 )
								s = "; " + s;
							s = et.getText() + s;
							et.setText(s);
							et.requestFocus();
							
							et.setSelection(s.length());
							}
						}
					
					@Override
					public void onNothingSelected(AdapterView<?> arg0)
						{
						// Do Nothing...
						}
					});
		
		rl.addView(sp);
			RelativeLayout.LayoutParams paramsArrow = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsArrow.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			paramsArrow.addRule(RelativeLayout.CENTER_VERTICAL);
			
			ImageView iv = new ImageView(this);
				iv.setLayoutParams(paramsArrow);
				iv.setImageResource(R.drawable.drop_arrow);
				iv.setPadding(0, 0, 10, 0);
		rl.addView(iv);
		
		return rl;
		}
	
	public void onResume()
		{
		BlueUnitRow UnitRow;
		int size = BlueList.getChildCount();
		if( !act.tagReadOnly && size > 0 && LastServiceAddressId != act.serviceAddressId)
			{
			LastServiceAddressId = act.serviceAddressId;
		    boolean hasServiceAddress = act.serviceAddressId != 0;
		    BuildEquipmentAdapter();
		    
			for( int i = 0; i < size; i++ )
				{
				UnitRow = (BlueUnitRow) BlueList.getChildAt(i).getTag();
				
				if( hasServiceAddress )
					{
					UnitRow.equipment.setBackgroundResource(R.drawable.editbox);
					UnitRow.equipment.setEnabled(true);
					UnitRow.scanner.setVisibility(View.VISIBLE);
					}
				else
					{
					UnitRow.equipment.setBackgroundResource(R.drawable.editbox_disabled);
					UnitRow.equipment.setEnabled(false);
					UnitRow.scanner.setVisibility(View.INVISIBLE);
					}
					
				UnitRow.equipment.setAdapter(EquipmentAdapter);
				}
			}
		
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
		ContentValues cv = new ContentValues();
		
		// Find out if the blue will be a new blue
		idReturn blueId = getBlueId();
		
		// Non-Idiots way of deleting incomplete open blues
		db.rawQuery("DELETE FROM blueUnit WHERE BlueId = " + blueId.ID + " AND Completed != 'Y'");
		
		// BlueID Where Clause
		ArrayList<Twix_SQLite.WhereClause> Clauses = new ArrayList<Twix_SQLite.WhereClause>();
		Twix_SQLite.WhereClause clause = new Twix_SQLite.WhereClause();
		clause.column = "BlueId";
		clause.params.add(blueId.ID + "");
		Clauses.add(clause);
		// Completed where Clause
		clause = new Twix_SQLite.WhereClause();
		clause.column = "Completed";
		clause.params.add("Y");
		clause.not = true;
		Clauses.add(clause);
		
		db.delete("BlueUnit", Clauses);
		
		// Delete the blue if the blue unit list is 0 and return. All blues have been removed
		// AND there are no completed blueUnits attached to the blue
		int size = BlueList.getChildCount();
		
		if( size < 1 && !HasCompletedBlues )
			{
			db.delete("blue", "serviceTagId", act.serviceTagId);
			return;
			}
		
		// If this is a newId, then create the blue, otherwise continue using the ID
		if( blueId.newId )
			{
			// Set the Blue Content Values
			cv.put("blueId", blueId.ID);
			cv.put("serviceTagId", act.serviceTagId);
			cv.put("dateCreated", Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT));
			
			// Insert the Record
			db.db.insertOrThrow("blue", null, cv);
			cv.clear();
			}
		
		String s; BlueUnitRow links; float val;
		EquipmentItem item;
		// Loops through the entire Blue Unit list
		for( int i = 0; i < size; i++ )
			{
			links = (BlueUnitRow) BlueList.getChildAt(i).getTag();
			if( !links.Completed )
				{
				cv.put( "blueUnitId", db.newNegativeId("blueUnit", "blueUnitId") );
				cv.put( "blueId", blueId.ID );
				
				item = (EquipmentItem) links.equipment.getSelectedItem();
				cv.put("equipmentId", item.EquipmentId);
				if( links.complete.isChecked() )
					cv.put( "completed", "M" );
				else
					cv.put( "completed", "N" );
				
				cv.put( "description", links.repair.getText().toString() );
				cv.put( "materials", links.materials.getText().toString() );
				cv.put( "notes", links.notes.getText().toString() );
				
				s = links.cost.getText().toString();
				if( s.contentEquals(".") || s.length() <= 0 )
					val = 0;
				else
					val = Float.parseFloat(s);
				cv.put( "cost", val );
				
				s = links.labor.getText().toString();
				if( s.contentEquals(".") || s.length() <= 0 )
					val = 0;
				else
					val = Float.parseFloat(s);
				cv.put( "laborHours", val );
			
				s = links.tradesmen.getText().toString();
				if( s.contentEquals(".") || s.length() <= 0 )
					val = 0;
				else
					val = Float.parseFloat(s);
				cv.put( "tradesmenhrs", val );
				
				s = links.other.getText().toString();
				if( s.contentEquals(".") || s.length() <= 0 )
					val = 0;
				else
					val = Float.parseFloat(s);
				cv.put( "otherhrs", val );
				
				db.db.insertOrThrow("blueUnit", null, cv);
				cv.clear();
				}
			}
		}
	
	private class idReturn
		{
		public int ID;
		public boolean newId;
		}
	
	/**
	 * Used to keep track of BlueUnits and their fields for database updates
	 * 
     * BlueList Structure:
     * 
     * 0 - Equipment Spinner
     * 1 - Scanner Button
     * 2 - CheckBox Complete
     * 3 - Repair Text
     * 4 - Materials Text
     * 5 - Notes Text
     * 6 - Materials Cost
     * 7 - Labor Hours
     * 
     */
	private class BlueUnitRow
		{
		Spinner equipment;
		CheckBox complete;
		EditText repair;
		EditText materials;
		EditText notes;
		EditText cost;
		EditText labor;
		EditText tradesmen;
		EditText other;
		LinearLayout scanner;
		
		boolean Completed;
		}
	
	private idReturn getBlueId()
		{
		idReturn ret = new idReturn();
		String sqlQ = "SELECT blue.blueId FROM blue WHERE serviceTagId = " + act.serviceTagId;
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret.ID = cursor.getInt(0);
			ret.newId = false;
			}
		else
			{
			ret.ID = db.newNegativeId("blue", "blueId");
			ret.newId = true;
			}
		
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	public void scanResult( String barCode )
    	{
    	String sqlQ = "SELECT equipmentId FROM equipment " +
    			"WHERE barCodeNo = '" + barCode + "' " +
    				"AND serviceAddressId = '" + act.serviceAddressId + "'";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	int id = 0;
		if (cursor.moveToFirst())
			{
			id = cursor.getInt(0);
			if( cursor.moveToNext() )
				Toast.makeText(mContext, "Warning: This BarCode is " +
						"shared by mulitple equipment. Please Make sure " +
						"all equipment have unique barcodes.", Toast.LENGTH_LONG).show();
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
    	
		if( id > 0 )
			{
			currentScan.setSelection(
					((EquipmentArrayAdapter)currentScan.getAdapter()).getIndexOfEquipmentId(id) );
			}
		else
			Toast.makeText(mContext, "No equipment registered with that barcode.", Toast.LENGTH_LONG).show();
		}
	
	public boolean hasEquipment()
		{
		int size = BlueList.getChildCount();
		BlueUnitRow links;
		for( int i = 0; i < size; i++ )
			{
			links = (BlueUnitRow) BlueList.getChildAt(i).getTag();
			if( ((EquipmentItem)links.equipment.getSelectedItem()).EquipmentId > 0 )
				return true;
			}
		
		return false;
		}
	
	public class EquipmentItem
		{
		int EquipmentId;
		String Display;
		
		public EquipmentItem(int EquipmentId, String Display)
			{
			this.EquipmentId = EquipmentId;
			this.Display = Display;
			}
		}
	
	public class EquipmentArrayAdapter extends ArrayAdapter<EquipmentItem>
		{
		private List<EquipmentItem>	items;
		private Context			mContext;
		private LinearLayout.LayoutParams layoutParams;
		
		public EquipmentArrayAdapter(Context c, List<EquipmentItem> items)
			{
			super(c, R.layout.spinner_popup, items);
			this.items = items;
			this.mContext = c;
			this.layoutParams = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			}
		
		// The View displayed for selecting in the list
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
			EquipmentItem data = items.get(position);
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
			tv.setText(data.Display);
			
			return tv;
			}

		@Override
		public EquipmentItem getItem(int position)
			{
			return items.get(position);
			}
		
		// Views displayed AFTER selecting
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
			{
			TextView tv = (TextView) convertView;
			EquipmentItem data = items.get(position);
			
			if (tv == null)
				{
				tv = new TextView(mContext);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(Twix_Theme.headerSize);
				}
			
			tv.setTextColor(Twix_Theme.headerValue);
			
			tv.setText(data.Display);
			tv.setPadding(4, 4, 4, 4);
			
			return tv;
			}
		
		public int size()
			{
			return items.size();
			}
		
		public List<EquipmentItem> getItemList()
			{
			return items;
			}
		
		public Context getContext()
			{
			return this.mContext;
			}
		
		public int getIndexOfEquipmentId(int find)
			{
			int ret = 0;
			int size = items.size();
			for( int i = 0; i < size; i++ )
				{
				if( find == items.get(i).EquipmentId )
					{
					ret = i;
					break;
					}
				}
			
			return ret;
			}
		}
	
	private LinearLayout CreateRow()
		{
		LinearLayout row = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		row.setLayoutParams(params);
		return row;
		}
	
	private View CreateDeleteButton(LinearLayout BlueHost)
		{
		ImageButton bn = new ImageButton(this);
		bn.setTag(BlueHost);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		params.setMargins(2, 2, 2, 2);
		bn.setLayoutParams(params);
		bn.setImageResource(R.drawable.minus2);
		bn.setBackgroundColor(0x00000000);
		bn.setPadding(5, 5, 5, 5);
		bn.setOnClickListener(new View.OnClickListener()
			{
			public void onClick(View v)
				{
				LinearLayout BlueHost = (LinearLayout) v.getTag();
				BlueList.removeView(BlueHost);
				}
			});
		
		return bn;
		}
	
	private RelativeLayout CreateEquipmentSpinner(int EquipmentId, boolean Editable, BlueUnitRow UnitRow)
		{
		//Equipment spinner
		RelativeLayout rl = new RelativeLayout(this);
		LinearLayout.LayoutParams paramsRel = new LinearLayout.LayoutParams(
	    		0, LayoutParams.WRAP_CONTENT);
		paramsRel.setMargins(5, 2, 5, 2);
		paramsRel.weight = 2;
		rl.setLayoutParams(paramsRel);
		rl.setPadding(5, 8, 5, 8);
		
		Spinner sp = new Spinner(mContext);
		RelativeLayout.LayoutParams paramsSpinner = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		paramsSpinner.setMargins(2, 2, 2, 2);
		sp.setLayoutParams(paramsSpinner);
		sp.setAdapter(EquipmentAdapter);
		sp.setSelection(EquipmentAdapter.getIndexOfEquipmentId(EquipmentId));
		// Only allow the spinner to change if the blue is editable and the service address ID is available
		sp.setEnabled(Editable);
		if( Editable )
			sp.setBackgroundResource(R.drawable.editbox);
		else
			sp.setBackgroundResource(R.drawable.editbox_disabled);
		
		rl.addView(sp);
		RelativeLayout.LayoutParams paramsArrow = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		paramsArrow.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		paramsArrow.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsArrow.setMargins(0, 0, 10, 0);
			
		ImageView iv = new ImageView(this);
		iv.setLayoutParams(paramsArrow);
		iv.setImageResource(R.drawable.drop_arrow);
		rl.addView(iv);
		
		// Set the Row's Spinner, if it exists. It will be null if the unit is already complete
		if( UnitRow != null )
			UnitRow.equipment = sp;
		
		return rl;
		}
	
	private LinearLayout CreateScanner(BlueUnitRow UnitRow)
		{
		// Only add the scan button if the service address is available
		LinearLayout scanBn = new LinearLayout(this);
		scanBn.setTag(UnitRow.equipment);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		params.setMargins(2, 2, 2, 2);
		scanBn.setLayoutParams(params);
		scanBn.setOrientation(LinearLayout.HORIZONTAL);
		scanBn.setBackgroundResource(R.drawable.button_bg);
		scanBn.setGravity(Gravity.CENTER);
		scanBn.setOnClickListener( new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				currentScan = (Spinner) v.getTag();
				// QR Droid Barcode Scanner
		    	Intent intent = new Intent( "la.droid.qr.scan" );
		    	intent.setPackage("la.droid.qr");
		    	intent.putExtra( "la.droid.qr.complete", true);
		    	try
		    		{
		    		((Twix_TabActivityGroup)mContext).startActivityForResult( intent, Twix_AgentActivityGroup2.SCAN_BARCODE_BLUE );
		    		}
		    	catch (ActivityNotFoundException e)
		    		{
		    		Toast.makeText(mContext,
		    				"Cannot Find QR Droid Bar Code scanner. Please install QR Scanner from the Android Market and try again",
		    				Toast.LENGTH_LONG).show();
		    		}					
				}
			});
		
		// Scanner Image
		ImageView iv = new ImageView(this);
		RelativeLayout.LayoutParams paramsArrow = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		paramsArrow.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsArrow.setMargins(0, 0, 10, 0);
		iv.setLayoutParams(paramsArrow);
		iv.setImageResource(R.drawable.icon_scanner);
		scanBn.addView(iv);
		
		// Scanner Text
		TextView tv = new TextView(this);
		tv.setLayoutParams(paramsArrow);
		tv.setText("Scan Lookup");
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		scanBn.addView(tv);
		
		UnitRow.scanner = scanBn;
		return scanBn;
		}
	
	private LinearLayout CreateBlueComplete(BlueData data, BlueUnitRow UnitRow, boolean editable)
		{
		// Blue Complete Box
		LinearLayout blueComplete = new LinearLayout(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 35, 2);
		blueComplete.setLayoutParams(params);
		blueComplete.setOrientation(LinearLayout.HORIZONTAL);
		blueComplete.setBackgroundColor(Twix_Theme.tableBG2);
		if( editable )
			{
			blueComplete.setOnClickListener( new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					CheckBox cb = (CheckBox)v.getTag();
					cb.toggle();
					}
				});
			}
		
		LinearLayout.LayoutParams paramsPlain = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		paramsPlain.setMargins(2, 2, 2, 2);
		paramsPlain.gravity = Gravity.CENTER_VERTICAL;
		
		TextView tv = new TextView(this);
		tv.setLayoutParams(paramsPlain);
		tv.setText("Unit Complete");
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		tv.setPadding(10, 0, 0, 0);
		blueComplete.addView(tv);
		
		CheckBox cb = new CheckBox(this);
		blueComplete.setTag(cb);
		LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
	    		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		cb.setLayoutParams(checkParams);
		cb.setChecked( !data.Completed.contentEquals("N") );
		cb.setEnabled(editable);
		UnitRow.complete = cb;
		blueComplete.addView(cb);
		
		return blueComplete;
		}
	
	}
