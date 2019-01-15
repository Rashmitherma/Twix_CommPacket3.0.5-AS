package com.twix_agent;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class FormLayout extends ViewGroup
	{
	private int MaxWidth = 0;
	float[] EffectiveWidths;
	
	private FormCell[][] CellMatrix;
	private int[] mHeight;
	
	private int cell_padding_y;
	private int cell_padding_x;
	
	private int cell_spacing_y;
	private int cell_spacing_x;
	
	int curX = 0;
	int curY = 0;
	
	private class FormCell
		{
		View v;
		int rowSpan;
		int colSpan;
		
		public FormCell(View v)
			{
			this.v = v;
			rowSpan = 1;
			colSpan = 1;
			}
		
		public FormCell(View v, int rowSpan, int colSpan)
			{
			this.v = v;
			this.rowSpan = rowSpan;
			this.colSpan = colSpan;
			}
		}
	
	public FormLayout(Context context, int MaxWidth, String[] SetWidths, String[] SetHeights)
		{
		super(context);
		mHeight = new int[SetHeights.length];
		//mWidth = new int[SetWidths.length];
		
		this.MaxWidth = MaxWidth;
		EffectiveWidths = buildWidths(SetWidths);
		//this.SetHeights = SetHeights;
		CellMatrix = new FormCell[SetHeights.length][SetWidths.length];
		
		
		
		cell_padding_y = 0;
		cell_padding_x = 0;
		
		cell_spacing_y = 0;
		cell_spacing_x = 0;
		}
	
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams (ViewGroup.LayoutParams p) 
		{
		return new ViewGroup.LayoutParams(p.width, p.height);
		}
	
	public void setCellPadding( int y_padding, int x_padding )
		{
		cell_padding_y = y_padding;
		cell_padding_x = x_padding;
		}
	
	public void setCellSpacing( int y_spacing, int x_spacing )
		{
		cell_spacing_y = y_spacing;
		cell_spacing_x = x_spacing;
		}
	
	private int getTotalHeight()
		{
		int ret = 0;
		for( int y = 0; y < mHeight.length; y++ )
			ret += mHeight[y] + (cell_padding_y*2) + (cell_spacing_y);
		return ret + cell_spacing_y;
		}
	
	private int getMaxHeight(int rowIndex)
		{
		int ret = 0;
		int cur;
		FormCell cell;
		//for( int x = 0; x < mWidth.length; x++ )
		for( int x = 0; x < EffectiveWidths.length; x++ )
			{
			cell = CellMatrix[rowIndex][x];
			if( cell != null && cell.v.getVisibility() != View.GONE )
				{
				cur = cell.v.getMeasuredHeight();
				for( int y = rowIndex+1; y < (cell.rowSpan + rowIndex); y++ )
					{
					cur -= mHeight[y];
					}
				
				if(cur > ret)
					ret = cur;
				}
			}
		
		return ret;
		}
	
	private float getEffectiveCellWidth(int cellIndex, int colSpan)
		{
		float width = 0;
		for( int x = cellIndex; x < EffectiveWidths.length && x < cellIndex+colSpan; x++ )
			{
			width += EffectiveWidths[x];
			}
		
		return width;
		}
	
	private float[] buildWidths(String[] SetWidths)
		{
		float[] EffectiveWidths = new float[SetWidths.length];
		
		String temp;
		float per;
		float px;
		float totalPercentage = 0;
		int emptyColCnter = 0;
		float leftoverPercentage = 0;
		if( mHeight.length > 0 )
			{
			for( int x = 0; x < EffectiveWidths.length; x++ )
				{
				temp = SetWidths[x];
				if( temp != null && temp.contains("%") )
					{
					try { per = Float.parseFloat(temp.replaceAll("%", "")); }
					catch(Exception e) {per = 0;}
					EffectiveWidths[x] = (per/100);
					}
				else if( temp != null && temp.contains("px") )
					{
					try { px = Float.parseFloat(temp.replaceAll("px", "")); }
					catch(Exception e) {px = 0;}
					EffectiveWidths[x] = 100*(px/MaxWidth);
					}
				else
					{
					EffectiveWidths[x] = 0;
					emptyColCnter++;
					}
				
				totalPercentage += EffectiveWidths[x];
				}
			
			if( emptyColCnter > 0 )
				{
				leftoverPercentage = ((1-totalPercentage)/emptyColCnter);
				if( leftoverPercentage < 0 )
					leftoverPercentage = 0;
				
				for( int x = 0; x < EffectiveWidths.length; x++ )
					{
					if( EffectiveWidths[x] == 0 )
						EffectiveWidths[x] = leftoverPercentage;
					}
				}
			}
		
		return EffectiveWidths;
		}
	
	@Override
	public void addView (View child) 
		{
		//if( curX > mWidth.length )
		if( curX > EffectiveWidths.length )
			{
			curX = 0;
			curY++;
			}
		if( curY > mHeight.length )
			curY = 0;
		
		CellMatrix[curX][curY] = new FormCell(child);
		super.addView(child);
		curX++;
		}
	
	public void addView (View child, int x, int y, int rowSpan, int colSpan) 
		{
		CellMatrix[y][x] = new FormCell(child, rowSpan, colSpan);
		super.addView(child);
		}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
		final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
		
        for( int y = mHeight.length-1; y >= 0; y-- )
			mHeight[y] = getMaxHeight(y);
        
		FormCell cell;
		for( int y = 0; y < mHeight.length; y++ )
			{
			for( int x = 0; x < EffectiveWidths.length; x++ )
				{
				cell = CellMatrix[y][x];
				if( cell != null && (cell.v.getVisibility() != GONE) )
					{
					int affectiveHeight = 0;
					for( int i = y; (i < mHeight.length) && (i < y+cell.rowSpan); i++ )
						affectiveHeight += mHeight[i];
					
					int affectiveWidth = (int) (getEffectiveCellWidth(x, cell.colSpan) * width);
					cell.v.measure(
							MeasureSpec.makeMeasureSpec(affectiveWidth, MeasureSpec.EXACTLY), 
							MeasureSpec.makeMeasureSpec(affectiveHeight, MeasureSpec.UNSPECIFIED) );
					}
				}
			}
		
		setMeasuredDimension(width, getTotalHeight());
		}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
		{
		int xpos = getPaddingLeft() + cell_spacing_x;
		int ypos = getPaddingTop() + cell_spacing_y + cell_padding_y;
        
        FormCell cell;
        int affectiveWidth;
		int affectiveHeight;
        for( int y = 0; y < mHeight.length; y++ )
        	{
        	for( int x = 0; x < EffectiveWidths.length; x++ )
				{
				cell = CellMatrix[y][x];
				
				if( cell != null && cell.v.getVisibility() != View.GONE )
					{
					affectiveWidth = (int) ((r-l) * getEffectiveCellWidth(x, cell.colSpan));
					affectiveHeight = 0;
					for( int i = y; (i < mHeight.length) && (i < y+cell.rowSpan); i++ )
						affectiveHeight += mHeight[i];
					
					if( ((x + (cell.colSpan-1)) == EffectiveWidths.length-1) && (affectiveWidth+xpos) < (r))
						affectiveWidth += r-(affectiveWidth+xpos);
					
					cell.v.layout(xpos, ypos, xpos + affectiveWidth, ypos + affectiveHeight);
					}
				else
					affectiveWidth = (int) ((r-l) * getEffectiveCellWidth(x, 1));
				xpos += affectiveWidth = (int) ((r-l) * getEffectiveCellWidth(x, 1)) + cell_spacing_x + (cell_padding_x*2);
				}
        	xpos = getPaddingLeft() + cell_spacing_x;
        	ypos += mHeight[y] + cell_spacing_y + cell_padding_y;
        	}
		}
	
	}
