package com.twix_agent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/*******************************************************************************************************************
 * Class: Twix_SQLite
 * 
 * Purpose: Provides an easy to access SQLite database for Twix_Agent. This class provides functionality to create,
 * 			modify, update, and manage the SQLite database. Many functions included are convenience functions.
 * 
 * Relevant XML: none
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_SQLite
	{
	private Twix_Application app;
	private static final String DATABASE_NAME = "twix_db.db";
	private static final int DATABASE_VERSION = 17;
	
	public static Vector<String> TableList;
	
	private final static String Tables[] = {
			"serviceAddress", 			//0 X
			"serviceAddressContact",	//1 X
			"dispatch",					//2 X
			"serviceTag",				//3 X
			"serviceTagUnit",			//4 X
			"serviceMaterial",			//5 X
			"serviceLabor",				//6 X
			"servicePhoto",				//7 X
			"equipmentCategory",		//8 X
			"equipment",				//9 X
			"fan",						//10 X
			"belt",						//11 X
			"sheave",					//12 X
			"filter",					//13 X
			"filterSize",				//14 X
			"filterType",				//15 X
			"refCircuit",				//16 X
			"Compressor",				//17 X
			"RefrigerantType",			//18 X
			"blue",						//19 X
			"blueUnit",					//20 X
			"pmAddressChecklist",		//21 X
			"pmCheckList",				//22 X
			"pmStdCheckList",			//23 X
			"openServiceTag",			//24 X
			"safetyChecklist",			//25 X
			"safetyTagChecklist",		//26 X
			"safetyTagChecklistItem",	//27 X
			"serviceDescription",		//28 X
			"mechanic",					//29 X
			"serviceReceipt",			//30 X
			"billing",					//31
			"serviceTagGroup",			//32
			"serviceTagGroupXref",		//33
			"serviceAddressTenant",		//34
			"closedBlue",				//35
			"closedBlueUnit",			//36
			"dispatchPriority"			//37
			};
	
	private final static int staticTables[] = { 7, 8, 14, 15, 18, 23, 24, 25, 28, 29, 30, 37  };
	
	private final static String DateTables[] = { "DispatchPriority", "Form", "FormSection", "AttrDef", "AttrXRef" }; 
	
	private Context context;
	public SQLiteDatabase db;

	private SQLiteStatement insertStmt;
	
	public Twix_SQLite(Context context, Twix_Application a)
		{
		this.app = a;
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		}
	
	// Helps to open the Database. If the database doesn't exist, it creates it. If it does exist, it opens it.
	//  If the database is out of date, it updates it.
	private class OpenHelper extends SQLiteOpenHelper
		{
		OpenHelper(Context context)
			{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
	
		@Override
		public void onCreate(SQLiteDatabase db)
			{
			Twix_SQLite.this.app.prefs.edit().putBoolean("firstStart", true)
											 .putBoolean("reqUpdate", false).commit();
			db.execSQL("CREATE TABLE " + Tables[0] + " (serviceAddressId INTEGER PRIMARY KEY, siteName TEXT, address1 TEXT" + 
					", address2 TEXT, city TEXT, state TEXT, zip TEXT, buildingNo TEXT, note TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[1] + " (contactId INTEGER PRIMARY KEY, serviceAddressId INTEGER" +
					", contactName TEXT, phone1 TEXT, phone1Type TEXT, phone2 TEXT, phone2Type TEXT, email TEXT, contactType TEXT" +
					", ext1 TEXT, ext2 TEXT, modified TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[2] + " (dispatchId INTEGER PRIMARY KEY, serviceAddressId TEXT, batchNo TEXT" +
					", jobNo TEXT, cusNo TEXT, altBillTo TEXT, contractType TEXT, dateStarted TEXT, dateEnded TEXT, dateOrdered TEXT" +
					", customerPO TEXT, requestedBy TEXT, requestedByPhone TEXT, requestedByEmail TEXT, siteContact TEXT, siteContactPhone TEXT" +
					", description TEXT, mechanic1 TEXT, mechanic2 TEXT, siteName TEXT, siteAddress1 TEXT, siteAddress2 TEXT" +
					", mechanic3 TEXT, mechanic4 TEXT, mechanic5 TEXT, mechanic6 TEXT, mechanic7 TEXT, tenant TEXT, PMComments TEXT, PMEstTime TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[3] + " (serviceTagId INTEGER PRIMARY KEY" +
					", serviceAddressId INTEGER, dispatchId INTEGER, serviceType TEXT, serviceDate TEXT" +
					", billTo TEXT, billAddress1 TEXT, billAddress2 TEXT, billAddress3 TEXT, billAddress4 TEXT, billAttn TEXT" +
					", siteName TEXT, tenant TEXT, address1 TEXT, address2 TEXT, city TEXT, state TEXT, zip TEXT, buildingNo TEXT, note TEXT" +
					", batchNo TEXT, jobNo TEXT, customerPO TEXT, requestedBy TEXT, requestedByPhone TEXT, requestedByEmail TEXT" +
					", description TEXT, empno TEXT, disposition TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[4] + " (serviceTagUnitId INTEGER PRIMARY KEY, serviceTagId INTEGER, equipmentId INTEGER, " +
					"servicePerformed TEXT, comments TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[5] + " (serviceMaterialId INTEGER PRIMARY KEY, serviceTagUnitId INTEGER, quantity REAL" +
					", materialDesc TEXT, cost REAL, refrigerantAdded TEXT, source TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[6] + " (serviceLaborId INTEGER PRIMARY KEY, serviceTagUnitId INTEGER, serviceDate TEXT" + 
					", regHours REAL, thHours REAL, dtHours REAL, mechanic TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[7] + " (servicePhotoId INTEGER PRIMARY KEY, serviceTagUnitId INTEGER, photoDate TEXT" + 
					", photo BLOB, comments TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[8] + " (equipmentCategoryId INTEGER PRIMARY KEY, categoryDesc TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[9] + " (equipmentId INTEGER PRIMARY KEY" +
					", serviceAddressId INTEGER, equipmentCategoryId INTEGER, unitNo TEXT, barCodeNo TEXT, manufacturer TEXT, model TEXT" + 
					", productIdentifier TEXT, serialNo TEXT, voltage TEXT, economizer TEXT, capacity TEXT, capacityUnits TEXT, refrigerantType TEXT" +
					", areaServed TEXT, mfgYear TEXT, dateInService TEXT, dateOutService TEXT, photo BLOB, notes TEXT" +
					", verifiedByEmpno TEXT, verified TEXT, modified TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[10] + " (fanId INTEGER PRIMARY KEY, equipmentId INTEGER, partType TEXT, number TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[11] + " (fanId INTEGER PRIMARY KEY, beltSize TEXT, quantity INTEGER)");
			
			db.execSQL("CREATE TABLE " + Tables[12] + " (fanId INTEGER, type TEXT, number TEXT, manufacturer TEXT)");	
			
			db.execSQL("CREATE TABLE " + Tables[13] + " (equipmentId, type TEXT, quantity INTEGER, filterSize TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[14] + " (filterSize TEXT, filterSizeId INTEGER PRIMARY KEY)");
			
			db.execSQL("CREATE TABLE " + Tables[15] + " (filterType TEXT, filterTypeId INTEGER PRIMARY KEY)");
			
			db.execSQL("CREATE TABLE " + Tables[16] + " (circuitId INTEGER PRIMARY KEY, equipmentId INTEGER, circuitNo INTEGER" + 
					", lbsRefrigerant real)");
			
			db.execSQL("CREATE TABLE " + Tables[17] + " (circuitId, compressorNo TEXT, manufacturer TEXT" + 
					", model TEXT, serialNo TEXT, dateInService TEXT, dateOutService TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[18] + " (RefrigerantType TEXT, RefrigerantTypeId INTEGER PRIMARY KEY)");
			
			db.execSQL("CREATE TABLE " + Tables[19] + " (blueId INTEGER PRIMARY KEY, serviceTagId INTEGER, dateCreated TEXT, completed TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[20] + " (blueUnitId INTEGER PRIMARY KEY, blueId INTEGER, equipmentId INTEGER, description TEXT, " + 
					"materials TEXT, cost REAL, laborHours REAL, notes TEXT, completed TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[21] + " (pmchecklistId INTEGER PRIMARY KEY, serviceAddressId INTEGER, equipmentCategoryId INTEGER, " +
					"itemText TEXT, itemType TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[22] + " (pmChecklistId INTEGER, serviceTagUnitId INTEGER, itemText TEXT, " +
					"itemType TEXT, itemValue TEXT, itemComment TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[23] + " (pmChecklistId INTEGER PRIMARY KEY, equipmentCategoryId INTEGER, itemText TEXT, " +
					"itemType TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[24] + " (serviceTagId INTEGER PRIMARY KEY" +
					", serviceAddressId INTEGER, dispatchId INTEGER, serviceType TEXT, serviceDate TEXT" +
					", billTo TEXT, billAddress1 TEXT, billAddress2 TEXT, billAddress3 TEXT, billAddress4 TEXT, billAttn TEXT" +
					", siteName TEXT, tenant TEXT, address1 TEXT, address2 TEXT, city TEXT, state TEXT, zip TEXT, buildingNo TEXT, note TEXT" +
					", batchNo TEXT, jobNo TEXT, customerPO TEXT, requestedBy TEXT, requestedByPhone TEXT, requestedByEmail TEXT" +
					", description TEXT, empno TEXT, disposition TEXT, completed TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[25] + " (safetyChecklistId INTEGER PRIMARY KEY, sortOrder INTEGER, LOTO TEXT, " +
					"itemType TEXT, itemText TEXT, itemTextBold TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[26] + " (serviceTagId INTEGER, checklistDate TEXT, comments TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[27] + " (serviceTagId INTEGER, safetyChecklistId INTEGER, itemRequired TEXT, " +
					"itemValue TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[28] + " (descriptionId INTEGER PRIMARY KEY, description TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[29] + " (mechanic TEXT, mechanic_name TEXT, Terminated TEXT, dept Text)");
			
			db.execSQL("CREATE TABLE " + Tables[30] + " (serviceReceiptId INTEGER PRIMARY KEY, serviceTagId INTEGER, photoDate TEXT" + 
					", photo BLOB, comments TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[31] + " (CustomerID TEXT, Name TEXT, AltBillId TEXT, Address1 TEXT, Address2 TEXT, " +
														"Address3 TEXT, Address4 TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[32] + " (groupId INTEGER PRIMARY KEY, signature BLOB, noSignatureReason TEXT, dateCreated TEXT, emailTo TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[33] + " (groupId INTEGER, serviceTagId INTEGER)");
			
			db.execSQL("CREATE TABLE " + Tables[34] + " (tenantId INTEGER PRIMARY KEY, serviceAddressId INTEGER, tenant TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[35] + " (blueId INTEGER PRIMARY KEY, serviceTagId INTEGER, dateCreated TEXT)");
			
			db.execSQL("CREATE TABLE " + Tables[36] + " (blueUnitId INTEGER PRIMARY KEY, blueId INTEGER, equipmentId INTEGER, " +
								"description TEXT, materials TEXT, laborHours REAL, notes TEXT, cost REAL)");
			
			db.execSQL("CREATE TABLE " + Tables[37] + " (PriorityId INTEGER PRIMARY KEY, RGBColor TEXT, DaysLate INTEGER, DateChanged TEXT)");
			
			CreateFormTables(db);
			CreateAttributeTables(db);
			}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
			{
			switch(oldVersion)
				{
				case 1:
					db.execSQL("ALTER TABLE serviceMaterial ADD COLUMN source TEXT");
				case 2:
					db.execSQL("CREATE TABLE pmCheckList_new (pmChecklistId INTEGER, serviceTagUnitId INTEGER, itemText TEXT, " +
						"itemType TEXT, itemValue TEXT, itemComment TEXT);" +
						
						"INSERT INTO pmChecklist_new SELECT * FROM pmCheckList;" +
						
						"DROP TABLE pmCheckList;" +
						
						"ALTER TABLE pmCheckList_new RENAME TO pmCheckList");
				case 3:
					db.execSQL(
						"CREATE TABLE serviceTagGroup " +
							"(groupId INTEGER PRIMARY KEY, signature BLOB, noSignatureReason TEXT, dateCreated TEXT, emailTo TEXT); " +
						
						"CREATE TABLE serviceTagGroupXref " +
							"(groupId INTEGER, serviceTagId INTEGER);" +
						"CREATE TABLE openServiceTag_new" +
							"(serviceTagId INTEGER PRIMARY KEY" +
							", serviceAddressId INTEGER, dispatchId INTEGER, serviceType TEXT, serviceDate TEXT" +
							", billTo TEXT, billAddress1 TEXT, billAddress2 TEXT, billAddress3 TEXT, billAddress4 TEXT, billAttn TEXT" +
							", siteName TEXT, address1 TEXT, address2 TEXT, city TEXT, state TEXT, zip TEXT, buildingNo TEXT, note TEXT" +
							", batchNo TEXT, jobNo TEXT, customerPO TEXT, requestedBy TEXT, requestedByPhone TEXT, requestedByEmail TEXT" +
							", description TEXT, empno TEXT, disposition TEXT, completed TEXT);" +
							
							"INSERT INTO openServiceTag_new" +
							"(serviceTagId, serviceAddressId, dispatchId, serviceType, serviceDate, billTo, billAddress1, billAddress2, " +
								"billAddress3, billAddress4, billAttn, siteName, address1, address2, city, state, zip, buildingNo, " +
								"note, batchNo, jobNo, customerPO, requestedBy, requestedByPhone, requestedByEmail, description, " +
								"empno, disposition, completed) " +
							"SELECT serviceTagId, serviceAddressId, dispatchId, serviceType, serviceDate, billTo, billAddress1, billAddress2, " +
								"billAddress3, billAddress4, billAttn, siteName, address1, address2, city, state, zip, buildingNo, " +
								"note, batchNo, jobNo, customerPO, requestedBy, requestedByPhone, requestedByEmail, description, " +
								"empno, disposition, 'N' " +
							"FROM openServiceTag;" +
							
							"DROP TABLE openServiceTag;" +
							
							"ALTER TABLE openServiceTag_new RENAME TO openServiceTag;" +
							
							"UPDATE openServiceTag SET completed = 'N';" +
							
							"ALTER TABLE dispatch ADD COLUMN tenant TEXT;" +
							
							"CREATE TABLE serviceAddressTenant (tenantId INTEGER PRIMARY KEY, serviceAddressId INTEGER, tenant TEXT);" +
							
							"ALTER TABLE mechanic ADD COLUMN Terminated TEXT;" );
							
				case 4:
					db.execSQL(
							"ALTER TABLE billing ADD COLUMN AltBillId TEXT;" );
				
				case 5:
					db.execSQL(
							"ALTER TABLE openServiceTag ADD COLUMN tenant TEXT;" +
							"ALTER TABLE serviceTag ADD COLUMN tenant TEXT;" );
				case 6:
					db.execSQL(
							"ALTER TABLE dispatch ADD COLUMN PMComments TEXT;" +
							"ALTER TABLE dispatch ADD COLUMN PMEstTime TEXT;" +
							"CREATE TABLE closedBlue (blueId INTEGER PRIMARY KEY, serviceTagId INTEGER, dateCreated TEXT);" +
							"CREATE TABLE closedBlueUnit (blueUnitId INTEGER PRIMARY KEY, blueId INTEGER, equipmentId INTEGER, " +
								"description TEXT, materials TEXT, laborHours REAL, notes TEXT, cost REAL);");
				case 7:
					db.execSQL("CREATE TABLE dispatchPriority (PriorityId INTEGER PRIMARY KEY, RGBColor TEXT, DaysLate INTEGER, DateChanged TEXT);" +
								"ALTER TABLE mechanic ADD COLUMN dept TEXT;");
				case 8:
					CreateFormTables(db);
					CreateAttributeTables(db);
				case 9:
					db.execSQL("CREATE TABLE serviceLaborRate (rateId INTEGER PRIMARY KEY, rate TEXT, ratedesc TEXT);" );
					db.execSQL("ALTER TABLE serviceLabor ADD COLUMN rate TEXT;" );
				case 10:
					db.execSQL("CREATE TABLE jobdoc (jobno TEXT, documentContents BLOB, documentName TEXT, documentTitle TEXT, dataSubmitted  TEXT,jobsite TEXT);");
				case 11:
					db.execSQL("DROP TABLE jobdoc;");
				case 12:
				db.execSQL("CREATE TABLE notes (noteid INTEGER PRIMARY KEY , serviceaddressid INTEGER, notes TEXT, modified text);");
				case 13:
				db.execSQL("ALTER TABLE pmAddressChecklist ADD COLUMN identifier TEXT;" );
				db.execSQL("ALTER TABLE pmChecklist ADD COLUMN identifier TEXT;" );
				db.execSQL("ALTER TABLE pmStdChecklist ADD COLUMN identifier TEXT;" );
				case 14:
				db.execSQL("ALTER TABLE blueUnit ADD COLUMN tradesmenhrs REAL;" );
				db.execSQL("ALTER TABLE blueUnit ADD COLUMN otherhrs REAL;" );
				db.execSQL("ALTER TABLE closedBlueUnit ADD COLUMN tradesmenhrs REAL;");
				db.execSQL("ALTER TABLE closedBlueUnit ADD COLUMN otherhrs REAL;");
				case 15:
				//db.execSQL("Drop Table serviceRefrigerant;" );
				db.execSQL("Create Table serviceRefrigerant (serviceRefrigerantId INTEGER PRIMARY KEY, serviceTagUnitId INTEGER, transferDate text, techName Text," +
				"typeOfRefrigerant TEXT, amount REAL, nameOfCylinder TEXT, cylinderSerialNo TEXT, transferedTo Text,modelNo Text,serialNo Text);");
				case 16:
				db.execSQL("ALTER TABLE serviceTag ADD COLUMN xoi_flag TEXT;");
				db.execSQL("ALTER TABLE openServiceTag ADD COLUMN xoi_flag TEXT;");
				}
			
			}
		
		private void CreateFormTables(SQLiteDatabase db)
			{
			db.execSQL("CREATE TABLE Form (FormId INTEGER PRIMARY KEY, type TEXT, EquipmentCategoryId INTEGER, " +
					"AttrId INTEGER, Description TEXT, DateChanged TEXT, VersionId INTEGER, VersionNum INTEGER)");
			
			db.execSQL("CREATE TABLE FormSection (FormSecId INTEGER PRIMARY KEY, Title TEXT, RowCnt INTEGER, " +
					"ColCnt INTEGER, DateChanged TEXT, VersionId INTEGER, VersionNum INTEGER)");
			
			db.execSQL("CREATE TABLE FormSecXRef (XRefId INTEGER, FormId INTEGER, FormSecId INTEGER, " +
					"SortOrder INTEGER, SectionType TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormSecXRef_PK ON FormSecXRef (XRefId, FormId, FormSecId)");
			
			db.execSQL("CREATE TABLE FormMatrix (MatrixId INTEGER PRIMARY KEY, " +
					"InputType TEXT, Image BLOB, ImageType TEXT)");
			
			db.execSQL("CREATE TABLE FormMatrixXRef (MatrixId INTEGER, " +
					"AttrId INTEGER, PickId INTEGER, Text TEXT, Modifiable TEXT, ChildFormId INTEGER, " +
					"Row INTEGER, Col INTEGER, " +
					"FormSecId INTEGER, RowSpan INTEGER, ColSpan INTEGER, " +
					"FontSize INTEGER, FontColor TEXT, Bold TEXT, Italic TEXT, Underline TEXT, " +
					"Align TEXT, VAlign TEXT, BGColor TEXT, BorderLeft TEXT, BorderBottom TEXT, " +
					"BorderRight TEXT, BorderTop TEXT, Required TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormMatrixXRef_PK ON FormMatrixXRef (MatrixId, Row, Col, FormSecId)");
			
			db.execSQL("CREATE TABLE FormOptions (FormSecId INTEGER, MatrixId INTEGER, Value TEXT)");
			// Index
			//	db.execSQL("CREATE UNIQUE INDEX FormOptions_PK ON FormOptions (FormSecId, MatrixId)");
			
			db.execSQL("CREATE TABLE FormSecHeights (FormSecId INTEGER, Row INTEGER, Height TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormSecHeights_PK ON FormSecHeights (FormSecId, Row)");
			
			db.execSQL("CREATE TABLE FormSecWidths (FormSecId INTEGER, Col INTEGER, Width TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormSecWidths_PK ON FormSecWidths (FormSecId, Col)");
			
			db.execSQL("CREATE TABLE FormType (FormType TEXT PRIMARY KEY, Description TEXT)");
			
			db.execSQL("CREATE TABLE PickList (PickId INTEGER PRIMARY KEY, Description TEXT, DateChanged TEXT)");
			
			db.execSQL("CREATE TABLE PickListItem (PickItemId INTEGER PRIMARY KEY, PickId INTEGER, " +
					"itemValue TEXT)");
			
			db.execSQL("CREATE TABLE FormData (FormDataId INTEGER PRIMARY KEY, FormId INTEGER, " +
					"ParentTable TEXT, ParentId INTEGER, LinkTable TEXT, LinkId INTEGER, " +
					"InputByEmpno TEXT, DateEntered TEXT, Completed TEXT, tabletMEID TEXT)");
			
			db.execSQL("CREATE TABLE FormDataValues (FormDataId INTEGER, XRefId INTEGER, MatrixTrail TEXT, " +
					"Value TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormDataValues_PK ON FormDataValues (FormDataId, XRefId, MatrixTrail)");
			
			db.execSQL("CREATE TABLE FormDataSignatures (FormDataId INTEGER, XRefId INTEGER, MatrixTrail TEXT, " +
					"Value BLOB)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX FormDataSignatures_PK ON FormDataSignatures (FormDataId, XRefId, MatrixTrail)");
			
				
			db.execSQL("CREATE TABLE FormPhotos (FormPhotoId INTEGER PRIMARY KEY, FormDataId INTEGER, Photo BLOB, " +
					"DateCreated TEXT, Comments TEXT)");
			}
		
		private void CreateAttributeTables(SQLiteDatabase db)
			{
			db.execSQL("CREATE TABLE AttrDef (AttrId INTEGER PRIMARY KEY, Type TEXT, Len INTEGER, " +
					"DisplayName TEXT, ShortName TEXT, SortOrder INTEGER, InputMask TEXT, " +
					"HostTable TEXT, HostColumn TEXT, DateChanged TEXT, Deprecated TEXT)");
			
			db.execSQL("CREATE TABLE AttrXRef (Parent INTEGER, Child TEXT, DateChanged TEXT)");
			// Index
				db.execSQL("CREATE UNIQUE INDEX AttrXRef_PK ON AttrXRef (Parent, Child)");
			
			db.execSQL("CREATE TABLE CategoryAttrLink (equipmentCategoryId INTEGER, AttrId TEXT)");
			}
		}
	
	
	
	public long insert(String TableName, List<String> l1, List<String> l2)
		{
		int size = l1.size();
		if( size != l2.size())
			return (-1);
		
		String s = "insert into " + TableName + "(";
		
		for( int i = 0; i < size; i++)
			{
			
			s += l1.get(i);
			if( i < size-1 )
				s += ", ";
			}
		s += ") values (";
		
		for( int i = 0; i < size; i++)
			{
			s += "?";
			if( i < size-1 )
				s += ", ";
			}
		
		s += ")";
		
		this.insertStmt = this.db.compileStatement(s);
		
		for( int i = 0; i < size; i++)
			{
			if( l2.get(i) == null )
				this.insertStmt.bindString( i+1, "" );
			else
				this.insertStmt.bindString( i+1, l2.get(i) );
			}
		
		return this.insertStmt.executeInsert();
		}
	
	public void bulkInsert(String TableName, List<String> l1, List<List<String>> l2)
		{
		int size = l1.size();
		if( size < 1 )
			return;
		
		int numRows = l2.size();
		if( numRows < 1 )
			return;
		
		int varCount = 0, rowCount = 0;
		int index = 0, lastIndex = 0;
		String s, select = "";
		
		while( index < numRows )
			{
			s = "insert into " + TableName + "(";
			for( int i = 0; i < size; i++)
				{
				s += l1.get(i);
				if( i < size-1 )
					s += ", ";
				}
			s += ")";
			
			// Loops through all the insert rows, incrementing index
			for( ; index < numRows; index++)
				{
				if( (varCount > 0) && (index < numRows) )
					{
					select = " UNION ALL";
					}
				else
					select = "";
				
				select += " SELECT ";
				// Adds the '?'s for the insert statement bindings
				for( int j = 0; (j < size) && (varCount < 800); j++, varCount++ )
					{
					select += "?";
					if( j < size-1 )
						select += ", ";
					}
				
				if( varCount < 800 )
					{
					s += select;
					rowCount++;
					}
				else
					{
					break;
					}
				}
			
			SQLiteStatement statement = db.compileStatement(s);
			
			String val;
			int bindTo = 0;
			for( int i = 0; i < rowCount; i++)
				{
				for( int j = 0; j < size; j++ )
					{
					val = l2.get(i+lastIndex).get(j);
					bindTo = ((size*(i))+(j+1));
					if( val == null )
						statement.bindString( bindTo, "" );
					else
						statement.bindString( bindTo, val );
					}
				}
			
			try
				{
				statement.executeInsert();
				}
			catch (Exception e)
				{
				Log.e("twix_agent:Twix_SQLite",
						"MAJOR ERROR: Bulk insert failed! Table: '" + TableName + "' Reason: " + e.getMessage(), e);
				}
			varCount = 0; rowCount = 0;
			lastIndex = index;
			}
		
		}
	
	public long insertForId(String TableName)
		{
		String s = "insert into " + TableName + "(";
		
		List<String> colHeaders = getHeaders( TableName );
		List<String> colTypes = getTypes( TableName, colHeaders );
		
		int size = colHeaders.size();
		for( int i = 0; i < size; i++)
			{
			
			s += colHeaders.get(i);
			if( i < size-1 )
				s += ", ";
			}
		s += ") values (";
		
		for( int i = 0; i < size; i++)
			{
			s += "?";
			if( i < size-1 )
				s += ", ";
			}
		
		s += ")";
		
		this.insertStmt = this.db.compileStatement(s);
		
		for( int i = 0; i < size; i++)
			{
			if( colTypes.get(i).contentEquals("BLOB") )
				this.insertStmt.bindString( i+1, null );
			else
				this.insertStmt.bindString( i+1, "" );
			}
		
		return this.insertStmt.executeInsert();
		}
	
	public int newNegativeId( String TableName, String ID )
		{
		int ret = 0;
		String sqlQ = "select MIN(" + ID + ") from " + TableName;
    	Cursor cursor = rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
			ret = cursor.getInt(0);
    	if (cursor != null && !cursor.isClosed())
			cursor.close();
    	
    	if( ret > 0 )
    		return -1;
    	
    	return --ret;
		}
	
	public void update( String TableName, String colName, byte[] bytes, String whereCheck, String expression )
		{
		String s = "UPDATE " + TableName + " SET " + colName + " = (?) "
				+ "WHERE " + whereCheck + " = '" + expression + "'";
		Object[] o = new Object[1];
		o[0] = bytes;
		db.execSQL(s, o);
		}
	
	/**
	 * Convenience method for nullifying one column with a clause
	 * 
	 * @param TableName
	 * @param colName
	 * @param whereCheck
	 * @param expression
	 */
	public void nullify( String TableName, String colName, String whereCheck, String expression )
		{
		ContentValues args = new ContentValues();
		args.putNull(colName);
		String[] expressionArg = new String[1];
		expressionArg[0] = expression;
		
		db.update(TableName, args, whereCheck, expressionArg);
		}
	
	public long insertPhoto( String TableName, String idName, String id, byte[] bytes, String comments )
		{
		String s = "INSERT INTO " + TableName + " ( " + idName + ", photoDate, photo, comments) VALUES (?, ?, ?, ?)";
		Calendar c = Calendar.getInstance();
		int temp = 0;
		String date = c.get(Calendar.YEAR) + "-";
		
		temp = c.get(Calendar.MONTH)+1;
		if( temp < 10 )
			date += "0" + temp;
		else
			date += temp;
		
		temp = c.get(Calendar.DAY_OF_MONTH);
		if( temp < 10 )
			date += "-0" + temp;
		else
			date += "-" + temp;
		
		insertStmt = db.compileStatement(s);
		
		insertStmt.bindString( 1, id );
		insertStmt.bindString( 2, date );
		insertStmt.bindBlob( 3, bytes );
		insertStmt.bindString( 4, comments );
		
		return insertStmt.executeInsert();
		}
	
	public void update( String TableName, List<String> l1, List<String> l2, String whereCheck, String expression)
		{
		int size = l1.size();
		if( size != l2.size() || size <= 0)
			return;
		
		String s = "UPDATE " + TableName + " SET ";
		
		for( int i = 0; i < size; i++)
			{
			s += l1.get(i);
			s += " = ";
			s += "(?)";
			if( i < size-1 )
				s += ", ";
			else
				s += " ";
			}
		
		s += "WHERE " + whereCheck + " = '" + expression + "'";
		
		String[] args = new String[l2.size()];
		l2.toArray(args);
		db.execSQL(s, args);
		}
	
	public void delete( String TableName, String whereCheck, String expression )
		{
		String s = "DELETE FROM " + TableName + " WHERE " + whereCheck + " = '" + expression + "'";
		db.execSQL(s);
		}
	
	public void delete( String TableName, List<String> whereCheck, List<String> expression )
		{
		int whereSize = whereCheck.size();
		int expressionSize = expression.size();
		
		if( (whereSize < 1) || (whereSize != expressionSize) )
			return;
		
		String s = "DELETE FROM " + TableName;
		s += " WHERE " + whereCheck.get(0) + " = '" + expression.get(0) + "'";
		for( int i = 1; i < whereSize; i++ )
			{
			s += " AND " + whereCheck.get(i) + " = '" + expression.get(i) + "'";
			}
		
		db.execSQL(s);
		}
	
	/**
	 * Convenience Method for deleting rows with all the expressions for a single ID
	 * 
	 * @param TableName
	 * @param whereCheck
	 * @param expression
	 */
	
	public void delete( String TableName, String whereCheck, List<String> expression )
		{
		int expressionSize = expression.size();
		
		if( (whereCheck.length() < 1) || (expressionSize < 1) )
			return;
		
		String s = "DELETE FROM " + TableName;
		s += " WHERE " + whereCheck + " = '" + expression.get(0) + "'";
		for( int i = 1; i < expressionSize; i++ )
			{
			s += " OR " + whereCheck + " = '" + expression.get(i) + "'";
			}
		
		db.execSQL(s);
		}
	
	/**
	 * 
	 * SQL Query in the following dynamic format
	 * 	DELETE FROM [TableName]
	 * 		WHERE [IDname] = '[ID]'
	 * 			AND (
	 * 				[whereCheck] [equal -> =/!= ] '[expression-0]'
	 * 				[inclusive -> OR / AND ]
	 * 				[whereCheck] [equal -> =/!= ] '[expression-1]'
	 * 				)
	 * 
	 * @param TableName
	 * @param IDname
	 * @param ID
	 * @param whereCheck
	 * @param expression
	 * @param inclusive
	 * @param equal
	 */
	
	public void deleteMultiClause( String TableName, String IDname, String ID,
			String whereCheck, List<String> expression, boolean inclusive, boolean equal)
		{
		int expressionSize = expression.size();
		
		// If ANY parameters are invalid, don't bother with the query
		if( (whereCheck.length() < 1) || (expressionSize < 1) || IDname.length() < 1 || ID.length() < 1 )
			return;
		
		String s = "DELETE FROM " + TableName;
		s += " WHERE " + IDname + " = '" + ID + "' AND ( ";
		for( int i = 0; i < expressionSize; i++ )
			{
			if( i > 0 )
				{
				if( inclusive )
					s += " OR ";
				else
					s += " AND ";
				}
			s += whereCheck;
			
			if( equal )
				s += " = '";
			else
				s += " != '";
			s += expression.get(i) + "'";
			}
		s+= " )";
		db.execSQL(s);
		}
	
	public void deleteAll()
		{
		deleteClosedPhotos();
		//migrateSignatures();
		// Hotfix for Migration
		this.db.delete("openServiceTag", null, null);
		
		for( int i = 0; i < Tables.length ; i++ )
			{
			if( canDeleteAll(i) )
				this.db.delete(Tables[i], null, null);
			}
		}
	
	private boolean canDeleteAll(int i)
		{
		for( int j = 0; j < staticTables.length; j++ )
			{
			if( staticTables[j] == i )
				return false;
			}
		
		return true;
		}
	
	public Cursor rawQuery(String s1, String[] s2)
		{
		return db.rawQuery(s1, s2);	
		}
	
	public Cursor rawQuery(String s1)
		{
		return db.rawQuery(s1, null);	
		}
	
	public void insertSignature(Bitmap bmp)
		{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bmp.compress(Bitmap.CompressFormat.PNG, 100, baos); 
		byte[] b = baos.toByteArray();
		
		//String s = String.format("%0" + b.length + "s", b.toString());
		StringBuilder sb = new StringBuilder(b.length * 2);
		  
	    Formatter formatter = new Formatter(sb);  
	    for (byte b2 : b)
	    	{  
	        formatter.format("%02x", b2);  
	    	}
		db.execSQL("INSERT INTO Signature(signatureId, sigPic) values(NULL, X'" + sb.toString().toUpperCase() +  "' )");	
		}
	
	public List<String> getHeaders( String tableName )
		{
		String sqlQ = "PRAGMA table_info(" + tableName + ")";
		Cursor cursor = db.rawQuery(sqlQ, null);
		List<String> list = new ArrayList<String>();
		
		if (cursor.moveToFirst())
			{
			do
				{
				list.add( cursor.getString(1) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return list;
		}
	
	public List<String> getTypes( String tableName, List<String> headers )
		{
		String sqlQ = "PRAGMA table_info(" + tableName + ")";
		Cursor cursor = db.rawQuery(sqlQ, null);
		List<String> list = new ArrayList<String>();
		int i = 0;
		int size = 0;
		if ( cursor.moveToFirst() )
			{
			if( headers != null)
				{
				size = headers.size();
				}
			do
				{
				if( headers != null)
					{
					if( cursor.getString(1).toLowerCase().contentEquals( headers.get(i).toLowerCase() ) )
						{
						list.add( cursor.getString(2) );
						i++;
						if( i >= size )
							break;
						}
					}
				else
					list.add( cursor.getString(2) );
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		return list;
		}
	
	private void deleteClosedPhotos()
    	{
    	String sqlQ = "SELECT serviceTagId FROM openServiceTag " +
    					"WHERE completed = 'Y'";
		Cursor cursor = db.rawQuery(sqlQ, null);
		List<String> tagList = new ArrayList<String>();
		if (cursor.moveToFirst())
			{
			do
				{
				tagList.add(cursor.getString(0));
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		sqlQ = "SELECT serviceTagUnitId FROM serviceTagUnit " +
				"WHERE 0=1";
		int size = tagList.size();
		for( int i = 0; i < size; i++ )
			{
			sqlQ += " OR serviceTagId = '" + tagList.get(i) +"'";
			}
		
		cursor = db.rawQuery(sqlQ, null);
		List<String> unitList = new ArrayList<String>();
		if (cursor.moveToFirst())
			{
			do
				{
				unitList.add(cursor.getString(0));
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		delete("servicePhoto", "serviceTagUnitId", unitList);
		delete("serviceReceipt", "serviceTagId", tagList);
    	}
	
	
	// Good Code :)
	private String[] ArgToStringArray(Long...args)
		{
		String whereArgs[] = new String[args.length];
		for( int i = 0; i < args.length; i++ )
			whereArgs[i] =  args[i] + "";
		
		return whereArgs;
		}
	
	public void update(String table, ContentValues values, String whereClause, Integer... args)
		{
		db.update(table, values, whereClause+"=?", ArgToStringArray(LongArrayToInteger(args)) );
		}
	
	public void update(String table, ContentValues values, String whereClause, Long... args)
		{
		db.update(table, values, whereClause+"=?", ArgToStringArray(args) );
		}
	
	private Long[] LongArrayToInteger(Integer... args)
		{
		Long[] argCast = new Long[args.length];
		for( int i = 0; i < args.length; i++ )
			argCast[i] = args[i].longValue();
		return argCast;
		}
	
	public void delete(String table, String whereClause, Integer... args)
		{
		delete(table, whereClause, LongArrayToInteger(args));
		}
	
	public void delete(String table, String whereClause, Long... args)
		{
		int size = args.length;
		if( size > 0 )
			{
			String sql = "DELETE FROM " + table + " WHERE " + whereClause + " IN (" + buildParams(size) + ")";
			SQLiteStatement stmt = db.compileStatement(sql);
			
			for( int i = 0; i < size; i++ )
				stmt.bindLong(i+1, args[i]);
			
			stmt.executeUpdateDelete();
			}
		
		}
	
	public void deleteList(String table, String whereClause, List<Integer> args)
		{
		int size = args.size();
		if( size > 0 )
			{
			String sql = "DELETE FROM " + table + " WHERE " + whereClause + " IN (" + buildParams(size) + ")";
			SQLiteStatement stmt = db.compileStatement(sql);
			
			for( int i = 0; i < size; i++ )
				stmt.bindLong(i+1, args.get(i));
			
			stmt.executeUpdateDelete();
			}
		}
	
	private String buildParams(int size)
		{
		String ret = "";
		
		for( int i = 0; i < size; i++ )
			{
			ret += "?";
			if( i < size-1 )
				ret += ", ";
			}
		
		return ret;
		}
	
	// Form Specific Functions
	public boolean DropAllForms()
		{
		boolean ret = true;
		
		db.beginTransaction();
		try
			{
			db.delete("Form", null, null);
			db.delete("FormSection", null, null);
			db.delete("FormSecXRef", null, null);
			db.delete("FormMatrix", null, null);
			db.delete("FormMatrixXRef", null, null);
			db.delete("FormOptions", null, null);
			db.delete("FormSecHeights", null, null);
			db.delete("FormSecWidths", null, null);
			db.delete("FormType", null, null);
			db.delete("FormPickList", null, null);
			db.delete("FormPickListItem", null, null);
			db.delete("FormData", null, null);
			db.delete("FormDataValues", null, null);
			
			db.setTransactionSuccessful();
			}
		catch( Exception e )
			{
			ret = false;
			}
		finally
			{
			db.endTransaction();
			}
		
		return ret;
		}
	
	// Sync Helper Functions
	public String GetLatestDate()
		{
		String ret = null;
		String sql = "SELECT MAX(DateChanged) FROM (";
		
		for( int i = 0; i < DateTables.length; i++ )
			{
			sql += "SELECT MAX(DateChanged) as DateChanged FROM " + DateTables[i] + " WHERE DateChanged IS NOT NULL ";
			if( i < DateTables.length-1 )
				sql += "UNION ALL ";
			}
		sql += ") as DateTable";
		
		Cursor cursor = rawQuery(sql);
		if (cursor.moveToFirst())
			{
			ret = cursor.getString(0);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	// Dynamic Statements
	public static class WhereClause
		{
		String column;
		ArrayList<Object> params;
		boolean not;
		
		public WhereClause()
			{
			column = "";
			params = new ArrayList<Object>();
			not = false;
			}
		}
	/**
	 * A convenience method to update a specific record with multiple clauses. This method does not
	 * 	reuse the SQL statement, so use it sparingly.
	 * @param table - Table name to update
	 * @param values - Content Values to update the record with
	 * @param Clauses - Where Clauses to pass into the update query
	 */
	public void update(String table, ContentValues values, ArrayList<WhereClause> Clauses)
		{
		String sql = "UPDATE " + table + " SET ";
		for( Iterator<String> i = values.keySet().iterator(); i.hasNext(); )
			{
			sql += i.next() + "=?";
			if( i.hasNext() )
				sql += ", ";
			}
		
		sql += "WHERE 1=1 ";
		WhereClause where;
		int size = Clauses.size();
		int sizej;
		for( int i = 0; i < size; i++ )
			{
			where = Clauses.get(i);
			sql += "AND (" + where.column;
			sizej = where.params.size();
			if( sizej > 1 )
				sql += "IN (";
			else
				sql += "=";
			for( int j = 0; j < sizej; j++ )
				{
				sql += "?";
				if( j < sizej-1 )
					sql += ", ";
				}
			if( sizej > 1 )
				sql += ")";
			sql += ") ";
			}
		
		SQLiteStatement stmt = db.compileStatement(sql);
		Object val;
		int index = 0;
		for( Iterator<String> i = values.keySet().iterator(); i.hasNext(); )
			{
			val = values.get(i.next());
			if( val == null )
				stmt.bindNull(index++);
			else if( val instanceof Integer || val instanceof Long )
				stmt.bindLong(index++, (Long)val);
			else if( val instanceof String )
				stmt.bindString(index++, (String)val);
			else if( val instanceof Double )
				stmt.bindDouble(index++, (Double)val);
			else if( val instanceof byte[] )
				stmt.bindBlob(index++, (byte[])val);
			}
		
		for( int i = 0; i < size; i++ )
			{
			where = Clauses.get(i);
			sizej = where.params.size();
			for( int j = 0; j < sizej; j++ )
				{
				val = where.params.get(j);
				//if( val == null )
				//	stmt.bindNull(index++);
				if( val instanceof Integer || val instanceof Long )
					stmt.bindLong(index++, (Long)val);
				else if( val instanceof String )
					stmt.bindString(index++, (String)val);
				else if( val instanceof Double )
					stmt.bindDouble(index++, (Double)val);
				else if( val instanceof byte[] )
					stmt.bindBlob(index++, (byte[])val);
				}
			}
		
		stmt.executeUpdateDelete();
		}
	
	public void delete(String table, ArrayList<WhereClause> Clauses)
		{
		String sql = "DELETE FROM " + table + " WHERE 1=1 ";
		WhereClause where;
		int size = Clauses.size();
		int sizej;
		for( int i = 0; i < size; i++ )
			{
			where = Clauses.get(i);
			sql += "AND (" + where.column;
			sizej = where.params.size();
			if( !where.not )
				{
				if( sizej > 1 )
					sql += " IN (";
				else
					sql += " = ";
				}
			else
				{
				if( sizej > 1 )
					sql += " NOT IN (";
				else
					sql += " != ";
				}
			
			for( int j = 0; j < sizej; j++ )
				{
				sql += "?";
				if( j < sizej-1 )
					sql += ", ";
				}
			if( sizej > 1 )
				sql += ")";
			sql += ") ";
			}
		
		SQLiteStatement stmt = db.compileStatement(sql);
		Object val;
		int index = 1;
		for( int i = 0; i < size; i++ )
			{
			where = Clauses.get(i);
			sizej = where.params.size();
			for( int j = 0; j < sizej; j++ )
				{
				val = where.params.get(j);
				if( val == null )
					stmt.bindNull(index++);
				if( val instanceof Integer || val instanceof Long )
					stmt.bindLong(index++, (Long)val);
				else if( val instanceof String )
					stmt.bindString(index++, (String)val);
				else if( val instanceof Double )
					stmt.bindDouble(index++, (Double)val);
				else if( val instanceof byte[] )
					stmt.bindBlob(index++, (byte[])val);
				}
			}
		
		stmt.executeUpdateDelete();
		}
	}
