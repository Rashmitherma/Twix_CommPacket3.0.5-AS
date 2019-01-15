package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*******************************************************************************************************************
 * Class: Twix_AgentServiceUnitPhoto
 * 
 * Purpose: Allows the user to take photos of pieces of equipment. These photos are tagged with a date/time stamp
 * 			and user comments. They can also attach the photos from their device to the tag.
 * 
 * Relevant XML: servicetag_photo.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentServiceUnitPhoto extends Activity
	{
	public Twix_AgentServiceUnitTabHost unitAct;
	private Twix_Application app;
	private Twix_SQLite db;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private Button UnitInfo;
	private LinearLayout ll;
	private static final int	REQUEST_PATH	= 1;
	private Uri imageUri;
	private File file;
	private File file1;
	private List<Integer> removeIdList;
	private List<View> photoList;
	
	private static final int IMG_ID = 1001;
	
	// Input Filters
	private InputFilter[] max5k;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.servicetag_photo, null);
		this.setContentView( viewToLoad );
        
		removeIdList = new ArrayList<Integer>();
		photoList = new ArrayList<View>();
		
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		ll = (LinearLayout) findViewById(R.id.PhotoTableHost);
		
		unitAct = (Twix_AgentServiceUnitTabHost) getParent();
		LocalActivityManager manager2 = unitAct.getLocalActivityManager();
		//UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).spinnerUnitNo;
		UnitInfo = ((Twix_AgentServiceTagUnit)manager2.getActivity("Unit")).unitSpinner;
		readOnlySetup();
		max5k = new InputFilter[] {new InputFilter.LengthFilter(5000)};
		
		readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( unitAct.tag.tagReadOnly )
			{
			findViewById(R.id.TakePhoto).setVisibility(View.INVISIBLE);
			}
		}
	
	private void readSQL()
		{
		String sqlQ = "select servicePhotoId, photoDate, " +
				"comments, photo " +
			"from servicePhoto " +
			"where servicePhoto.serviceTagUnitId = " + unitAct.serviceTagUnitId;

		Cursor cursor = db.rawQuery(sqlQ);
		PhotoData data;
		if ( cursor.moveToFirst() )
			{
			do
				{
				data = new PhotoData(false);
				data.servicePhotoId = cursor.getInt(0);
				data.photoDate = cursor.getString(1);
				data.comments = cursor.getString(2);
				data.Photo = cursor.getBlob(3);
				
				addPhoto(data);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	private class PhotoData
		{
		int servicePhotoId = 0;
		String photoDate = "";
		String comments = "";
		
		// Not null when the image is new. Otherwise null
		byte[] Photo;
		
		// Android View Field References
		EditText commentsView;
		ImageView photoView;
		
		boolean delete = false;
		boolean newPhoto;
		
		public PhotoData(boolean newPhoto)
			{
			this.newPhoto = newPhoto;
			}
		
		/**
		 * Returns a Bitmap object used for display purposes.
		 * 
		 * ***Warning: This can ONLY be called once. After that, the byte array has
		 * 				already been marked for garbage collection.
		 * @return - A Bitmap for display purposes
		 */
		public Bitmap getPhoto()
			{
			if( Photo != null )
				{
				System.gc();
				BitmapFactory.Options opts=new BitmapFactory.Options();
				opts.inDither=false;                     //Disable Dithering mode
				opts.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
				opts.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
				opts.inTempStorage=new byte[32 * 1024];
				
				Bitmap ret = BitmapFactory.decodeByteArray(Photo, 0, Photo.length, opts);
				// Setup for the garbage collection. We no longer need a reference to the photo
				Photo = null;
				return ret;
				}
			return null;
			}
		}
	
	private void addPhoto( PhotoData data )
		{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
		params.setMargins(3, 3, 3, 3);
		
		LinearLayout.LayoutParams paramsHost = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		paramsHost.setMargins(3, 3, 3, 3);
		
		RelativeLayout.LayoutParams paramsSub = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		paramsSub.setMargins(3, 3, 3, 3);
		
		RelativeLayout.LayoutParams paramsSub2 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		paramsSub2.setMargins(3, 3, 3, 3);
		paramsSub2.addRule(RelativeLayout.ALIGN_RIGHT, IMG_ID );
		paramsSub2.addRule(RelativeLayout.ALIGN_TOP, IMG_ID);
		
		LinearLayout host = new LinearLayout(this);
		host.setTag( data );
		host.setLayoutParams(paramsHost);
		host.setBackgroundColor(Twix_Theme.tableBG);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setPadding(10, 10, 10, 10);
		
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setBackgroundColor(Twix_Theme.headerBG);
		tv.setTextColor(Twix_Theme.headerText);
		tv.setText( Twix_TextFunctions.DBtoNormal(data.photoDate) );
		tv.setTextSize(Twix_Theme.headerSize);
		tv.setPadding( 5, 5, 5, 5 );
		host.addView(tv);
		
		RelativeLayout lay1 = new RelativeLayout(this);
		lay1.setLayoutParams(paramsSub);
		host.addView(lay1);
		
		ImageView iv = new ImageView(this);
		iv.setLayoutParams(paramsSub);
		iv.setImageBitmap(data.getPhoto());
		iv.setId(IMG_ID);
		lay1.addView(iv);
		data.photoView = iv;
		
		Button ib = new Button(this);
		ib.setLayoutParams(paramsSub2);
		ib.setBackgroundResource(R.drawable.minus2);
		ib.setOnClickListener( new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	// ImageButton (ib) -> LinearLayout (lay1) -> LinearLayout (host) -> LinearLayout (PhotoTableHost)
            	View host = (View) v.getParent().getParent();
            	PhotoData row = (PhotoData) host.getTag();
            	row.delete = true;
            	if( row.servicePhotoId != 0 )
            		removeIdList.add(row.servicePhotoId);
            	photoList.remove(host);
            	ll.removeView(host);
            	}
        	});
		lay1.addView(ib);
		
		EditText et = new EditText(this);
		et.setLayoutParams(params);
		et.setHint("comments");
		et.setMinLines(2);
		et.setGravity(Gravity.TOP);
		et.setText( data.comments );
		et.setBackgroundResource(R.drawable.editbox);
		et.addTextChangedListener(unitAct.setDirtyFlag);
		et.setFilters(max5k);
		data.commentsView = et;
		
		host.addView(et);
		
		if( data.servicePhotoId == 0 )
			photoList.add(host);
		
		ll.addView(host);
		}
	
	private File createPhotoLocation()
		{
	    try
			{
			File tmp = Environment.getExternalStorageDirectory();
			tmp = new File( tmp.getAbsolutePath() + "/.temp/" );
			if (!tmp.exists())
				{
				tmp.mkdir();
				}
			
			return File.createTempFile("pic", ".jpg", tmp);
			}
		catch ( IOException e )
			{
			return null;
			}
		}
	
	
	public void takePhoto()
		{
		try
			{
			file = createPhotoLocation();
			}
		catch ( Exception e )
			{
			Toast.makeText(mContext, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
			}
		imageUri = Uri.fromFile(file);
		
		Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
		intent.putExtra( MediaStore.EXTRA_OUTPUT, imageUri );
		((Twix_TabActivityGroup)mContext).startActivityForResult( intent, Twix_AgentActivityGroup2.TAKE_PIC );
		}
	public void takePhoto2()
		{
		try
			{
			
			//openGallery();
			Toast.makeText(mContext, "Hello!", Toast.LENGTH_LONG).show();
			Intent data = null;
			Intent intent1 = new Intent(this, photopicker.class);
			((Twix_TabActivityGroup)mContext).startActivityForResult( intent1, Twix_AgentActivityGroup2.PHOTO );
						
			}
		catch ( Exception e )
			{
			Toast.makeText(mContext, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
			}
		
		}
 
	public void getpic(String curFileName)
		{
	//	Toast.makeText(this, "Fetch the File and convert to picture location 2" +curFileName, Toast.LENGTH_SHORT).show();
		 Bitmap bmp = BitmapFactory.decodeFile(curFileName);
		 String ext =curFileName.toString().substring(curFileName.toString().length()-3);
		 if(ext.equals("jpg"))
			 {
		 //Toast.makeText(mContext, "extension is " +a, Toast.LENGTH_LONG).show();
		 if( bmp != null )
				{
				int width = bmp.getWidth() / 5;
				int height = bmp.getHeight() / 5;
				bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				bmp.compress(CompressFormat.JPEG, 35, os);
				bmp.recycle();
				byte[] bytes = os.toByteArray();
				//bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Date date = new Date();
				
			//file.delete();
				
				PhotoData data = new PhotoData(true);
				data.Photo = bytes;
				data.photoDate = dateFormat.format(date);
				addPhoto( data );
				unitAct.dirtyFlag = true;
				}
			 }
		 if(ext.equals("png"))
			 {
		 //Toast.makeText(mContext, "extension is " +a, Toast.LENGTH_LONG).show();
		 if( bmp != null )
				{
				int width = bmp.getWidth() / 5;
				int height = bmp.getHeight() / 5;
				bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				bmp.compress(CompressFormat.PNG, 35, os);
				bmp.recycle();
				byte[] bytes = os.toByteArray();
				//bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Date date = new Date();
				
			//file.delete();
				
				PhotoData data = new PhotoData(true);
				data.Photo = bytes;
				data.photoDate = dateFormat.format(date);
				addPhoto( data );
				unitAct.dirtyFlag = true;
				}
			 }
			 }
		
	public void photoListAdd()
		{
		Bitmap bmp = BitmapFactory.decodeFile( imageUri.getPath() );
		if( bmp != null )
			{
			int width = bmp.getWidth() / 5;
			int height = bmp.getHeight() / 5;
			bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, 35, os);
			bmp.recycle();
			byte[] bytes = os.toByteArray();
			//bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date date = new Date();
			
			file.delete();
			
			PhotoData data = new PhotoData(true);
			data.Photo = bytes;
			data.photoDate = dateFormat.format(date);
			addPhoto( data );
			unitAct.dirtyFlag = true;
			}
		else
			Toast.makeText(mContext, "Failed to read photo. Please try again", Toast.LENGTH_LONG).show();
		}
	
	public void updateDB()
		{
		// Remove all the rows with IDs marked for deletion
		db.deleteList("servicePhoto", "servicePhotoId", removeIdList );
		
		ContentValues cv = new ContentValues();
		PhotoData row;
		Bitmap bmp;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int size = photoList.size();
		for( int i = 0; i < size; i++ )
			{
			row = (PhotoData) photoList.get(i).getTag();
			
			if( row.newPhoto )
				{
				cv.put("servicePhotoId", db.newNegativeId("servicePhoto", "servicePhotoId"));
				cv.put("serviceTagUnitId", unitAct.serviceTagUnitId);
				cv.put("photoDate", row.photoDate);
				cv.put("comments", row.commentsView.getText().toString() );
				
				bmp = ((BitmapDrawable)row.photoView.getDrawable()).getBitmap();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				cv.put("photo", bos.toByteArray() );
				// Cleanup the variables
				bos.reset();
				bmp.recycle();
				System.gc();
				
				db.db.insertOrThrow("servicePhoto", null, cv);
				}
			else if( !row.delete )
				{
				cv.put("comments", row.commentsView.getText().toString() );
				db.update("servicePhoto", cv, "servicePhotoId", row.servicePhotoId);
				}
			
			cv.clear();
			}
		/*
		PhotoRow row;
		int size = photoList.size();
		for( int i = 0; i < size; i++ )
			{
			row = (PhotoRow) photoList.get(i).getTag();
			
			cv.put("servicePhotoId", db.newNegativeId("servicePhoto", "servicePhotoId"));
			cv.put("serviceTagUnitId", unitAct.serviceTagUnitId);
			
			cv.put("photoDate", row.photoDate);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			row.photo.compress(CompressFormat.JPEG, 100, bos); 
			byte[] bytes = bos.toByteArray();
			cv.put("photo", bytes );
			
			cv.put("comments", row.comments.getText().toString() );
			
			db.db.insertOrThrow("servicePhoto", null, cv);
			cv.clear();
			}
		*/
		}
	
	public void onResume()
		{
		//((TextView)findViewById(R.id.UnitInfo)).setText( (String) UnitInfo.getSelectedItem() );
		((TextView)findViewById(R.id.UnitInfo)).setText( UnitInfo.getText() );
		
		super.onResume();
		}
	
	/**
	 * Force the activity to use the activity group's provided back functionality
	 */
	@Override
	public void onBackPressed()
		{
		((Twix_TabActivityGroup)mContext).onBackPressed();
		}
	}
