package com.example.android.githubdemoapp.util;

import android.support.annotation.NonNull;

import com.example.android.githubdemoapp.model.Repo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SortUtils {

    public static LinkedHashMap<String, List<Repo>> sortProgLangByRepoCount(@NonNull HashMap<String, List<Repo>> progLangRepoListMap, final boolean isProgLanOrderIncreasing) {

        LinkedHashMap<String, List<Repo>> sortedProgLangListMap = new LinkedHashMap<>(progLangRepoListMap.size());

        List<Map.Entry<String, List<Repo>>> list = new ArrayList<>(progLangRepoListMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, List<Repo>>>() {
            @Override
            public int compare(Map.Entry<String, List<Repo>> o1,
                               Map.Entry<String, List<Repo>> o2) {
                if (!isProgLanOrderIncreasing) {
                    return o2.getValue().size() - o1.getValue().size();
                } else {
                    return o1.getValue().size() - o2.getValue().size();
                }
            }
        });

        for (Map.Entry<String, List<Repo>> entry : list)
        {   List<Repo> repos = entry.getValue();
            Collections.sort(repos, new Comparator<Repo>() {
                @Override
                public int compare(Repo repo, Repo t1) {
                    return t1.getStars() - repo.getStars();
                }
            });

            sortedProgLangListMap.put(entry.getKey(), entry.getValue());
        }


        return sortedProgLangListMap;
    }
}
