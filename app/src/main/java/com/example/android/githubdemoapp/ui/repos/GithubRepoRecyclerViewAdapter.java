package com.example.android.githubdemoapp.ui.repos;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.githubdemoapp.R;
import com.example.android.githubdemoapp.model.Repo;
import com.example.android.githubdemoapp.ui.GithubDemoAppNavigotor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class GithubRepoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Map<String, List<Repo>> repos = Collections.EMPTY_MAP;
    private GithubDemoAppNavigotor navigator;

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 2;

    private SparseArray<Object> headerArray;
    private int size;

    public static final String TAG = GithubRepoRecyclerViewAdapter.class.getSimpleName();


    public GithubRepoRecyclerViewAdapter(GithubDemoAppNavigotor navigator) {
        this.navigator = navigator;
    }

    public void setRepos(Map<String, List<Repo>> repos) {
        this.repos = repos;
        setupHeaderPosition(repos);
        this.notifyDataSetChanged();
    }

    private void setupHeaderPosition(Map<String, List<Repo>> repos) {
        if(repos.keySet().isEmpty()) {
            return;
        }

        headerArray = new SparseArray<>();

        int lastHeaderIndex = 0;

        final Iterator<Map.Entry<String, List<Repo>>> iterator = repos.entrySet().iterator();
        do {
            Map.Entry<String, List<Repo>> mapEntry = iterator.next();
            ListIterator<Repo> repoIterator = mapEntry.getValue().listIterator();

//            Log.e(TAG, "Header Index - "+lastHeaderIndex);

            while (repoIterator.hasNext()) {
//                Log.e(TAG, String.valueOf(repoIterator.nextIndex() + lastHeaderIndex + 1));
                headerArray.append(repoIterator.nextIndex() + 1 + lastHeaderIndex,repoIterator.next());
            }

//            Log.e(TAG, "----");
            int size  = mapEntry.getValue().size();
            headerArray.put(lastHeaderIndex, mapEntry.getKey());
            lastHeaderIndex += size +1 ;

        } while (iterator.hasNext());

        this.size = lastHeaderIndex;
//        Log.e(TAG, String.valueOf(this.size));

    }

    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private boolean isHeader(int position) {
        Object object = headerArray.get(position);
        if(object instanceof Repo) {
            return false;
        }
        return true;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            final View headerView = layoutInflater.inflate(R.layout.item_github_language_section_header, parent, false);
            return new RepoHeaderViewHolder(headerView);
        } else {
            final View itemView = layoutInflater.inflate(R.layout.item_github_repo_entry, parent, false);
            return new RepoItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RepoHeaderViewHolder) {
            // handle header
            ((RepoHeaderViewHolder) holder).setRepoHeader((String) headerArray.get(position));
        } else if (holder instanceof RepoItemViewHolder) {
            // handle item
            final Repo repo = (Repo) this.headerArray.get(position);
            ((RepoItemViewHolder) holder).setRepoItem(repo, navigator);
        }
    }

    @Override
    public int getItemCount() {
        return size;
    }

    static class RepoItemViewHolder extends RecyclerView.ViewHolder {

        private TextView repositoryTitle;
        private TextView repositoryStars;
        private  View repoItemContainer;

        RepoItemViewHolder(View itemView) {
            super(itemView);
            repositoryTitle = itemView.findViewById(R.id.tv_repo_name);
            repositoryStars = itemView.findViewById(R.id.tv_repo_star);
            repoItemContainer = itemView.findViewById(R.id.repo_item_container);
        }

        void setRepoItem(Repo repoItem, final GithubDemoAppNavigotor navigator) {
            repositoryTitle.setText(repoItem.getName());
            repositoryStars.setText(String.valueOf(repoItem.getStars()));
            repoItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigator.startGithubDetailActivity(repoItem.getOwner(), repoItem.getName());
                }
            });
        }
    }

    static class RepoHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView repositoryHeader;

        RepoHeaderViewHolder(View itemView) {
            super(itemView);
            repositoryHeader = itemView.findViewById(R.id.tv_section_prog_lang);
        }

        void setRepoHeader(String header) {
            repositoryHeader.setText(header);
        }
    }

}
