package com.twix_agent;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;

/*******************************************************************************************************************
 * Class: Twix_AgentTab3
 * 
 * Purpose: Highest level activity for Tab 3 (Site Info). This allows the user to select from a list of service
 * 			addresses and view or edit their details. Also allows the user to search and download service addresses
 * 			from the server (See Twix_AgentSiteSearch.java for details).
 * 
 * Relevant XML: tab3.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentTab3 extends Activity
	{
	private Context mContext;
	private boolean readOnly;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private LinearLayout tl;
	private Twix_SQLite db;
	
	private String CurrentSearch = "SiteName";
	private boolean desc = false;
	private int curCol = 0;
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab3);
        
        // Set the context
        mContext = getParent();
        
        tl = (LinearLayout) findViewById(R.id.ServiceAddressTable_Results);
        
        app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        setClickListeners();
        
        clearBgs();
        
        findViewById(R.id.ServiceAddressTable).setBackgroundColor(Twix_Theme.lineColor);
        findViewById(R.id.ScrollView01).setBackgroundColor(Twix_Theme.lineColor);
        findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortAsc);
        
        onSearch();
    	}
    
    public void onSearch()
    	{
    	readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
    	if( readOnly )
    		{
    		findViewById(R.id.DownloadButton).setVisibility(View.GONE);
    		}
    	tl.removeAllViews();
    	
    	String sqlQ = "SELECT serviceAddressId, siteName, address1, address2, city, state, zip, buildingNo " +
    				"FROM ServiceAddress ORDER BY " + CurrentSearch;
    	if( !desc )
    		sqlQ += " asc";
    	else
    		sqlQ += " desc";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	ServiceAddressData data;
    	float weight[] = { 1.2f, 1.3f, 0.6f, 0.3f, 0.3f, 0.6f };
    	
		if (cursor.moveToFirst())
			{
			do
				{
				data = new ServiceAddressData();
				data.serviceAddressId	= cursor.getInt(0);
				data.siteName			= cursor.getString(1);
				data.address1			= cursor.getString(2);
				data.address2			= cursor.getString(3);
				data.city				= cursor.getString(4);
				data.state				= cursor.getString(5);
				data.zip				= cursor.getString(6);
				data.buildingNo			= cursor.getString(7);
				
				createRow( data, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
    	}
    
    private void noResults()
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	
    	LinearLayout row = new LinearLayout(this);
    	row.setLayoutParams(params);
    	
    	row.addView( createTV( "No Sites Available. Try Syncing or Downloading Sites.", 1, Twix_Theme.headerBG) );
    	
		tl.addView(row);
    	}
    
    private class ServiceAddressData
	    {
	    int serviceAddressId;
	    
	    String siteName;
	    String address1;
	    String address2;
	    String city;
	    String state;
	    String zip;
	    String buildingNo;
	    }
    
    public void createRow(final ServiceAddressData data, float weight[])
	    {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	
    	LinearLayout row = new LinearLayout(this);
    	row.setLayoutParams(params);
    	
    	row.addView( createTV( data.siteName, weight[0], Twix_Theme.headerBG) );
    	String address = data.address1;
    	if( data.address2 != null && data.address2.length() > 0 )
    		address += "\n" + data.address2;
    	row.addView( createTV( address, weight[1], Twix_Theme.sub2BG) );
    	row.addView( createTV( data.city, weight[2], Twix_Theme.sub2BG) );
    	row.addView( createTV( data.state, weight[3], Twix_Theme.sub2BG) );
    	row.addView( createTV( data.zip, weight[4], Twix_Theme.sub2BG) );
    	row.addView( createTV( data.buildingNo, weight[5], Twix_Theme.sub2BG) );
    	
    	//Create the row OnClick
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	ServiceAddressData data = (ServiceAddressData) v.getTag();
            	
            	Intent intent = new Intent(mContext, Twix_AgentSiteSummary.class);
            	intent.putExtra("serviceAddressId", data.serviceAddressId);
            	intent.putExtra("SiteName", data.siteName);
                
                Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)mContext;
                parentActivity.startChildActivity("Twix_AgentSiteSummary", intent);
                }
        	});
        
    	row.setTag(data);
    	row.setBackgroundResource(R.drawable.clickable_bg);
        
		tl.addView(row);
	    }
    
    private TextView createTV(String s, float weight, int bg)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			0, LayoutParams.FILL_PARENT);
    	params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(this);
    	tv.setLayoutParams(params);
    	tv.setText(s);
    	
    	tv.setTextSize(Twix_Theme.headerSize);
    	tv.setBackgroundColor(bg);
    	tv.setTextColor(Twix_Theme.headerValue);
	    tv.setGravity(Gravity.CENTER_VERTICAL);
	    tv.setPadding(10, 10, 10, 10);
    	
    	return tv;
    	}
    
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Address).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_City).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_State).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Zip).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_BuildingNo).setBackgroundColor(Twix_Theme.sortNone);
    	}
    
    private void setClickListeners()
	    {
	    findViewById(R.id.Sort_SiteName).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "SiteName";
            	onSearch();
                }
        	});
	    
	    findViewById(R.id.Sort_Address).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "Address1";
            	onSearch();
                }
        	});
	    
	    findViewById(R.id.Sort_City).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "City";
            	onSearch();
                }
        	});
	    
	    findViewById(R.id.Sort_State).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "State";
            	onSearch();
                }
        	});
	    
	    findViewById(R.id.Sort_Zip).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "Zip";
            	onSearch();
                }
        	});
	    
	    findViewById(R.id.Sort_BuildingNo).setOnClickListener(new  OnClickListener()
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
            	CurrentSearch = "BuildingNo";
            	onSearch();
                }
        	});
	    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
		{
		return false;
		}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    	{
    	return false;
    	}
    
    
    /*public void downloadjobdocs(View v)
    	{
    	Intent intent = new Intent(mContext, Twix_Agentjobdocs.class);
        
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)mContext;
        parentActivity.startChildActivity("Twix_Agentjobdocs", intent);
    	}*/
    public void downloadlargefiles(View v)
    	{
    	Intent intent = new Intent(mContext, webview.class);
        
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)mContext;
        parentActivity.startChildActivity("webview", intent);
    	}
    public void downloadSites(View v)
    	{
    	Intent intent = new Intent(mContext, Twix_AgentSiteSearch.class);
        
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)mContext;
        parentActivity.startChildActivity("Twix_AgentSiteSearch", intent);
    	}
    
	}