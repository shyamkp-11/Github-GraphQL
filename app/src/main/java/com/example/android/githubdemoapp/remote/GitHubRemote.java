package com.example.android.githubdemoapp.remote;

import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.example.android.githubdemoapp.Constants;
import com.example.android.githubdemoapp.api.GitHubOrganizationQuery;
import com.example.android.githubdemoapp.api.GitHubUsersQuery;
import com.example.android.githubdemoapp.model.ApiInterface;
import com.example.android.githubdemoapp.model.Repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class GitHubRemote implements ApiInterface{

    private ApolloClient mApolloClient;
    private static GitHubRemote sInstance;

    public static final String TAG = ApiInterface.class.getName();

    private GitHubRemote(ApolloClient mApolloClient) {
        this.mApolloClient = mApolloClient;
    }

    public static GitHubRemote getGitHubRemote(ApolloClient apolloClient) {
        if(sInstance == null) {
            sInstance = new GitHubRemote(apolloClient);
        }
        return sInstance;
    }

    @Override
    public Observable<HashMap<String, List<Repo>>> getOrgRepoIdsAndLanguages(String orgname) {
        Observable<Response<GitHubOrganizationQuery.Data>> observable = Rx2Apollo.from(mApolloClient.query(
                GitHubOrganizationQuery.builder()
                        .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                        .org(orgname)
                        .after(null)
                        .build()));

        return observable.map(new Function<Response<GitHubOrganizationQuery.Data>, HashMap<String, List<Repo>>>() {
            @Override
            public HashMap<String, List<Repo>> apply(Response<GitHubOrganizationQuery.Data> dataResponse) throws Exception {
                if (dataResponse.hasErrors()) {
                    // Todo handle error
                    // if error
                    Log.e(TAG, "Error is "+ dataResponse.errors().toString());
                    return null;
                }
                if (dataResponse.data() == null) {
                    Log.e(TAG, "NULL response.data()");
                    return null;
                }
                // Todo check for errors!
                HashMap<String, List<Repo>> languageRepoListMap = new HashMap<>();
                GitHubOrganizationQuery.Organization organization = dataResponse.data().organization();
                if (organization != null) {
                    GitHubOrganizationQuery.Repositories repositories = organization.repositories();

                    if (repositories.nodes() == null) {
                        // Todo check when nodes are null.
                        Log.w(TAG, "Repositories Null");
                        return null;
                    }
                    if(repositories.nodes().size() == 0) {
                        Log.d(TAG,"No Repos");
                    }

                    for (GitHubOrganizationQuery.Node node : repositories.nodes()) {
                        Log.d(TAG, "Node id: " + node.id()
                                + "Languages(count): " + node.languages().totalCount());
                        if (node.languages() == null || node.languages().totalCount() < 1) {
                            List<Repo> reposIds = languageRepoListMap.get(Constants.GITHUB_NO_LANGUAGE_MAP_KEY);
                            Repo repo = new Repo(node.id(),
                                    node.name(),
                                    node.stargazers().totalCount(),
                                    null);
                            if (reposIds == null) {
                                reposIds = new ArrayList<>();
                                languageRepoListMap.put(Constants.GITHUB_NO_LANGUAGE_MAP_KEY, reposIds);
                            }
                            reposIds.add(repo);
                        } else {
                            List<GitHubOrganizationQuery.Node1> node1s = node.languages().nodes();
                            Set<String> progLangSet = new HashSet<>(node1s.size());
                            Repo repo = new Repo(node.id(),
                                    node.name(),
                                    node.stargazers().totalCount(),
                                    progLangSet);
                            for (GitHubOrganizationQuery.Node1 progLang : node1s) {
                                String langName = progLang.name();
                                progLangSet.add(langName);
                                List<Repo> reposIds = languageRepoListMap.get(langName);
                                if (reposIds == null) {
                                    reposIds = new ArrayList<>();
                                    languageRepoListMap.put(langName, reposIds);
                                }
                                reposIds.add(repo);
                            }
                        }
                    }
                }
                return languageRepoListMap;
            }
        });
    }

    @Override
    public Observable<HashMap<String, List<Repo>>> getUserRepoIdsAndLanguages(String username) {
        Observable<Response<GitHubUsersQuery.Data>> observable = Rx2Apollo.from(mApolloClient.query(
                GitHubUsersQuery.builder()
                        .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                        .user(username)
                        .after(null)
                        .build()));

        return observable.map(new Function<Response<GitHubUsersQuery.Data>, HashMap<String, List<Repo>>>() {
            @Override
            public HashMap<String, List<Repo>> apply(Response<GitHubUsersQuery.Data> dataResponse) throws Exception {
                if (dataResponse.hasErrors()) {
                    // Todo handle error
                    // if error
                    Log.e(TAG, "Error is "+ dataResponse.errors().toString());
                    return null;
                }
                if (dataResponse.data() == null) {
                    Log.e(TAG, "NULL response.data()");
                    return null;
                }
                // Todo check for errors!
                HashMap<String, List<Repo>> languageRepoListMap = new HashMap<>();
                GitHubUsersQuery.User user = dataResponse.data().user();
                if (user != null) {
                    GitHubUsersQuery.Repositories repositories = user.repositories();

                    if (repositories.nodes() == null) {
                        // Todo check when nodes are null.
                        Log.w(TAG, "Repositories Null");
                        return null;
                    }
                    if(repositories.nodes().size() == 0) {
                        Log.d(TAG,"No Repos");
                    }

                    for (GitHubUsersQuery.Node node : repositories.nodes()) {
                        Log.d(TAG, "Node id: " + node.id()
                                + "Languages(count): " + node.languages().totalCount());
                        if (node.languages() == null || node.languages().totalCount() < 1) {
                            List<Repo> reposIds = languageRepoListMap.get(Constants.GITHUB_NO_LANGUAGE_MAP_KEY);
                            Repo repo = new Repo(node.id(),
                                    node.name(),
                                    node.stargazers().totalCount(),
                                    null);
                            if (reposIds == null) {
                                reposIds = new ArrayList<>();
                                languageRepoListMap.put(Constants.GITHUB_NO_LANGUAGE_MAP_KEY, reposIds);
                            }
                            reposIds.add(repo);
                        } else {
                            List<GitHubUsersQuery.Node1> node1s = node.languages().nodes();
                            Set<String> progLangSet = new HashSet<>(node1s.size());
                            Repo repo = new Repo(node.id(),
                                    node.name(),
                                    node.stargazers().totalCount(),
                                    progLangSet);
                            for (GitHubUsersQuery.Node1 progLang : node1s) {
                                String langName = progLang.name();
                                progLangSet.add(langName);
                                List<Repo> reposIds = languageRepoListMap.get(langName);
                                if (reposIds == null) {
                                    reposIds = new ArrayList<>();
                                    languageRepoListMap.put(langName, reposIds);
                                }
                                reposIds.add(repo);
                            }
                        }
                    }
                }
                return languageRepoListMap;
            }
        });
    }
}
