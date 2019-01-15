package com.twix_agent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.AnimatorSet.Builder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/*******************************************************************************************************************
 * Class: Twix_AgentEmailAttachments.java
 * 
 *  Relevant XML: email_attachments.xml
 * 
 * 
 * @author Rashmi Kulkarni, Therma Corp.
 *
 */
public class Twix_AgentEmailAttachments extends Activity {
private boolean readOnly;
private Twix_Application app;
private Twix_AgentTheme Twix_Theme;
private Context mContext;
Twix_SQLite db;
 EditText et_address, et_subject, et_message;
 String address, subject, message, file_path;
 Button bt_send, bt_attach,bt_addemail;
 String curFileName;
 TextView tv_attach,tv_attach2,tv_attach3,tv_attach4,tv_attach5;
 
 public int temp=0;
 public String jo = null;
 public String de = null;
 public String getp=null;
 private static final int PICK_IMAGE = 100;
private static final int	REQUEST_PATH	= 1;
 Uri URI = null;
 int columnindex;
 //public String newString;
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.email_attachments);
  initializeViews();
 // bt_send.setOnClickListener(this);
 // bt_attach.setOnClickListener(this);
  app = (Twix_Application) getApplication();
  db = app.db;
  Twix_Theme = app.Twix_Theme;
  
  Intent intent = getIntent();
  temp = intent.getIntExtra("font", 0);
  jo = intent.getStringExtra("job");
  de =intent.getStringExtra("desc");
  getp =intent.getStringExtra("GetPath");
  String temp1 = Integer.toString(temp);
  et_subject.setText("Document for job number '" +jo);
  et_subject.append("' tag number '" +temp1);
  et_subject.append("' - " +de);
  
  bt_attach.setOnClickListener(new OnClickListener()
		{
		@Override
		public void onClick(View v)
			{
			if(et_address.getText().toString().equals(""))
				   {
				   android.app.AlertDialog.Builder alert1 = new AlertDialog.Builder(Twix_AgentEmailAttachments.this);
				    alert1.setTitle("ALERT - NO EMAIL ADDRESS SELECTED");
				    alert1.setMessage("The email address field is empty. Please select the email address");
				    alert1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					dialog.cancel();
					
				}});
				    alert1.show();
				    et_address.requestFocus(et_address.length());
				   }
			else{
			openGallery();}
			}
 });
  bt_send.setOnClickListener(new OnClickListener()
		{
		@Override
		public void onClick(View v)
			{
			
			   address = et_address.getText().toString();
			   subject = et_subject.getText().toString();
			   message = et_message.getText().toString();
			   //ArrayList<Uri> uris = new ArrayList<Uri>();
			   String emailAddresses[] = { address };
			  // List<String> attachments = null;
			   final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,emailAddresses);
			   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			   emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
			   emailIntent.setType("plain/text");
			   
			   String[] attachments = new String[] {tv_attach.getText().toString(),tv_attach2.getText().toString(),tv_attach3.getText().toString(),tv_attach4.getText().toString(),tv_attach5.getText().toString()};
			  // emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,address);
			 //  emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			  // emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
			   ArrayList<Uri> uris = new ArrayList<Uri>();
			   long result =0;
			   for (String file : attachments) {
			   
			   File fileIn = new File(file);
			   long size = fileIn.length();
			   result = result + size;
			      
			    Uri u = Uri.fromFile(fileIn);
			    uris.add(u);
			   }
			   if (uris!= null)
			   emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			   
			   if (result > 15728640)   // this number is no of bytes i.e. 15 mb in size
				   {
				   android.app.AlertDialog.Builder alert = new AlertDialog.Builder(Twix_AgentEmailAttachments.this);
				    alert.setTitle("ALERT - OVER SIZE LIMIT");
				    alert.setMessage("The size of the attachments is over limit");
				    alert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, close
					// current activity
					dialog.cancel();
					
				}});
				   AlertDialog alertDialog = alert.create();
				   alertDialog.show();
				    
				   }
			   else
				   {
				android.app.AlertDialog.Builder alert = new AlertDialog.Builder(Twix_AgentEmailAttachments.this);
			    alert.setTitle("ALERT - MAKE SURE YOU SELECT THE THERMA EMAIL ACCOUNT");
			    alert.setMessage("Please select the therma email account(Email) for sending emails and choose 'Always'. By doing this the application will not keep on asking you everytime you send emails");
			    alert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				// current activity
				finish();
				 startActivity(emailIntent);
				   
				
			}});
			   AlertDialog alertDialog = alert.create();
			   alertDialog.show();
				  //finish();
			  // startActivity(emailIntent);
			   
				   }}
});
  
