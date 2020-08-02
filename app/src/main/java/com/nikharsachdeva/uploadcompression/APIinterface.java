package com.nikharsachdeva.uploadcompression;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIinterface {


    @Multipart
    @POST("api/index.php/admin/upload_customer_pic")
    Call<RatingModel> uploadProPicMultipart(@Part MultipartBody.Part image,@Part("code") RequestBody code);

}