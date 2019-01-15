package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentSafetyChecklist
 * 
 * Purpose: Displays all the safety checklist items for open tags. Safety checklist items have two catagories,
 * 			LOTO and non-LOTO. When an item is marked "required" it must be marked complete as well.
 * 			Requirements are up to the user's discretion.
 * 
 * Relevant XML: safety_checklist.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSafetyChecklist extends Activity
	{
	private int checklistCount = 0;
	
	private View viewToLoad;
	private Context mContext;
	private Twix_AgentOpenTag act;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private boolean bgColor = false;
	private boolean bgColorLOTO = false;
	TableLayout checklistBody, LOTOlistBody, checklistHeader, LOTOlistHeader;
	
	private List<View> safetyRows;
	
	// Input Filters
	private InputFilter[] max50;
	
	public void onCreate(Bundle savedInstanceState)
    	{
    	super.onCreate(savedInstanceState);
		mContext = getParent().getParent();
		viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.safety_checklist, null);
		
        
        app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
        
		LocalActivityManager manager = ((Twix_AgentActivityGroup2)mContext).getLocalActivityManager();
		LocalActivityManager manager2 = ((Twix_AgentOpenTagsTabHost)manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		act = (Twix_AgentOpenTag) manager2.getActivity("Tag");
		
		readOnlySetup();
        
        checklistHeader = (TableLayout) viewToLoad.findViewById(R.id.ChecklistHeader);
        LOTOlistHeader = (TableLayout) viewToLoad.findViewById(R.id.LOTOlistHeader);
        
        checklistBody = (TableLayout) viewToLoad.findViewById(R.id.ChecklistBody);
        LOTOlistBody = (TableLayout) viewToLoad.findViewById(R.id.LOTOlistBody);

        safetyRows = new ArrayList<View>();
        max50 = new InputFilter[] {new InputFilter.LengthFilter(50)};
        
        readSQL();
        this.setContentView( viewToLoad );
    	}
	
	private void readOnlySetup()
		{
		((EditText)viewToLoad.findViewById(R.id.Comments)).setEnabled(!act.tagReadOnly);
		}
	
	private class SafetyChecklistData
		{
		int safetyChecklistId = 0;
		String itemType;
		String itemText;
		String itemTextBold;
		}
	
	private class SafetyChecklistRow
		{
		int safetyChecklistId = 0;
		CheckBox required;
		View value;
		}
	
	private void readSQL()
		{
		if( act.serviceTagId == 0 )
	    	{
	    	return;
	    	}
	    // Build the SafetyChecklist frame
    	String sqlQ = "SELECT safetyChecklistId, itemType, itemText, itemTextBold, LOTO " +
    					"FROM safetyChecklist " +
						"ORDER BY sortOrder";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	LayoutParams params = new LayoutParams();
    	params.width = LayoutParams.MATCH_PARENT;
    	params.height = LayoutParams.WRAP_CONTENT;
    	
    	SafetyChecklistData data;
    	
		if (cursor.moveToFirst())
			{
			createHeader(checklistHeader);
			createHeader(LOTOlistHeader);
			
			do
				{
				data = new SafetyChecklistData();
				data.safetyChecklistId	= cursor.getInt(0);
				data.itemType			= cursor.getString(1);
				data.itemText			= cursor.getString(2);
				data.itemTextBold		= cursor.getString(3);
				
				if( Twix_TextFunctions.clean(cursor.getString(4)).contentEquals("Y") )
					{
					createRow(data, LOTOlistBody, bgColorLOTO);
					bgColorLOTO = !bgColorLOTO;
					}
				else
					{
					createRow(data, checklistBody, bgColor);
					bgColor = !bgColor;
					}
				
				checklistCount++;
				}
			while (cursor.moveToNext());
			}
		else
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		if( checklistBody.getChildCount() < 1 )
			{
			checklistHeader.setVisibility(View.GONE);
			checklistBody.setVisibility(View.GONE);
			((TextView)viewToLoad.findViewById(R.id.CheckListText)).setVisibility(View.GONE);
			}
		
		if( LOTOlistBody.getChildCount() < 1 )
			{
			LOTOlistHeader.setVisibility(View.GONE);
			LOTOlistBody.setVisibility(View.GONE);
			((TextView)viewToLoad.findViewById(R.id.LOTOListText)).setVisibility(View.GONE);
			}
		
		
		// Fetch the SafetyChecklist values saved in the database
		sqlQ = "SELECT safetyChecklistId, itemRequired, itemValue " +
				"FROM safetyTagChecklistItem " +
					"WHERE serviceTagId = " + act.serviceTagId;
		cursor = db.rawQuery(sqlQ);
		SafetyChecklistRow row;
		if (cursor.moveToFirst())
			{
			int index = 0;
			do
				{
				if( index < checklistCount )
					{
					row = (SafetyChecklistRow) safetyRows.get(0).getTag();
					
					row.required.setChecked( Twix_TextFunctions.clean(cursor.getString(1)).contentEquals("Y") );
					
					if( row.value.getClass() == CheckBox.class )
						((CheckBox)row.value).setChecked( Twix_TextFunctions.clean(cursor.getString(2)).contentEquals("Y") );
					else
						((EditText)row.value).setText( cursor.getString(2) );
					
					}
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		// Fetch the SafetyChecklist Comments
		sqlQ = "SELECT comments " +
				"FROM safetyTagChecklist " +
					"WHERE serviceTagId = " + act.serviceTagId;
		cursor = db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			((EditText)viewToLoad.findViewById(R.id.Comments)).setText(cursor.getString(0));
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		}
	
	private void createHeader( TableLayout table )
		{
		TableRow row = new TableRow(this);
		
		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		params.weight = 1;
		
		LayoutParams paramsNoWeight200 = new LayoutParams();
		paramsNoWeight200.height = LayoutParams.MATCH_PARENT;
		paramsNoWeight200.width = LayoutParams.WRAP_CONTENT;
		
		LayoutParams paramsNoWeight100 = new LayoutParams();
		paramsNoWeight100.height = LayoutParams.FILL_PARENT;
		paramsNoWeight100.width = LayoutParams.WRAP_CONTENT;
		
		row.addView( createText("Required", paramsNoWeight100, false, Gravity.CENTER_HORIZONTAL ) );
		row.addView( createText("", params, false, Gravity.CENTER_HORIZONTAL ) );
		row.addView( createText("Complete", paramsNoWeight200, false, Gravity.CENTER_HORIZONTAL ) );
		
		table.addView(row);
		}
	
	private void createRow(SafetyChecklistData data, TableLayout table, boolean bg)
		{
		SafetyChecklistRow rowTag = new SafetyChecklistRow();
		rowTag.safetyChecklistId = data.safetyChecklistId;
		
		TableRow row = new TableRow(this);
		row.setTag(rowTag);
		safetyRows.add(row);
		
		TableLayout.LayoutParams p = new TableLayout.LayoutParams();
		p.setMargins(0, 0, 0, 2);
		row.setLayoutParams(p);
		if( bg )
			row.setBackgroundColor(Twix_Theme.sortNone);
		else
			row.setBackgroundColor(Twix_Theme.sortDesc);
		
		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		params.weight = 1;
		
		LayoutParams paramsNoWeight = new LayoutParams();
		paramsNoWeight.height = LayoutParams.WRAP_CONTENT;
		paramsNoWeight.width = LayoutParams.MATCH_PARENT;
		
		LayoutParams paramsNoWeight2 = new LayoutParams();
		paramsNoWeight2.height = LayoutParams.WRAP_CONTENT;
		paramsNoWeight2.width = LayoutParams.MATCH_PARENT;
		paramsNoWeight2.setMargins(5, 0, 0, 0);
		
		LayoutParams paramsNoWeight100 = new LayoutParams();
		paramsNoWeight100.height = LayoutParams.MATCH_PARENT;
		paramsNoWeight100.width = 100;
		paramsNoWeight100.gravity = Gravity.CENTER_HORIZONTAL;
		
		LayoutParams paramsNoWeight200 = new LayoutParams();
		paramsNoWeight200.height = LayoutParams.MATCH_PARENT;
		paramsNoWeight200.width = 200;
		
		CheckBox cb = new CheckBox(this);
		cb.setLayoutParams(paramsNoWeight100);
		cb.setEnabled(!act.tagReadOnly);
		rowTag.required = cb;
		row.addView( cb );
		
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(params);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity( Gravity.CENTER_VERTICAL);
		ll.addView( createText(data.itemText, paramsNoWeight, false, Gravity.CENTER_VERTICAL ) );
		if( data.itemTextBold != null  && data.itemTextBold.length() > 0 )
				ll.addView( createText(data.itemTextBold, paramsNoWeight, false, Gravity.CENTER_VERTICAL ) );
		row.addView(ll);
		
		
		if( data.itemType.contentEquals("C") )
			{
			cb = new CheckBox(this);
			paramsNoWeight200.gravity = Gravity.CENTER;
			cb.setLayoutParams(paramsNoWeight200);
			cb.setEnabled(!act.tagReadOnly);
			row.addView(cb);
			rowTag.value = cb;
			}
		else
			{
			EditText et = new EditText(this);
			paramsNoWeight200.gravity = Gravity.CENTER_VERTICAL;
			et.setLayoutParams(paramsNoWeight200);
			et.setHint("response");
			et.setTextColor(Twix_Theme.sub1Value);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!act.tagReadOnly);
			et.setSingleLine();
			et.setFilters(max50);
			row.addView(et);
			rowTag.value = et;
			}
		
		table.addView(row);
		}
	
	private TextView createText(String s, LayoutParams params, boolean bold, int gravity)
		{
		TextView tv = new TextView(this);
		
		tv.setLayoutParams(params);
		tv.setTextSize(Twix_Theme.subSize);
		tv.setTextColor(Twix_Theme.sub1Header);
		tv.setText(s);
		tv.setPadding(5, 0, 5, 0);
		tv.setGravity(gravity);
		if( bold )
			tv.setTypeface(Typeface.DEFAULT_BOLD);
		
		return tv;
		}
	
	private void updateDB()
		{
		db.delete("safetyTagChecklistItem", "serviceTagId", act.serviceTagId );
		db.delete("safetyTagChecklist", "serviceTagId", act.serviceTagId );
		
		
		ContentValues cv = new ContentValues();
		String comments = ((EditText)viewToLoad.findViewById(R.id.Comments)).getText().toString();
		
		cv.put("serviceTagId",		act.serviceTagId);
		cv.put("checkListDate",		Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT) );
		cv.put("comments",			comments);
		db.db.insertOrThrow("safetyTagCheckList", null, cv);
		cv.clear();
		
		SafetyChecklistRow row;
		int size = safetyRows.size();
		for( int i = 0; i < size; i++ )
			{
			row = (SafetyChecklistRow) safetyRows.get(i).getTag();
			
			cv.put("serviceTagId",		act.serviceTagId);
			cv.put("safetyChecklistId",	row.safetyChecklistId);
			if( row.required.isChecked() )
				cv.put("itemRequired",		"Y");
			else
				cv.put("itemRequired",		"N");
			
			if( row.value.getClass() == CheckBox.class )
				{
				if( ((CheckBox)row.value).isChecked() )
					cv.put("itemValue",			"Y");
				else
					cv.put("itemValue",			"N");
				}
			else
				cv.put("itemValue",			((EditText)row.value).getText().toString() );
			
			db.db.insertOrThrow("safetyTagChecklistItem", null, cv);
			cv.clear();
			}
		}
	
	public void onDestroy()
		{
		if( !act.tagReadOnly )
			updateDB();
		
		super.onDestroy();
		}
	}
