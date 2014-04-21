package com.github.wdxzs1985.html;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Crawler {

    public String getHtml(String url) throws Exception {
        if (StringUtils.hasText(url)) {
            CloseableHttpClient httpc = null;
            try {
                httpc = HttpClients.createMinimal();
                final HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpc.execute(httpget);
                return EntityUtils.toString(response.getEntity(), "utf8");
            } finally {
                if (httpc != null) {
                    httpc.close();
                }
            }
        }
        return null;
    }

}
