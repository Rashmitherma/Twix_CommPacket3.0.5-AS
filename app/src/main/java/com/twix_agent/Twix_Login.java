package com.twix_agent;

import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.twix.ClientRequest;
import com.twix.DispatchRequest;
import com.twix.SiteSearch;

import android.app.Dialog;
import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Twix_Login
	{
	public LoginDialog dialog;
	public Twix_Login(final Context context, final Twix_Application app, final int action, final boolean changeUser,
			final Object passObj, final boolean ReInit)
		{
		dialog = new LoginDialog(context, app);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	if( (app.empno == null) || (app.empno.length() <= 0) )
    		dialog.setCancelable(false);
    	
		View viewToLoad = LayoutInflater.from(context).inflate(R.layout.login, null);
		
		Button submit = (Button) viewToLoad.findViewById(R.id.submit_login);
		
		final EditText username = (EditText) viewToLoad.findViewById(R.id.login_username);
		if( app.username != null && !changeUser )
			username.setText(app.username);
		final EditText password = (EditText) viewToLoad.findViewById(R.id.login_password);
		final CheckBox offline = (CheckBox) viewToLoad.findViewById(R.id.Offline);
		final LinearLayout offlineHost = (LinearLayout) viewToLoad.findViewById(R.id.OfflineHost);
		
		
		final String last_empno		= app.prefs.getString("app.empno", null);
		final String last_techEmail	= app.prefs.getString("app.techEmail", null);
		final String last_hash_user	= app.prefs.getString("hash_user", null);
		final String last_hash_pw	= app.prefs.getString("hash_pw", null);
		// If the request is a Login AND no variables are null
		if( action == ClientRequest.LOGIN &&
			(last_hash_user != null && last_hash_pw != null &&
			last_empno != null))
			offlineHost.setVisibility(View.VISIBLE);
		else if ( action != ClientRequest.LOGIN && action != ClientRequest.SYNC_UPLOAD && action != ClientRequest.SYNC_DOWNLOAD )
			{
			username.setEnabled(false);
			username.setTextColor(app.Twix_Theme.headerValue);
			username.setBackgroundColor(app.Twix_Theme.disabledColorBG);
			password.requestFocus();
			}
		else
			{
			if( app.username != null && !changeUser )
				password.requestFocus();
			else
				username.requestFocus();
			}
		
		
		
		submit.setOnClickListener(new OnClickListener()
			{
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v)
				{
				if( (username.length() > 0) && password.length() > 0 )
					{
					// Fetch the Username and Password Entered
					app.username = username.getText().toString();
					String plain_pw = password.getText().toString();
					
					// Encrypt the password
					String secret_key = "3vHdP5KKCxCvCrNnooC40Q==";
					SecretKeySpec KS = new SecretKeySpec(Base64.decode(secret_key, Base64.DEFAULT), "AES");
					Cipher cipher;
					try
						{
						cipher = Cipher.getInstance("AES");
						cipher.init(Cipher.ENCRYPT_MODE, KS);
						
						byte[] encrypted = cipher .doFinal( plain_pw.getBytes() );
						app.password = Base64.encodeToString(encrypted, Base64.NO_WRAP );
						}
					catch ( Exception e )
						{
						e.printStackTrace();
						}
					
					// Check if this is a login request 
					if( action == ClientRequest.LOGIN && offline.isChecked() )
						{
						if( app.password != null )
							{
							String hash_user	= app.oneWayHash(app.username);
							String hash_pw		= app.oneWayHash(app.password);
							
							if( hash_user.contentEquals(last_hash_user) &&
									hash_pw.contentEquals(last_hash_pw) )
								{
								app.empno = last_empno;
								app.techEmail = last_techEmail;
								app.refreshTabs();
								
								app.prefs.edit().putBoolean("offline", true ).commit();
								
								dismiss();
								app.login = null;
								app.password = null;
								}
							else
								{
								Toast.makeText(context, "Login Failed. Please try again.", Toast.LENGTH_LONG).show();
								}
							}
						else
							Toast.makeText(context, "Failed to Encrypt Password. " +
									"Please contact your local administrator.", Toast.LENGTH_LONG).show();
						}
					else
						{
						if( app.password != null )
							{
							if( action == ClientRequest.SYNC_UPLOAD ||  action == ClientRequest.LOGIN )
								app.Sync(true, false, ReInit);
							else if( action == ClientRequest.SITE_SEARCH )
								app.SiteSearch(true, (SiteSearch) passObj);
							else if( action == ClientRequest.SITE_DOWNLOAD )
								app.SiteDownload(true, (ArrayList<Integer>) passObj);
							else if( action == ClientRequest.DOWNLOAD_UPDATE )
								app.UpdateDownload(true);
							else if( action == ClientRequest.ASSIGN_MECHANIC )
								app.AssignDispatch(true, (DispatchRequest) passObj);
							}
						else
							Toast.makeText(context, "Failed to Encrypt Password. " +
									"Please contact your local administrator.", Toast.LENGTH_LONG).show();
						}
					
					
					
					
					}
				else
					Toast.makeText(context, "Username and/or Password cannot be blank.", Toast.LENGTH_LONG).show();
				}
			});
		
		dialog.setContentView(viewToLoad);
		dialog.show();
		}
	
	public synchronized void dismiss()
		{
		if( dialog != null && dialog.isShowing() )
			dialog.dismiss();
		}
	
	public class LoginDialog extends Dialog
		{
		Twix_Application app;
		public LoginDialog(Context context, Twix_Application a)
			{
			super(context);
			app = a;
			}
		
		@Override
		public void onBackPressed()
			{
			if( app.empno != null && app.empno.length() > 0 )
				super.onBackPressed();
			else
				app.MainTabs.finish();
			}
		
		}
	
	}
