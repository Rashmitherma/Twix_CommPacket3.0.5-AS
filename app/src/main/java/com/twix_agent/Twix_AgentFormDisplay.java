package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Twix_AgentFormDisplay extends DialogFragment
	{
	// CONSTANTS
	private static final int NONE = 0;
	private static final int SOLID = 1;
	private static final int DASHED = 2;
	
	private static final int PHOTO_INTENT = 100;
	
	// Class Dependent Variables
	private int FormId;
	private long FormDataId;
	private Context mContext;
	private Twix_Application app;
	private ActivityCallback callback;
	private boolean ReadOnly = false;
	private boolean PopupOpen = false;
	
	// Android View Variables
	private LinearLayout Main;
	private LinearLayout Header;
	private LinearLayout Content;
	private LinearLayout Footer;
	private BitmapDrawable dCheck;
	
	// Form Building Variables
	private Map<Long, Map<Long, List<String>>> DropOptions;
	private Map<Long, List<PickItem>> PickList;
	private Map<Long, String> AttrValues;
	
	// Submission Variables
	private List<Object> UserInputList;
	private String ParentTable;
	private int ParentId;
	private String LinkTable;
	private int LinkId;
	
	// Click Listeners
	private OnClickListener AutoCompleteClick;
	private OnFocusChangeListener AutoCompleteFocus;
	
	// Previous Values Map
	//	** Note: A NestedMatrixId of 0 means the cell's actual value, no nesting exists
	private Map<Long, Object> PrevValues;
	
	// Previous Signature Mapping
	private Map<Long, Object> PrevSignatureValues;
	
	// Photo Variables
	private Dialog photoDialog;
	private Uri imageUri = null;
	private ArrayList<FormPhoto> FormPhotos;
	private ImageView viewPhotosbn;
	
	// Signature Variables
	private Twix_Signable.Callback SigCallback;
	
	// Subclasses
	private class FormSectionDetails
		{
		long XRefId;
		
		String Title;
		int RowCnt;
		int ColCnt;
		
		String[] SetWidths;
		String[] SetHeights;
		
		FormCell[][] FormMatrix;
		}

	public class FormCell
		{
		long XRefId;
		
		long MatrixId;
		String MatrixTrail;
		long AttrId;
		long PickId;
		String Text;
		long ChildFormId;
		String InputType;
		boolean Modifiable;
		byte[] Image;
		String ImageType;
		
		
		int Row;
		int Col;
		
		int RowSpan;
		int ColSpan;
		int width;
		int height;
		
		int minSize = 50;
		
		String Description;
		boolean Static;
		
		CellFormatting cellFormat;
		
		// Signature Variable
		Bitmap signature;
		
		public FormCell()
			{
			cellFormat = new CellFormatting();
			signature = null;
			}
		
		public byte[] getDBSignature()
			{
			byte[] ret = null;
			if( signature != null )
				{
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				signature.compress(Bitmap.CompressFormat.JPEG, 100, os);
				ret = os.toByteArray();
				}
			return ret;
			}
		}
	
	private class CellFormatting
		{
		int FontSize;
		int FontColor;
		boolean bold;
		boolean italic;
		boolean underline;
		
		int align;
		int valign;
		
		int bgcolor;
		
		public BorderProperties bLeft;
		public BorderProperties bBottom;
		public BorderProperties bRight;
		public BorderProperties bTop;
		
		public CellFormatting()
			{
			FontSize = 14;
			FontColor = 0xFF000000;
			
			bold = false;
			italic = false;
			underline = false;
			
			align = Gravity.LEFT;
			valign = Gravity.TOP;
			
			bgcolor = 0xFFFFFFFF;
			
			//bLeft = new BorderProperties();
			//bBottom = new BorderProperties();
			//bRight = new BorderProperties();
			//bTop = new BorderProperties();
			}
		
		public void SetFont(int FontSize, String FontColor)
			{
			if( FontSize > 0 )
				this.FontSize = FontSize;
			
			if( FontColor != null )
				{
				try
					{
					this.FontColor = 0xFF000000 + Integer.parseInt(FontColor, 16);
					}
				catch( Exception e)
					{
					this.FontColor = 0xFF000000;
					}
				}
			}
		
		public void SetFontEffects(String bold, String italic, String underline)
			{
			this.bold = isTrue(bold);
			this.italic = isTrue(italic);
			this.underline = isTrue(underline);
			}
		
		private boolean isTrue(String s)
			{
			if( s != null )
				{
				if( s.contentEquals("Y") )
					return true;
				else
					return false;
				}
			else
				return false;
			}
		
		public void SetAlignment(String align)
			{
			if( align != null )
				{
				if( align.contentEquals("center") )
					this.align = Gravity.CENTER_HORIZONTAL;
				else if( align.contentEquals("right") )
					this.align = Gravity.RIGHT;
				else
					this.align = Gravity.LEFT;
				}
			else
				this.align = Gravity.LEFT;
			}
		
		public void SetVAlignment(String valign)
			{
			if( valign != null )
				{
				if( valign.contentEquals("middle") )
					this.valign = Gravity.CENTER_VERTICAL;
				else if( valign.contentEquals("bottom") )
					this.valign = Gravity.BOTTOM;
				else
					this.valign = Gravity.TOP;
				}
			else
				this.valign = Gravity.TOP;
			}
		
		public void SetBGColor(String bgcolor)
			{
			if( bgcolor != null )
				{
				try
					{
					this.bgcolor = 0xFF000000 + Integer.parseInt(bgcolor, 16);
					}
				catch( Exception e)
					{
					this.bgcolor = 0xFFFFFFFF;
					}
				}
			}
		
		public void SetBGColor(int bgcolor)
			{
			this.bgcolor = bgcolor;
			}
		
		public BorderDrawable GetBackground(boolean editable)
			{
			BorderDrawable ret = new BorderDrawable(new RectShape(), this, editable);
			return ret;
			}
		
		public int GetGravity()
			{
			return align | valign;
			}
		}
	
	private class BorderProperties
		{
		int style;
		int width;
		int color;
		
		public BorderProperties(String width, String style, String color)
			{
			Set(width, style, color);
			}
		
		public BorderProperties(int width, int style, int color)
			{
			this.width = width;
			this.style = style;
			this.color = color;
			}
		
		public void Set(String width, String style, String color)
			{
			if( style != null )
				{
				if( style.contentEquals("solid") )
					this.style = SOLID;
				else if( style.contentEquals("dashed") )
					this.style = DASHED;
				else
					this.style = NONE;
				}
			
			if( width != null )
				{
				width = width.replaceAll("px", "");
				try
					{
					this.width = Integer.parseInt(width, 16);
					}
				catch (Exception e)
					{
					this.width = 0;
					}
				}
			
			if( color != null )
				{
				color = color.replaceAll("#", "");
				try
					{
					this.color = 0xFF000000 + Integer.parseInt(color, 16);
					}
				catch (Exception e)
					{
					this.color = 0x00000000;
					}
				}
			
			}
		}
	
	private class PickItem
		{
		long PickItemId;
		String Text;
		}
	
	private class FormCheckBox extends ImageView
		{
		private Drawable dCheck;
		private boolean mChecked;
		private String Text;
		
		// Draw Variables
		private Rect oBounds = new Rect();
		private Rect bounds = new Rect();
		
		public FormCheckBox(Context c, Drawable d, String text)
			{
			super(c);
			dCheck = d;
			Text = text;
			int height = dCheck.getMinimumHeight();
			int padding = (int)(height*0.2f);
			setPadding(padding);
			setMinimumHeight(height+padding*2);
			// Make sure the performClick function gets called
			this.setClickable(true);
			this.setFocusable(true);
			}
		
		@Override
		public void setPadding(int left, int top, int right, int bottom) 
			{
			int highest = left;
			if( top > highest )
				highest = top;
			if( right > highest )
				highest = right;
			if( bottom > highest )
				highest = bottom;
			
			setPadding(highest);
			}
		
		public void setPadding(int pad)
			{
			super.setPadding(pad, pad, pad, pad);
			}
		
		public boolean isChecked()
			{
			return mChecked;
			}
		
		public void SetChecked(boolean checked)
			{
			mChecked = checked;
			//if( mChecked = checked )
				//this.setImageDrawable(dCheck);
			//else
			//	this.setImageDrawable(null);
			}
		
		private boolean toggle()
			{
			return (mChecked = !mChecked);
			}
		
		@Override
		protected int getSuggestedMinimumHeight()
			{
			return 40;
			}
		
		@Override
		protected int getSuggestedMinimumWidth()
			{
			return 40;
			}
		
		@Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
			{
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);
			}
		
		@Override
		protected void onDraw(Canvas canvas)
			{
			canvas.getClipBounds(oBounds);
			bounds.left = (int)(oBounds.right*0.15f);
			bounds.top = (int)(oBounds.bottom*0.15f);
			bounds.right = (int)(FloatMath.ceil(oBounds.right*0.85f));
			bounds.bottom = (int)(FloatMath.ceil(oBounds.bottom*0.85f));
			
			dCheck.setBounds(bounds);
			if( mChecked )
				dCheck.draw(canvas);
			super.onDraw(canvas);
			}
		
		public String getText()
			{
			return Text;
			}
		
		@Override
		public boolean performClick()
			{
			this.requestFocusFromTouch();
			if( isEnabled() )
				toggle();
			return super.performClick();
			}
		}
	
	private class FormRadioButton extends ImageView
		{
		private ShapeDrawable dCheck;
		private Paint checkPaint;
		private boolean mChecked;
		private List<Object> RadioGroup;
		private String Text;
		
		// Draw Variables
		private Rect oBounds = new Rect();
		private Rect bounds = new Rect();
		
		public FormRadioButton(Context c, List<Object> RadioGroup, String text)
			{
			super(c);
			this.RadioGroup = RadioGroup;
			Text = text;
			dCheck = new ShapeDrawable(new OvalShape());
			checkPaint = dCheck.getPaint();
			checkPaint.setARGB(180, 0, 0, 0);
			int height = dCheck.getMinimumHeight();
			int padding = (int)(height*0.2f);
			setPadding(padding);
			setMinimumHeight(height+padding*2);
			// Make sure the performClick function gets called
			this.setClickable(true);
			this.setFocusable(true);
			}
		
		@Override
		public void setPadding(int left, int top, int right, int bottom) 
			{
			int highest = left;
			if( top > highest )
				highest = top;
			if( right > highest )
				highest = right;
			if( bottom > highest )
				highest = bottom;
			
			setPadding(highest);
			}
		
		public void setPadding(int pad)
			{
			super.setPadding(pad, pad, pad, pad);
			}
		
		public boolean isChecked()
			{
			return mChecked;
			}
		
		public void setChecked(boolean checked)
			{
			if( mChecked = checked )
				{
				this.setImageDrawable(dCheck);
				
				FormRadioButton bn;
				int size = RadioGroup.size();
				for( int i = 0; i < size; i++ )
					{
					bn = (FormRadioButton) RadioGroup.get(i);
					if( bn != this )
						bn.setChecked(false);
					}
				}
			else
				this.setImageDrawable(null);
			}
		
		@Override
		protected int getSuggestedMinimumHeight()
			{
			return 40;
			}
		
		@Override
		protected int getSuggestedMinimumWidth()
			{
			return 40;
			}
		
		@Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
			{
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);
			}
		
		@Override
		protected void onDraw(Canvas canvas)
			{
			canvas.getClipBounds(oBounds);
			bounds.left = (int)(oBounds.right*0.25f);
			bounds.top = (int)(oBounds.bottom*0.25f);
			bounds.right = (int)(FloatMath.ceil(oBounds.right*0.75f));
			bounds.bottom = (int)(FloatMath.ceil(oBounds.bottom*0.75f));
			
			dCheck.setBounds(bounds);
			if( mChecked )
				dCheck.draw(canvas);
			super.onDraw(canvas);
			}
		
		public String getText()
			{
			return Text;
			}
		
		@Override
		public boolean performClick()
			{
			this.requestFocusFromTouch();
			if( isEnabled() )
				setChecked(true);
			return super.performClick();
			}
		}
	
	public BorderProperties CreateBorder(String border)
		{
		BorderProperties ret = null;
		if( border != null )
			{
			String[] words = border.split(" ");
			if( words.length == 3 )
				{
				ret = new BorderProperties(words[0], words[1], words[2]);
				//ret.Set( words[0], words[1], words[2]);
				}
			}
		
		return ret;
		}
	
	// Class Methods
