package com.example.android.githubdemoapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class SimpleRepoModel {

    @PrimaryKey
    @NonNull
    private String repoName;

    private String repoDetail;

    private String dateString;

    public SimpleRepoModel(String repoName, String repoDetail, String dateString) {
        this.repoName = repoName;
        this.repoDetail = repoDetail;
        this.dateString = dateString;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoDetail() {
        return repoDetail;
    }

    public void setRepoDetail(String repoDetail) {
        this.repoDetail = repoDetail;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
}
