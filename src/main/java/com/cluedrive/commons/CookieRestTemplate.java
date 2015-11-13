package com.cluedrive.commons;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class CookieRestTemplate extends RestTemplate {
    private List<String> cookies;

    public CookieRestTemplate(List<String> cookies) {
        this.cookies = cookies;
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        ClientHttpRequest request = super.createRequest(url, method);
        String cookieValue = "";
        for(int i = 0; i < cookies.size(); i++) {
            cookieValue += cookies.get(i);
            if(i < cookies.size() - 1) {
                cookieValue += "; ";
            }
        }
        if(cookies.size() > 0) {
            request.getHeaders().add("Cookie", cookieValue);
        }
        return request;
    }


    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }
}