package com.twix_agent;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class webview extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        WebView browser = (WebView)findViewById(R.id.browser);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twix.therma.com/twix"));
        startActivity(browserIntent);
    }

    public void openURLinBrowser(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        startActivity(browserIntent);
    }
}
