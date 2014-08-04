package com.github.wdxzs1985.html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application implements CommandLineRunner {

    public static void main(final String[] args) {
        new SpringApplication(Application.class).run(args);
    }

    private static final Pattern RVT_PTN = Pattern.compile("ComiketWebCatalog.AntiForgeryToken = (\\{.*?\\});");

    @Autowired
    private DesktopHttpClient http;

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public void run(final String... args) throws Exception {
        final boolean isLogin = this.doLogin();
        if (isLogin) {
            this.afterLogin();
            final String rvf = this.findRequestVerificationToken();
            if (rvf != null) {
                final JSONObject rvfJsonObject = JSONObject.fromObject(rvf);
                final String rvfValue = rvfJsonObject.optString("value");
                // getMapping
                final JSONObject mapping = this.getMapping(rvfValue);

                for (final Object circleId : mapping.values()) {
                    // getCircle
                    final JSONObject circle = this.getCircle(rvfValue,
                                                             String.valueOf(circleId));
                    this.log.info(String.format("%s | %s | %s-%s-%s ",
                                                circle.optString("Name"),
                                                circle.optString("Genre"),
                                                circle.optString("Hall"),
                                                circle.optString("Block"),
                                                circle.optString("Space")));
                    Thread.sleep(1000);
                    break;
                }
            }
        }
    }

    public boolean doLogin() {
        final String authUrl = "https://auth.circle.ms/auth/";
        final List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("ReturnUrl",
                                        "https://webcatalog.circle.ms/Account/Login"));
        nvps.add(new BasicNameValuePair("state", "/"));
        nvps.add(new BasicNameValuePair("Username", "bushing@msn.com"));
        nvps.add(new BasicNameValuePair("Password", "wdxzs1985"));

        final HttpResponse response = this.http.post(authUrl, nvps, false);
        if (response.getStatusLine().getStatusCode() == 302) {
            return true;
        }
        return false;
    }

    public void afterLogin() {
        this.http.getForHtml("https://webcatalog.circle.ms/Account/Login?state=%2f&success=1");
    }

    public String findRequestVerificationToken() {
        final String url = "https://webcatalog-free.circle.ms/Map/Hall?day=Day2&genreCode=241&hall=e456";
        final String html = this.http.noReturn(this.http.getForHtml(url));

        final Matcher matcher = RVT_PTN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public JSONObject getMapping(final String rvfValue) {
        final String url = "https://webcatalog-free.circle.ms/Map/GetMapping";
        final List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("__RequestVerificationToken", rvfValue));
        nvps.add(new BasicNameValuePair("day", "Day2"));
        nvps.add(new BasicNameValuePair("hall", "e"));
        return this.http.postForJSON(url, nvps);
    }

    public JSONObject getCircle(final String rvfValue, final String circleId) {
        final String url = "https://webcatalog-free.circle.ms/Circle/" + circleId;
        final List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
        nvps.add(new BasicNameValuePair("__RequestVerificationToken", rvfValue));
        nvps.add(new BasicNameValuePair("id", circleId));
        return this.http.postForJSON(url, nvps);
    }

}
