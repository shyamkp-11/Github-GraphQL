package com.example.android.githubdemoapp.ui.repos;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.api.Response;
import com.example.android.githubdemoapp.ApiClient;
import com.example.android.githubdemoapp.Constants;
import com.example.android.githubdemoapp.R;
import com.example.android.githubdemoapp.api.GitHubOrganizationQuery;
import com.example.android.githubdemoapp.api.GitHubUsersQuery;
import com.example.android.githubdemoapp.model.Repo;
import com.example.android.githubdemoapp.remote.GitHubRemote;
import com.example.android.githubdemoapp.repository.RepoService;
import com.example.android.githubdemoapp.ui.GithubDemoAppNavigotor;
import com.example.android.githubdemoapp.ui.repodetails.RepoDetailsActivity;
import com.example.android.githubdemoapp.util.SortUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements GithubDemoAppNavigotor {

    public static final String TAG = MainActivity.class.getName();

    ViewGroup content;
    ProgressBar progressBar;
    GithubRepoRecyclerViewAdapter repoAdapter;
    Disposable sub;

    boolean isSearchForUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        content = (ViewGroup) findViewById(R.id.content_holder);
        progressBar = (ProgressBar) findViewById(R.id.loading_bar);

        RecyclerView repoRecyclerView = (RecyclerView) findViewById(R.id.rv_repo_list);
        repoAdapter = new GithubRepoRecyclerViewAdapter(this);
        repoRecyclerView.setAdapter(repoAdapter);
        repoRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        EditText searchEditText = findViewById(R.id.et_search_name);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    searchEditText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    progressBar.setVisibility(View.VISIBLE);
                    fetchQueries(searchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }


    private void fetchQueries(String searchString) {


        unsub();
        RepoService repoService = RepoService.getRespoService(ApiClient.getOkHttpClient(),
                ApiClient.getApolloClient(),
                GitHubRemote.getGitHubRemote(ApiClient.getApolloClient()));

        Observable<HashMap<String, List<Repo>>> observable;

        if(isSearchForUser) {
             observable = repoService.getUserRepos(searchString, true)
                    .subscribeOn(Schedulers.io())
                    .map(userMap)
                    .map((Function<HashMap<String, List<Repo>>, HashMap<String, List<Repo>>>) stringListHashMap -> SortUtils.sortProgLangByRepoCount(stringListHashMap, false))
                    .observeOn(AndroidSchedulers.mainThread());


        } else {
            observable = repoService.getOrgRepos(searchString, true)
                    .subscribeOn(Schedulers.io())
                    .map(orgMap)
                    .map((Function<HashMap<String, List<Repo>>, HashMap<String, List<Repo>>>) stringListHashMap -> SortUtils.sortProgLangByRepoCount(stringListHashMap, false))
                    .observeOn(AndroidSchedulers.mainThread());
        }

            observable.subscribe(new Observer<HashMap<String,List<Repo>>>() {

                @Override
                public void onSubscribe(Disposable d) {
                    sub = d;
                }

                @Override
            public void onNext(HashMap<String, List<Repo>> progLangRepoMap) {
                progressBar.setVisibility(View.GONE);

                if(progLangRepoMap.size() == 0) {
                    Toast.makeText(MainActivity.this, "No Repository for the user",Toast.LENGTH_SHORT ).show();
                }

                repoAdapter.setRepos(progLangRepoMap);

//                for (Map.Entry<String, List<Repo>> entry : progLangRepoMap.entrySet()) {
////                    String idStr = "";
////                    for (Repo repo : entry.getValue()) {
////                        if (entry.getValue() != null) {
////                            idStr += ", " + repo.getName() + (" stars - ") + repo.getStars();
////                        }
////                    }
////                    Log.d(TAG, "Lang: " + entry.getKey() + "\t" + idStr);
////                }
            }

            @Override
            public void onError(Throwable e) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, e.toString());
                Toast.makeText(MainActivity.this, e.getMessage(),Toast.LENGTH_LONG ).show();
                unsub();
            }

            @Override
            public void onComplete() {

            }
        });


