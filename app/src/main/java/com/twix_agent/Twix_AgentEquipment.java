package com.twix_agent;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentEquipment
 * 
 * 
 * Purpose: Lists all of the equipment at a given site. The activity expects a serviceAddressId be provided
 * 			from the launching activity. Each row is clickable, allowing the user to view the equipment
 * 			details. Each row also contains a button that allows the user to edit the piece of equipment.
 * 			Users are also able to create new pieces of equipment at the site, as well as delete pieces
 * 			that have been added but not synced.
 * 
 * Relevant XML: equipment.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentEquipment extends Activity
	{
	private boolean readOnly;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext;
	private LinearLayout ll;
	
	private String addressText = "";
	private int serviceAddressId;
	
	int categoryW = 0;
	private boolean desc = false;
	private String CurrentSearch = "CategoryDesc";
	private OnClickListener rowClick;
	
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.equipment, null);
		this.setContentView( viewToLoad );
        
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
        if( readOnly )
        	findViewById(R.id.AddEquipmentHolder).setVisibility(View.GONE);
        addressText = getIntent().getStringExtra("SiteName");
        serviceAddressId = getIntent().getIntExtra("serviceAddressId", 0);
        ((TextView)findViewById(R.id.title)).setText("Equipment for " + addressText);
        
        ll = (LinearLayout) findViewById(R.id.EquipmentList);
        // Changes the background according to the Twix_Theme. This may be the way to handling theming in the future
        ll.setBackgroundColor(Twix_Theme.lineColor);
        findViewById(R.id.EquipmentHeader).setBackgroundColor(Twix_Theme.lineColor);
        setClickListeners();
        
    	Update_Equipment();
    	}
    
    public void readSQL()
	    {
	    if( serviceAddressId == 0 )
	    	return;
	    ll.removeAllViews();
	    
    	String sqlQ =
    			"SELECT * FROM (" +
    			"SELECT e.equipmentId, ec.CategoryDesc, " +
    				"e.UnitNo, e.AreaServed, e.Manufacturer, e.Model, " +
    				"e.SerialNo, e.verified, e.verifiedByEmpno, " +
    				"CASE WHEN e.DateOutService IS NULL OR e.DateOutService = '' OR e.DateOutService LIKE '1900-01-01%' THEN 0 ELSE 1 END AS outServiceFlag " +
    			"FROM Equipment as e " +
    				"LEFT OUTER JOIN EquipmentCategory as ec " +
    					"ON e.EquipmentCategoryId = ec.EquipmentCategoryId " +
    			"WHERE e.serviceAddressID = " + serviceAddressId + ") " +
    			"ORDER BY outServiceFlag asc, " + CurrentSearch;
    	
    	
    	if( !desc )
    		sqlQ += " asc";
    	else
    		sqlQ += " desc";
    	
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	EquipmentData data;
		if (cursor.moveToFirst())
			{
			do
				{
				data = new EquipmentData();
				data.EquipmentId	= cursor.getInt(0);
				data.EqCat			= Twix_TextFunctions.clean( cursor.getString(1) );
				data.UnitNo			= Twix_TextFunctions.clean( cursor.getString(2) );
				data.AreaServed		= Twix_TextFunctions.clean( cursor.getString(3) );
				data.Manufacturer	= Twix_TextFunctions.clean( cursor.getString(4) );
				data.Model			= Twix_TextFunctions.clean( cursor.getString(5) );
				data.SerialNo		= Twix_TextFunctions.clean( cursor.getString(6) );
				
				data.verified		= ( (Twix_TextFunctions.clean(cursor.getString(7)).contentEquals("Y"))
											|| (Twix_TextFunctions.clean(cursor.getString(8)).length() > 0) );
				data.OutOfServiceFlag = (cursor.getInt(9) > 0);
				
				ll.addView( createRow( data ) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	    }
    
    private class EquipmentData
    	{
    	int EquipmentId;
    	String EqCat;
    	String UnitNo;
    	String AreaServed;
    	String Manufacturer;
    	String Model;
    	String SerialNo;
    	boolean verified;
    	boolean OutOfServiceFlag;
    	}
    
    public LinearLayout createRow(EquipmentData data)
	    {
    	LinearLayout row = createRow();
        row.setTag(data);
        
        row.addView( createTV(data.EqCat,		0.6f, true, data.OutOfServiceFlag) );
        row.addView( createTV(data.UnitNo,		0.6f, false, data.OutOfServiceFlag) );
        row.addView( createTV(data.AreaServed,	0.8f, false, data.OutOfServiceFlag) );
        row.addView( createTV(data.Manufacturer,0.8f, false, data.OutOfServiceFlag) );
        row.addView( createTV(data.Model,		1.0f, false, data.OutOfServiceFlag) );
        row.addView( createTV(data.SerialNo,	1.0f, false, data.OutOfServiceFlag) );
        row.addView( createCB(data.verified,	0.5f) );
        
		return row;
	    }
    
    private LinearLayout createRow()
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ret.setLayoutParams(params);
		ret.setOrientation(LinearLayout.HORIZONTAL);
		ret.setBackgroundResource(R.drawable.clickable_bg);
		ret.setClickable(true);
		ret.setOnClickListener(rowClick);
		
		return ret;
		}
    
    private TextView createTV(String text, float weight, boolean firstCol, boolean OutOfServiceFlag )
		{
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
		params.weight = weight;
		params.setMargins(2, 2, 2, 2);
		tv.setLayoutParams(params);
		
		tv.setText(text);
		tv.setTextSize(Twix_Theme.subSize);
    	tv.setPadding(10, 10, 10, 10);
    	tv.setMaxLines(1);
    	if( OutOfServiceFlag )
    		{
    		tv.setTextColor(Twix_Theme.disabledColor);
    		if( firstCol )
    			tv.setBackgroundColor(0xFFBFBFBF); // Slightly brighter than the disabled color
    		else
    			tv.setBackgroundColor(Twix_Theme.disabledColorBG);
    		}
    	else
    		{
    		tv.setTextColor(Twix_Theme.headerValue);
    		if( firstCol )
    			tv.setBackgroundColor(Twix_Theme.sortAsc);
    		else
    			tv.setBackgroundColor(Twix_Theme.headerBG);
    		}
    	
		return tv;
		}
    
    private LinearLayout createCB(boolean checked, float weight)
    	{
    	// Wrapper so the CheckBox can be centered
    	LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
		params.weight = weight;
		params.setMargins(2, 2, 2, 2);
		ret.setLayoutParams(params);
		ret.setGravity(Gravity.CENTER);
		
    	// Actual CheckBox
    	CheckBox cb = new CheckBox(mContext);
    	LinearLayout.LayoutParams paramsCB =
    			new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
    	cb.setLayoutParams(paramsCB);
		
		cb.setChecked(checked);
		cb.setClickable(false);
		
		ret.addView(cb);
		return ret;
    	}
    
    public void Update_Equipment()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_Category).setBackgroundColor(Twix_Theme.sortAsc);
    	
    	readSQL();
    	}
    
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_Category).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_UnitNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_AreaServed).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Manufacturer).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Model).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_SerialNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Verified).setBackgroundColor(Twix_Theme.sortNone);
    	}
    
    public void scanResult( String barCode )
    	{
    	String sqlQ = "SELECT equipmentId FROM equipment " +
    			"WHERE barCodeNo = '" + barCode + "' " +
    				"AND serviceAddressId = '" + serviceAddressId + "'";
    	
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
			{
			cursor.close();
			}
    	
		if( id > 0 )
			{
	    	Intent intent;
	    	String intentName;
	    	
	    	intent = new Intent(mContext, Twix_AgentEquipmentTabHost_Edit.class);
	    	intentName = "Twix_AgentEquipmentTabHost_Edit";
	    	
	    	intent.putExtra("equipmentId", id);
	    	intent.putExtra("siteName", addressText);
	    	((Twix_TabActivityGroup) mContext).startChildActivity(intentName, intent);
			}
		else
			Toast.makeText(mContext, "No equipment registered with that barcode.", Toast.LENGTH_LONG).show();
		}
    
    private void setClickListeners()
	    {
	    findViewById(R.id.ScannerLookup).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	// QR Droid Barcode Scanner
              Intent intent = new Intent( "la.droid.qr.scan" );
              intent.setPackage("la.droid.qr");
               intent.putExtra( "la.droid.qr.complete", true);
                
             //   Intent intent = new Intent( "com.qrcodescanner.barcodescanner" );
           // 	intent.setPackage("com.qrcodescanner.barcodescanner");
          //  	intent.putExtra( "com.qrcodescanner.barcodescanner", true);
                try
                	{
                	((Twix_TabActivityGroup)mContext)
                		.startActivityForResult( intent, Twix_AgentActivityGroup3.SCAN_BARCODE_READ );
                	}
                catch (ActivityNotFoundException e)
                	{
                	Toast.makeText(mContext,
                			"Cannot Find QR Droid Bar Code scanner. " +
                			"Please install QR Scanner from the Android Market and try again",
                			Toast.LENGTH_LONG).show();
                	}
            	}
        	});
	    
	    findViewById(R.id.Sort_Category).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "CategoryDesc" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "CategoryDesc";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_UnitNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "UnitNo" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "UnitNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_AreaServed).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "AreaServed" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "AreaServed";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Manufacturer).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "Manufacturer" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "Manufacturer";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Model).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "Model" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "Model";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_SerialNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "SerialNo" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "SerialNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Verified).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "verified, verifiedByEmpno" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		}
            	CurrentSearch = "verified, verifiedByEmpno";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.AddEquipmentButton).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	if( serviceAddressId == 0 )
            		return;
            	
            	Intent intent = new Intent(mContext, Twix_AgentEquipmentTabHost_Edit.class);
            	intent.putExtra("serviceAddressId", serviceAddressId);
            	intent.putExtra("siteName", addressText);
                ((Twix_TabActivityGroup) mContext).startChildActivity("Twix_AgentEquipmentTabHost_Edit", intent);
                }
        	});
	    
	    rowClick = new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	Intent intent = new Intent(mContext, Twix_AgentEquipmentTabHost_Edit.class);
            	EquipmentData data = (EquipmentData) v.getTag();
            	intent.putExtra("equipmentId", data.EquipmentId);
            	intent.putExtra("siteName", addressText);
                ((Twix_TabActivityGroup) mContext).startChildActivity("Twix_AgentEquipmentTabHost_Edit", intent);
            	}
        	}
    	;
	    }
    
	public void backPage(View v)
		{
		finish();
		}
	}