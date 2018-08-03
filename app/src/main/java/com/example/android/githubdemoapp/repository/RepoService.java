package com.example.android.githubdemoapp.repository;

import android.support.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.example.android.githubdemoapp.api.GitHubOrganizationQuery;
import com.example.android.githubdemoapp.api.GitHubUsersQuery;
import com.example.android.githubdemoapp.model.ApiInterface;

import java.util.List;

import io.reactivex.Observable;

import okhttp3.OkHttpClient;

public class RepoService {

    private OkHttpClient mOkHttpClient;
    private ApolloClient mApolloClient;
    private ApiInterface mApiInterface;
    private static RepoService instance;

    private RepoService(OkHttpClient okHttpClient, ApolloClient apolloClient, ApiInterface apiInterface) {
        this.mOkHttpClient = okHttpClient;
        this.mApolloClient = apolloClient;

        this.mApiInterface = apiInterface;
    }

    public static RepoService getRespoService(OkHttpClient okHttpClient, ApolloClient apolloClient, ApiInterface apiInterface) {
        if (instance == null) {
            instance = new RepoService(okHttpClient, apolloClient, apiInterface);
        }
        return instance;
    }


    public Observable<List<Response<GitHubUsersQuery.Data>>> getUserRepos(@NonNull String username, boolean forceReload) {


        return mApiInterface.getUserRepos(username);


    }

    public Observable<List<Response<GitHubOrganizationQuery.Data>>> getOrgRepos(@NonNull String orgname, boolean forceReload) {

        return mApiInterface.getOrgRepos(orgname);

    }
}
