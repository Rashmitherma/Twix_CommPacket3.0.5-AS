package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceUnitLabor
 * 
 * Purpose: Contains a labor records for a specific piece of equipment. These labor records are later used to add
 * 			in timecard data. All labor record data is validated before saving. Empty labor record rows are ignored.
 * 
 * Relevant XML: servicetag_labor.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitLabor extends Activity
	{
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext;
	private Button UnitInfo;
	private LinearLayout tl,tl2;
	private OnItemSelectedListener dirtySelect;
	private int rowCount = 0;
	private static final int	REQUEST_PATH	= 1;
	String curRateName ; 
	private int currentCount = 0;
	public ArrayAdapter<CharSequence> rAdapter;
	private final String hoursRegx = "^[-]{0,1}[0-9]{0,4}(\\.[0-9]{0,2}){0,1}$";
	private MechanicArrayAdapter adapter;
	private RateArrayAdapter radapter;
	private TextWatcher t_r;
	//Key Listeners
	private NumberKeyListener hrs;
	
	// Text Watchers --- For character limit numbers
	private TextWatcher tw_Hrs;
	private TextKeyListener rt;
	private TextWatcher tw_rt;
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_labor, null);
		this.setContentView( viewToLoad );
        
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		LocalActivityManager manager2 = unitAct.getLocalActivityManager();
		//UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).spinnerUnitNo;
		UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).unitSpinner;
		
		if( unitAct.tag.tagReadOnly )
			findViewById(R.id.dummyDelete_Labor).setVisibility(View.GONE);
		
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		
        //main = findViewById(R.id.main);
        tl = (LinearLayout)findViewById(R.id.LaborTable1);
        tl2 = (LinearLayout)findViewById(R.id.LaborTable2);
        
        createWatchers();
        buildAdapters();
        buildAdapters2();
        createKeyListeners();
        
        readOnlySetup();
        
        readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( unitAct.tag.tagReadOnly )
			{
			findViewById(R.id.AddLabor).setVisibility(View.INVISIBLE);
			}
		}
	
	private void buildAdapters()
		{
		adapter = new MechanicArrayAdapter(mContext, new ArrayList<MechanicData>());
		String sqlQ = "SELECT mechanic.mechanic, mechanic.mechanic_name, " +
							"CASE WHEN dept = 'REF' THEN 0 ELSE 1 END as deptSort " +
						"FROM mechanic " +
							"WHERE Terminated != 'Y' " +
						"ORDER BY deptSort asc, mechanic.mechanic_name asc";
    	Cursor cursor = db.rawQuery(sqlQ);
    	// Add a default first selection to the drop down. This choice will not be accepted by the validateSave()
    	MechanicData data = new MechanicData();
		data.mechanic = "";
		data.name = "--Select One--";
		data.refDept = false;
		adapter.add(data);
    	if ( cursor.moveToFirst() )
			{
			do
				{
				data = new MechanicData();
				data.mechanic = cursor.getString(0);
				data.name = cursor.getString(1);
				data.refDept = cursor.getInt(2) == 0;
				adapter.add(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		buildDirty();
		}
	
	private void buildAdapters2()
		{
		radapter = new RateArrayAdapter(mContext, new ArrayList<RateData>());
		String sqlQ = "SELECT rate, rateDesc " +
						"FROM serviceLaborRate";
    	Cursor cursor = db.rawQuery(sqlQ);
    	// Add a default first selection to the drop down. This choice will not be accepted by the validateSave()
    	RateData data2 = new RateData();
		data2.rate = " --Select One--";
		data2.ratedesc = "";
		data2.refDept = false;
		radapter.add(data2);
    	if ( cursor.moveToFirst() )
			{
			do
				{
				data2 = new RateData();
				data2.rate = cursor.getString(0);
				data2.ratedesc = cursor.getString(1);
				//data2.refDept = cursor.getInt(2) == 0;
				radapter.add(data2);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		buildDirty();
		}
	
	/*private void includeTermAdapter2(String mechanic2, Spinner sp2)
		{
		String sqlQ = "select mechanic.mechanic_name " +
				"from mechanic " +
				"WHERE mechanic.mechanic == '" + mechanic2 + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		if ( cursor.moveToFirst() )
			{
			RateArrayAdapter rtermAdapter = new RateArrayAdapter(mContext, new ArrayList<RateData>(radapter.getItemList()) );
			RateData data2 = new RateData();
			data2.equipmentCategoryId	= cursor.getString(0);
			data2.categoryDesc		= mechanic2;
			data2.refDept	= false;
			rtermAdapter.add(data2);
			sp2.setAdapter(rtermAdapter);
			/*
			//ArrayAdapter<CharSequence> adapterTerm = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
			//int size = adapter.getCount();
			//for( int i = 0; i < size; i++ )
			//	{
			//	adapterTerm.add(adapter.getItem(i));
			//	}
			
			// Add the mechanic name and id to the adapters
			mirrorAdapterTerm.add( mechanic );
			adapterTerm.add( Twix_TextFunctions.clean(cursor.getString(0)) );
			
			sp.setAdapter(adapterTerm);
			sp.setTag(mirrorAdapterTerm);
			*/
	/*		}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	*/
	private void includeTermAdapter(String mechanic, Spinner sp)
		{
		String sqlQ = "select mechanic.mechanic_name " +
				"from mechanic " +
				"WHERE mechanic.mechanic == '" + mechanic + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		if ( cursor.moveToFirst() )
			{
			MechanicArrayAdapter termAdapter = new MechanicArrayAdapter(mContext, new ArrayList<MechanicData>(adapter.getItemList()) );
			MechanicData data = new MechanicData();
			data.mechanic	= cursor.getString(0);
			data.name		= mechanic;
			data.refDept	= false;
			termAdapter.add(data);
			sp.setAdapter(termAdapter);
			/*
			//ArrayAdapter<CharSequence> adapterTerm = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
			//int size = adapter.getCount();
			//for( int i = 0; i < size; i++ )
			//	{
			//	adapterTerm.add(adapter.getItem(i));
			//	}
			
			// Add the mechanic name and id to the adapters
			mirrorAdapterTerm.add( mechanic );
			adapterTerm.add( Twix_TextFunctions.clean(cursor.getString(0)) );
			
			sp.setAdapter(adapterTerm);
			sp.setTag(mirrorAdapterTerm);
			*/
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
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
	
	private void createWatchers()
		{
		tw_Hrs	= generateTextLimit(8);
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
	
	private class ServiceLaborData
		{
		String serviceDate = "";
		float regHours = 0f;
		float thHours = 0f;
		float dtHours = 0f;
		String rate;
		String mechanic;
		}
	
	private void readSQL()
		{
		String sqlQ = "SELECT substr(serviceLabor.serviceDate, 1, 10 ), serviceLabor.regHours, " +
				"serviceLabor.thHours, serviceLabor.dtHours,serviceLabor.mechanic,serviceLabor.rate " +
				"FROM serviceLabor " +
			"WHERE serviceLabor.serviceTagUnitId = " + unitAct.serviceTagUnitId + " " + 
			"ORDER BY serviceLabor.serviceLaborId desc";
		Cursor cursor = db.rawQuery(sqlQ);
		
		ServiceLaborData data;
		int index;
		if ( cursor.moveToFirst() )
			{
			do
				{
				index = 0;
				data = new ServiceLaborData();
				data.serviceDate = cursor.getString(0);
				data.regHours	 = cursor.getFloat(1);
				data.thHours	 = cursor.getFloat(2);
				data.dtHours	 = cursor.getFloat(3);
				data.mechanic	 = cursor.getString(4);
				data.rate		 = cursor.getString(5);
				Toast.makeText(app.getBaseContext(),(String)data.rate, 
		                Toast.LENGTH_SHORT).show();
				addLabor(data);
				rowCount++;
				}
			while (cursor.moveToNext());
			}
		else
			addLabor(null);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		}
	
	public class ServiceLaborRow
		{
		TextView serviceDate;
		EditText regHours;
		EditText thHours;
		EditText dtHours;
		Spinner mechanic;
		Spinner rate;
		public static final int REG_HOURS = 0;
		public static final int TH_HOURS = 1;
		public static final int DT_HOURS = 2;
		
		public boolean isEmpty()
			{
			return !(serviceDate.length() > 0 ||
					regHours.length() > 0 ||
					thHours.length() > 0 ||
					dtHours.length() > 0 );
					//|| mechanic.getSelectedItemPosition() > 0);
					// Added convenience for auto generated labor records. This way these are considered empty
			}
		
		public float getHours(int which)
			{
			EditText et = null;
			switch(which)
				{
				case REG_HOURS:
					et = regHours;
					break;
				case TH_HOURS:
					et = thHours;
					break;
				case DT_HOURS:
					et = dtHours;
					break;
				
				}
			
			if( et == null || et.getText().length() <= 0 )
				return 0f;
			
			float f;
			try
				{
				f = Float.parseFloat(et.getText().toString());
				}
			catch(NumberFormatException e)
				{
				Log.e("twix_agent", "Failed to convert hours to float. ");
				f = 0f;
				}
					
			return f;
			}
		
		public String getMechanic()
			{
			MechanicData data = (MechanicData) mechanic.getSelectedItem();
			return data.mechanic;
			/*
			@SuppressWarnings("unchecked")
			ArrayList<String> mirrorTerm = (ArrayList<String>) mechanic.getTag();
			if( mirrorTerm == null )
				return mirrorAdapter.get(mechanic.getSelectedItemPosition());
			else
				return mirrorTerm.get(mechanic.getSelectedItemPosition());
			*/
			}
		public String getRate()
			{
			RateData datar = (RateData) rate.getSelectedItem();
			return datar.rate;
			/*
			@SuppressWarnings("unchecked")
			ArrayList<String> mirrorTerm = (ArrayList<String>) mechanic.getTag();
			if( mirrorTerm == null )
				return mirrorAdapter.get(mechanic.getSelectedItemPosition());
			else
				return mirrorTerm.get(mechanic.getSelectedItemPosition());
			*/
			}
		private void resetBgs()
			{
			unMark(serviceDate);
			unMark(regHours);
			unMark(thHours);
			unMark(dtHours);
			unMark(mechanic);
			unMark(rate);
			}
		
		public boolean valid()
			{
			if( isEmpty() )
				return true;
			
			boolean ret = true;
			resetBgs();
			if( !(serviceDate.length() > 0) )
				{
				ret = false;
				mark( serviceDate );
				}
			if( !(rate.getSelectedItemPosition() > 0) )
				{
				ret = false;
				mark(rate );
				}
			
			
			if( !regHours.getText().toString().matches(hoursRegx) )
				{
				ret = false;
				mark( regHours );
				}
			if( !thHours.getText().toString().matches(hoursRegx) )
				{
				ret = false;
				mark( thHours );
				}
			if( !dtHours.getText().toString().matches(hoursRegx) )
				{
				ret = false;
				mark( dtHours );
				}
			if( !(mechanic.getSelectedItemPosition() > 0) )
				{
				ret = false;
				mark( mechanic );
				}
			
			return ret;
			}
		}
	
	public void addLabor( ServiceLaborData data )
		{
		if( data == null )
			data = new ServiceLaborData();
		
		EditText et,startBox,startbox2,et1;
		TextView tv,tv1;
		RelativeLayout sp,sp2;
		
		LinearLayout row = createRow();
		ServiceLaborRow rowTag = new ServiceLaborRow();
		row.setTag(rowTag);
		
		// Add the minus button to the row
		LayoutParams paramsBn = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		  	
		LayoutParams paramsr = new LayoutParams(
				130,50);
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
	            	unitAct.dirtyFlag = true;
	            	LinearLayout row = (LinearLayout) v.getParent();
	            	LinearLayout tl = (LinearLayout) row.getParent();
	            	LinearLayout tl2 = (LinearLayout) row.getParent();
	            	tl.removeView(row);
	            	tl2.removeView(row);
	            	//removeRow((TableRow)v.getParent());
	                }
	        	});
	    	row.addView(bn);
    		}
    	
    	
    	
		
		
		// Service Date of Labor
		tv = createTextView();
		tv.setText( data.serviceDate );
		tv.addTextChangedListener(unitAct.setDirtyFlag);
		rowTag.serviceDate = tv;
		row.addView(tv);
		
		
		
		
		// Regular Hours
		startBox = createEditBox(data.regHours+"", 35, hrs);
		startBox.setHint("0.0");
		startBox.setEnabled(!unitAct.tag.tagReadOnly);
		startBox.addTextChangedListener(tw_Hrs);
		rowTag.regHours = startBox;
		row.addView(startBox);
		
		// Time and a half Hours
		et = createEditBox(data.thHours+"", 12, hrs);
		et.setHint("0.0");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.addTextChangedListener(tw_Hrs);
		rowTag.thHours = et;
		row.addView(et);
		
		// Double Time Hours
		et = createEditBox(data.dtHours+"", 14, hrs);
		et.setHint("0.0");
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.addTextChangedListener(tw_Hrs);
		rowTag.dtHours = et;
		row.addView(et);
		
		//Rate
		/*
				et1 = createEditBox1(paramsr, 2, rt);
				et1.setHint(" ");
				et1.setEnabled(!unitAct.tag.tagReadOnly);
				et1.setText(data.rate);
				et1.setFocusable(false);
				
				et1.addTextChangedListener(unitAct.setDirtyFlag);
			//	et1.addTextChangedListener(tw_rt);
				rowTag.rate = et1;
				row.addView(et1);
				 et1.setOnClickListener(onclicklistener);
*/
		sp2 = createSpinner2(!unitAct.tag.tagReadOnly);
		int size2 = radapter.size(), index2 = 0;
		Spinner spinner2 = (Spinner)sp2.getChildAt(0);
		String s2;
		if( data.rate != null)
			s2 = data.rate;
		else
			s2 = app.empno;
		
		for( int i = 0; i < size2; i++ )
			{
			if( radapter.getItem(i).rate.contentEquals(s2) )
				{
				index2 = i;
				break;
				}
			}
		
		if( (s2.length() > 0) && (index2 == 0) )
			{
			//includeTermAdapter2(s2, spinner2);
			index2 = (spinner2).getAdapter().getCount()-1;
			}
		
		spinner2.setSelection(index2);
		rowTag.rate = spinner2;
		row.addView(sp2);
		
		
		
	
		
		
		
				
		// Mechanic Selector
		sp = createSpinner(!unitAct.tag.tagReadOnly);
		int size = adapter.size(), index = 0;
		Spinner spinner = (Spinner)sp.getChildAt(0);
		String s;
		if( data.mechanic != null)
			s = data.mechanic;
		else
			s = app.empno;
		
		for( int i = 0; i < size; i++ )
			{
			if( adapter.getItem(i).mechanic.contentEquals(s) )
				{
				index = i;
				break;
				}
			}
		
		if( (s.length() > 0) && (index == 0) )
			{
			includeTermAdapter(s, spinner);
			index = (spinner).getAdapter().getCount()-1;
			}
		
		spinner.setSelection(index);
		rowTag.mechanic = spinner;
		row.addView(sp);
	
		
		
		tl2.addView(row);
		
		ScrollView sv2 = (ScrollView) findViewById(R.id.ScrollView02);
		sv2.fullScroll(ScrollView.FOCUS_DOWN);
		startBox.requestFocus();
		//tl.addView(row);
		
		//ScrollView sv = (ScrollView) findViewById(R.id.ScrollView01);
		//sv.fullScroll(ScrollView.FOCUS_DOWN);
		//startBox.requestFocus();
		}
	
	
	public boolean validateSave ()
		{
		boolean ret = true;
		ServiceLaborRow row;
		
		int size = tl.getChildCount();
		for( int i = 0; i < size; i++ )
			{
			row = (ServiceLaborRow) tl.getChildAt(i).getTag();
			if( !row.valid() )
				ret = false;
			}
		
		return ret;
		}
	
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
switch (requestCode) {
case 1:
    if (resultCode == RESULT_OK) {
    curRateName = data.getStringExtra("GetFileName");
      Toast.makeText(getApplicationContext(), "text is " + curRateName , Toast.LENGTH_LONG).show();

        }
        break;
    }
}




	
	public void updateDB()
		{
		db.delete("serviceLabor", "serviceTagUnitId", unitAct.serviceTagUnitId);
		
		ContentValues cv = new ContentValues();
		
		ServiceLaborRow row;
		int size = tl2.getChildCount();
		for( int i = 0; i < size; i++ )
			{
			row = (ServiceLaborRow) tl2.getChildAt(i).getTag();
			
			if( !row.isEmpty() )
				{
				Toast.makeText(app.getBaseContext(),"rate = " + row.rate, 
		                Toast.LENGTH_SHORT).show();
				cv.put("serviceLaborId",	db.newNegativeId("serviceLabor", "serviceLaborId"));
				cv.put("serviceTagUnitId",	unitAct.serviceTagUnitId);
				cv.put("serviceDate",		row.serviceDate.getText().toString());
				cv.put("regHours",			row.getHours(ServiceLaborRow.REG_HOURS) );
				cv.put("thHours",			row.getHours(ServiceLaborRow.TH_HOURS) );
				cv.put("dtHours",			row.getHours(ServiceLaborRow.DT_HOURS) );
				cv.put("mechanic",			row.getMechanic() );
				cv.put("rate", 				row.getRate() );
				db.db.insertOrThrow("serviceLabor", null, cv);
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
	
	private LinearLayout createRow()
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		LinearLayout ret = new LinearLayout(mContext);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.HORIZONTAL);
		
		return ret;
		}
	
	private EditText createEditBox(String text, int size, KeyListener key)
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params.weight = 1.2f;
		params.setMargins(2, 2, 2, 2);
		
		EditText et = new EditText(this);
		
		et.setLayoutParams(params);
		et.setTextColor(Twix_Theme.sub1Value);
		et.setBackgroundResource(R.drawable.editbox);
		et.setTextSize(Twix_Theme.subSize);
		et.setPadding(5, 5, 5, 5);
		if( text != null && !text.contentEquals("0.0"))
			et.setText(text);
		if( key != null )
			et.setKeyListener(key);
		et.setSingleLine();
		//et.setTypeface(Typeface.MONOSPACE);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		
		return et;
		}
	private EditText createEditBox1(LayoutParams params, int size, KeyListener key)
		{
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
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
	private TextView createTextView()
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params.weight = 1.3f;
		params.setMargins(2, 2, 2, 2);
		
		TextView tv = new TextView(this);
		
		tv.setLayoutParams(params);
		tv.setTextColor(Twix_Theme.sub1Value);
		tv.setBackgroundColor(Twix_Theme.editBG);
		tv.setTextSize(Twix_Theme.subSize);
		tv.setPadding(5, 5, 5, 5);
		tv.setSingleLine();
		if( !unitAct.tag.tagReadOnly )
			tv.setOnClickListener(new View.OnClickListener()
				{
				public void onClick(View v)
					{
					((Twix_AgentActivityGroup2)mContext).changeDate(v);
					}
				});
		
		return tv;
		}
	
	
	private RelativeLayout createSpinner(boolean editable)
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params.weight = 4.2f;
		params.setMargins(2, 2, 2, 2);
		
		RelativeLayout rl = new RelativeLayout(this);
		rl.setLayoutParams(params);
		
		RelativeLayout.LayoutParams paramsSp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
		
		RelativeLayout.LayoutParams paramsIv = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT );
		paramsIv.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		paramsIv.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsIv.setMargins(5, 5, 5, 5);
		
		Spinner sp = new Spinner(mContext);
		sp.setLayoutParams(paramsSp);
		sp.setBackgroundColor(Twix_Theme.editBG);
		sp.setAdapter(adapter);
		sp.setEnabled(editable);
		sp.setOnItemSelectedListener(dirtySelect);
		
		ImageView iv = new ImageView(this);
		iv.setLayoutParams(paramsIv);
		iv.setImageResource(R.drawable.drop_arrow);
		
		rl.addView(sp);
		rl.addView(iv);
		
		return rl;
		}
	private RelativeLayout createSpinner2(boolean editable)
		{
		LinearLayout.LayoutParams paramsr = new LinearLayout.LayoutParams(
				0,80);
		paramsr.weight = 2.3f;
		paramsr.setMargins(2, 2, 2, 2);
		
		RelativeLayout r2 = new RelativeLayout(this);
		r2.setLayoutParams(paramsr);
		
		RelativeLayout.LayoutParams paramsSpr = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
		
		RelativeLayout.LayoutParams paramsIvr = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT );
		paramsIvr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		paramsIvr.addRule(RelativeLayout.CENTER_VERTICAL);
		paramsIvr.setMargins(5, 5, 5, 5);
		
		Spinner spr = new Spinner(mContext);
		spr.setLayoutParams(paramsSpr);
		spr.setBackgroundColor(Twix_Theme.editBG);
		spr.setAdapter(radapter);
		spr.setEnabled(editable);
		spr.setOnItemSelectedListener(dirtySelect);
		
		ImageView iv2 = new ImageView(this);
		iv2.setLayoutParams(paramsIvr);
		iv2.setImageResource(R.drawable.drop_arrow);
		
		r2.addView(spr);
		r2.addView(iv2);
		
		return r2;
		}
	private void createKeyListeners()
		{
		hrs = new NumberKeyListener()
			{
		    public int getInputType()
		    	{
			    return InputType.TYPE_CLASS_PHONE;
			    }
		
		    @Override
		    protected char[] getAcceptedChars()
		    	{
			    return new char[] 
			    	{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' };
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
	
	//*************************
	// Custom Mechanic Adapter
	//*************************
	
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

		@Override
		public MechanicData getItem(int position)
			{
			return items.get(position);
			}
		
		// Views displayed AFTER selecting
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
			{
			TextView tv = (TextView) convertView;
			MechanicData data = items.get(position);
			
			if (tv == null)
				{
				//tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tv = new TextView(mContext);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(Twix_Theme.headerSize);
				}
			
			tv.setTextColor(Twix_Theme.headerValue);
			
			tv.setText(data.name);
			if( data.refDept )
				{
				//tv.setBackgroundColor(Twix_Theme.editBG);
				parent.setBackgroundColor(Twix_Theme.editBG);
				}
			else
				{
				//tv.setBackgroundColor(Twix_Theme.disabledColorBG);
				parent.setBackgroundColor(Twix_Theme.disabledColorBG);
				}
			return tv;
			}
		
		public int size()
			{
			return items.size();
			}
		
		public List<MechanicData> getItemList()
			{
			return items;
			}
		
		public Context getContext()
			{
			return this.mContext;
			}
		
		}
	public class RateArrayAdapter extends ArrayAdapter<RateData>
		{
		private List<RateData>	items;
		private Context			mContext;
		private LinearLayout.LayoutParams layoutParams;
		
		public RateArrayAdapter(Context c, List<RateData> items)
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
			RateData data = items.get(position);
			TextView tvr = (TextView) super.getView(position, convertView, parent);
			
			// If the layout inflater fails/DropDown not set
			if (tvr == null)
				{
				tvr = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				//tv = new TextView(mContext);
				tvr.setLayoutParams(layoutParams);
				tvr.setTextSize(Twix_Theme.headerSize);
				}
			
			tvr.setTextColor(Twix_Theme.headerValue);
			tvr.setText(data.rate + " - " +data.ratedesc);
			if( data.refDept )
				tvr.setBackgroundColor(Twix_Theme.editBG);
			else
				tvr.setBackgroundColor(Twix_Theme.disabledColorBG);
			
			return tvr;
			}

		@Override
		public RateData getItem(int position)
			{
			return items.get(position);
			}
		
		// Views displayed AFTER selecting
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
			{
			TextView tvr1 = (TextView) convertView;
			RateData data = items.get(position);
			
			if (tvr1 == null)
				{
				//tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tvr1 = new TextView(mContext);
				tvr1.setLayoutParams(layoutParams);
				tvr1.setTextSize(Twix_Theme.headerSize);
				}
			
			tvr1.setTextColor(Twix_Theme.headerValue);
			
			tvr1.setText(data.rate);
			if( data.refDept )
				{
				//tv.setBackgroundColor(Twix_Theme.editBG);
				parent.setBackgroundColor(Twix_Theme.editBG);
				}
			else
				{
				//tv.setBackgroundColor(Twix_Theme.disabledColorBG);
				parent.setBackgroundColor(Twix_Theme.disabledColorBG);
				}
			return tvr1;
			}
		
		public int size()
			{
			return items.size();
			}
		
		public List<RateData> getItemList()
			{
			return items;
			}
		
		public Context getContext()
			{
			return this.mContext;
			}
		
		}
	private class MechanicData
		{
		String mechanic;
		String name;
		boolean refDept;
		}
	private class RateData
	{
	String rate;
	String ratedesc;
	boolean refDept;
	}
	}
