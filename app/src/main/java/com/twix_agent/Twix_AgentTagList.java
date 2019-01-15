package com.twix_agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.database.Cursor;
import android.widget.LinearLayout;
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
public class Twix_AgentTagList
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private List<Integer> tagIds;
	private int OpenServiceTagId;
	private int DispatchId;
	private boolean thisTagOnly;
	private boolean repeatTagHeader = true;
	private boolean printEquipmentSummary = false;
	private String tagIdListFormat = "";
	private Set<Integer> equipmentIds;
	private boolean TM = true;
	
	public Twix_AgentTagList(Twix_Application a, Context c, List<Integer> ids, boolean repeat)
		{
		app = a;
		mContext = c;
		Twix_Theme = app.Twix_Theme;
		tagIds = ids;
		repeatTagHeader = repeat;
		equipmentIds = new TreeSet<Integer>();
		}
	
	public Twix_AgentTagList(Twix_Application a, Context c, int dispId, int otagId, boolean tagOnly )
		{
		app = a;
		Twix_Theme = app.Twix_Theme;
		mContext = c;
		DispatchId = dispId;
		OpenServiceTagId = otagId;
		thisTagOnly = tagOnly;
		}
	
	public List<LinearLayout> generate()
		{
		List<LinearLayout> LLlist = new ArrayList<LinearLayout>();
		int size = tagIds.size();
		
		if( size <= 0 )
			return LLlist;
		if( repeatTagHeader )
			{
			for( int i = 0; i < size; i++ )
				{
				LLlist.add( createTag(tagIds.get(i), (i != 0)) ); 
				}
			}
		else
			{
			LLlist.add( tagHeaderDetails(tagIds.get(0)) );
			for( int i = 0; i < size; i++ )
				{
				LLlist.add( tagDetails(tagIds.get(i), (i != 0)) ); 
				}
			}
		
		if( printEquipmentSummary ) // TODO add && (size > 1)
			{
			// Build a tag list for the summary
			for( int i = 0; i < size; i++ )
				{
				tagIdListFormat += tagIds.get(i);
				if( i+1 < size )
					tagIdListFormat += ", ";
				}
			
			LinearLayout summary = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 5, 5, 5);
			summary.setLayoutParams(params);
			summary.setOrientation(LinearLayout.VERTICAL);
			
			summary.setBackgroundColor(Twix_Theme.tableBG);
			LinearLayout row = createRow();
			row.addView( createTextView("Summary", 1, false, false, false, Twix_Theme.headerBG) );
			summary.addView(row);
			
			LinearLayout summaryItems = null;
			size = equipmentIds.size();
			for( Iterator<Integer> i = equipmentIds.iterator(); i.hasNext(); )
				{
				summaryItems = createEquipmentSummary( i.next());
				if( summaryItems != null )
					summary.addView( summaryItems );
				}
			if( summary.getChildCount() > 1 )
				LLlist.add(summary);
			}
		
		return LLlist;
		}
	
	public List<LinearLayout> generate2()
		{
		List<LinearLayout> LLlist = new ArrayList<LinearLayout>();
		
		LLlist.add( dispatchSiteHeader(DispatchId) );
		
		LLlist.addAll( getServiceUnits(DispatchId, OpenServiceTagId) );
		
		return LLlist;
		}
	
	private class TagHeader
		{
		int	TagNo;
		String siteName;
		String tenant;
		String serviceDate;
		String batchNo;
		String jobNo;
		String serviceType;
		String disposition;
		String add1;
		String add2;
		String city;
		String state;
		String zip;
		String buildingNo;
		
		String requestedBy;
		String siteContact;
		String description;
		
		String empno;
		}
	
	private class UnitHeader
		{
		int	serviceTagUnitId;
		
		int equipmentId;
		String equipment;
		String serialNo;
		String manufacturer;
		String model;
		String servicePerformed;
		String comments;
		
		ArrayList<ServiceData> serviceData;
		
		public UnitHeader()
			{
			serviceData = new ArrayList<ServiceData>();
			}
		}
	
	private class ServiceData
		{
		int serviceTagId;
		String servicePerformed;
		String comments;
		
		public ServiceData()
			{
			serviceTagId = 0;
			}
		
		public boolean isEmpty()
			{
			return  ( (servicePerformed == null) || (servicePerformed.length() <= 0) ) &&
					( (comments == null) || (comments.length() <= 0) );
			}
		}
	
	/**
	 * NEW CONSOLIDATED SUMMARY
	 */
	
	private LinearLayout dispatchSiteHeader( int dispatchId )
		{
		TagHeader tagHeader = new TagHeader();
		
		String sqlQ =
			"SELECT " +
				"sa.siteName, d.tenant, sa.address1, sa.address2, sa.city, sa.state, sa.zip, sa.buildingNo, " + 
				"d.batchNo, d.jobNo, d.contractType, d.dateStarted, " + 
				"d.requestedBy, d.siteContact, d.description " + 
				
		
			"FROM dispatch AS d " + 
				"LEFT OUTER JOIN serviceAddress AS sa ON sa.serviceAddressId = d.serviceAddressId " + 
			
			"WHERE d.dispatchId = " + dispatchId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		int index = 0;
		
		if (cursor.moveToFirst())
			{
			// Service Address Details
			tagHeader.siteName		= Twix_TextFunctions.clean(cursor.getString(0));
			tagHeader.tenant		= Twix_TextFunctions.clean(cursor.getString(1));
			tagHeader.add1			= Twix_TextFunctions.clean(cursor.getString(2));
			tagHeader.add2			= Twix_TextFunctions.clean(cursor.getString(3));
			tagHeader.city			= Twix_TextFunctions.clean(cursor.getString(4));
			tagHeader.state			= Twix_TextFunctions.clean(cursor.getString(5));
			tagHeader.zip			= Twix_TextFunctions.clean(cursor.getString(6));
			tagHeader.buildingNo	= Twix_TextFunctions.clean(cursor.getString(7));
			
			// Dispatch Details
			tagHeader.batchNo		= Twix_TextFunctions.clean(cursor.getString(8));
			tagHeader.jobNo			= Twix_TextFunctions.clean(cursor.getString(9));
			tagHeader.serviceType	= Twix_TextFunctions.clean(cursor.getString(10));
			tagHeader.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(11));
			tagHeader.requestedBy	= Twix_TextFunctions.clean(cursor.getString(12));
			tagHeader.siteContact	= Twix_TextFunctions.clean(cursor.getString(13));
			tagHeader.description	= Twix_TextFunctions.clean(cursor.getString(14));
			
			// Set the Time and Materials Flag
			if( tagHeader.serviceType.contentEquals("PM") || tagHeader.serviceType.contentEquals("CONT") )
				TM = false;
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return buildHeader(tagHeader);
		}
	
	private LinearLayout buildHeader(TagHeader tagHeader)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.tableBG);
		
		// Ensure we have information
		if( tagHeader == null )
			return ret;
		
		LinearLayout row;
		
		// Row 1
		row = createRow();
		row.addView( createTextView("Site Name:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.siteName, 1, false, true, true, Twix_Theme.headerBG) );
		if( tagHeader.tenant.length() > 0 )
			{
			row.addView( createTextView("Tenant:", 0, true, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(tagHeader.tenant, 0, true, true, true, Twix_Theme.headerBG, 35) );
			}
		row.addView( createTextView("Call Date:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.serviceDate, 0, true, true, true, Twix_Theme.headerBG, 35) );
		ret.addView(row);
		
		// Row 2
		row = createRow();
		row.addView( createTextView("Batch No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.batchNo, 1, false, true, true) );
		row.addView( createTextView("Job No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.jobNo.replaceAll("(TTCA)", ""), 1, false, true, true) );
		row.addView( createTextView("Service Type:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.serviceType, 1, false, true, true) );
		ret.addView(row);
		
		// Row 3
		row = createRow();
		row.addView( createTextView("Requested By:", 1f, false, false, false) );
		row.addView( createTextView(tagHeader.requestedBy, 2f, false, true, true) );
		row.addView( createTextView("Contact:", 1f, false, false, false) );
		row.addView( createTextView(tagHeader.siteContact, 2f, false, true, true) );
		ret.addView(row);
		
		// Row 3.5
		row = createRow();
		row.addView( createTextView("Dispatch Description:", 1f, false, false, false) );
		TextView tv = createTextView(tagHeader.description, 4f, false, true, true);
		tv.setSingleLine();
		row.addView( tv );
		ret.addView(row);
		
		// Row 4
		row = createRow();
		row.addView( createTextView("Address 1:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add1, 1, false, true, true) );
		row.addView( createTextView("City:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.city, 1, false, true, true) );
		ret.addView(row);
		
		// Row 5
		row = createRow();
		row.addView( createTextView("Address 2:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add2, 1, false, true, true) );
		row.addView( createTextView("State:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.state, 1, false, true, true) );
		ret.addView(row);
		
		// Row 6
		row = createRow();
		row.addView( createTextView("Building No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.buildingNo, 1, false, true, true) );
		row.addView( createTextView("Zip:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.zip, 1, false, true, true) );
		ret.addView(row);
		
		return ret;
		}
	
	private List<LinearLayout> getServiceUnits(int dispatchId, int openServiceTagId)
		{
		List<LinearLayout> ret = new ArrayList<LinearLayout>();
		
		String sqlQ =
		"select DISTINCT su.equipmentId, eq.unitNo || ' - ' || ec.categoryDesc as title, eq.serialNo, " +
				"eq.manufacturer, eq.model " + 
		
		"from serviceTagUnit as su " + 
			"LEFT OUTER JOIN serviceTag as st ON st.serviceTagId = su.serviceTagId " + 
			"LEFT OUTER JOIN openServiceTag as ost ON ost.serviceTagId = su.serviceTagId " + 
			"LEFT OUTER JOIN equipment as eq ON eq.equipmentId = su.equipmentId " + 
			"LEFT OUTER JOIN equipmentCategory as ec ON ec.equipmentCategoryId = eq.equipmentCategoryId " + 
		
		"WHERE (st.dispatchId = " + dispatchId + " OR ost.dispatchId = " + dispatchId + ") ";
		
		if( thisTagOnly )
			sqlQ += "AND";
		else
			sqlQ += "OR";
		
		sqlQ += " ost.serviceTagId = " + openServiceTagId + " " + 
		
			"ORDER BY title";
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		Cursor cursor2;
		UnitHeader unit;
		if (cursor.moveToFirst())
			{
			do
				{
				unit = new UnitHeader();
				unit.equipmentId	= cursor.getInt(0);
				unit.equipment		= cursor.getString(1);
				unit.serialNo		= cursor.getString(2);
				unit.manufacturer	= cursor.getString(3);
				unit.model			= cursor.getString(4);
				
				sqlQ = "select su.serviceTagId, su.servicePerformed, su.comments " + 
						
						"from serviceTagUnit as su " + 
							"LEFT OUTER JOIN serviceTag as st ON st.serviceTagId = su.serviceTagId " + 
							"LEFT OUTER JOIN openServiceTag as ost ON ost.serviceTagId = su.serviceTagId " + 
							
						"WHERE (st.dispatchId = " + dispatchId + " OR ost.dispatchId = " + dispatchId + ") " +
							"AND su.equipmentId = " + unit.equipmentId + " ";
				
				if( thisTagOnly )
					sqlQ += "AND ost.serviceTagId = " + OpenServiceTagId + " ";
				
				sqlQ +=	"ORDER BY su.serviceTagId";
				cursor2 = app.db.rawQuery(sqlQ);
				ServiceData data;
				while( cursor2.moveToNext() )
					{
					data = new ServiceData();
					
					data.serviceTagId		= cursor2.getInt(0);
					data.servicePerformed	= cursor2.getString(1);
					data.comments			= cursor2.getString(2);
					
					unit.serviceData.add(data);
					}
				if (cursor2 != null && !cursor2.isClosed())
					cursor2.close();
				
				
				ret.add( buildServiceUnitEquipment(unit, dispatchId) );
				}
			while( cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	private LinearLayout buildServiceUnitEquipment( UnitHeader unitHeader, int dispatchId )
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.sortNone);
		
		LinearLayout row;
		
		if( unitHeader.equipmentId > 0 )
			{
			row = createRow();
			row.addView( createTextView("Equipment:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(unitHeader.equipment, 1, false, true, true, Twix_Theme.headerBG) );
			row.addView( createTextView("Serial No:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(unitHeader.serialNo, 1, false, true, true, Twix_Theme.headerBG) );
			ret.addView(row);
			
			row = createRow();
			row.addView( createTextView("Manufactuer:", 1, false, false, false) );
			row.addView( createTextView(unitHeader.manufacturer, 1, false, true, true) );
			row.addView( createTextView("Model:", 1, false, false, false) );
			row.addView( createTextView(unitHeader.model, 1, false, true, true) );
			ret.addView(row);
			}
		else
			{
			row = createRow();
			row.addView( createTextView("Equipment:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Not Selected", 3, false, true, true, Twix_Theme.headerBG) );
			ret.addView(row);
			}
		
		row = createRow();
		row.addView( createTextView("TagNo:", 1, false, false, false) );
		row.addView( createTextView("Service Performed:", 3, false, false, false) );
		row.addView( createTextView("Comments & Follow-Up:", 3, false, false, false) );
		row.setBackgroundColor(Twix_Theme.sortAsc);
		ret.addView(row);
		
		ServiceData data;
		int size = unitHeader.serviceData.size();
		for( int i = 0; i < size; i++ )
			{
			data = unitHeader.serviceData.get(i);
			if( !data.isEmpty() )
				{
				row = createRow();
				
				if( data.serviceTagId > 0 )
					row.addView( createTextView(data.serviceTagId+"", 1, false, false, true) );
				else
					row.addView( createTextView("New Tag", 1, false, false, true) );
				
				if( data.servicePerformed.length() > 0 )
					row.addView( createTextView(data.servicePerformed, 3, false, false, true) );
				else
					row.addView( createTextView("Not provided", 3, false, false, true) );
				
				if( data.comments.length() > 0 )
					row.addView( createTextView(data.comments, 3, false, false, true) );
				else
					row.addView( createTextView("None", 3, false, false, true) );
				ret.addView(row);
				}
			
			}
		
		LinearLayout labor = createLabor(unitHeader.equipmentId, dispatchId);
		LinearLayout material = createMaterial(unitHeader.equipmentId, dispatchId);
		
		if( labor != null )
			ret.addView( labor );
		
		if( material != null )
			ret.addView( material );
		
		return ret;
		}
	
	private LinearLayout createLabor(int equipmentId, int dispatchId)
		{
		LinearLayout ret = null;
		// SQL Query
		String sqlQ = "SELECT su.serviceTagId, sl.serviceDate, sl.regHours, sl.thHours, sl.dtHours, m.mechanic_name " +
			
			"FROM serviceLabor AS sl " +
				"LEFT OUTER JOIN mechanic AS m ON m.mechanic = SL.mechanic " +
				"LEFT OUTER JOIN serviceTagUnit AS su ON sl.serviceTagUnitId = su.serviceTagUnitId " +
				"LEFT OUTER JOIN serviceTag AS st ON st.serviceTagId = su.serviceTagId " +
				"LEFT OUTER JOIN openServiceTag AS ost ON ost.serviceTagId = su.serviceTagId " + 
			
			"WHERE su.equipmentId = " + equipmentId + " " + 
				"AND (st.dispatchId = " + dispatchId + " OR ost.dispatchId = " + dispatchId + ") ";
		
		if( thisTagOnly )
			sqlQ += "AND ost.serviceTagId = " + OpenServiceTagId + " ";
		
		sqlQ +=	"ORDER BY su.serviceTagId";
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 5, 10, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.tableBG2);
			
			LinearLayout row;
			
			// Create the Header
			row = createRow();
			row.addView( createTextView("TagNo", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Date:", 1, false, false, false, Twix_Theme.headerBG) );
			if( TM )
				{
				row.addView( createTextView("Regular Hours", 1, false, false, false, Twix_Theme.headerBG) );
				row.addView( createTextView("Time & Half Hours", 1, false, false, false, Twix_Theme.headerBG) );
				row.addView( createTextView("Double Time Hours", 1, false, false, false, Twix_Theme.headerBG) );
				}
			row.addView( createTextView("Mechanic", 1, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			int index = 0; int serviceTagId;
			do
				{
				index = 0;
				row = createRow();
				serviceTagId = cursor.getInt(0);
				if( serviceTagId > 0 )
					row.addView( createTextView( serviceTagId+"", 1, false, false, true) );
				else
					row.addView( createTextView( "New Tag", 1, false, false, true) );
				row.addView( createTextView( Twix_TextFunctions.DBtoNormal(cursor.getString(1)), 1, false, false, true) );
				if( TM )
					{
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(2)), 1, false, false, true) );
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(3)), 1, false, false, true) );
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(4)), 1, false, false, true) );
					}
				else
					index+=3;
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(5)), 1, false, false, true) );
				ret.addView(row);
				}
			while( cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private LinearLayout createMaterial(int equipmentId, int dispatchId)
		{
		LinearLayout ret = null;
		// SQL Query
		String sqlQ = "select su.serviceTagId, sm.quantity, sm.materialDesc " +
				
			"from serviceMaterial as sm " +
				"LEFT OUTER JOIN serviceTagUnit as su ON sm.serviceTagUnitId = su.serviceTagUnitId " +
				"LEFT OUTER JOIN serviceTag as st ON st.serviceTagId = su.serviceTagId " +
				"LEFT OUTER JOIN openServiceTag as ost ON ost.serviceTagId = su.serviceTagId " + 
			
			"WHERE su.equipmentId = " + equipmentId + " " + 
				"AND (st.dispatchId = " + dispatchId + " OR ost.dispatchId = " + dispatchId + ") ";
		
		if( thisTagOnly )
			sqlQ += "AND ost.serviceTagId = " + OpenServiceTagId + " ";
			
		sqlQ += "ORDER BY su.serviceTagId";
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 5, 10, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.tableBG2);
			
			LinearLayout row;
			
			// Create the Header
			row = createRow();
			row.addView( createTextView("TagNo", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Material Quantity", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Material Description", 3, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			int index; int serviceTagId;
			do
				{
				index = 0;
				row = createRow();
				serviceTagId = cursor.getInt(0);
				if( serviceTagId > 0 )
					row.addView( createTextView( serviceTagId+"", 1, false, false, true) );
				else
					row.addView( createTextView( "New Tag", 1, false, false, true) );
				
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(1)), 1, false, false, true) );
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(2)), 3, false, false, true) );
				ret.addView(row);
				}
			while( cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	/**
	 * OLD UNCONSOLIDATED SUMMARY
	 */
	
	/**
	 * 
	 * @param serviceTagId
	 * @return
	 */
	private LinearLayout tagHeaderDetails(int serviceTagId)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.tableBG);
		
		TagHeader tagHeader = new TagHeader();
		String sqlQ = "SELECT " +
				
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.siteName) ELSE ( SA.siteName) END AS siteName, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( '' ) ELSE ( DS.tenant ) END AS tenant, " +
				
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.batchNo ) ELSE ( DS.batchNo ) END AS batchNo, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.jobNo ) ELSE ( substr(DS.jobNo, 5) ) END AS jobNo, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.serviceType ) ELSE ( DS.contractType ) END AS serviceType, " +
				
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.address1) ELSE ( SA.address1) END AS address1, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.address2) ELSE ( SA.address2) END AS address2, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.city) ELSE ( SA.city) END AS city, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.state) ELSE ( SA.state) END AS state, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.zip) ELSE ( SA.zip) END AS zip, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( substr( ST.serviceDate, 1, 10 ) ) ELSE ( DS.dateOrdered ) END AS callDate, " +
			
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.serviceType ) ELSE ( DS.contractType ) END AS tenant " +
			"from openServiceTag as ST " +
				
			"LEFT OUTER JOIN serviceAddress as SA " + 
				"on SA.serviceAddressId = ST.serviceAddressId " +
			"LEFT OUTER JOIN dispatch as DS " + 
				"on DS.dispatchId = ST.dispatchId " +
			"WHERE ST.serviceTagId = " + serviceTagId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			tagHeader.TagNo			= serviceTagId;
			tagHeader.siteName		= Twix_TextFunctions.clean(cursor.getString(0));
			tagHeader.tenant		= Twix_TextFunctions.clean(cursor.getString(1));
			tagHeader.batchNo		= Twix_TextFunctions.clean(cursor.getString(2));
			tagHeader.jobNo			= Twix_TextFunctions.clean(cursor.getString(3));
			tagHeader.serviceType	= Twix_TextFunctions.clean(cursor.getString(4));
			// Set the Time and Materials Flag
			if( tagHeader.serviceType.contentEquals("PM") || tagHeader.serviceType.contentEquals("CONT") )
				TM = false;
			
			tagHeader.add1			= Twix_TextFunctions.clean(cursor.getString(5));
			tagHeader.add2			= Twix_TextFunctions.clean(cursor.getString(6));
			tagHeader.city			= Twix_TextFunctions.clean(cursor.getString(7));
			tagHeader.state			= Twix_TextFunctions.clean(cursor.getString(8));
			tagHeader.zip			= Twix_TextFunctions.clean(cursor.getString(9));
			tagHeader.serviceDate	= Twix_TextFunctions.DBtoNormal( cursor.getString(10) );
			if( Twix_TextFunctions.DBtoNormal( cursor.getString(11) ).contentEquals("PM") )
				printEquipmentSummary = true;
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		LinearLayout row;
		
		row = createRow();
		row.addView( createTextView("Site Name:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.siteName, 1, false, true, true, Twix_Theme.headerBG) );
		if( tagHeader.tenant.length() > 0 )
			{
			row.addView( createTextView("Tenant:", 0, true, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(tagHeader.tenant, 0, true, true, true, Twix_Theme.headerBG, 35) );
			}
		row.addView( createTextView("Call Date:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.serviceDate, 0, true, true, true, Twix_Theme.headerBG, 35) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Batch No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.batchNo, 1, false, true, true) );
		row.addView( createTextView("Job No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.jobNo, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("", 2, false, false, false) );
		row.addView( createTextView("Service Type:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.serviceType, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Address 1:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add1, 1, false, true, true) );
		row.addView( createTextView("City:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.city, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Address 2:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add2, 1, false, true, true) );
		row.addView( createTextView("State:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.state, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("", 2, false, true, false) );
		row.addView( createTextView("Zip:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.zip, 1, false, true, true) );
		ret.addView(row);
		
		return ret;
		}
	
	private LinearLayout tagDetails(int serviceTagId, boolean closed)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.tableBG);
		
		TagHeader tagHeader = new TagHeader();
		String sqlQ = "SELECT " +
				"ST.disposition, " + 
				"substr( ST.serviceDate, 1, 10 ) as serviceDate, " +
				"m.mechanic_name ";
		
		if( closed )
			sqlQ += "from serviceTag as ST ";
		else
			sqlQ += "from openServiceTag as ST ";
		
		sqlQ += "LEFT OUTER JOIN mechanic as m " +
					"ON m.mechanic = ST.empno " +
			"WHERE ST.serviceTagId = '" + serviceTagId + "'";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		// Temp string variable for the Disposition text
		String s;
		
		if (cursor.moveToFirst())
			{
			tagHeader.TagNo			= serviceTagId;
			
			// Set the Disposition text
			s = Twix_TextFunctions.clean(cursor.getString(0));
			if( s.contentEquals("C"))
				tagHeader.disposition	= "Call Complete";
			else
				tagHeader.disposition	= "Must Return";
			
			tagHeader.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(1));
			tagHeader.empno			= Twix_TextFunctions.clean(cursor.getString(2));
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		LinearLayout row;
		
		row = createRow();
		row.addView( createTextView("TagNo:", 0, true, false, false, Twix_Theme.headerBG) );
		if( tagHeader.TagNo > 0 )
			s = tagHeader.TagNo + "";
		else
			s = "New Tag";
		row.addView( createTextView(s, 1, false, true, true, Twix_Theme.headerBG) );
		row.addView( createTextView("Service Date:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.serviceDate, 0, true, true, true, Twix_Theme.headerBG, 35) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Dispostion:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.disposition, 1, false, true, true) );
		row.addView( createTextView("Submitted by:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.empno, 1, false, true, true) );
		ret.addView(row);
		
		//Create the Units
		List<LinearLayout> units = createServiceUnits( serviceTagId );
		int size = units.size();
		for( int i = 0; i < size; i++ )
			{
			ret.addView( units.get(i) );
			}
		
		return ret;
		}
	
	private LinearLayout createTag(int serviceTagId, boolean closed)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.tableBG);
		
		TagHeader tagHeader = new TagHeader();
		String sqlQ = "SELECT " +
				
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.siteName) ELSE ( SA.siteName) END AS siteName, " +
				
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.batchNo ) ELSE ( DS.batchNo ) END AS batchNo, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.jobNo ) ELSE ( substr(DS.jobNo, 5) ) END AS jobNo, " +
				"CASE WHEN ST.dispatchId = '0' THEN ( ST.serviceType ) ELSE ( DS.contractType ) END AS serviceType, " +
				
				"ST.disposition, " + 
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.address1) ELSE ( SA.address1) END AS address1, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.address2) ELSE ( SA.address2) END AS address2, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.city) ELSE ( SA.city) END AS city, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.state) ELSE ( SA.state) END AS state, " +
				"CASE WHEN ST.serviceAddressId = '0' THEN ( ST.zip) ELSE ( SA.zip) END AS zip, " +
				"substr( ST.serviceDate, 1, 10 ) as serviceDate ";
				
				
				if( closed )
					sqlQ += "from serviceTag as ST ";
				else
					sqlQ += "from openServiceTag as ST ";
				
				sqlQ +=	"LEFT OUTER JOIN serviceAddress as SA " + 
							"on SA.serviceAddressId = ST.serviceAddressId " +
						"LEFT OUTER JOIN dispatch as DS " + 
							"on DS.dispatchId = ST.dispatchId " +
					"WHERE ST.serviceTagId = " + serviceTagId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		// Temp string variable for the Disposition text
		String s;
		
		if (cursor.moveToFirst())
			{
			tagHeader.TagNo			= serviceTagId;
			tagHeader.siteName		= Twix_TextFunctions.clean(cursor.getString(0));
			tagHeader.batchNo		= Twix_TextFunctions.clean(cursor.getString(1));
			tagHeader.jobNo			= Twix_TextFunctions.clean(cursor.getString(2));
			tagHeader.serviceType	= Twix_TextFunctions.clean(cursor.getString(3));
			// Set the Time and Materials Flag
			if( tagHeader.serviceType.contentEquals("PM") || tagHeader.serviceType.contentEquals("CONT") )
				TM = false;
			
			// Set the Disposition text
			s = Twix_TextFunctions.clean(cursor.getString(4));
			if( s.contentEquals("C"))
				tagHeader.disposition	= "Call Complete";
			else
				tagHeader.disposition	= "Must Return";
			
			tagHeader.add1			= Twix_TextFunctions.clean(cursor.getString(5));
			tagHeader.add2			= Twix_TextFunctions.clean(cursor.getString(6));
			tagHeader.city			= Twix_TextFunctions.clean(cursor.getString(7));
			tagHeader.state			= Twix_TextFunctions.clean(cursor.getString(8));
			tagHeader.zip			= Twix_TextFunctions.clean(cursor.getString(9));
			tagHeader.serviceDate	= Twix_TextFunctions.DBtoNormal( cursor.getString(10) );
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		
		//Create the Header
		LinearLayout row;
		
		row = createRow();
		row.addView( createTextView("Site Name:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.siteName, 1, false, true, true, Twix_Theme.headerBG) );
		row.addView( createTextView("Service Date:", 0, true, false, false, Twix_Theme.headerBG) );
		row.addView( createTextView(tagHeader.serviceDate, 0, true, true, true, Twix_Theme.headerBG, 35) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Batch No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.batchNo, 1, false, true, true) );
		row.addView( createTextView("Job No:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.jobNo, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Dispostion:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.disposition, 1, false, true, true) );
		row.addView( createTextView("Service Type:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.serviceType, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Address 1:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add1, 1, false, true, true) );
		row.addView( createTextView("City:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.city, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("Address 2:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.add2, 1, false, true, true) );
		row.addView( createTextView("State:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.state, 1, false, true, true) );
		ret.addView(row);
		
		row = createRow();
		row.addView( createTextView("", 2, false, true, false) );
		row.addView( createTextView("Zip:", 1, false, false, false) );
		row.addView( createTextView(tagHeader.zip, 1, false, true, true) );
		ret.addView(row);
		
		//Create the Units
		List<LinearLayout> units = createServiceUnits( serviceTagId );
		int size = units.size();
		for( int i = 0; i < size; i++ )
			{
			ret.addView( units.get(i) );
			}
		
		return ret;
		}
	
	private TextView createTextView(String text, float weight, boolean wrap, boolean fill, boolean value, int... parm)
		{
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams params;
		
		// Check the Wrap parameter
		if( fill & wrap )
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		else if( wrap )
			params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		else if( fill )
			params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
		else
			params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		// Set the weight
		params.weight = weight;
		tv.setLayoutParams(params);
		
		// Set the text
		tv.setText(text);
		// Set the Background Color
		if( parm.length > 0 )
			tv.setBackgroundColor(parm[0]);
		if( parm.length > 1 )
			tv.setMinimumWidth(parm[1]);
		// Set the Padding
		tv.setPadding(3, 3, 3, 3);
		
		//Set the Theme
		tv.setTextSize(Twix_Theme.subSize);
		if( value )
			tv.setTextColor(Twix_Theme.sub1Value);
		else
			tv.setTextColor(Twix_Theme.sub1Header);
		
		return tv;
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
	
	private List<LinearLayout> createServiceUnits(int serviceTagId)
		{
		// Returns a list of linear layouts that contain each service unit, including labor and materials
		List<LinearLayout> LLlist = new ArrayList<LinearLayout>();
		
		// SQL Query
		UnitHeader unitHeader = new UnitHeader();
		String sqlQ =
			"SELECT SU.serviceTagUnitId, SU.equipmentId, " +
				
				"CASE WHEN SU.equipmentId = '0' THEN ( 'Not Selected' ) " +
					"ELSE ( EQ.unitNo || ' - ' || EC.categoryDesc ) END AS equipmentName, " +
				"EQ.serialNo, EQ.manufacturer, EQ.model, SU.servicePerformed, SU.comments " +
				
			"from serviceTagUnit as SU " + 
				
				"LEFT OUTER JOIN equipment as EQ " + 
						"on EQ.equipmentId = SU.equipmentId " +
				"LEFT OUTER JOIN equipmentCategory as EC " + 
						"on EC.equipmentCategoryId = EQ.equipmentCategoryId " +
					"WHERE SU.serviceTagId = " + serviceTagId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		boolean equipment = true;
		int equipmentId;
		
		if (cursor.moveToFirst())
			{
			do
				{
				unitHeader.serviceTagUnitId = cursor.getInt(0);
				equipmentId = cursor.getInt(1);
				if( printEquipmentSummary )
					equipmentIds.add(equipmentId);
				if( equipmentId == 0 )
					equipment = false;
				unitHeader.equipment		= Twix_TextFunctions.clean(cursor.getString(2));
				if( equipment )
					{
					unitHeader.serialNo			= Twix_TextFunctions.clean(cursor.getString(3));
					unitHeader.manufacturer		= Twix_TextFunctions.clean(cursor.getString(4));
					unitHeader.model			= Twix_TextFunctions.clean(cursor.getString(5));
					}
				unitHeader.servicePerformed	= Twix_TextFunctions.clean(cursor.getString(6));
				unitHeader.comments			= Twix_TextFunctions.clean(cursor.getString(7));
				
				LLlist.add( createServiceUnit(unitHeader, equipment) );
				}
			while( cursor.moveToNext());
			
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return LLlist;
		}
	
	private LinearLayout createServiceUnit( UnitHeader unitHeader, boolean equipment)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setBackgroundColor(Twix_Theme.sortNone);
		
		LinearLayout row;
		
		if( equipment )
			{
			row = createRow();
			row.addView( createTextView("Equipment:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(unitHeader.equipment, 1, false, true, true, Twix_Theme.headerBG) );
			row.addView( createTextView("Serial No:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(unitHeader.serialNo, 1, false, true, true, Twix_Theme.headerBG) );
			ret.addView(row);
			
			row = createRow();
			row.addView( createTextView("Manufactuer:", 1, false, false, false) );
			row.addView( createTextView(unitHeader.manufacturer, 1, false, true, true) );
			row.addView( createTextView("Model:", 1, false, false, false) );
			row.addView( createTextView(unitHeader.model, 1, false, true, true) );
			ret.addView(row);
			}
		else
			{
			row = createRow();
			row.addView( createTextView("Equipment:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView(unitHeader.equipment, 3, false, true, true, Twix_Theme.headerBG) );
			ret.addView(row);
			}
		
		row = createRow();
		row.addView( createTextView("Service Performed:", 1, false, false, false) );
		row.addView( createTextView("Comments & Follow-Up:", 1, false, false, false) );
		ret.addView(row);
		
		row = createRow();
		if( unitHeader.servicePerformed.length() > 0 )
			row.addView( createTextView(unitHeader.servicePerformed, 1, false, false, true) );
		else
			row.addView( createTextView("Not provided", 1, false, false, true) );
		
		if( unitHeader.comments.length() > 0 )
			row.addView( createTextView(unitHeader.comments, 1, false, false, true) );
		else
			row.addView( createTextView("None", 1, false, false, true) );
		ret.addView(row);
		
		LinearLayout labor = createUnitLabor(unitHeader.serviceTagUnitId);
		LinearLayout material = createUnitMaterial(unitHeader.serviceTagUnitId);
		
		if( labor != null )
			ret.addView( labor );
		
		if( material != null )
			ret.addView( material );
		
		return ret;
		}
	
	private LinearLayout createUnitLabor(int serviceTagUnitId)
		{
		LinearLayout ret = null;
		// SQL Query
		String sqlQ =
			"SELECT SL.serviceDate, SL.regHours, SL.thHours, SL.dtHours, m.mechanic_name " +
			"from serviceLabor as SL " +
				"LEFT OUTER JOIN mechanic as m " +
					"on m.mechanic = SL.mechanic " + 
				"WHERE SL.serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 5, 10, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.tableBG2);
			
			LinearLayout row;
			
			// Create the Header
			row = createRow();
			row.addView( createTextView("Date:", 1, false, false, false, Twix_Theme.headerBG) );
			if( TM )
				{
				row.addView( createTextView("Regular Hours", 1, false, false, false, Twix_Theme.headerBG) );
				row.addView( createTextView("Time & Half Hours", 1, false, false, false, Twix_Theme.headerBG) );
				row.addView( createTextView("Double Time Hours", 1, false, false, false, Twix_Theme.headerBG) );
				}
			row.addView( createTextView("Mechanic", 1, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			do
				{
				row = createRow();
				row.addView( createTextView( Twix_TextFunctions.DBtoNormal(cursor.getString(0)), 1, false, false, true) );
				if( TM )
					{
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(1)), 1, false, false, true) );
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(2)), 1, false, false, true) );
					row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(3)), 1, false, false, true) );
					}
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(4)), 1, false, false, true) );
				ret.addView(row);
				}
			while( cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private LinearLayout createUnitMaterial(int serviceTagUnitId)
		{
		LinearLayout ret = null;
		// SQL Query
		String sqlQ =
			"SELECT SM.quantity, SM.materialDesc " +
			"from serviceMaterial as SM " + 
				"WHERE SM.serviceTagUnitId = " + serviceTagUnitId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 5, 10, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.tableBG2);
			
			LinearLayout row;
			
			// Create the Header
			row = createRow();
			row.addView( createTextView("Material Quantity:", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Material Description", 3, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			do
				{
				row = createRow();
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(0)), 1, false, false, true) );
				row.addView( createTextView( Twix_TextFunctions.clean(cursor.getString(1)), 3, false, false, true) );
				ret.addView(row);
				}
			while( cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	
	private LinearLayout createEquipmentSummary(int equipmentId)
		{
		LinearLayout ret = null;
		// Make sure we have labor or materials, otherwise don't bother adding it to the summary
		LinearLayout labor = null;
		if( TM )
			{
			labor = createEquipmentLabor(equipmentId);
			}
		LinearLayout materials = createEquipmentMaterial(equipmentId);
		
		if( labor != null || materials != null)
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 5, 5, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.tableBG2);
			
			LinearLayout row;
			row = createRow();
			
			String sqlQ = "select e.unitNo, ec.categoryDesc " +
					"from equipment AS e " + 
					"LEFT OUTER JOIN equipmentCategory AS ec " + 
						"on ec.equipmentCategoryId = e.equipmentCategoryId " +
					"WHERE e.equipmentId = " + equipmentId;
			Cursor cursor = app.db.rawQuery(sqlQ);
			
			if (cursor.moveToFirst())
				{
				row.addView( createTextView("Equipment: ", 1, false, false, false) );
				row.addView( createTextView(
							Twix_TextFunctions.clean(cursor.getString(0)) +
							" - " +
							Twix_TextFunctions.clean(cursor.getString(1))
									, 2, false, false, true) );
				}
			else
				{
				row.addView( createTextView("Equipment: ", 1, false, false, false) );
				row.addView( createTextView("Not Selected", 2, false, false, true) );
				}
			if (cursor != null && !cursor.isClosed())
				{
				cursor.close();
				}
			ret.addView(row);
			
			
			if( materials != null )
				ret.addView( materials );
			
			if( labor != null )
				ret.addView( labor );
			}
		
		return ret;
		}
	
	private LinearLayout createEquipmentLabor(int equipmentId)
		{
		LinearLayout ret = null;
		String sqlQ = "select " + 
				"SUM(SL.regHours) AS regHours, " +
				"SUM(SL.thHours) AS thHours, " +
				"SUM(SL.dtHours) AS dtHours, " +
				"m.mechanic_name " +
			"FROM serviceLabor AS SL " +
				"LEFT OUTER JOIN serviceTagUnit AS SU " +
					"ON SU.serviceTagUnitId = SL.serviceTagUnitId " +
				"LEFT OUTER JOIN serviceTag AS ST " +
					"ON ST.serviceTagId = SU.serviceTagId " +
				"LEFT OUTER JOIN openServiceTag AS oST " +
					"ON oST.serviceTagId = SU.serviceTagId " +
				"LEFT OUTER JOIN mechanic AS m " +
					"ON m.mechanic = SL.mechanic " +
	
			"WHERE SU.equipmentId = " + equipmentId + " " +
				"AND ( ST.serviceTagId IN (" + tagIdListFormat + ") " +
						"OR oST.serviceTagId IN (" + tagIdListFormat + ") " +
					") " +
			
			"GROUP BY SL.mechanic " +
			"ORDER BY m.mechanic_name";
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 5, 5, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.sortNone);
			
			LinearLayout row;
			row = createRow();
			row.addView( createTextView("Reg Hours", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Time & Half Hours", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Double Time Hours", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Mechanic", 2, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			do
				{
				row = createRow();
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(0)), 1, false, false, true) );
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(1)), 1, false, false, true) );
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(2)), 1, false, false, true) );
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(3)), 2, false, false, true) );
				ret.addView(row);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private LinearLayout createEquipmentMaterial(int equipmentId)
		{
		LinearLayout ret = null;
		String sqlQ = "select " + 
				"SM.materialDesc, " +
				"SUM(SM.quantity) AS quantity " +
			"FROM serviceMaterial AS SM " +
				"LEFT OUTER JOIN serviceTagUnit AS SU " +
					"ON SU.serviceTagUnitId = SM.serviceTagUnitId " +
				"LEFT OUTER JOIN serviceTag AS ST " +
					"ON ST.serviceTagId = SU.serviceTagId " +
				"LEFT OUTER JOIN openServiceTag AS oST " +
					"ON oST.serviceTagId = SU.serviceTagId " +
				
			"WHERE SU.equipmentId = " + equipmentId + " " +
				"AND ( ST.serviceTagId IN (" + tagIdListFormat + ") " +
						"OR oST.serviceTagId IN (" + tagIdListFormat + ") " +
					") " +
						
			"GROUP BY SM.materialDesc " +
			"ORDER BY SM.materialDesc";
		
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			ret = new LinearLayout(mContext);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 5, 5, 5);
			ret.setLayoutParams(params);
			ret.setOrientation(LinearLayout.VERTICAL);
			ret.setBackgroundColor(Twix_Theme.sortNone);
			
			LinearLayout row;
			row = createRow();
			row.addView( createTextView("Quanity", 1, false, false, false, Twix_Theme.headerBG) );
			row.addView( createTextView("Material Description", 3, false, false, false, Twix_Theme.headerBG) );
			ret.addView(row);
			
			do
				{
				row = createRow();
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(1)), 1, false, false, true) );
				row.addView( createTextView(Twix_TextFunctions.clean(cursor.getString(0)), 3, false, false, true) );
				ret.addView(row);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	}
