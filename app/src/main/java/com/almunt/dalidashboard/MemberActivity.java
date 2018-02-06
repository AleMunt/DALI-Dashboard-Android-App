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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.util.Arrays;

/**
 * An activity for displaying information about DALI Members
 */
public class MemberActivity extends AppCompatActivity {

    DALIMember daliMember;
    Bitmap memberImage;
    Bitmap frameImage;
    DisplayMetrics displayMetrics;
    int imageSize;
    ImageView imageView;
    ImageView imageFrameView;
    TextView messageTextView;
    TextView websiteTextView;
    TextView locationTextView;
    TextView termsOnTextView;
    TextView projectsTextView;
    String url;

    /**
     * Use a Chrome Custom Tab for a more seamless and faster transition to website
     */
    CustomTabsIntent customTabsIntent;
    CustomTabsIntent.Builder builder;

    Context context;

    /**
     * Grab all data from the intent and displays it in the Member Activity UI
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageSize = displayMetrics.widthPixels / 2;
        daliMember = new Gson().fromJson(getIntent().getStringExtra("member"), DALIMember.class);
        GetMemberImage();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(daliMember.getName());
        messageTextView = findViewById(R.id.message);
        messageTextView.setText("\"" + daliMember.getMessage() + "\"");
        websiteTextView = findViewById(R.id.website);
        if (daliMember.getUrl().startsWith("//"))
            url = daliMember.getUrl().substring(2);
        else
            url = "mappy.dali.dartmouth.edu/" + daliMember.getUrl();
        websiteTextView.setText(("Website: " + url));
        locationTextView = findViewById(R.id.location);
        locationTextView.setText("Location: (" + daliMember.getLat_long()[0] + ", " + daliMember.getLat_long()[1] + ")");
        termsOnTextView = findViewById(R.id.terms_on);
        String termsOnText = Arrays.toString(daliMember.getTerms_on()).substring(1, Arrays.toString(daliMember.getTerms_on()).length() - 1);
        termsOnTextView.setText(termsOnText);
        projectsTextView = findViewById(R.id.projects);
        String projectsText = Arrays.toString(daliMember.getProject()).substring(1, Arrays.toString(daliMember.getProject()).length() - 1);
        if (projectsText.trim().isEmpty())
            projectsText = "None";
        projectsTextView.setText(projectsText);
        builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        customTabsIntent = builder.build();
        context = this;
    }

    /**
     * Get the DALI Member's image from the image filename and display it with a frame that has rounded corners
     */
    public void GetMemberImage() {
        frameImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.frame), imageSize, imageSize, false);

        // 7 is the size of "images/" string. 7 is hardcoded for performance.
        File imageFile = new File(this.getFilesDir() + "/" + daliMember.getIconUrl().substring(7));
        if (imageFile.exists()) {
            Bitmap currentImage = BitmapFactory.decodeFile(imageFile.getPath());

            // Crop the image into a square if its height is greater than its width
            if (currentImage.getHeight() > currentImage.getWidth()) {
                currentImage = Bitmap.createBitmap(currentImage, 0, currentImage.getHeight() / 2 - currentImage.getWidth() / 2, currentImage.getWidth(), currentImage.getWidth());
            }
            memberImage = Bitmap.createScaledBitmap(currentImage, imageSize, imageSize, false);
        } else {

            // Show the DALI logo if the app has not downloaded an image yet
            memberImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.dali_logo), imageSize, imageSize, false);
        }
        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(memberImage);
        imageFrameView = findViewById(R.id.imageViewFrame);
        imageFrameView.setImageBitmap(frameImage);
    }

    /**
     * Starts the link to open a Chrome Custom Tab
     * @param view
     */
    public void OpenWebsite(View view) {
        customTabsIntent.launchUrl(context, Uri.parse("http://" + url));
    }

    /**
     * Starts a Map Activity displaying one DALI Member
     * @param view
     */
    public void OpenMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("memberNames", new String[]{daliMember.getName()});
        intent.putExtra("memberLocations", daliMember.getLat_long());
        intent.putExtra("title", daliMember.getName() + "'s Location");
        intent.putExtra("center", true);
        this.startActivity(intent);
    }
}
