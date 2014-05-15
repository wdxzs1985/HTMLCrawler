package com.github.wdxzs1985.pixiv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.boot.CommandLineRunner;

public class PixivBookmark extends PixivBase implements CommandLineRunner {

    private final static Pattern USERDATA_PATTERN = Pattern.compile("<div class=\"userdata\"><a href=\"member.php\\?id=[\\d]+\" class=\"ui-profile-popup\" data-user_id=\"(\\d+)\" data-profile_img=\"(.*?)\" data-user_name=\"(.*?)\">.*?</a>(.*?)<br><span> </span></div>");
    private final static Pattern BOOKMARK_NEXT_PATTERN = Pattern.compile("<a href=\"bookmark.php\\?type=user&rest=show&p=([\\d]+)\" class=\"button\" rel=\"next\">");

    @Override
    public void run(String... arg0) throws Exception {
        this.login = this.doLogin();
        boolean isLast = false;
        int p = 1;
        while (!isLast) {
            String url = "http://www.pixiv.net/bookmark.php?type=user&rest=show&p=" + p;
            String html = Application.getHtml(url);
            html = StringEscapeUtils.unescapeHtml4(html);
            html = Application.replaceReturn(html);

            this.findUser(html);
            isLast = !this.findNextPage(html);

            p++;
        }

    }

    private void findUser(String html) {
        Matcher matcher = USERDATA_PATTERN.matcher(html);
        while (matcher.find()) {
            String userId = matcher.group(1);
            String profileImg = matcher.group(2);
            String userName = matcher.group(3);
            String description = matcher.group(4);
            this.log.info(userId);
            this.log.info(profileImg);
            this.log.info(userName);
            this.log.info(description);
        }
    }

    private boolean findNextPage(String html) {
        Matcher matcher = BOOKMARK_NEXT_PATTERN.matcher(html);
        if (matcher.find()) {
            String nextPage = matcher.group(1);
            this.log.info(nextPage);
            return true;
        }
        return false;
    }

}
