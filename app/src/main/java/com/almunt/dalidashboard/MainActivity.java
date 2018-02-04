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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> termsOnFilters;
    boolean[] selectedFilter;
    ArrayList<String> projectFilters;
    String[] allFilters;
    ArrayList<DALIMember> filteredDALIMembers;
    ArrayList<DALIMember> daliMembers;
    RecyclerView memberViewer;
    LinearLayoutManager linearLayoutManager;
    RVAdapter rvAdapter;
    Intent intent;
    Context context;
    File jsonFile;
    boolean[] downloadedImages;
    Bitmap noImageAvailable;
    int imageSize;
    Bitmap frameImage;
    DisplayMetrics displayMetrics;
    boolean currentlyFiltered = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageSize = displayMetrics.widthPixels / 3;
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        memberViewer = findViewById(R.id.recyclerView);
        memberViewer.setLayoutManager(linearLayoutManager);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(daliMembers.size()>0)
                    launchFilterMenu();
                else
                    ShowToast("Error: DALI Dashboard Data has not been downloaded");
            }
        });
        Init();
    }

    public void Init()
    {
        termsOnFilters = new ArrayList<>();
        projectFilters = new ArrayList<>();
        filteredDALIMembers = new ArrayList<>();
        daliMembers = new ArrayList<>();
        jsonFile = new File(this.getFilesDir().getAbsolutePath() + "/members.json");
        if (!jsonFile.exists()) {
            ShowToast("Downloading DALI Dashboard Data...");
            DownloadFile("members.json", -1);
        }
        else {
            JSONDownloaded();
        }
    }

    public void CheckImages() {
        noImageAvailable = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dali_logo), imageSize, imageSize, false);
        Bitmap frameImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.frame), imageSize, imageSize, false);
        File currentFile;
        for (int i = 0; i < downloadedImages.length; i++) {
            currentFile = new File(context.getFilesDir() + "/" + daliMembers.get(i).getIconUrl().substring(7));
            if (currentFile.exists()) {
                downloadedImages[i] = true;
                Bitmap currentImage = BitmapFactory.decodeFile(currentFile.getPath());
                if (currentImage.getHeight() > currentImage.getWidth()) {
                    currentImage = Bitmap.createBitmap(currentImage, 0, currentImage.getHeight() / 2 - currentImage.getWidth() / 2, currentImage.getWidth(), currentImage.getWidth());
                }
                daliMembers.get(i).setBitmap(Bitmap.createScaledBitmap(currentImage, imageSize, imageSize, false));
            } else {
                downloadedImages[i] = false;
                daliMembers.get(i).setBitmap(noImageAvailable);
            }
        }

        rvAdapter = new RVAdapter(daliMembers, frameImage, this);
        memberViewer.setAdapter(rvAdapter);
        for (int i = 0; i < downloadedImages.length; i++)
            if (downloadedImages[i] == false) {
                ShowToast("Downloading Images");
                DownloadImage(i);
                break;
            }
    }

    public void DownloadImage(int imageIndex) {
        if (downloadedImages[imageIndex] && downloadedImages.length > imageIndex + 1)
            DownloadImage(imageIndex + 1);
        else
            DownloadFile(daliMembers.get(imageIndex).getIconUrl().substring(7), imageIndex);
    }

    public void DownloadFile(String url, int imageIndex) {
        intent = new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra("url", url);
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        intent.putExtra("stop", true);
        intent.putExtra("internalStorageDir", this.getFilesDir().getAbsolutePath());
        intent.putExtra("imageIndex", imageIndex);
        startService(intent);
    }

    public void JSONDownloaded() {
        String jsonString = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile));

            StringBuffer fileContents = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                fileContents.append(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            jsonString = fileContents.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jsonString.endsWith("}]")) {
            Type listType = new TypeToken<ArrayList<DALIMember>>() {
            }.getType();
            daliMembers = new Gson().fromJson(jsonString, listType);
            downloadedImages = new boolean[daliMembers.size()];
            Collections.sort(daliMembers, new Comparator<DALIMember>(){

                @Override
                public int compare(DALIMember member1, DALIMember member2) {
                    return member1.getName().compareTo(member2.getName());
                }
            });
            CreateFilters();
            CheckImages();
        } else {
            jsonFile.delete();
            RetryDownload("members.json", "There was an error downloading DALI members data.", -1);
        }
    }

    public void CreateFilters() {
        for (DALIMember daliMember : daliMembers)
            if (daliMember.getTerms_on().length > 0) {
                for (int j = 0; j < daliMember.getTerms_on().length; j++)
                    if (!ArrayContainsString(termsOnFilters, daliMember.getTerms_on()[j]))
                        termsOnFilters.add(daliMember.getTerms_on()[j]);

                for (int j = 0; j < daliMember.getProject().length; j++)
                    if (!ArrayContainsString(projectFilters, daliMember.getProject()[j]))
                        if (daliMember.getProject()[j].trim().length() > 0)
                            projectFilters.add(daliMember.getProject()[j]);
            }
        allFilters = new String[termsOnFilters.size() + projectFilters.size()];
        for (int i = 0; i < termsOnFilters.size(); i++)
            allFilters[i] = termsOnFilters.get(i)+" (Term)";
        for (int i = 0; i < projectFilters.size(); i++)
            allFilters[termsOnFilters.size() + i] = projectFilters.get(i)+" (Project)";
        selectedFilter = new boolean[allFilters.length];
        for (boolean selected : selectedFilter)
            selected = false;
    }

    public boolean ArrayContainsString(ArrayList<String> strings, String searchedString) {
        for (String currentString : strings)
            if (currentString.equals(searchedString))
                return true;
        return false;
    }

    public void launchFilterMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter")
                .setMultiChoiceItems(allFilters, selectedFilter, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item, boolean b) {

                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean allFalse = true;
                        boolean allTrue = true;
                        for(boolean bool: selectedFilter)
                        {
                            if(bool)
                                allFalse=false;
                            else
                                allTrue=false;
                        }
                        if(allFalse || allTrue) {
                            rvAdapter = new RVAdapter(daliMembers, frameImage, context);
                            memberViewer.setAdapter(rvAdapter);
                            currentlyFiltered = false;
                            return;
                        }
                        filteredDALIMembers = new ArrayList<>();
                        for(DALIMember member:daliMembers)
                        {
                            Boolean missingAFilter = false;
                            for(int i=0;i<termsOnFilters.size();i++)
                            {
                                boolean hasFilter = true;
                                if(selectedFilter[i]==true) {
                                    hasFilter = false;
                                    for (String term:member.getTerms_on())
                                        if(termsOnFilters.get(i).equals(term))
                                            hasFilter = true;
                                }
                                if(!hasFilter)
                                    missingAFilter = true;
                            }
                            for(int i=0;i<projectFilters.size();i++)
                            {
                                boolean hasFilter = true;
                                if(selectedFilter[termsOnFilters.size()+i]==true) {
                                    hasFilter = false;
                                    for (String project:member.getProject())
                                        if(projectFilters.get(i).equals(project))
                                            hasFilter = true;
                                }
                                if(!hasFilter)
                                    missingAFilter = true;
                            }
                            if(!missingAFilter)
                                filteredDALIMembers.add(member);
                        }
                        rvAdapter = new RVAdapter(filteredDALIMembers, frameImage, context);
                        memberViewer.setAdapter(rvAdapter);
                        currentlyFiltered = true;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        builder.create().show();
    }

    public void RetryDownload(final String filename, String errorDetails, final int imageIndex) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Download Error");
        alertDialog.setMessage(errorDetails);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (filename.equals("members.json"))
                    DownloadFile(filename, -1);
                else
                    DownloadFile(filename, imageIndex);
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_licenses) {
            LicencesDialog();
            return true;
        }
        else if(id == R.id.maps)
        {
            if(daliMembers.size()==0) {
                ShowToast("Error: DALI Dashboard Data has not been downloaded");
                return true;
            }
            String members[]=new String[daliMembers.size()];
            double memberLocations[] = new double[daliMembers.size()*2];
            for(int i=0;i<daliMembers.size();i++) {
                members[i] = daliMembers.get(i).getName();
                memberLocations[i*2]=daliMembers.get(i).getLat_long()[0];
                memberLocations[i*2+1]=daliMembers.get(i).getLat_long()[1];
            }
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("members", members);
            intent.putExtra("memberLocations", memberLocations);
            intent.putExtra("title", "Everyone's Locations");
            this.startActivity(intent);
            return true;
        }
        else if(id==R.id.action_refresh)
        {
            if(daliMembers.size()==0)
                return true;
            for(boolean downloaded:downloadedImages)
                if(!downloaded)
                    return true;
            File toBeDeletedFolder = new File(this.getFilesDir().toString());
            if (toBeDeletedFolder.exists()) {
                File[] files = toBeDeletedFolder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
            Init();
        }
        return super.onOptionsItemSelected(item);
    }

    public void ShowToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void LicencesDialog() {
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
                String filename = resultData.getString("filename");
                int imageIndex = resultData.getInt("imageIndex", -1);
                if (resultData.getBoolean("error")) {
                    String error = resultData.getString("errorDetails");
                    File file = new File(context.getFilesDir() + "/temp/" + filename + ".temp");
                    file.delete();
                    RetryDownload(filename, error, imageIndex);
                } else if (resultData.getBoolean("done")) {
                    if (filename.equals("members.json")) {
                        ShowToast("DALI Dashboard Data Downloaded");
                        JSONDownloaded();
                    }
                    else {
                        File image = new File(context.getFilesDir() + "/" + daliMembers.get(imageIndex).getIconUrl().substring(7));
                        Log.d("file", String.valueOf(imageIndex));
                        daliMembers.get(imageIndex).setBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(image.getPath()), imageSize, imageSize, false));
                        rvAdapter.notifyItemChanged(imageIndex);
                        if (daliMembers.size() > imageIndex + 1)
                            DownloadImage(imageIndex + 1);
                    }
                }
            }
        }
    }
}