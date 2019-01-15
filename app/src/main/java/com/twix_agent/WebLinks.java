package com.twix_agent;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

public class WebLinks extends Activity {
@Override

public void onCreate(Bundle savedInstanceState) {

   super.onCreate(savedInstanceState);
   setContentView(R.layout.browser);

 WebView wv=(WebView)findViewById(R.id.browser);

    WebSettings ws=wv.getSettings();
      // ws.setBuiltInZoomControls(true);
       
       Intent intent = getIntent();
       Bundle extras = intent.getExtras();
       String jobno = extras.getString("jobNo");
       String batchno = extras.getString("batchNo");
       String address = extras.getString("address1");
       String sitename = extras.getString("sitename");
       
      // String password_string = extras.getString("EXTRA_PASSWORD");
       
       
   //    int jobno = getIntent().getIntExtra("j", 0);
   //    int batchno = getIntent().getIntExtra("b", 0);
      // int tagno = getIntent().getIntExtra("serviceTagId", 0);
       wv.getSettings().setJavaScriptEnabled(true);
      // StringBuffer buffer=new StringBuffer("http://vision.xoeye.io/partners/5b9fa2702a7489ca0ff2209d/share/");
       //buffer.append("shareId="+URLEncoder.encode("53574700240848"));
       //buffer.append("&serviceTicket="+URLEncoder.encode("53574700240848"));
       //buffer.append("customer="+URLEncoder.encode("Therma Test"));
       //wv.loadUrl(buffer.toString());
       ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      // wv.setWebViewClient(new Callback());
      // asubstring = hallostring.substring(0, 1); 
       String unique = jobno.substring(0, 6)  + batchno.substring(1,8);
       String url = "http://vision.xoeye.io/partners/5b9fa2702a7489ca0ff2209d/share/";
       String fullUrl = url + "?shareId=" +unique + "&serviceTicket=" + unique + "&customer=" + sitename + "&location=" + address + "" ;
       wv.postUrl(fullUrl, Base64.encode(bytes.toByteArray(), Base64.DEFAULT));
     // url = url + "?mode=app";
     //  String postData = "shareId=53574700240848&serviceTicket=53574700240848&customer=Therma Test&location='1601 Las Plumas'&tags='test'";
     //  wv.postUrl(url,EncodingUtils.getBytes(postData, "BASE64"));
     //  url = url + "?mode=app";
       
   //   String postData = "shareId=53574700240848&secret=9ec493480630926ac5c0e04e70e9730d&tags=test";
       
       //wv.postUrl(
    	//	   "https://vision.xoeye.io/partner/5b9fa2702a7489ca0ff2209d/share/endpoint",
          // EncodingUtils.getBytes(postData, "BASE64"));
      
       
       RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.xoi_connecting);
       rlayout.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
          finish();
          }

       });
       
      Button Xoi_done_btn=(Button) findViewById(R.id.Xoi_btn_done);
       
   	OnClickListener Xoi_done = new OnClickListener()
		{
		@Override
		public void onClick(View v)
			{
			finish();
			
			}		};
			Xoi_done_btn.setOnClickListener(Xoi_done);
   }


@Override
protected void onResume() {
    super.onResume();
  finish();
    

}

}


