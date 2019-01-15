package com.twix_agent;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class Twix_Signable extends DialogFragment
	{
	private Twix_Application app;
	private Context mContext;
	private LinearLayout Main;
	private Callback callback;
	private Bitmap prevSig = null;
	private Panel Signature;
	private Object AffectedObject;
	private Twix_AgentFormDisplay FormDisplay;
	private boolean ReadOnly = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
		this.setCancelable(false);
		app = (Twix_Application) this.getActivity().getApplication();
		mContext = this.getActivity();
		Main = new LinearLayout(mContext);
		Main.setOrientation(LinearLayout.VERTICAL);
		Main.setLayoutParams(new LinearLayout.LayoutParams
				(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		Main.setBackgroundColor(app.Twix_Theme.headerBG);
		
		TextView tv = new TextView(mContext);
		tv.setText("Please Sign");
		tv.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		tv.setTextSize(app.Twix_Theme.headerSizeLarge);
		tv.setTextColor(app.Twix_Theme.headerText);
		tv.setPadding(5, 5, 5, 5);
		Main.addView(tv);
		
		Signature = new Panel(mContext, prevSig, ReadOnly);
		LinearLayout.LayoutParams SigParams =
				new LinearLayout.LayoutParams(1000, 300);
		SigParams.setMargins(5, 5, 5, 5);
		Signature.setLayoutParams(SigParams);
		Main.addView(Signature);
		
		LinearLayout row = CreateRow();
		row.setPadding(5, 5, 5, 5);
		Main.addView(row);
		
		LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
				100, 40);
		buttonParams.setMargins(5, 5, 20, 5);
		
		if( !ReadOnly )
			{
			// Save Button
			Button Save = new Button(mContext);
			Save.setLayoutParams(buttonParams);
			Save.setText("Save");
			Save.setTextSize(app.Twix_Theme.headerSize);
			Save.setTextColor(app.Twix_Theme.headerText);
			Save.setBackgroundResource(R.drawable.button_bg);
			Save.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					callback.Saved(Signature.getSignature(), AffectedObject);
					Twix_Signable.this.dismiss();
					}
				});
			row.addView(Save);
			
			// Clear Signature Button
			Button Clear = new Button(mContext);
			Clear.setLayoutParams(buttonParams);
			Clear.setText("Clear");
			Clear.setTextSize(app.Twix_Theme.headerSize);
			Clear.setTextColor(app.Twix_Theme.headerText);
			Clear.setBackgroundResource(R.drawable.button_bg);
			Clear.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					Signature.clear();
					}
				});
			row.addView(Clear);
			
			// Delete Signature Button
			Button Delete = new Button(mContext);
			Delete.setLayoutParams(buttonParams);
			Delete.setText("Delete");
			Delete.setTextSize(app.Twix_Theme.headerSize);
			Delete.setTextColor(app.Twix_Theme.headerText);
			Delete.setBackgroundResource(R.drawable.button_bg);
			Delete.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					callback.Delete(AffectedObject);
					Twix_Signable.this.dismiss();
					}
				});
			
			row.addView(Delete);
			}
		else
			{
			// Close window - Read Only
			Button Close = new Button(mContext);
			Close.setLayoutParams(buttonParams);
			Close.setText("Close");
			Close.setTextSize(app.Twix_Theme.headerSize);
			Close.setTextColor(app.Twix_Theme.headerText);
			Close.setBackgroundResource(R.drawable.button_bg);
			Close.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					Twix_Signable.this.dismiss();
					}
				});
			
			row.addView(Close);
			}
		}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
		{
		return Main;
		}
	
	public void SetReadOnly(boolean ReadOnly)
		{
		this.ReadOnly = ReadOnly;
		}
	
	public void SetFormDisplay(Twix_AgentFormDisplay display)
		{
		FormDisplay = display;
		}
	
	public void SetSignature(Bitmap bmp)
		{
		if( bmp != null )
			prevSig = bmp;
		}
	
	public void SetAffectedObject(Object obj)
		{
		this.AffectedObject = obj;
		}
	
	public void SetCallback(Callback callback)
		{
		this.callback = callback;
		}
	
	private LinearLayout CreateRow()
		{
		LinearLayout row = new LinearLayout(mContext);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(rowParams);
		row.setOrientation(LinearLayout.HORIZONTAL);
		
		return row;
		}
	
	interface Callback
		{
		public void Saved(Bitmap bmp, Object obj);
		public void Delete(Object obj);
		}
	
	@Override
	public void onDismiss(DialogInterface dialog)
		{
		if( FormDisplay != null )
			FormDisplay.setPopupStatus(false);
		super.onDismiss(dialog);
		}
	}

