package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.HttpStack;
import com.eTilbudsavis.etasdk.network.Network;
import com.eTilbudsavis.etasdk.network.NetworkResponse;
import com.eTilbudsavis.etasdk.network.Request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NetworkImpl implements Network {
	
	public static final String TAG = Constants.getTag(NetworkImpl.class);
	
	private static final int BUFFER_SIZE = 0x1000; // 4K
	
	HttpStack mStack;
	
	public NetworkImpl(HttpStack stack) {
		mStack = stack;
	}
	
	public NetworkResponse performRequest(Request<?> request) throws EtaError {

		byte[] content = null;
		Map<String, String> responseHeaders = new HashMap<String, String>();
		try {
			
			HttpResponse resp = mStack.performNetworking(request);
			
			if (resp.getEntity() == null) {
				// add 0-byte for to mock no-content
				content = new byte[0];
			} else {
				request.addEvent("reading-input");
				content = entityToBytes(resp.getEntity());
			}
			
			/* 
			 * TODO report back content and body length, to collect stats on
			 * transferred data, to compare with MsgPack later.
			 */
			int respLength = content.length;
			int bodyLength = (request.getBody() == null ? 0 : request.getBody().length);
			
			request.stats(respLength, bodyLength);
			
			for (org.apache.http.Header h : resp.getAllHeaders()) {
				responseHeaders.put(h.getName(), h.getValue());
			}
			
			NetworkResponse r = new NetworkResponse(resp.getStatusLine().getStatusCode(), content, responseHeaders);
			
			return r;
			
		} catch (Exception e) {
			EtaLog.e(TAG, e.getMessage(), e);
			throw new NetworkError(e);
		}
		
	}

	private static byte[] entityToBytes(HttpEntity entity) throws IllegalStateException, IOException {
		
		// Find best buffer size
		int init_buf = 0 <= entity.getContentLength() ? (int)entity.getContentLength() : BUFFER_SIZE;
		
		ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
			
		InputStream is = entity.getContent();
		if (is == null)
			return bytes.toByteArray();
		
		byte[] buf = new byte[init_buf];
		int c = -1;
		while (( c = is.read(buf)) != -1) {
			bytes.append(buf, 0, c);
		}
		
		return bytes.toByteArray();
	}
	
}
