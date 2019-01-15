package com.twix_agent;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

/*******************************************************************************************************************
 * Class: Twix_AgentContacts
 * 
 * Purpose: Provides a listing of site contacts, including their Name, position, two phone numbers, and an
 * 			email address. The user can add contacts and delete contacts they added before syncing. After
 * 			a contact is synced, it is permanent as far as the tablet is concerned.
 * 
 * 
 * Relevant XML: contacts.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentContacts extends Activity
	{
	private boolean readOnly;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Twix_SQLite db;
	private int serviceAddress;
	private LinearLayout ll;
	private Context mContext;
	
	private ArrayAdapter<CharSequence> phoneTypeAdapter;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent();
        View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.contacts, null);
		this.setContentView( viewToLoad );
        
		app = (Twix_Application) getApplication();
		Twix_Theme = app.Twix_Theme;
        db = app.db;
        readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
        if( readOnly )
        	{
        	findViewById(R.id.AddContactHolder).setVisibility(View.GONE);
        	}
        ll = (LinearLayout)findViewById(R.id.ContactsBuild);
        
        String addressText = getIntent().getStringExtra("SiteName");
        serviceAddress = getIntent().getIntExtra("serviceAddressId", 0);
        ((TextView)findViewById(R.id.title)).setText(addressText);
    	
        buildAdapters();
        setClickListeners();
        
    	readSQL();
    	}
	
	private void buildAdapters()
		{
		phoneTypeAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
		phoneTypeAdapter.add("");
		phoneTypeAdapter.add("Office");
		phoneTypeAdapter.add("Mobile");
		phoneTypeAdapter.add("Fax");
		phoneTypeAdapter.add("Home");
		phoneTypeAdapter.add("Other");
		phoneTypeAdapter.setDropDownViewResource(R.layout.spinner_popup);
		}
	
	private void setClickListeners()
		{
		OnClickListener addContact = new OnClickListener()
			{
			@Override
			public void onClick(View arg0)
				{
				changeContact(null);
				}
			}
		;
		
		ImageButton addContact_bn = (ImageButton) findViewById(R.id.AddContact);
		addContact_bn.setOnClickListener(addContact);
		}
	
	public void readSQL()
		{
		if( ll.getChildCount() > 0 )
			ll.removeAllViews();
		
		String sqlQ = "select serviceAddressContact.contactId, serviceAddressContact.contactName," +
				" serviceAddressContact.phone1, serviceAddressContact.phone1Type, serviceAddressContact.phone2, " +
				" serviceAddressContact.phone2Type, serviceAddressContact.email, serviceAddressContact.contactType," +
				" serviceAddressContact.ext1, serviceAddressContact.ext2" +
				" FROM serviceAddressContact" +
				" WHERE serviceAddressContact.serviceAddressId = '" + serviceAddress + "'";
		
		Cursor cursor = db.rawQuery(sqlQ);
		ContactSQLData data;
		
		if (cursor.moveToFirst())
			{
			do
				{	
				data = new ContactSQLData();
				data.serviceAddressContactId= cursor.getInt(0);
				data.contactName			= Twix_TextFunctions.clean( cursor.getString(1) );
				data.phone1					= Twix_TextFunctions.clean( cursor.getString(2) );
				data.phone1Type				= Twix_TextFunctions.clean( cursor.getString(3) );
				data.phone2					= Twix_TextFunctions.clean( cursor.getString(4) );
				data.phone2Type				= Twix_TextFunctions.clean( cursor.getString(5) );
				data.email					= Twix_TextFunctions.clean( cursor.getString(6) );
				data.contactType			= Twix_TextFunctions.clean( cursor.getString(7) );
				data.ext1					= Twix_TextFunctions.clean( cursor.getString(8) );
				data.ext2					= Twix_TextFunctions.clean( cursor.getString(9) );
				
				ll.addView( createContactTable(data) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		}
	
	public LinearLayout createContactTable(ContactSQLData data)
		{
		String s;
		LayoutParams tableParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		tableParams.setMargins(0, 2, 0, 8);
		tableParams.weight = 1;
		
		LayoutParams rowParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		LinearLayout linearl = new LinearLayout(this);
		linearl.setBackgroundColor(Twix_Theme.tableBG);
		linearl.setLayoutParams(tableParams);
		linearl.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setOrientation(LinearLayout.HORIZONTAL);
		TextView tv = new TextView(this);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.weight = 0;
		
		LayoutParams paramsBn = new LayoutParams(40, 40);
		paramsBn.weight = 0;
		paramsBn.gravity = Gravity.FILL;
// Name and ContactType
		
	    params.setMargins(2, 2, 2, 2);
	    if( !readOnly )
	    	{
	    	ImageButton bn = new ImageButton(this);
		    bn.setLayoutParams(paramsBn);
		    bn.setTag( data );
		    bn.setImageResource(R.drawable.icon_edit);
		    bn.setBackgroundResource(R.drawable.button_bg);
		    bn.setPadding(12, 3, 12, 3);
		    bn.setOnClickListener(new View.OnClickListener()
	    		{
				public void onClick(View v)
	    			{
	                changeContact( (ContactSQLData)v.getTag() );
	    			}
	    		});
		    row.addView(bn);
	    	}
		if( data.contactName.length() <= 0 )
			s = "Name Not Provided";
		else
			s = "Name: " + data.contactName;
		params = new LayoutParams(params);
		params.setMargins(150, 2, 0, 2);
    	tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.headerSizeLarge);
	    tv.setPadding(10, 3, 10, 3);
	    tv.setGravity(Gravity.LEFT);
	    tv.setTextColor(Twix_Theme.headerText);
	    row.addView(tv);
    	
	    if( data.contactType.length() > 0 )
	    	{
	    	s = "-  " + data.contactType;
		    tv = new TextView(this);
		    params = new LayoutParams(params);
		    params.setMargins(0, 2, 2, 2);
		    params.weight = 0;
		    tv.setLayoutParams(params);
		    tv.setText( s );
		    tv.setTypeface(Typeface.DEFAULT_BOLD);
		    tv.setTextSize(Twix_Theme.headerSizeLarge);
		    tv.setPadding(3, 3, 3, 3);
		    tv.setGravity(Gravity.LEFT);
		    tv.setTextColor(Twix_Theme.headerText);
		    row.addView(tv);
	    	}
	    
	    row.setBackgroundColor(Twix_Theme.headerBG);
	    
	    linearl.addView(row);
	    
	    row = new LinearLayout(this);
	    row.setLayoutParams(rowParams);
	    row.setOrientation(LinearLayout.HORIZONTAL);
	    
// Phone 1 and Email
	    
	    // Phone 1 + Type
	    if( data.phone1.length() > 0 )
	    	{
		    if( data.phone1Type.length() > 0 )
		    	s = data.phone1Type + " Phone: " + data.phone1;
		    else
				s = "Phone: " + data.phone1;
	    	}
	    else
	    	s = "";
	    tv = new TextView(this);
	    params = new LayoutParams(params);
	    params.weight = 1;
	    params.setMargins(250, 2, 0, 2);
	    tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.subSize);
	    tv.setPadding(3, 3, 3, 3);
	    tv.setGravity(Gravity.LEFT);
	    tv.setTextColor(Twix_Theme.sub1Header);
	    row.addView(tv);
    	
	    // Phone 1 Ext
	    if( data.ext1.length() > 0 )
			s = "Ext: " + data.ext1;
	    else
	    	s = "";
	    tv = new TextView(this);
	    params = new LayoutParams(params);
	    params.weight = 1;
	    params.setMargins(0, 2, 2, 2);
	    tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.subSize);
	    tv.setPadding(3, 3, 3, 3);
	    tv.setGravity(Gravity.LEFT);
	    tv.setTextColor(Twix_Theme.sub1Header);
	    row.addView(tv);
	    
	    
	    if( data.email.length() > 0 )
	    	s = "Email: " + data.email;
	    else
			s = "Email: Not Provided";
		
	    tv = new TextView(this);
	    params = new LayoutParams(params);
	    params.weight = 0;
	    params.setMargins(2, 2, 100, 2);
	    tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.subSize);
	    tv.setPadding(3, 3, 3, 3);
	    tv.setGravity(Gravity.RIGHT);
	    tv.setTextColor(Twix_Theme.sub1Header);
	    row.addView(tv);
	    
	    linearl.addView(row);
	    
	    row = new LinearLayout(this);
	    row.setLayoutParams(rowParams);
	    row.setOrientation(LinearLayout.HORIZONTAL);
	    
//Phone 2 and Edit Button
	    
	    // Phone 2 + Type
	    if( data.phone2.length() > 0 )
	    	{
		    if( data.phone2Type.length() > 0 )
		    	s = data.phone2Type + " Phone: " + data.phone2;
		    else
				s = "Phone: " + data.phone2;
	    	}
	    else
	    	s = "";
	    tv = new TextView(this);
	    params = new LayoutParams(params);
	    params.weight = 1;
	    params.setMargins(250, 2, 0, 2);
	    tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.subSize);
	    tv.setPadding(3, 3, 3, 3);
	    tv.setGravity(Gravity.LEFT);
	    tv.setTextColor(Twix_Theme.sub1Header);
	    row.addView(tv);
    	
	    // Phone 2 Ext
	    if( data.ext2.length() > 0 )
			s = "Ext: " + data.ext2;
	    else
	    	s = "";
	    tv = new TextView(this);
	    params = new LayoutParams(params);
	    params.weight = 1;
	    params.setMargins(0, 2, 2, 2);
	    tv.setLayoutParams(params);
	    tv.setText( s );
	    tv.setTypeface(Typeface.DEFAULT_BOLD);
	    tv.setTextSize(Twix_Theme.subSize);
	    tv.setPadding(3, 3, 3, 3);
	    tv.setGravity(Gravity.LEFT);
	    tv.setTextColor(Twix_Theme.sub1Header);
	    row.addView(tv);
	    
	    linearl.addView(row);
	    
		return linearl;
		}
	
	private class ContactSQLData
		{
		int serviceAddressContactId;
		String contactName;
		String contactType;
		
		String phone1;
		String phone1Type;
		String ext1;
		
		String phone2;
		String phone2Type;
		String ext2;
		
		String email;
		}
	
	private class ContactData
		{
		int serviceAddressContactId;
		EditText contactName;
		EditText contactType;
		
		EditText phone1;
		Spinner phone1Type;
		EditText ext1;
		
		EditText phone2;
		Spinner phone2Type;
		EditText ext2;
		
		EditText email;
		}
	
	public void updateDB( ContactData data )
		{
		ContentValues cv = new ContentValues();
		cv.put("contactName", data.contactName.getText().toString() );
		cv.put("contactType", data.contactType.getText().toString() );
		cv.put("phone1Type", (String) data.phone1Type.getSelectedItem() );
		cv.put("phone1", data.phone1.getText().toString() );
		cv.put("ext1", data.ext1.getText().toString() );
		cv.put("phone2Type", (String) data.phone2Type.getSelectedItem() );
		cv.put("phone2", data.phone2.getText().toString() );
		cv.put("ext2", data.ext2.getText().toString() );
		cv.put("email", data.email.getText().toString() );
		
		cv.put("serviceAddressId", serviceAddress );
		cv.put("modified", "Y" );
		
		db.update("serviceAddressContact", cv, "contactId", data.serviceAddressContactId);
		
		readSQL();
		}
	
	public void insertDB( ContactData data )
		{
		ContentValues cv = new ContentValues();
		cv.put("contactName", data.contactName.getText().toString() );
		cv.put("contactType", data.contactType.getText().toString() );
		cv.put("phone1Type", (String) data.phone1Type.getSelectedItem() );
		cv.put("phone1", data.phone1.getText().toString() );
		cv.put("ext1", data.ext1.getText().toString() );
		cv.put("phone2Type", (String) data.phone2Type.getSelectedItem() );
		cv.put("phone2", data.phone2.getText().toString() );
		cv.put("ext2", data.ext2.getText().toString() );
		cv.put("email", data.email.getText().toString() );
		
		cv.put("contactId", db.newNegativeId("serviceAddressContact", "contactId") );
		cv.put("serviceAddressId", serviceAddress );
		cv.put("modified", "Y" );
		
		db.db.insertOrThrow("serviceAddressContact", null, cv);
		
		readSQL();
		}
	
	private void deleteDB( int id )
		{
		db.delete( "serviceAddressContact", "contactId", id );
		readSQL();
		}
	
	public void changeContact(final ContactSQLData sqlData)
		{
		final ContactData data = new ContactData();
		if( sqlData == null)
			data.serviceAddressContactId = 0;
		else
			data.serviceAddressContactId = sqlData.serviceAddressContactId;
		
		
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.contacts_edit, null);
		
		final Dialog dialog = new Dialog(mContext);
		
		data.phone1Type = (Spinner)viewToLoad.findViewById(R.id.type1);
		data.phone1Type.setAdapter(phoneTypeAdapter);
		
		data.phone2Type = (Spinner)viewToLoad.findViewById(R.id.type2);
		data.phone2Type.setAdapter(phoneTypeAdapter);
		
		data.contactName = ((EditText)viewToLoad.findViewById(R.id.name));
		data.contactType = ((EditText)viewToLoad.findViewById(R.id.contact_type));
		data.phone1 = ((EditText)viewToLoad.findViewById(R.id.phone1));
		data.ext1 = ((EditText)viewToLoad.findViewById(R.id.ext1));
		data.phone2 = ((EditText)viewToLoad.findViewById(R.id.phone2));
		data.ext2 = ((EditText)viewToLoad.findViewById(R.id.ext2));
		data.email = ((EditText)viewToLoad.findViewById(R.id.email));
		
		if( sqlData != null )
			{
			int sizeAdapter = phoneTypeAdapter.getCount();
			int start = 0;
			
			
			for( int j = 0; j < sizeAdapter; j++ )
				if( sqlData.phone1Type.contentEquals(phoneTypeAdapter.getItem(j).toString()) )
					{
					start = j;
					break;
					}
			data.phone1Type.setSelection(start);
			
			start = 0;
			for( int j = 0; j < sizeAdapter; j++ )
				if( sqlData.phone2Type.contentEquals(phoneTypeAdapter.getItem(j).toString()) )
					{
					start = j;
					break;
					}
			data.phone2Type.setSelection(start);
			
			data.contactName.setText(sqlData.contactName);
			data.contactType.setText(sqlData.contactType);
			data.phone1.setText(sqlData.phone1);
			data.ext1.setText(sqlData.ext1);
			data.phone2.setText(sqlData.phone2);
			data.ext2.setText(sqlData.ext2);
			data.email.setText(sqlData.email);
			}
		
		data.phone1.addTextChangedListener(new TextWatcher()
			{
	        @Override
	        public void afterTextChanged(Editable arg0)
	        	{
	        	}
	        
	        @Override
	        public void beforeTextChanged(CharSequence s, int start,
	                int count, int after)
	        	{
	        	}
	        
	        @Override
	        public void onTextChanged(CharSequence s, int start,
	                int before, int count)
	        	{
	        	int selection = data.phone1.getSelectionEnd();
	        	int sSize = s.length();
	        	if( sSize > 13 )
	        		{
	        		String s2 = s.toString();
	        		String endS = "";
	        		if( selection < sSize )
	        			{
		        		endS += "" + s2.subSequence(0, selection-1 ) + s2.subSequence(selection, sSize );
	        			}
	        		else
	        			endS += "" + s2.subSequence(0, selection-1);
	        		data.phone1.setText(endS);
	        		data.phone1.setSelection(selection-1);
	        		return;
	        		}
	        	String regExp;
	        	if( sSize > 8 )
	        		regExp = "^\\(([0-9]{0,3})\\)([0-9]{0,3})[-]([0-9]{0,4})$";
	        	else if ( sSize > 4 )
	        		regExp = "^\\(([0-9]{0,3})\\)([0-9]{0,3})$";
	        	else
	        		regExp = "^\\(([0-9]{0,3})$";
	        	
		        if(!s.toString().matches(regExp))
		            {
		            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
		            int size = userInput.length();
		            if (size > 0)
		            	{
		            	String expression = "(";
		            	Object[] intS = new Object[size];
		            	for( int i = 0; i < size; i++ )
		            		{
		            		intS[i] = userInput.charAt(i);
		            		expression += "%" + (i+1) + "$s";
		            		if( i == 2 )
		            			expression += ")";
		            		if( i == 5 )
		            			expression += "-";
		            		}
		            	String newS = String.format(expression, intS );
		            	data.phone1.setText(newS);
		            	data.phone1.setSelection(selection+1);
		            	}
		            }
	        	}
	        
			});
		data.phone2.addTextChangedListener(new TextWatcher()
			{
	        @Override
	        public void afterTextChanged(Editable arg0)
	        	{
	        	}
	        
	        @Override
	        public void beforeTextChanged(CharSequence s, int start,
	                int count, int after)
	        	{
	        	}
	        
	        @Override
	        public void onTextChanged(CharSequence s, int start,
	                int before, int count)
	        	{
	        	int selection = data.phone2.getSelectionEnd();
	        	int sSize = s.length();
	        	if( sSize > 13 )
	        		{
	        		String s2 = s.toString();
	        		String endS = "";
	        		if( selection < sSize )
	        			{
		        		endS += "" + s2.subSequence(0, selection-1 ) + s2.subSequence(selection, sSize );
	        			}
	        		else
	        			endS += "" + s2.subSequence(0, selection-1);
	        		data.phone2.setText(endS);
	        		data.phone2.setSelection(selection-1);
	        		return;
	        		}
	        	String regExp;
	        	if( sSize > 8 )
	        		regExp = "^\\(([0-9]{0,3})\\)([0-9]{0,3})[-]([0-9]{0,4})$";
	        	else if ( sSize > 4 )
	        		regExp = "^\\(([0-9]{0,3})\\)([0-9]{0,3})$";
	        	else
	        		regExp = "^\\(([0-9]{0,3})$";
	        	
		        if(!s.toString().matches(regExp))
		            {
		            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
		            int size = userInput.length();
		            if (size > 0)
		            	{
		            	String expression = "(";
		            	Object[] intS = new Object[size];
		            	for( int i = 0; i < size; i++ )
		            		{
		            		intS[i] = userInput.charAt(i);
		            		expression += "%" + (i+1) + "$s";
		            		if( i == 2 )
		            			expression += ")";
		            		if( i == 5 )
		            			expression += "-";
		            		}
		            	String newS = String.format(expression, intS );
		            	data.phone2.setText(newS);
		            	data.phone2.setSelection(selection+1);
		            	}
		            }
	        	}
	        
			});
		
		dialog.setContentView(viewToLoad);
		dialog.setTitle("Edit Contact");
		dialog.show();
		
		((Button)dialog.findViewById(R.id.Save)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				List<String> error = new ArrayList<String>();
		        View v = (View) v2.getParent().getParent().getParent();
		        
				if( validateSave(error, v))
					{
					List<String> newValues = new ArrayList<String>();
			        newValues.add( ((EditText)v.findViewById(R.id.name)).getText().toString() );
			        newValues.add( ((EditText)v.findViewById(R.id.contact_type)).getText().toString() );
			        newValues.add( (String) ((Spinner)v.findViewById(R.id.type1)).getSelectedItem() );
			        newValues.add( ((EditText)v.findViewById(R.id.phone1)).getText().toString() );
			        newValues.add( ((EditText)v.findViewById(R.id.ext1)).getText().toString() );
			        newValues.add( (String) ((Spinner)v.findViewById(R.id.type2)).getSelectedItem() );
			        newValues.add( ((EditText)v.findViewById(R.id.phone2)).getText().toString() );
			        newValues.add( ((EditText)v.findViewById(R.id.ext2)).getText().toString() );
			        newValues.add( ((EditText)v.findViewById(R.id.email)).getText().toString() );
			        
					if( sqlData != null )
			        	updateDB(data);
			        else
			        	insertDB(data);
					dialog.dismiss();
					}
				else
					{
					String errorMsg = "You have made the following errors:\n\n";
					int size = error.size();
					for( int i = 0; i < size; i++ )
						{
						errorMsg += " " + error.get(i) + "\n";
						}
					Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
					}
				}
			});
		
		((Button)dialog.findViewById(R.id.Cancel)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				dialog.dismiss();
				}
			});
		
		if( data.serviceAddressContactId > 0 )
			dialog.findViewById(R.id.Delete).setVisibility(View.GONE);
		else
			((Button)dialog.findViewById(R.id.Delete)).setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v2)
					{
					deleteDB(data.serviceAddressContactId);
					dialog.dismiss();
					}
				});
		}
	
	public boolean validateSave(List<String> error, View v)
		{
		boolean valid = true;
		TextView tv = ((TextView)v.findViewById(R.id.name));
		if( tv.getText().toString().length() < 1  )
			{
			valid = false;
			error.add("A Name must be provided");
			((TextView)v.findViewById(R.id.Title_Name)).setTextColor(Twix_Theme.warnColor);
			}
		else
			{
			((TextView)v.findViewById(R.id.Title_Name)).setTextColor(Twix_Theme.sub1Header);
			}
		
		tv = ((TextView)v.findViewById(R.id.phone1));
		if( !tv.getText().toString().matches("^(\\(([0-9]{3})\\)([0-9]{3})[-]([0-9]{4}))?$")  )
			{
			valid = false;
			error.add("The first phone number is not valid");
			((TextView)v.findViewById(R.id.Title_Phone1)).setTextColor(Twix_Theme.warnColor);
			}
		else if( tv.getText().toString().length() < 1  )
			{
			if( ((String)((Spinner)v.findViewById(R.id.type1)).getSelectedItem()).length() > 0 )
				{
				valid = false;
				error.add("The first phone must contains a type, but no number");
				((TextView)v.findViewById(R.id.Title_Phone1)).setTextColor(Twix_Theme.warnColor);
				}
			if( ((TextView)v.findViewById(R.id.ext1)).getText().toString().length() > 0 )
				{
				valid = false;
				error.add("The first phone must contains a extension, but no number");
				((TextView)v.findViewById(R.id.Title_Phone1)).setTextColor(Twix_Theme.warnColor);
				}
			}
		else
			{
			((TextView)v.findViewById(R.id.Title_Phone1)).setTextColor(Twix_Theme.sub1Header);
			}
		
		tv = ((TextView)v.findViewById(R.id.phone2));
		if( !tv.getText().toString().matches("^(\\(([0-9]{3})\\)([0-9]{3})[-]([0-9]{4}))?$")  )
			{
			valid = false;
			error.add("The second phone number is not valid");
			((TextView)v.findViewById(R.id.Title_Phone2)).setTextColor(Twix_Theme.warnColor);
			}
		else if( tv.getText().toString().length() < 1  )
			{
			if( ((String)((Spinner)v.findViewById(R.id.type2)).getSelectedItem()).length() > 0 )
				{
				valid = false;
				error.add("The second phone must contains a type, but no number");
				((TextView)v.findViewById(R.id.Title_Phone2)).setTextColor(Twix_Theme.warnColor);
				}
			if( ((TextView)v.findViewById(R.id.ext2)).getText().toString().length() > 0 )
				{
				valid = false;
				error.add("The second phone must contains a extension, but no number");
				((TextView)v.findViewById(R.id.Title_Phone2)).setTextColor(Twix_Theme.warnColor);
				}
			}
		else
			{
			((TextView)v.findViewById(R.id.Title_Phone2)).setTextColor(Twix_Theme.sub1Header);
			}
		
		return valid;
		}
	
	}
