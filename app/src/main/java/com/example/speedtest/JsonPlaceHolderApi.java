package com.example.speedtest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface JsonPlaceHolderApi {

    @GET("{filename}")
    @Streaming
    Call<Post> getPost(@Path("filename") String filename,
            @Query(value = "country") String sortCountry,
            @Query(value = "city") String sortCity,
            @Query(value = "isp") String sortIsp
    );
}