//Signature Drawing Panel
class Panel extends GLSurfaceView implements SurfaceHolder.Callback
	{
	private SurfaceHolder mHolder;
	private Bitmap		mBitmap;
	private Canvas		mCanvas;
	private final Paint	mPaint;
	private float		mCurX;
	private float		mCurY;
	private float		pCurX;
	private float		pCurY;
	private Paint		LinePaint = new Paint();
	private boolean		ReadOnly;
	
	public Panel(Context context, Bitmap bmp, boolean ReadOnly)
		{
		super(context);
		this.ReadOnly = ReadOnly;
		this.setZOrderOnTop(true);
		
		getHolder().addCallback(this);

		mPaint = new Paint();
		mPaint.setARGB(255, 255, 255, 255);

		pCurX = -1;
		pCurY = -1;

		if (bmp != null)
			{
			mBitmap = bmp;
			}
		
		LinePaint.setARGB(255, 0, 0, 0);
		LinePaint.setStrokeWidth(2);
		
		mHolder = getHolder();
		}

	public void doDraw(Canvas canvas)
		{
		if (mBitmap != null)
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
		{
		// Do nothing
		}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
		{
		Canvas canvas = holder.lockCanvas();
		if( mBitmap == null )
			canvas.drawARGB(255, 255, 255, 255);
		else
			canvas.drawBitmap(mBitmap, 0, 0, null);
		holder.unlockCanvasAndPost(canvas);
		}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
		{
		//if (mThread.isAlive())
			{
		//	mThread.setRunning(false);
			}
		}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
		{
		Bitmap newBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.RGB_565);
		newBitmap.eraseColor(Color.WHITE);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		if (mBitmap != null)
			{
			newCanvas.drawBitmap(mBitmap, 0, 0, null);
			}

		mBitmap = newBitmap;
		mCanvas = newCanvas;
		}
	
	private void Draw()
		{
		Canvas c = mHolder.lockCanvas();
		doDraw(c);
		mHolder.unlockCanvasAndPost(c);
		}
	
	@Override
	protected void onDraw(Canvas canvas)
		{
		if (mBitmap != null)
			canvas.drawBitmap(mBitmap, 0, 0, null);
		else
			canvas.drawARGB(255, 255, 255, 255);
		}
	
	public void clear()
		{
		if (mCanvas != null)
			{
			mCanvas.drawPaint(mPaint);
			Draw();
			}
		}

	@Override
	public boolean onTouchEvent(MotionEvent event)
		{
		if( ReadOnly )
			return true;
		
		int action = event.getActionMasked();
		if (action != MotionEvent.ACTION_UP
				&& action != MotionEvent.ACTION_CANCEL)
			{
			int N = event.getHistorySize();
			int P = event.getPointerCount();
			for (int i = 0; i < N; i++)
				{
				for (int j = 0; j < P; j++)
					{
					mCurX = event.getHistoricalX(j, i);
					mCurY = event.getHistoricalY(j, i);
					drawPoint(mCurX, mCurY);
					}
				}
			for (int j = 0; j < P; j++)
				{
				mCurX = event.getX(j);
				mCurY = event.getY(j);
				drawPoint(mCurX, mCurY);
				}
			}
		if (action == MotionEvent.ACTION_UP)
			{
			pCurX = -1;
			pCurY = -1;
			}
		
		Draw();
		return true;
		}

	private void drawPoint(float x, float y)
		{
		if (mBitmap != null)
			{
			if (pCurX >= 0 && pCurY >= 0)
				mCanvas.drawLine(pCurX, pCurY, x, y, LinePaint);
				
			mCanvas.drawPoint(x, y, LinePaint);
			pCurX = x;
			pCurY = y;
			}
		}

	public Bitmap getSignature()
		{
		return mBitmap;
		}
	
	}
