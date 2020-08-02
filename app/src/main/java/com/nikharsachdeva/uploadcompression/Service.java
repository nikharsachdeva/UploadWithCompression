package com.nikharsachdeva.uploadcompression;

import com.nikharsachdeva.uploadcompression.APIinterface;
import com.nikharsachdeva.uploadcompression.MainActivity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class Service {

    public static Retrofit getClient() {



        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.BASE_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static APIinterface apIinterface() {
        return getClient().create(APIinterface.class);
    }

}
