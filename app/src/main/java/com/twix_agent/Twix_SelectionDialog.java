package com.twix_agent;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Twix_SelectionDialog
	{
	private Dialog dialog;
	private Context mContext;
	private OnClickListener RowListener;
	private ArrayList<RowInfo> RowInfo;
	private String DialogTitle;
	
	public Twix_SelectionDialog(Context c, OnClickListener rListener, ArrayList<RowInfo> rInfo, String title)
		{
		mContext = c;
		RowListener = rListener;
		RowInfo = rInfo;
		DialogTitle = title;
		}
	
	public Dialog generateDialog()
		{
		dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.selectiondialog, null);
		populateSelections( (LinearLayout) viewToLoad.findViewById(R.id.container));
		((TextView) viewToLoad.findViewById(R.id.Title)).setText(DialogTitle);
		viewToLoad.findViewById(R.id.Cancel).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				dialog.dismiss();
				}
			});
		dialog.setContentView(viewToLoad);
		return dialog;
		}
	
	private void populateSelections(LinearLayout container)
		{
		RowInfo info;
		int size = RowInfo.size();
		for( int i = 0; i < size; i++ )
			{
			info = RowInfo.get(i);
			info.contents.setTag(info.tag);
			info.contents.setOnClickListener(RowListener);
			
			container.addView(info.contents);
			}
		}
	
	static public class RowInfo
		{
		Object tag;
		View contents;
		}
	}
