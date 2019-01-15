package com.twix_agent;

//import com.twix_agent.Twix_AgentContacts.ContactSQLData;

//import com.twix_agent.Twix_AgentContacts.ContactSQLData;





import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentSiteSummary
 * 
 * Purpose: Provides basic details on a site. A user can create an open tag from the site summary, automatically
 * 			linking the new tag to the site. The site then branches to three main functions.
 * 			Main Functions:
 * 			- Site History: Displays previous service tags for the site (See Twix_AgentSiteHistory_Tags.java for details)
 * 			- Site Contacts: Allows editing and viewing of site contacts (See Twix_AgentContacts.java for details)
 * 			- Site Equipment: Allows editing and view of site equipment (See Twix_AgentEquipment.java for details)
 * 
 * Relevant XML: site_summary.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentSiteSummary extends Activity
	{
	private boolean readOnly;
	private Twix_Application app;
	private Twix_SQLite db;
	private int serviceAddressId;
	private String siteName;
	private Context mContext;
	private Twix_AgentTheme Twix_Theme;
	private LinearLayout NavigateMapsButton;
	private String NavigationAddress;
	
	public void onCreate(Bundle savedInstanceState)
	   	{
	    super.onCreate(savedInstanceState);
	    mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.site_summary, null);
		this.setContentView( viewToLoad );
	    
		app = (Twix_Application) this.getApplication();
	    db = app.db;
	    Twix_Theme = app.Twix_Theme;
	    
	    readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
	    
	    if( readOnly )
	    	findViewById(R.id.CreateOpenTag).setVisibility(View.GONE);
	    
	    siteName = getIntent().getStringExtra("SiteName");
	    serviceAddressId = getIntent().getIntExtra("serviceAddressId", 0);
	    TextView tv = ((TextView)findViewById(R.id.site_title));
	    tv.setText("Site Summary for " + siteName);
	    
	    Button bn = (Button) findViewById(R.id.CreateOpenTag);
	    bn.setOnClickListener(new OnClickListener()
	    	{
			@Override
			public void onClick(View v)
				{
				if( checkPages() )
					{
					newTag( );
					}
				else
					{
					promptPages();
					}
				}
	    	});
	    
	    Button btnsave = (Button) findViewById(R.id.Save);
	    btnsave.setOnClickListener(new OnClickListener()
	    	{@Override
			public void onClick(View v){
			   savenotes();
				
	    	}});
	    readSQL();
	    readSQL2();
	    buildTenants();
	   	
	   	NavigateMapsButton = (LinearLayout) this.findViewById(R.id.NavigateMaps);
	   	NavigateMapsButton.setOnClickListener(new OnClickListener()
	   		{
			@Override
			public void onClick(View v)
				{
				try
					{
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse("http://maps.google.com/maps?daddr=" + NavigationAddress) );
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(intent);
					}
				catch (Exception e)
					{
					Toast.makeText(Twix_AgentSiteSummary.this,
							"Error attempting to open Navigate. Please make sure Google Maps is installed.",
							Toast.LENGTH_LONG).show();
					}
				}
			});
	   	}
	
	private void readSQL()
	    {
	    if( serviceAddressId == 0 )
	    	{
	    	return;
	    	}
	    
    	String sqlQ = "SELECT serviceAddress.address1, serviceAddress.address2, " +
    					"serviceAddress.city, serviceAddress.state, serviceAddress.zip, serviceAddress.buildingNo, " +
    					"serviceAddress.note " + 
    					"FROM serviceAddress WHERE serviceAddressId = " + serviceAddressId;
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	String s;
    	
		if (cursor.moveToFirst())
			{
			s = cursor.getString(0) + " " + cursor.getString(1);
			((TextView)findViewById(R.id.address1)).setText(s);
			NavigationAddress = s;
			
			s = cursor.getString(2) + ", " + cursor.getString(3) + " " + cursor.getString(4);
			((TextView)findViewById(R.id.address2)).setText(s);
			NavigationAddress += s;
			
			s = cursor.getString(5);
			((TextView)findViewById(R.id.buildingNo)).setText(s);
			
			s = cursor.getString(6);
			((TextView)findViewById(R.id.notes)).setText(s);
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	    }
	private void readSQL2()
	    {
	    if( serviceAddressId == 0 )
	    	{
	    	return;
	    	}
	    
    	String sqlQ = "SELECT noteid,serviceaddressid,notes  " +
    					"FROM notes WHERE serviceaddressid = " + serviceAddressId;
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	String s;
    	
		if (cursor.moveToFirst())
			{
			
			s = cursor.getString(2);
			((TextView)findViewById(R.id.note2)).setText(s);
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	    }
	
	public void gotoHistory()
		{
		Intent intent = new Intent(getParent(), Twix_AgentSiteHistory_Tags.class);
    	intent.putExtra("serviceAddressId", serviceAddressId);
    	intent.putExtra("SiteName", siteName);
        
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)getParent();
        parentActivity.startChildActivity("Twix_AgentSiteHistory_Tags", intent);
		}
	private class notesdata
	{
	int noteid=0;
	int serviceAddressId=0;
	String notes=null;
	String modified=null;
	
	}
	public void savenotes()
		{
		//EditText  note2;
		EditText note2 = (EditText)findViewById(R.id.note2);
		String n = note2.getText().toString();
		int s = serviceAddressId;
		//Context context = getApplicationContext();
		//int duration = Toast.LENGTH_SHORT;
		//Toast toast = Toast.makeText(context, "sa=" +serviceAddressId, duration);
		//toast.show();
		String sqlQ = "select noteid, serviceaddressid,notes from notes" +
				//" WHERE serviceaddressid = ' " + serviceAddressId + " ' and modified = 'Y' ";
		
				" WHERE serviceaddressid = " + serviceAddressId;
				
		Cursor cursor = db.rawQuery(sqlQ);
		notesdata data;
		
		if (cursor.moveToFirst())
			{
			
				//note2.setText(cursor.getString(2));
				//String v = cursor.getString(2).toString();
				data = new notesdata();
				data.noteid= cursor.getInt(0);
				data.serviceAddressId			= cursor.getInt(1);
				data.notes					= n;
				updateDB(data);
					       		
				
			}
		 else
		        	{
		        	data = new notesdata();
					data.noteid= db.newNegativeId("notes", "noteid");
					data.serviceAddressId			= serviceAddressId;
					data.notes					= n;
		        	insertDB(data);
		        	}	
			
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
		
	
		
		/*Cursor cursor2 = db.rawQuery(sqlQ);
		int p=cursor2.getCount();
		if(cursor2.getCount()>0)
			{
			Context context2 = getApplicationContext();
			int duration2 = Toast.LENGTH_SHORT;
			Toast toast2 = Toast.makeText(context2,"data found" , duration2);
			toast2.show();
			}
		else 
			{
			Context context3 = getApplicationContext();
			int duration3 = Toast.LENGTH_SHORT;
			Toast toast3 = Toast.makeText(context3, "data not found" , duration3);
			toast3.show();
			
			}
			}*/
     	/*notesdata ndata;
     	
		ndata = new notesdata();
		int p =  cursor2.getInt(0);
			Context context2 = getApplicationContext();
			int duration2 = Toast.LENGTH_SHORT;
			Toast toast2 = Toast.makeText(context2, "data found" +ndata.serviceAddressId, duration2);
			toast2.show();
			cursor2.close();
			*/
			
			
	public void updateDB( notesdata data )
		{
		ContentValues cv = new ContentValues();
		cv.put("serviceaddressid", data.serviceAddressId);
		cv.put("notes", data.notes);
		cv.put("modified", "Y" );
		db.update("notes", cv, "noteid", data.noteid);
		//String sql = "UPDATE notes SET notes = notes + 'data.notes' where noteid = 'data.noteid' ";
		//db.rawQuery(sql);
		//Context context2 = getApplicationContext();
		//int duration2 = Toast.LENGTH_SHORT;
		//Toast toast2 = Toast.makeText(context2,"data updated" , duration2);
		//toast2.show();
		}
		
	public void insertDB( notesdata data )
		{
		ContentValues cv = new ContentValues();
		cv.put("serviceaddressid", data.serviceAddressId );
		cv.put("notes", data.notes);
		cv.put("modified","Y");
		cv.put("noteid", db.newNegativeId("notes", "noteid") );
		
		db.db.insertOrThrow("notes", null, cv);
		Context context2 = getApplicationContext();
		int duration2 = Toast.LENGTH_SHORT;
		Toast toast2 = Toast.makeText(context2,"data inserted" , duration2);
		toast2.show();
		
		}
	public void gotoContacts()
		{
		Intent intent = new Intent(getParent(), Twix_AgentContacts.class);
    	intent.putExtra("serviceAddressId", serviceAddressId);
    	intent.putExtra("SiteName", siteName);
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)getParent();
        parentActivity.startChildActivity("Twix_AgentContacts", intent);
		}
	
	public void gotoEquipment()
		{
		Intent intent = new Intent(getParent(), Twix_AgentEquipment.class);
    	intent.putExtra("serviceAddressId", serviceAddressId);
    	intent.putExtra("SiteName", siteName);
        
        Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)getParent();
        parentActivity.startChildActivity("Twix_AgentEquipment", intent);
		}
	
    public void backPage(View v)
		{
		finish();
		}
	
    private void buildTenants()
    	{
    	LinearLayout ll = (LinearLayout) findViewById(R.id.TenantList);
    	
    	String sqlQ = "SELECT tenant " + 
    					"FROM serviceAddressTenant WHERE serviceAddressId = " + serviceAddressId;
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	LinearLayout row = null;
    	
		if (cursor.moveToFirst())
			{
			row = createRow(false);
			row.addView(addRowContent( "Tenants:" ));
			ll.addView(row);
			int col = 0;
			do
				{
				if( col % 4 == 0 )
					row = createRow(true);
				
				row.addView(addRowContent( cursor.getString(0) ));
				col++;
				if( col % 4 == 0 )
					ll.addView(row);
				}
			while(cursor.moveToNext());
			
			switch( col % 4  )
				{
				case 1:
					row.addView(addRowContent(""));
				case 2:
					row.addView(addRowContent(""));
				case 3:
					row.addView(addRowContent(""));
					ll.addView(row);
				}
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
    	}
    
    private LinearLayout createRow(boolean margin)
    	{
    	LinearLayout ret = new LinearLayout(mContext);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	if( margin )
    		params.setMargins(10, 3, 10, 3);
    	else
    		params.setMargins(3, 3, 3, 3);
    	
    	ret.setLayoutParams(params);
    	ret.setOrientation(LinearLayout.HORIZONTAL);
    	
    	
    	
    	return ret;
    	}
    
    private TextView addRowContent(String text)
    	{
    	TextView tv = new TextView(mContext);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			0, LayoutParams.FILL_PARENT);
    	params.weight = 1;
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	tv.setTextColor(Twix_Theme.headerText);
    	
    	return tv;
    	}
    
    /**
     *  Saving all Pages
	 */
	
	public void newTag()
		{
		Twix_AgentTabActivity tabActivity = (Twix_AgentTabActivity)((Twix_AgentActivityGroup3)mContext).getParent();
		tabActivity.getTabHost().setCurrentTab(1);
		
		LocalActivityManager manager = tabActivity.getLocalActivityManager();
		Twix_AgentActivityGroup2 act = (Twix_AgentActivityGroup2) manager.getActivity("tags");
		act.newOpenTag(0, serviceAddressId);
		}
	
	private void promptPages()
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    	{
		        switch (which)
			        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	String error = saveAllPages();
			        	if( error.length() < 1)
			        		{
			        		newTag();
			        		}
			        	else
			        		{
			        		AlertDialog alert = new AlertDialog.Builder(mContext).create();
			            	alert.setTitle("Error Saving Pages:");
			            	alert.setMessage( error );
			            	alert.setButton("Ok", new DialogInterface.OnClickListener()
			            		{  
			            		public void onClick(DialogInterface dialog, int which)
			            			{
			            			return;  
			            			}
			            		});
			            	alert.show();
			        		}
			            break;
		
			        case DialogInterface.BUTTON_NEGATIVE:
			        	// Do Nothing
			            break;
			        }
		    	}
			};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Would you like to save your current open tag?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		}
	
	private boolean checkPages()
		{
		TabActivity host = (TabActivity) ((Twix_TabActivityGroup)mContext).getParent();
		LocalActivityManager manager = host.getLocalActivityManager();

		Twix_TabActivityGroup group = (Twix_TabActivityGroup) manager.getActivity("tags");
		if (group != null)
			{
			if (group.activityExists("Twix_AgentOpenTagsTabHost"))
				return false;
			}
		
		return true;
		}
	
	private String saveAllPages()
		{
		return "";
		}
	}
