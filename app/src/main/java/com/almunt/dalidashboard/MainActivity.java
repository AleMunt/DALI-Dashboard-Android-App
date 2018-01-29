/*
 * Copyright 2018 Alexandru Munteanu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almunt.dalidashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> termsOnFilters = new ArrayList<>();
    ArrayList<String> projectFilters = new ArrayList<>();
    ArrayList<DALIMember> daliMembers = new ArrayList<>();
    RecyclerView memberViewer;
    LinearLayoutManager linearLayoutManager;
    RVAdapter rvAdapter;
    static AsyncTask asyncTask;
    Intent intent;
    Context context;
    File jsonFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        memberViewer = (RecyclerView) findViewById(R.id.recyclerView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFilterMenu();
            }
        });
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        String text=getPreference("members.json");
        jsonFile=new File(this.getFilesDir().getAbsolutePath()+"/members.json");
        if(!jsonFile.exists())
            DownloadJson();
        else {
            JSONDownloaded();
        }
        memberViewer.setLayoutManager(linearLayoutManager);

    }

    public void DownloadJson ()
    {
        intent = new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra("url", "members.json");
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        intent.putExtra("stop",true);
        intent.putExtra("internalStorageDir",this.getFilesDir().getAbsolutePath());
        startService(intent);
    }
    public void JSONDownloaded ()
    {
        Toast.makeText(this, "This is my Toast message!",
                Toast.LENGTH_LONG).show();
        String jsonString="";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile));

        StringBuffer fileContents = new StringBuffer();
        String line = bufferedReader.readLine();
        while (line != null) {
            fileContents.append(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        jsonString=fileContents.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Type listType = new TypeToken<ArrayList<DALIMember>>(){}.getType();
        daliMembers = new Gson().fromJson(jsonString, listType);
        rvAdapter=new RVAdapter(daliMembers);
        memberViewer.setAdapter(rvAdapter);
    }
    public void launchFilterMenu()
    {
        // TODO Write Filter Menu
    }

    public void setPreference(String name, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, name);
        editor.apply();
        editor.commit();
    }

    public String getPreference(String key) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=settings.edit();
        String value = settings.getString(key, "");
        return value;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_licenses) {
            LicencesDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void LicencesDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Open Source Licenses");
        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        alert.setView(webView);
        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    @SuppressLint("ParcelCreator")
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                double progress = resultData.getInt("progress");
                String filename=resultData.getString("filename");
                double total=resultData.getInt("total");
                double percent=(progress/total)*100;

                if(resultData.getBoolean("error",false))
                {
                    String error=resultData.getString("errordetails");
                    File file=new File(context.getFilesDir()+"/temp/"+filename+".temp");
                    file.delete();
//                    RetryDownload(filename, error);
                }
                if ((int)percent == 100) {
                    if(filename.equals("members.json"));
                    JSONDownloaded();
                }
            }
        }
    }
}