//	public Twix_AgentFormDisplay(int FormId, Map<String, Integer> AttrFindIds, long FormDataId,
	public void Setup( int FormId, Map<String, String> AttrFindIds, long FormDataId,
			String ParentTable, int ParentId, String LinkTable, int LinkId, 
			Context context, Twix_Application app, ActivityCallback callback, boolean ReadOnly)
		{
		//super();
		this.setCancelable(false);
		this.FormId = FormId;
		this.FormDataId = FormDataId;
		
		this.ParentTable = ParentTable;
		this.ParentId = ParentId;
		this.LinkTable = LinkTable;
		this.LinkId = LinkId;
		
		this.mContext = context;
		this.app = app;
		this.callback = callback;
		
		this.ReadOnly = ReadOnly;
		
		this.FormPhotos = new ArrayList<FormPhoto>();
		
		buildClickListeners();
		dCheck = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.icon_check);
		
		PrepareAttrValues(this.FormId, AttrFindIds);
		DropOptions = PrepareDropOptions(this.FormId);
		PickList = PreparePickLists(this.FormId);
		
		UserInputList = new ArrayList<Object>();
		
		if( FormDataId != 0 )
			{
			FetchValues();
			FetchSignatureValues();
			}
		Main = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				1200, LayoutParams.WRAP_CONTENT);
		Main.setLayoutParams(params);
		Main.setMinimumWidth(1200);
		Main.setOrientation(LinearLayout.VERTICAL);
		Main.setBackgroundColor(0xFF000000);
		
		SigCallback = new Twix_Signable.Callback()
			{
			@Override
			public void Saved(Bitmap bmp, Object obj)
				{
				Button bn = (Button) obj;
				FormCell cell = (FormCell) bn.getTag();
				cell.signature = bmp;
				bn.setText("Review Signature");
				}

			@Override
			public void Delete(Object obj)
				{
				Button bn = (Button) obj;
				FormCell cell = (FormCell) bn.getTag();
				cell.signature = null;
				bn.setText("Click to Sign");
				}
			}
		;
		
		if( this.FormDataId != 0 )
			BuildPhotos();
		BuildForm();
		
		//Main.setFocusable(true);
		//Main.setFocusableInTouchMode(true);
		Main.requestFocus();
		Main.invalidate();
		Main.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
		this.setRetainInstance(true);
		
		}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
		{
		Window w = this.getDialog().getWindow();
		if( w != null )
			w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		
		return Main;
		}
	
	private void buildClickListeners()
		{
		AutoCompleteClick = new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				((AutoCompleteTextView)v).showDropDown();
				}
			}
		;
		
		AutoCompleteFocus = new OnFocusChangeListener()
			{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
				{
				if( hasFocus )
					((AutoCompleteTextView)v).showDropDown();
				}
			}
		;
		}
	
	@SuppressWarnings("unchecked")
	private void FetchValues()
		{
		PrevValues = new HashMap<Long, Object>();
		String sql = "SELECT XRefId, Value, MatrixTrail FROM FormDataValues WHERE FormDataId = " + FormDataId + " ORDER BY XRefId, MatrixTrail";
		Cursor cursor = app.db.rawQuery(sql);
		
		long XRefId;
		long PrevXRefId = 0;
		String Value;
		String MatrixTrail;
		String[] TrailList;
		boolean CreateEntries = false;
		HashMap<Long, Object> MatrixMap = null;
		HashMap<Long, Object> NextMap = null;
		long MatrixId = 0;
		while( cursor.moveToNext() )
			{
			XRefId = cursor.getLong(0);
			Value = cursor.getString(1);
			MatrixTrail = cursor.getString(2);
			
			if( PrevXRefId != XRefId )
				{
				PrevXRefId = XRefId;
				MatrixMap = new HashMap<Long, Object>();
				PrevValues.put(XRefId, MatrixMap);
				}
			else
				MatrixMap = (HashMap<Long, Object>) PrevValues.get(XRefId);
			
			TrailList = MatrixTrail.split(",");
			CreateEntries = false;
			for( int i = 0; i < TrailList.length-1; i++ )
				{
				MatrixId = Long.parseLong(TrailList[i]);
				NextMap = (HashMap<Long, Object>) MatrixMap.get( MatrixId );
				if( !CreateEntries )
					{
					if( NextMap != null )
						{
						MatrixMap = NextMap;
						}
					else
						{
						CreateEntries = true;
						NextMap = new HashMap<Long, Object>();
						MatrixMap.put(MatrixId, NextMap);
						MatrixMap = NextMap;
						}
					}
				else
					{
					NextMap = new HashMap<Long, Object>();
					MatrixMap.put(MatrixId, NextMap);
					MatrixMap = NextMap;
					}
				}
			
			MatrixMap.put(Long.parseLong(TrailList[TrailList.length-1]), Value);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	@SuppressWarnings("unchecked")
	private void FetchSignatureValues()
		{
		PrevSignatureValues = new HashMap<Long, Object>();
		String sql = "SELECT XRefId, Value, MatrixTrail FROM FormDataSignatures WHERE FormDataId = " + FormDataId + " ORDER BY XRefId, MatrixTrail";
		Cursor cursor = app.db.rawQuery(sql);
		
		long XRefId;
		long PrevXRefId = 0;
		byte[] Value;
		String MatrixTrail;
		String[] TrailList;
		boolean CreateEntries = false;
		HashMap<Long, Object> MatrixMap = null;
		HashMap<Long, Object> NextMap = null;
		long MatrixId = 0;
		while( cursor.moveToNext() )
			{
			XRefId = cursor.getLong(0);
			Value = cursor.getBlob(1);
			MatrixTrail = cursor.getString(2);
			
			if( PrevXRefId != XRefId )
				{
				PrevXRefId = XRefId;
				MatrixMap = new HashMap<Long, Object>();
				PrevSignatureValues.put(XRefId, MatrixMap);
				}
			else
				MatrixMap = (HashMap<Long, Object>) PrevSignatureValues.get(XRefId);
			
			TrailList = MatrixTrail.split(",");
			CreateEntries = false;
			for( int i = 0; i < TrailList.length-1; i++ )
				{
				MatrixId = Long.parseLong(TrailList[i]);
				NextMap = (HashMap<Long, Object>) MatrixMap.get( MatrixId );
				if( !CreateEntries )
					{
					if( NextMap != null )
						{
						MatrixMap = NextMap;
						}
					else
						{
						CreateEntries = true;
						NextMap = new HashMap<Long, Object>();
						MatrixMap.put(MatrixId, NextMap);
						MatrixMap = NextMap;
						}
					}
				else
					{
					NextMap = new HashMap<Long, Object>();
					MatrixMap.put(MatrixId, NextMap);
					MatrixMap = NextMap;
					}
				}
			
			MatrixMap.put(Long.parseLong(TrailList[TrailList.length-1]), Value);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	private StateListDrawable BuildCheckBox()
		{
		// Checked and Unchecked share the same background
		CellFormatting fmt = new CellFormatting();
		fmt.SetBGColor(0xFFDDDDDD);
		fmt.bLeft = CreateBorder("2px solid #AAAAAA");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable unpressed = new BorderDrawable(new RectShape(), fmt, false);
		
		// Pressed just changes the background
		fmt = new CellFormatting();
		fmt.SetBGColor(0xFFEEEEEE);
		fmt.bLeft = CreateBorder("2px solid #CCCCCC");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable pressed = new BorderDrawable(new RectShape(), fmt, false);
		
		// Disabled is slightly darker
		fmt = new CellFormatting();
		fmt.SetBGColor(0xFFCCCCCC);
		fmt.bLeft = CreateBorder("2px solid #888888");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable disabled = new BorderDrawable(new RectShape(), fmt, false);
		
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_enabled,	android.R.attr.state_pressed }, pressed);
		states.addState(new int[] { android.R.attr.state_enabled,	-android.R.attr.state_pressed }, unpressed);
		states.addState(new int[] { -android.R.attr.state_enabled }, disabled);
		
		return states;
		}
	
	private StateListDrawable BuildRadioButton()
		{
		// Checked and Unchecked share the same background
		BorderProperties border = new BorderProperties(2, SOLID, 0xFFAAAAAA);
		RoundedBorderDrawable unpressed = new RoundedBorderDrawable(new OvalShape(), 0xFFDDDDDD, border, false);
		
		// Pressed just changes the background
		border = new BorderProperties(2, SOLID, 0xFFCCCCCC);
		RoundedBorderDrawable pressed = new RoundedBorderDrawable(new OvalShape(), 0xFFEEEEEE, border, false);
		
		// Disabled is slightly darker
		border = new BorderProperties(2, SOLID, 0xFF888888);
		RoundedBorderDrawable disabled = new RoundedBorderDrawable(new OvalShape(), 0xFFCCCCCC, border, false);
		
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_enabled,	android.R.attr.state_pressed }, pressed);
		states.addState(new int[] { android.R.attr.state_enabled,	-android.R.attr.state_pressed }, unpressed);
		states.addState(new int[] { -android.R.attr.state_enabled }, disabled);
		
		return states;
		}
	
	private StateListDrawable BuildButton()
		{
		// Checked and Unchecked share the same background
		CellFormatting fmt = new CellFormatting();
		fmt.SetBGColor(0xFFDDDDDD);
		fmt.bLeft = CreateBorder("3px solid #AAAAAA");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable unpressed = new BorderDrawable(new RectShape(), fmt, false);
		
		// Pressed just changes the background
		fmt = new CellFormatting();
		fmt.SetBGColor(0xFFEEEEEE);
		fmt.bLeft = CreateBorder("3px solid #CCCCCC");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable pressed = new BorderDrawable(new RectShape(), fmt, false);
		
		// Disabled is slightly darker
		fmt = new CellFormatting();
		fmt.SetBGColor(0xFFCCCCCC);
		fmt.bLeft = CreateBorder("3px solid #888888");
		fmt.bTop = fmt.bLeft;
		fmt.bBottom = fmt.bLeft;
		fmt.bRight = fmt.bLeft;
		BorderDrawable disabled = new BorderDrawable(new RectShape(), fmt, false);
		
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[] { android.R.attr.state_enabled,	android.R.attr.state_pressed }, pressed);
		states.addState(new int[] { android.R.attr.state_enabled,	-android.R.attr.state_pressed }, unpressed);
		states.addState(new int[] { -android.R.attr.state_enabled }, disabled);
		
		//dCheckBox = states;
		return states;
		}
	
	private View GenerateBreak(int height)
		{
		LinearLayout v = new LinearLayout(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, height);
		v.setLayoutParams(params);
		v.setBackgroundColor(0x000000);
		return v;
		}
	
	@SuppressWarnings("unchecked")
	private void BuildForm()
		{
		String sqlQ = "SELECT XRefId, FormSecId, SectionType " +
				"FROM FormSecXRef " +
					"WHERE FormId = " + FormId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		long XRefId;
		long FormSecId;
		String SectionType;
		FormSectionDetails det;
		LinearLayout Section;
		HashMap<Long, Object> MatrixMap = null;
		HashMap<Long, Object> SigMap = null;
		
		int index;
		while (cursor.moveToNext())
			{
			index = 0;
			XRefId = cursor.getLong(0);
			FormSecId = cursor.getLong(1);
			SectionType = cursor.getString(2);
			
			if( SectionType != null )
				{
				if( SectionType.contentEquals("H") )
					{
					if( Header == null )
						{
						Header = new LinearLayout(mContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						params.setMargins(3, 3, 3, 3);
						Header.setLayoutParams(params);
						Header.setOrientation(LinearLayout.VERTICAL);
						Header.setBackgroundColor(0xFFFFFFFF);
						}
					
					det = ReadFormSectionDetails(FormSecId);
					det.XRefId = XRefId;
					ReadFormSection(FormSecId, det, "");
					Section = CreateSection();
					if( PrevValues != null )
						MatrixMap = (HashMap<Long, Object>)PrevValues.get(XRefId);
					if( PrevSignatureValues != null )
						SigMap = (HashMap<Long, Object>)PrevSignatureValues.get(XRefId);
					Section.addView(PlaceCells(det, 1200, MatrixMap, SigMap, DropOptions.get(FormSecId)));
					Header.addView(Section);
					}
				else if( SectionType.contentEquals("F") )
					{
					if( Footer == null )
						{
						Footer = new LinearLayout(mContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						params.setMargins(3, 3, 3, 3);
						Footer.setLayoutParams(params);
						Footer.setOrientation(LinearLayout.VERTICAL);
						Footer.setBackgroundColor(0xFFFFFFFF);
						}
					
					det = ReadFormSectionDetails(FormSecId);
					det.XRefId = XRefId;
					ReadFormSection(FormSecId, det, "");
					Section = CreateSection();
					if( PrevValues != null )
						MatrixMap = (HashMap<Long, Object>)PrevValues.get(XRefId);
					if( PrevSignatureValues != null )
						SigMap = (HashMap<Long, Object>)PrevSignatureValues.get(XRefId);
					Section.addView(PlaceCells(det, 1200, MatrixMap, SigMap, DropOptions.get(FormSecId)));
					Footer.addView(Section);
					}
				else
					{
					if( Content == null )
						{
						Content = new LinearLayout(mContext);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						params.setMargins(3, 3, 3, 3);
						Content.setLayoutParams(params);
						Content.setOrientation(LinearLayout.VERTICAL);
						Content.setBackgroundColor(0xFFFFFFFF);
						}
					
					det = ReadFormSectionDetails(FormSecId);
					det.XRefId = XRefId;
					ReadFormSection(FormSecId, det, "");
					Section = CreateSection();
					if( PrevValues != null )
						MatrixMap = (HashMap<Long, Object>)PrevValues.get(XRefId);
					if( PrevSignatureValues != null )
						SigMap = (HashMap<Long, Object>)PrevSignatureValues.get(XRefId);
					Section.addView(PlaceCells(det, 1200, MatrixMap, SigMap, DropOptions.get(FormSecId)));
					Content.addView(Section);
					}
					
				}
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// Construct the Scroller
		LinearLayout.LayoutParams secParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		secParams.weight = 1;
		Section = new LinearLayout(mContext);
		Section.setLayoutParams(secParams);
		Section.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams svParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		svParams.weight = 1;
		
		NonFocusingScrollView sv = new NonFocusingScrollView(mContext);
		svParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		svParams.gravity = Gravity.TOP;
		sv.setLayoutParams(svParams);
		sv.addView(Section);
		sv.setFillViewport(true);
		Main.addView(sv);
		
		// Add the Parts
		if( Header != null )
			{
			Section.addView(Header);
			Section.addView(GenerateBreak(3));
			}
		if( Content != null )
			{
			Section.addView(Content);
			Section.addView(GenerateBreak(3));
			}
		if( Footer != null )
			{
			Section.addView(Footer);
			}
		
		
		LinearLayout host = new LinearLayout(mContext);
		host.setOrientation(LinearLayout.HORIZONTAL);
		host.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		if( !ReadOnly )
			{
			LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			saveParams.setMargins(15, 0, 0, 0);
			//saveParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			Button save = new Button(mContext);
			save.setId(2);
			save.setLayoutParams(saveParams);
			save.setPadding(8, 4, 8, 4);
			save.setText("Save");
			save.setTextColor(app.Twix_Theme.sub1Value);
			save.setTextSize(app.Twix_Theme.subSize);
			save.setBackgroundDrawable(BuildButton());
			save.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					SubmitData();
					}
				});
			host.addView(save);
			
			if( FormDataId != 0 )
				{
				Button delete = new Button(mContext);
				LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				//deleteParams.addRule(RelativeLayout.RIGHT_OF, 2);
				deleteParams.leftMargin = 15;
				delete.setLayoutParams(deleteParams);
				delete.setPadding(8, 4, 8, 4);
				delete.setText("Delete");
				delete.setTextColor(app.Twix_Theme.sub1Value);
				delete.setTextSize(app.Twix_Theme.subSize);
				delete.setBackgroundDrawable(BuildButton());
				delete.setOnClickListener(new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						ConfirmDeleteData(false);
						}
					});
				host.addView(delete);
				}
			else
				{
				Button cancel = new Button(mContext);
				LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				//deleteParams.addRule(RelativeLayout.RIGHT_OF, 2);
				deleteParams.leftMargin = 15;
				cancel.setLayoutParams(deleteParams);
				cancel.setPadding(8, 4, 8, 4);
				cancel.setText("Cancel");
				cancel.setTextColor(app.Twix_Theme.sub1Value);
				cancel.setTextSize(app.Twix_Theme.subSize);
				cancel.setBackgroundDrawable(BuildButton());
				cancel.setOnClickListener(new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						ConfirmDeleteData(true);
						}
					});
				host.addView(cancel);
				}
			}
		else
			{
			Button Close = new Button(mContext);
			RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			closeParams.addRule(RelativeLayout.RIGHT_OF, 2);
			closeParams.leftMargin = 15;
			Close.setLayoutParams(closeParams);
			Close.setPadding(8, 4, 8, 4);
			Close.setText("Close");
			Close.setTextColor(app.Twix_Theme.sub1Value);
			Close.setTextSize(app.Twix_Theme.subSize);
			Close.setBackgroundDrawable(BuildButton());
			Close.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					callback.Refresh();
					Twix_AgentFormDisplay.this.dismiss();
					}
				});
			host.addView(Close);
			}
		
		
		
		// Spacer because relative layouts suck
		LinearLayout spacer = new LinearLayout(mContext);
		LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		spacerParams.weight = 1f;
		spacer.setLayoutParams(spacerParams);
		host.addView(spacer);
		
		if( !ReadOnly )
			{
			// Take Photo Button
			ImageButton takePhoto = new ImageButton(mContext);
			LinearLayout.LayoutParams takePhotoParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, 30);
			takePhotoParams.rightMargin = 15;
			//takePhotoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			takePhoto.setLayoutParams(takePhotoParams);
			takePhoto.setId(1);
			takePhoto.setImageResource(R.drawable.icon_camera);
			takePhoto.setBackgroundResource(R.drawable.button_bg);
			takePhoto.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					takePhoto();
					}
				});
			host.addView(takePhoto);
			}
		
		// Show Photo Button
		viewPhotosbn = new ImageButton(mContext);
		LinearLayout.LayoutParams viewPhotosParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, 30);
		viewPhotosParams.rightMargin = 15;
		//viewPhotosParams.addRule(RelativeLayout.LEFT_OF, 1);
		viewPhotosbn.setLayoutParams(viewPhotosParams);
		viewPhotosbn.setImageResource(R.drawable.icon_picture);
		viewPhotosbn.setBackgroundResource(R.drawable.button_bg);
		viewPhotosbn.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				ShowPhotos();
				}
			});
		if( FormPhotos.size() <= 0 )
			viewPhotosbn.setVisibility(View.GONE);
		host.addView(viewPhotosbn);
		
		
		
		Section.addView(host);
		}
	
	private FormSectionDetails ReadFormSectionDetails(long FormSecId)
		{
		FormSectionDetails ret = null;
		String sqlQ = "SELECT Title, RowCnt, ColCnt FROM FormSection WHERE FormSecId = " + FormSecId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		int index;
		if (cursor.moveToNext())
			{
			index = 0;
			ret = new FormSectionDetails();
			ret.Title = cursor.getString(0);
			ret.RowCnt = cursor.getInt(1);
			ret.ColCnt = cursor.getInt(2);
			ret.FormMatrix = new FormCell[ret.RowCnt][ret.ColCnt];
			
			ret.SetWidths = new String[ret.ColCnt];
			ret.SetHeights = new String[ret.RowCnt];
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// Fetch the Widths
		sqlQ = "SELECT Col, Width FROM FormSecWidths WHERE FormSecId = " + FormSecId;
		cursor = app.db.rawQuery(sqlQ);
		while(cursor.moveToNext())
			ret.SetWidths[cursor.getInt(0)] = cursor.getString(1);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// Fetch the Heights
		sqlQ = "SELECT Row, Height FROM FormSecHeights WHERE FormSecId = " + FormSecId;
		cursor = app.db.rawQuery(sqlQ);
		while(cursor.moveToNext())
			ret.SetHeights[cursor.getInt(0)] = cursor.getString(1);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return ret;
		}
	
	// Prepare the Attribute Values - Recursion as well
	private void PrepareAttrValues(int FormId, Map<String, String> AttrFindIds)
		{
		AttrValues = new HashMap<Long, String>();
		
		String sqlQ = "SELECT DISTINCT FormSecId FROM FormSecXRef WHERE FormId = " + FormId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		Map<String, Long> values = null;
		Map<String, Map<String, Long>> Tables = new HashMap<String, Map<String, Long>>();
		while (cursor.moveToNext())
			PrepareAttrValues(cursor.getLong(0), Tables);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		Entry<String, Map<String, Long>> Table;
		Entry<String, Long> itCols;
		for( Iterator<Entry<String, Map<String, Long>>>  it = Tables.entrySet().iterator(); it.hasNext(); )
			{
			Table = it.next();
			
			if( AttrFindIds.containsKey(Table.getKey()))
				{
				//values = Tables.get("equipment");
				values = Tables.get(Table.getKey());
				sqlQ = "SELECT "; 
				if( Table.getKey().contentEquals("users") )
					{
					sqlQ += "mechanic_name as [last_name || ', ' || u.first_name]";
					sqlQ += " FROM mechanic WHERE mechanic = '" + AttrFindIds.get("users") + "'";
					}
				else
					{
					for( Iterator<Entry<String, Long>>  colIt = Table.getValue().entrySet().iterator(); colIt.hasNext(); )
						{
						itCols = colIt.next();
						if( itCols.getKey().contentEquals("jobNo") )
							{
							sqlQ += "REPLACE(jobNo, 'TTCA', '') as jobNo";
							}
						else
							sqlQ += itCols.getKey();
						if( colIt.hasNext() )
							sqlQ += ", ";
						}
					
					sqlQ += " FROM " + Table.getKey() + " WHERE " + Table.getKey() + "Id = " + AttrFindIds.get(Table.getKey());
					}
				
				//sqlQ += " FROM " + Table.getKey() + " WHERE " + Table.getKey() + "Id = " + AttrFindIds.get(Table.getKey());
				cursor = app.db.rawQuery(sqlQ);
				String[] columns = cursor.getColumnNames();
				String val;
				long attrId;
				if( cursor.moveToNext() )
					{
					for( int i = 0; i < columns.length; i++ )
						{
						val = cursor.getString(i);
						attrId = values.get(columns[i]);
						AttrValues.put(attrId, val);
						}
					}
				if (cursor != null && !cursor.isClosed())
					cursor.close();
				}
			}
		}
	
	private void PrepareAttrValues(long FormSecId, Map<String, Map<String, Long>> Tables)
		{
		String sqlQ = "SELECT AttrId, HostTable, HostColumn FROM AttrDef WHERE AttrId IN " +
				"( SELECT DISTINCT AttrId FROM FormMatrixXRef WHERE FormSecId = " + FormSecId + " AND AttrId > 0) " +
					"AND AttrId IS NOT NULL " +
				"ORDER BY HostTable asc, HostColumn asc";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		String HostTable = null;
		String PrevTable = null;
		Map<String, Long> values = null;
		while (cursor.moveToNext())
			{
			HostTable = cursor.getString(1);
			if( values == null || !PrevTable.contentEquals(HostTable) )
				{
				PrevTable = HostTable;
				values = new HashMap<String, Long>();
				Tables.put(HostTable, values);
				}
			values.put( cursor.getString(2).replaceAll("sa.", "").replaceAll("[+]", "||"), cursor.getLong(0) );
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		if( values != null )
			Tables.put(HostTable, values);
		
		// Recursively Check the Form Section for Pick Items
		sqlQ = "SELECT ChildFormId FROM FormMatrixXRef WHERE FormSecId = " + FormSecId + " AND ChildFormId > 0";
		cursor = app.db.rawQuery(sqlQ);
		while (cursor.moveToNext())
			PrepareAttrValues(cursor.getLong(0), Tables);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	
	// Prepare the Drop Options - Recursion as well
	private HashMap<Long, Map<Long, List<String>>> PrepareDropOptions(int FormId)
		{
		HashMap<Long, Map<Long, List<String>>> DropOptions = new HashMap<Long, Map<Long, List<String>>>();
		
		String sqlQ = "SELECT FormSecId FROM FormSecXRef WHERE FormId = " + FormId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		while (cursor.moveToNext())
			PrepareDropOptions(cursor.getLong(0), DropOptions);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return DropOptions;
		}
	
	private void PrepareDropOptions(long FormSecId, HashMap<Long, Map<Long, List<String>>> DropOptions)
		{
		if( !DropOptions.containsKey(FormSecId) )
			{
			HashMap<Long, List<String>> SecMap = new HashMap<Long, List<String>>();
			DropOptions.put(FormSecId, SecMap);
			
			String sqlQ = "SELECT MatrixId, Value FROM FormOptions WHERE FormSecId = " + FormSecId + " " +
					"ORDER BY MatrixId asc";
			Cursor cursor = app.db.rawQuery(sqlQ);
			
			long MatrixId = 0;
			long prevMatrixId = 0;
			List<String> values = null;
			while (cursor.moveToNext())
				{
				MatrixId = cursor.getLong(0);
				if( MatrixId != prevMatrixId )
					{
					prevMatrixId = MatrixId;
					values = new ArrayList<String>();
					SecMap.put(MatrixId, values);
					}
				values.add(cursor.getString(1));
				}
			if (cursor != null && !cursor.isClosed())
				cursor.close();
			
			// Recursively Check the Form Section for Pick Items
			sqlQ = "SELECT ChildFormId FROM FormMatrixXRef WHERE FormSecId = " + FormSecId + " AND ChildFormId > 0";
			cursor = app.db.rawQuery(sqlQ);
			while (cursor.moveToNext())
				PrepareDropOptions(cursor.getLong(0), DropOptions);
			if (cursor != null && !cursor.isClosed())
				cursor.close();
			}
		}
	
	// Prepare the Pick Lists - Recursion as well
	private HashMap<Long, List<PickItem>> PreparePickLists(int FormId)
		{
		HashMap<Long, List<PickItem>> PickList = new HashMap<Long, List<PickItem>>();
		
		String sqlQ = "SELECT FormSecId FROM FormSecXRef WHERE FormId = " + FormId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		while (cursor.moveToNext())
			PreparePickLists(cursor.getLong(0), PickList);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return PickList;
		}
	
	private void PreparePickLists(long FormSecId, HashMap<Long, List<PickItem>> PickList)
		{
		String sqlQ = "SELECT PickId, PickItemId, itemValue FROM PickListItem WHERE PickId IN " +
				"( SELECT DISTINCT PickId FROM FormMatrixXRef WHERE FormSecId = " + FormSecId + " AND PickId > 0 ) " +
				"ORDER BY PickId asc, PickItemId asc";
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		long PickId = 0;
		long PrevPickId = 0;
		List<PickItem> values = null;
		PickItem item;
		while (cursor.moveToNext())
			{
			PickId = cursor.getLong(0);
			if( PrevPickId != PickId )
				{
				PrevPickId = PickId;
				values = PickList.get(PickId);
				if( values == null )
					{
					values = new ArrayList<PickItem>();
					PickList.put(PickId, values);
					}
				else
					values = null;
				}
			if( values != null )
				{
				item = new PickItem();
				item.PickItemId = cursor.getLong(1);
				item.Text = cursor.getString(2);
				values.add(item);
				}
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		// Recursively Check the Form Section for Pick Items
		sqlQ = "SELECT ChildFormId FROM FormMatrixXRef WHERE FormSecId = " + FormSecId + " AND ChildFormId > 0";
		cursor = app.db.rawQuery(sqlQ);
		while (cursor.moveToNext())
			PreparePickLists(cursor.getLong(0), PickList);
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	
	private void ReadFormSection(long FormSecId, FormSectionDetails det, String MatrixTrail)
		{
		String sqlQ = "SELECT m.MatrixId, mx.AttrId, mx.PickId, mx.Text, mx.ChildFormId, m.InputType, mx.Modifiable, " +
					"m.Image, m.ImageType, " +
					"mx.Row, mx.Col, mx.RowSpan, mx.ColSpan, " +
					"mx.fontSize, mx.fontColor, mx.bold, mx.italic, mx.underline, mx.align, mx.valign, " + 
					"mx.bgcolor, mx.borderTop, mx.borderBottom, mx.borderLeft, mx.borderRight, " +
					"mw.Width, mh.Height " +
					
					"FROM FormMatrixXRef as mx " +
						"LEFT OUTER JOIN FormMatrix as m " +
							"ON m.MatrixId = mx.MatrixId " +
						"LEFT OUTER JOIN FormSecWidths as mw " +
							"ON mw.FormSecId = mx.FormSecId AND mw.Col = mx.Col " +
						"LEFT OUTER JOIN FormSecHeights as mh " +
							"ON mh.FormSecId = mx.FormSecId AND mh.Row = mx.Row " +
						"WHERE mx.FormSecId = " + FormSecId;
		Cursor cursor = app.db.rawQuery(sqlQ);
		
		FormCell[][] FormMatrix = det.FormMatrix;
		FormCell cell;
		int index;
		String s;
		
		while (cursor.moveToNext())
			{
			index = 0;
			cell = new FormCell();
			cell.XRefId = det.XRefId;
			cell.MatrixId = cursor.getLong(0);
			cell.AttrId = cursor.getLong(1);
			cell.PickId = cursor.getLong(2);
			cell.Text = cursor.getString(3);
			cell.ChildFormId = cursor.getLong(4);
			cell.InputType = cursor.getString(5);
			s = cursor.getString(6);
			cell.Modifiable = (s != null) && ( s.contentEquals("Y"));
			
			// Cell Image
			cell.Image = cursor.getBlob(7);
			cell.ImageType = cursor.getString(8);
			
			cell.Row = cursor.getInt(9);
			cell.Col = cursor.getInt(10);
			
			cell.RowSpan = cursor.getInt(11);
			cell.ColSpan = cursor.getInt(12);
			
			cell.cellFormat.SetFont(cursor.getInt(13), cursor.getString(14));
			cell.cellFormat.SetFontEffects(cursor.getString(15), cursor.getString(16), cursor.getString(17));
			cell.cellFormat.SetAlignment(cursor.getString(18));
			cell.cellFormat.SetVAlignment(cursor.getString(19));
			cell.cellFormat.SetBGColor(cursor.getString(20));
			cell.cellFormat.bTop = CreateBorder(cursor.getString(21));
			cell.cellFormat.bBottom = CreateBorder(cursor.getString(22));
			cell.cellFormat.bLeft = CreateBorder(cursor.getString(23));
			cell.cellFormat.bRight = CreateBorder(cursor.getString(24));
			
			cell.width = cursor.getInt(25);
			cell.height = cursor.getInt(26);
			
			cell.MatrixTrail = MatrixTrail;
			if( cell.MatrixTrail.length() > 0 )
				cell.MatrixTrail += ",";
			cell.MatrixTrail += cell.MatrixId + "";
			
			FormMatrix[cell.Row][cell.Col] = cell;
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		}
	
	private FormLayout PlaceCells(FormSectionDetails details, int MaxWidth, 
			HashMap<Long, Object> MatrixMap, HashMap<Long, Object> SigMap,
			Map<Long, List<String>> OptionsMap)
		{
		FormLayout fl = new FormLayout(mContext, MaxWidth, details.SetWidths, details.SetHeights);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		fl.setLayoutParams(params);
		fl.setBackgroundColor(0xFFFF3333);
		
		FormCell cell;
		for( int y = 0; y < details.RowCnt; y++ )
			{
			for( int x = 0; x < details.ColCnt; x++ )
				{
				cell = details.FormMatrix[y][x];
				if( cell != null )
					{
					fl.addView(BuildCell(cell, 1f, MatrixMap, SigMap, OptionsMap), x, y, cell.RowSpan, cell.ColSpan);
					}
				}
			}
		
		
		return fl;
		}
	
	private LinearLayout CreateOption(String type, String text, FormCell cell, CellFormatting format,
			List<Object> objList, boolean checked)
		{
		LinearLayout container = new LinearLayout(mContext);
		PredicateLayout.LayoutParams pParams = new PredicateLayout.LayoutParams(2, 2);
		container.setLayoutParams(pParams);
		container.setOrientation(LinearLayout.HORIZONTAL);
		
		int chkSize = (int)(format.FontSize*1.5f);
		LinearLayout.LayoutParams eleParams = 
				new LinearLayout.LayoutParams(chkSize, chkSize);
		
		LinearLayout.LayoutParams txtParams = 
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		txtParams.setMargins(3, 0, 3, 0);
		
		if( type.contentEquals("R") )
			{
			FormRadioButton bn = new FormRadioButton(mContext, objList, text);
			bn.setEnabled(!ReadOnly);
			bn.setLayoutParams(eleParams);
			bn.setBackgroundDrawable(BuildRadioButton());
			bn.setTag(cell);
			
			if( FormDataId != 0 )
				bn.setChecked( checked );
			
			objList.add(bn);
			container.addView(bn);
			}
		else if( type.contentEquals("C") )
			{
			FormCheckBox cb = new FormCheckBox(mContext, dCheck, text);
			cb.setEnabled(!ReadOnly);
			cb.setLayoutParams(eleParams);
			cb.setBackgroundDrawable(BuildCheckBox());
			cb.setTag(cell);
			
			if( FormDataId != 0 )
				cb.SetChecked( checked );
			
			objList.add(cb);
			container.addView(cb);
			}
		
		TextView tv = new TextView(mContext);
		tv.setPadding(5,5,5,5);
		tv.setLayoutParams(txtParams);
		tv.setText(text);
		tv.setTextSize(format.FontSize);
		tv.setTextColor(format.FontColor);
		tv.setBackgroundColor(format.bgcolor);
		//tv.setGravity(format.GetGravity());
		if( format.bold && format.italic )
			tv.setTypeface(null, Typeface.BOLD_ITALIC);
		else if( format.bold )
			tv.setTypeface(null, Typeface.BOLD);
		else if( format.italic )
			tv.setTypeface(null, Typeface.ITALIC);
		
		if( format.underline )
			tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		
		
		container.addView(tv);
		return container;
		}
	
	@SuppressWarnings("unchecked")
	private View BuildCell(FormCell cell, float weight, HashMap<Long, Object> MatrixMap,
			HashMap<Long, Object> SigMap, Map<Long, List<String>> OptionsMap)
		{
		View v = null;
		
		if( cell != null && cell.cellFormat != null )
			{
			CellFormatting format = cell.cellFormat;
			String inputType = Twix_TextFunctions.clean(cell.InputType);
			if( inputType.contentEquals("T") )
				{
				TextView tv;
				if( cell.Modifiable )
					{
					tv = new EditText(mContext);
					tv.setEnabled(!ReadOnly);
					BorderDrawable d = new BorderDrawable(new RectShape(), format, true);
					tv.setBackgroundDrawable(d);
					tv.setTag(cell);
					UserInputList.add(tv);
					}
				else
					{
					tv = new TextView(mContext);
					//tv.setBackgroundColor(format.bgcolor);
					BorderDrawable d = new BorderDrawable(new RectShape(), format, false);
					tv.setBackgroundDrawable(d);
					if( cell.AttrId > 0 )
						{
						tv.setTag(cell);
						UserInputList.add(tv);
						}
					}
				
				if( MatrixMap != null && FormDataId != 0 && (cell.Modifiable || cell.AttrId > 0))
					tv.setText( (String)MatrixMap.get(cell.MatrixId) );
				else if( cell.AttrId > 0 )
					tv.setText(AttrValues.get(cell.AttrId));
				else
					tv.setText(cell.Text);
				
				tv.setTextSize(format.FontSize);
				tv.setTextColor(format.FontColor);
				tv.setGravity(format.GetGravity());
				tv.setSingleLine(false);
				tv.setHorizontallyScrolling(false);
				if( format.bold && format.italic )
					tv.setTypeface(null, Typeface.BOLD_ITALIC);
				else if( format.bold )
					tv.setTypeface(null, Typeface.BOLD);
				else if( format.italic )
					tv.setTypeface(null, Typeface.ITALIC);
				
				if( format.underline )
					tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				
				v = tv;
				}
			else if( inputType.contentEquals("C") )
				{
				String curItem;
				int size;
				boolean checked;
				PredicateLayout pLay = new PredicateLayout(mContext);
				pLay.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				pLay.setBackgroundDrawable(format.GetBackground(false));
				
				String[] checkedItems = null;
				List<Object> CheckItems = new ArrayList<Object>();
				List<String> items = OptionsMap.get(cell.MatrixId);
				
				if( FormDataId != 0 )
					{
					String val = null;
					if( MatrixMap != null )
						val = (String)MatrixMap.get(cell.MatrixId);
					if( val != null )
						checkedItems = SplitAndDecode(val, '|');
					}
				
				if( items != null )
					{
					size = items.size();
					for( int i = 0; i < size; i++ )
						{
						curItem = Twix_TextFunctions.clean(items.get(i));
						checked = false;
						if( checkedItems != null )
							checked = inArray( curItem, checkedItems );
						
						pLay.addView( CreateOption(inputType, curItem, cell, format,
								CheckItems, checked) );
						}
					}
				
				if( cell.PickId > 0 )
					{
					PickItem pItem;
					List<PickItem> pickItems = PickList.get(cell.PickId);
					size = pickItems.size();
					for(int i = 0; i < size; i++ )
						{
						pItem = pickItems.get(i);
						curItem = Twix_TextFunctions.clean(pItem.Text);
						
						checked = false;
						if( checkedItems != null )
							checked = inArray(curItem, checkedItems );
						
						pLay.addView( CreateOption(inputType, curItem, cell, format,
								CheckItems, checked) );
						}
					}
				
				UserInputList.add(CheckItems);
				v = pLay;
				}
			else if( inputType.contentEquals("D") && cell.MatrixId != 0 )
				{
				v = BuildDropDown(cell, cell.cellFormat, MatrixMap, OptionsMap);
				}
			else if( inputType.contentEquals("R") )
				{
				List<Object> radioList = new ArrayList<Object>();
				String curItem;
				String val = null;
				int size;
				boolean checked = false;
				
				PredicateLayout pLay = new PredicateLayout(mContext);
				//pLay.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				pLay.setBackgroundDrawable(format.GetBackground(false));
				
				if( MatrixMap != null && FormDataId != 0 )
					{
					val = (String)MatrixMap.get(cell.MatrixId);
					}
				
				List<String> items = OptionsMap.get(cell.MatrixId);
				if( items != null )
					{
					size = items.size();
					for( int i = 0; i < size; i++ )
						{
						curItem = Twix_TextFunctions.clean(items.get(i));
						
						if( val != null )
							checked = val.contentEquals(curItem);
						
						pLay.addView( CreateOption(inputType, items.get(i), cell, format, radioList, checked) );
						}
					}
				
				if( cell.PickId > 0 )
					{
					PickItem pItem;
					List<PickItem> pickItems = PickList.get(cell.PickId);
					size = pickItems.size();
					for(int i = 0; i < size; i++ )
						{
						pItem = pickItems.get(i);
						curItem = Twix_TextFunctions.clean(pItem.Text);
						
						if( val != null )
							checked = val.contentEquals(curItem);
						
						pLay.addView( CreateOption(inputType, curItem, cell, format, radioList, checked) );
						}
					}
				
				UserInputList.add(radioList);
				v = pLay;
				}
			else if( inputType.contentEquals("S") )
				{
				Button Signature = new Button(mContext);
				Signature.setTextSize(app.Twix_Theme.headerSize);
				Signature.setTextColor(app.Twix_Theme.headerText);
				Signature.setBackgroundResource(R.drawable.button_bg);
				Signature.setOnClickListener(new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						if( !PopupOpen )
							{
							PopupOpen = true;
							Button bn = (Button) v;
							FormCell cell = (FormCell) bn.getTag();
							Twix_Signable signable = new Twix_Signable();
							signable.SetFormDisplay(Twix_AgentFormDisplay.this);
							signable.SetReadOnly(ReadOnly);
							signable.SetAffectedObject(bn);
							signable.SetCallback(SigCallback);
							if( cell.signature != null )
								{
								signable.SetSignature(cell.signature);
								}
							signable.show(Twix_AgentFormDisplay.this.getFragmentManager(), "Signable");
							}
						}
					});
				
				Signature.setTag(cell);
				Bitmap bmp = null;
				if( SigMap != null )
					{
					byte[] temp = (byte[]) SigMap.get(cell.MatrixId);
					if( temp != null )
						bmp = BitmapFactory.decodeByteArray(temp, 0, temp.length);
					}
				cell.signature = bmp;
				if( cell.signature != null )
					Signature.setText("Review Signature");
				else
					Signature.setText("Click To Sign");
				
				
				UserInputList.add(Signature);
				v = Signature;
				}
			else if( inputType.contentEquals("I") && cell.Image != null && cell.ImageType != null )
				{
				LinearLayout container = new LinearLayout(mContext);
				container.setGravity(format.GetGravity());
				container.setBackgroundDrawable(format.GetBackground(false));
				ImageView iv = new ImageView(mContext);
				LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				Bitmap bmp = BitmapFactory.decodeByteArray(cell.Image, 0, cell.Image.length);
				iv.setLayoutParams(ivParams);
				iv.setImageBitmap(bmp);
				
				container.addView(iv);
				v = container;
				}
			else if( cell.ChildFormId > 0 )
				{
				FormSectionDetails det = ReadFormSectionDetails(cell.ChildFormId);
				det.XRefId = cell.XRefId;
				ReadFormSection(cell.ChildFormId, det, cell.MatrixTrail);
				LinearLayout Section = new LinearLayout(mContext);
				HashMap<Long, Object> NextMatrixMap = null;
				HashMap<Long, Object> NextSigMap = null;
				if( MatrixMap != null )
					NextMatrixMap = (HashMap<Long, Object>) MatrixMap.get(cell.MatrixId);
				if( SigMap != null )
					NextSigMap = (HashMap<Long, Object>) SigMap.get(cell.MatrixId);
				
				Section.setBackgroundDrawable(format.GetBackground(false));
				Section.addView(PlaceCells(det, 1200, NextMatrixMap, NextSigMap, DropOptions.get(cell.ChildFormId)));
				
				v = Section;
				}
			else
				{
				TextView tv = new TextView(mContext);
				tv.setText("");
				tv.setTextColor(format.FontColor);
				tv.setBackgroundColor(format.bgcolor);
				v = tv;
				}
			
			if( v != null )
				{
				LinearLayout.LayoutParams params;
				params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				if( cell.width > cell.minSize )
					v.setMinimumWidth(cell.width);
				else
					v.setMinimumWidth(cell.minSize);
				
				if( cell.height > cell.minSize )
					v.setMinimumHeight(cell.width);
				else
					v.setMinimumHeight(cell.minSize);
				
				params.weight = weight;
				if( !(v instanceof AutoCompleteTextView) )
					v.setLayoutParams(params);
				v.setPadding(3, 3, 3, 3);
				v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				}
			}
		
		return v;
		}
	
	private View BuildDropDown(FormCell cell, CellFormatting format, HashMap<Long, Object> MatrixMap,
			Map<Long, List<String>> OptionsMap)
		{
		View ret = null;
		List<String> items = OptionsMap.get(cell.MatrixId);
		List<String> temp = new ArrayList<String>();
		DropDownArrayAdapter adapter = null;
		ViewGroup.LayoutParams params;
		//if( !cell.Modifiable && false )
		//	params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		//else
			params = new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		//if( items != null )
		//	{
			adapter = new DropDownArrayAdapter(mContext, temp, format, params);
		//	}
		if( items != null )
			adapter.addAll(items);
		
		
		if( cell.PickId > 0 )
			{
			//if( adapter == null )
			//	adapter = new DropDownArrayAdapter(mContext, new ArrayList<String>(), format, params);
			
			PickItem pItem;
			List<PickItem> pickItems = PickList.get(cell.PickId);
			int size = pickItems.size();
			for(int i = 0; i < size; i++ )
				{
				pItem = pickItems.get(i);
				adapter.add(pItem.Text);
				}
			}
		
		if( cell.Modifiable )
			{
			AutoCompleteTextView at = new AutoCompleteTextView(mContext);
			at.setEnabled(!ReadOnly);
			BorderDrawable d = new BorderDrawable(new RectShape(), format, true);
			at.setBackgroundDrawable(d);
			at.setTag(cell);
			
			at.setTextSize(format.FontSize);
			at.setTextColor(format.FontColor);
			at.setGravity(format.GetGravity());
			if( format.bold && format.italic )
				at.setTypeface(null, Typeface.BOLD_ITALIC);
			else if( format.bold )
				at.setTypeface(null, Typeface.BOLD);
			else if( format.italic )
				at.setTypeface(null, Typeface.ITALIC);
			
			if( format.underline )
				at.setPaintFlags(at.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			
			at.setAdapter(adapter);
			//at.setDropDownHeight(LayoutParams.WRAP_CONTENT);
			at.setThreshold(1);
			at.setOnClickListener(AutoCompleteClick);
			at.setOnFocusChangeListener(AutoCompleteFocus);
			
			if( MatrixMap != null && FormDataId != 0 )
				at.setText((String)MatrixMap.get(cell.MatrixId));
			
			UserInputList.add(at);
			ret = at;
			}
		else //If i don't want to work
			{
			// Hosting Relative Layout
			RelativeLayout host = new RelativeLayout(mContext);
			host.setBackgroundDrawable(format.GetBackground(false));
			
			// Spinner for DropDown
			Spinner sp = new Spinner(mContext);
			sp.setEnabled(!ReadOnly);
			sp.setTag(cell);
			RelativeLayout.LayoutParams spParams = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			sp.setLayoutParams(spParams);
			sp.setAdapter(adapter);
			host.addView(sp);
			
			// Arrow for DropDown
			spParams = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			spParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			spParams.addRule(RelativeLayout.CENTER_VERTICAL);
			spParams.setMargins(5, 5, 5, 5);
			ImageView arrow = new ImageView(mContext);
			arrow.setLayoutParams(spParams);
			arrow.setImageResource(R.drawable.drop_arrow);
			host.addView(arrow);
			
			if( FormDataId != 0 && MatrixMap != null)
				{
				String val = (String)MatrixMap.get(cell.MatrixId);
				String curItem;
				int size = adapter.size();
				for( int i = 0; i < size; i++ )
					{
					curItem = adapter.getItem(i);
					if( curItem != null && val != null && curItem.contentEquals(val) )
						{
						sp.setSelection(i);
						break;
						}
					}
				}
			
			// Use the Host as the cell layout
			ret = host;
			UserInputList.add(sp);
			}
		
		return ret;
		}
	
	private LinearLayout CreateSection()
		{
		LinearLayout row = new LinearLayout(mContext);
		row.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		row.setOrientation(LinearLayout.VERTICAL);
		
		return row;
		}
	
	public class BorderDrawable extends ShapeDrawable
		{
		int counter = 0;
		
		private Paint fillpaint;
		
		private Paint leftPaint;
		private Paint topPaint;
		private Paint bottomPaint;
		private Paint rightPaint;
		
		private CellFormatting cForm;
		
		public BorderDrawable(Shape s, CellFormatting formatting, boolean emboss)
			{
			super(s);
			cForm = formatting;
			
			fillpaint = new Paint( this.getPaint() );
			fillpaint.setColor(cForm.bgcolor);
			if( emboss )
				{
				EmbossMaskFilter fil = new EmbossMaskFilter(new float[]{-2f,-2f,0.3f}, 0.75f, 10, 1 );
				fillpaint.setMaskFilter(fil);
				}
			
			DashPathEffect dashed = new DashPathEffect(new float[] {10,20}, 0);
			
			if( cForm.bTop != null && cForm.bTop.width > 0 && cForm.bTop.style != NONE )
				{
				topPaint = new Paint();
				topPaint.setColor(cForm.bTop.color);
				topPaint.setStyle(Paint.Style.FILL);
				if( cForm.bTop.style == DASHED )
					topPaint.setPathEffect(dashed);
				topPaint.setStrokeWidth(cForm.bTop.width);
				}
				
			if( cForm.bLeft != null && cForm.bLeft.width > 0 && cForm.bLeft.style != NONE )
				{
				leftPaint = new Paint();
				leftPaint.setColor(cForm.bLeft.color);
				leftPaint.setStyle(Paint.Style.FILL);
				if( cForm.bLeft.style == DASHED )
					leftPaint.setPathEffect(dashed);
				leftPaint.setStrokeWidth(cForm.bLeft.width);
				}
			
			if( cForm.bBottom != null && cForm.bBottom.width > 0 && cForm.bBottom.style != NONE )
				{
				bottomPaint = new Paint();
				bottomPaint.setColor(cForm.bBottom.color);
				bottomPaint.setStyle(Paint.Style.FILL);
				if( cForm.bBottom.style == DASHED )
					bottomPaint.setPathEffect(dashed);
				bottomPaint.setStrokeWidth(cForm.bBottom.width);
				}
			
			if( cForm.bRight != null && cForm.bRight.width > 0 && cForm.bRight.style != NONE )
				{
				rightPaint = new Paint();
				rightPaint.setColor(cForm.bRight.color);
				rightPaint.setStyle(Paint.Style.FILL);
				if( cForm.bRight.style == DASHED )
					rightPaint.setPathEffect(dashed);
				rightPaint.setStrokeWidth(cForm.bRight.width);
				}
			
			}
		
		@Override
		protected void onDraw(Shape shape, Canvas canvas, Paint paint)
			{
			Rect rect = canvas.getClipBounds();
			shape.resize(rect.right, rect.bottom);
			shape.draw(canvas, fillpaint);
			
			if( topPaint != null )
				canvas.drawLine(0, cForm.bTop.width/2f, rect.right, cForm.bTop.width/2f, topPaint);
			
			if( leftPaint != null )
				canvas.drawLine(cForm.bLeft.width/2f, 0, cForm.bLeft.width/2f, rect.bottom, leftPaint);
			
			if( bottomPaint != null )
				canvas.drawLine(0, rect.bottom-(cForm.bBottom.width/2f), rect.right, rect.bottom-(cForm.bBottom.width/2f), bottomPaint);
			
			if( rightPaint != null )
				canvas.drawLine(rect.right-(cForm.bRight.width/2f), 0, rect.right-(cForm.bRight.width/2f), rect.bottom, rightPaint);
			
			}
		}
	
	public class RoundedBorderDrawable extends ShapeDrawable
		{
		int counter = 0;
		
		private Paint fillpaint;
		private Paint strokepaint;
		
		private BorderProperties border;
		
		public RoundedBorderDrawable(Shape s, int bgColor, BorderProperties bp, boolean emboss)
			{
			super(s);
			border = bp;
			
			fillpaint = new Paint( this.getPaint() );
			fillpaint.setColor(bgColor);
			if( emboss )
				{
				EmbossMaskFilter fil = new EmbossMaskFilter(new float[]{-1f,-1f,0.3f}, 0.7f, 10, 1 );
				fillpaint.setMaskFilter(fil);
				}
			
			DashPathEffect dashed = new DashPathEffect(new float[] {10,20}, 0);
			
			if( border != null && border.width > 0 && border.style != NONE )
				{
				strokepaint = new Paint();
				strokepaint.setColor(border.color);
				strokepaint.setStyle(Paint.Style.STROKE);
				if( border.style == DASHED )
					strokepaint.setPathEffect(dashed);
				strokepaint.setStrokeWidth(border.width);
				strokepaint.setAntiAlias(true);
				}
				
			}
		
		@Override
		protected void onDraw(Shape shape, Canvas canvas, Paint paint)
			{
			Rect rect = canvas.getClipBounds();
			//rect.top = (int)(border.width/2f);
			//rect.left = (int)(border.width/2f);
			//rect.right = rect.right-(int)(border.width/2f);
			//rect.bottom = rect.bottom-(int)(border.width/2f);
			shape.resize(rect.right, rect.bottom);
			shape.draw(canvas, fillpaint);
			
			if( strokepaint != null )
				{
				Matrix matrix = new Matrix();
			    matrix.setRectToRect(new RectF(0, 0, rect.right,
			    			rect.bottom),
			            new RectF(border.width/2, border.width/2, rect.right - border.width/2,
			            		rect.bottom - border.width/2),
			            Matrix.ScaleToFit.FILL);
			    canvas.concat(matrix);
				
				shape.resize(rect.right, rect.bottom);
				shape.draw(canvas, strokepaint);
				}
			}
		}
	
	
	/*******************************
	 * PickList Adapter Declaration
	 *******************************/
	public class PickListArrayAdapter extends ArrayAdapter<PickItem>
		{
		private List<PickItem>	items;
		private Context			mContext;
		private LinearLayout.LayoutParams layoutParams;
		private CellFormatting format;
		
		public PickListArrayAdapter(Context c, List<PickItem> items, CellFormatting format)
			{
			super(c, R.layout.spinner_popup, items);
			this.items = items;
			this.mContext = c;
			this.layoutParams = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			this.format = format;
			}
		
		// The View displayed for selecting in the list
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
			PickItem data = items.get(position);
			TextView tv = (TextView) super.getView(position, convertView, parent);
			
			// If the layout inflater fails/DropDown not set
			if (tv == null)
				{
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(app.Twix_Theme.headerSize);
				}
			
			tv.setTextColor(app.Twix_Theme.headerValue);
			tv.setText(data.Text);
			tv.setBackgroundColor(app.Twix_Theme.editBG);
			//tv.setBackgroundDrawable(format.GetBackground(false));
			
			return tv;
			}

		@Override
		public PickItem getItem(int position)
			{
			return items.get(position);
			}
		
		// Views displayed AFTER selecting
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
			{
			TextView tv = (TextView) convertView;
			PickItem data = items.get(position);
			
			if (tv == null)
				{
				//tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tv = new TextView(mContext);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(format.FontSize);
				}
			
			tv.setTextColor(format.FontColor);
			tv.setText(data.Text);
			//parent.setBackgroundColor(app.Twix_Theme.editBG);
			parent.setBackgroundDrawable(format.GetBackground(true));
				
			return tv;
			}
		
		public int size()
			{
			return items.size();
			}
		
		public List<PickItem> getItemList()
			{
			return items;
			}
		
		public Context getContext()
			{
			return this.mContext;
			}
		}
	
	/*******************************
	 * DropDown Adapter Declaration
	 *******************************/
	public class DropDownArrayAdapter extends ArrayAdapter<String>
		{
		private List<String>	items;
		private Context			mContext;
		private ViewGroup.LayoutParams layoutParams;
		private CellFormatting format;
		
		public DropDownArrayAdapter(Context c, List<String> items, CellFormatting format, ViewGroup.LayoutParams params)
			{
			super(c, R.layout.spinner_popup, items);
			this.items = items;
			this.mContext = c;
			this.layoutParams = params;//new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			this.format = format;
			}
		
		// The View displayed for selecting in the list
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
			TextView tv = (TextView) super.getView(position, convertView, parent);
			//TextView tv = (TextView) convertView;
			// If the layout inflater fails/DropDown not set
			if (tv == null)
				{
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tv = new TextView(mContext);
				tv.setPadding(4, 4, 4, 4);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(app.Twix_Theme.headerSize);
				//tv.setMinHeight(app.Twix_Theme.headerSize);
				}
			
			tv.setTextColor(app.Twix_Theme.headerValue);
			tv.setText(items.get(position));
			//tv.setBackgroundColor(app.Twix_Theme.editBG);
			tv.setBackgroundResource(R.drawable.clickable_bg2);
			//tv.setBackgroundDrawable(format.GetBackground(false));
			
			return tv;
			}

		@Override
		public String getItem(int position)
			{
			return items.get(position);
			}
		
		// Views displayed AFTER selecting
		@Override
		public View getView (int position, View convertView, ViewGroup parent)
			{
			TextView tv = (TextView) convertView;
			
			if (tv == null)
				{
				//tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.spinner_popup, null);
				tv = new TextView(mContext);
				tv.setLayoutParams(layoutParams);
				tv.setTextSize(format.FontSize);
				tv.setMinHeight(format.FontSize);
				}
			
			tv.setTextColor(format.FontColor);
			tv.setText(items.get(position));
			//parent.setBackgroundColor(app.Twix_Theme.editBG);
			parent.setBackgroundDrawable(format.GetBackground(true));
			
			return tv;
			}
		
		@Override
		public int getCount()
			{
			return items.size();
			}
		
		public int size()
			{
			return items.size();
			}
		
		public List<String> getItemList()
			{
			return items;
			}
		
		public Context getContext()
			{
			return this.mContext;
			}
		}
	
	/***********************
	 * Value Helper Functions
	 ***********************/
	private String[] SplitAndDecode(String toSplit, char delimiter)
		{
		String[] ret;
		String de = "["+delimiter+"]";
		ret = toSplit.split(de, -1);
		for( int i = 0; i < ret.length; i++ )
			try
				{
				ret[i] = URLDecoder.decode(ret[i], "UTF-8");
				}
			catch ( UnsupportedEncodingException e )
				{
				Toast.makeText(mContext,"Failed to retreive checkbox value '" +
						ret[i] + "'", Toast.LENGTH_LONG).show();
				e.printStackTrace();
				}
		
		return ret;
		}
	
	private boolean inArray(String test, String[] array )
		{
		boolean ret = false;
		for( int k = 0; k < array.length; k++ )
			{
			if( ret = test.contentEquals(array[k]) )
				break;
			}
		
		return ret;
		}
	
	/***********************
	 * Submission Functions
	 ***********************/
	
	@SuppressWarnings("unchecked")
	private void SubmitData()
		{
		Object o;
		FormCell cell = null;
		String value;
		Class<?> cls;
		TextView tv;
		Button sig;
		EditText et;
		AutoCompleteTextView at;
		Spinner sp;
		FormCheckBox cb;
		List<Object> objList;
		FormRadioButton bn;
		Object curobj;
		ContentValues cv = new ContentValues();
		app.db.db.beginTransaction();
		
		try
			{
			if( FormDataId == 0 )
				{
				FormDataId = app.db.newNegativeId("FormData", "FormDataId");
				cv.put("FormDataId", FormDataId);
				cv.put("FormId", FormId);
				cv.put("ParentId", ParentId);
				cv.put("ParentTable", ParentTable);
				cv.put("LinkId", LinkId);
				cv.put("LinkTable", LinkTable);
				
				cv.put("InputByEmpno", app.empno );
				cv.put("DateEntered", Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT_COMPLEX) );
				cv.put("Completed", "N" );
				cv.put("tabletMEID", app.getDeviceId() );
				app.db.db.insertOrThrow("FormData", null, cv);
				cv.clear();
				}
			else
				{
				cv.put("DateEntered", Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT_COMPLEX) );
				app.db.db.update("FormData", cv, "FormDataId=?", new String[] { FormDataId+"" });
				cv.clear();
				app.db.delete("FormDataValues", "FormDataId", FormDataId);
				app.db.delete("FormDataSignatures", "FormDataId", FormDataId);
				}
			
			UpdateDBPhotos(FormDataId);
			
			int size = UserInputList.size();
			int size2;
			for( int i = 0; i < size; i++ )
				{
				cv.put("FormDataId", FormDataId);
				o = UserInputList.get(i);
				
				cls = o.getClass();
				if( cls == TextView.class )
					{
					tv = (TextView) o;
					cell = (FormCell) tv.getTag();
					cv.put("MatrixTrail",		cell.MatrixTrail);
					cv.put("XRefId",			cell.XRefId);
					cv.put("Value",				tv.getText().toString());
					
					app.db.db.insertOrThrow("FormDataValues", null, cv);
					}
				else if( cls == EditText.class )
					{
					et = (EditText) o;
					cell = (FormCell) et.getTag();
					cv.put("MatrixTrail",		cell.MatrixTrail);
					cv.put("XRefId",			cell.XRefId);
					cv.put("Value",				et.getText().toString());
					
					app.db.db.insertOrThrow("FormDataValues", null, cv);
					}
				else if( cls == AutoCompleteTextView.class )
					{
					at = (AutoCompleteTextView) o;
					cell = (FormCell) at.getTag();
					cv.put("MatrixTrail",		cell.MatrixTrail);
					cv.put("XRefId",			cell.XRefId);
					cv.put("Value",				at.getText().toString());
					
					app.db.db.insertOrThrow("FormDataValues", null, cv);
					}
				else if( cls == Spinner.class )
					{
					sp = (Spinner) o;
					cell = (FormCell) sp.getTag();
					cv.put("MatrixTrail",		cell.MatrixTrail);
					cv.put("XRefId",			cell.XRefId);
					cv.put("Value",				(String)sp.getSelectedItem());
					
					app.db.db.insertOrThrow("FormDataValues", null, cv);
					}
				else if( cls == ArrayList.class)
					{
					cell = null;
					objList = (List<Object>) o;
					size2 = objList.size();
					value = null;
					boolean itemChecked = false;
					for( int j = 0; j < size2; j++ )
						{
						curobj = objList.get(j);
						if( curobj.getClass() == FormRadioButton.class )
							{
							bn = (FormRadioButton) curobj;
							if( bn.isChecked() )
								{
								itemChecked = true;
								cell = (FormCell) bn.getTag();
								value = bn.getText();
								}
							}
						else if( curobj.getClass() == FormCheckBox.class )
							{
							cb = (FormCheckBox) curobj;
							if( cb.isChecked() )
								{
								itemChecked = true;
								cell = (FormCell) cb.getTag();
								if( value == null )
									value = "";
								if( value.length() > 0 )
									value += "|";
								try
									{
									value += URLEncoder.encode(cb.getText(), "UTF-8");
									}
								catch ( UnsupportedEncodingException e )
									{
									Toast.makeText(mContext,"Failed to save checkbox value '" +
											cb.getText() + "'", Toast.LENGTH_LONG).show();
									e.printStackTrace();
									}
								
								if( value.length() == 0 && size2 > 1)
									value += "|";
								}
							}
						}
					
					if( cell != null )
						{
						cv.put("MatrixTrail",		cell.MatrixTrail);
						cv.put("XRefId",			cell.XRefId);
						cv.put("Value",				value);
						}
					
					if( itemChecked )
						app.db.db.insertOrThrow("FormDataValues", null, cv);
					}
				else if( cls == Button.class )
					{
					sig = (Button) o;
					cell = (FormCell) sig.getTag();
					cv.put("MatrixTrail",		cell.MatrixTrail);
					cv.put("XRefId",			cell.XRefId);
					byte[] signature = cell.getDBSignature();
					if( signature != null )
						cv.put("Value",				signature);
					else
						cv.putNull("Value");
					
					app.db.db.insertOrThrow("FormDataSignatures", null, cv);
					}
				cv.clear();
				}
			
			app.db.db.setTransactionSuccessful();
			this.dismiss();
			callback.Refresh();
			}
		catch( SQLiteException e)
			{
			e.printStackTrace();
			}
		finally
			{
			app.db.db.endTransaction();
			}
		}
	
	private void ConfirmDeleteData(final boolean cancelOnly)
		{
		AlertDialog alert = new AlertDialog.Builder(mContext).create();
		if( !cancelOnly )
			{
			alert.setTitle("Confirm Delete" );
	    	alert.setMessage( "Are you sure you want to delete this form?" );
			}
		else
			{
			alert.setTitle("Confirm Cancel" );
	    	alert.setMessage( "Are you sure you want to cancel this form?" );
			}
    	
    	alert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener()
    		{  
    		public void onClick(DialogInterface dialog, int which)
    			{
    			DeleteData(cancelOnly);
    			}
    		});
    	alert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener()
    		{  
    		public void onClick(DialogInterface dialog, int which)
    			{
    			return;  
    			}
    		});
    	alert.show();
		}
	
	private void DeleteData(boolean cancelOnly)
		{
		if( !cancelOnly )
			{
			app.db.delete("FormData", "FormDataId", FormDataId );
			app.db.delete("FormDataValues", "FormDataId", FormDataId );
			app.db.delete("FormPhotos", "FormDataId", FormDataId );
			app.db.delete("FormDataSignatures", "FormDataId", FormDataId );
			}
		try
			{
			this.dismiss();
			}
		catch( Exception e)
			{
			e.printStackTrace();
			}
		callback.Refresh();
		}
	
	/**
	 * Interface for the Form Display to communicate on. This allows the calling activity to
	 * 	make changes when the Form is closed or submitted
	 */
	public interface ActivityCallback
		{
		public void Refresh();
		}
	
	/**
	 * Form Photo Functions
	 */
	private void BuildPhotos()
		{
		FormPhoto fPhoto;
		byte[] photobytes;
		String sql = "SELECT FormPhotoId, Photo, DateCreated, Comments " +
					"FROM FormPhotos " + 
						"WHERE FormDataId = " + FormDataId;
		Cursor cursor = app.db.rawQuery(sql);
		if (cursor.moveToFirst())
			{
			do
				{
				fPhoto = new FormPhoto();
				fPhoto.FormPhotoId = cursor.getLong(0);
				photobytes = cursor.getBlob(1);
				fPhoto.Photo = BitmapFactory.decodeByteArray(photobytes, 0, photobytes.length);
				fPhoto.DateCreated = cursor.getString(2);
				fPhoto.Comments = cursor.getString(3);
				
				FormPhotos.add(fPhoto);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	/**
	 * Photo Results
	 * @return File to put the photo
	 */
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
		File file = null;
		try
			{
			file = createPhotoLocation();
			imageUri = Uri.fromFile(file);
			}
		catch ( Exception e )
			{
			Toast.makeText(mContext, "Unable to save photos, please check the SD Card.", Toast.LENGTH_LONG).show();
			}
		
		if( file != null &&  imageUri != null )
			{
			Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			intent.putExtra( MediaStore.EXTRA_OUTPUT, imageUri );
			((Activity)mContext).startActivityForResult( intent, PHOTO_INTENT );
			}
		}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
		if( requestCode == PHOTO_INTENT )
			{
			if (resultCode == -1)
				{
				AddFormPhoto(BitmapFactory.decodeFile( imageUri.getPath() ));
				}
			else if (resultCode == 0)
				{
				Toast.makeText(mContext, "Picture was Cancelled", Toast.LENGTH_SHORT).show();
				}
			else
				{
				Toast.makeText(mContext, "Picture was not taken", Toast.LENGTH_SHORT).show();
				}
			}
		else
			super.onActivityResult(requestCode, resultCode, data);
		}
	
	private void AddFormPhoto(Bitmap photoBMP)
		{
		System.gc();
	    Runtime.getRuntime().gc();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		photoBMP.compress(CompressFormat.JPEG, 50, bos); 
		byte[] bytes = bos.toByteArray();
		photoBMP = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		
		FormPhoto fPhoto = new FormPhoto();
		fPhoto.FormPhotoId = 0;
		fPhoto.Photo = Bitmap.createScaledBitmap(photoBMP, photoBMP.getWidth()/4, photoBMP.getHeight()/4, true);
		fPhoto.DateCreated = Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT_COMPLEX);
		fPhoto.Comments = "";
		
		FormPhotos.add(fPhoto);
		viewPhotosbn.setVisibility(View.VISIBLE);
		}
	
	private class FormPhoto
		{
		long FormPhotoId;
		Bitmap Photo;
		String DateCreated;
		String Comments = "";
		boolean delete;
		}
	
	private void ShowPhotos()
		{
		if( photoDialog != null )
			return;
		if( photoDialog != null && photoDialog.isShowing() )
			return;
		
		// Dialog Popup Box
		photoDialog = new Dialog(mContext);
		photoDialog.setOnCancelListener(new OnCancelListener()
			{
			@Override
			public void onCancel(DialogInterface dialog)
				{
				photoDialog = null;
				}
			});
		
		
		LinearLayout container = new LinearLayout(mContext);
		LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		container.setLayoutParams(cParams);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setFocusable(true);
		container.setFocusableInTouchMode(true);
		container.setPadding(0, 0, 0, 3);
		container.setBackgroundColor(app.Twix_Theme.disabledColor);
		
		NonFocusingScrollView scroller = new NonFocusingScrollView(mContext);
		LinearLayout.LayoutParams svParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		svParams.weight = 1;
		scroller.setLayoutParams(svParams);
		scroller.setFillViewport(true);
		container.addView(scroller);
		
		LinearLayout photoLayout = new LinearLayout(mContext);
		ScrollView.LayoutParams lParams = new ScrollView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		photoLayout.setLayoutParams(lParams);
		photoLayout.setOrientation(LinearLayout.VERTICAL);
		photoLayout.setFocusable(true);
		photoLayout.setFocusableInTouchMode(true);
		scroller.addView(photoLayout);
		
		ArrayList<EditText> Comments = new ArrayList<EditText>();
		
		FormPhoto fPhoto;
		int size = FormPhotos.size();
		for( int i = 0; i < size; i++ )
			{
			fPhoto = FormPhotos.get(i);
			// Don't show photos to be deleted
			if( !fPhoto.delete)
				photoLayout.addView(createPhotoLayout(fPhoto, Comments));
			}
		
		LinearLayout row = new LinearLayout(mContext);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(rowParams);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setPadding(0, 3, 0, 0);
		
		if( !ReadOnly )
			{
			Button savebn = new Button(mContext);
			LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, 35);
			saveParams.leftMargin = 15;
			savebn.setLayoutParams(saveParams);
			savebn.setTag(Comments);
			savebn.setText("Close");
			savebn.setBackgroundResource(R.drawable.button_bg);
			savebn.setTextSize(app.Twix_Theme.headerSize);
			savebn.setTextColor(app.Twix_Theme.headerText);
			savebn.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					if( photoDialog != null && photoDialog.isShowing() )
						{
						@SuppressWarnings("unchecked")
						ArrayList<EditText> Comments = (ArrayList<EditText>) v.getTag();
						EditText et;
						FormPhoto fPhoto;
						int size = Comments.size();
						for( int i = 0; i < size; i++ )
							{
							et = Comments.get(i);
							fPhoto = (FormPhoto) et.getTag();
							fPhoto.Comments = et.getText().toString();
							}
						photoDialog.dismiss();
						photoDialog = null;
						}
					}
				});
			row.addView(savebn);
			container.addView(row);
			}
		else
			{
			Button closebn = new Button(mContext);
			LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, 35);
			closeParams.leftMargin = 15;
			closebn.setLayoutParams(closeParams);
			closebn.setTag(Comments);
			closebn.setText("Close");
			closebn.setBackgroundResource(R.drawable.button_bg);
			closebn.setTextSize(app.Twix_Theme.headerSize);
			closebn.setTextColor(app.Twix_Theme.headerText);
			closebn.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					if( photoDialog != null && photoDialog.isShowing() )
						{
						photoDialog.dismiss();
						photoDialog = null;
						}
					}
				});
			row.addView(closebn);
			container.addView(row);
			}
		
		photoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		photoDialog.setContentView(container);
		photoDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		photoDialog.show();
		photoLayout.requestFocus();
		}
	
	private LinearLayout createPhotoLayout(FormPhoto fPhoto, ArrayList<EditText> Comments)
		{
		LinearLayout ret = new LinearLayout(mContext);
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lParams.setMargins(5, 5, 5, 5);
		ret.setLayoutParams(lParams);
		ret.setOrientation(LinearLayout.VERTICAL);
		ret.setTag(fPhoto);
		ret.setPadding(2, 2, 2, 2);
		ret.setBackgroundColor(app.Twix_Theme.tableBG);
		
		LinearLayout row = new LinearLayout(mContext);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(rowParams);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setBackgroundColor(app.Twix_Theme.headerBG);
		row.setPadding(5, 5, 5, 5);
		
		TextView dateChanged = new TextView(mContext);
		LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dateParams.weight = 1;
		dateParams.gravity = Gravity.LEFT;
		dateChanged.setLayoutParams(dateParams);
		dateChanged.setTextSize(app.Twix_Theme.headerSize);
		dateChanged.setTextColor(app.Twix_Theme.headerText);
		dateChanged.setText( Twix_TextFunctions.ComplexToNormal(fPhoto.DateCreated) );
		row.addView(dateChanged);
		
		if( !ReadOnly )
			{
			ImageView deletebn = new ImageView(mContext);
			LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			deleteParams.gravity = Gravity.CENTER_VERTICAL;
			deletebn.setLayoutParams(deleteParams);
			deletebn.setImageResource(R.drawable.minus2);
			deletebn.setTag(ret);
			deletebn.setOnClickListener(new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						LinearLayout deleteme = (LinearLayout) v.getTag();
						LinearLayout Host = (LinearLayout) deleteme.getParent();
						FormPhoto fPhoto = (FormPhoto) deleteme.getTag();
						FormPhotos.remove(fPhoto);
						
						if( FormPhotos.size() <= 0 )
							{
							viewPhotosbn.setVisibility(View.GONE);
							if( photoDialog != null && photoDialog.isShowing() )
								{
								photoDialog.dismiss();
								photoDialog = null;
								}
							}
						Host.removeView(deleteme);
						}
					});
			row.addView(deletebn);
			}
		
		ret.addView(row);
		
		ImageView photo = new ImageView(mContext);
		LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		photo.setLayoutParams(photoParams);
		photo.setImageBitmap(fPhoto.Photo);
		photo.setScaleType(ImageView.ScaleType.FIT_CENTER);
		ret.addView(photo);
		
		TextView commentText = new TextView(mContext);
		commentText.setLayoutParams(rowParams);
		commentText.setTextSize(app.Twix_Theme.headerSize);
		commentText.setTextColor(app.Twix_Theme.headerText);
		commentText.setText( "Comments:" );
		commentText.setBackgroundColor(app.Twix_Theme.headerBG);
		commentText.setPadding(5, 0, 0, 0);
		ret.addView(commentText);
		
		EditText comments = new EditText(mContext);
		comments.setEnabled(!ReadOnly);
		comments.setTag(fPhoto);
		comments.setLayoutParams(rowParams);
		comments.setBackgroundResource(R.drawable.editbox);
		comments.setTextSize(app.Twix_Theme.headerSize);
		comments.setTextColor(app.Twix_Theme.headerValue);
		comments.setText(fPhoto.Comments);
		comments.setPadding(2, 2, 2, 2);
		ret.addView(comments);
		Comments.add(comments);
		
		return ret;
		}
	
	private void UpdateDBPhotos(long FormDataId)
		{
		ContentValues cv = new ContentValues();
		
		FormPhoto fPhoto;
		ByteArrayOutputStream bos;
		byte[] bytes;
		int size = FormPhotos.size();
		for( int i = 0; i < size; i++ )
			{
			fPhoto = FormPhotos.get(i);
			
			if( fPhoto.FormPhotoId == 0 )
				{
				bos = new ByteArrayOutputStream(); 
				fPhoto.Photo.compress(CompressFormat.JPEG, 100, bos); 
				bytes = bos.toByteArray();
				
				cv.put("FormPhotoId", app.db.newNegativeId("FormPhotos", "FormPhotoId"));
				cv.put("FormDataId", FormDataId);
				cv.put("Photo", bytes);
				cv.put("DateCreated", Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT_COMPLEX));
				cv.put("Comments", fPhoto.Comments);
				
				try
					{ app.db.db.insertOrThrow("FormPhotos", null, cv); }
				catch( Exception e)
					{ e.printStackTrace(); }
				}
			else if( fPhoto.delete )
				{
				app.db.delete("FormPhoto", "FormPhotoId", fPhoto.FormPhotoId);
				}
			else 
				{
				cv.put("Comments", fPhoto.Comments);
				try
					{ app.db.update("FormPhotos", cv, "FormPhotoId", fPhoto.FormPhotoId); }
				catch( Exception e)
					{ e.printStackTrace(); }
				}
			cv.clear();
			}
		}
	
	public void setPopupStatus(boolean status)
		{
		this.PopupOpen = status;
		}
	}