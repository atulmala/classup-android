package com.classup;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface VideoInterface {
    String server_ip = "http://10.0.2.2:8000";
    //String server_ip = "https://www.classupclient.com";
    @Multipart
    @POST(server_ip + "/pic_share/upload_video/")
    Call<ResultObject> uploadVideoToServer(@Part MultipartBody.Part video,
                                           @Part MultipartBody.Part params);
}
