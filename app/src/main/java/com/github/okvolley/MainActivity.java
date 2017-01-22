package com.github.okvolley;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.OkHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = (TextView) findViewById(R.id.tv_info);
    }

    public void onGetInfoClick(View view) {
        //使用默认的OkHttpStack
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        StringRequest stringRequest = new StringRequest("https://api.github.com/users/CoderChoy",
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        tvInfo.setText(response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println(error);
//            }
//        });
//        requestQueue.add(stringRequest);


        //自定义OkHttpClient，添加HttpLoggingInterceptor打印日志
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        OkHttpStack okHttpStack = new OkHttpStack(okHttpClient);

        RequestQueue requestQueue = Volley.newRequestQueue(this, okHttpStack);
        StringRequest stringRequest = new StringRequest("https://api.github.com/users/CoderChoy",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        tvInfo.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        requestQueue.add(stringRequest);
    }
}
