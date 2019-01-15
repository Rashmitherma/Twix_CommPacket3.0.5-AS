package com.twix_agent;

import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Twix_StepList extends Dialog
	{
	private String[] sql;
	private String[] sqlIds;
	private String[][] ColumnHeaders;
	private float[][] weight;
	private ActivityCallback callback;
	
	private Twix_Application app;
	private Context mContext;
	private LinearLayout MainLayout;
	private LinearLayout ListHost;
	private ScrollView ListScroller;
	private View.OnClickListener rowClick;
	private int CurrentStep = 0;
	
	private String[] CurrentArguement;
	private Map<String,String> ResultIds;
	
	public Twix_StepList(Context context)
		{
		super(context);
		mContext = context;
		
		MainLayout = new LinearLayout(mContext);
		MainLayout.setOrientation(LinearLayout.VERTICAL);
		MainLayout.setLayoutParams(
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		ListHost = new LinearLayout(mContext);
		ListHost.setOrientation(LinearLayout.VERTICAL);
		ListHost.setLayoutParams(
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		ListScroller = new ScrollView(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		ListScroller.setLayoutParams(params);
		ListScroller.addView(ListHost);
		MainLayout.addView(ListScroller);
		
		ResultIds = new HashMap<String,String>();
		
		rowClick = new View.OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				@SuppressWarnings("unchecked")
				Map<String,String> rowDetail = (Map<String, String>) v.getTag();
				String Identifier = rowDetail.get(sqlIds[CurrentStep]);
				ResultIds.put(sqlIds[CurrentStep], Identifier);
				
				CurrentStep++;
				if( CurrentStep < weight.length )
					{
					CurrentArguement = new String[] {Identifier};
					ClearList();
					PopulateList();
					}
				else
					{
					callback.Result(ResultIds);
					Twix_StepList.this.dismiss();
					}
				}
			}
		;
		}
	
	public void Setup(Twix_Application app, String[] sql, String[] sqlIds, String[][] ColumnHeaders, float[][] weight, String CurrentArguement, ActivityCallback callback)
		{
		this.app = app;
		this.sql = sql;
		this.sqlIds = sqlIds;
		this.ColumnHeaders = ColumnHeaders;
		this.weight = weight;
		this.CurrentArguement = new String[] {CurrentArguement};
		this.callback = callback;
		
		MainLayout.setBackgroundColor(app.Twix_Theme.lineColor);
		MainLayout.addView(BuildButtons());
		
		PopulateList();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MainLayout);
		getWindow().setLayout(1000, LayoutParams.WRAP_CONTENT);
		}
	
	private void ClearList()
		{
		MainLayout.removeViewAt(0);
		ListHost.removeAllViews();
		}
	
	private LinearLayout GenerateColumnHeader()
		{
		LinearLayout row = CreateRow();
		for( int i = 0; i < ColumnHeaders[CurrentStep].length; i++ )
			row.addView(GenerateText(ColumnHeaders[CurrentStep][i], weight[CurrentStep][i], true) );
		
		return row;
		}
	
	private void PopulateList()
		{
		Cursor cursor;
		if( CurrentArguement[0] == null )
			cursor = app.db.rawQuery(sql[CurrentStep]);
		else
			cursor = app.db.db.rawQuery(sql[CurrentStep], CurrentArguement );
		
		LinearLayout row;
		Map<String,String> rowDetail;
		String column;
		String value;
		int wIndex;
		int colCnt;
		MainLayout.addView(GenerateColumnHeader(), 0);
		while( cursor.moveToNext() )
			{
			colCnt = cursor.getColumnCount();
			wIndex = 0;
			rowDetail = new HashMap<String,String>();
			row = CreateRow();
			row.setBackgroundResource(R.drawable.clickable_bg);
			row.setTag(rowDetail);
			row.setOnClickListener(rowClick);
			for( int i = 0; i < colCnt; i++ )
				{
				column = cursor.getColumnName(i);
				value = cursor.getString(i);
				if( !sqlIds[CurrentStep].contentEquals(column) )
					{
					row.addView(GenerateText(value, weight[CurrentStep][wIndex], false) );
					wIndex++;
					}
				rowDetail.put(column, value);
				}
			
			ListHost.addView(row);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		}
	
	private LinearLayout BuildButtons()
		{
		LinearLayout row = CreateRow();
		
		Button bn = new Button(mContext);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 40);
		params.setMargins(3, 3, 3, 3);
		bn.setLayoutParams(params);
		bn.setTextSize(app.Twix_Theme.headerSize);
		bn.setTextColor(app.Twix_Theme.headerText);
		bn.setBackgroundResource(R.drawable.button_bg);
		bn.setPadding(2,2,2,2);
		bn.setText("Cancel");
		bn.setOnClickListener(new View.OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				Twix_StepList.this.dismiss();
				}
			})
		;
		
		row.addView(bn);
		return row;
		}
	
	private LinearLayout CreateRow()
		{
		LinearLayout row = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(params);
		row.setOrientation(LinearLayout.HORIZONTAL);
		
		return row;
		}
	
	private TextView GenerateText(String text, float wt, boolean header)
		{
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
		params.setMargins(2, 2, 2, 2);
		params.weight = wt;
		tv.setLayoutParams(params);
		tv.setTextSize(app.Twix_Theme.headerSize);
		tv.setTextColor(app.Twix_Theme.headerText);
		if( header )
			tv.setBackgroundColor(app.Twix_Theme.headerBG);
		else
			tv.setBackgroundColor(app.Twix_Theme.sub2BG);
		tv.setPadding(2,6,2,6);
		
		tv.setText(text);
		return tv;
		}
	
	public interface ActivityCallback
		{
		public void Result(Map<String,String> results);
		}
	}
