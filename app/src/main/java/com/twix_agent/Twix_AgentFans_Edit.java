package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentFans_Edit
 * 
 * Purpose: Allows the user to edit the fans on a piece of equipment.
 * 			Fans are made up of:
 * 			- Fan details
 * 			- A single belt
 * 			- Up to four sheaves
 * 
 * Relevant XML: fans_edit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentFans_Edit extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_AgentEquipmentTabHost_Edit eqAct;
	private Twix_SQLite db;
	private Context mContext;
	private LinearLayout ll;
	
	private static final int MAX_SHEAVE = 2; //Allows 2 Sheaves per fan
	
	private ArrayAdapter<CharSequence> adapterFanType;
	private ArrayAdapter<CharSequence> adapterSheaveType;
	private ArrayAdapter<CharSequence> adapterSheaveManufacturer;
	
	private OnClickListener dropClick;
	private OnFocusChangeListener dropFocus;
	
	private List<Integer> fanIdList; // Contains the IDs for the original query. Used to clear the old rows out on update
	
	// Input Filters
	private InputFilter[] IF2 = new InputFilter[1];
	private InputFilter[] IF20 = new InputFilter[1];
	private InputFilter[] IF50 = new InputFilter[1];
	private InputFilter[] IF9 = new InputFilter[1];
	private InputFilter[] IF10 = new InputFilter[1];
	private final InputFilter IF2ele = new InputFilter.LengthFilter(2);
	private final InputFilter IF20ele = new InputFilter.LengthFilter(20);
	private final InputFilter IF50ele = new InputFilter.LengthFilter(50);
	private final InputFilter IF9ele = new InputFilter.LengthFilter(9); //For ints: 2147483647
	private final InputFilter IF10ele = new InputFilter.LengthFilter(10);
	private NumberKeyListener numbersOnly;
	
	// Click Listeners
	private OnClickListener addFan;
	private OnClickListener addSheave;
	private OnClickListener deleteFan;
	private OnClickListener deleteSheave;
	
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.fans_edit, null);
		this.setContentView( viewToLoad );
		
        eqAct = (Twix_AgentEquipmentTabHost_Edit)getParent();
		
		ll = (LinearLayout) findViewById(R.id.TableHost_Fan);
		
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        fanIdList = new ArrayList<Integer>();
        
        if( eqAct.readOnly )
        	{
        	findViewById(R.id.New_Fan).setVisibility(View.GONE);
        	findViewById(R.id.Title_New_Fan).setVisibility(View.GONE);
        	}
        
        setupInputFilters();
        buildAdapters();
        
        createClickListeners();
        readSQLClass();
    	}
	
	private void setupInputFilters()
		{
		IF2[0] = IF2ele;
		IF20[0] = IF20ele;
		IF50[0] = IF50ele;
		IF9[0] = IF9ele;
		IF10[0] = IF10ele;
		
		numbersOnly = new NumberKeyListener()
			{
		    public int getInputType()
		    	{
			    return InputType.TYPE_CLASS_PHONE;
			    }
		
		    @Override
		    protected char[] getAcceptedChars()
		    	{
			    return new char[] 
			    	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
			    }
		    };
		}
	
	public void buildAdapters()
		{
		//FAN TYPE ADAPTER
		adapterFanType = Twix_TextFunctions.BuildAdapter(db, mContext, "Fan Type");
		adapterSheaveType = Twix_TextFunctions.BuildAdapter(db, mContext, "Sheave Type");
		adapterSheaveManufacturer = Twix_TextFunctions.BuildAdapter(db, mContext, "Sheave Manufacturer");
		
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
		
		}
	
 // Classes to handle the data
 	private class FanData
 		{
 		int FanId;
 		String FanType;
 		String FanNumber;
 		String BeltSize;
 		String BeltQty;
 		
 		List<SheaveData> Sheaves;
 		
 		public FanData()
 			{
 			Sheaves = new ArrayList<SheaveData>();
 			}
 		}
     
 	private class SheaveData
 		{
 		String Type;
 		String Number;
 		String Manufacturer;
 		}
 	
 	private class FanRow
 		{
 		LinearLayout Table;
 		LinearLayout Children;
 		LinearLayout AddSheave;
 		
 		AutoCompleteTextView FanType;
 		EditText FanNumber;
 		EditText BeltSize;
 		EditText BeltQty;
 		}
     
 	private class SheaveRow
 		{
 		AutoCompleteTextView Type;
 		EditText Number;
 		EditText Manufacturer;
 		}
 	
 	private void readSQLClass()
 	    {
     	String sqlQ = "SELECT F.fanId, F.partType, F.number, " +
     					"B.beltSize, B.quantity " + 
 					"FROM Fan as F " +
 						"LEFT OUTER JOIN belt as B " +
 							"ON B.fanId = F.fanId " +
 					"WHERE F.equipmentId = " + eqAct.equipmentId;
     	
     	Cursor cursorFan = db.rawQuery(sqlQ);
     	Cursor cursorSheave;
     	
     	LayoutParams params = new LayoutParams();
     	params.width = LayoutParams.MATCH_PARENT;
     	params.height = LayoutParams.WRAP_CONTENT;
     	params.setMargins(3, 3, 3, 10);
     	
 		if (cursorFan.moveToFirst())
 			{
 			FanData fanData;
 			FanRow vTable;
 			SheaveData sheaveData;
 			do
 				{
 				fanData = new FanData();
 				fanData.FanId		= cursorFan.getInt(0);
 				fanData.FanType		= Twix_TextFunctions.clean(cursorFan.getString(1));
 				fanData.FanNumber	= Twix_TextFunctions.clean(cursorFan.getString(2));
 				fanData.BeltSize	= Twix_TextFunctions.clean(cursorFan.getString(3));
 				fanData.BeltQty		= Twix_TextFunctions.clean(cursorFan.getString(4));
 				
 				sqlQ = 	"SELECT type, number, manufacturer " +
 						"FROM sheave WHERE fanId = '" + fanData.FanId + "'";
 				cursorSheave = db.rawQuery(sqlQ);
 				if (cursorSheave.moveToFirst())
 					{
 					do
 						{
 						sheaveData = new SheaveData();
 						sheaveData.Type			= Twix_TextFunctions.clean(cursorSheave.getString(0));
 						sheaveData.Number		= Twix_TextFunctions.clean(cursorSheave.getString(1));
 						sheaveData.Manufacturer	= Twix_TextFunctions.clean(cursorSheave.getString(2));
 						fanData.Sheaves.add(sheaveData);
 						}
 					while (cursorSheave.moveToNext());
 					}
 				if (cursorSheave != null && !cursorSheave.isClosed())
 					{
 					cursorSheave.close();
 					}
 				
 				// Add the id so we can delete the old records later
 				fanIdList.add(fanData.FanId);
 				// Create build all the rows
 				vTable = createFanRow(fanData);
 				// Add the resulting table to the host table
 				ll.addView(vTable.Table);
 				}
 			while (cursorFan.moveToNext());
 			}
 		if (cursorFan != null && !cursorFan.isClosed())
 			{
 			cursorFan.close();
 			}
 	    }
 	
 	private void createClickListeners()
 		{
 		deleteFan = new OnClickListener()
 			{
 			@Override
 			public void onClick(View v)
 				{
 				final View bn = v;
 				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 				builder.setMessage("Are you sure you want to delete this Fan?")
 						.setCancelable(true)
 						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 							{
 							public void onClick(DialogInterface dialog, int id)
 								{
 								eqAct.dirtyFlag = true;
 								FanRow row = (FanRow) ((View)bn.getParent().getParent()).getTag();
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
 		
 		deleteSheave = new OnClickListener()
 			{
 			@Override
 			public void onClick(View v)
 				{
 				final View bn = v;
 				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 				builder.setMessage("Are you sure you want to delete this Sheave?")
 						.setCancelable(true)
 						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 							{
 							public void onClick(DialogInterface dialog, int id)
 								{
 								eqAct.dirtyFlag = true;
 								FanRow host = (FanRow) ((View)bn.getParent().getParent().getParent().getParent()).getTag();
 								
 								host.Children.removeView( (View) bn.getParent().getParent() );
 								if( host.Children.getChildCount() < MAX_SHEAVE)
 									host.AddSheave.setVisibility(View.VISIBLE);
 								
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
 		
 		addFan = new OnClickListener()
 			{
 			@Override
 			public void onClick(View v)
 				{
 				eqAct.dirtyFlag = true;
 				FanData data = new FanData();
 				data.FanType = "";
 				data.FanNumber = "";
 				
 				ll.addView( createFanRow(data).Table );
 				}
 			}
 		;
 		
 		addSheave = new OnClickListener()
 			{
 			@Override
 			public void onClick(View v)
 				{
 				eqAct.dirtyFlag = true;
 				SheaveData data = new SheaveData();
 				data.Type = "";
 				data.Number = "";
 				data.Manufacturer = "";
 				
 				FanRow host = (FanRow) ((View)v.getParent().getParent()).getTag();
 				
 				host.Children.addView( createSheaveRow( data ) );
 				
 				if( host.Children.getChildCount() >= MAX_SHEAVE )
 					{
 					host.AddSheave.setVisibility(View.GONE);
 					}
 				}
 			}
 		;
 		
 		ImageButton ib = (ImageButton)findViewById(R.id.New_Fan);
 		ib.setOnClickListener(addFan);
 		
 		}
 	
 	private FanRow createFanRow(FanData data)
 		{
 		FanRow vTable	= new FanRow();
 		vTable.FanType	= createAT( data.FanType, true, adapterFanType, false, 1f, IF20, InputType.TYPE_CLASS_TEXT );
 		vTable.FanNumber= createET( data.FanNumber, true, false, 1f, IF2, InputType.TYPE_CLASS_TEXT );
 		vTable.BeltSize	= createET( data.BeltSize, false, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
 		vTable.BeltQty	= createET( data.BeltQty, false, false, 1f, IF9, InputType.TYPE_CLASS_PHONE );
 		if( !eqAct.readOnly )
 			{
 			//vTable.FanNumber.addTextChangedListener(autoCaps);
 			vTable.BeltQty.setKeyListener(numbersOnly);
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
 			bn.setOnClickListener(deleteFan);
 			bn.setImageResource(R.drawable.minus);
 			bn.setBackgroundColor(0x00000000);
 			row.addView(bn);
 			}
 			
 			// Fan Row
 			row.addView(createTV( "Fan Type: ", true, true, 1f ));
 			row.addView(vTable.FanType);
 			row.addView(createTV( "Fan Number: ", true, true, 1f ));
 			row.addView(vTable.FanNumber);
 		vTable.Table.addView(row);
 		
 		// Belt Row
 		row = createRow();
 			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
 		row.addView(createTV( "Belt Size: ", false, true, 1f ));
			row.addView(vTable.BeltSize);
			row.addView(createTV( "Belt Quantity: ", false, true, 1f ));
			row.addView(vTable.BeltQty);
		vTable.Table.addView(row);
		
 		vTable.Children = createTable();
 		int size = data.Sheaves.size();
 		for( int i = 0; i < size; i++ )
 			{
 			vTable.Children.addView( createSheaveRow(data.Sheaves.get(i)) );
 			}
 		vTable.Table.addView( vTable.Children );
 		
 		if( !eqAct.readOnly )
 			{
 			vTable.AddSheave = createAddSheave();
 			vTable.Table.addView(vTable.AddSheave);
 			if( vTable.Children.getChildCount() >= MAX_SHEAVE || eqAct.readOnly )
 				vTable.AddSheave.setVisibility(View.GONE);
 			}
 		
 		return vTable;
 		}
 	
 	private LinearLayout createSheaveRow(SheaveData data)
 		{
 		LinearLayout ret = createTable();
 		((LinearLayout.LayoutParams)ret.getLayoutParams()).setMargins(8,3,8,3);
 		ret.setBackgroundColor(Twix_Theme.tableBG2);
 		
 		LinearLayout row;
 		SheaveRow vRow = new SheaveRow();
 		
 		vRow.Type = createAT(data.Type, false, adapterSheaveType, false, 1f, IF20, InputType.TYPE_CLASS_TEXT );
 		vRow.Number = createET(data.Number, false, false, 1f, IF10, InputType.TYPE_CLASS_TEXT );
 		vRow.Manufacturer = createAT(data.Manufacturer, false, adapterSheaveManufacturer, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
 		
 		row = createRow();
 		if( !eqAct.readOnly )
 			{
 			row.addView(createIB(R.drawable.minus, deleteSheave));
 			}
 		else
 			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
 		row.addView( createTV("Sheave Type: ", false, false, 1f ) );
 		row.addView( vRow.Type );
 		row.addView( createTV("Sheave Number: ", false, false, 1f ) );
 		row.addView( vRow.Number );
 		ret.addView( row );
 		
 		row = createRow();
 			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
 		row.addView( createTV("Sheave Manufacturer: ", false, false, 1f ) );
 		row.addView( vRow.Manufacturer );
 		row.addView( createTV("", false, false, 2f ) );
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
 			params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
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
 	
 	private LinearLayout createAddSheave()
 		{
 		LinearLayout ret = createRow();
 		((LinearLayout.LayoutParams) ret.getLayoutParams()).leftMargin = 50;
 		ret.addView(createIB(R.drawable.plus, addSheave));
 		ret.addView(createTV("Add Sheave", false, true, 1));
 		
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
 		
 		FanRow fRow;
 		SheaveRow sRow;
 		String s;
 		
 		int size2 = 0;
 		for( int i = size-1; i >= 0; i-- )
 			{
 			fRow = (FanRow) ll.getChildAt(i).getTag();
 			newId = db.newNegativeId("fan", "fanId");
 			
 			cv.put("equipmentId",	eqAct.equipmentId);
 			cv.put("fanId",			newId);
 			cv.put("partType",		fRow.FanType.getText().toString());
 			cv.put("number",		fRow.FanNumber.getText().toString());
 			db.db.insertOrThrow("Fan", null, cv);
 			cv.clear();
 			
 			cv.put("fanId",			newId);
 			cv.put("beltSize",		fRow.BeltSize.getText().toString());
 			s = fRow.BeltQty.getText().toString();
 			if( s.length() > 0)
 				cv.put("quantity",		Integer.parseInt(s) );
 			else
 				cv.put("quantity",		0 );
 			
 			db.db.insertOrThrow("Belt", null, cv);
 			cv.clear();
 			
 			size2 = fRow.Children.getChildCount();
 			for( int j = 0; j < size2; j++ )
 				{
 				sRow = (SheaveRow) fRow.Children.getChildAt(j).getTag();
 				
 				cv.put("fanId",			newId);
 	 			cv.put("type",			sRow.Type.getText().toString() );
 	 			cv.put("number",		sRow.Number.getText().toString() );
 	 			cv.put("manufacturer",	sRow.Manufacturer.getText().toString() );
 	 			db.db.insertOrThrow("Sheave", null, cv);
 	 			cv.clear();
 				}
 			}
 		
 		}
    
 	private void removeOriginalEntries()
    	{
    	// Better Deletes
    	db.db.execSQL("DELETE FROM Sheave WHERE FanId IN (SELECT FanId FROM Fan WHERE EquipmentId = " + eqAct.equipmentId + ")");
    	db.db.execSQL("DELETE FROM Belt WHERE FanId IN (SELECT FanId FROM Fan WHERE EquipmentId = " + eqAct.equipmentId + ")");
    	db.db.execSQL("DELETE FROM Fan WHERE EquipmentId = " + eqAct.equipmentId);
    	/* Old Deletes - Depends on Fetching Ids. Not Necessary
    	int size = fanIdList.size();
    	int id;
    	for( int i = 0; i < size; i++ )
    		{
    		id = fanIdList.get(i);
    		db.delete("Fan",	"FanId", id);
    		db.delete("Belt",	"FanId", id);
    		db.delete("Sheave",	"FanId", id);
    		}
    	*/
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