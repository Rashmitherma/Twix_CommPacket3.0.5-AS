package com.twix_agent;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;

public class NonFocusingScrollView extends ScrollView
	{
	public NonFocusingScrollView(Context context)
		{
		super(context);
		}

	public NonFocusingScrollView(Context context, AttributeSet attrs)
		{
		super(context, attrs);
		}

	public NonFocusingScrollView(Context context, AttributeSet attrs,
			int defStyle)
		{
		super(context, attrs, defStyle);
		}

	@Override
	protected boolean onRequestFocusInDescendants(int direction,
			Rect previouslyFocusedRect)
		{
		return true;
		}
	
	@Override
	public boolean fullScroll (int direction)
		{
		return true;
		}
	
	@Override
	public boolean pageScroll (int direction)
		{
		return true;
		}
	
	@Override
	public ArrayList<View> getFocusables(int direction)
		{
		return new ArrayList<View>();
		}
	
	}
