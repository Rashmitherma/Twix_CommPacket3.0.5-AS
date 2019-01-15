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
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
 * Class: Twix_AgentOpenTagReceipt
 * 
 * Purpose: Contains pictures to be posted to the server as receipts. Photos are scaled down and grayscaled to
 * 			minimize bandwidth. Each photo can contain comments and is automatically timestamped. 
 * 
 * Note:	Receipt photos are lost when switching users or tablets since the photos are not stored on the
 * 			central server.
 *  
 * Relevant XML: open_tag_receipt.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentOpenTagReceipt extends Activity
	{
	private Twix_Application app;
	private Twix_SQLite db;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	private Twix_AgentOpenTag TagAct;
	private LinearLayout ll;
	
	private Uri imageUri;
	private File file;
	private List<Integer> removeIdList;
	private List<View> photoList;
	
	private static final int IMG_ID = 1001;
	
	// Input Filters
	private InputFilter[] max5k;
	
	public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent().getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.open_tag_receipt, null);
		this.setContentView( viewToLoad );
        
		removeIdList = new ArrayList<Integer>();
		photoList = new ArrayList<View>();
		
		app = (Twix_Application) getApplication();
		db = app.db;
		Twix_Theme = app.Twix_Theme;
		
		ll = (LinearLayout) findViewById(R.id.PhotoTableHost);
		
        LocalActivityManager manager = ((Twix_TabActivityGroup)mContext).getLocalActivityManager();
		LocalActivityManager manager2 = ((TabActivity)manager.getActivity("Twix_AgentOpenTagsTabHost")).getLocalActivityManager();
		TagAct = (Twix_AgentOpenTag)manager2.getActivity("Tag");
		readOnlySetup();
		max5k = new InputFilter[] {new InputFilter.LengthFilter(5000)};
		
		readSQL();
    	}
	
	private void readOnlySetup()
		{
		if( TagAct.tagReadOnly )
			{
			findViewById(R.id.TakeReceipt).setVisibility(View.INVISIBLE);
			}
		}
	
	private class ReceiptData
		{
		int serviceReceiptId = 0;
		String photoDate = "";
		String comments = "";
		
		// Not null when the image is new. Otherwise null
		byte[] Photo;
		
		// Android View Field References
		EditText commentsView;
		ImageView photoView;
		
		boolean delete = false;
		boolean newPhoto;
		
		public ReceiptData(boolean newPhoto)
			{
			this.newPhoto = newPhoto;
			}
		//Bitmap photo;
		
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
	
	private void readSQL()
		{
		String sqlQ = "select serviceReceipt.serviceReceiptId, serviceReceipt.photoDate, " +
				"serviceReceipt.comments, serviceReceipt.photo " +
			"from serviceReceipt " +
			"where serviceReceipt.serviceTagId = " + TagAct.serviceTagId;

		Cursor cursor = db.rawQuery(sqlQ);
		ReceiptData data;
		if ( cursor.moveToFirst() )
			{
			do
				{
				data = new ReceiptData(false);
				data.serviceReceiptId = cursor.getInt(0);
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
	
	private void addPhoto( ReceiptData data )
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
		
		if( !TagAct.tagReadOnly )
			{
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
	            	ReceiptData row = (ReceiptData) host.getTag();
	            	row.delete = true;
	            	if( row.serviceReceiptId != 0 )
	            		removeIdList.add(row.serviceReceiptId);
	            	photoList.remove(host);
	            	ll.removeView(host);
	                }
	        	});
			lay1.addView(ib);
			}
		
		EditText et = new EditText(this);
		et.setLayoutParams(params);
		et.setHint("comments");
		et.setMinLines(2);
		et.setGravity(Gravity.TOP);
		et.setText( data.comments );
		et.setEnabled(!TagAct.tagReadOnly);
		et.setBackgroundResource(R.drawable.editbox);
		et.setFilters(max5k);
		data.commentsView = et;
		
		host.addView(et);
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
			Toast.makeText(mContext, "Error: Not enough space to store a new photo.\nPlease Check the device storage and make sure there is space.", Toast.LENGTH_LONG).show();
			}
		imageUri = Uri.fromFile(file);
		
		Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
		intent.putExtra( MediaStore.EXTRA_OUTPUT, imageUri );
		intent.putExtra( MediaStore.EXTRA_VIDEO_QUALITY, 1 );
		((Twix_TabActivityGroup)mContext).startActivityForResult( intent, Twix_AgentActivityGroup2.TAKE_RECEIPT );
		}
	
	public void photoListAdd()
		{
		// 0 - Call the garbage collector to ensure we have enough memory to work with bitmaps
		System.gc();
		
		// 1 - Put the image into memory
		Bitmap camera_bmp = BitmapFactory.decodeFile( imageUri.getPath() );
		Bitmap color_bmp;
		
		/*
		if( color_bmp != null )
			{
			// 2 - Scale the image by 1/4
			int width = color_bmp.getWidth() / 4;
			int height = color_bmp.getHeight() / 4;
			color_bmp = Bitmap.createScaledBitmap(color_bmp, width, height, true);
			Bitmap grayscale_bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			
			int pixel, A, R, G, B, gray;
			for (int x = 0; x < width; ++x)
				{
				for (int y = 0; y < height; ++y)
					{
					pixel = color_bmp.getPixel(x, y);
					
					A = Color.alpha(pixel);
	                R = Color.red(pixel);
	                G = Color.green(pixel);
	                B = Color.blue(pixel);
					
	                gray = (int)(0.299 * R + 0.587 * G + 0.114 * B);
	                
					grayscale_bmp.setPixel(x,y, Color.argb(A, gray, gray, gray));
					}
				}
			*/
			int width = camera_bmp.getWidth() / 4;
			int height = camera_bmp.getHeight() / 4;
			color_bmp = Bitmap.createScaledBitmap(camera_bmp, width, height, true);
			camera_bmp.recycle(); // As soon as we are done with the camera bitmap, recycle it
			Bitmap grayscale_bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			
			
			// 3 - Convert the image to black and white
			Canvas c = new Canvas(grayscale_bmp);
			Paint p = new Paint();
			
		    ColorMatrix cm = new ColorMatrix();
		    cm.setSaturation(0);
		    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
		    p.setColorFilter(filter);
		    c.drawBitmap(color_bmp, 0, 0, p);
		    color_bmp.recycle(); // As soon as we are done with the color bitmap, recycle it
		    
		   
			// 4 - Remove the old file and add the photo.
		    file.delete();
			
		    // 5. Convert the bitmap to a byte array so everything is declared in the heap
		    ByteArrayOutputStream os = new ByteArrayOutputStream();
		    grayscale_bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
		    byte[] bytes = os.toByteArray();
		    grayscale_bmp.recycle();
		    
		    // 6. Create the current date
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date date = new Date();
		    
			// 7. Create the Receipt Data and pass it along into the view creation functions
		    ReceiptData data = new ReceiptData(true);
			data.Photo = bytes;
			data.photoDate = dateFormat.format(date);
			addPhoto(data);
		//	}
		//else
		//	Toast.makeText(mContext, "Failed to read photo. Please try again", Toast.LENGTH_LONG).show();
		}
	
	public void updateDB()
		{
		// Remove all the rows with IDs marked for deletion
		db.deleteList("serviceReceipt", "serviceReceiptId", removeIdList );
		
		ContentValues cv = new ContentValues();
		ReceiptData row;
		Bitmap bmp;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int size = photoList.size();
		for( int i = 0; i < size; i++ )
			{
			row = (ReceiptData) photoList.get(i).getTag();
			
			if( row.newPhoto )
				{
				cv.put("serviceReceiptId", db.newNegativeId("serviceReceipt", "serviceReceiptId"));
				cv.put("serviceTagId", TagAct.serviceTagId);
				cv.put("photoDate", row.photoDate);
				cv.put("comments", row.commentsView.getText().toString() );
				
				bmp = ((BitmapDrawable)row.photoView.getDrawable()).getBitmap();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				cv.put("photo", bos.toByteArray() );
				// Cleanup the variables
				bos.reset();
				bmp.recycle();
				System.gc();
				
				db.db.insertOrThrow("serviceReceipt", null, cv);
				}
			else if( !row.delete )
				{
				cv.put("comments", row.commentsView.getText().toString() );
				db.update("serviceReceipt", cv, "serviceReceiptId", row.serviceReceiptId);
				}
			
			cv.clear();
			}
		}
	
	public void onResume()
		{
		((TextView)findViewById(R.id.TagInfo)).setText( ((TextView)TagAct.tvs.get(0)).getText() );
		
		super.onResume();
		}
	
	public void onDestroy()
		{
		if( !TagAct.tagReadOnly )
			updateDB();
		
		super.onDestroy();
		}
	}
