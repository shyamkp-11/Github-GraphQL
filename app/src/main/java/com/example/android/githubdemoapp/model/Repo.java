package com.example.android.githubdemoapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

public class Repo implements RepoIdAndLang{

    private String id;
    private String name;
    private int stars;
    @Nullable
    private Set<String> progLangs;

    public Repo(String id, String name, int stars, Set<String> progLangs) {
        this.id = id;
        this.name = name;
        this.stars = stars;
        this.progLangs = progLangs;
    }

    @Override
    public String toString() {
        return "Repo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", stars='" + stars + '\'' +
                ", progLangs=" + progLangs +
                '}';
    }

    @NonNull
    @Override
    public String getRepoId() {
        return id;
    }

    @NonNull
    @Override
    public Set<String> getProLangs() {
        return progLangs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStars() {
        return stars;
    }

    @Nullable
    public Set<String> getProgLangs() {
        return progLangs;
    }
}
