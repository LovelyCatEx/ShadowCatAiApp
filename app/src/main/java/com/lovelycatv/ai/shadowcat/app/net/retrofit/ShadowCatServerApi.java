package com.lovelycatv.ai.shadowcat.app.net.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class ShadowCatServerApi {
    private String baseUrl;
    private Retrofit retrofit;

    public ShadowCatServerApi(String baseUrl) {
        this.baseUrl = baseUrl;
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder()
                        .addInterceptor(httpLoggingInterceptor)
                        .build()
                )
                .addConverterFactory(FastJsonConverterFactory.create())
                .build();
    }

    public IShadowCatServerApi getApi() {
        return this.retrofit.create(IShadowCatServerApi.class);
    }
}
