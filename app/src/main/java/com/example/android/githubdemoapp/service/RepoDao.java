package com.example.android.githubdemoapp.service;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.android.githubdemoapp.model.SimpleRepoModel;

import java.util.List;

@Dao
public abstract class RepoDao {

    @Query("select * from SimpleRepoModel")
    public abstract List<SimpleRepoModel> loadSimpleRepoModels();

    @Query("select * from SimpleRepoModel where SimpleRepoModel.repoName = :repoName")
    public abstract SimpleRepoModel loadSimpleRepoModel(String repoName);

    @Insert
    public abstract void insertSimpleRepoModel(SimpleRepoModel simpleRepoModel);

    @Query("DELETE FROM SimpleRepoModel")
    public abstract void clearTable();

}
