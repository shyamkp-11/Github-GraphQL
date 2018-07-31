package com.example.android.githubdemoapp.model;


import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;


public interface ApiInterface {


    Observable<HashMap<String, List<Repo>>> getOrgRepoIdsAndLanguages(String orgname);

    Observable<HashMap<String, List<Repo>>> getUserRepoIdsAndLanguages(String username);

}
