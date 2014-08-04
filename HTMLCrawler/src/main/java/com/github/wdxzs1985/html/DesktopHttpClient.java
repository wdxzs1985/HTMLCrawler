package com.github.wdxzs1985.html;

import org.springframework.stereotype.Component;

@Component
public class DesktopHttpClient extends CommonHttpClient {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36";

    public DesktopHttpClient() {
        super(USER_AGENT);
    }

    public String noReturn(final String html) {
        return html.replaceAll("\\r\\n[\\t\\s]*|\\r[\\t\\s]*|\\n[\\t\\s]*", "");
    }
}
