package com.twix_agent;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceUnitPMChecklist
 * 
 * Purpose: Provides a PM checklist to the user if the open service tag is a PM service type. The PM checklist is
 * 			derived from a static table provided in the sync. If the service address has a specific PM checklist
 * 			associated with an equipment catagory, that list is used instead. 
 * 
 * Relevant XML: servicetag_pmchecklist.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitPMChecklist extends Activity
	{
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_Application app;
	private Twix_SQLite db;
	private Context mContext;
	private Twix_AgentTheme Twix_Theme;
	private LinearLayout tl,t2,t3;
	private Twix_AgentServiceTagUnit TagUnit;
	private ArrayList<PMChecklistData> checkListData;
	private int currentEquipmentCategoryId = 0;
	
	private OnCheckedChangeListener dirtyCheck;
	private boolean init = false;
	
	private InputFilter[] max100;
	public boolean fg = false;
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		final View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_pmchecklist, null);
		this.setContentView( viewToLoad );
        
		app = (Twix_Application) getApplication();
        db = app.db;
        Twix_Theme = app.Twix_Theme;
       // final LinearLayout row=new LinearLayout(this);
        
        tl = (LinearLayout) findViewById(R.id.CheckListBuild);
        
        final CheckBox cb,cb1;
        cb = new CheckBox(this);
		final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
        cb1 = (CheckBox) findViewById(R.id.pm);
        cb2 = (CheckBox) findViewById(R.id.pm2);
        cb3 = (CheckBox) findViewById(R.id.pm3);
        cb4 = (CheckBox) findViewById(R.id.pm4);
        cb5 = (CheckBox) findViewById(R.id.pm5);
        cb6 = (CheckBox) findViewById(R.id.pm6);
        cbrev = (CheckBox) findViewById(R.id.pmrev);
        
		cb1.setEnabled(false);
		cb2.setEnabled(false);
		cb3.setEnabled(false);
		cb4.setEnabled(false);
		cb5.setEnabled(false);
		cb6.setEnabled(false);
		//cbrev.setEnabled(false);
		cbrev.setOnClickListener(new View.OnClickListener() 
			{
			public void onClick(View v) {
	        checkListData.clear();
	        tl.removeAllViews();
	        if (((CheckBox) v).isChecked())
	        	  {
	        	 
	        readSQLr(false);
	
	        }			
	        else
	        	{
	        	readSQL(false);
	        	}
			}});
		
        cb1.setOnClickListener(new View.OnClickListener() {
								        public void onClick(View v) {
								       // tl.removeAllViews();
								       // cb2.setEnabled(false);
								        //cb2.setChecked(false);
								        if (((CheckBox) v).isChecked())
								        	  {
								        	 
								        	 readSQL1(false);
								        	  }
								        else 
								        	  {checkListData.clear();
								        	  tl.removeAllViews();
								        	  if(cb2.isChecked())
								        		  {
								        		  readSQL2(false);
								        		  }
								        	  if(cb3.isChecked())
								        		  {
								        		  readSQL3(false);
								        		  }
								        	  if(cb4.isChecked())
								        		  {
								        		  readSQL4(false);
								        		  }
								        	  if(cb5.isChecked())
								        		  {
								        		  readSQL5(false);
								        		  }
								        	  if(cb6.isChecked())
								        		  {
								        		  readSQL6(false);
								        		  }
								    	  
								        	  }}
      });
        cb2.setOnClickListener(new View.OnClickListener() {
								        public void onClick(View v) {
								        // tl.removeAllViews();
								       // cb1.setEnabled(false);
								          if (((CheckBox) v).isChecked())
								        	  {
								        	 readSQL2(false);
								              }
								          else
								        	  {
								        	  checkListData.clear();
								        	  tl.removeAllViews();
								        	  if(cb1.isChecked())
								        		  {
								        		  readSQL1(false);
								        		  }
								        	  if(cb3.isChecked())
								        		  {
								        		  readSQL3(false);
								        		  }
								        	  if(cb4.isChecked())
								        		  {
								        		  readSQL4(false);
								        		  }
								        	  if(cb5.isChecked())
									    		  {
									    		  readSQL5(false);
									    		  }
								        	  if(cb6.isChecked())
									    		  {
									    		  readSQL6(false);
									    		  }
								        	  }}
      });
        cb3.setOnClickListener(new View.OnClickListener() {
									        public void onClick(View v) {
									        //tl.removeAllViews();
									       // cb1.setEnabled(false);
									          if (((CheckBox) v).isChecked())
									        	  {
									        	 readSQL3(false);
									              } else
									      	  {
									      	  checkListData.clear();
									      	  tl.removeAllViews();
									      	  if(cb1.isChecked())
									      		  {
									      		  readSQL1(false);
									      		  }
									      	  if(cb2.isChecked())
									      		  {
									      		  readSQL2(false);
									      		  }
									      	  if(cb4.isChecked())
									    		  {
									    		  readSQL4(false);
									    		  }
									      	  if(cb5.isChecked())
									    		  {
									    		  readSQL5(false);
									    		  }
									      	  if(cb6.isChecked())
									    		  {
									    		  readSQL6(false);
									    		  }
									      	  }}		                 
									        	  
      });
        cb4.setOnClickListener(new View.OnClickListener() {
										        public void onClick(View v) {
										        //tl.removeAllViews();
										       // cb1.setEnabled(false);
										          if (((CheckBox) v).isChecked())
										        	  {
										        	 readSQL4(false);
										              } else
										      	  {
										      	  checkListData.clear();
										      	  tl.removeAllViews();
										      	  if(cb1.isChecked())
										      		  {
										      		  readSQL1(false);
										      		  }
										      	  if(cb2.isChecked())
										      		  {
										      		  readSQL2(false);
										      		  }
										      	  if(cb3.isChecked())
										    		  {
										    		  readSQL3(false);
										    		  }
										      	if(cb5.isChecked())
										    		  {
										    		  readSQL5(false);
										    		  }
										      	if(cb6.isChecked())
										    		  {
										    		  readSQL6(false);
										    		  }
										      	  }}		                 
        	  
});
        cb5.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        //tl.removeAllViews();
       // cb1.setEnabled(false);
          if (((CheckBox) v).isChecked())
        	  {
        	 readSQL5(false);
              } else
      	  {
      	  checkListData.clear();
      	  tl.removeAllViews();
      	  if(cb1.isChecked())
      		  {
      		  readSQL1(false);
      		  }
      	  if(cb2.isChecked())
      		  {
      		  readSQL2(false);
      		  }
      	  if(cb3.isChecked())
    		  {
    		  readSQL3(false);
    		  }
      	if(cb4.isChecked())
    		  {
    		  readSQL4(false);
    		  }
      	if(cb6.isChecked())
    		  {
    		  readSQL6(false);
    		  }
      	  }}		                 

});
        cb6.setOnClickListener(new View.OnClickListener() {
										        public void onClick(View v) {
										        //tl.removeAllViews();
										       // cb1.setEnabled(false);
										          if (((CheckBox) v).isChecked())
										        	  {
										        	 readSQL6(false);
										              } else
										      	  {
										      	  checkListData.clear();
										      	  tl.removeAllViews();
										      	  if(cb1.isChecked())
										      		  {
										      		  readSQL1(false);
										      		  }
										      	  if(cb2.isChecked())
										      		  {
										      		  readSQL2(false);
										      		  }
										      	  if(cb3.isChecked())
										    		  {
										    		  readSQL3(false);
										    		  }
										      	if(cb4.isChecked())
										    		  {
										    		  readSQL4(false);
										    		  }
										      	if(cb5.isChecked())
										    		  {
										    		  readSQL5(false);
										    		  }
										      	  }}		                 

});
        
        unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		LocalActivityManager manager2 = unitAct.getLocalActivityManager();
        TagUnit = (Twix_AgentServiceTagUnit) manager2.getActivity("Unit");
    	
    	checkListData = new ArrayList<PMChecklistData>();
    	buildAdapters();
    	currentEquipmentCategoryId = TagUnit.currentEquipmentCategoryId;
    	tl.removeAllViews();
    	cb1.setEnabled(true);
		cb2.setEnabled(true);
		cb3.setEnabled(true);
		cb4.setEnabled(true);
		cb5.setEnabled(true);
		cb6.setEnabled(true);
		cbrev.setEnabled(true);
    	readSQL( false );
    	
    	 /*cbrev.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
         
         tl.removeAllViews();
         // cb1.setEnabled(false);
           if (((CheckBox) v).isChecked())
         	  {
         	tl.removeAllViews();
         	readSQL( false );;
         }
         }
       });*/
    	}
	
	public void onResume()
		{
		init = true;
		
		if( !(currentEquipmentCategoryId == TagUnit.currentEquipmentCategoryId) )
			{
			currentEquipmentCategoryId = TagUnit.currentEquipmentCategoryId;
			checkListData.clear();
			tl.removeAllViews();
			
			readSQL(true);
			}
		
		super.onResume();
		}
	
	private void buildAdapters()
		{
		dirtyCheck = new OnCheckedChangeListener()
			{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked)
				{
				if( init )
					unitAct.dirtyFlag = true;
				}
			};
		
		max100 = new InputFilter[] {new InputFilter.LengthFilter(100)};
		}
	
	private boolean readAddressSQL()
		{
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId;
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				if(item.identifier.toUpperCase().contentEquals("ANNUALLY") || item.identifier.toUpperCase().contentEquals("SEMI-ANNUAL") || 
						item.identifier.toUpperCase().contentEquals("TRI-ANNUAL") || item.identifier.toUpperCase().contentEquals("QUARTERLY") || 
						item.identifier.toUpperCase().contentEquals("MONTHLY") || item.identifier.toUpperCase().contentEquals("BI-MONTHLY")  )
					{
					final CheckBox cbrev;
					final CheckBox cb1;
					final CheckBox cb2,cb3,cb4,cb5,cb6;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        cb5 = (CheckBox) findViewById(R.id.pm5);
			        cb6 = (CheckBox) findViewById(R.id.pm6);
					cb1.setEnabled(true);
					cb2.setEnabled(true);
					cb3.setEnabled(true);
					cb4.setEnabled(true);
					cb5.setEnabled(true);
					cb6.setEnabled(true);
					cbrev = (CheckBox) findViewById(R.id.pmrev);
					cbrev.setEnabled(false);
					}
				else{
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        cb5 = (CheckBox) findViewById(R.id.pm5);
			        cb6 = (CheckBox) findViewById(R.id.pm6);
					cb1.setEnabled(false);
					cb2.setEnabled(false);
					cb3.setEnabled(false);
					cb4.setEnabled(false);
					cb5.setEnabled(false);
					cb6.setEnabled(false);
					cbrev = (CheckBox) findViewById(R.id.pmrev);
					cbrev.setEnabled(true);
					createCheckListRow(item);
					
					}
				// leave the comments field to default of ""
				
				}
			while (cursor.moveToNext());
			}
		
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private boolean readAddressSQLr()
		{
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId;
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				if(item.identifier.toUpperCase().contentEquals("ANNUALLY") || item.identifier.toUpperCase().contentEquals("SEMI-ANNUAL") || 
						item.identifier.toUpperCase().contentEquals("TRI-ANNUAL") || item.identifier.toUpperCase().contentEquals("QUARTERLY") || 
						item.identifier.toUpperCase().contentEquals("MONTHLY") || item.identifier.toUpperCase().contentEquals("BI-MONTHLY")  )
					{
					final CheckBox cbrev;
					final CheckBox cb1;
					final CheckBox cb2,cb3,cb4,cb5,cb6;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        cb5 = (CheckBox) findViewById(R.id.pm5);
			        cb6 = (CheckBox) findViewById(R.id.pm6);
					cb1.setEnabled(true);
					cb2.setEnabled(true);
					cb3.setEnabled(true);
					cb4.setEnabled(true);
					cb5.setEnabled(true);
					cb6.setEnabled(true);
					cbrev = (CheckBox) findViewById(R.id.pmrev);
					cbrev.setEnabled(false);
						
					}else{
					
					createCheckListRowr(item);
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        cb5 = (CheckBox) findViewById(R.id.pm5);
			        cb6 = (CheckBox) findViewById(R.id.pm6);
					cb1.setEnabled(false);
					cb2.setEnabled(false);
					cb3.setEnabled(false);
					cb4.setEnabled(false);
					cb5.setEnabled(false);
					cb6.setEnabled(false);
					cbrev = (CheckBox) findViewById(R.id.pmrev);
					cbrev.setEnabled(true);
					}
				// leave the comments field to default of ""
				
				}
			while (cursor.moveToNext());
			}
		
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readAddressSQL1()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'ANNUALLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readAddressSQL2()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'SEMI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readAddressSQL3()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'TRI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readAddressSQL4()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'QUARTERLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private boolean readAddressSQL5()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readAddressSQL6()
		{
		boolean ret = false;
		
		String sqlQ = "SELECT pmchecklistId, itemText, itemType, identifier " +
				"FROM pmAddressChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId +
						" AND serviceAddressId = " + unitAct.tag.serviceAddressId + 
						" AND identifier = 'BI-MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private void readStdSQL()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId;
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				final CheckBox cb1;
				final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
		        cb1 = (CheckBox) findViewById(R.id.pm);
		        cb2 = (CheckBox) findViewById(R.id.pm2);
		        cb3 = (CheckBox) findViewById(R.id.pm3);
		        cb4 = (CheckBox) findViewById(R.id.pm4);
		        cb5 = (CheckBox) findViewById(R.id.pm5);
		        cb6 = (CheckBox) findViewById(R.id.pm6);
				cb1.setEnabled(false);
				cb2.setEnabled(false);
				cb3.setEnabled(false);
				cb4.setEnabled(false);
				cb5.setEnabled(false);
				cb6.setEnabled(false);
				cbrev = (CheckBox) findViewById(R.id.pmrev);
				cbrev.setEnabled(true);
				createCheckListRow(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void readStdSQLr()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId;
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRowr(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void readStdSQL1()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'ANNUALLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	
	private void readStdSQL2()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'SEMI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void readStdSQL3()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'TRI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void readStdSQL4()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'QUARTERLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void readStdSQL5()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private void readStdSQL6()
		{
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, identifier " +
				"FROM pmStdChecklist " +
				"WHERE equipmentCategoryId = " + TagUnit.currentEquipmentCategoryId + 
				" AND identifier = 'BI-MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		if ( cursor.moveToFirst() )
			{
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.identifier = cursor.getString(3);
				// Set the value for a newly generated CheckBox
				if( item.itemType.toUpperCase().contentEquals("C") )
					item.itemValue = "N";
				// leave the comments field to default of ""
				createCheckListRow2(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		}
	private boolean readPMChecklist(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId ;
		//+			" AND itemValue = 'Y' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		 CheckBox cb,cb1;
        cb = new CheckBox(this);
		CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
        cb1 = (CheckBox) findViewById(R.id.pm);
        cb2 = (CheckBox) findViewById(R.id.pm2);
        cb3 = (CheckBox) findViewById(R.id.pm3);
        cb4 = (CheckBox) findViewById(R.id.pm4);
        cb5 = (CheckBox) findViewById(R.id.pm5);
        cb6 = (CheckBox) findViewById(R.id.pm6);
        cbrev = (CheckBox) findViewById(R.id.pmrev);
        
	/*	cb1.setEnabled(false);
		cb2.setEnabled(false);
		cb3.setEnabled(false);
		cb4.setEnabled(false);
		cb5.setEnabled(false);
		cb6.setEnabled(false);*/
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				
				if(item.identifier != null)
					{
					// final CheckBox cb,cb1;
				      //  cb = new CheckBox(this);
					//	final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
				      //  cb1 = (CheckBox) findViewById(R.id.pm);
				     //   cb2 = (CheckBox) findViewById(R.id.pm2);
				     //   cb3 = (CheckBox) findViewById(R.id.pm3);
				     //   cb4 = (CheckBox) findViewById(R.id.pm4);
				     //   cb5 = (CheckBox) findViewById(R.id.pm5);
				     //   cb6 = (CheckBox) findViewById(R.id.pm6);
				     //   cbrev = (CheckBox) findViewById(R.id.pmrev);
				        
					/*	cb1.setEnabled(true);
						cb2.setEnabled(true);
						cb3.setEnabled(true);
						cb4.setEnabled(true);
						cb5.setEnabled(true);
						cb6.setEnabled(true);*/
				if(item.identifier.toUpperCase().contentEquals("ANNUALLY") )
					{
					//final CheckBox cb1;
					//final CheckBox cb2, cbrev,cb3,cb4;
			       // cb1 = (CheckBox) findViewById(R.id.pm);
			       // cb2 = (CheckBox) findViewById(R.id.pm2);
			       // cb3 = (CheckBox) findViewById(R.id.pm3);
			       // cbrev = (CheckBox) findViewById(R.id.pmrev);
					cbrev.setEnabled(false);
					cb1.setChecked(true);
					cb1.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("SEMI-ANNUAL"))
					{
					//final CheckBox cb1;
					//final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
			       // cb1 = (CheckBox) findViewById(R.id.pm);
			       // cb2 = (CheckBox) findViewById(R.id.pm2);
			       // cb3 = (CheckBox) findViewById(R.id.pm3);
			      //  cbrev = (CheckBox) findViewById(R.id.pmrev);
			        cbrev.setEnabled(false);
					cb2.setChecked(true);
					cb2.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("TRI-ANNUAL") )
					{
					
					//final CheckBox cb1;
					//final CheckBox cb2, cbrev,cb3,cb4;
			       // cb1 = (CheckBox) findViewById(R.id.pm);
			      //  cb2 = (CheckBox) findViewById(R.id.pm2);
			      //  cb3 = (CheckBox) findViewById(R.id.pm3);
			      //  cbrev = (CheckBox) findViewById(R.id.pmrev);
			        cbrev.setEnabled(false);
					cb3.setChecked(true);
					cb3.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("QUARTERLY") )
					{
					
					//final CheckBox cb1;
					//final CheckBox cb2, cbrev,cb3,cb4;
			       // cb1 = (CheckBox) findViewById(R.id.pm);
			       // cb2 = (CheckBox) findViewById(R.id.pm2);
			      //  cb3 = (CheckBox) findViewById(R.id.pm3);
			     //   cb4 = (CheckBox) findViewById(R.id.pm4);
			        cbrev = (CheckBox) findViewById(R.id.pmrev);
			        cbrev.setEnabled(false);
					cb4.setChecked(true);
					cb4.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("MONTHLY") )
					{
					
					//final CheckBox cb1;
					//final CheckBox cb2, cbrev,cb3,cb4,cb5;
			       // cb1 = (CheckBox) findViewById(R.id.pm);
			      //  cb2 = (CheckBox) findViewById(R.id.pm2);
			      //  cb3 = (CheckBox) findViewById(R.id.pm3);
			      //  cb4 = (CheckBox) findViewById(R.id.pm4);
			      //  cb5 = (CheckBox) findViewById(R.id.pm5);
			      //  cbrev = (CheckBox) findViewById(R.id.pmrev);
			        cbrev.setEnabled(false);
					cb5.setChecked(true);
					cb5.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("BI-MONTHLY") )
						{
						
					//	final CheckBox cb1;
					//	final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
				      //  cb1 = (CheckBox) findViewById(R.id.pm);
				      //  cb2 = (CheckBox) findViewById(R.id.pm2);
				      //  cb3 = (CheckBox) findViewById(R.id.pm3);
				      //  cb4 = (CheckBox) findViewById(R.id.pm4);
				      //  cb5 = (CheckBox) findViewById(R.id.pm5);
				      //  cb6 = (CheckBox) findViewById(R.id.pm6);
				      //  cbrev = (CheckBox) findViewById(R.id.pmrev);
				        cbrev.setEnabled(false);
						cb6.setChecked(true);
						cb6.setEnabled(false);
						}
					}
					
				createCheckListRow(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklistr(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId ;
		//+			" AND itemValue = 'Y' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				
				if(item.identifier != null)
					{
					
				if(item.identifier.toUpperCase().contentEquals("ANNUALLY") )
					{
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
					
					cb1.setChecked(true);
					cb1.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("SEMI-ANNUAL"))
					{
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
					
					cb2.setChecked(true);
					cb2.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("TRI-ANNUAL") )
					{
					
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
					
					cb3.setChecked(true);
					cb3.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("QUARTERLY") )
					{
					
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        
					cb4.setChecked(true);
					cb4.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("MONTHLY") )
					{
					
					final CheckBox cb1;
					final CheckBox cb2, cbrev,cb3,cb4,cb5;
			        cb1 = (CheckBox) findViewById(R.id.pm);
			        cb2 = (CheckBox) findViewById(R.id.pm2);
			        cb3 = (CheckBox) findViewById(R.id.pm3);
			        cb4 = (CheckBox) findViewById(R.id.pm4);
			        cb5 = (CheckBox) findViewById(R.id.pm5);
			        
					cb5.setChecked(true);
					cb5.setEnabled(false);
					}
				else if(item.identifier.toUpperCase().contentEquals("BI-MONTHLY") )
						{
						
						final CheckBox cb1;
						final CheckBox cb2, cbrev,cb3,cb4,cb5,cb6;
				        cb1 = (CheckBox) findViewById(R.id.pm);
				        cb2 = (CheckBox) findViewById(R.id.pm2);
				        cb3 = (CheckBox) findViewById(R.id.pm3);
				        cb4 = (CheckBox) findViewById(R.id.pm4);
				        cb5 = (CheckBox) findViewById(R.id.pm5);
				        cb6 = (CheckBox) findViewById(R.id.pm6);
						cb6.setChecked(true);
						cb6.setEnabled(false);
						}
					}
					
				createCheckListRowr(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklist1(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'ANNUALLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	
	private boolean readPMChecklist2(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'SEMI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklist3(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'TRI-ANNUAL' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklist4(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'QUARTERLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklist5(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private boolean readPMChecklist6(boolean newList)
		{
		if( newList )
			return false;
		
		boolean ret = false;
		
		String sqlQ = "SELECT pmCheckListId, itemText, itemType, itemValue, itemComment, identifier " +
				"FROM pmChecklist " +
				"WHERE pmChecklist.serviceTagUnitId = " + unitAct.serviceTagUnitId +
				" AND identifier = 'BI-MONTHLY' ";
		Cursor cursor = db.rawQuery(sqlQ);
		PMChecklistItem item;
		//tl.removeAllViews();
		if ( cursor.moveToFirst() )
			{
			ret = true;
			do
				{
				item = new PMChecklistItem();
				item.PMChecklistId = cursor.getInt(0);
				item.itemText = cursor.getString(1);
				item.itemType = cursor.getString(2);
				item.itemValue = cursor.getString(3);
				item.itemComment = cursor.getString(4);
				item.identifier = cursor.getString(5);
				createCheckListRow1(item);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return ret;
		}
	private void readSQLr(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklistr(newList) )
			if( !readAddressSQLr() )
				readStdSQLr();
		}
	
	private void readSQL(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist(newList) )
			if( !readAddressSQL() )
				readStdSQL();
		}
	private void readSQL1(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist1(newList) )
			if( !readAddressSQL1() )
				readStdSQL1();
		}
	
	private void readSQL2(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist2(newList) )
			if( !readAddressSQL2() )
				readStdSQL2();
		}
	private void readSQL3(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist3(newList) )
			if( !readAddressSQL3() )
				readStdSQL3();
		}
	private void readSQL4(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist4(newList) )
			if( !readAddressSQL4() )
				readStdSQL4();
		}
	private void readSQL5(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist5(newList) )
			if( !readAddressSQL5() )
				readStdSQL5();
		}
	private void readSQL6(boolean newList)
		{
		//tl.removeAllViews();
		if( !readPMChecklist6(newList) )
			if( !readAddressSQL6() )
				readStdSQL6();
		}
	private class PMChecklistItem
		{
		int PMChecklistId = 0;
		String itemText = "";
		String itemType = "C";
		String itemValue = "";
		String itemComment = "";
		String identifier = "";
		}
	
	private class PMChecklistData
		{
		int PMChecklistId = 0;
		String itemText = "";
		View editBox;
		EditText comments;
		String identifier = "";
		}
	
	private void createCheckListRow(PMChecklistItem item)
		{
		PMChecklistData data = new PMChecklistData();
		data.PMChecklistId = item.PMChecklistId;
		
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params0.weight = 1.5f;
		params0.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params2.weight = 1;
		params2.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params3.weight = 2;
		params3.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.LEFT); // Not sure if we want to left justify all rows. We want the checkbox left justified
		
		TextView tv = new TextView(this);
		tv.setText( item.itemText );
		tv.setLayoutParams(params1);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		data.itemText =  item.itemText;
		row.addView(tv);
		CheckBox cb; EditText et;
		
		TextView tv1 = new TextView(this);
		tv1.setText( item.identifier);
		tv1.setLayoutParams(params0);
		tv1.setTextColor(Twix_Theme.headerText);
		tv1.setTextSize(Twix_Theme.headerSize);
		data.identifier =  item.identifier;
		row.addView(tv1);
		
		if( item.itemType.toUpperCase().contentEquals("C") )
			{
			
			cb = new CheckBox(this);
			cb.setEnabled(!unitAct.tag.tagReadOnly);
			cb.setLayoutParams(params2);
			cb.setChecked(item.itemValue.contentEquals("Y"));
			//cb.setChecked(true);
			cb.setOnCheckedChangeListener(dirtyCheck);
			data.editBox = cb;
			row.addView(cb);
			}
		else
			{
			
			et = new EditText(this);
			et.setHint( "details" );
			et.setLayoutParams(params2);
			et.setTextColor(Twix_Theme.headerValue);
			et.setTextSize(Twix_Theme.headerSize);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!unitAct.tag.tagReadOnly);
			et.setText(item.itemValue);
			et.addTextChangedListener(unitAct.setDirtyFlag);
			et.setFilters(max100);
			data.editBox = et;
			row.addView(et);
			}
		
		et = new EditText(this);
		et.setHint("comments");
		et.setLayoutParams(params3);
		et.setTextColor(Twix_Theme.headerValue);
		et.setTextSize(Twix_Theme.headerSize);
		et.setBackgroundColor(Twix_Theme.editBG);
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setText(item.itemComment);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max100);
		data.comments = et;
		row.addView(et);
		
		tl.addView(row);
		checkListData.add(data);
		}
	private void createCheckListRowr(PMChecklistItem item)
		{
		PMChecklistData data = new PMChecklistData();
		data.PMChecklistId = item.PMChecklistId;
		
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params0.weight = 1.5f;
		params0.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params2.weight = 1;
		params2.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params3.weight = 2;
		params3.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.LEFT); // Not sure if we want to left justify all rows. We want the checkbox left justified
		
		TextView tv = new TextView(this);
		tv.setText( item.itemText );
		tv.setLayoutParams(params1);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		data.itemText =  item.itemText;
		row.addView(tv);
		CheckBox cb; EditText et;
		
		TextView tv1 = new TextView(this);
		tv1.setText( item.identifier);
		tv1.setLayoutParams(params0);
		tv1.setTextColor(Twix_Theme.headerText);
		tv1.setTextSize(Twix_Theme.headerSize);
		data.identifier =  item.identifier;
		row.addView(tv1);
		
		if( item.itemType.toUpperCase().contentEquals("C") )
			{
			
			cb = new CheckBox(this);
			cb.setEnabled(!unitAct.tag.tagReadOnly);
			cb.setLayoutParams(params2);
			//cb.setChecked(item.itemValue.contentEquals("Y"));
			cb.setChecked(true);
			cb.setOnCheckedChangeListener(dirtyCheck);
			data.editBox = cb;
			row.addView(cb);
			}
		else
			{
			
			et = new EditText(this);
			et.setHint( "details" );
			et.setLayoutParams(params2);
			et.setTextColor(Twix_Theme.headerValue);
			et.setTextSize(Twix_Theme.headerSize);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!unitAct.tag.tagReadOnly);
			et.setText(item.itemValue);
			et.addTextChangedListener(unitAct.setDirtyFlag);
			et.setFilters(max100);
			data.editBox = et;
			row.addView(et);
			}
		
		et = new EditText(this);
		et.setHint("comments");
		et.setLayoutParams(params3);
		et.setTextColor(Twix_Theme.headerValue);
		et.setTextSize(Twix_Theme.headerSize);
		et.setBackgroundColor(Twix_Theme.editBG);
		et.setEnabled(!unitAct.tag.tagReadOnly);
		et.setText(item.itemComment);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max100);
		data.comments = et;
		row.addView(et);
		
		tl.addView(row);
		checkListData.add(data);
		}
	private void createCheckListRow1(PMChecklistItem item)
		{
		PMChecklistData data = new PMChecklistData();
		data.PMChecklistId = item.PMChecklistId;
		
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params0.weight = 1.5f;
		params0.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params2.weight = 1;
		params2.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params3.weight = 2;
		params3.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.LEFT); // Not sure if we want to left justify all rows. We want the checkbox left justified
		
		TextView tv = new TextView(this);
		tv.setText( item.itemText );
		tv.setLayoutParams(params1);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		data.itemText =  item.itemText;
		row.addView(tv);
		CheckBox cb22; EditText et;
		
		TextView tv1 = new TextView(this);
		tv1.setText( item.identifier);
		tv1.setLayoutParams(params0);
		tv1.setTextColor(Twix_Theme.headerText);
		tv1.setTextSize(Twix_Theme.headerSize);
		data.identifier =  item.identifier;
		row.addView(tv1);
		
		
		if( item.itemType.toUpperCase().contentEquals("C") )
			{
			cb22 = new CheckBox(this);
			cb22.setEnabled(!unitAct.tag.tagReadOnly);
			cb22.setLayoutParams(params2);
			cb22.setChecked(true);
			cb22.setOnCheckedChangeListener(dirtyCheck);
			data.editBox = cb22;
			row.addView(cb22);
			}
		else
			{
			
			et = new EditText(this);
			et.setHint( "details" );
			et.setLayoutParams(params2);
			et.setTextColor(Twix_Theme.headerValue);
			et.setTextSize(Twix_Theme.headerSize);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!unitAct.tag.tagReadOnly);
			et.setText(item.itemValue);
			et.addTextChangedListener(unitAct.setDirtyFlag);
			et.setFilters(max100);
			data.editBox = et;
			row.addView(et);
			}
		
		et = new EditText(this);
		et.setHint("comments");
		et.setLayoutParams(params3);
		et.setTextColor(Twix_Theme.headerValue);
		et.setTextSize(Twix_Theme.headerSize);
		et.setBackgroundColor(Twix_Theme.editBG);
		et.setEnabled(!unitAct.tag.tagReadOnly);
		//et.setText(item.itemComment);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max100);
		data.comments = et;
		row.addView(et);
		
		tl.addView(row);
		checkListData.add(data);
		}
	private void createCheckListRow2(PMChecklistItem item)
		{
		PMChecklistData data = new PMChecklistData();
		data.PMChecklistId = item.PMChecklistId;
		
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params0.weight = 1.5f;
		params0.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params2.weight = 1;
		params2.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params3.weight = 2;
		params3.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.LEFT); // Not sure if we want to left justify all rows. We want the checkbox left justified
		
		TextView tv = new TextView(this);
		tv.setText( item.itemText );
		tv.setLayoutParams(params1);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		data.itemText =  item.itemText;
		row.addView(tv);
		CheckBox cb22; EditText et;
		
		TextView tv1 = new TextView(this);
		tv1.setText( item.identifier);
		tv1.setLayoutParams(params0);
		tv1.setTextColor(Twix_Theme.headerText);
		tv1.setTextSize(Twix_Theme.headerSize);
		data.identifier =  item.identifier;
		row.addView(tv1);
		
		
		if( item.itemType.toUpperCase().contentEquals("C") )
			{
			cb22 = new CheckBox(this);
			cb22.setEnabled(!unitAct.tag.tagReadOnly);
			cb22.setLayoutParams(params2);
			cb22.setChecked(true);
			cb22.setOnCheckedChangeListener(dirtyCheck);
			data.editBox = cb22;
			row.addView(cb22);
			}
		else
			{
			
			et = new EditText(this);
			et.setHint( "details" );
			et.setLayoutParams(params2);
			et.setTextColor(Twix_Theme.headerValue);
			et.setTextSize(Twix_Theme.headerSize);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!unitAct.tag.tagReadOnly);
			et.setText(item.itemValue);
			et.addTextChangedListener(unitAct.setDirtyFlag);
			et.setFilters(max100);
			data.editBox = et;
			row.addView(et);
			}
		
		et = new EditText(this);
		et.setHint("comments");
		et.setLayoutParams(params3);
		et.setTextColor(Twix_Theme.headerValue);
		et.setTextSize(Twix_Theme.headerSize);
		et.setBackgroundColor(Twix_Theme.editBG);
		et.setEnabled(!unitAct.tag.tagReadOnly);
		//et.setText(item.itemComment);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max100);
		data.comments = et;
		row.addView(et);
		
		tl.addView(row);
		checkListData.add(data);
		}
	/*private void createCheckListRow3(PMChecklistItem item)
		{
		PMChecklistData data = new PMChecklistData();
		data.PMChecklistId = item.PMChecklistId;
		
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params0.weight = 1.5f;
		params0.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params1.weight = 1.5f;
		params1.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params2.weight = 1;
		params2.setMargins(2, 2, 2, 2);
		
		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				0, LayoutParams.MATCH_PARENT);
		params3.weight = 2;
		params3.setMargins(2, 2, 2, 2);
		
		LinearLayout row = new LinearLayout(this);
		row.setLayoutParams(rowParams);
		row.setGravity(Gravity.LEFT); // Not sure if we want to left justify all rows. We want the checkbox left justified
		
		TextView tv = new TextView(this);
		tv.setText( item.itemText );
		tv.setLayoutParams(params1);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setTextSize(Twix_Theme.headerSize);
		data.itemText =  item.itemText;
		row.addView(tv);
		CheckBox cb22; EditText et;
		
		TextView tv1 = new TextView(this);
		tv1.setText( item.identifier);
		tv1.setLayoutParams(params0);
		tv1.setTextColor(Twix_Theme.headerText);
		tv1.setTextSize(Twix_Theme.headerSize);
		data.identifier =  item.identifier;
		row.addView(tv1);
		
		
		if( item.itemType.toUpperCase().contentEquals("C") )
			{
			cb22 = new CheckBox(this);
			cb22.setEnabled(!unitAct.tag.tagReadOnly);
			cb22.setLayoutParams(params2);
			cb22.setChecked(true);
			cb22.setOnCheckedChangeListener(dirtyCheck);
			data.editBox = cb22;
			row.addView(cb22);
			}
		else
			{
			
			et = new EditText(this);
			et.setHint( "details" );
			et.setLayoutParams(params2);
			et.setTextColor(Twix_Theme.headerValue);
			et.setTextSize(Twix_Theme.headerSize);
			et.setBackgroundColor(Twix_Theme.editBG);
			et.setEnabled(!unitAct.tag.tagReadOnly);
			et.setText(item.itemValue);
			et.addTextChangedListener(unitAct.setDirtyFlag);
			et.setFilters(max100);
			data.editBox = et;
			row.addView(et);
			}
		
		et = new EditText(this);
		et.setHint("comments");
		et.setLayoutParams(params3);
		et.setTextColor(Twix_Theme.headerValue);
		et.setTextSize(Twix_Theme.headerSize);
		et.setBackgroundColor(Twix_Theme.editBG);
		et.setEnabled(!unitAct.tag.tagReadOnly);
		//et.setText(item.itemComment);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max100);
		data.comments = et;
		row.addView(et);
		
		tl.addView(row);
		checkListData.add(data);
		}*/
	public void updateDB()
		{
		db.delete("pmCheckList", "serviceTagUnitId", unitAct.serviceTagUnitId );
		
		// Only insert when the equipmentCategoryId has not changed. Or else you could
		//	end up with a pmchecklist for an AC unit on a boiler
		if( currentEquipmentCategoryId == TagUnit.currentEquipmentCategoryId )
			{
			ContentValues cv = new ContentValues();
			PMChecklistData cur;
			
			int size = checkListData.size();
			for(int i = 0; i < size; i++ )
				{
				cur = checkListData.get(i);
				
				cv.put("pmChecklistId", cur.PMChecklistId);
				cv.put("serviceTagUnitId", unitAct.serviceTagUnitId);
				cv.put("itemText", cur.itemText);
				cv.put("identifier", cur.identifier);
				// itemType and itemValue
				if( cur.editBox.getClass() == CheckBox.class )
					{
					cv.put("itemType", "C");
					if( ((CheckBox)cur.editBox).isChecked() )
						cv.put("itemValue", "Y");
					else
						cv.put("itemValue", "N");
					}
				else
					{
					cv.put("itemType", "T");
					cv.put("itemValue", ((TextView)cur.editBox).getText().toString());
					}
				
				cv.put("itemComment", cur.comments.getText().toString());
				
				db.db.insertOrThrow("pmCheckList", null, cv);
				cv.clear();
				}
			}
		}
	
	/**
	 * Force the activity to use the activity group's provided back functionality
	 */
	@Override
	public void onBackPressed()
		{checkListData.clear();
		tl.removeAllViews();
		((Twix_TabActivityGroup)mContext).onBackPressed();
		}
	}
