package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

import com.twix_agent.R;

/*******************************************************************************************************************
 * Class: Twix_AgentTab2
 * 
 * Purpose: Highest level activity for Tab 2 (Open Tags). This allows the user to select from a list of open service
 * 			tags and view or edit their details. Open service tags can be marked for submission at this level.
 * 
 * Relevant XML: tab2.xml
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentTab2 extends Activity
	{
	private boolean readOnly;
	private Twix_Application app;
	private Twix_AgentTheme Twix_Theme;
	private Context mContext;
	Twix_SQLite db;
	
	private boolean desc = false;
	private String CurrentSearch = "openServiceTag.serviceTagId";
	private LinearLayout tl;
	private OnClickListener createSig, removeSig, submitForm;
	private Dialog submitDialog;
	private Dialog filterDialog;
	private ArrayAdapter<CharSequence> sigAdapter;
	
	private static final String NO_DISPATCH_OR_SERVICEADDRESS =
			"Manually entered data requires the following:\n\t\tJobNo, " +
			"BatchNo, SiteName, Address1\n\t\tCity, State, and Zip";
	
	private static final String NO_DISPATCH =
			"If a Dispatch is not linked,\n\t\ta BatchNo and JobNo must be entered";
	
	private static final String ERROR_ADMIN =
			"An Error has occured. Please Contact your local administrator.";
	
    public void onCreate(Bundle savedInstanceState)
    	{
        super.onCreate(savedInstanceState);
        mContext = getParent();
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.tab2, null);
		this.setContentView( viewToLoad );
        
		app = (Twix_Application) getApplication();
	    db = app.db;
	    Twix_Theme = app.Twix_Theme;
		
        tl = (LinearLayout) findViewById(R.id.TagBuild);
        tl.setBackgroundColor(Twix_Theme.lineColor);
        
        // Build the adapter before setting it in the click listener.
        buildAdapters();
        // Build the click listeners for signing and removing signatures
        createClickListeners();
        
    	Update_Page();
    	}
    
    private void buildAdapters()
    	{
    	sigAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.spinner_layout);
		sigAdapter.setDropDownViewResource(R.layout.spinner_popup);
		sigAdapter.add("");								//0
		sigAdapter.add("Customer Signature");			//1
		sigAdapter.add("Customer not available");		//2
		sigAdapter.add("Off site management");			//3
		sigAdapter.add("Confirmed via phone");			//4
		sigAdapter.add("Left voicemail for customer");	//5
		sigAdapter.add("Progress service tag");			//6
    	}
    
    private void createClickListeners()
    	{
    	createSig = new OnClickListener()
    		{
    		@Override
			public void onClick(View v)
				{
				final OpenTagRow param = (OpenTagRow) ((View)v.getParent()).getTag();
				
				//final String tagId = param.serviceTagId;//(String) ((View)(v.getParent())).getTag();
				List<String> errors = canCheck( param.serviceTagId );
				int size = errors.size();
				
				if( size < 1 )
					{
					submitDialog = new Dialog(mContext);
					submitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.open_tag_submit_1, null);
			    	
			    	// Fetch the radio buttons
			    	//final RadioButton tagOnly = (RadioButton) viewToLoad.findViewById(R.id.radio_TagSingle);
			    	final RadioButton tagAll = (RadioButton) viewToLoad.findViewById(R.id.radio_TagAll);

			    	// Setup the next button
			    	Button Next = (Button)viewToLoad.findViewById(R.id.Submit);
			    	Next.setOnClickListener(new OnClickListener()
			    		{
						@Override
						public void onClick(View v)
							{
							submitDialog.dismiss();
							submitDialog = new SignatureDialog(mContext);
							submitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.open_tag_submit_2, null);
					    	
					    	final List<Integer> tagIdList = new ArrayList<Integer>();
					    	tagIdList.add(param.serviceTagId);
					    	
					    	if( tagAll.isChecked() )
					    		{
					    		tagIdList.addAll( getAllTags(param.serviceTagId) );
					    		}
					    	
					    	((SignatureDialog)submitDialog).tagIdList = tagIdList;
					    	
					    	// Add the tags to the layout
					    	LinearLayout tagList = (LinearLayout) viewToLoad.findViewById(R.id.TagHolder);
					    	
					    	Twix_AgentTagList tags;
					    	List<LinearLayout> tagLL;
					    	if( param.dispatchId > 0 )
					    		{
					    		tags = new Twix_AgentTagList(app, mContext, param.dispatchId, param.serviceTagId, !tagAll.isChecked());
						    	tagLL = tags.generate2();
					    		}
					    	else
					    		{
						    	tags = new Twix_AgentTagList(app, mContext, tagIdList, false);
						    	tagLL = tags.generate();
					    		}
					    	
					    	int size = tagLL.size();
					    	for( int i = 0; i < size; i++ )
					    		{
					    		tagList.addView(tagLL.get(i));
					    		}
					    	
					    	
					    	Button addEmail = (Button) viewToLoad.findViewById(R.id.AddEmail);
					    	addEmail.setOnClickListener(new OnClickListener()
					    		{
								@Override
								public void onClick(View v)
									{
									LinearLayout emailList = (LinearLayout) submitDialog.findViewById(R.id.EmailTo);
									int size = emailList.getChildCount();
									
									// Add all the emails to a list so we don't repeat them.
									String emailToList = "";
									for( int i = 0; i < size; i++ )
										{
										emailToList += ((TextView)((LinearLayout)emailList.getChildAt(i)).findViewById(R.id.Item_Text)).getText();
										if( i < size -1 )
											{
											emailToList += "', '";
											}
										}
									
									emailDialog(param.serviceTagId, emailToList);
									}
					    		});
					    	Button Atta = (Button) viewToLoad.findViewById(R.id.Attachment);
					    	Atta.setOnClickListener(new OnClickListener()
					    		{
								@Override
								public void onClick(View v)
									{
									//inflateFilter();
									//EditText ett = (EditText) findViewById(R.id.T1);
									int p = param.serviceTagId;
									String q = param.jobNo;
									String r = param.description;
									
									Intent i = new Intent(Twix_AgentTab2.this, Twix_AgentEmailAttachments.class);        
									 i.putExtra("font",p);
									 i.putExtra("job",q);
									 i.putExtra("desc",r);
									 startActivity(i);
									//Toast.makeText(app.getBaseContext(),p, Toast.LENGTH_SHORT).show();
									//Intent addattch = new Intent(Twix_AgentTab2.this, Twix_AgentEmailAttachments.class);
									
									//Twix_AgentTab2.this.startActivity(addattch);
									
					    		}});
					    	Button Submit = (Button) viewToLoad.findViewById(R.id.Submit);
					    	Submit.setOnClickListener(new OnClickListener()
					    		{
								@Override
								public void onClick(View v)
									{
									// Fetch all the check values before submission
									Spinner sigSpinner = (Spinner) submitDialog.findViewById(R.id.NoSignatureReason);
									LinearLayout emailList = (LinearLayout) submitDialog.findViewById(R.id.EmailTo);
									int size = emailList.getChildCount();
									
									if( sigSpinner.getSelectedItemPosition() == 0 )
										{
										AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
										builder.setMessage("You must select a Signature or Reason before submitting.")
												.setCancelable(true)
												.setNeutralButton("Ok", new DialogInterface.OnClickListener()
													{
													public void onClick(DialogInterface dialog, int id)
														{
														dialog.dismiss();
														}
													});
										AlertDialog alert = builder.create();
										alert.show();
										}
									else if( param.callComplete && (size < 1) )
										{
										AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
										builder.setMessage("Call Complete Tags must have at least one email recipient.")
												.setCancelable(true)
												.setNeutralButton("Ok", new DialogInterface.OnClickListener()
													{
													public void onClick(DialogInterface dialog, int id)
														{
														dialog.dismiss();
														}
													});
										AlertDialog alert = builder.create();
										alert.show();
										}
									else if( (sigSpinner.getSelectedItemPosition() != 6) && (size <= 0) )
										{
										AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
										builder.setMessage("You have no email recipients. Are you sure you want to submit this tag?")
												.setCancelable(false)
												.setPositiveButton("Yes", new DialogInterface.OnClickListener()
													{
													public void onClick(DialogInterface dialog, int id)
														{
														submitTag();
														}
													})
												.setNegativeButton("No", new DialogInterface.OnClickListener()
													{
													public void onClick(DialogInterface dialog, int id)
														{
														dialog.dismiss();
														}
													});
										AlertDialog alert = builder.create();
										alert.show();
										}
									else
										{
										submitTag();
										}
									
									}
					    		});
					    	
					    	Button Cancel = (Button) viewToLoad.findViewById(R.id.Cancel);
					    	Cancel.setOnClickListener(new OnClickListener()
					    		{
								@Override
								public void onClick(View v)
									{
									submitDialog.dismiss();
									}
					    		});
					    	
					    	Spinner SignatureReason = (Spinner) viewToLoad.findViewById(R.id.NoSignatureReason);
							SignatureReason.setAdapter(sigAdapter);
							
							SignatureReason.setOnItemSelectedListener(new OnItemSelectedListener()
								{
								@Override
								public void onItemSelected(AdapterView<?> adapterViewParent,
										View clickedView, int itemIndex, long rowId)
									{
									if( itemIndex == 1 )
										{
										sigPopup();
										}
									else
										{
										submitDialog.findViewById(R.id.SignatureHolder).setVisibility(View.INVISIBLE);
										}
									}

								@Override 
								public void onNothingSelected(AdapterView<?> arg0)
									{
									// Do nothing.
									}
								});
							
					    	submitDialog.setContentView(viewToLoad);
					    	submitDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					    	submitDialog.show();
							}
			    		});
			    	
			    	Button Cancel = (Button)viewToLoad.findViewById(R.id.Cancel);
			    	Cancel.setOnClickListener(new OnClickListener()
			    		{
						@Override
						public void onClick(View v)
							{
							submitDialog.dismiss();
							}
			    		});
			    	
			    	submitDialog.setContentView(viewToLoad);
			    	submitDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			    	submitDialog.show();
					}
				else
					{
					String error = "";
					for( int i = 0; i < size; i++ )
						error += "\t-\t" + errors.get(i) + "\n";
					// Alert
					AlertDialog alert = new AlertDialog.Builder(mContext).create();
		        	alert.setTitle("Cannot Submit Tag");
		        	alert.setMessage("Your tag has the following errors:\n\n" + error);
		        	alert.setButton("Ok", new DialogInterface.OnClickListener()
		        		{  
		        		public void onClick(DialogInterface dialog, int which)
		        			{
		        			return;  
		        			}
		        		});
		        	alert.show();
					}
				
				}
    		};
    	
    	removeSig = new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				final OpenTagRow data = (OpenTagRow) ((View)(v.getParent())).getTag();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage("Are you sure you want to remove the customer signature and email list?")
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								removeSubmission( data.serviceTagId );
								}
							})
						.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
							public void onClick(DialogInterface dialog, int id)
								{
								dialog.dismiss();
								}
							});
				AlertDialog alert = builder.create();
				alert.show();
				
				//
				}
    		};
    	;
    	
    	submitForm = new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				// Start Sync
				synchronized(v)
					{
					Button b = (Button) v;
					FormData data = (FormData) v.getTag();
					ContentValues cv = new ContentValues();
					
					if( data.completed )
						{
						b.setText("Submit Form");
						cv.put("Completed", "N");
						}
					else
						{
						b.setText("Remove Submission");
						cv.put("Completed", "M");
						}
					
					data.completed = !data.completed;
					db.update("FormData", cv, "FormDataId", data.FormDataId);
					}
				// End Sync
				}
			}
    	;
    	
    	LinearLayout newTag = (LinearLayout) findViewById(R.id.NewOpenTag);
    	newTag.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				Twix_AgentActivityGroup2 parent = (Twix_AgentActivityGroup2) Twix_AgentTab2.this.getParent();
				parent.newOpenTag();
				}
    		})
    	;
    	
    	LinearLayout newForm = (LinearLayout) findViewById(R.id.NewForm);
    	newForm.setOnClickListener(new OnClickListener()
    		{
			@Override
			public void onClick(View v)
				{
				Twix_StepList.ActivityCallback callback = new Twix_StepList.ActivityCallback()
					{
					@Override
					public void Result(Map<String, String> results)
						{
						int dispatchId = Integer.parseInt(results.get("dispatchId"));
						int equipmentId = Integer.parseInt(results.get("equipmentId"));
						int formId = Integer.parseInt(results.get("FormId"));
						
						Twix_AgentFormDisplay.ActivityCallback callback = new Twix_AgentFormDisplay.ActivityCallback()
							{
							@Override
							public void Refresh()
								{
								readSQL();
								}
							}
						;
						
						Map<String, String> AttrFindIds = new HashMap<String, String>();
						AttrFindIds.put("equipmentId", equipmentId+"");
						AttrFindIds.put("dispatchId", dispatchId+"");
						
						//Twix_AgentFormDisplay d = new Twix_AgentFormDisplay(formId, AttrFindIds, 0L,
						//		"Dispatch", dispatchId, "Equipment", equipmentId, 
						//		mContext, app, callback);
						Twix_AgentFormDisplay d = new Twix_AgentFormDisplay();
						d.Setup(formId, AttrFindIds, 0L,
								"Dispatch", dispatchId, "Equipment", equipmentId, 
								mContext, app, callback, false);
						
						d.show(((Activity)mContext).getFragmentManager(), "FormDisplay");
						}
					}
				;
				
				float[][] weights = new float[][]{{1f, 1f, 1f, 1f, 1f}, {1f, 1f}, {1f, 1f}};
				String[] SQLStatements = new String[3];
				String[] SQLIds = new String[3];
				String[][] ColumnHeaders = new String[][]{
							{"JobNo", "Batch No", "Site Name", "Address 1", "Address 2"},
							{"UnitNo", "Equipment Category"},
							{"Type", "Form Description"}};
				
				SQLStatements[0] = "SELECT d.dispatchId, d.JobNo, d.BatchNo, sa.siteName, sa.address1, sa.address2 " +
							"FROM dispatch as d " +
								"LEFT OUTER JOIN serviceAddress AS sa " +
									"ON sa.serviceAddressId = d.serviceAddressId";
				SQLIds[0] = "dispatchId";
				
				SQLStatements[1] = "SELECT e.equipmentId, e.unitNo, ec.categoryDesc " +
						"FROM equipment as e " +
							"LEFT OUTER JOIN equipmentCategory AS ec " +
								"ON ec.equipmentCategoryId = e.equipmentCategoryId " +
						"WHERE e.serviceAddressId = (SELECT serviceAddressId FROM dispatch WHERE dispatchId = ?)";
				SQLIds[1] = "equipmentId";
				
				SQLStatements[2] = "SELECT f.FormId, ft.Description as Type, f.Description " +
						"FROM Form as f " +
							"LEFT OUTER JOIN FormType AS ft " +
								"ON ft.FormType = f.Type " +
						"WHERE f.EquipmentCategoryId = (SELECT equipmentCategoryId FROM equipment WHERE equipmentId = ?)";
				SQLIds[2] = "FormId";
				
				Twix_StepList StepList = new Twix_StepList(mContext);
				StepList.Setup(app, SQLStatements, SQLIds, ColumnHeaders, weights, null, callback);
				StepList.show();
				}
    		})
    	;
    	}
    
    
    public void inflateFilter()
    	{
    	filterDialog = new Dialog(mContext);
    	filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.dispatch_filter, null);
    	
    	final LinearLayout ll_Mech1 = (LinearLayout) viewToLoad.findViewById(R.id.ListView_Mechanic1);
    	final LinearLayout ll_Mech2 = (LinearLayout) viewToLoad.findViewById(R.id.ListView_Mechanic2);
    	
    	final Button bn_Apply = (Button) viewToLoad.findViewById(R.id.Button_Apply);
    	final Button bn_Cancel = (Button) viewToLoad.findViewById(R.id.Button_Cancel);
    	final Button bn_Mech1 = (Button) viewToLoad.findViewById(R.id.Button_AddMechanic1);
    	final Button bn_Mech2 = (Button) viewToLoad.findViewById(R.id.Button_AddMechanic2);
    	final Button bn_ALL = (Button) viewToLoad.findViewById(R.id.Button_AllCall);
    	
    	final CheckBox ck_NoTags = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_NoTags);
    	final CheckBox ck_MustReturn = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_MustReturn);
    	final CheckBox ck_CallComplete = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_CallComplete);
    	
    	final CheckBox ck_Mech1 = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_Mechanic1);
    	final CheckBox ck_Mech2 = (CheckBox) viewToLoad.findViewById(R.id.CheckBox_Mechanic2);
    	
    	}
    
    /**
     * Creates the Signature popup and populates the submitDialog with the signature bitmap if saved.
     */
    private void sigPopup()
		{
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.signature_popup, null);
    	final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		final Panel p = new Panel(mContext, ((SignatureDialog)submitDialog).signature);
		((LinearLayout)viewToLoad.findViewById(R.id.SignatureHolder)).addView(p);
		
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialog.setContentView(viewToLoad);
		dialog.show();
		
		((Button)dialog.findViewById(R.id.Save)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				((SignatureDialog)submitDialog).signature = p.mBitmap;
				ImageView iv = (ImageView)submitDialog.findViewById(R.id.SignatureHolder);
				Bitmap tempMap = Bitmap.createScaledBitmap(p.mBitmap, (p.mBitmap.getWidth()/2), (p.mBitmap.getHeight()/2), false);
				iv.setImageBitmap(tempMap);
				dialog.dismiss();
				submitDialog.findViewById(R.id.SignatureHolder).setVisibility(View.VISIBLE);
				}
			});
		
		((Button)dialog.findViewById(R.id.Cancel)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				if( submitDialog != null )
					{
					Spinner sigSpinner = (Spinner) submitDialog.findViewById(R.id.NoSignatureReason);
					if( sigSpinner != null )
						sigSpinner.setSelection(0);
					}
				
				dialog.dismiss();
				}
			});
		
		((Button)dialog.findViewById(R.id.ClearSig)).setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v2)
				{
				p.clear();
				}
			});
		}
    
    /**
     * Handles setting the submission status on a service tag, as well as creates the grouping for the tag
     */
    private void submitTag()
    	{
    	ContentValues values = new ContentValues();
    	/**
    	 * Mark the open service tag for submission
    	 */
    	// Collect the list of tagIds. The first record is always the open tag being submitted
    	List<Integer> tagIdList = ((SignatureDialog)submitDialog).tagIdList;
    	values.put("completed", "Y");
		app.db.update("openServiceTag", values, "serviceTagId", tagIdList.get(0) );
		values.clear();
		
    	/**
    	 * Collect the service tag group information
    	 */
    	Spinner sigSpinner = (Spinner) submitDialog.findViewById(R.id.NoSignatureReason);
		LinearLayout emailList = (LinearLayout) submitDialog.findViewById(R.id.EmailTo);
		int size = emailList.getChildCount();
		
    	String emailTo = "";
		for( int i = 0; i < size; i++ )
			{
			emailTo += ((TextView)((LinearLayout)emailList.getChildAt(i)).findViewById(R.id.Item_Text)).getText();
			if( i < size -1 )
				{
				emailTo += ", ";
				}
			}
		
		/********************************************
		 *  Insert the service tag group record
		 ********************************************/
		int groupId = app.db.newNegativeId("serviceTagGroup", "groupId");
		values.put("groupId", groupId);
		
		// Deal with the signature/noSignatureReason
		if( (sigSpinner).getSelectedItemPosition() == 1 )
			{
			// Build the signature data for the database
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			((SignatureDialog)submitDialog).signature.compress(CompressFormat.JPEG, 100, bos); 
			byte[] bytes = bos.toByteArray();
			
			values.put("signature", bytes);
			values.put("noSignatureReason", "");
			}
		else
			{
			// Put null if there is a no signature reason
			values.putNull("signature");
			values.put("noSignatureReason", (String) sigSpinner.getSelectedItem() );
			}
		
		// Generate a current date for submission
		values.put("dateCreated", Twix_TextFunctions.getCurrentDate(Twix_TextFunctions.DB_FORMAT) );
		values.put("emailTo", emailTo);
		app.db.db.insertOrThrow("serviceTagGroup", null, values);
		values.clear();
		
		/********************************************
		 *  Add the service tags to the group
		 ********************************************/
		size = tagIdList.size();
		for( int i = 0; i < size; i++ )
			{
			values.put("groupId", groupId);
			values.put("serviceTagId", tagIdList.get(i) );
			app.db.db.insertOrThrow("serviceTagGroupXref", null, values);
			values.clear();
			}
		
		submitDialog.dismiss();
		readSQL();
    	}
    
    private void removeSubmission(int serviceTagId)
    	{
    	String sqlQ = "select groupId " +
    			"from serviceTagGroupXref " +
    				"WHERE serviceTagId = " + serviceTagId;
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			{
			db.delete("serviceTagGroupXref", "groupId", cursor.getInt(0));
			db.delete("serviceTagGroup", "groupId", cursor.getInt(0));
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		ContentValues cv = new ContentValues();
		cv.put("completed", "N");
		app.db.update("openServiceTag", cv, "serviceTagId", serviceTagId);
		readSQL();
    	}
    
    private List<Integer> getAllTags(int serviceTagId)
	    {
	    List<Integer> ret = new ArrayList<Integer>();
	    
	    String sqlQ = "select dispatchId " +
				"from openServiceTag " + 
					"WHERE serviceTagId = '" + serviceTagId + "'";
		Cursor cursor = db.rawQuery(sqlQ);
		
		String dispatchId = null;
		if (cursor.moveToFirst())
			{
			dispatchId = cursor.getString(0);
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
	    
		if( dispatchId != null )
			{
			if( !dispatchId.contentEquals("0") )
				{
				sqlQ = "select serviceTagId " +
						"from serviceTag " + 
							"WHERE dispatchId = '" + dispatchId + "'";
				cursor = db.rawQuery(sqlQ);
				
				if (cursor.moveToFirst())
					{
					do
						{
						ret.add( cursor.getInt(0) );
						}
					while( cursor.moveToNext());
					}
				if (cursor != null && !cursor.isClosed())
					{
					cursor.close();
					}
				}
			}
	    return ret;
	    }
    
    private void emailDialog(int serviceTagId, String currentEmailTo)
    	{
    	final Dialog emailTo = new Dialog(mContext);
		emailTo.setTitle("Select a email address");
		View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.email_selector, null);
		
		LinearLayout ll = (LinearLayout) viewToLoad.findViewById(R.id.main);
		
		String sqlQ = "SELECT SC.contactName, SC.email " +
				"from openServiceTag as ST " + 
				"LEFT OUTER JOIN serviceAddressContact as SC " + 
					"on SC.serviceAddressId = ST.serviceAddressId " +
				"WHERE ST.serviceTagId = " + serviceTagId + " " +
						"AND SC.email IS NOT NULL " +
						"AND SC.email != '' " +
						"AND SC.email NOT IN ('" + currentEmailTo + "') " +
				"ORDER BY SC.contactName";
		Cursor cursor = db.rawQuery(sqlQ);
		
		View row;
		
		if( (app.techEmail != null) && (app.techEmail.length() > 0) )
			{
			row = LayoutInflater.from(mContext).inflate(R.layout.dropdown_item, null);
			((TextView)row.findViewById(R.id.text1)).setText(app.techName);
			row.setTag(app.techEmail);
			((TextView)row.findViewById(R.id.text2)).setText(app.techEmail);
			row.setOnClickListener(new OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					if( submitDialog != null )
						{
						if( submitDialog.isShowing() )
							{
							addEmail( (String) v.getTag() );
							emailTo.dismiss();
							}
						}
					emailTo.dismiss();
					}
				});
			
			ll.addView(row);
			}
		
		if (cursor.moveToFirst())
			{
			do
				{
				row = LayoutInflater.from(mContext).inflate(R.layout.dropdown_item, null);
				((TextView)row.findViewById(R.id.text1)).setText(cursor.getString(0));
				String email = Twix_TextFunctions.clean(cursor.getString(1));
				row.setTag(email);
				((TextView)row.findViewById(R.id.text2)).setText(email);
				row.setOnClickListener(new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						if( submitDialog != null )
							{
							if( submitDialog.isShowing() )
								{
								addEmail( (String) v.getTag() );
								emailTo.dismiss();
								}
							}
						emailTo.dismiss();
						}
					});
				
				ll.addView(row);
				}
		    while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		row = LayoutInflater.from(mContext).inflate(R.layout.dropdown_item, null);
		((TextView)row.findViewById(R.id.text1)).setText("Other email...");
		((TextView)row.findViewById(R.id.Separator)).setText("");
		
		row.setOnClickListener(new OnClickListener()
			{
			@Override
			public void onClick(View v)
				{
				emailTo.dismiss();
				if( submitDialog != null )
					{
					if( submitDialog.isShowing() )
						{
						final Dialog dialog = new Dialog(mContext);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				    	View viewToLoad = LayoutInflater.from(mContext).inflate(R.layout.open_tag_manual_email, null);
				    	
				    	Button Submit = (Button) viewToLoad.findViewById(R.id.Submit);
				    	Submit.setOnClickListener(new OnClickListener()
							{
							@Override
							public void onClick(View v)
								{
								if( submitDialog != null )
									{
									if( submitDialog.isShowing() )
										{
										String email = ((EditText)dialog.findViewById(R.id.EmailTo)).getText().toString();
										//				  "^([a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\.[a-zA-Z]{2,}(,\s*)*)+$"
										// CHANGE: Allows emails with multiple sub domains to be entered. 1.0.9
										if( email.matches("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\@([a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\.[a-zA-Z]{2,})$") )
														//"^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\@([a-zA-Z0-9_-]+\\.[a-zA-Z]{2,})$"
											{
											addEmail( email );
											dialog.dismiss();
											}
										else
											{
											AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
											builder.setMessage("The email you provided is not formed properly. Please format your email like email@site.com")
													.setCancelable(true)
													.setNeutralButton("Ok", new DialogInterface.OnClickListener()
														{
														public void onClick(DialogInterface dialog, int id)
															{
															dialog.dismiss();
															}
														});
											AlertDialog alert = builder.create();
											alert.show();
											}
										}
									}
								}
							});
				    	
				    	Button Cancel = (Button) viewToLoad.findViewById(R.id.Cancel);
				    	Cancel.setOnClickListener(new OnClickListener()
							{
							@Override
							public void onClick(View v)
								{
								dialog.dismiss();
								}
							});
				    	
				    	dialog.setContentView(viewToLoad);
				    	dialog.show();
						}
					else
						emailTo.dismiss();
					}
				else
					emailTo.dismiss();
				
				}
			});
		ll.addView(row);
		
		//submitDialog
		emailTo.setContentView(viewToLoad);
		emailTo.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		emailTo.show();
    	}
    
    private void addEmail(String emailText)
    	{
    	LinearLayout ll = (LinearLayout) submitDialog.findViewById(R.id.EmailTo);
		View emailRec = LayoutInflater.from(mContext).inflate(R.layout.dispatch_mechanic_item, null);
		((TextView)emailRec.findViewById(R.id.Item_Text)).setText( emailText );
		((ImageButton)emailRec.findViewById(R.id.Item_Delete)).setOnClickListener(
				new OnClickListener()
					{
					@Override
					public void onClick(View v)
						{
						View row = (View) v.getParent();
						LinearLayout host = (LinearLayout) row.getParent();
						host.removeView(row);
						}
					});
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(3, 3, 3, 3);
		emailRec.setLayoutParams(params);
		
		ll.addView(emailRec);
    	}
    
    public void readSQL()
	    {
	    readOnly = app.prefs.getBoolean("reqUpdate", true) || app.prefs.getBoolean("data_dirty", true);
	    if( readOnly )
	    	{
	    	findViewById(R.id.NewOpenTag).setVisibility(View.GONE);
	    	findViewById(R.id.NewForm).setVisibility(View.GONE);
	    	}
	    else
	    	{
	    	findViewById(R.id.NewOpenTag).setVisibility(View.VISIBLE);
	    	// Uncomment for Dispatch Level Forms
	    	//findViewById(R.id.NewForm).setVisibility(View.VISIBLE);
	    	}
	    
    	tl.removeAllViews();
    	String sqlQ = "SELECT openServiceTag.dispatchId, openServiceTag.serviceTagId, " +
    					"CASE WHEN openServiceTag.serviceAddressId = '0' THEN ( " +
    					"CASE WHEN openServiceTag.dispatchId = '0' THEN (openServiceTag.siteName) ELSE ( " +
    						"select serviceAddress.siteName from serviceAddress " +
    							"where serviceAddress.serviceAddressId = dispatch.serviceAddressId) END " +
    																		") ELSE (serviceAddress.siteName) END AS siteName, " +
    					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.tenant ) ELSE ( dispatch.tenant ) END AS tenant, " +
    					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.batchNo ) ELSE ( dispatch.batchNo ) END AS batchNo, " +
    					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( openServiceTag.jobNo ) ELSE ( substr(dispatch.jobNo, 5) ) END AS jobNo, " +
    					"CASE WHEN openServiceTag.dispatchId = '0' THEN ( '' ) ELSE ( dispatch.description ) END AS description, " +
    					"openServiceTag.completed, openServiceTag.disposition, openServiceTag.xoi_flag " + 
    					"FROM openServiceTag " +
	    					"LEFT OUTER JOIN dispatch " +
	    						"ON openServiceTag.dispatchId = dispatch.dispatchId " +
	    					"LEFT OUTER JOIN serviceAddress " +
	    						"ON serviceAddress.serviceAddressId = openServiceTag.serviceAddressId " +
    					"ORDER BY " + CurrentSearch;
    	
    	if( !desc )
    		sqlQ += " asc";
    	else
    		sqlQ += " desc";
    	
    	Cursor cursor = db.rawQuery(sqlQ);
    	OpenTagRow row;
    	int index;
		if (cursor.moveToFirst())
			{
			do
				{
				index = 0;
				
				row = new OpenTagRow();
				row.dispatchId		= cursor.getInt(0);
				row.serviceTagId	= cursor.getInt(1);
				row.siteName		= Twix_TextFunctions.clean(cursor.getString(2));
				row.tenant			= Twix_TextFunctions.clean(cursor.getString(3));
				row.batchNo			= Twix_TextFunctions.clean(cursor.getString(4));
				row.jobNo			= Twix_TextFunctions.clean(cursor.getString(5));
				row.description		= Twix_TextFunctions.clean(cursor.getString(6));
				row.completed		= Twix_TextFunctions.clean(cursor.getString(7)).contentEquals("Y");
				row.callComplete	= Twix_TextFunctions.clean(cursor.getString(8)).contentEquals("C");
				row.xoi_flag		= Twix_TextFunctions.clean(cursor.getString(9));
				createRow(row);
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		
		sqlQ = "SELECT fd.FormDataId, fd.FormId, fd.ParentId, fd.LinkId, sa.siteName, " +
					"d.tenant, d.batchNo, substr(d.jobNo, 5) as jobNo, d.description, fd.completed " +
				"FROM FormData as fd " +
					"LEFT OUTER JOIN dispatch as d " +
						"ON d.dispatchId = fd.ParentId " +
					"LEFT OUTER JOIN serviceAddress as sa " +
						"ON sa.serviceAddressId = d.serviceAddressId " +
				"WHERE ParentTable = 'Dispatch' AND InputByEmpno = '" + app.empno + "' AND Completed != 'Y'";
		cursor = db.rawQuery(sqlQ);
		FormData fRow;
		while( cursor.moveToNext() )
			{
			index = 0;
			
			fRow = new FormData();
			fRow.FormDataId		= cursor.getLong(0);
			fRow.FormId			= cursor.getInt(1);
			fRow.dispatchId		= cursor.getInt(2);
			fRow.equipmentId	= cursor.getInt(3);
			fRow.siteName		= cursor.getString(4);
			fRow.tenant			= cursor.getString(5);
			fRow.batchNo		= cursor.getString(6);
			fRow.jobNo			= cursor.getString(7);
			fRow.description	= cursor.getString(8);
			fRow.completed		= Twix_TextFunctions.clean(cursor.getString(9)).contentEquals("M");
			
			createRow(fRow);
			}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		if( tl.getChildCount() <= 0 )
			noResults();
	    }
    
    // Row Creation
    public class OpenTagRow
    	{
    	int dispatchId = 0;
    	int serviceTagId;
    	String siteName;
    	String tenant;
    	String batchNo;
    	String jobNo;
    	String description;
    	boolean completed = false;
    	boolean callComplete = false;
    	String xoi_flag;
    	}
    public void createRow(OpenTagRow data)
	    {
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	LinearLayout row = new LinearLayout(mContext);
    	row.setOrientation(LinearLayout.HORIZONTAL);
    	row.setLayoutParams(params);
    	row.setTag(data);
    	
    	//float weight[] = { 0.45f, 1f, 0.6f, 0.6f, 0.4f, 1f/*, 0.6f*/ };
    	String s;
    	if( data.serviceTagId > 0 )
    		s = data.serviceTagId + "";
    	else
	    	s = "New Tag";
    	row.addView(createTextView( s,					0.45f,	true ));
    	row.addView(createTextView( data.siteName,		1f,		false ));
    	row.addView(createTextView( data.tenant,		0.6f,	false ));
    	row.addView(createTextView( data.batchNo,		0.6f,	false ));
    	row.addView(createTextView( data.jobNo,			0.4f,	false ));
    	row.addView(createTextView( data.description,	1f,		false ));
    	
    	// Build the Submit/Remove Signature button
    	Button b = new Button(mContext);
    	params = new LayoutParams();
    	params.height = LayoutParams.MATCH_PARENT;
    	params.width = 0;
    	params.weight = 0.6f;
    	params.setMargins(2, 2, 2, 2);
    	b.setLayoutParams(params);
    	b.setBackgroundResource(R.drawable.button_bg);
    	b.setTextColor(Twix_Theme.headerText);
    	b.setTextSize(Twix_Theme.headerSize);
    	b.setPadding(10, 0, 10, 0);
    	
    	if( !data.completed )
    		{
    		b.setText("Sign Tag");
        	b.setOnClickListener(createSig);
    		}
    	else
    		{
    		b.setText("Remove Signature");
    		b.setOnClickListener(removeSig);
    		}
    	row.addView(b);
    	
    	//Create the row OnClick
    	row.setClickable(true);
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	Intent intent = new Intent(getParent(), Twix_AgentOpenTagsTabHost.class);
            	OpenTagRow data = (OpenTagRow)v.getTag();
            	intent.putExtra("serviceTagId", data.serviceTagId);
            	((Twix_TabActivityGroup)mContext).startChildActivity("Twix_AgentOpenTagsTabHost", intent);
                }
        	});
        
        row.setBackgroundResource(R.drawable.clickable_bg);
    	
		tl.addView(row);
	    }
    
    private class FormData
		{
		long FormDataId;
		int FormId;
		int dispatchId;
		int equipmentId;
		
		String siteName;
		String tenant;
		String batchNo;
		String jobNo;
		String description;
		
		boolean completed;
		}
    public void createRow(FormData data)
    	{
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	LinearLayout row = new LinearLayout(mContext);
    	row.setOrientation(LinearLayout.HORIZONTAL);
    	row.setLayoutParams(params);
    	row.setTag(data);
    	
    	String s;
    	if( data.FormDataId > 0 )
    		s = data.FormDataId + "";
    	else
	    	s = "New Tag";
    	row.addView(createTextView( s,					0.45f,	true ));
    	row.addView(createTextView( data.siteName,		1f,		false ));
    	row.addView(createTextView( data.tenant,		0.6f,	false ));
    	row.addView(createTextView( data.batchNo,		0.6f,	false ));
    	row.addView(createTextView( data.jobNo,			0.4f,	false ));
    	row.addView(createTextView( data.description,	1f,		false ));
    	
    	// Build the Submit/Remove Signature button
    	Button b = new Button(mContext);
    	params = new LayoutParams();
    	params.height = LayoutParams.MATCH_PARENT;
    	params.width = 0;
    	params.weight = 0.6f;
    	params.setMargins(2, 2, 2, 2);
    	b.setLayoutParams(params);
    	b.setBackgroundResource(R.drawable.button_bg);
    	b.setTextColor(Twix_Theme.headerText);
    	b.setTextSize(Twix_Theme.headerSize);
    	b.setPadding(10, 0, 10, 0);
    	
    	if( !data.completed )
    		b.setText("Submit Form");
    	else
    		b.setText("Remove Submission");
    	
    	b.setTag(data);
    	b.setOnClickListener(submitForm);
    	row.addView(b);
    	
    	//Create the row OnClick
        row.setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	FormData data = (FormData)v.getTag();
            	
				Twix_AgentFormDisplay.ActivityCallback callback = new Twix_AgentFormDisplay.ActivityCallback()
					{
					@Override
					public void Refresh()
						{
						readSQL();
						}
					}
				;
				
				Map<String, String> AttrFindIds = new HashMap<String, String>();
				AttrFindIds.put("equipmentId", data.equipmentId+"");
				AttrFindIds.put("dispatchId", data.dispatchId+"");
				
				Twix_AgentFormDisplay d = new Twix_AgentFormDisplay();
				d.Setup(data.FormId, AttrFindIds, data.FormDataId,
						"Dispatch", data.dispatchId, "Equipment", data.equipmentId, 
						mContext, app, callback, false);
				
				d.show(((Activity)mContext).getFragmentManager(), "FormDisplay");
                }
        	});
        
        row.setBackgroundResource(R.drawable.clickable_bg);
    	
		tl.addView(row);
    	}
    
    private TextView createTextView(String text, float weight, boolean firstCol)
    	{
    	LinearLayout.LayoutParams params = new LayoutParams();
    	params.height = LayoutParams.FILL_PARENT;
    	params.width = 0;
    	params.setMargins(2, 2, 2, 2);
    	params.weight = weight;
    	
    	TextView tv = new TextView(mContext);
    	tv.setLayoutParams(params);
    	tv.setText(text);
    	tv.setTextSize(Twix_Theme.headerSize);
    	tv.setTextColor(Twix_Theme.headerValue);
		tv.setGravity(Gravity.CENTER_VERTICAL);
		tv.setTypeface(Typeface.MONOSPACE);
		tv.setPadding(10, 10, 10, 10);
		if( firstCol)
			tv.setBackgroundColor(Twix_Theme.sortAsc);
		else
			tv.setBackgroundColor(Twix_Theme.headerBG);
		
    	return tv;
    	}
    
    // Submission Validation
    private List<String> canCheck(int tagId)
    	{
    	List<String> error = new ArrayList<String>();
    	String ret;
    	String sqlQ = "SELECT dispatchId, serviceAddressId, jobNo, batchNo, " +
				"siteName, address1, city, state, zip, " +
				"disposition, xoi_flag " +
			"FROM openServiceTag " +
			"where serviceTagId = " + tagId;
		Cursor cursor = db.rawQuery(sqlQ);
		
		
		int dispatchId, serviceAddressId;
		String jobNo, batchNo,
			siteName, address1, city, state, zip, disposition, xoi_flag;
		
		if (cursor.moveToFirst())
			{
			dispatchId			= cursor.getInt(0);
			serviceAddressId	= cursor.getInt(1);
			jobNo				= cursor.getString(2);
			batchNo				= cursor.getString(3);
			siteName			= cursor.getString(4);
			address1			= cursor.getString(5);
			city				= cursor.getString(6);
			state				= cursor.getString(7);
			zip					= cursor.getString(8);
			disposition			= cursor.getString(9);
			xoi_flag			= cursor.getString(10);
			}
		else
			{
			error.add(ERROR_ADMIN);
			return error;
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
		
		// Set Linkage Boolean
		ret = checkLinkage( dispatchId, serviceAddressId, jobNo, batchNo,
				siteName, address1, city, state, zip );
		if( ret.length() > 0 )
			error.add(ret);
		
		// Ensure a disposition has been chosen for the tag
		if( disposition == null )
			error.add("No Disposition selected for the service tag");
		else if( disposition.length() < 1 )
			error.add("No Disposition selected for the service tag");
		
		/*if(xoi_flag == null)
			error.add("No Xoi option selected");
		else if(xoi_flag == "N")
			error.add("You have choosen to not create xoi link");
		*/
		
		ret = safetyComplete(tagId);
		if( ret.length() > 0 )
			error.add(ret);
		
		ret = checkForms(tagId);
		if( ret.length() > 0 )
			error.add(ret);
		
		/*
		ret = checkPMChecklist(serviceType, tagId);
		if( ret.length() > 0 )
			error.add(ret);
		*/
		ret = checkBlues(tagId);
		if( ret.length() > 0 )
			error.add(ret);
		
    	return error;
    	}
    
    private String checkLinkage( int dispatchId, int serviceAddressId, String jobNo, String batchNo,
    		String siteName, String address1, String city, String state, String zip )
    	{
    	String error = "";
    	
		// Check linkage so we know we will have a jobNo and batchNo or site
		if( (dispatchId == 0) && (serviceAddressId == 0) )
			{
			if( jobNo == null || batchNo == null || siteName == null || address1 == null
					|| city == null || state == null || zip == null )
				return NO_DISPATCH_OR_SERVICEADDRESS;
			else if( (jobNo.length() < 1) || (batchNo.length() < 1) || (siteName.length() < 1) || (address1.length() < 1)
					&& (city.length() < 1) || (state.length() < 1) || (zip.length() < 1) )
				return NO_DISPATCH_OR_SERVICEADDRESS;
			}
		
		else if( dispatchId == 0 )
			{
			if( jobNo != null && batchNo != null )
				{
				if( (jobNo.length() < 1) || (batchNo.length() < 1) )
					 return NO_DISPATCH;
				}
			else
				return NO_DISPATCH;
			}
		
		return error;
    	}
    
    // PM Checklist is not monitored at the moment. This may change
    /*
    private String checkPMChecklist(String serviceType, String tagId)
    	{
    	String error = "";
    	if( serviceType != null )
			{
			if( serviceType.contentEquals("PM") )
				{
				String sqlQ = "SELECT pmCheckListId " +
	    			"FROM pmCheckList " +
	    			"where serviceTagId = '" + tagId + "'";
		    	Cursor cursor = db.rawQuery(sqlQ);
		    	
				if ( !cursor.moveToFirst() )
					{
					error = "The Service Tag is a PM, but no PM checklist has been filled out.";
					}
				if (cursor != null && !cursor.isClosed())
					{
					cursor.close();
					}
				}
			}
    	
    	return error;
    	}
    */
    
    private String safetyComplete( int tagId )
    	{
    	boolean oneChecked = false;
    	String error = "";
    	
    	String sqlQ = "select safetyTagChecklistItem.itemRequired, safetyTagChecklistItem.itemValue, safetyChecklist.itemType " +
    			"FROM safetyTagChecklistItem " +
    			"LEFT OUTER JOIN safetyChecklist on safetyTagChecklistItem.safetyChecklistId = safetyChecklist.safetyChecklistId " + 
    			"where safetyTagChecklistItem.serviceTagId = " + tagId;
    	
		Cursor cursor = db.rawQuery(sqlQ);
		String req, val, type;
		
		if (cursor.moveToFirst())
			{
			do
				{
				req = cursor.getString(0);
				val = cursor.getString(1);
				type = cursor.getString(2);
				if( req != null && val != null )
					{
					
					if( req.contentEquals("Y") )
						{
						if( type.contentEquals("C") )
							{
							if( (val.contentEquals("N")) )
								{
								error = "A Safety Checklist item marked \"Required\" is not marked complete.";
								break;
								}
							else
								oneChecked = true;
							}
						else
							{
							if( (val.length() < 1) )
								{
								error = "A Safety Checklist item marked \"Required\" is filled out.";
								break;
								}
							else
								oneChecked = true;
							}
						}
					else if( val.contentEquals("Y") )
						{
						oneChecked = true;
						}
					
					}
				}
			while (cursor.moveToNext());
			}
		if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
		if( !oneChecked )
			{
			if( error.length() > 0 )
				error += "\n\t-\t";
			error += "The Safety Checklist requires at least one item to be checked.";
			}
		
    	return error;
    	}
    
    private String checkBlues(int tagId)
    	{
    	String error = "";
    	
    	String completed = null;
    	
    	String sqlQ = "SELECT blueUnit.completed " +
    	    	"FROM blue " +
    	    		"LEFT OUTER JOIN blueUnit on blueUnit.blueId = blue.blueId " +
    	    	"WHERE blue.serviceTagId = " + tagId;
    	    		
    	Cursor cursor = db.rawQuery(sqlQ);
    	
    	if (cursor.moveToFirst())
    		{
    		do
    			{
	    		completed = cursor.getString(0);
	    		if( completed.contentEquals("N") )
	    			error = "You have Blues not marked complete.";
    			}
    		while (cursor.moveToNext());
    		}
    	if (cursor != null && !cursor.isClosed())
    		{
    		cursor.close();
    		}
    	
    	return error;
    	}
    
    private String checkForms(int serviceTagId)
    	{
    	String error = "";
    	String sqlQ = "SELECT FormDataId " +
			"FROM FormData " +
			"WHERE ParentTable = 'ServiceTagUnit' AND ParentId IN (" +
				"SELECT serviceTagUnitId FROM ServiceTagUnit WHERE serviceTagId = " + serviceTagId + ")" +
				" AND Completed != 'M'";
		Cursor cursor = db.rawQuery(sqlQ);
		
		if (cursor.moveToFirst())
			error += "There are forms not marked complete";
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		
		return error;
    	}
    
    private void noResults()
    	{
    	LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(
	    		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	LinearLayout row = new LinearLayout(mContext);
    	row.setLayoutParams(paramsRow);
    	
    	LayoutParams params = new LayoutParams();
		params.height = LayoutParams.WRAP_CONTENT;
		params.setMargins(10, 10, 10, 10);
		params.weight = 1;
		
		TextView tv = new TextView(this);
		tv.setLayoutParams(params);
		tv.setGravity(Gravity.LEFT);
		tv.setText("No Open Tags Available. Try Creating One.");
		tv.setTextSize(Twix_Theme.headerSizeLarge);
		tv.setTextColor(Twix_Theme.headerValue);
		tv.setBackgroundColor(Twix_Theme.sub2BG);
		tv.setTypeface(Typeface.MONOSPACE);
		tv.setPadding(10, 10, 10, 10);
		row.addView(tv);
    	
		tl.addView(row);
    	}
    
    public void Update_Page()
    	{
    	clearBgs();
    	findViewById(R.id.Sort_TagNo).setBackgroundColor(Twix_Theme.sortAsc);
    	setClickListeners();
    	readSQL();
    	}
    
    public void clearBgs()
    	{
    	findViewById(R.id.Sort_TagNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_SiteName).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Tenant).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_BatchNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_JobNo).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Description).setBackgroundColor(Twix_Theme.sortNone);
    	findViewById(R.id.Sort_Completed).setBackgroundColor(Twix_Theme.sortNone);
    	}
    
    private void setClickListeners()
	    {
	    findViewById(R.id.Sort_TagNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "openServiceTag.serviceTagId" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "openServiceTag.serviceTagId";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_SiteName).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "siteName" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "siteName";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Tenant).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "tenant" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "tenant";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_BatchNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "dispatch.batchNo" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "dispatch.batchNo";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_JobNo).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "dispatch.jobNo" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "dispatch.jobNo";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Description).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "description" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "description";
            		}
            	
            	readSQL();
                }
        	});
	    
	    findViewById(R.id.Sort_Completed).setOnClickListener(new  OnClickListener()
        	{
            @Override
            public void onClick(View v)
            	{
            	clearBgs();
            	if( CurrentSearch == "openServiceTag.completed" )
            		{
            		if( desc )
            			{
            			v.setBackgroundColor(Twix_Theme.sortAsc);
            			desc = false;
            			}
            		else
            			{
            			v.setBackgroundColor(Twix_Theme.sortDesc);
            			desc = true;
            			}
            		}
            	else
            		{
            		desc = false;
            		v.setBackgroundColor(Twix_Theme.sortAsc);
            		CurrentSearch = "openServiceTag.completed";
            		}
            	
            	readSQL();
                }
        	});
	   
	    
	    }
    
    public void newEquipment()
    	{
    	/*
    	if( serviceAddress == null || serviceAddress.contentEquals("") )
    		return;
    	List<String> l1 = new ArrayList<String>();
    	List<String> l2 = new ArrayList<String>();
    	String newEquipmentIdS = "";
    	l1.add("EquipmentCategoryId");	l1.add("ServiceAddressId");	l1.add("UnitNo");
    	l1.add("BarCodeNo");			l1.add("Manufacturer");		l1.add("Model");
    	l1.add("ProductIdentifier");	l1.add("SerialNo");			l1.add("Voltage");
    	l1.add("Economizer");			l1.add("Capacity");			l1.add("RefrigerantType");
    	l1.add("AreaServed");			l1.add("MfgYear");			l1.add("DateInService");
    	l1.add("DateOutService");		l1.add("Photo");			l1.add("Notes");
    	l1.add("CapacityUnits");
    	
    	for( int i = 0; i < 19; i++ )
    		{
	    	if( i == 1 )
	    		l2.add(serviceAddress);
	    	else
	    		l2.add("");
    		}
	    
    	String sqlQ = "select MIN(Equipment.equipmentId) from Equipment";
    	Cursor cursor = db.rawQuery(sqlQ);
    	if (cursor.moveToFirst())
			{
			newEquipmentIdS = cursor.getString(0);
			}
    	if (cursor != null && !cursor.isClosed())
			{
			cursor.close();
			}
    	
    	long newEquipmentId = Long.parseLong(newEquipmentIdS);
    	if( newEquipmentIdS.contentEquals("") || newEquipmentId > 0 )
    		newEquipmentIdS="-1";
    	else
    		newEquipmentIdS = Long.toString(newEquipmentId-1);
    	
    	l1.clear(); l2.clear();
    	
    	l1.add("serverEquipmentId");
    	l1.add("equipmentId");
    	l1.add("serviceAddressId");
    	
    	l2.add("0");
    	l2.add(newEquipmentIdS);
    	l2.add(serviceAddress);
    	
    	db.insert("Equipment", l1, l2);
    	Intent intent = new Intent(getParent(), Twix_AgentEquipmentTabHost_Edit.class);
    	intent.putExtra("serviceAddressId", serviceAddress);
    	intent.putExtra("equipmentId", newEquipmentIdS);
    	Twix_TabActivityGroup parentActivity = (Twix_TabActivityGroup)getParent();
        parentActivity.startChildActivity("Twix_AgentEquipmentTabHost_Edit", intent);
    	*/
    	}
	
    class SignatureDialog extends Dialog
    	{
    	public Bitmap signature;
    	public List<Integer> tagIdList = new ArrayList<Integer>();
		public SignatureDialog(Context context)
			{
			super(context);
			}
    
    	}
    
    // Signature Drawing Panel
	class Panel extends SurfaceView implements SurfaceHolder.Callback
		{
		private ViewThread	mThread;
		public Bitmap		mBitmap;
		private Canvas		mCanvas;
		private final Rect	mRect	= new Rect();
		private final Paint	mPaint;
		private float		mCurX;
		private float		mCurY;
		private float		pCurX;
		private float		pCurY;

		Paint				paint	= new Paint();

		public Panel(Context context, Bitmap bmp)
			{
			super(context);
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);

			getHolder().addCallback(this);
			mThread = new ViewThread(this);
			this.setFocusable(true);
			this.setLayoutParams(params);

			mPaint = new Paint();
			mPaint.setColor(Color.WHITE);

			pCurX = -1;
			pCurY = -1;

			if (bmp != null)
				{
				mBitmap = bmp;
				}
			}

		public void doDraw(Canvas canvas)
			{
			if (mBitmap != null)
				{
				canvas.drawBitmap(mBitmap, 0, 0, null);
				}
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
			if (!mThread.isAlive())
				{
				mThread = new ViewThread(this);
				mThread.setRunning(true);
				mThread.start();
				}
			}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
			{
			if (mThread.isAlive())
				{
				mThread.setRunning(false);
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

		@Override
		protected void onDraw(Canvas canvas)
			{
			if (mBitmap != null)
				{
				mPaint.setARGB(255, 0, 0, 0);
				canvas.drawBitmap(mBitmap, 0, 0, null);
				}
			}

		public void clear()
			{
			if (mCanvas != null)
				{
				mPaint.setARGB(0xff, 255, 255, 255);
				mCanvas.drawPaint(mPaint);
				invalidate();
				}
			}

		@Override
		public boolean onTouchEvent(MotionEvent event)
			{
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
			return true;
			}

		private void drawPoint(float x, float y)
			{
			if (mBitmap != null)
				{
				float radius = 1.5f;
				mPaint.setARGB(255, 0, 0, 0);
				mCanvas.drawCircle(x, y, radius, mPaint);
				if (pCurX >= 0 && pCurY >= 0)
					{
					mCanvas.drawLine(pCurX, pCurY, x, y, mPaint);
					mCanvas.drawLine(pCurX - 1, pCurY, x - 1, y, mPaint);
					mCanvas.drawLine(pCurX + 1, pCurY, x + 1, y, mPaint);
					mCanvas.drawLine(pCurX, pCurY - 1, x, y - 1, mPaint);
					mCanvas.drawLine(pCurX, pCurY + 1, x, y + 1, mPaint);
					}
				pCurX = x;
				pCurY = y;

				mRect.set((int) (x - radius - 2), (int) (y - radius - 2),
						(int) (x + radius + 2), (int) (y + radius + 2));
				invalidate(mRect);
				}
			}

		}
	
	class ViewThread extends Thread
		{
		private Panel			mPanel;
		private SurfaceHolder	mHolder;
		private boolean			mRun	= false;

		public ViewThread(Panel panel)
			{
			mPanel = panel;
			mHolder = mPanel.getHolder();
			}

		public void setRunning(boolean run)
			{
			mRun = run;
			}

		@Override
		public void run()
			{
			Canvas canvas = null;
			while (mRun)
				{
				canvas = mHolder.lockCanvas();
				if (canvas != null)
					{
					mPanel.doDraw(canvas);
					mHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	
	}