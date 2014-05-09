package com.github.wdxzs1985.pixiv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

public class PixivUser extends PixivBase implements CommandLineRunner {

    private final static Pattern NAME_PATTERN = Pattern.compile("<h1 class=\"user\">(.*?)</h1>");
    private final static Pattern ILLUST_PATTERN = Pattern.compile("<a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\" class=\"work\">");

    @Value("${pixiv}")
    private String pixiv;

    @Override
    public void run(String... arg0) throws Exception {
        this.login = this.doLogin();
        this.getPixivUser(this.pixiv);
    }

    private void getPixivUser(String pixiv) {
        String url = String.format("http://www.pixiv.net/member_illust.php?id=%s",
                                   pixiv);
        String html = Application.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = Application.replaceReturn(html);

        this.findName(html);
        this.findIllust(html);
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
            this.getIllust(illustId);
            // if (this.login) {
            // this.getIllustLarge(illustId);
            // }
        }
    }

}
