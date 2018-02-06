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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * An Activity for displaying a Google Map with markers
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    /**
     * The names and locations of the DALI memberNames
     */
    String[] memberNames;
    double[] memberLocations;
    /**
     * Is true if there should be a corrected center on the map
     */
    boolean center;

    /**
     * Starts the activity and retrieves a list of memberNames, their locations and if the map
     * should be centered.
     *
     * Also gives the activity a title in the toolbar
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Obtains DALI members names and locations from the intent
        memberNames = getIntent().getStringArrayExtra("memberNames");
        memberLocations = getIntent().getDoubleArrayExtra("memberLocations");
        center = getIntent().getBooleanExtra("center", false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("title"));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     *  Sets up the googleMap object with markers once it is ready
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        /**
         *  Add the latitudes and longitudes of everybody to the map.
         *  Move in twos in member locations as there is latitude and a longitude
         */
        for (int i = 0; i < memberNames.length; i++) {
            LatLng memberLatLng=new LatLng(memberLocations[i * 2], memberLocations[i * 2 + 1]);
            MarkerOptions memberMarker= new MarkerOptions().position(memberLatLng).title(memberNames[i]);
            googleMap.addMarker(memberMarker);
        }
        /**
         *  If there is a center then move map camera to the center.
         *  This also means that there is only one marker on the map.
         */
        if (center) {
            LatLng memberLatLng=new LatLng(memberLocations[0], memberLocations[1]);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(memberLatLng));
        }
    }
}