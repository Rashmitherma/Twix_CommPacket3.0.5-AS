package com.twix_agent;

import java.util.ArrayList;

import com.twix_agent.Twix_AgentServiceTagUnit.UnitDetails2;

import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceUnitMaterial
 * 
 * Purpose: Contains a material records for a specific piece of equipment. All material record data is validated
 * 			before saving. Empty material record rows are ignored.
 * 
 * Relevant XML: servicetag_material.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitMaterial extends Activity
	{
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext;
	//private Spinner UnitInfo;
	private Button UnitInfo;
	private TableLayout tl;
	private View main;
	private ArrayList<View> masterList;
	public Button unitSpinner,unitSpinner2;
	//Key Listeners
	private NumberKeyListener qty;
	private TextKeyListener desc;
	private NumberKeyListener cost;
	private NumberKeyListener refrig;
	
	// Text Watchers --- For character limit numbers
	private TextWatcher tw_qty;
	private TextWatcher tw_desc;
	private TextWatcher tw_cost;
	private TextWatcher tw_refrig;
	private TextWatcher tw_source;
	public int currentpickId;
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_material, null);
		this.setContentView( viewToLoad );
        
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		LocalActivityManager manager2 = unitAct.getLocalActivityManager();
		//UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).spinnerUnitNo;
		UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).unitSpinner;
		
		if( unitAct.tag.tagReadOnly )
			findViewById(R.id.dummyDelete_Material).setVisibility(View.GONE);
		
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        masterList = new ArrayList<View>();
        main = findViewById(R.id.main);
        
        tl = (TableLayout)findViewById(R.id.MaterialTable);
        unitSpinner = (Button)viewToLoad.findViewById(R.id.equiprentals);
        createWatchers();
        createClickListeners();
        createKeyListeners();
        readOnlySetup();
        clickListeners();
        readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( unitAct.tag.tagReadOnly )
			{
			findViewById(R.id.AddMaterial).setVisibility(View.INVISIBLE);
			findViewById(R.id.QuickButtons).setVisibility(View.GONE);
			}
		}
	private void clickListeners()
		{
		
		
		findViewById( R.id.equiprentals ).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	
            	final Dialog selection2;
            	unitSpinner.setEnabled(true);
				selection2 = new Dialog(mContext);
				selection2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		    	selection2.setContentView(R.layout.spinner_dropdown);
		    	selection2.getWindow().setLayout(600, LayoutParams.WRAP_CONTENT);
		    	
		    	selection2.setCanceledOnTouchOutside(true);
		    	selection2.setOnCancelListener(new OnCancelListener()
			    	{
					@Override
					public void onCancel(DialogInterface dialog)
						{
						selection2.dismiss();
						}
			    	});

		    	
		    	LinearLayout listHost = (LinearLayout) selection2.findViewById(R.id.ListHost);
		    	
		    	//String sqlQ = 
	    			//	"SELECT equipmentCategoryId, categoryDesc from equipmentCategory";
		    	
		    	String sqlQ = "select pi.PickItemId,pi.pickId, pi.itemValue from pickListItem pi inner join pickList p on p.pickId = pi.pickId where pi.PickItemId >= 227";
		    	Cursor cursor = db.rawQuery(sqlQ);
		    	UnitDetails2 unit2 = new UnitDetails2();
		    	unit2.selection2 = selection2;
		    	final boolean used = false;
		    	unit2.pickId = 0;
		    	listHost.addView( createSelectionRow2(unit2,false) );
		    	//boolean used = false;
		    	//boolean eall = false;
		    	int size = 0;
		    	
		    	if (cursor.moveToFirst())
					{
					do
						{
						unit2 = new UnitDetails2();
						unit2.selection2 = selection2;
						unit2.title =Twix_TextFunctions.clean(cursor.getString(1));
						
						
						unit2.pickId = cursor.getInt(1);
						unit2.Desc = cursor.getString(2);
						
						
						
						//for( int i = 0; i < size; i++ )
						//	if( unit2.equipmentId == usedPMEquipmentIds.get(i) )
						//		used = true;
						    
						listHost.addView( createSelectionRow2(unit2,used) );
						
						}
				    while (cursor.moveToNext());
					}
				if (cursor != null && !cursor.isClosed())
					{
					cursor.close();
					}
		    	
		    	selection2.show();
            	}});}
	
	private void createClickListeners()
		{
		OnClickListener reclaim = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				MaterialData reclaim = new MaterialData();
				reclaim.qty = "1";
				reclaim.desc = "Reclaim Refrigerant";
				reclaim.source = "Therma";
				addMaterial(reclaim);
				}
			};
		
		OnClickListener vacuum = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				MaterialData reclaim = new MaterialData();
				reclaim.qty = "1";
				reclaim.desc = "Vacuum";
				reclaim.source = "Therma";
				addMaterial(reclaim);
				}
			};
		
		OnClickListener weld = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				MaterialData reclaim = new MaterialData();
				reclaim.qty = "1";
				reclaim.desc = "Weld";
				reclaim.source = "Therma";
				addMaterial(reclaim);
				}
			};
		
		OnClickListener disposal = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				MaterialData reclaim = new MaterialData();
				reclaim.qty = "1";
				reclaim.desc = "Disposal";
				reclaim.source = "Therma";
				addMaterial(reclaim);
				}
			};
		
		
		//Button bn = (Button) findViewById(R.id.Reclaim);
	//	bn.setOnClickListener(reclaim);
		
		//bn = (Button) findViewById(R.id.Vacuum);
		//bn.setOnClickListener(vacuum);
		
		//bn = (Button) findViewById(R.id.Weld);
		//bn.setOnClickListener(weld);
		
		//bn = (Button) findViewById(R.id.Disposal);
		//bn.setOnClickListener(disposal);
		}
	
	private void createWatchers()
		{
		tw_qty		= generateTextLimit(13);
		tw_desc		= generateTextLimit(100);
		tw_cost		= generateTextLimit(13);
		tw_refrig	= generateTextLimit(10);
		tw_source	= generateTextLimit(200);
		}
	
	private TextWatcher generateTextLimit(final int limit)
		{
		TextWatcher tw = new TextWatcher()
			{
			CharSequence prev;
			boolean dirty = false;
			@Override
			public void afterTextChanged(Editable s)
				{
				if( s.length() > limit && !dirty )
					{
					dirty = true;
					s.clear();
					s.append(prev);
					}
				else
					dirty = false;
				}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
				{
				if( !dirty )
					prev = s.toString();
				}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count)
				{
				}
			};
		
		return tw;
		}
	
	private void readSQL()
		{
		createDummyRow();
		
		String sqlQ = "select serviceMaterial.quantity, serviceMaterial.materialDesc, " +
							"serviceMaterial.cost, serviceMaterial.refrigerantAdded, serviceMaterial.source " +
						"from serviceMaterial " +
						"where serviceMaterial.serviceTagUnitId = " + unitAct.serviceTagUnitId;
		
    	Cursor cursor = db.rawQuery(sqlQ);
    	MaterialData row = new MaterialData();
		if ( cursor.moveToFirst() )
			{
			do
				{
				row.reset();
				row.qty		= Twix_TextFunctions.clean(cursor.getString(0));
				row.desc	= Twix_TextFunctions.clean(cursor.getString(1));
				row.cost	= Twix_TextFunctions.clean(cursor.getString(2));
				row.refrig	= Twix_TextFunctions.clean(cursor.getString(3));
				row.source	= Twix_TextFunctions.clean(cursor.getString(4));
				addMaterial(row);
				}
			while (cursor.moveToNext());
			}
		else
			addMaterial(new MaterialData());
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	private class UnitDetails2
	{
	String title = "Not Selected";
	int equipmentId;
	String Desc = null;
	int pickId;
	Dialog selection2;
	
	
	}
	
	private class MaterialData
		{
		String qty = "";
		String desc = "";
		String source = "";
		String cost = "";
		String refrig = "";
		
		public void reset()
			{
			qty = "";
			desc = "";
			source = "";
			cost = "";
			refrig = "";
			}
		}
	
	private class MaterialRow
		{
		EditText Qty;
		EditText Description;
		EditText Source;
		EditText Cost;
		EditText RefAdded;
		
		public boolean isEmpty()
			{
			return ( (Qty.length() <= 0)
					&& (Description.length() <= 0)
					&& (Source.length() <= 0) 
					&& (Cost.length() <= 0)
					&& (RefAdded.length() <= 0) );
			}
		}
	
	public void addMaterial(MaterialData input)
		{
		if( input == null )
			input = new MaterialData();
		
		MaterialRow rowTag = new MaterialRow();
		
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.FILL_PARENT;
		params.height = LayoutParams.FILL_PARENT;
		params.weight = 1;
		params.setMargins(2, 2, 2, 2);
		
		LayoutParams params1 = new LayoutParams();
		params1.width = 0;
		params1.height = LayoutParams.FILL_PARENT;
		params1.weight = 1;
		params1.setMargins(2, 2, 2, 2);
		
		LayoutParams params2 = new LayoutParams();
		params2.width = 0;
		params2.height = LayoutParams.FILL_PARENT;
		params2.weight = 2;
		params2.setMargins(2, 2, 2, 2);
		
		LayoutParams params3 = new LayoutParams();
		params3.width = 0;
		params3.height = LayoutParams.FILL_PARENT;
		params3.weight = 3;
		params3.setMargins(2, 2, 2, 2);
		
		EditText et, startBox;
		
		TableRow row = new TableRow(this);
		row.setTag(rowTag);
		
		// Add the minus button to the row
		LayoutParams paramsBn = new LayoutParams();
    	paramsBn.height = LayoutParams.WRAP_CONTENT;
    	paramsBn.width = LayoutParams.WRAP_CONTENT;
    	
    	if( !unitAct.tag.tagReadOnly )
    		{
	    	ImageButton bn = new ImageButton(this);
	    	bn.setLayoutParams(paramsBn);
	    	bn.setImageResource(R.drawable.minus);
	    	bn.setBackgroundColor(Twix_Theme.sub1BG);
			bn.setPadding(10, 10, 10, 10);
			bn.setOnClickListener(new  OnClickListener()
	        	{
	            @Override
	            public void onClick(View v)
	            	{
	            	removeRow((TableRow)v.getParent());
	                }
	        	});
	    	row.addView(bn);
    		}
		// Add each edit text to the row
    	/**
    	 * Quantity
    	 */
    	startBox = createEditBox(params1, 5, qty);
    	startBox.setHint("0");
    	startBox.setEnabled(!unitAct.tag.tagReadOnly);
		startBox.setText( input.qty );
		startBox.addTextChangedListener(unitAct.setDirtyFlag);
		startBox.addTextChangedListener(tw_qty);
		rowTag.Qty = startBox;
		row.addView(startBox);
		
		/**
    	 * Description
    	 */
		et = createEditBox(params3, 27, desc);
		et.setHint("description");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		et.setText( input.desc );
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.addTextChangedListener(tw_desc);
		rowTag.Description = et;
		row.addView(et);
		
		/**
    	 * Source
    	 */
		et = createEditBox(params2, 8, desc);
		et.setHint("source");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		et.setText( input.source );
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.addTextChangedListener(tw_source);
		rowTag.Source = et;
		row.addView(et);
		
		/**
    	 * Cost
    	 */
		et = createEditBox(params1, 12, cost);
		et.setHint("0.00");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setText( input.cost );
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.addTextChangedListener(tw_cost);
		rowTag.Cost = et;
		row.addView(et);
		
		/**
    	 * Refrigeration
    	 */
		et = createEditBox(params1, 14, refrig);
		et.setHint("0.0000");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setText( input.refrig );
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.addTextChangedListener(tw_refrig);
		rowTag.RefAdded = et;
		row.addView(et);
		
		masterList.add(row);
		tl.addView(row);
		
		
		ScrollView sv = (ScrollView) findViewById(R.id.ScrollView01);
		sv.fullScroll(ScrollView.FOCUS_DOWN);
		startBox.requestFocus();
		}
	
	private void removeRow(TableRow row)
		{
		if( masterList.size() > 0 )
			{
			int index = tl.indexOfChild(row)-1; // Starts at 0, but we have the dummy row taking up the '0' index
			masterList.remove(index);
			tl.removeView(row);
			main.invalidate();
			}
		}
	private View createSelectionRow2(UnitDetails2 unit2,boolean used)
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);
		final TextView tv2 = new TextView(mContext);
		
		
		tv2.setLayoutParams(params);
		tv2.setTextSize(Twix_Theme.headerSize);
		tv2.setTextColor(Twix_Theme.headerValue);
		tv2.setBackgroundColor(Twix_Theme.tableBG2);
		
		 //final boolean eall =false;
			if( used )
				tv2.setBackgroundResource(R.drawable.clickable_bg);
			else
				tv2.setBackgroundResource(R.drawable.clickable_bg2);
		
		
		tv2.setText(unit2.Desc);
		
		tv2.setTag(unit2);
		tv2.setPadding(3, 3, 3, 3);
		tv2.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				UnitDetails2 unit2 = (UnitDetails2) v.getTag();
				
			
				currentpickId = unit2.pickId;
				
				
				
				
				
				unit2.selection2.dismiss();
				
				unitAct.dirtyFlag = true;
				MaterialData reclaim = new MaterialData();
				reclaim.qty = "";
				reclaim.desc = unit2.Desc;
				reclaim.source = "Therma";
				addMaterial(reclaim);
				}
				
			});
		
		return tv2;
		}
	private void createDummyRow()
    	{
		TableRow row = new TableRow(this);
    	
		int[] arr = { 1, 3, 2, 1, 1 };
		
		LayoutParams paramsBn = new LayoutParams();
    	paramsBn.height = 0;
    	paramsBn.width = LayoutParams.WRAP_CONTENT;
    	
    	ImageButton bn = new ImageButton(this);
    	bn.setLayoutParams(paramsBn);
    	bn.setImageResource(R.drawable.minus);
    	bn.setBackgroundColor(Twix_Theme.sub1BG);
    	bn.setPadding(10, 10, 10, 10);
		row.addView(bn);
		
    	for( int i = 0; i < 5; i++) // Hard coded number of columns
    		{
			View tv = new TextView(this);
			LayoutParams params = new LayoutParams();
	    	params.width = 0;
			//params.height = LayoutParams.WRAP_CONTENT;
			params.height = 0;
			params.setMargins(2, 0, 2, 0);
			params.weight = arr[i];
			
		    tv.setLayoutParams(params);
		    tv.setPadding(5, 0, 5, 0);
		    //tv.setBackgroundColor(Twix_Theme.headerBG);
		    row.addView(tv);
    		}
		tl.addView(row);
    	}
	
	public boolean validateSave()
		{
		boolean ret = true;
		boolean valid = true;
		boolean isEmpty = true;
		resetBGs();
		
		MaterialRow row;
		String s;
		
		int size = masterList.size();
		for( int i = 0; i < size; i++ )
			{
			row = (MaterialRow) masterList.get(i).getTag();
			isEmpty = row.isEmpty();
			valid = true;
			
			if( !isEmpty )
				{
				
				}
			s = row.Description.getText().toString();
			if ( s.length() < 1 )
				{
				mark(row.Description);
				valid = false;
				}
			
			s = row.Source.getText().toString();
			if ( s.length() < 1 )
				{
				mark(row.Source);
				valid = false;
				}
			
			s = row.Cost.getText().toString();
			if( !s.matches("^[0-9]{0,6}(\\.[0-9]{0,2}){0,1}$") )
				{
				mark(row.Cost);
				valid = false;
				}
			
			s = row.RefAdded.getText().toString();
			if( !s.matches("^[0-9]{0,6}(\\.[0-9]{0,4}){0,1}$") )
				{
				mark(row.RefAdded);
				valid = false;
				}
			
			s = row.Qty.getText().toString();
			if ( (!s.matches("^[0-9]{1,6}$")) )
				{
				mark(row.Qty);
				valid = false;
				}
			
			// Don't return, because we want to mark any incorrect fields
			if( !valid && !isEmpty)
				ret = false;
			}
		
		return ret;
		}
	
	private void resetBGs()
		{
		MaterialRow row;
		int size = masterList.size();
		for( int i = 0; i < size; i++ )
			{
			row = (MaterialRow) masterList.get(i).getTag();
			unMark(row.Qty);
			unMark(row.Description);
			unMark(row.Source);
			unMark(row.Cost);
			unMark(row.RefAdded);
			}
		}
	
	public void updateDB()
		{
		db.delete("serviceMaterial", "serviceTagUnitId", unitAct.serviceTagUnitId );
		
		int size = masterList.size();
		if( size < 1 )
			return;
		
		ContentValues cv = new ContentValues();
		String cost;
		
		MaterialRow row;
		// Traverse the list backwards so it doesn't reorder
		for( int i = size-1; i >= 0; i-- )
			{
			row = (MaterialRow) masterList.get(i).getTag();
			if( !row.isEmpty() )
				{
				cv.put("serviceMaterialId", db.newNegativeId("serviceMaterial", "serviceMaterialId") );
				cv.put("serviceTagUnitId",	unitAct.serviceTagUnitId );
				cv.put("quantity",			row.Qty.getText().toString() );
				cv.put("materialDesc",		row.Description.getText().toString() );
				cost = row.Cost.getText().toString();
				if( cost.length() <= 0 )
					cost = "0";
				cv.put("cost",				Float.parseFloat(cost) );
				cv.put("refrigerantAdded",	row.RefAdded.getText().toString() );
				cv.put("source",			row.Source.getText().toString() );
				
				db.db.insertOrThrow("serviceMaterial", null, cv);
				cv.clear();
				}
			}
		
		}
	
	private void mark( View v )
		{
		v.setBackgroundColor(Twix_Theme.warnColorLight);
		}
	
	private void unMark(View v)
		{
		v.setBackgroundColor(Twix_Theme.editBG);
		}
	
	private EditText createEditBox(LayoutParams params, int size, KeyListener key)
		{
		EditText et = new EditText(this);
		
		et.setLayoutParams(params);
		et.setTextColor(Twix_Theme.sub1Value);
		et.setBackgroundResource(R.drawable.editbox);
		et.setTextSize(Twix_Theme.subSize);
		et.setPadding(5, 5, 5, 5);
		if( key != null )
			et.setKeyListener(key);
		et.setSingleLine();
		//et.setTypeface(Typeface.MONOSPACE);
		
		return et;
		}
	
	private void createKeyListeners()
		{
		qty = new NumberKeyListener()
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
		
		desc = new TextKeyListener(TextKeyListener.Capitalize.WORDS, true)
			{
		    public int getInputType()
		    	{
			    return InputType.TYPE_CLASS_TEXT;
			    }
		    };
		
		cost = new NumberKeyListener()
			{
		    public int getInputType()
		    	{
			    return InputType.TYPE_CLASS_PHONE;
			    }
		
		    @Override
		    protected char[] getAcceptedChars()
		    	{
			    return new char[] 
			    	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' };
			    }
		    };
			    
		refrig = new NumberKeyListener()
			{
		    public int getInputType()
		    	{
			    return InputType.TYPE_CLASS_PHONE;
			    }
		
		    @Override
		    protected char[] getAcceptedChars()
		    	{
			    return new char[] 
			    	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' };
			    }
		    };
		
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
	}
