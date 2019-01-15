package com.twix_agent;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentSiteHistory_Tags
 * 
 * Purpose: Displays a list service tag history at a service address. When selecting a service address, the details
 * 			of the service tag are displayed.
 * 
 * Relevant XML: sitehistory_servicetags.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSiteHistory_Tags extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private Twix_SQLite db;
	
	private int curCol = 0;
	private boolean desc = false;
	private String CurrentSearch = "st.serviceTagId";
	private LinearLayout tl;
	
	private int serviceAddressId;
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.sitehistory_servicetags, null);
		this.setContentView( viewToLoad );
        
		serviceAddressId = getIntent().getIntExtra("serviceAddressId", 0);
		((TextView)findViewById(R.id.History_SiteName)).setText(getIntent().getStringExtra("SiteName"));
		
		app = (Twix_Application) getApplication();
	    db = app.db;
	    Twix_Theme = app.Twix_Theme;
	    final Button button = (Button) findViewById(R.id.B1);
	    
	    button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        
				TextView t6 = (TextView)findViewById(R.id.test_6months);
				t6.setVisibility(View.GONE);
				Update_Page2();
				
			
	        }
	        
	    });
        tl = (LinearLayout) findViewById(R.id.TagBuild);
        tl.setBackgroundColor(Twix_Theme.lineColor);
        
    	Update_Page();
    	}
    
    private class HistoryData
	    {
	    int serviceTagId;
	    String tenant;
	    String batchNo;
	    String jobNo;
	    String serviceType;
	    String disposition;
	    String serviceDate;
	    String dispatchDesc;
	    }
    
    public void readSQL()
	    {
    	tl.removeAllViews();
    	
    	Date d = new Date(System.currentTimeMillis()-(15552000000L));
    	String sqlQ = "SELECT st.serviceTagId, st.tenant, st.batchNo, st.jobNo, " +
    						"st.serviceType, st.disposition, st.serviceDate, st.description " + 
    					"FROM serviceTag as st " +
	    					"WHERE st.serviceAddressId = " + serviceAddressId + " " +
    					"and st.serviceDate > '"+ d +"' "+
	    					"ORDER BY " + CurrentSearch;
    	
    	if( !desc )
    		sqlQ += " asc";
    	else
    		sqlQ += " desc";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	HistoryData data;
    	int index;
		if (cursor.moveToFirst())
			{
			float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
			
			do
				{
				index = 0;
				data = new HistoryData();
				data.serviceTagId	= cursor.getInt(0);
				data.tenant			= cursor.getString(1);
				data.batchNo		= cursor.getString(2);
				data.jobNo			= Twix_TextFunctions.clean(cursor.getString(3)).replaceAll("(TTCA)", "");
				data.serviceType	= cursor.getString(4);
				data.disposition	= Twix_TextFunctions.clean(cursor.getString(5));
				if( data.disposition.contentEquals("R"))
					data.disposition = "Must Return";
				else
					data.disposition = "Call Complete";
				data.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(6));
				data.dispatchDesc	= cursor.getString(7);
				
				createRow( data, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	    }
  //Function to fetch history from a specified date(from date picker)
    public void readSQL2() throws java.text.ParseException
    {
 
	tl.removeAllViews();
	//Current Date is fetched using sysem.currentTimeMillis(). We are substracting 15552000000 (6 Months date i.e. 180*24*60*60*1000) 	
    //Date d = new Date(System.currentTimeMillis());
	DatePicker dobPicker;
	dobPicker = (DatePicker)findViewById(R.id.date_picker1);
	//TextView text1;
	dobPicker = (DatePicker)findViewById(R.id.date_picker1);
	//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    Integer dobYear = dobPicker.getYear()-1900;
    Integer dobMonth = dobPicker.getMonth();
    Integer dobDate = dobPicker.getDayOfMonth();
   
    Date d1 = new Date(dobYear,dobMonth,dobDate);
    
   //Toast.makeText(getApplicationContext(), "changed date is " + d1,  Toast.LENGTH_SHORT).show();
    
   String sqlQ = "SELECT st.serviceTagId, st.tenant, st.batchNo, st.jobNo, " +
			"st.serviceType, st.disposition, st.serviceDate, st.description " + 
		"FROM serviceTag as st " +
			"WHERE st.serviceAddressId = " + serviceAddressId + " " +
		"and st.serviceDate >= '"+ d1 +"' " +  
		"ORDER BY " + CurrentSearch;

	if( !desc )
		sqlQ += " asc";
	else
		sqlQ += " desc";
	
	Cursor cursor = db.rawQuery(sqlQ); 
	HistoryData data;
	int index;
	if (cursor.moveToFirst())
		{
		float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
		
		do
			{
			index = 0;
			data = new HistoryData();
			data.serviceTagId	= cursor.getInt(0);
			data.tenant			= cursor.getString(1);
			data.batchNo		= cursor.getString(2);
			data.jobNo			= Twix_TextFunctions.clean(cursor.getString(3)).replaceAll("(TTCA)", "");
			data.serviceType	= cursor.getString(4);
			data.disposition	= Twix_TextFunctions.clean(cursor.getString(5));
			if( data.disposition.contentEquals("R"))
				data.disposition = "Must Return";
			else
				data.disposition = "Call Complete";
		
			
			
			data.serviceDate	= Twix_TextFunctions.DBtoNormal(cursor.getString(6));
			data.dispatchDesc	= cursor.getString(7);
			
			createRow( data, weight );
			
			}
		while (cursor.moveToNext());
		}
	else
		noResults();
	if (cursor != null && !cursor.isClosed())
		cursor.close();
    }

    public void createRow(final HistoryData data, float[] weight)
	    {
	    final LinearLayout row = createRow();
    	row.setTag(data);
    	//row.setBackgroundResource(R.drawable.clickable_bg);
    	
    	row.addView(createTextView( data.serviceTagId+"",	weight[0], true ));
    	row.addView(createTextView( data.tenant,			weight[1], false ));
    	row.addView(createTextView( data.batchNo,			weight[2], false ));
    	row.addView(createTextView( data.jobNo,				weight[3], false ));
    	row.addView(createTextView( data.serviceType,		weight[4], false ));
    	row.addView(createTextView( data.disposition,		weight[5], false ));
    	row.addView(createTextView( data.serviceDate,		weight[6], false ));
    	row.addView(createTextView( data.dispatchDesc,		weight[7], false ));
    	
    	row.setClickable(true);
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	Integer s=data.serviceTagId;
            	v.getBackground();
            	v.setBackgroundColor(Color.YELLOW);
            	Intent intent = new Intent(mContext, Twix_AgentSiteHistory_TagDetail.class);
            	HistoryData data = (HistoryData)v.getTag();
            	intent.putExtra("serviceTagId", data.serviceTagId);
            	((Twix_TabActivityGroup)mContext).startChildActivity("Twix_AgentSiteHistory_TagDetail", intent);
                }
        	});
        
		tl.addView(row);
	    }
    
    private LinearLayout createRow()
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
     	LinearLayout row = new LinearLayout(mContext);
     	row.setOrientation(LinearLayout.HORIZONTAL);
     	row.setLayoutParams(params);
     	
     	return row;
    	}
    
    private TextView createTextView(String text, float weight, boolean firstCol)
    	{
    	LinearLayout.LayoutParams params =
    			new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
    	params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	tv.setTextColor(Twix_Theme.headerValue);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setPadding(10, 10, 10, 10);
		if( firstCol)
			tv.setBackgroundColor(Twix_Theme.sortAsc);
		else
			tv.setBackgroundColor(Twix_Theme.headerBG);
		
    	return tv;
    	}
    
    private void noResults()
    	{
    	LinearLayout row = createRow();
    	
    	row.addView(createTextView( "No Service Tag History Found.", 1f, false ));
    	
		tl.addView(row);
    	}
    
    public void Update_Page()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_TagNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	readSQL();
    	}
    public void Update_Page2()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_TagNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	try
			{
			readSQL2();
			}
		catch ( ParseException e )
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
    	}
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_TagNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Tenant).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_BatchNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_ServiceType).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Disposition).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Date).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Desc).setBackgroundColor(Twix_Theme.sortNone);
    	}
    
    private void setClickListeners()
	    {
	    findViewById(R.id.Sort_TagNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 0 )
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
            		curCol = 0;
            		}
            	CurrentSearch = "st.serviceTagId";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Tenant).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 1 )
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
            		curCol = 1;
            		}
            	CurrentSearch = "st.tenant";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_BatchNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 2 )
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
            		curCol = 2;
            		}
            	CurrentSearch = "st.batchNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_JobNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 3 )
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
            		curCol = 3;
            		}
            	CurrentSearch = "st.jobNo";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_ServiceType).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 4 )
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
            		curCol = 4;
            		}
            	CurrentSearch = "st.serviceType";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Disposition).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 5 )
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
            		curCol = 5;
            		}
            	CurrentSearch = "st.disposition";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Date).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 6 )
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
            		curCol = 6;
            		}
            	CurrentSearch = "st.serviceDate";
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Desc).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( curCol == 7 )
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
            		curCol = 7;
            		}
            	CurrentSearch = "st.description";
            	readSQL();
                }
        	});
	    }
    
	}