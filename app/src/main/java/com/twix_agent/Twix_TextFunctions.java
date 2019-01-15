package com.twix_agent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.widget.ArrayAdapter;

/*******************************************************************************************************************
 * Class: Twix_TextFunctions
 * 
 * Purpose: Provides static text functions for Twix_Agent. Static functions include wrapping text and various
 * 			date formatting.
 * 
 * Relevant XML: none
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *
 ********************************************************************************************************************/
public class Twix_TextFunctions
	{
	final static int DB_FORMAT = 0;
	final static int NORMAL_FORMAT = 1;
	final static int DB_FORMAT_COMPLEX = 2;
	
	public static String clean(String s)
		{
		if( s == null )
			return "";
		return s;
		}
	
	public static String getCurrentDate(int type)
		{
		String date = "";
		Calendar c = Calendar.getInstance();
		int month = c.get(Calendar.MONTH)+1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int year = c.get(Calendar.YEAR);
		switch(type)
			{
			case DB_FORMAT:
				date += year + "-";
				
				if( month < 10 )
					date += "0";
				date += month + "-";
				
				if( day < 10 )
					date += "0";
				date += day;
				break;
			case DB_FORMAT_COMPLEX:
				Date d = new Date();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = fmt.format(d);
				break;
			case NORMAL_FORMAT:
			default:
				if( month < 10 )
					date += "0";
				date += month + "/";
				
				if( day < 10 )
					date += "0";
				date += day + "/";
				
				date += year;
				break;
			}
		
		return date;
		}
	
	public static String formatToDB( int month, int day, int year )
		{
		String date = "";
		
		if( month < 10 )
			date += "0";
		date += month + "/";
		
		if( day < 10 )
			date += "0";
		date += day + "/";
		
		date += year;
		
		return date;
		}
	
	/**
	 * Converts the human readable DD-MM-YYYY or DD/MM/YYYY to database format YYYY-MM-DD
	 */
	public static String convertDateForDB( String date )
		{
		String ret = "";
		
		ret += date.substring(6,10) + "-";
		ret += date.substring(3,5) + "-";
		ret += date.substring(0,2);
		
		return ret;
		}
	
	public static String stripHeader( String input, String header )
		{
		if( input != null )
			{
			if( input.startsWith( header ) )
				{
				input = input.substring(4);
				}
			}
		
		return input;
		}
	
	public static String DBtoNormal( String input )
		{
		if( input == null )
			return input;
		if( input.length() < 10 )
			return input;
		
		String original = input;
		String ret;
		try
			{
			input = input.substring(0, 10);
			
			/**
			 * Expected input:
			 * 
			 * 	2	0	1	2	-	0	1	-	1	8
			 * 
			 * 	0	1	2	3	4	5	6	7	8	9	10 (end)
			 * Index
			 * 
			 * Output Format
			 * 
			 * 	0	1	-	1	8	-	2	0	1	2
			 * 
			 */
			
			ret =  input.substring(5,7) + "/";
			ret += input.substring(8,10) + "/";
			ret += input.substring(0,4);
			}
		catch( Exception e )
			{
			ret = original;
			Log.e("twix_agent:Twix_TextFunctions", "Error converting '"+input+"' to a normal date format", e);
			}
		
		return ret;
		}
	
	public static String DBPhonetoNormal(String input)
		{
		if( input == null || input.length() < 7 )
			return input;
		
		boolean areaCode = input.length() > 7;
		boolean ext = input.length() > 9;
		
		String ret = null;
		
		try
			{
			String phone1;
			String phone2;
			String AreaCode = "";
			String Ext = "";
			int start = 0;
			if( areaCode )
				{
				AreaCode = "(" + input.substring(start, start+3) + ") ";
				
				start += 3;
				}
			
			phone1 = input.substring(start, start+3);
			start += 3;
			phone2 = input.substring(start, start+4);
			start += 4;
			
			if( ext )
				Ext = " ext: " + input.substring(start);
			
			ret = AreaCode + phone1 + phone2 + Ext;
			}
		catch(Exception e)
			{
			ret = input;
			}
		
		return ret;
		}
	
	public static int BrightenColor(int c, float multi)
    	{
    	int a = Color.alpha(c);
		int r = Color.red(c);
		int b = Color.blue(c);
		int g = Color.green(c);
		int def = (int) (multi*10) - 10;
		
		if (r == 0 && b == 0 && g == 0)
			return Color.argb(a, def, def, def);
		
		// Red
		if (r < 3 && r != 0)
			{
			r = def;
			}
		else
			{
			r = (int) (r * multi);
			r = (r > 255) ? 255 : r;
			}
		
		// Blue
		if (b < 3 && b != 0)
			{
			b = def;
			}
		else
			{
			b = (int) (b * multi);
			b = (b > 255) ? 255 : b;
			}
		
		// Green
		if (g < 3 && g != 0)
			{
			g = def;
			}
		else
			{
			g = (int) (g * multi);
			g = (g > 255) ? 255 : g;
			}

	    return Color.argb(a, r, g, b);
    	}
	
	public static String ComplexToNormal(String s)
		{
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat to = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
		Date d;
		try
			{
			d = from.parse(s);
			}
		catch (Exception e)
			{
			d = new Date();
			}
		
		return to.format(d);
		}
	
	public static ArrayAdapter<CharSequence> BuildAdapter(Twix_SQLite db, Context mContext, String PickListName)
		{
		String sqlQ = "SELECT itemValue FROM PickListItem WHERE PickId = " +
				"(SELECT PickId FROM PickList WHERE Description = '" + PickListName + "' LIMIT 1) ORDER BY itemValue asc";
		Cursor cursor = db.rawQuery(sqlQ);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
		adapter.setDropDownViewResource(R.layout.spinner_popup);
		while (cursor.moveToNext())
			adapter.add(cursor.getString(0));
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return adapter;
		}
	}
