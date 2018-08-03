package com.example.android.githubdemoapp.model;


import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.android.githubdemoapp.api.GitHubOrganizationQuery;
import com.example.android.githubdemoapp.api.GitHubUsersQuery;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;


public interface ApiInterface {




    Observable<List<Response<GitHubOrganizationQuery.Data>>> getOrgRepos(String orgname);

    Observable<List<Response<GitHubUsersQuery.Data>>> getUserRepos(String username);
}
