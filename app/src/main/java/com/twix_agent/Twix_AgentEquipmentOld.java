package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentEquipmentOld
 * 
 * Purpose: Allows for direct SQL queries. This is a debugging page only.
 * 
 * Relevant XML: equipment_old.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentEquipmentOld extends Activity
	{
	private Twix_SQLite db;
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equipment_old);
        
        db = ((Twix_Application) this.getApplication()).db;
    	}
	
    public void readSQL(String tableName)
	    {
	    try
	    	{
	    	TableLayout tl = (TableLayout) findViewById(R.id.WorkFlowBuild);
	    	tl.removeAllViews();
	    	
	    	/*
	    	int position = tableName.toLowerCase().indexOf("from");
	    	if( position > -1 )
	    		createDummyRow( tableName.subSequence( position+4, tableName.length() ).toString() );
	    	*/
	    	
	    	List<String> list = new ArrayList<String>();
	    	//String s = "SELECT	* FROM " + tableName;
	    	Cursor cursor = db.rawQuery(tableName);
	    	int size = cursor.getColumnCount();
	    	
			if (cursor.moveToFirst())
				{
				do
					{
					for( int i = 0; i < size; i++)
						{
						list.add(cursor.getString(i));
						}
					createRow( list );
					list.clear();
					}
				while (cursor.moveToNext());
				}
			if (cursor != null && !cursor.isClosed())
				{
				cursor.close();
				}
	    	}
	    catch (Exception e)
	    	{
	    	Toast.makeText(this, "Error in Query:\n\t" + e.getMessage(), Toast.LENGTH_LONG ).show();
	    	}
	    }
    
    public void writeSQL(String query)
	    {
    	db.db.execSQL(query);
	    }
    
    public void readSQLSig()
	    {
    	TableLayout tl = (TableLayout) findViewById(R.id.WorkFlowBuild);
    	tl.removeAllViews();
    	//createDummyRow();
    	
    	Cursor cursor = db.rawQuery("SELECT	* FROM Signature");
    	
		if (cursor.moveToFirst())
			{
			do
				{
				createSignature( cursor.getString(0), cursor.getBlob(1) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	    }
    
    public void createSignature( String s, byte[] b)
    	{
    	TableLayout tl = (TableLayout) findViewById(R.id.WorkFlowBuild);
    	TableRow row = new TableRow(this);
    	
    	LayoutParams params = new LayoutParams();
    	params.height = LayoutParams.WRAP_CONTENT;
    	params.setMargins(1, 1, 1, 1);
    	params.weight = 1;
    	
    	TextView tv = new TextView(this);
		tv.setLayoutParams(params);
    	tv.setText( s );
		tv.setBackgroundColor(0xffcccccc);
		tv.setTextColor(0xff000000);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		row.addView(tv);
		
    	Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(bmp);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		iv.setLayoutParams(lp);
		row.addView(iv);
		
		tl.addView(row);
    	}
    
    public void createRow(List<String> list)
	    {
    	TableLayout tl = (TableLayout) findViewById(R.id.WorkFlowBuild);
    	TableRow row = new TableRow(this);
    	
    	LayoutParams params = new LayoutParams();
    	params.height = LayoutParams.WRAP_CONTENT;
    	params.setMargins(1, 1, 1, 1);
    	params.weight = 1;
    	int strSize = 0;
    	
    	int size = list.size();
    	for( int i = 0; i < size; i++ )
	    	{
    		TextView tv = new TextView(this);
    		tv.setLayoutParams(params);
    		if( list.get(i) != null )
    			{
    			strSize = list.get(i).length();
	    		if( strSize > 15 )
	    			tv.setText( list.get(i).substring(0, 15) );
	    		else
	    			tv.setText( list.get(i).substring(0, strSize) );
    			}
    		else
    			tv.setText("");
    		if( i == 0)
    			tv.setBackgroundColor(0xffcccccc);
    		else
    			tv.setBackgroundColor(0xffeeeeee);
    		tv.setTextColor(0xff000000);
    		tv.setGravity(Gravity.CENTER_HORIZONTAL);
    		row.addView(tv);
	    	}
    	row.setClickable(true);
    	
		tl.addView(row, new LayoutParams());
	    }
    
    public void createDummyRow(String tableName)
    	{
    	TableLayout tl = (TableLayout) findViewById(R.id.WorkFlowBuild);
		TableRow row = new TableRow(this);
    	
    	LayoutParams params = new LayoutParams();
		params.height = LayoutParams.WRAP_CONTENT; //0;
		params.setMargins(1, 0, 1, 0);
		params.weight = 1;
		
    	List<String> colHeaders = db.getHeaders(tableName);
		int size = colHeaders.size();
		
    	for( int i = 0; i < size; i++)
    		{
			TextView tv = new TextView(this);
		    tv.setLayoutParams(params);
		    tv.setGravity(Gravity.CENTER_HORIZONTAL);
		    tv.setText( colHeaders.get(i) );
		    row.addView(tv);	
    		}
		tl.addView(row, new LayoutParams());
    	}
    
    public void Select(View v)
    	{
    	String tableName = ((EditText)findViewById(R.id.Text_Input)).getText().toString();
    	readSQL(tableName);
    	}
    
    public void WriteOut(View v)
    	{
    	String query = ((EditText)findViewById(R.id.Text_Input)).getText().toString();
    	writeSQL(query);
    	}
    
	}