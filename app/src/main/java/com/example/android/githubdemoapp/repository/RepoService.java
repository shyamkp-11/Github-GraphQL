package com.example.android.githubdemoapp.repository;

import android.support.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;
import com.example.android.githubdemoapp.model.Repo;
import com.example.android.githubdemoapp.model.ApiInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

public class RepoService {

    private OkHttpClient mOkHttpClient;
    private ApolloClient mApolloClient;
    private ApiInterface mApiInterface;
    private static RepoService instance;
    private Observable<HashMap<String, List<Repo>>> mObservable;

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



    public Observable<HashMap<String, List<Repo>>> getUserRepos(@NonNull String username, boolean forceReload) {
        // Todo differentiate mobservable
        if(mObservable != null && !forceReload) {
            return mObservable;
        }

        return mApiInterface.getUserRepoIdsAndLanguages(username);

    }

    public Observable<HashMap<String, List<Repo>>> getOrgRepos(@NonNull String orgname, boolean forceReload) {
        if(mObservable != null && !forceReload) {
            return mObservable;
        }

        return mApiInterface.getOrgRepoIdsAndLanguages(orgname);

    }

    public static String getStringFromRetrofitResponse(@NonNull ResponseBody responseBody) {

        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();


        reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return sb.toString();
    }
}
