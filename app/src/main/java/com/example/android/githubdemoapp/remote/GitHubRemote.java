package com.example.android.githubdemoapp.remote;

import android.database.Cursor;
import android.os.Looper;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.exception.ApolloNetworkException;
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
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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




    private Response<GitHubOrganizationQuery.Data> getOrgRepoIdsAndLanguages(String orgname, String after) throws ApolloException {
        Response<GitHubOrganizationQuery.Data> response = mApolloClient.query(GitHubOrganizationQuery.builder()
                .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                .org(orgname)
                .after(after)
                .build()).execute();


        return response;

    }

    @Override
    public Observable<List<Response<GitHubOrganizationQuery.Data>>> getOrgRepos(String orgname) {

        return Observable.create(new ObservableOnSubscribe<List<Response<GitHubOrganizationQuery.Data>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Response<GitHubOrganizationQuery.Data>>> emitter) throws Exception {
                List<Response<GitHubOrganizationQuery.Data>> githubResponses = new ArrayList<>();
                Log.d(TAG, "Starting fetch");
                String cursor = null;
                boolean hasNextPage = false;

                do {

                    Response<GitHubOrganizationQuery.Data> response;
                    try {
                        response = getOrgRepoIdsAndLanguages(orgname, cursor);
                    } catch (Exception e) {
                        if (emitter != null && !emitter.isDisposed()) {
                            emitter.onError(e);
                            Log.e(TAG, e.toString());
                        }
                        return;
                    }
                    if(!response.hasErrors()) {
                        // Add response to the list
                        githubResponses.add(response);

                        // Find the cursor
                        GitHubOrganizationQuery.Repositories repositories = response.data().organization().repositories();
                        if(repositories.edges().size() <= 0 ) {
                            break;
                        }
                        Log.d(TAG, "Size ="+repositories.edges().size());
                        int last = repositories.edges().size() - 1;
                        GitHubOrganizationQuery.Edge edge = repositories.edges().get(last);
                        cursor =  edge.cursor();


                        hasNextPage = repositories.pageInfo().hasNextPage();

                    } else {

                        emitter.onError(new Throwable(getErrorMessage(orgname, response.errors())));
                    }

                } while (hasNextPage);
                if(!emitter.isDisposed()) {
                    emitter.onNext(githubResponses);
                    emitter.onComplete();
                } else {
                    Log.w(TAG, "Emitter is Disposed");
                }

            }
        });

    }


    private String getErrorMessage(String entityName, List<Error> errorList) {
        Log.e(TAG, "RESPONSE ERROR -> " + errorList.toString());
        String errorString ;
        if(errorList.size() == 1 && (errorList.get(0).customAttributes().get("type").equals("NOT_FOUND"))) {
            String userType = errorList.get(0).customAttributes().get("path").toString();
            errorString = "No "+userType+" found with name "+entityName;
        } else {
            errorString = "Something went wrong :"+errorList.toString();
        }
        return errorString;
    }

    private Response<GitHubUsersQuery.Data> getUserRepoIdsAndLanguages(String username, String after) throws ApolloException {
       Response<GitHubUsersQuery.Data> response = mApolloClient.query(GitHubUsersQuery.builder()
                .first(Constants.GITHUB_REPOS_FETCH_ONE_GO_COUNT)
                .user(username)
                .after(after)
                .build()).execute();


        return response;

    }

    @Override
    public Observable<List<Response<GitHubUsersQuery.Data>>> getUserRepos(String username) {

        return Observable.create(new ObservableOnSubscribe<List<Response<GitHubUsersQuery.Data>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Response<GitHubUsersQuery.Data>>> emitter) {
                List<Response<GitHubUsersQuery.Data>> githubResponses = new ArrayList<>();

                    String cursor = null;
                    boolean hasNextPage = false;

                    do {


                        Response<GitHubUsersQuery.Data> response = null;

                        try {
                            response = getUserRepoIdsAndLanguages(username, cursor);
                        } catch (Exception e) {
                            if(emitter !=  null && !emitter.isDisposed()) {
                                emitter.onError(e);
                                Log.e(TAG, e.toString());
                            }

                            return;
                        }

                        if(!response.hasErrors()) {
                            // Add response to the list
                            githubResponses.add(response);

                            // Find the cursor
                            GitHubUsersQuery.Repositories repositories = response.data().user().repositories();
                            if(repositories.edges().size() <= 0 ) {
                                break;
                            }
                            int last = repositories.edges().size() - 1;
                            GitHubUsersQuery.Edge edge = repositories.edges().get(last);
                            cursor =  edge.cursor();


                            hasNextPage = repositories.pageInfo().hasNextPage();

                        } else {
                            emitter.onError(new Throwable(getErrorMessage(username, response.errors())));
                        }

                    } while (hasNextPage);
                if(!emitter.isDisposed()) {
                    emitter.onNext(githubResponses);
                    emitter.onComplete();
                } else {
                    Log.w(TAG, "Emitter is Disposed");
                }

                }
        });

    }
}
