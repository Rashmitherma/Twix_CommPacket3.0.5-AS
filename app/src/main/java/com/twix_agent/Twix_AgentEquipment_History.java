package com.twix_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentEquipment_History
 * 
 * Purpose: Provides a dynamic way to build an equipment history. It requires a context to build from, the
 * 			application pointer, and an equipment id. On creation, it becomes a popup containing all the
 * 			known equipment history.
 * 
 * Note:	On close, the class destroys all external links, allowing it to be cleaned up.
 * 
 * 
 * Relevant XML: equipment_history.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentEquipment_History
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private int equipmentId;
	
	private LinearLayout ll, llScroll;
	private ScrollView scroll;
	
	public Twix_AgentEquipment_History(Twix_Application a, Context c, int eqId)
		{
		app = a;
		mContext = c;
		Twix_Theme = app.Twix_Theme;
		equipmentId = eqId;
		
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.equipment_history, null);
		ll = (LinearLayout) viewToLoad.findViewById(R.id.ServiceUnitBuild);
		//readServiceUnit();
		readUnitHeaders();
		setupScroll();
		readUnitDetails();
		
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		((Button)viewToLoad.findViewById(R.id.Close)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				dialog.dismiss();
				app = null;
				mContext = null;
				Twix_Theme = null;
				}
			});
		
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		
		dialog.setContentView(viewToLoad, params);
		dialog.show();
		
		scroll.invalidate();
		}
	
	private void setupScroll()
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 10, 0, 0);
		scroll = new ScrollView(mContext);
		scroll.setLayoutParams(params);
		scroll.setFillViewport(true);
		
		FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		llScroll = new LinearLayout(mContext);
		llScroll.setLayoutParams(params2);
		llScroll.setOrientation(LinearLayout.VERTICAL);
		
		scroll.addView(llScroll);
		ll.addView(scroll);
		}
	
	private boolean readUnitHeaders()
		{
		/**
		 * Unit Headers Structure
		 * 
		 * 0 - Unit Category + UnitNo
		 * 1 - Manufacturer
		 * 2 - Serial No
		 * 3 - Model
		 * 
		 */
		boolean ret = false;
		String sqlQ = "SELECT " +
				"equipmentCategory.categoryDesc || ' - ' || equipment.UnitNo, " +
				"equipment.manufacturer, equipment.serialNo, " +
				"equipment.Model " +
			"FROM serviceTagUnit " +
				"LEFT OUTER JOIN equipment on equipment.equipmentId = serviceTagUnit.equipmentId " +
				"LEFT OUTER JOIN equipmentCategory on equipmentCategory.equipmentCategoryId = equipment.equipmentCategoryId " +
			"WHERE serviceTagUnit.equipmentId = " + equipmentId;
		List<String> listUnit = new ArrayList<String>();
		Cursor cursor = app.db.rawQuery(sqlQ);
		int size = cursor.getColumnCount();
		String s = "";
		
		if (cursor.moveToFirst())
			{
			ret = true;
			for( int i = 0; i < size; i++)
				{
				s = Twix_TextFunctions.clean( cursor.getString(i) );
				if( i == 1)
					{
					if( s.contentEquals("") || s.contentEquals("0") )
						s = "No Category";
					}
				listUnit.add(s);
				}
			
			buildServiceUnitHeader(listUnit);
			listUnit.clear();
			}
		else
			{
			ll.addView( createTextView( "No Service Records Available", Twix_Theme.headerText, new TableRow.LayoutParams() ) );
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private void buildUnitDetailsLL(ServiceUnitData data)
		{
    	LinearLayout tl = createTable(3);
		tl.setBackgroundColor(Twix_Theme.tableBG);
		
		LinearLayout row = createRow();
		
		row.addView( createTextView( "Tag No: ", 1f, false, true, false) );
		row.addView( createTextView( data.serviceTagId+"", 2f, true, true, false) );
		row.addView( createTextView( "Date: ", 1f, false, true, false) );
		row.addView( createTextView( data.serviceDate, 2f, true, true, false) );
		tl.addView(row);
		
		row = createRow();
		row.addView( createTextView( "Mechanic: ", 1f, false, false, false) );
		row.addView( createTextView( data.mechanic, 3f, true, false, false) );
		tl.addView(row);
		
		row = createRow();
		row.addView( createTextView( "Service Performed: ", 1f, false, false, false) );
		row.addView( createTextView( "Comments: ", 1f, false, false, false) );
		tl.addView(row);
		
		row = createRow();
		row.addView( createTextView( data.servicePerformed, 1f, true, false, false) );
		row.addView( createTextView( data.comments, 1f, true, false, false) );
		tl.addView(row);
		
		LinearLayout details = laborTable(data.serviceLabor);
		if( details != null )
			tl.addView( details );
		
		details = materialTable(data.serviceMaterial);
		if( details != null )
			tl.addView( details );
		
		llScroll.addView(tl);
		}
	
	private void buildBlueDetailsLL(BlueUnitData data)
		{
    	LinearLayout tl = createTable(3);
		tl.setBackgroundColor(Twix_Theme.tableBGAlt);
		
		LinearLayout row = createRow();
		
		row.addView( createTextView( "Tag No: ", 1f, false, true, true) );
		row.addView( createTextView( data.serviceTagId+"", 2f, true, true, true) );
		row.addView( createTextView( "Date: ", 1f, false, true, true) );
		row.addView( createTextView( data.dateCreated, 2f, true, true, true) );
		tl.addView(row);
		
		if( data.description != null  && data.description.length() > 0 )
			{
			row = createRow();
			row.addView( createTextView( "Repair Description: ", 1f, false, false, true) );
			tl.addView(row);
			row = createRow(10);
			row.addView( createTextView( data.description, 1f, true, false, true) );
			tl.addView(row);
			}
		
		if( data.materials != null  && data.materials.length() > 0 )
			{
			row = createRow();
			row.addView( createTextView( "Materials: ", 1f, false, false, true) );
			tl.addView(row);
			row = createRow(10);
			row.addView( createTextView( data.materials, 1f, true, false, true) );
			tl.addView(row);
			}
		
		if( data.notes != null  && data.notes.length() > 0 )
			{
			row = createRow();
			row.addView( createTextView( "Notes: ", 1f, false, false, true) );
			tl.addView(row);
			row = createRow(20);
			row.addView( createTextView( data.notes, 1f, true, false, true) );
			tl.addView(row);
			}
		
		row = createRow();
		row.addView( createTextView( "Estimated Labor: ", 1f, false, false, true) );
		row.addView( createTextView( data.laborHours+"", 1f, true, false, true) );
		row.addView( createTextView( "Cost: ", 1f, false, false, true) );
		row.addView( createTextView( data.cost+"", 1f, true, false, true) );
		tl.addView(row);
		
		llScroll.addView(tl);
		}
	
	private LinearLayout createTable(Integer... args)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	if( args.length > 0 )
    		params.setMargins(args[0], 5, args[0], 5);
    	
     	LinearLayout row = new LinearLayout(mContext);
     	row.setOrientation(LinearLayout.VERTICAL);
     	row.setLayoutParams(params);
     	
     	return row;
    	}
	
	private LinearLayout createRow(Integer... args)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	if( args.length > 0 )
    		params.setMargins(args[0], 0, args[0], 0);
    	
     	LinearLayout row = new LinearLayout(mContext);
     	row.setOrientation(LinearLayout.HORIZONTAL);
     	row.setLayoutParams(params);
     	
     	return row;
    	}
    
    private TextView createTextView(String text, float weight, boolean value, boolean header, boolean alt)
    	{
    	LinearLayout.LayoutParams params =
    			new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
    	//params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	if( value )
    		tv.setTextColor(Twix_Theme.headerValue);
    	else
    		tv.setTextColor(Twix_Theme.headerText);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setPadding(4, 4, 4, 4);
		if( header && !alt )
			tv.setBackgroundColor(Twix_Theme.headerBG);
		else if( header && alt)
			tv.setBackgroundColor(Twix_Theme.headerBGAlt);
		
		
    	return tv;
    	}
	
	private class ServiceUnitData
		{
		int serviceTagId;
		int serviceTagUnitId;
		String serviceDate;
		String dbDate;
		String mechanic;
		String servicePerformed;
		String comments;
		
		List<ServiceLaborData> serviceLabor;
		List<ServiceMaterialData> serviceMaterial;
		}
	
	private class BlueUnitData
		{
		int serviceTagId;
		String dateCreated;
		String dbDate;
		String description;
		String materials;
		String notes;
		float cost;
		float laborHours;
		}
	
	// Reads all the Service Unit data
	private void readUnitDetails()
		{
		// Fetch the Service Unit Data
		String sqlQ = "SELECT serviceTag.serviceTagId, serviceTagUnit.serviceTagUnitId, " +
				"serviceTag.serviceDate, mechanic.mechanic_name, serviceTagUnit.servicePerformed, serviceTagUnit.comments " +
			"FROM serviceTag " +
				"LEFT OUTER JOIN serviceTagUnit on serviceTagUnit.serviceTagId = serviceTag.serviceTagId " +
				"LEFT OUTER JOIN mechanic on mechanic.mechanic = serviceTag.empno " +
			"WHERE serviceTagUnit.equipmentId = " + equipmentId;
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		List<Object> units_blues = new ArrayList<Object>();
		ServiceUnitData data;
		int index;
		while (cursor.moveToNext())
			{
			index = 0;
			data = new ServiceUnitData();
			data.serviceTagId		= cursor.getInt(0);
			data.serviceTagUnitId	= cursor.getInt(1);
			data.dbDate				= Twix_TextFunctions.clean(cursor.getString(2));
			data.serviceDate		= Twix_TextFunctions.DBtoNormal(data.dbDate);
			data.mechanic			= cursor.getString(3);
			data.servicePerformed	= cursor.getString(4);
			data.comments			= cursor.getString(5);
			
			data.serviceLabor = getLabor( data.serviceTagUnitId );
			data.serviceMaterial = getMaterials( data.serviceTagUnitId );
			
			units_blues.add(data);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// Fetch the Blue Unit Data
		sqlQ = "SELECT b.serviceTagId, b.dateCreated, bu.description, " +
				"bu.materials, bu.notes, bu.laborHours, bu.cost " +
			"FROM closedblueUnit as bu " +
				"LEFT OUTER JOIN closedblue as b on b.blueId = bu.blueId " +
			"WHERE bu.equipmentId = " + equipmentId;
		
		cursor = app.db.rawQuery(sqlQ);
		
		BlueUnitData bdata;
		while (cursor.moveToNext())
			{
			index = 0;
			bdata = new BlueUnitData();
			bdata.serviceTagId		= cursor.getInt(0);
			bdata.dbDate			= Twix_TextFunctions.clean(cursor.getString(1));
			bdata.dateCreated		= Twix_TextFunctions.DBtoNormal(bdata.dbDate);
			bdata.description		= cursor.getString(2);
			bdata.materials			= cursor.getString(3);
			bdata.notes				= cursor.getString(4);
			bdata.laborHours		= cursor.getFloat(5);
			bdata.cost				= cursor.getFloat(6);
			
			units_blues.add(bdata);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		
		int size = units_blues.size();
		if( size > 0 )
			{
			// Sort the Results by date
			Collections.sort(units_blues, new UnitBlueSorter(true));
			
			Object o;
			
			for( int i = 0; i < size; i++ )
				{
				o = units_blues.get(i);
				if( o instanceof ServiceUnitData )
					buildUnitDetailsLL( (ServiceUnitData)o );
				else if( o instanceof BlueUnitData )
					buildBlueDetailsLL( (BlueUnitData)o );
				}
			}
		else
			{
			TextView tv = createTextView( "No Service Records Available", 0f, false, true, false );
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tv.getLayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.WRAP_CONTENT;
			ll.addView( tv );
			}
		
		}
	
	private class ServiceLaborData
		{
		String serviceDate;
		float regHours;
		float thHours;
		float dtHours;
		String mechanic;
		}
	
	private List<ServiceLaborData> getLabor( int serviceTagUnitId )
		{
		List<ServiceLaborData> ret = new ArrayList<ServiceLaborData>();
		String sqlQ = "SELECT serviceLabor.serviceDate, serviceLabor.regHours, " +
					"serviceLabor.thHours, serviceLabor.dtHours, mechanic.mechanic_name " +
				"FROM serviceLabor " +
					"LEFT OUTER JOIN mechanic on mechanic.mechanic = serviceLabor.mechanic " +
				"WHERE serviceLabor.serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		ServiceLaborData data;
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				data = new ServiceLaborData();
				data.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(0));
				data.regHours		= cursor.getFloat(1);
				data.thHours		= cursor.getFloat(2);
				data.dtHours		= cursor.getFloat(3);
				data.mechanic		= cursor.getString(4);
				
				ret.add(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}

	private class ServiceMaterialData
		{
		float quantity;
		String materialDesc;
		}
	
	private List<ServiceMaterialData> getMaterials( int serviceTagUnitId )
		{
		List<ServiceMaterialData> ret = new ArrayList<ServiceMaterialData>();
		String sqlQ = "SELECT serviceMaterial.quantity, serviceMaterial.materialDesc " +
				"FROM serviceMaterial " +
				"WHERE serviceMaterial.serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		ServiceMaterialData data;
		int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				data = new ServiceMaterialData();
				data.quantity		= cursor.getFloat(0);
				data.materialDesc	= cursor.getString(1);
				
				ret.add(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}

	
	private void buildServiceUnitHeader(List<String> list)
		{
		/**
		 * Unit Headers Structure
		 * 
		 * 0 - Unit Category + UnitNo
		 * 1 - Manufacturer
		 * 2 - Serial No
		 * 3 - Model
		 * 
		 */
		LinearLayout.LayoutParams paramsTL = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		paramsTL.setMargins(3, 3, 3, 3);
		
    	TableLayout tl = new TableLayout(mContext);
    	tl.setLayoutParams(paramsTL);
		tl.setBackgroundColor(Twix_Theme.tableBG);
		
		TableRow row = new TableRow(mContext);
		
		LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		row.setLayoutParams(paramsRow);
		
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.height = LayoutParams.WRAP_CONTENT;
		params.weight = 1;
		
		TableRow.LayoutParams paramsL = new TableRow.LayoutParams();
		paramsL.height = LayoutParams.WRAP_CONTENT;
		paramsL.weight = 1;
		paramsL.setMargins(25, 0, 0, 0);
		
		TableRow.LayoutParams paramsR = new TableRow.LayoutParams();
		paramsR.height = LayoutParams.WRAP_CONTENT;
		paramsR.weight = 1;
		paramsR.setMargins(0, 0, 25, 0);
		
		TableRow.LayoutParams params2WeightL = new TableRow.LayoutParams();
		params2WeightL.height = LayoutParams.WRAP_CONTENT;
		params2WeightL.weight = 2;
		params2WeightL.setMargins(25, 0, 0, 0);
		
		TableRow.LayoutParams params2WeightR = new TableRow.LayoutParams();
		params2WeightR.height = LayoutParams.WRAP_CONTENT;
		params2WeightR.weight = 2;
		params2WeightR.setMargins(0, 0, 25, 0);
		
		int[] bg = { Twix_Theme.headerBG };
		
		row.addView( createTextView( "Equipment: ", Twix_Theme.headerText, params, bg) );
		row.addView( createTextView( list.get(0), Twix_Theme.headerValue, params, bg) );
		row.addView( createTextView( "Manufactuer: ", Twix_Theme.headerText, params, bg) );
		row.addView( createTextView( list.get(1), Twix_Theme.headerValue, params, bg) );
		
		tl.addView(row);
		row = new TableRow(mContext);
		
		row.addView( createTextView( "Serial No: ", Twix_Theme.headerText, paramsL) );
		row.addView( createTextView( list.get(2), Twix_Theme.headerValue, params) );
		row.addView( createTextView( "Model: ", Twix_Theme.headerText, params) );
		row.addView( createTextView( list.get(3), Twix_Theme.headerValue, paramsR) );
		
		tl.addView(row);
		
		ll.addView(tl);
		}
	
	private LinearLayout laborTable(List<ServiceLaborData> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		
		LinearLayout tl = createTable(10);
		tl.setBackgroundColor(Twix_Theme.tableBG2);
		
		LinearLayout row = createRow();
		row.addView( createTextView( "Date", 1f, false, true, false) );
		row.addView( createTextView( "Reg Hrs", 1f, false, true, false) );
		row.addView( createTextView( "TH Hrs", 1f, false, true, false) );
		row.addView( createTextView( "DT Hrs", 1f, false, true, false) );
		row.addView( createTextView( "Mechanic", 3f, false, true, false) );
		tl.addView(row);
		
		ServiceLaborData data;
		for( int i = 0; i < size; i++ )
			{
			data = list.get(i);
			row = createRow();
			row.addView( createTextView( data.serviceDate, 1f, true, false, false) );
			row.addView( createTextView( data.regHours+"", 1f, true, false, false) );
			row.addView( createTextView( data.thHours+"", 1f, true, false, false) );
			row.addView( createTextView( data.dtHours+"", 1f, true, false, false) );
			row.addView( createTextView( data.mechanic, 3f, true, false, false) );
			tl.addView(row);
			}
		
		return tl;
		}
	
	private LinearLayout materialTable(List<ServiceMaterialData> list)
		{
		int size = list.size();
		if( size < 1 )
			return null;
		
		LinearLayout tl = createTable(10);
		tl.setBackgroundColor(app.Twix_Theme.tableBG2);
		
		LinearLayout row = createRow();
		row.addView( createTextView( "Material Quantity", 1f, false, true, false) );
		row.addView( createTextView( "Material Description", 2f, false, true, false) );
		tl.addView(row);
		
		ServiceMaterialData data;
		for( int i = 0; i < size; i++ )
			{
			data = list.get(i);
			row = createRow();
			row.addView( createTextView( data.quantity+"", 1f, true, false, false) );
			row.addView( createTextView( data.materialDesc, 2f, true, false, false) );
			tl.addView(row);
			}
		
		return tl;
		}
	private TextView createTextView(String s, int color, LinearLayout.LayoutParams params, int... bg )
		{
		TextView tv = new TextView(mContext);
		
		tv.setLayoutParams(params);
		tv.setTextSize(app.Twix_Theme.headerSize);
		tv.setText(s);
		tv.setTextColor(color);
		tv.setPadding(5, 5, 5, 5);
		if( bg.length > 0 )
			{
			tv.setBackgroundColor(bg[0]);
			}
		
		return tv;
		}
	
	// Used to sort the two results
	private class UnitBlueSorter implements Comparator<Object>
    	{
    	boolean desc = false;
    	UnitBlueSorter( boolean dir )
	    	{
	    	desc = dir;
	    	}
		@Override
		public int compare(Object v1, Object v2)
			{
			String date1;
			String date2;
			
			if( v1 instanceof ServiceUnitData )
				date1 = ((ServiceUnitData)v1).dbDate;
			else if( v1 instanceof BlueUnitData )
				date1 = ((BlueUnitData)v1).dbDate;
			else
				return 0;
			
			if( v2 instanceof ServiceUnitData )
				date2 = ((ServiceUnitData)v2).dbDate;
			else if( v2 instanceof BlueUnitData )
				date2 = ((BlueUnitData)v2).dbDate;
			else
				return 0;
			
			if( desc )
				return -date1.compareTo(date2);
			return date1.compareTo(date2);
			}
	    }
	}
