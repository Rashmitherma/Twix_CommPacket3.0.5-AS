package com.twix_agent;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

/*******************************************************************************************************************
 * Class: Twix_SQLite
 * 
 * Purpose: Manages the Activity group hierarchy. Basically all the activities are listed in a stack and when the
 * 			back button is pressed, activities are finished until the top level activity.
 * 
 * Relevant XML: none
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *
 ********************************************************************************************************************/
public class Twix_TabActivityGroup extends ActivityGroup
	{
	private InputMethodManager imm;
	private ArrayList<String> mIdList;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		if (mIdList == null)
			mIdList = new ArrayList<String>();
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		}
	
	/**
	* This is called when a child activity of this one calls its finish method.
	* This implementation calls {@link LocalActivityManager#destroyActivity} on the child activity
	* and starts the previous activity.
	* If the last child activity just called finish(),this activity (the parent),
	* calls finish to finish the entire group.
	*/
	@Override
	public void finishFromChild(Activity child)
		{
		LocalActivityManager manager = getLocalActivityManager();
		int index = mIdList.size()-1;
		
		if (index < 1)
			{
			finish();
			return;
			}
	
		manager.destroyActivity(mIdList.get(index), true);
		mIdList.remove(index);
		index--;
		String lastId = mIdList.get(index);
		Intent lastIntent = manager.getActivity(lastId).getIntent();
		Window newWindow = manager.startActivity(lastId, lastIntent);
		setContentView(newWindow.getDecorView());
		}
	
	/**
	* Starts an Activity as a child Activity to this.
	* @param Id Unique identifier of the activity to be started.
	* @param intent The Intent describing the activity to be started.
	* @throws android.content.ActivityNotFoundException.
	*/
	public void startChildActivity(String Id, Intent intent)
		{
		Window window = getLocalActivityManager().startActivity(Id,intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		if (window != null)
			{
			if( !mIdList.contains(Id) )
				mIdList.add(Id);
			setContentView(window.getDecorView());
			}
		}
	
	public Activity getTopActivity()
		{
		LocalActivityManager manager = getLocalActivityManager();
		Activity act = manager.getActivity( mIdList.get( mIdList.size()-1 ) );
		return act;
		}
	
	public int getActivityCount()
		{
		return mIdList.size();
		}
	
	public boolean activityExists(String id)
		{
		boolean ret = false;
		int size = mIdList.size();
		
		for( int i = 0; i < size; i++ )
			{
			if( mIdList.get(i).contentEquals(id) )
				{
				ret = true;
				break;
				}
			}
		
		return ret;
		}
	
	/**
	* The primary purpose is to prevent systems before android.os.Build.VERSION_CODES.ECLAIR
	* from calling their default KeyEvent.KEYCODE_BACK during onKeyDown.
	*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
		{
		if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			//preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
			return true;
			}
		return super.onKeyDown(keyCode, event);
		}
	
	/**
	* Overrides the default implementation for KeyEvent.KEYCODE_BACK
	* so that all systems call onBackPressed().
	*/
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
		{
		if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			onBackPressed();
			return true;
			}
		return super.onKeyUp(keyCode, event);
		}
	
	/**
	* If a Child Activity handles KeyEvent.KEYCODE_BACK.
	* Simply override and add this method.
	*/
	@Override
	public void onBackPressed()
		{
		backPressed();
		}
	
	public boolean finishActivity()
		{
		int length = mIdList.size();
		if ( length > 1)
			{
			Activity current = getLocalActivityManager().getActivity(mIdList.get(length-1));
			current.finish();
			return true;
			}
		return false;
		}
	
	public void finishAll()
		{
		int length = mIdList.size();
		while ( length > 1 )
			{
			Activity current = getLocalActivityManager().getActivity(mIdList.get(length-1));
			current.finish();
			length--;
			}
		}
	
	public boolean beforeBack()
		{
		// Overrideable
		return false;
		}
	
	public void backPage(View v)
		{
		backPressed();
		}
	
	protected void backPressed()
		{
		if( !beforeBack() )
			{
			//Close the keyboard if it's open
			if( this.getCurrentFocus() != null ) 
	    		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			finishActivity();
			}
		}
	
	}
