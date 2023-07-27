package com.elwin013.gitlabpatauth.api;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


public class HttpUtil implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private final ResponseHandler<String> responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status == 200) {
            HttpEntity responseEntity = response.getEntity();
            return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
        } else {
            LOG.error("Error while calling endpoint '{}': {}", response.getStatusLine().getReasonPhrase(), LOG.isErrorEnabled() ? EntityUtils.toString(response.getEntity()) : "");
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    };
    private final CloseableHttpClient client;

    public HttpUtil() {
        this.client = HttpClients.createDefault();
    }

    public static Map<String, String> getMap(String... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    public String get(String uri, Map<String, String> headers, Map<String, String> queryParams) throws URISyntaxException, IOException {
        LOG.debug("[GET] uri: '{}', headers: {} queryParams: {}", uri, headers, queryParams);
        URIBuilder uriBuilder = new URIBuilder(uri);
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            uriBuilder.addParameter(param.getKey(), param.getValue());
        }

        HttpGet get = new HttpGet(uriBuilder.build().toASCIIString());
        for (Map.Entry<String, String> param : headers.entrySet()) {
            get.setHeader(param.getKey(), param.getValue());
        }

        return client.execute(get, responseHandler);

    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }
}
