package com.example.android.githubdemoapp.remote;

import android.os.Looper;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
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

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class GitHubRemote implements ApiInterface {

    private ApolloClient mApolloClient;
    private static GitHubRemote sInstance;

    public static final String TAG = ApiInterface.class.getName();

    private GitHubRemote(ApolloClient mApolloClient) {
        this.mApolloClient = mApolloClient;
    }

    public static GitHubRemote getGitHubRemote(ApolloClient apolloClient) {
        if (sInstance == null) {
            sInstance = new GitHubRemote(apolloClient);
        }
        return sInstance;
    }

    private Observable<Response<GitHubOrganizationQuery.Data>> getOrgRepoAllData(String orgname) throws ApolloException {

        return Observable.generate(() -> (getOrgRepoIdsAndLanguages(orgname, null)),
                (Response<GitHubOrganizationQuery.Data> previousPage, Emitter<Response<GitHubOrganizationQuery.Data>> responseEmitter) -> {
                    if (previousPage.hasErrors()) {
                        // Todo handle error
                        // if error
                        Log.e(TAG, "!!!Error is " + previousPage.errors().toString());
                        responseEmitter.onComplete();
                    }
                    if (previousPage.data() == null) {
                        Log.e(TAG, "NULL response.data()");
                        responseEmitter.onComplete();
                    }
                    GitHubOrganizationQuery.Organization user = previousPage.data().organization();

                    GitHubOrganizationQuery.Repositories repositories = user.repositories();

                    Log.d(TAG, "Page Repo Count -> " + repositories.edges().size());



                    if (repositories.edges().size() > 0) {
                        int last = repositories.edges().size() - 1;
                        GitHubOrganizationQuery.Edge edge = repositories.edges().get(last);
                        Log.d(TAG, "Cursor - >" + edge.cursor());
                        Response<GitHubOrganizationQuery.Data> result = getOrgRepoIdsAndLanguages(orgname, edge.cursor());

                        if (result.hasErrors() || !repositories.pageInfo().hasNextPage()) {
                            responseEmitter.onComplete();
                        }
                        responseEmitter.onNext(result);
                        return result;
                    } else {
                        Log.d(TAG, "Else !! ");
//                        Todo Handle no repositories for user
                        return null;
                    }
                });
    }

    @Override
    public Observable<HashMap<String, List<Repo>>> getOrgRepoIdsAndLanguages(String orgname) {
        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        Log.d(TAG, "IsMainThread " + String.valueOf(isMainThread));

        Observable<HashMap<String, List<Repo>>> map = null;
        try {
            map = getOrgRepoAllData(orgname).map(new Function<Response<GitHubOrganizationQuery.Data>, HashMap<String, List<Repo>>>() {
                @Override
                public HashMap<String, List<Repo>> apply(Response<GitHubOrganizationQuery.Data> dataResponse) throws Exception {
                    if (dataResponse.hasErrors()) {
                        // Todo handle error
                        // if error
                        Log.e(TAG, "Error is " + dataResponse.errors().toString());
                        return null;
                    }
                    if (dataResponse.data() == null) {
                        Log.e(TAG, "NULL response.data()");
                        return null;
                    }

                    // Todo check for errors!
                    HashMap<String, List<Repo>> languageRepoListMap = new HashMap<>();
                    GitHubOrganizationQuery.Organization org = dataResponse.data().organization();
                    GitHubOrganizationQuery.Repositories repositories = org.repositories();

                    if (repositories.edges() == null) {
                        // Todo check when nodes are null.
                        Log.w(TAG, "Repositories Null");
                        return null;
                    }
                    if (repositories.edges().size() == 0) {
                        Log.d(TAG, "No Repos");
                    }

                    Log.d(TAG, "Repo Count -> " + repositories.edges().size());
                    for (GitHubOrganizationQuery.Edge edge : repositories.edges()) {
                        GitHubOrganizationQuery.Node node = edge.node();
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

                    return languageRepoListMap;
                }
            });
        } catch (ApolloException e) {
            e.printStackTrace();
        }
        return map;
    }


    private Response<GitHubOrganizationQuery.Data> getOrgRepoIdsAndLanguages(String orgname, String after) throws ApolloException {


        return mApolloClient.query(GitHubOrganizationQuery.builder()
                .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                .org(orgname)
                .after(after)
                .build()).execute();


    }

    private Observable<Response<GitHubUsersQuery.Data>> getUserRepoAllData(String username) throws ApolloException {

        return Observable.generate(() -> (getUserRepoIdsAndLanguages(username, null)),
                (Response<GitHubUsersQuery.Data> previousPage, Emitter<Response<GitHubUsersQuery.Data>> responseEmitter) -> {
                    if (previousPage.hasErrors()) {
                        // Todo handle error
                        // if error
                        Log.e(TAG, "!!!Error is " + previousPage.errors().toString());
                        responseEmitter.onComplete();
                    }
                    if (previousPage.data() == null) {
                        Log.e(TAG, "NULL response.data()");
                        responseEmitter.onComplete();
                    }
                    GitHubUsersQuery.User user = previousPage.data().user();

                    GitHubUsersQuery.Repositories repositories = user.repositories();

                    Log.d(TAG, "Page Repo Count -> " + repositories.edges().size());



                    if (repositories.edges().size() > 0) {
                        int last = repositories.edges().size() - 1;
                        GitHubUsersQuery.Edge edge = repositories.edges().get(last);
                        Log.d(TAG, "Cursor - >" + edge.cursor());
                        Response<GitHubUsersQuery.Data> result = getUserRepoIdsAndLanguages(username, edge.cursor());

                        if (result.hasErrors() || !repositories.pageInfo().hasNextPage()) {
                            responseEmitter.onComplete();
                        }
                        responseEmitter.onNext(result);
                        return result;
                    } else {
                        Log.d(TAG, "Else !! ");
//                        Todo Handle no repositories for user
                        return null;
                    }
                });
    }

    @Override
    public Observable<HashMap<String, List<Repo>>> getUserRepoIdsAndLanguages(String username) {
        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        Log.d(TAG, "IsMainThread " + String.valueOf(isMainThread));

        Observable<HashMap<String, List<Repo>>> map = null;
        try {
            map = getUserRepoAllData(username).map(new Function<Response<GitHubUsersQuery.Data>, HashMap<String, List<Repo>>>() {
                @Override
                public HashMap<String, List<Repo>> apply(Response<GitHubUsersQuery.Data> dataResponse) throws Exception {
                    if (dataResponse.hasErrors()) {
                        // Todo handle error
                        // if error
                        Log.e(TAG, "Error is " + dataResponse.errors().toString());
                        return null;
                    }
                    if (dataResponse.data() == null) {
                        Log.e(TAG, "NULL response.data()");
                        return null;
                    }

                    // Todo check for errors!
                    HashMap<String, List<Repo>> languageRepoListMap = new HashMap<>();
                    GitHubUsersQuery.User user = dataResponse.data().user();
                    GitHubUsersQuery.Repositories repositories = user.repositories();

                    if (repositories.edges() == null) {
                        // Todo check when nodes are null.
                        Log.w(TAG, "Repositories Null");
                        return null;
                    }
                    if (repositories.edges().size() == 0) {
                        Log.d(TAG, "No Repos");
                    }

                    Log.d(TAG, "Repo Count -> " + repositories.edges().size());
                    for (GitHubUsersQuery.Edge edge : repositories.edges()) {
                        GitHubUsersQuery.Node node = edge.node();
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

                    return languageRepoListMap;
                }
            });
        } catch (ApolloException e) {
            e.printStackTrace();
        }
        return map;
    }


    private Response<GitHubUsersQuery.Data> getUserRepoIdsAndLanguages(String username, String after) throws ApolloException {


       return mApolloClient.query(GitHubUsersQuery.builder()
                .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                .user(username)
                .after(after)
                .build()).execute();


    }
}
