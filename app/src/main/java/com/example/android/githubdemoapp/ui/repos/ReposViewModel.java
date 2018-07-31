package com.example.android.githubdemoapp.ui.repos;

import android.app.Application;
import android.arch.lifecycle.ViewModel;

import com.example.android.githubdemoapp.model.Repo;
import com.example.android.githubdemoapp.repository.RepoService;
import com.example.android.githubdemoapp.util.SortUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ReposViewModel extends ViewModel{
    private Application mApplication;
    private RepoService mRepoService;
    private Observable<SortedMap<String, Repo>> mSortedRepoMap;

    public ReposViewModel(Application application, RepoService repoService) {
        this.mApplication = application;
        this.mRepoService = repoService;
    }

    public void getSortedRepos(Observable<SortedMap<String, Repo>> sortedRepoMap) {

    }


    public Observable<Map<String, List<Repo>>> getUserRepoIdProgLangs(String username) {
        return mRepoService.getUserRepos(username, true)
                .map(new Function<HashMap<String, List<Repo>>, Map<String, List<Repo>>>() {
            @Override
            public Map<String, List<Repo>> apply(HashMap<String, List<Repo>> stringListHashMap) throws Exception {
//                        Log.d(TAG, "Sorting Thread is Main "+String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
                return SortUtils.sortProgLangByRepoCount(stringListHashMap, false);
            }
        });

    }

    public Observable<Map<String, List<Repo>>> getOrgRepoIdProgLangs(String orgname) {
        return mRepoService.getOrgRepos(orgname, true)
                .map(new Function<HashMap<String, List<Repo>>, Map<String, List<Repo>>>() {
                    @Override
                    public Map<String, List<Repo>> apply(HashMap<String, List<Repo>> stringListHashMap) throws Exception {
//                        Log.d(TAG, "Sorting Thread is Main "+String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
                        return SortUtils.sortProgLangByRepoCount(stringListHashMap, false);
                    }
                });

    }
}
