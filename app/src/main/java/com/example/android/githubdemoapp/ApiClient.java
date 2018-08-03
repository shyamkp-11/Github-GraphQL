package com.example.android.githubdemoapp;

import com.apollographql.apollo.ApolloClient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiClient {

    private static OkHttpClient okHttpClient = null;

    private static ApolloClient apolloClient = null;


    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
             okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request orig = chain.request();
                            Request.Builder builder = orig.newBuilder()
                                    .method(orig.method(), orig.body())
                                    .header("Authorization", "bearer " + Constants.GITHUB_API_TOKEN);

                            return (chain.proceed(builder.build()));
                        }
                    })
                    .build();
        }
        return okHttpClient;
    }

    public static ApolloClient getApolloClient() {
        if (apolloClient == null) {
            apolloClient = ApolloClient.builder()
                    .serverUrl(Constants.GITHUB_GRAPHQL_API_BASE_URL)
                    .okHttpClient(ApiClient.getOkHttpClient())
                    .build();
        }

        return apolloClient;
    }
}
