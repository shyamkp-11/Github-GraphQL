package com.example.android.githubdemoapp.ui.repos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.android.githubdemoapp.ApiClient;
import com.example.android.githubdemoapp.R;
import com.example.android.githubdemoapp.model.Repo;
import com.example.android.githubdemoapp.remote.GitHubRemote;
import com.example.android.githubdemoapp.repository.RepoService;

import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReposViewModel reposViewModel = new ReposViewModel(getApplication(),
                RepoService.getRespoService(ApiClient.getOkHttpClient(),
                        ApiClient.getApolloClient(),
                        GitHubRemote.getGitHubRemote(ApiClient.getApolloClient())));


        Disposable disposable = reposViewModel.getOrgRepoIdProgLangs("google" )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Map<String, List<Repo>>>() {
                    @Override
                    public void accept(Map<String, List<Repo>> stringListHashMap) throws Exception {
                        for (Map.Entry<String, List<Repo>> entry : stringListHashMap.entrySet()) {
                            Log.d(TAG, "Lang : " + entry.getKey());
                            String idStr = "";
                            for (Repo repo : entry.getValue()) {
                                if (entry.getValue() != null) {
                                    idStr += ", " + repo.getId() + (" stars - ")+ repo.getStars();
                                }
                            }
                            Log.d(TAG, "\t" + idStr);
                        }
                    }
                });
        this.disposable.add(disposable);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}