bt_addemail.setOnClickListener(new OnClickListener()
		{
		@Override
		public void onClick(View v)
			{
			final Dialog dialog = new Dialog(Twix_AgentEmailAttachments.this);
	        dialog.setContentView(R.layout.cities_listview);
	        dialog.setTitle("Select City");
	        final ListView listView = (ListView) dialog.findViewById(R.id.list);
	        List<String> values = getAllCategory();
	        dialog.show();
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Twix_AgentEmailAttachments.this,
	                android.R.layout.simple_list_item_1, android.R.id.text1, values);
	        listView.setAdapter(adapter);
	        listView.setOnItemClickListener(new OnItemClickListener() {

	         
	            public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) {
	                int itemPosition = position;
	                String itemValue = (String) listView
	                        .getItemAtPosition(position);
	                
	                // Show Alert
	                //Toast.makeText(
	                  //      getApplicationContext(),
	                    //    "Position :" + itemPosition + "  ListItem : "
	                      //          + itemValue, Toast.LENGTH_LONG).show();
	              
	               // et_address.append(itemValue);
	                Editable address = et_address.getText();
	               // Toast.makeText(getApplicationContext(), "text is " +address, Toast.LENGTH_LONG).show();
	                if (address.toString().equals(""))
	                	{
	                	et_address.setText(itemValue);	                	}
	                	
	                	else
	                	{
	                	et_address.append(";");
	                	  et_address.append(itemValue);
	                	}
	                
	                
	                
	            // 	if (address.toString().equals(" ")) {
	             //   		et_address.append(itemValue);
	             // 	}
	             //   		else {
	              //  		et_address.append(";");
	              //  	  et_address.append(itemValue);
	              //  		}
	                //if et_address.
	                dialog.cancel();
	                
	                
	                
			}
});
  
  
			
			}});}
  


 public List<String> getAllCategory() {
 List<String> List = new ArrayList<String>();
 // Select All Query
 
 String selectQuery = "SELECT SC.contactName, SC.email " +
			"from openServiceTag as ST " + 
			"LEFT OUTER JOIN serviceAddressContact as SC " + 
				"on SC.serviceAddressId = ST.serviceAddressId " +
			"WHERE ST.serviceTagId = " + temp + " " +
					"AND SC.email IS NOT NULL " +
					"AND SC.email != '' " +
					"ORDER BY SC.contactName";
 Cursor cursor = db.rawQuery(selectQuery, null);

 // looping through all rows and adding to list
 if (cursor.moveToFirst()) {
     do {
         List.add(cursor.getString(1));
     } while (cursor.moveToNext());
 }
 return List;
}  
 private void initializeViews() {
  et_address = (EditText) findViewById(R.id.et_address_id);
  et_subject = (EditText) findViewById(R.id.et_subject_id);
  et_message = (EditText) findViewById(R.id.et_message_id);
  bt_send = (Button) findViewById(R.id.bt_send_id);
  bt_attach = (Button) findViewById(R.id.bt_attach_id);
  tv_attach = (TextView) findViewById(R.id.tv_attach_id);
  tv_attach2 = (TextView) findViewById(R.id.tv_attach2_id);
  tv_attach3 = (TextView) findViewById(R.id.tv_attach3_id);
  tv_attach4 = (TextView) findViewById(R.id.tv_attach4_id);
  tv_attach5 = (TextView) findViewById(R.id.tv_attach5_id);
  bt_addemail = (Button) findViewById(R.id.bt_add_email);
 }
 private void openGallery() {
 
 Intent intent1 = new Intent(this, filechooser.class);
 startActivityForResult(intent1,REQUEST_PATH);
 
 }

 
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == REQUEST_PATH){
  if (resultCode == RESULT_OK) {
  //Toast.makeText(getApplicationContext(), "text is " + , Toast.LENGTH_LONG).show();
  curFileName = data.getStringExtra("GetFileName");
  
  
 // Editable attach = tv_attach.getText();
  CharSequence attach = tv_attach.getText();
  //Toast.makeText(getApplicationContext(), "text is " + , Toast.LENGTH_LONG).show();
  //Toast.makeText(getApplicationContext(), "text is " +address, Toast.LENGTH_LONG).show();
  if (attach.toString().equals(""))
  	{
  	tv_attach.setText(curFileName);
  	}
  	
  	else if(tv_attach.getText().toString() != "" && tv_attach2.getText().toString().equals(""))
  	{
  	tv_attach2.setText(curFileName);
  	}
  	else if (tv_attach.getText().toString() != "" && tv_attach2.getText().toString() != "" && tv_attach3.getText().toString().equals(""))
  		{
  		tv_attach3.setText(curFileName);
  		}
  	else if (tv_attach.getText().toString() != "" && tv_attach2.getText().toString() != "" && tv_attach3.getText().toString() != "" && tv_attach4.getText().toString().equals(""))
  		{
  		tv_attach4.setText(curFileName);
  		}
  	else if (tv_attach.getText().toString() != "" && tv_attach2.getText().toString() != "" && tv_attach3.getText().toString() != "" &&  tv_attach4.getText().toString() != "" && tv_attach5.getText().toString().equals(""))
  		{
  		tv_attach5.setText(curFileName);
  		}
  	else if (tv_attach.getText().toString() != "" && tv_attach2.getText().toString() != "" && tv_attach3.getText().toString() != "" && tv_attach4.getText().toString() != "" && tv_attach5.getText().toString() != "")
  	{
  	android.app.AlertDialog.Builder alert = new AlertDialog.Builder(Twix_AgentEmailAttachments.this);
    alert.setTitle("ALERT - NO MORE ATTACHMENTS");
    alert.setMessage("You Can attach maximum of 5 attachments at a time");
    alert.setPositiveButton("OK", null);
    alert.show();
  	//Toast.makeText(getApplicationContext(), "You Can attach maximum of 3 attachments at a time", Toast.LENGTH_LONG).show();
  	}
  
  
   //tv_attach.setText(curFileName);
  // URI = Uri.parse("file://" + curFileName);
  }
  }}}
 //@Override
 /*public void onClick(View v) {
  switch (v.getId()) {

  case R.id.bt_attach_id:
   openGallery();
   break;

  case R.id.bt_send_id:
   address = et_address.getText().toString();
   subject = et_subject.getText().toString();
   message = et_message.getText().toString();

   String emailAddresses[] = { address };

   Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
     emailAddresses);
   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
   emailIntent.setType("plain/text");
   emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
   if (URI != null)
    emailIntent.putExtra(Intent.EXTRA_STREAM, URI);

   startActivity(emailIntent);

   break;

  }

*/

 

 

	  
					
	 