//        this.disposable.add(observer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unsub();
        progressBar.setVisibility(View.GONE);
    }

    public void unsub() {
        if (sub != null && !sub.isDisposed()) {
            sub.dispose();
        }
    }

    @Override
    public void startGithubDetailActivity(String owner, String repoFullName) {
        final Intent intent = RepoDetailsActivity.newIntent(this, owner, repoFullName);
        startActivity(intent);
    }

    Function<List<Response<GitHubOrganizationQuery.Data>>, HashMap<String, List<Repo>>> orgMap = new Function<List<Response<GitHubOrganizationQuery.Data>>, HashMap<String, List<Repo>>>() {

        @Override
        public HashMap<String, List<Repo>> apply(List<Response<GitHubOrganizationQuery.Data>> responses) throws Exception {
            HashMap<String, List<Repo>> progLangRepoMap = new HashMap<>();

            for (Response<GitHubOrganizationQuery.Data> response : responses) {

                GitHubOrganizationQuery.Organization organization = response.data().organization();
                GitHubOrganizationQuery.Repositories repositories = organization.repositories();

                if (repositories.edges() == null) {
                    // Todo check when nodes are null.
                    Log.w(TAG, "Repositories Null");
                }
                if (repositories.edges().size() == 0) {
                    Log.d(TAG, "No Repos");
                }

//                Log.d(TAG, "Repo Count -> " + repositories.edges().size());
                for (GitHubOrganizationQuery.Edge edge : repositories.edges()) {
                    GitHubOrganizationQuery.Node node = edge.node();
//                    Log.d(TAG, "Node owner: " + node.owner().login()
//                            + "Languages(count): " + node.languages().totalCount());
                    if (node.languages() == null || node.languages().totalCount() < 1) {
                        List<Repo> reposIds = progLangRepoMap.get(Constants.GITHUB_NO_LANGUAGE_MAP_KEY);
                        Repo repo = new Repo(node.owner().login(),
                                node.name(),
                                node.stargazers().totalCount(),
                                null);
                        if (reposIds == null) {
                            reposIds = new ArrayList<>();
                            progLangRepoMap.put(Constants.GITHUB_NO_LANGUAGE_MAP_KEY, reposIds);
                        }
                        reposIds.add(repo);
                    } else {
                        List<GitHubOrganizationQuery.Node1> node1s = node.languages().nodes();
                        Set<String> progLangSet = new HashSet<>(node1s.size());
                        Repo repo = new Repo(node.owner().login(),
                                node.name(),
                                node.stargazers().totalCount(),
                                progLangSet);
                        for (GitHubOrganizationQuery.Node1 progLang : node1s) {
                            String langName = progLang.name();
                            progLangSet.add(langName);
                            List<Repo> reposIds = progLangRepoMap.get(langName);
                            if (reposIds == null) {
                                reposIds = new ArrayList<>();
                                progLangRepoMap.put(langName, reposIds);
                            }
                            reposIds.add(repo);
                        }
                    }
                }

            }
            return progLangRepoMap;
        }
    };


    Function<List<Response<GitHubUsersQuery.Data>>, HashMap<String, List<Repo>>> userMap = new Function<List<Response<GitHubUsersQuery.Data>>, HashMap<String, List<Repo>>>() {

        @Override
        public HashMap<String, List<Repo>> apply(List<Response<GitHubUsersQuery.Data>> responses) throws Exception {
            HashMap<String, List<Repo>> progLangRepoMap = new HashMap<>();

            for (Response<GitHubUsersQuery.Data> response : responses) {

                GitHubUsersQuery.User user = response.data().user();
                GitHubUsersQuery.Repositories repositories = user.repositories();

                if (repositories.edges() == null) {
                    // Todo check when nodes are null.
                    Log.w(TAG, "Repositories Null");
                }
                if (repositories.edges().size() == 0) {
                    Log.d(TAG, "No Repos");
                }

//                Log.d(TAG, "Repo Count -> " + repositories.edges().size());
                for (GitHubUsersQuery.Edge edge : repositories.edges()) {
                    GitHubUsersQuery.Node node = edge.node();
//                    Log.d(TAG, "Node id: " + node.owner().login()
//                            + "Languages(count): " + node.languages().totalCount());
                    if (node.languages() == null || node.languages().totalCount() < 1) {
                        List<Repo> reposIds = progLangRepoMap.get(Constants.GITHUB_NO_LANGUAGE_MAP_KEY);
                        Repo repo = new Repo(node.owner().login(),
                                node.name(),
                                node.stargazers().totalCount(),
                                null);
                        if (reposIds == null) {
                            reposIds = new ArrayList<>();
                            progLangRepoMap.put(Constants.GITHUB_NO_LANGUAGE_MAP_KEY, reposIds);
                        }
                        reposIds.add(repo);
                    } else {
                        List<GitHubUsersQuery.Node1> node1s = node.languages().nodes();
                        Set<String> progLangSet = new HashSet<>(node1s.size());
                        Repo repo = new Repo(node.owner().login(),
                                node.name(),
                                node.stargazers().totalCount(),
                                progLangSet);
                        for (GitHubUsersQuery.Node1 progLang : node1s) {
                            String langName = progLang.name();
                            progLangSet.add(langName);
                            List<Repo> reposIds = progLangRepoMap.get(langName);
                            if (reposIds == null) {
                                reposIds = new ArrayList<>();
                                progLangRepoMap.put(langName, reposIds);
                            }
                            reposIds.add(repo);
                        }
                    }
                }

            }
            return progLangRepoMap;
        }
    };


    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_user:
                if (checked)
                    isSearchForUser = true;
                    break;
            case R.id.radio_organization:
                if (checked)
                    isSearchForUser = false;
                    break;
        }
    }

}
