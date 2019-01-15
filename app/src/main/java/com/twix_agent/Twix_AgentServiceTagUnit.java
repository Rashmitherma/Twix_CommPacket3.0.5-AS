package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceTagUnit
 * 
 * Purpose: Contains a details for a unit being serviced in a service tag. It contains a few shortcuts to access data
 * 			quickly and easily. Choosing the piece of equipment can be done from a dropdown box or selected with a
 * 			barcode scanner. Services performed can be appended from a dropdown box.
 * 
 * Relevant XML: servicetag_unit.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceTagUnit extends Activity
	{
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_Application app;
	private Twix_SQLite db;
	private Context mContext;
	private Twix_AgentTheme Twix_Theme;
	public Button unitSpinner,unitSpinner2,allequipments;
	private Spinner spinnerUnitNo,spinnerUnitNo2;
	private Spinner spinnerServices;
	private ArrayAdapter<CharSequence> adapterUnitNo, adapterServices;
	private ArrayList<String> mirrorUnitNo;
	private ArrayList<Integer> mirrorEquipmentCategoryId;
	private ArrayList<UnitDetails> udata;
	public int currentEquipmentCategoryId;
	public int EquipId = 0;
	public boolean init = false;
	private int equipmentSel = -1;
	private List<Integer> usedPMEquipmentIds;
	private boolean HasForms = false;
//	public boolean all =false;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_unit, null);
		this.setContentView(viewToLoad);
        
		unitSpinner = (Button)viewToLoad.findViewById(R.id.UnitSpinner);
        spinnerUnitNo = (Spinner)viewToLoad.findViewById(R.id.UnitNo);
        unitSpinner2 = (Button)viewToLoad.findViewById(R.id.UnitSpinner2);
        spinnerUnitNo2 = (Spinner)viewToLoad.findViewById(R.id.UnitNo2);
        spinnerServices = (Spinner)viewToLoad.findViewById(R.id.PredefinedServices);
       
		unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		
        boolean hasServiceAddress = unitAct.tag.serviceAddressId > 0;
        spinnerUnitNo.setEnabled( hasServiceAddress );
        if( !hasServiceAddress )
        	viewToLoad.findViewById(R.id.ScannerLookup).setVisibility(View.INVISIBLE);
        
        app = (Twix_Application) this.getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        readOnlySetup();
        
        usedPMEquipmentIds = new ArrayList<Integer>();
        
        getUsedEquipmentIds();
        buildAdapters();
        
        clickListeners();
        
        readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( unitAct.tag.tagReadOnly )
			{
			((Spinner)findViewById(R.id.UnitNo)).setEnabled(false);
			findViewById(R.id.ScannerLookup).setVisibility(View.GONE);
			((Spinner)findViewById(R.id.PredefinedServices)).setEnabled(false);
			((EditText)findViewById(R.id.ServicesPerformed)).setEnabled(false);
			((EditText)findViewById(R.id.Comments)).setEnabled(false);
		 //	((Button)findViewById(R.id.all)).setEnabled(true);
			}
		
		String sqlQ = "SELECT FormDataId FROM FormData WHERE ParentTable = 'ServiceTagUnit' AND ParentId = " + unitAct.serviceTagUnitId;
		Cursor cursor = db.rawQuery(sqlQ);
		HasForms = cursor.moveToNext();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	
	public void buildAdapters()
		{
		adapterUnitNo = new ArrayAdapter<CharSequence>(getParent().getParent(), R.layout.spinner_layout);
		adapterUnitNo.setDropDownViewResource(R.layout.spinner_popup);
		mirrorUnitNo = new ArrayList<String>();
		mirrorEquipmentCategoryId = new ArrayList<Integer>();
		
		adapterUnitNo.add("No Unit Selected");		mirrorUnitNo.add("0");
		
		spinnerUnitNo.setAdapter(adapterUnitNo);	mirrorEquipmentCategoryId.add(0);
		
		adapterServices = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
		adapterServices.setDropDownViewResource(R.layout.spinner_popup);
		adapterServices.add("");
		spinnerServices.setAdapter(adapterServices);
		}
	
	private void readSQL()
		{
		//******* Update the page's details
		String categoryAt = "";
    	String sqlQ = "SELECT serviceTagUnit.servicePerformed, serviceTagUnit.comments, " +
    			"equipment.equipmentId, " +
    			"equipment.unitNo, equipmentCategory.categoryDesc, equipmentCategory.equipmentCategoryId " +
    				"FROM serviceTagUnit " +
    				"LEFT OUTER JOIN equipment on equipment.equipmentId = serviceTagUnit.equipmentId " +
    				"LEFT OUTER JOIN equipmentCategory on equipmentCategory.equipmentCategoryId = equipment.equipmentCategoryId " +
    			"WHERE serviceTagUnit.serviceTagUnitId = " + unitAct.serviceTagUnitId;
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
			{
			EditText et;
			do
				{
				categoryAt = clean( cursor.getString(2) );
				et = (EditText)findViewById(R.id.ServicesPerformed);
				et.setText( clean( cursor.getString(0) ) );
				et.addTextChangedListener(unitAct.setDirtyFlag);
				
				et = (EditText)findViewById(R.id.Comments);
				et.setText( clean( cursor.getString(1) ) );
				et.addTextChangedListener(unitAct.setDirtyFlag);
				
				UnitDetails unit = new UnitDetails();
				unit.selection = null;
				String unitNo = Twix_TextFunctions.clean(cursor.getString(3));
				String catDesc = Twix_TextFunctions.clean(cursor.getString(4));
				if( (unitNo.length() > 0) || (catDesc.length() > 0) )
					unit.title = unitNo + " - " + catDesc;
				unit.equipmentId = cursor.getInt(2);
				unitAct.EquipmentId = unit.equipmentId;
				unit.equipmentCategoryId = currentEquipmentCategoryId = cursor.getInt(5);
				unitSpinner.setTag(unit);
				unitSpinner.setText(unit.title);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		// Build the list of equipment choices
		sqlQ = "select equipment.equipmentId, equipment.unitNo, " +
    			"equipmentCategory.categoryDesc, equipmentCategory.equipmentCategoryId " +
    					"from equipment " + 
						"INNER JOIN equipmentCategory " + 
							"on equipment.equipmentCategoryId = equipmentCategory.equipmentCategoryId " +
							"WHERE equipment.serviceAddressId = " + unitAct.tag.serviceAddressId + " " + 
							"ORDER BY equipmentCategory.categoryDesc";
    	cursor = db.rawQuery(sqlQ);
    	
    	int count = 1;
    	if (cursor.moveToFirst())
			{
			do
				{
				if( cursor.getString(0).contentEquals(categoryAt) )
					spinnerUnitNo.setSelection(count);
				adapterUnitNo.add( cursor.getString(2) + " - " + cursor.getString(1) );
				mirrorUnitNo.add( cursor.getString(0) );
				mirrorEquipmentCategoryId.add( cursor.getInt(3) );
				count++;
				}
		    	while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		
		//******* Build the Service Descriptions
		sqlQ = "select serviceDescription.description from serviceDescription " +
				"ORDER BY serviceDescription.description";
		cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			do
				{
				adapterServices.add(cursor.getString(0));
				}
		    	while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	private void getUsedEquipmentIds()
		{
		/*
		// Used as subquery
		String dispatchSelect = "SELECT D.dispatchId " +
						"FROM openServiceTag as ST, serviceTagUnit as SU, dispatch as D " +
						"WHERE " +
							"SU.serviceTagUnitId = " + unitAct.serviceTagUnitId + " " +
							"AND ST.serviceTagId = SU.serviceTagId " +
							"AND D.dispatchId = ST.dispatchId " +
							"AND D.contractType = 'PM'" +
						"LIMIT 1";
		
		// Main Query
    	String sqlQ =
    			"SELECT DISTINCT SU.equipmentId " +
    	
    			"FROM serviceTagUnit as SU " +
    				"LEFT OUTER JOIN openServiceTag as oST " +
    					"ON oST.serviceTagId = SU.serviceTagId " +
    				"LEFT OUTER JOIN serviceTag as ST " +
    					"ON ST.serviceTagId = SU.serviceTagId " +
    					
    				
    			"WHERE ST.dispatchId = (" + dispatchSelect + ") " +
    					"OR oST.dispatchId = ("+ dispatchSelect + ") ";
    	*/
    	
    	String sqlQ =
    			"SELECT DISTINCT SU.equipmentId " +
    	
    			"FROM serviceTagUnit as SU " +
    				"LEFT OUTER JOIN openServiceTag as oST " +
    					"ON oST.serviceTagId = SU.serviceTagId " +
    				"LEFT OUTER JOIN serviceTag as ST " +
    					"ON ST.serviceTagId = SU.serviceTagId " +
    					
    				
    			"WHERE ST.dispatchId = " + unitAct.tag.dispatchId + " " +
    					"OR oST.dispatchId = "+ unitAct.tag.dispatchId + " ";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
			{
			do
				{
				usedPMEquipmentIds.add( cursor.getInt(0) );
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void clickListeners()
		{
		
		
		findViewById( R.id.UnitSpinner2 ).setOnClickListener(new  OnClickListener()
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
		    	
		    	String sqlQ = "select DISTINCT ec.equipmentCategoryId, ec.categoryDesc  from equipmentCategory as ec inner join equipment as e on ec.equipmentCategoryId = e.equipmentCategoryId " +
		    			" where serviceAddressId = " + unitAct.tag.serviceAddressId + 
		    			" and unitNo != 'all'";
		    	Cursor cursor = db.rawQuery(sqlQ);
		    	UnitDetails2 unit2 = new UnitDetails2();
		    	unit2.selection2 = selection2;
		    	final boolean used = false;
		    	unit2.equipmentCategoryId = 0;
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
						
						
						unit2.equipmentCategoryId = cursor.getInt(0);
						
						
						
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
            	}});
		findViewById( R.id.ScannerLookup ).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	Intent intent = new Intent( "la.droid.qr.scan" );
            	intent.setPackage("la.droid.qr");
            	intent.putExtra( "la.droid.qr.complete", true);
            	try
            		{
            		((Twix_TabActivityGroup)mContext).startActivityForResult( intent, Twix_AgentActivityGroup3.SCAN_BARCODE_READ );
            		}
            	catch (ActivityNotFoundException e)
            		{
            		Toast.makeText(mContext,
            				"Cannot Find QR Droid Bar Code scanner. Please install QR Scanner from the Android Market and try again",
            				Toast.LENGTH_LONG).show();
            		}
    			return;  
                }
        	});
		
		
		spinnerUnitNo.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
				{
				if( (equipmentSel != arg2) && (equipmentSel != -1) )
					unitAct.dirtyFlag = true;
				
				int temp = mirrorEquipmentCategoryId.get(arg2);
				if( currentEquipmentCategoryId > 0 )
					{
					if( !(currentEquipmentCategoryId == temp) )
						{
						currentEquipmentCategoryId = temp;
						db.delete("pmCheckList", "serviceTagUnitId", unitAct.serviceTagUnitId );
						}
					}
				else
					currentEquipmentCategoryId = temp;
				
				equipmentSel = arg2;
				}
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				//Do Nothing
				}
			});
		
		if( !HasForms )
			{
			OnClickListener spinnerUnitSelect = new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					final Dialog selection;
					selection = new Dialog(mContext);
					selection.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			    	selection.setContentView(R.layout.spinner_dropdown);
			    	selection.getWindow().setLayout(600, LayoutParams.WRAP_CONTENT);
			    	
			    	selection.setCanceledOnTouchOutside(true);
			    	selection.setOnCancelListener(new OnCancelListener()
				    	{
						@Override
						public void onCancel(DialogInterface dialog)
							{
							selection.dismiss();
							}
				    	});

			    	
			    	LinearLayout listHost = (LinearLayout) selection.findViewById(R.id.ListHost);
			    	
			    	String sqlQ = 
		    				"SELECT e.equipmentId, e.unitNo, " +
		    					"ec.categoryDesc, ec.equipmentCategoryId,e.areaServed " +
		    				"FROM equipment AS e " + 
								"INNER JOIN equipmentCategory AS ec " + 
									"on e.equipmentCategoryId = ec.equipmentCategoryId " +
							"WHERE e.serviceAddressId = " + unitAct.tag.serviceAddressId +
							    " AND ec.equipmentCategoryId = " + currentEquipmentCategoryId +
								" AND (e.DateOutService IS NULL OR e.DateOutService = '' OR e.DateOutService LIKE '1900-01-01%' ";
			    	if( unitAct.EquipmentId != 0 )
			    		sqlQ += 			"OR e.equipmentId = " + unitAct.EquipmentId;
			    	sqlQ += 		")" + 
								"ORDER BY ec.categoryDesc";
			    	Cursor cursor = db.rawQuery(sqlQ);
			    	UnitDetails unit = new UnitDetails();
			    	unit.selection = selection;
			    	unit.equipmentId = 0;
			    	unit.equipmentCategoryId = 0;
			    	listHost.addView( createSelectionRow(unit, false) );
			    	boolean used = false;
			    	//boolean eall = false;
			    	int size = 0;
			    	if( usedPMEquipmentIds != null )
			    		size = usedPMEquipmentIds.size();
			    	
			    	if (cursor.moveToFirst())
						{
						do
							{
							unit = new UnitDetails();
							unit.selection = selection;
							unit.title = Twix_TextFunctions.clean(cursor.getString(2)) + " - " +
									Twix_TextFunctions.clean(cursor.getString(1));
							unit.all = cursor.getString(1);
						//	if(unit.all == "all")
						//		eall=true;
							unit.equipmentId = cursor.getInt(0);
							unit.equipmentCategoryId = cursor.getInt(3);
							used=false;
						//	unit.areaserved = cursor.getString(4);
							String pp = cursor.getString(4);
							if(pp != null)
								{
							unit.areaserved = cursor.getString(4);
								}
							
							for( int i = 0; i < size; i++ )
								if( unit.equipmentId == usedPMEquipmentIds.get(i) )
									used = true;
							    
							listHost.addView( createSelectionRow(unit, used) );
							
							}
					    while (cursor.moveToNext());
						}
					if (cursor != null && !cursor.isClosed())
						{
						cursor.close();
						}
			    	
			    	selection.show();
			    	
					}
				}
			;
			unitSpinner.setOnClickListener(spinnerUnitSelect);
			}
		else
			unitSpinner.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					AlertDialog alert = new AlertDialog.Builder(mContext).create();
			    	alert.setTitle("Cannot Change Equipment Choice" );
			    	alert.setMessage(
			    			"There are Forms filled out for this service unit, " +
			    			"which are dependant on the equipment.");
			    	alert.setButton("Ok", new DialogInterface.OnClickListener()
			    		{  
			    		public void onClick(DialogInterface dialog, int which)
			    			{
			    			return;  
			    			}
			    		});
			    	alert.show();
					}
				})
			;
		
		spinnerServices.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3)
				{
				if( arg2 > 0 )
					{
					if( init )
						unitAct.dirtyFlag = true;
					
					TextView tv = (TextView)findViewById(R.id.ServicesPerformed);
					String curText = tv.getText().toString();
					String s = (String)arg0.getSelectedItem();
					arg0.setSelection(0);
					
					if( (curText.length() + s.length()) > 5000 )
						{
						Toast.makeText(Twix_AgentServiceTagUnit.this, "Service performed field too large to add more.", Toast.LENGTH_LONG).show();
						return;
						}
					s = curText + s + "; ";
					tv.setText(s);
					}
				}
			@Override
			public void onNothingSelected(AdapterView<?> arg0)
				{
				//Do Nothing
				}
			
			});
		}
	
	private class UnitDetails
		{
		String title = "Not Selected";
		int equipmentId;
		int equipmentCategoryId;
		Dialog selection;
		String all = null;
		String areaserved = " ";
		}
	public class UnitDetails2
	{
	String title = "Not Selected";
	int equipmentId;
	String categoryDesc = null;
	int equipmentCategoryId;
	Dialog selection2;
	
	
	}
	
	private View createSelectionRow(UnitDetails unit, boolean used)
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);
		final TextView tv = new TextView(mContext);
		boolean all = false;
		
		tv.setLayoutParams(params);
		tv.setTextSize(Twix_Theme.headerSize);
		tv.setTextColor(Twix_Theme.headerValue);
		tv.setBackgroundColor(Twix_Theme.tableBG2);
		 final boolean eall =false;
		if( used )
			tv.setBackgroundResource(R.drawable.clickable_bg);
		else
			tv.setBackgroundResource(R.drawable.clickable_bg2);
		//unit.areaserved = "   ";
		tv.setText(unit.title + "              " + unit.areaserved );
		
		tv.setTag(unit);
		tv.setPadding(3, 3, 3, 3);
		tv.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				UnitDetails unit = (UnitDetails) v.getTag();
				
				unitAct.EquipmentId = unit.equipmentId;
				currentEquipmentCategoryId = unit.equipmentCategoryId;
				
				unitSpinner.setText(unit.title);
				unitSpinner.setTag(unit);
			//	if(unit.all == "all")
				
				//     eall = true;
					
				
				
				unit.selection.dismiss();
				
				unitAct.dirtyFlag = true;
				
				}
				
			});
		
		return tv;
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
		
		 final boolean eall =false;
			if( used )
				tv2.setBackgroundResource(R.drawable.clickable_bg);
			else
				tv2.setBackgroundResource(R.drawable.clickable_bg2);
		
		
		tv2.setText(unit2.title);
		
		tv2.setTag(unit2);
		tv2.setPadding(3, 3, 3, 3);
		tv2.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				UnitDetails2 unit2 = (UnitDetails2) v.getTag();
				
				unitAct.EquipmentId = unit2.equipmentId;
				currentEquipmentCategoryId = unit2.equipmentCategoryId;
				
				unitSpinner2.setText(unit2.title);
				unitSpinner2.setTag(unit2);
				
				
				
				unit2.selection2.dismiss();
				
				unitAct.dirtyFlag = true;
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, unitSpinner2.getText(), duration);
				toast.show();
				}
				
			});
		
		return tv2;
		}
	
	public void scanResult( String barCode )
    	{
    	String sqlQ = "SELECT equipmentId, equipment.unitNo, equipmentCategory.categoryDesc, equipment.equipmentCategoryId " +
    			"FROM equipment " +
    				"LEFT OUTER JOIN equipmentCategory on equipmentCategory.equipmentCategoryId = equipment.equipmentCategoryId " +
    			"WHERE barCodeNo = '" + barCode + "' " +
    				"AND serviceAddressId = " + unitAct.tag.serviceAddressId;
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	UnitDetails unit = null;
		if (cursor.moveToFirst())
			{
			unit = new UnitDetails();
			unit.selection = null;
			unit.equipmentId = cursor.getInt(0);
			unit.equipmentCategoryId = currentEquipmentCategoryId = cursor.getInt(3);
			unit.title = Twix_TextFunctions.clean( cursor.getString(1) ) + " - " + Twix_TextFunctions.clean( cursor.getString(2) );
			unitSpinner.setTag(unit);
			unitSpinner.setText(unit.title);
			
			if( cursor.moveToNext() )
				Toast.makeText(mContext, "Warning: This BarCode is " +
						"shared by mulitple equipment. Please Make sure " +
						"all equipment have unique barcodes.", Toast.LENGTH_LONG).show();
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
	    // If the equipmentId isn't found, post an error message
	    if( unit == null )
	    	Toast.makeText(mContext, "No equipment registered with that barcode.", Toast.LENGTH_LONG).show();

		}
	
	public void updateDB()
		{
		UnitDetails unit = (UnitDetails) unitSpinner.getTag();
		
		ContentValues cv = new ContentValues();
		cv.put("equipmentId", verifyEquipment(unit.equipmentId) );
		cv.put("servicePerformed", ((EditText)findViewById( R.id.ServicesPerformed )).getText().toString().replaceAll("\\n", "\n") );
		cv.put("comments", ((EditText)findViewById( R.id.Comments )).getText().toString().replaceAll("\\n", "\n") );
		
		db.update("serviceTagUnit", cv, "serviceTagUnitId", unitAct.serviceTagUnitId );
		}
	
	private String clean(String s)
		{
		if( s == null )
			return "";
		
		return s;
		}
	
	private int verifyEquipment(int id)
		{
		int ret = 0;
    	String sqlQ = "SELECT equipmentId from equipment where equipmentId = " + id;
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
			ret = cursor.getInt(0);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	@Override
	public void onResume()
		{
		super.onResume();
		
		findViewById(R.id.dummy_focus).requestFocus();
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
