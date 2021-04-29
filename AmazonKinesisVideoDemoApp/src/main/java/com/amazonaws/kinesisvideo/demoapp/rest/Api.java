package com.amazonaws.kinesisvideo.demoapp.rest;

import com.amazonaws.kinesisvideo.demoapp.rest.model.FaceDetectionResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface Api {

    String BASE_URL = "https://76mgbx3wue.execute-api.eu-west-1.amazonaws.com/";

    @GET("v0/event/{id}")
    Call<FaceDetectionResult> getFaceResult(@Path(value = "id") int id);
}
