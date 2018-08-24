package com.example.android.githubdemoapp.ui.repodetails;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.example.android.githubdemoapp.ApiClient;
import com.example.android.githubdemoapp.R;
import com.example.android.githubdemoapp.api.GithubDetailQuery;
import com.example.android.githubdemoapp.db.AppDatabase;
import com.example.android.githubdemoapp.model.SimpleRepoModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    private AppDatabase mDatabase;

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

        mDatabase =  AppDatabase.getInstance(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        fetchRepositoryDetails();

        showDataFromDborFetchFromNetwork(false);
    }

    private void setData(SimpleRepoModel simpleRepoModel) {

        content.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        if (simpleRepoModel != null) {
            name.setText(simpleRepoModel.getRepoName());
            description.setText(simpleRepoModel.getRepoDetail());
            DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ");
            Date date;
            try {
                date = formatter.parse(simpleRepoModel.getDateString().replaceAll("Z$", "+0000"));
                Log.d(TAG, date.toString());
                DateFormat displayFormat = new SimpleDateFormat("MMMM dd yy h:mma", Locale.ENGLISH);
                createdAt.setText(displayFormat.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    private void insertDataToDb(GithubDetailQuery.Data data) {

        final GithubDetailQuery.Repository repository = data.repository();

        // insert in db
        SimpleRepoModel repoModel = new SimpleRepoModel(repoFullName, repository.description(), repository.createdAt().toString());

        mDatabase.repoDao().insertSimpleRepoModel(repoModel);

        Log.d(TAG, "Data inserted");
    }

    private SimpleRepoModel getSimpleRepoModelFromDb(String repoFullName) {

        return mDatabase.repoDao().loadSimpleRepoModel(repoFullName);
    }


    private void fetchRepositoryDetails() {

        ApolloCall<GithubDetailQuery.Data> repoDetailQuery = ApiClient.getApolloClient()
                .query(new GithubDetailQuery(owner, repoFullName));

        //Example call using Rx2Support
        Rx2Apollo.from(repoDetailQuery)
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<Response<GithubDetailQuery.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Response<GithubDetailQuery.Data> dataResponse) {
                        insertDataToDb(dataResponse.data());
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        showDataFromDborFetchFromNetwork(false);
                    }
                });

    }

    private void showDataFromDborFetchFromNetwork(boolean forceNetworkLoad) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                // check db for data
                SimpleRepoModel simpleRepoModel = getSimpleRepoModelFromDb(repoFullName);

                // if db doesnt have cached data then network fetch
                if(simpleRepoModel!=null && !forceNetworkLoad) {
                    // Show data in main thread
                    new Handler(getApplicationContext().getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // display data on mainthread
                              setData(simpleRepoModel);
                        }
                    });
                } else {
                    Log.d(TAG, "Network Fetch");
                    fetchRepositoryDetails();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.dispose();
    }

}
