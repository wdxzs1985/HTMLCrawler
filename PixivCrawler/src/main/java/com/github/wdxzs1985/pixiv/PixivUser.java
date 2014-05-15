package com.github.wdxzs1985.pixiv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PixivUser extends PixivBase implements CommandLineRunner {

    private final static Pattern NAME_PATTERN = Pattern.compile("<h1 class=\"user\">(.*?)</h1>");
    private final static Pattern ILLUST_PATTERN = Pattern.compile("<a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\" class=\"work\"><img src=\"(.*?)\" class=\"_thumbnail\"><h1 class=\"title\" title=\"(.*?)\">.*?</h1>");
    private final static Pattern MEMBERILLUST_NEXT_PATTERN = Pattern.compile("<a href=\"\\?id=[\\d]+&p=([\\d]+)\" rel=\"next\" class=\"_button\" title=\"次へ\">");

    @Value("${pixiv}")
    private String pixiv;

    @Override
    public void run(String... arg0) throws Exception {
        this.login = this.doLogin();
        this.getPixivUser(this.pixiv);
    }

    private void getPixivUser(String pixiv) {
        int p = 1;
        while (p > 0) {
            String url = String.format("http://www.pixiv.net/member_illust.php?id=%s&p=%d",
                                       pixiv,
                                       p);
            String html = Application.getHtml(url);
            html = StringEscapeUtils.unescapeHtml4(html);
            html = Application.replaceReturn(html);

            this.findIllust(html);
            p = this.findNextPage(html);
        }
    }

    private String findName(String html) {
        String name = null;
        Matcher matcher = null;
        if ((matcher = NAME_PATTERN.matcher(html)).find()) {
            name = matcher.group(1);
            this.log.info(name);
        }
        return name;
    }

    private void findIllust(String html) {
        Matcher matcher = null;
        matcher = ILLUST_PATTERN.matcher(html);
        while (matcher.find()) {
            String illustId = matcher.group(1);
            String thumbnail = matcher.group(2);
            String title = matcher.group(3);
            this.log.info(illustId);
            this.log.info(thumbnail);
            this.log.info(title);
            // this.getIllust(illustId);
        }
    }

    private int findNextPage(String html) {
        Matcher matcher = MEMBERILLUST_NEXT_PATTERN.matcher(html);
        if (matcher.find()) {
            String nextPage = matcher.group(1);
            return Integer.valueOf(nextPage);
        }
        return 0;
    }
}
