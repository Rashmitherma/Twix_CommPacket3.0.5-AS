package com.twix_agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_Agentjobdocsy_Tags
 * 
 * Purpose: Displays a list service tag history at a service address. When selecting a service address, the details
 * 			of the service tag are displayed.
 * 
 * Relevant XML: jdocs.xml
 * 
 * 
 * @author Rashmi Kulkarni, Therma Corp
 *
 ********************************************************************************************************************/
public class Twix_Agentjobdocs extends Activity
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
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.jdocs, null);
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
	    final Button button2 = (Button) findViewById(R.id.B2);
	    button2.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        
			TextView t6 = (TextView)findViewById(R.id.test_6months);
			t6.setVisibility(View.GONE);
			
			Update_Page3();
		
        }
        
    });
        tl = (LinearLayout) findViewById(R.id.TagBuild);
        tl.setBackgroundColor(Twix_Theme.lineColor);
        
    	Update_Page();
    	}
    
    private class HistoryData
	    {
	    
	   
	    String jobNo;
	    byte[] documentContents=null;
	    String documentName;
	    String documentTitle;
	    String dataSubmitted;
	    String jobsite;
	   
	    }
    private class HistoryData2
    {
      
    String jobNo;
    byte[] documentContents=null;
    String documentName;
    String documentTitle;
    String dataSubmitted;
    String jobsite;
   
    }
    private class HistoryData3
    {
      
    String jobNo;
    byte[] documentContents=null;
    String documentName;
    String documentTitle;
    String dataSubmitted;
    String jobsite;
   
    }
    
    public void readSQL()
	    {
    	
	    tl.removeAllViews();
    	//Date d = new Date(System.currentTimeMillis()-(15552000000L));
    	String sqlQ = "SELECT jobno,documentName,documentTitle, dataSubmitted, jobsite " +
    					"FROM jobdoc where documentTitle not like '%Twix%'"; 
    	
    	
    	
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
				data.jobNo	= cursor.getString(0);
				//data.documentContents=cursor.getBlob(1);
				 //File pdfFile = convertBytesToFile(data.documentContents);

				data.documentName			= cursor.getString(1);
				data.documentTitle	= cursor.getString(2);
				data.dataSubmitted = cursor.getString(3);
				data.jobsite = cursor.getString(4);
				createRow( data, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	    }
    
    public void readSQL2()
	    {
	    tl.removeAllViews();
    	EditText jobno=(EditText)findViewById(R.id.jobno);
    	Editable j = jobno.getText();
    	
        if(j.toString().equals("") || j.toString().equals(null))
        	{
        	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("You must enter JobNo in JobNo field to search by JobNo")
					.setCancelable(true)
					.setNeutralButton("Ok", new DialogInterface.OnClickListener()
						{
						public void onClick(DialogInterface dialog, int id)
							{
							dialog.dismiss();
							}
						});
			AlertDialog alert = builder.create();
			alert.show();
        	} 
        	else
        		{
    	String sqlQ = "SELECT jobno,documentName,documentTitle, dataSubmitted, jobsite " +
    					"FROM jobdoc where jobno =" +j+ 
    					" AND documentTitle not like '%Twix%'";
    	
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	HistoryData2 data2;
    	int index;
		if (cursor.moveToFirst())
			{
			float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
			
			do
				{
				index = 0;
				data2 = new HistoryData2();
				data2.jobNo	= cursor.getString(0);
				//data.documentContents=cursor.getBlob(1);
				 //File pdfFile = convertBytesToFile(data.documentContents);

				data2.documentName			= cursor.getString(1);
				data2.documentTitle	= (cursor.getString(2));
				data2.dataSubmitted = cursor.getString(3);
				data2.jobsite = cursor.getString(4);
				createRow2( data2, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			j.clear();
			cursor.close();
	    }
    
	    }
    public void readSQL3()
    	    {
    	    
	    tl.removeAllViews();
    	EditText sitename=(EditText)findViewById(R.id.sitename);
    	Editable sn = sitename.getText();
    	if(sn.toString().equals("") || sn.toString().equals(null))
        	{
        	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("You must enter SiteName in SiteName field to search by Sitename")
					.setCancelable(true)
					.setNeutralButton("Ok", new DialogInterface.OnClickListener()
						{
						public void onClick(DialogInterface dialog, int id)
							{
							dialog.dismiss();
							}
						});
			AlertDialog alert = builder.create();
			alert.show();
        	} 
        	else
        		{
    	String sqlQ = "SELECT jobno,documentName,documentTitle, dataSubmitted, jobsite " +
        			"From jobdoc where jobsite like '%" + sn + "%'" +
        			" ANd documentTitle not like '%Twix%'";
    	
    	
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	HistoryData3 data3;
    	int index;
		if (cursor.moveToFirst())
			{
			float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
			
			do
				{
				index = 0;
				data3 = new HistoryData3();
				data3.jobNo	= cursor.getString(0);
				//data.documentContents=cursor.getBlob(1);
				 //File pdfFile = convertBytesToFile(data.documentContents);

				data3.documentName			= cursor.getString(1);
				data3.documentTitle	= cursor.getString(2);
				data3.dataSubmitted = cursor.getString(3);
				data3.jobsite = cursor.getString(4);
				createRow3( data3, weight );
				}
			while (cursor.moveToNext());
			}
		else
			noResults();
		if (cursor != null && !cursor.isClosed())
			cursor.close();
	    }}
 // Converts the array of bytes into a File
    private File convertBytesToFile(byte [] byteToConvert){

        File fileToReturn = new File( Environment.getExternalStorageDirectory() + "/PDF-doc.pdf");
       long fsize=fileToReturn.length();
       Context context = getApplicationContext();
      
       int duration = Toast.LENGTH_SHORT;

       Toast toast = Toast.makeText(context, "size" +fsize, duration);
       toast.show();
        try{
 
            FileOutputStream fileOutputStream = new FileOutputStream(fileToReturn);
            fileOutputStream.write(byteToConvert);
            fileOutputStream.close();

        }catch(FileNotFoundException ex){
            ex.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return fileToReturn;
    }

    public void createRow(final HistoryData data, float[] weight)
	    {
	    final LinearLayout row = createRow();
    	row.setTag(data);
    	//row.setBackgroundResource(R.drawable.clickable_bg);
    	
    	row.addView(createTextView( data.jobNo,		weight[0], false ));
    	
    	row.addView(createTextView( data.documentName,		weight[1], false ));
    	row.addView(createTextView( data.documentTitle,		weight[2], false ));
    	row.addView(createTextView( data.dataSubmitted,		weight[3], false ));
        row.addView(createTextView( data.jobsite,		weight[4], false ));
    	
    	
    	row.setClickable(true);
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
                
            	String sqlQ = "SELECT jobno,documentContents,documentName from jobdoc " +
            			" where jobNo = "+ data.jobNo + 
            			" AND documentName like '%" + data.documentName + "%'" + 
            			" AND documentTitle like '%" + data.documentTitle + "%'";
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
        				data.jobNo	= cursor.getString(0);
        				data.documentContents=cursor.getBlob(1);
        				File pdfFile = convertBytesToFile(data.documentContents);

        				 Uri path = Uri.fromFile(pdfFile);

        				    // Parse the file into a uri to share with another application

        				    Intent newIntent = new Intent(Intent.ACTION_VIEW);
        				    newIntent.setDataAndType(path, "application/pdf");
        				    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        				    try{
        				        startActivity(newIntent);
        				    }catch(ActivityNotFoundException ex){
        				        ex.printStackTrace();
        				    }

        				      				
        				}
        			while (cursor.moveToNext());
        			}
        		else
        			noResults();
        		if (cursor != null && !cursor.isClosed())
        			cursor.close();
                }
        	});
        
		tl.addView(row);
	    }
    public void createRow2(final HistoryData2 data2, float[] weight)
	    {
	    final LinearLayout row = createRow();
    	row.setTag(data2);
    	//row.setBackgroundResource(R.drawable.clickable_bg);
    	
    	row.addView(createTextView( data2.jobNo,		weight[0], false ));
    	
    	row.addView(createTextView( data2.documentName,		weight[1], false ));
    	row.addView(createTextView( data2.documentTitle,		weight[2], false ));
    	row.addView(createTextView( data2.dataSubmitted,		weight[3], false ));
        row.addView(createTextView( data2.jobsite,		weight[4], false ));
    	
    	
    	row.setClickable(true);
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
                String j = data2.jobNo;
                String t = data2.documentTitle;
                
            	String sqlQ = "SELECT jobno,documentContents,documentName from jobdoc " +
            			" where jobNo = "+ j + 
            			" AND documentName like '%" + data2.documentName + "%'" + 
            			" AND documentTitle like '%" + t + "%'";
    	
            	
            	
            	Cursor cursor = db.rawQuery(sqlQ);
            	int len = cursor.getCount();
            	Context context = getApplicationContext();
                
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, "size" +len, duration);
                toast.show();
            	HistoryData2 data2;
            	int index;
        		if (cursor.moveToFirst())
        			{
        			float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
        			
        			do
        				{
        				index = 0;
        				data2 = new HistoryData2();
        				data2.jobNo	= cursor.getString(0);
        				data2.documentContents=cursor.getBlob(1);
        				File pdfFile = convertBytesToFile(data2.documentContents);

        				 Uri path = Uri.fromFile(pdfFile);

        				    // Parse the file into a uri to share with another application

        				    Intent newIntent = new Intent(Intent.ACTION_VIEW);
        				    newIntent.setDataAndType(path, "application/pdf");
        				    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        				    try{
        				        startActivity(newIntent);
        				    }catch(ActivityNotFoundException ex){
        				        ex.printStackTrace();
        				    }

        				      				
        				}
        			while (cursor.moveToNext());
        			}
        		else
        			noResults();
        		if (cursor != null && !cursor.isClosed())
        			cursor.close();
                }
        	});
        
		tl.addView(row);
	    }
    public void createRow3(final HistoryData3 data3, float[] weight)
	    {
	    final LinearLayout row = createRow();
    	row.setTag(data3);
    	//row.setBackgroundResource(R.drawable.clickable_bg);
    	
    	row.addView(createTextView( data3.jobNo,		weight[0], false ));
    	
    	row.addView(createTextView( data3.documentName,		weight[1], false ));
    	row.addView(createTextView( data3.documentTitle,		weight[2], false ));
    	row.addView(createTextView( data3.dataSubmitted,		weight[3], false ));
        row.addView(createTextView( data3.jobsite,		weight[4], false ));
    	
    	
    	row.setClickable(true);
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
                
            	String sqlQ = "SELECT jobno,documentContents,documentName from jobdoc " +
            			" where jobNo = "+ data3.jobNo + 
            			" AND documentName like '%" + data3.documentName + "%'" + 
            			" AND documentTitle like '%" + data3.documentTitle + "%'";
            	Cursor cursor = db.rawQuery(sqlQ);
            	HistoryData3 data3;
            	int index;
        		if (cursor.moveToFirst())
        			{
        			float[] weight = { 0.55f, 1.5f, 0.9f, 0.7f, 0.8f, 1.1f, 1f, 1.5f };
        			
        			do
        				{
        				index = 0;
        				data3 = new HistoryData3();
        				data3.jobNo	= cursor.getString(0);
        				data3.documentContents=cursor.getBlob(1);
        				File pdfFile = convertBytesToFile(data3.documentContents);

        				 Uri path = Uri.fromFile(pdfFile);

        				    // Parse the file into a uri to share with another application

        				    Intent newIntent = new Intent(Intent.ACTION_VIEW);
        				    newIntent.setDataAndType(path, "application/pdf");
        				    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        				    try{
        				        startActivity(newIntent);
        				    }catch(ActivityNotFoundException ex){
        				        ex.printStackTrace();
        				    }

        				      				
        				}
        			while (cursor.moveToNext());
        			}
        		else
        			noResults();
        		if (cursor != null && !cursor.isClosed())
        			cursor.close();
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
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	readSQL();
    	}
    public void Update_Page2()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	readSQL2();
    	}
    public void Update_Page3()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	readSQL3();
    	}
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_File).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Title).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_DateSubmitted).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortNone);
    	
    	}
    
    private void setClickListeners()
	    {
	    findViewById(R.id.Sort_JobNo).setOnClickListener(new  OnClickListener()
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
	    
	    findViewById(R.id.Sort_File).setOnClickListener(new  OnClickListener()
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
	    
	    findViewById(R.id.Sort_Title).setOnClickListener(new  OnClickListener()
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
	    
	    findViewById(R.id.Sort_DateSubmitted).setOnClickListener(new  OnClickListener()
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
	    
	    findViewById(R.id.Sort_SiteName).setOnClickListener(new  OnClickListener()
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
	    
	    
	    }
    }
    
	