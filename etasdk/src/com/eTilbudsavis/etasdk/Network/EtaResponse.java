package com.eTilbudsavis.etasdk.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Timer;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class EtaResponse {


	private static final int BUFFER_SIZE = 0x1000; // 4K
	
	private Object mData;
	private int mStatusCode = -1;
	private Header[] mHeaders;
	
	public EtaResponse() {
		mStatusCode = -1;
	}
	
	public void set(HttpResponse httpResponse) {
		
		mStatusCode = httpResponse.getStatusLine().getStatusCode();
		mHeaders = httpResponse.getAllHeaders();
		String response = null;

		try {
			
			Timer tp = new Timer();
			tp.setSizeMin(1000);
			
			response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
//			response = getString(httpResponse.getEntity(), HTTP.UTF_8);

//			tp.print("HttpEntity.toString()", response);
			
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		setData(response);

	}

	public void set(String error) {
		setData(error);
	}

	public void set(int StatusCode, String data) {
		mStatusCode = StatusCode;
		setData(data);
	}

	private void setData(String data) {

		if (data == null) return;
		
		try {
			if (data.startsWith("[") && data.endsWith("]")) {
				mData = new JSONArray(data);
			} else if(data.startsWith("{") && data.endsWith("}")) {
				mData = new JSONObject(data);
			} else {
				mData = data;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mData = data;
		}
	}
	
	public int getStatusCode() {
		return mStatusCode;
	}

	public JSONArray getJSONArray() {
		return isJSONArray() ? (JSONArray) mData : null;
	}

	public JSONObject getJSONObject() {
		return isJSONObject() ? (JSONObject) mData : null;
	}

	public String getString() {
		return mData == null ? null : mData.toString();
	}
	
	public Object getObject() {
		return mData;
	}
	
	public boolean isJSONObject() {
		return mData instanceof JSONObject;
	}

	public boolean isJSONArray() {
		return mData instanceof JSONArray;
	}
	
	public boolean isString() {
		return mData instanceof String;
	}
	
	public Header[] getHeaders() {
		return mHeaders;
	}

	public String getString(final HttpEntity entity, final String defaultCharset) throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
		}
		int i = (int)entity.getContentLength();
		if (i < 0) {
			i = BUFFER_SIZE;
		}

		String charset = getContentCharSet(entity);

		if (charset == null) {
			charset = defaultCharset;
		}
		if (charset == null) {
			charset = HTTP.DEFAULT_CONTENT_CHARSET;
		}

		String s = "";

//		s = fromBytes(instream, charset);
//		s = fromCharArray(instream, charset);
		s = fromStringBuilder(instream, charset);
		
		return s;
	}

	private static String fromBytes(final InputStream instream, final String charset) {

		String resp = null;

		try {
			ByteArrayBuffer buffer1 = new ByteArrayBuffer(BUFFER_SIZE);
			byte[] buf = new byte[BUFFER_SIZE];
			int r1 = -1;
			int count = 0;
			while (( r1 = instream.read(buf)) != -1) {
				buffer1.append(buf, 0, r1);
				count += 1;
			}
			Utils.logd("ddsdsa", "Count: " + String.valueOf(count));
			resp = new String(buffer1.toByteArray(), charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static String fromStringBuilder(final InputStream instream, final String charset) {

		String resp = null;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(instream));
			StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
			String line;
			while ((line = r.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static String fromCharArray(final InputStream instream, final String charset) {

		String resp = null;
		try {
			Reader reader = new InputStreamReader(instream, charset);
			CharArrayBuffer buffer = new CharArrayBuffer(BUFFER_SIZE);
			try {
				char[] tmp = new char[BUFFER_SIZE];
				int l;
				while((l = reader.read(tmp)) != -1) {
					buffer.append(tmp, 0, l);
				}
			} finally {
				reader.close();
			}
			resp = buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return resp;
	}
	
	public static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }
	
}
