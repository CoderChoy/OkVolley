/*
 * Copyright (C) 2017 CoderChoy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by CoderChoy
 * on 2017/1/22.
 * <p/>
 * An {@link HttpStack} based on {@link OkHttpClient}.
 */

public class OkHttpStack implements HttpStack {

    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    private OkHttpClient mOkHttpClient;
    private final SSLSocketFactory mSslSocketFactory;

    public OkHttpStack() {
        this(null);
    }

    public OkHttpStack(OkHttpClient mOkHttpClient) {
        this(mOkHttpClient, null);
    }

    public OkHttpStack(OkHttpClient mOkHttpClient, SSLSocketFactory mSslSocketFactory) {
        this.mOkHttpClient = mOkHttpClient;
        this.mSslSocketFactory = mSslSocketFactory;
    }

    @Override
    public Response performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        requestBuilder.url(request.getUrl());
        setConnectionParametersForRequest(requestBuilder, request);

        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        for (String headerName : map.keySet()) {
            requestBuilder.addHeader(headerName, map.get(headerName));
        }

        mOkHttpClient = createOkHttpClient(request);
        okhttp3.Request okRequest = requestBuilder.build();
        Call call = mOkHttpClient.newCall(okRequest);

        return call.execute();
    }

    private OkHttpClient createOkHttpClient(Request<?> request) throws IOException {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        }
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder()
                .connectTimeout(request.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(request.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(request.getTimeoutMs(), TimeUnit.MILLISECONDS);

        if (mSslSocketFactory != null && "https".equals(new URL(request.getUrl()).getProtocol())) {
            builder.sslSocketFactory(mSslSocketFactory);
        }
        return builder.build();
    }

    private static void setConnectionParametersForRequest(okhttp3.Request.Builder requestBuilder
            , Request<?> request) throws AuthFailureError, IOException {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getBody();
                if (postBody != null) {
                    MediaType mediaType = MediaType.parse(request.getBodyContentType());
                    requestBuilder.post(RequestBody.create(mediaType, postBody));
                }
                break;
            case Request.Method.GET:
                requestBuilder.get();
                break;
            case Request.Method.DELETE:
                requestBuilder.delete();
                break;
            case Request.Method.POST:
                requestBuilder.post(createBodyIfNonEmptyBody(request));
                break;
            case Request.Method.PUT:
                requestBuilder.put(createBodyIfNonEmptyBody(request));
                break;
            case Request.Method.HEAD:
                requestBuilder.head();
                break;
            case Request.Method.OPTIONS:
                requestBuilder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                requestBuilder.method("TRACE", null);
                break;
            case Request.Method.PATCH:
                requestBuilder.patch(createBodyIfNonEmptyBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createBodyIfNonEmptyBody(Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            MediaType mediaType = MediaType.parse(request.getBodyContentType());
            return RequestBody.create(mediaType, body);
        }
        return null;
    }
}
