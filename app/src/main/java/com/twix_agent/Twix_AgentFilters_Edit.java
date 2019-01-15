package com.twix_agent;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentFilters_Edit
 * 
 * Purpose: Allows the user to edit the filters on a piece of equipment.
 * 
 * Relevant XML: filters_edit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentFilters_Edit extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_AgentEquipmentTabHost_Edit eqAct;
	private Twix_SQLite db;
	private Context mContext;
	private LinearLayout ll;
	//private TableLayout tl;
	
	private ArrayAdapter<CharSequence> adapterFilterType;
	private ArrayAdapter<CharSequence> adapterFilterSize;
	
	private OnClickListener dropClick;
	private OnFocusChangeListener dropFocus;
	
	// Input Filters
	private InputFilter[] IF20 = new InputFilter[1];
	private InputFilter[] IF50 = new InputFilter[1];
	private InputFilter[] IF9 = new InputFilter[1];
	private final InputFilter IF20ele = new InputFilter.LengthFilter(20);
	private final InputFilter IF50ele = new InputFilter.LengthFilter(50);
	private final InputFilter IF9ele = new InputFilter.LengthFilter(9); //For ints: 2147483647
	NumberKeyListener numbersOnly;
	
	
	private OnClickListener addFilter;
	private OnClickListener deleteFilter;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
        View viewToLoad = LayoutInflater.from(getParent().getParent()).inflate(R.layout.filters_edit, null);
		this.setContentView(viewToLoad);
        
		eqAct = (Twix_AgentEquipmentTabHost_Edit)getParent();
		
		ll = (LinearLayout) findViewById(R.id.TableHost_Filter);
		
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        if( eqAct.readOnly )
        	{
        	findViewById(R.id.New_Filter).setVisibility(View.GONE);
        	findViewById(R.id.Title_New_Filter).setVisibility(View.GONE);
        	}
        
        setupInputFilters();
        buildAdapters();
        createClickListeners();
        
        //readSQL();
    	readSQLClass();
    	}
	
	private void setupInputFilters()
		{
		IF20[0] = IF20ele;
		IF50[0] = IF50ele;
		IF9[0] = IF9ele;
		
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
		
		adapterFilterType = Twix_TextFunctions.BuildAdapter(db, mContext, "Filter Type");
		adapterFilterSize = Twix_TextFunctions.BuildAdapter(db, mContext, "Filter Size");
		}
	
    private class FilterData
		{
		String Type;
		String Qty;
		String Size;
		}
 
	private class FilterRow
		{
		LinearLayout Table;
		
		AutoCompleteTextView Type;
		EditText Qty;
		AutoCompleteTextView Size;
		}
	
	private void readSQLClass()
	    {
	 	String sqlQ = "SELECT type, quantity, filterSize " + 
						"FROM filter " +
						"WHERE equipmentId = " + eqAct.equipmentId;
	 	
	 	Cursor cursor = db.rawQuery(sqlQ);
	 	
	 	LayoutParams params = new LayoutParams();
	 	params.width = LayoutParams.FILL_PARENT;
	 	params.height = LayoutParams.WRAP_CONTENT;
	 	params.setMargins(3, 3, 3, 10);
 	
		if (cursor.moveToFirst())
			{
			FilterData filterData;
			FilterRow vTable;
			do
				{
				filterData = new FilterData();
				filterData.Type		= Twix_TextFunctions.clean(cursor.getString(0));
				filterData.Qty		= Twix_TextFunctions.clean(cursor.getString(1));
				filterData.Size		= Twix_TextFunctions.clean(cursor.getString(2));
				
				// Create build all the rows
				vTable = createFilterRow(filterData);
				// Add the resulting table to the host table
				ll.addView(vTable.Table);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	    }
	
	private void createClickListeners()
		{
		deleteFilter = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				final View bn = v;
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to delete this Filter?")
						.setCancelable(true)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								eqAct.dirtyFlag = true;
								FilterRow row = (FilterRow) ((View)bn.getParent().getParent()).getTag();
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
		
		addFilter = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				eqAct.dirtyFlag = true;
				FilterData data = new FilterData();
				data.Type = "";
				data.Qty = "";
				data.Size = "";
				
				ll.addView( createFilterRow(data).Table );
				}
			}
		;
		
		ImageButton ib = (ImageButton)findViewById(R.id.New_Filter);
		ib.setOnClickListener(addFilter);
		
		}
	
	private FilterRow createFilterRow(FilterData data)
		{
		FilterRow vTable	= new FilterRow();
		vTable.Type	= createAT( data.Type, true, adapterFilterType, false, 1f, IF50, InputType.TYPE_CLASS_TEXT );
		vTable.Qty	= createET( data.Qty, false, false, 1f, IF9, InputType.TYPE_CLASS_PHONE );
		if( !eqAct.readOnly )
			{
			vTable.Qty.setKeyListener(numbersOnly);
			}
		vTable.Size	= createAT( data.Size, false, adapterFilterSize, false, 1f, IF20, InputType.TYPE_CLASS_TEXT );
		
		vTable.Table = createTable();
		vTable.Table.setTag(vTable);
		vTable.Table.setBackgroundColor(Twix_Theme.tableBG);
		((LinearLayout.LayoutParams)vTable.Table.getLayoutParams()).setMargins(3,6,3,6);
		
		LinearLayout row = createRow();
		row.setBackgroundColor(Twix_Theme.headerBG);
		if( !eqAct.readOnly )
			{
			row.addView(createIB(R.drawable.minus, deleteFilter));
			}
			
		// Filter Type
			row.addView(createTV( "Filter Type: ", true, true, 1f ));
			row.addView(vTable.Type);
			row.addView(createTV( "", false, true, 2f ));
		vTable.Table.addView(row);
		
		// Filter Quantity & Size
		row = createRow();
			((LinearLayout.LayoutParams)row.getLayoutParams()).setMargins(35,0,0,0);
		row.addView(createTV( "Filter Quantity: ", false, true, 1f ));
		row.addView(vTable.Qty);
		row.addView(createTV( "Filter Size: ", false, true, 1f ));
		row.addView(vTable.Size);
		vTable.Table.addView(row);
	
		return vTable;
		}
	
	private AutoCompleteTextView createAT(String text, boolean header,
			ArrayAdapter<CharSequence> adapter, boolean wrap,
			float weight, InputFilter[] inputFilter, int inputType )
		{
		AutoCompleteTextView at = new AutoCompleteTextView(mContext);
		LinearLayout.LayoutParams params;
		if( wrap )
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
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
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
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
			params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
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
	
	private LinearLayout createRow()
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.HORIZONTAL);
		
		return ret;
		}
	
	private LinearLayout createTable()
		{
		LinearLayout ret = new LinearLayout(mContext);
		
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		
		return ret;
		}
	
	public void updateDBClass()
 		{
 		ContentValues cv = new ContentValues();
 		removeOriginalEntries();
 		
 		int size = ll.getChildCount();
 		FilterRow fRow;
 		String s;
 		
 		for( int i = size-1; i >= 0; i-- )
 			{
 			fRow = (FilterRow) ll.getChildAt(i).getTag();
 			
 			cv.put("equipmentId",	eqAct.equipmentId);
 			cv.put("type",			fRow.Type.getText().toString());
 			s = fRow.Qty.getText().toString();
 			if( s.length() > 0 )
 				cv.put("quantity",		Integer.parseInt(s) );
 			else
 				cv.put("quantity",		0 );
 			cv.put("filterSize",	fRow.Size.getText().toString());
 			
 			db.db.insertOrThrow("Filter", null, cv);
 			cv.clear();
 			}
 		
 		}
    
	private void removeOriginalEntries()
    	{
    	db.delete("filter", "equipmentId", eqAct.equipmentId);
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