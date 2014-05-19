package com.github.wdxzs1985.batch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.wdxzs1985.service.PixivHttpService;

public class PixivBookmarkBatch implements Runnable {

    private final static Pattern USERDATA_PATTERN = Pattern.compile("<div class=\"userdata\"><a href=\"member.php\\?id=[\\d]+\" class=\"ui-profile-popup\" data-user_id=\"(\\d+)\" data-profile_img=\"(.*?)\" data-user_name=\"(.*?)\">.*?</a>(.*?)<br><span> </span></div>");
    private final static Pattern BOOKMARK_NEXT_PATTERN = Pattern.compile("<a href=\"bookmark.php\\?type=user&rest=show&p=([\\d]+)\" class=\"button\" rel=\"next\">");

    @Autowired
    private final PixivHttpService service = null;

    @Override
    public void run() {
        if (this.service.doLogin()) {
            boolean isLast = false;
            int p = 1;
            while (!isLast) {
                String url = "http://www.pixiv.net/bookmark.php?type=user&rest=show&p=" + p;
                String html = this.service.getHtml(url);
                html = StringEscapeUtils.unescapeHtml4(html);
                html = this.service.replaceReturn(html);

                this.findUser(html);

                isLast = !this.findNextPage(html);

                p++;
            }
        }
    }

    private void findUser(String html) {
        Matcher matcher = USERDATA_PATTERN.matcher(html);
        while (matcher.find()) {
            String userId = matcher.group(1);
            // String profileImg = matcher.group(2);
            // String userName = matcher.group(3);
            // String description = matcher.group(4);
            // this.service.log.info(userId);
            // this.service.log.info(profileImg);
            // this.service.log.info(userName);
            // this.service.log.info(description);
            // this.service.getPixivUser(userId);
        }
    }

    private boolean findNextPage(String html) {
        Matcher matcher = BOOKMARK_NEXT_PATTERN.matcher(html);
        if (matcher.find()) {
            String nextPage = matcher.group(1);
            // this.service.log.info(nextPage);
            return true;
        }
        return false;
    }

}
