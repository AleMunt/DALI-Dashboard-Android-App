/*
 * Copyright (C) 2018 Alexandru Munteanu
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

/**
 * The main class of the application
 */
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


    /**
     * Initializes the RecyclerView and determines image sizes based on screen width
     *
     * @param savedInstanceState
     */
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
        Init();
    }

    /**
     * Used to initialize variables that need to be reset when the app is refreshed.
     * Establishes onClick action of the floating action button as a filter.
     * Checks the current status of the member.json file and runs JSONDowloaded() if it
     * is downloaded. Attempts to download file if it is not downloaded
     */
    public void Init() {
        termsOnFilters = new ArrayList<>();
        projectFilters = new ArrayList<>();
        filteredDALIMembers = new ArrayList<>();
        daliMembers = new ArrayList<>();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (daliMembers.size() > 0)
                    launchFilterMenu();
                else
                    ShowToast("Error: DALI Dashboard Data has not been downloaded");
            }
        });
        jsonFile = new File(this.getFilesDir().getAbsolutePath() + "/members.json");
        if (!jsonFile.exists()) {
            ShowToast("Downloading DALI Dashboard Data...");
            DownloadFile("members.json", -1);
        } else {
            JSONDownloaded();
        }
    }

    /**
     * Creates a square placeholder Bitmap and a square frame Bitmap to surround images based on an
     * image size determined by the screen size of the device.
     * Creates an array of images for the RecyclerView images using a downloaded image or a placeholder
     * Creates a boolean array showing what images were downloaded and attempts a download of the
     * first image that was not downloaded
     */
    public void CheckImages() {
        noImageAvailable = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dali_logo), imageSize, imageSize, false);

        // Create a frame Bitmap with rounded corners to give the DALI member image rounded corners.
        Bitmap frameImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.frame), imageSize, imageSize, false);
        File currentFile;
        for (int i = 0; i < downloadedImages.length; i++) {

            // 7 is the size of "images/" string. 7 is hardcoded for performance.
            currentFile = new File(context.getFilesDir() + "/" + daliMembers.get(i).getIconUrl().substring(7));

            // Sets a boolean to true if an image is downloaded and false if the noImageAvailable Bitmap is used
            if (currentFile.exists()) {
                downloadedImages[i] = true;
                Bitmap currentImage = BitmapFactory.decodeFile(currentFile.getPath());

                // Crop an image into a square if its height is greater than its width
                if (currentImage.getHeight() > currentImage.getWidth()) {
                    currentImage = Bitmap.createBitmap(currentImage, 0, currentImage.getHeight() / 2 - currentImage.getWidth() / 2, currentImage.getWidth(), currentImage.getWidth());
                }
                daliMembers.get(i).setBitmap(Bitmap.createScaledBitmap(currentImage, imageSize, imageSize, false));
            } else {
                downloadedImages[i] = false;
                daliMembers.get(i).setBitmap(noImageAvailable);
            }
        }

        // Sets RVAdapter with DALIMembers to RecyclerView
        rvAdapter = new RVAdapter(daliMembers, frameImage, this);
        memberViewer.setAdapter(rvAdapter);

        // Downloads the first image that is not downloaded
        for (int i = 0; i < downloadedImages.length; i++)
            if (downloadedImages[i] == false) {
                ShowToast("Downloading Images");
                DownloadImage(i);
                break;
            }
    }

    /**
     * Checks if an image at the image index has been downloaded and downloads it if it hasn't
     * been downloaded. If it has been downloaded then it recursively checks the next image index if it can.
     *
     * @param imageIndex The index of an image if an image is being downloaded.
     *                   It should be -1 if an image is not being downloaded.
     */
    public void DownloadImage(int imageIndex) {
        if (downloadedImages[imageIndex] && downloadedImages.length > imageIndex + 1)
            DownloadImage(imageIndex + 1);
        else
            DownloadFile(daliMembers.get(imageIndex).getIconUrl().substring(7), imageIndex);
    }

    /**
     * Starts a DownloadService with a url and details about where it should be downloaded.
     * If the download is an image then imageIndex should be the index of the image.
     * If it isn't an image being downloaded then imageIndex should be -1
     *
     * @param url The url of the file being downloaded
     * @param imageIndex The index of an image if an image is being downloaded.
     *                   It should be -1 if an image is not being downloaded.
     */
    public void DownloadFile(String url, int imageIndex) {
        intent = new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra("url", url);
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        intent.putExtra("internalStorageDir", this.getFilesDir().getAbsolutePath());
        intent.putExtra("imageIndex", imageIndex);
        startService(intent);
    }

    /**
     * Reads the downloaded members.json file into a String, puts in into a sorted list of DALi members
     * and begins image downloads if the file has been correctly downloaded.
     */
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

        // Makes sure that file was downloaded correctly by checking it has a correct ending
        // Retries the download if the file is not complete.
        if (jsonString.endsWith("}]")) {
            Type listType = new TypeToken<ArrayList<DALIMember>>() {
            }.getType();
            daliMembers = new Gson().fromJson(jsonString, listType);
            downloadedImages = new boolean[daliMembers.size()];
            Collections.sort(daliMembers, new Comparator<DALIMember>() {

                @Override
                public int compare(DALIMember member1, DALIMember member2) {
                    return member1.getName().compareTo(member2.getName());
                }
            });

            // Search for possible filters and check for missing images
            CreateFilters();
            CheckImages();
        } else {
            jsonFile.delete();
            RetryDownload("members.json", "There was an error downloading DALI members data.", -1);
        }
    }

    /**
     * Search for possible filters in the DALI members and add them to the filters for terms or projects
     * if they have not been already added
     */
    public void CreateFilters() {

        // Check for all possible term and project filters in all DALI Members
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

        // Create a formatted list of all filters for the filter dialog
        allFilters = new String[termsOnFilters.size() + projectFilters.size()];
        for (int i = 0; i < termsOnFilters.size(); i++)
            allFilters[i] = termsOnFilters.get(i) + " (Term)";
        for (int i = 0; i < projectFilters.size(); i++)
            allFilters[termsOnFilters.size() + i] = projectFilters.get(i) + " (Project)";

        // Sets all filters to false
        selectedFilter = new boolean[allFilters.length];
        for (boolean selected : selectedFilter)
            selected = false;
    }

    /**
     * Check if an array contains a string
     *
     * @param strings List of strings that are being searched through
     * @param searchedString String that is being searched for
     * @return
     */
    public boolean ArrayContainsString(ArrayList<String> strings, String searchedString) {
        for (String currentString : strings)
            if (currentString.equals(searchedString))
                return true;
        return false;
    }

    /**
     * Open a dialog where filters can be selected and filter the DALI members
     */
    public void launchFilterMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter")
                .setMultiChoiceItems(allFilters, selectedFilter, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item, boolean b) {

                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Check if there are filters
                        boolean allFalse = true;
                        for (boolean bool : selectedFilter) {
                            if (bool)
                                allFalse = false;
                        }
                        if (allFalse) {
                            rvAdapter = new RVAdapter(daliMembers, frameImage, context);
                            memberViewer.setAdapter(rvAdapter);
                            return;
                        }

                        // Add DALI Members to a filtered list if they have all of the filters
                        filteredDALIMembers = new ArrayList<>();
                        for (DALIMember member : daliMembers) {
                            Boolean missingAFilter = false;
                            for (int i = 0; i < termsOnFilters.size(); i++) {
                                boolean hasFilter = true;
                                if (selectedFilter[i] == true) {
                                    hasFilter = false;
                                    for (String term : member.getTerms_on())
                                        if (termsOnFilters.get(i).equals(term))
                                            hasFilter = true;
                                }
                                if (!hasFilter)
                                    missingAFilter = true;
                            }
                            for (int i = 0; i < projectFilters.size(); i++) {
                                boolean hasFilter = true;
                                if (selectedFilter[termsOnFilters.size() + i] == true) {
                                    hasFilter = false;
                                    for (String project : member.getProject())
                                        if (projectFilters.get(i).equals(project))
                                            hasFilter = true;
                                }
                                if (!hasFilter)
                                    missingAFilter = true;
                            }
                            if (!missingAFilter)
                                filteredDALIMembers.add(member);
                        }

                        // Set up RecyclerView with filtered DALI members
                        rvAdapter = new RVAdapter(filteredDALIMembers, frameImage, context);
                        memberViewer.setAdapter(rvAdapter);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        builder.create().show();
    }

    /**
     * Display a dialog with the option to retry a download that could not be completed
     *
     * @param filename The filename of a file
     * @param errorDetails The details of a download error
     * @param imageIndex The index of an image if an image is being downloaded.
     *                   It should be -1 if an image is not being downloaded.
     */
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Run appropriate action for items being selected in the menu in the toolbar
     *
     * @param item The MenuItem that has been tapped on
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Display open source licenses used in app
        if (id == R.id.action_licenses) {
            LicencesDialog();
            return true;
        }

        // Display a map with all DALIMembers if members.json data has been loaded
        else if (id == R.id.maps) {
            if (daliMembers.size() == 0) {
                ShowToast("Error: DALI Dashboard Data has not been downloaded");
                return true;
            }
            String members[] = new String[daliMembers.size()];
            double memberLocations[] = new double[daliMembers.size() * 2];
            for (int i = 0; i < daliMembers.size(); i++) {

                // Add the names, latitudes and longitudes to arrays.
                //Move in twos in member locations as there is latitude and a longitude.
                members[i] = daliMembers.get(i).getName();
                memberLocations[i * 2] = daliMembers.get(i).getLat_long()[0];
                memberLocations[i * 2 + 1] = daliMembers.get(i).getLat_long()[1];
            }

            // Starts a map activity with names and locations
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("memberNames", members);
            intent.putExtra("memberLocations", memberLocations);
            intent.putExtra("title", "Everyone's Locations");
            this.startActivity(intent);
            return true;
        }

        // Deletes all files and redownloads members.json and all images
        else if (id == R.id.action_refresh) {
            if (daliMembers.size() == 0)
                return true;
            for (boolean downloaded : downloadedImages)
                if (!downloaded)
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

    /**
     * Shows a toast message that hovers on the screen for a few seconds
     *
     * @param message The message being displayed in the toast message
     */
    public void ShowToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a dialog with all open source licenses used in the app
     */
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

    /**
     * Class DownloadReceiver receives results and progress from a DownloadService object.
     */
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

                // Check for an error and attempt to retry a download if there is one
                if (resultData.getBoolean("error")) {
                    String error = resultData.getString("errorDetails");
                    File file = new File(context.getFilesDir() + "/temp/" + filename + ".temp");
                    file.delete();
                    RetryDownload(filename, error, imageIndex);

                    // Check for a completed download and load file if download is complete
                } else if (resultData.getBoolean("done")) {
                    if (filename.equals("members.json")) {

                        ShowToast("DALI Dashboard Data Downloaded");
                        JSONDownloaded();
                    } else {

                        // 7 is the size of "images/" string. 7 is hardcoded for performance.
                        String fileName = daliMembers.get(imageIndex).getIconUrl().substring(7);
                        File image = new File(context.getFilesDir() + "/" + fileName);
                        Bitmap currentImage = BitmapFactory.decodeFile(image.getPath());

                        // Crop an image into a square if its height is greater than its width
                        if (currentImage.getHeight() > currentImage.getWidth()) {
                            currentImage = Bitmap.createBitmap(currentImage, 0, currentImage.getHeight() / 2 - currentImage.getWidth() / 2, currentImage.getWidth(), currentImage.getWidth());
                        }
                        daliMembers.get(imageIndex).setBitmap(Bitmap.createScaledBitmap(currentImage, imageSize, imageSize, false));
                        rvAdapter.notifyItemChanged(imageIndex);

                        // Try downloading the next image if there are more DALI members than current image index
                        if (daliMembers.size() > imageIndex + 1)
                            DownloadImage(imageIndex + 1);
                    }
                }
            }
        }
    }
}