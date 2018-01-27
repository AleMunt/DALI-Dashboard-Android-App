package com.almunt.dalidashboard;

public class DALIMember {

    public String name, iconUrl, url, lat_long, message, terms_on, project;

    public DALIMember (String name, String iconUrl, String url, String message, String lat_long, String terms_on, String project) {

        this.name=name;
        this.iconUrl=iconUrl;
        this.url=url;
        this.message=message;
        this.lat_long=lat_long;
        this.terms_on=terms_on;
        this.project=project;
    }
}
