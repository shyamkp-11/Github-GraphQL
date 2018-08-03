package com.example.android.githubdemoapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

public class Repo implements RepoIdAndLang{

    private String name;
    private int stars;
    @Nullable
    private Set<String> progLangs;
    private String owner;

    public Repo(String owner, String name, int stars, Set<String> progLangs) {
        this.owner = owner;
        this.name = name;
        this.stars = stars;
        this.progLangs = progLangs;
    }

    @Override
    public String toString() {
        return "Repo{" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", stars='" + stars + '\'' +
                ", progLangs=" + progLangs +
                '}';
    }

    @NonNull
    @Override
    public String getRepoId() {
        return owner;
    }

    @NonNull
    @Override
    public Set<String> getProLangs() {
        return progLangs;
    }

    public String getOwner() {
        return owner;
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
