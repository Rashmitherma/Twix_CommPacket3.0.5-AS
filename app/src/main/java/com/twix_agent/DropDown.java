package com.twix_agent;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DropDown extends Button
	{
	private Context mContext;
	private OnClickListener altClickListener;
	private List<Item> items;
	private Dialog OptionsPopup;
	
	public DropDown(Context mContext, List<Item> items)
		{
		super(mContext);
		this.mContext = mContext;
		this.items = items;
	
		this.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				if( OptionsPopup == null )
					ShowOptions();
				}
			})
		;
		}
	
	private class Item
		{
		private String text;
		private Object tag;
		
		public Item(String text, Object tag)
			{
			this.text = text;
			this.tag = tag;
			}
		
		public Item(String text)
			{
			this.text = text;
			this.tag = null;
			}
		
		public String getText()
			{
			return text;
			}
		
		public Object getTag()
			{
			return tag;
			}
		}
	
	@Override
	public void setOnClickListener(OnClickListener click)
		{
		this.altClickListener = click;
		}
	
	private void ShowOptions()
		{
		OptionsPopup = new Dialog(mContext);
		
		ScrollView scroller = new ScrollView(mContext);
		LinearLayout.LayoutParams paramsSV = new LinearLayout.LayoutParams(
				800, LayoutParams.FILL_PARENT);
		scroller.setLayoutParams(paramsSV);
		
		LinearLayout listContainer = new LinearLayout(mContext);
		LinearLayout.LayoutParams paramsC = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		listContainer.setLayoutParams(paramsC);
		
		scroller.addView(listContainer);
		
		int size = items.size();
		for( int i = 0; i < size; i++ )
			{
			listContainer.addView(GenerateItemView(items.get(i)));
			}
		
		OptionsPopup.setContentView(scroller);
		OptionsPopup.show();
		}
	
	private TextView GenerateItemView(Item item)
		{
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(params);
		
		tv.setText(item.getText());
		tv.setTag(item);
		
		tv.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				Item item = (Item) v.getTag();
				DropDown self = DropDown.this;
				self.setText(item.getText());
				
				
				OptionsPopup.dismiss();
				OptionsPopup = null;
				}
			})
		;
		
		return tv;
		}
	
	}
