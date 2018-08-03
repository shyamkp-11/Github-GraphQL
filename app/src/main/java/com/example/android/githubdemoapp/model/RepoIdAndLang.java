package com.example.android.githubdemoapp.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Set;

public interface RepoIdAndLang {

    @NonNull String getRepoId();
    @NonNull Set<String> getProLangs();
}
