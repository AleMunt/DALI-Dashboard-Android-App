/*
 * Copyright (C) 2017 The LineageOS Project
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

public class DALIMember {
    private String name;
    private String iconUrl;
    private String url;
    private String message;
    private double[] lat_long;
    private String[] terms_on;
    private String[] project;

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public double[] getLat_long() {
        return lat_long;
    }

    public String[] getTerms_on() {
        return terms_on;
    }

    public String[] getProject() {
        return project;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLat_long(double[] lat_long) {
        this.lat_long = lat_long;
    }

    public void setTerms_on(String[] terms_on) {
        this.terms_on = terms_on;
    }

    public void setProject(String[] project) {
        this.project = project;
    }
}