/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import com.eTilbudsavis.etasdk.NetworkInterface.HttpStack;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.HeaderUtils;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class HttpURLNetwork implements HttpStack {
	
	public static final String TAG = "HttpURLNetwork";
	
	public HttpResponse performNetworking(Request<?> request) throws ClientProtocolException, IOException {
		
		URL url = new URL(Utils.buildQueryString(request));
		HttpURLConnection connection = openConnection(url, request);
		setHeaders(request, connection);
		setRequestMethod(connection, request);
		
        int sc = connection.getResponseCode();
        if (sc == -1) {
        	throw new IOException("Connection returned invalid response code.");
        }
        
        ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
        String rm = connection.getResponseMessage();
        BasicHttpResponse response = new BasicHttpResponse(pv, sc, rm);
        
        HttpEntity entity = getEntity(connection);
        response.setEntity(entity);
        
        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        
		return response;
	}
	
	private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
		
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setConnectTimeout(request.getTimeOut());
        connection.setReadTimeout(request.getTimeOut());
        connection.setUseCaches(false);
        connection.setDoInput(true);
        
        // use caller-provided custom SslSocketFactory, if any, for HTTPS
//        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
//            ((HttpsURLConnection)connection).setSSLSocketFactory(mSslSocketFactory);
//        }
        
        return connection;
    }

	private void setHeaders(Request<?> request, HttpURLConnection connection) {
		HashMap<String, String> headers = new HashMap<String, String>(request.getHeaders().size());
		headers.putAll(request.getHeaders());
		for(String key : headers.keySet())
			connection.setRequestProperty(key, headers.get(key));
	}
	
	static void setRequestMethod(HttpURLConnection connection, Request<?> request) throws IOException {
        
		String method = request.getMethod().toString();
		connection.setRequestMethod(method);
		
		switch (request.getMethod()) {
            case POST:
            case PUT:
                addBodyIfExists(connection, request);
                break;
            default:
                break;
                
        }
    }
	
    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request) throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HeaderUtils.CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
    
    private static HttpEntity getEntity(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

}
