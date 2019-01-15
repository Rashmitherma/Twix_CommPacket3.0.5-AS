package com.twix_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.twix.*;
import com.twix.ServerResponse.SearchData;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

/*******************************************************************************************************************
 * Class: Twix_AgentSiteSearch
 * 
 * Purpose: Allows the user to search for service addresses and download their details. Service addresses include
 * 			equipment, contacts, and previous service tags. When the user searches, they are shown a list of
 * 			service addresses (up to 20). The user can then select which service addresses they want to download.
 * 			Tapping download proceeds to download selected sites and their details.
 * 
 * Relevant XML: site_search.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSiteSearch extends Activity
	{
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private Context mContext;
	
	private InputMethodManager imm;
	private List<View> masterList;
	private LinearLayout table;
	
	private boolean desc = false;
	private int curCol = 0;
	
	
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.site_search);
        
        mContext = getParent();
        
        app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
        
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        
        masterList = new ArrayList<View>();
        
        setClickListeners();
        
        clearBgs();
        
        table = (LinearLayout) findViewById(R.id.Search_Results);
        findViewById(R.id.SearchHeader).setBackgroundColor(Twix_Theme.lineColor);
        findViewById(R.id.ScrollView01).setBackgroundColor(Twix_Theme.lineColor);
        
        findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortAsc);
    	}
    
    private List<Integer> getCurrentAddresses()
	    {
	    List<Integer> knownAddressIds = new ArrayList<Integer>();
	   
	    Cursor cursor = db.rawQuery("SELECT	serviceAddressId FROM serviceAddress");
		if (cursor.moveToFirst())
			{
			do
				{
				knownAddressIds.add( cursor.getInt(0) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
	    return knownAddressIds;
	    }
    
    public void search(View v)
    	{
    	((TextView)findViewById(R.id.Warning)).setVisibility(View.INVISIBLE);
    	
    	SiteSearch search = new SiteSearch();
    	search.knownServiceAddressIds = getCurrentAddresses();
    	search.siteName 	= ((EditText) findViewById(R.id.Input_SiteName)).getText().toString();
    	search.address		= ((EditText) findViewById(R.id.Input_Address)).getText().toString();
    	search.city			= ((EditText) findViewById(R.id.Input_City)).getText().toString();
    	search.buildingNo	= ((EditText) findViewById(R.id.Input_BuildingNo)).getText().toString();
    		
    	if( !search.isEmpty() )
    		{
    		app.SiteSearch(false, search);
    		}
    	
    	}
    
    public void postResults(ArrayList<ServerResponse.SearchData> data)
    	{
    	if( this.getCurrentFocus() != null ) 
    		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		
    	masterList.clear();
		table.removeAllViews();
		
		float weight[] = { 0.5f, 2f, 2f, 1.5f, 0.5f, 1f, 1f };
		LinearLayout row;
		
		int size = data.size();
		warnSearchLimit( size >= 20 );
		for( int i = 0; i < size; i++ )
			{
			row = createRow(data.get(i), weight);
			masterList.add(row);
			}
		
		updateResults();
    	}
    
    private void updateResults()
    	{
    	sortResults();
    	
    	table.removeAllViews();
    	int size = masterList.size();
    	for( int i = 0; i < size; i++ )
    		table.addView(masterList.get(i));
    	}
    
    private void warnSearchLimit(boolean warn)
    	{
    	if( warn )
    		((TextView)findViewById(R.id.Warning)).setVisibility(View.VISIBLE);
    	else
    		((TextView)findViewById(R.id.Warning)).setVisibility(View.INVISIBLE);
    	}
    
    private void sortResults()
    	{
    	Collections.sort(masterList, new ListSorter(curCol, desc));
    	}
    
    private class ListSorter implements Comparator<View>
    	{
    	int sortCol = 0;
    	boolean desc = false;
    	ListSorter( int col, boolean dir )
	    	{
	    	sortCol = col;
	    	desc = dir;
	    	}
		@Override
		public int compare(View v1, View v2)
			{
			ServerResponse.SearchData data1 = (SearchData) v1.getTag();
			ServerResponse.SearchData data2 = (SearchData) v2.getTag();
			
			int result = 0;
			
			switch(sortCol)
				{
				case 0:
					result = (data1.siteName.compareTo(data2.siteName));
					break;
				case 1:
					result = (data1.address.compareTo(data2.address));
					break;
				case 2:
					result = (data1.city.compareTo(data2.city));
					break;
				case 3:
					result = (data1.state.compareTo(data2.state));
					break;
				case 4:
					result = (data1.zip.compareTo(data2.zip));
					break;
				}
			
			if( desc )
				return -result;
			return result;
			}
	    }
    
    public LinearLayout createRow(ServerResponse.SearchData data, float weight[])
	    {
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	LinearLayout row = new LinearLayout(this);
    	row.setLayoutParams(params);
    	row.setTag( data );
    	row.setOrientation(LinearLayout.HORIZONTAL);
    	
    	params = new LinearLayout.LayoutParams(
    			0, LayoutParams.FILL_PARENT);
    	params.weight = weight[0];
    	params.setMargins(2, 2, 2, 2);
    	CheckBox cb =  new CheckBox(this);
    	cb.setLayoutParams(params);
    	row.addView(cb);
    	
    	row.addView(createTV(data.siteName, weight[1], Twix_Theme.headerBG));
    	row.addView(createTV(data.address, weight[2], Twix_Theme.sub2BG));
    	row.addView(createTV(data.city, weight[3], Twix_Theme.sub2BG));
    	row.addView(createTV(data.state, weight[4], Twix_Theme.sub2BG));
    	row.addView(createTV(data.zip, weight[5], Twix_Theme.sub2BG));
    	row.addView(createTV(data.buildingNo, weight[6], Twix_Theme.sub2BG));
    	
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	CheckBox cb = (CheckBox) ((LinearLayout)v).getChildAt(0);
            	if( cb.isChecked())
            		{
            		cb.setChecked(false);
            		}
            	else
            		cb.setChecked(true);
                }
        	});
    	row.setBackgroundResource(R.drawable.clickable_bg);
        
		return row;
	    }
    
    private TextView createTV(String s, float weight, int bg)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			0, LayoutParams.FILL_PARENT);
    	params.weight = weight;
    	params.setMargins(2, 2, 2, 2);
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(s);
    	tv.setBackgroundColor(bg);
    	tv.setTextColor(Twix_Theme.headerValue);
    	tv.setTextSize(Twix_Theme.headerSize);
	    tv.setGravity(Gravity.CENTER_VERTICAL);
	    tv.setTypeface(Typeface.MONOSPACE);
	    tv.setPadding(10, 14, 10, 14);
	    
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
            	updateResults();
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
            	updateResults();
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
            	updateResults();
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
            	updateResults();
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
            	updateResults();
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
            	updateResults();
                }
        	});
	    }
	
	public void download(View v)
		{
		// Make sure there are rows to select
		int size = masterList.size();
		if( size < 1 )
			{
			Toast.makeText(this, "Search for Addresses First", Toast.LENGTH_SHORT).show();
			return;
			}
		
		// Get a list of checked rows and build up their serviceAddressIds
		LinearLayout row;
		CheckBox cb;
		ServerResponse.SearchData data;
		ArrayList<Integer> list = new ArrayList<Integer>();
		for( int i = 0; i < size; i++ )
			{
			row = (LinearLayout)masterList.get(i);
			cb = (CheckBox)row.getChildAt(0);
			if( cb.isChecked() )
				{
				data = (SearchData) row.getTag();
				list.add( data.serviceAddressId );
				}
			}
		
		// Build the list of integers to download. Only if we have a list.
		size = list.size();
		if( size < 1 )
			Toast.makeText(this, "No rows selected", Toast.LENGTH_SHORT).show();
		else
			downloadAddresses( list );
		
		}
	
	private void downloadAddresses( ArrayList<Integer> list )
		{
		app.SiteDownload(false, list);
		}
	
	// Back Button Click Listener
	public void backPage(View v)
		{
		finish();
		}
	}