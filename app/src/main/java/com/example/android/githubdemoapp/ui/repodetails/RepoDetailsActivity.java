package com.example.android.githubdemoapp.ui.repodetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.example.android.githubdemoapp.ApiClient;
import com.example.android.githubdemoapp.R;
import com.example.android.githubdemoapp.api.GithubDetailQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RepoDetailsActivity extends AppCompatActivity{


    public static final String TAG = RepoDetailsActivity.class.getSimpleName();
    private static final String ARG_REPOSITORY_FULL_NAME = "arg_repo_full_name";
    private static final String ARG_OWNER_NAME = "arg_owner_name";

    ViewGroup content;
    ProgressBar progressBar;
    TextView description;
    TextView name;
    TextView createdAt;

    private String repoFullName;
    private String owner;

    private CompositeDisposable disposables = new CompositeDisposable();

    public static Intent newIntent(Context context, String ownerName, String repositoryFullName) {
        Intent intent = new Intent(context, RepoDetailsActivity.class);
        intent.putExtra(ARG_REPOSITORY_FULL_NAME, repositoryFullName);
        intent.putExtra(ARG_OWNER_NAME, ownerName);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_detail);
        repoFullName = getIntent().getStringExtra(ARG_REPOSITORY_FULL_NAME);
        owner = getIntent().getStringExtra(ARG_OWNER_NAME);
        content = (ViewGroup) findViewById(R.id.content_holder);
        progressBar = (ProgressBar) findViewById(R.id.loading_bar);
        name = (TextView) findViewById(R.id.tv_repo_name);
        description = (TextView) findViewById(R.id.tv_repository_description);
        createdAt = (TextView) findViewById(R.id.tv_created_at);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fetchRepositoryDetails();
    }

    private void setData(GithubDetailQuery.Data data) {
        content.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        final GithubDetailQuery.Repository repository = data.repository();
        if (repository != null) {
            name.setText(repository.name());
            description.setText(repository.description());
            DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ");
            Date date;
            try {
                date = formatter.parse(repository.createdAt().toString().replaceAll("Z$", "+0000"));
                Log.d(TAG, date.toString());
                DateFormat displayFormat = new SimpleDateFormat("MMMM dd yy h:mma", Locale.ENGLISH);
                createdAt.setText(displayFormat.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    private void fetchRepositoryDetails() {

        ApolloCall<GithubDetailQuery.Data> repoDetailQuery = ApiClient.getApolloClient()
                .query(new GithubDetailQuery(owner, repoFullName));

        //Example call using Rx2Support
        Rx2Apollo.from(repoDetailQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Response<GithubDetailQuery.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Response<GithubDetailQuery.Data> dataResponse) {
                        setData(dataResponse.data());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.dispose();
    }

}
