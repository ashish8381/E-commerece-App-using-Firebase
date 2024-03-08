package com.inventics.e_commerce;

import java.util.ArrayList;
import java.util.List;
import com.inventics.e_commerce.model.catModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitAPI {

    // as we are making get request
    // so we are displaying GET as annotation.
    // and inside we are passing
    // last parameter for our url.


    // as we are calling data from array
    // so we are calling it with json object
    @GET("categories")
    Call<ArrayList<String>> getCategory();

}
