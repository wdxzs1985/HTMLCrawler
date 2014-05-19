package com.github.wdxzs1985.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.wdxzs1985.domain.PixivIllust;
import com.github.wdxzs1985.domain.PixivUser;
import com.github.wdxzs1985.html.CommonHttpClient;

@Service
public class PixivHttpService {

    private final static Pattern LOGIN_PATTERN = Pattern.compile("<input type=\"hidden\" name=\"mode\" value=\"login\">");

    private final static Pattern WORKINFO_PATTERN = Pattern.compile("<section class=\"work-info ui-expander-container\"><div class=\"ui-expander-target\"><div class=\"user-reaction\"><section class=\"score\"><dl><dt>閲覧数</dt><dd class=\"view-count\">(\\d+)</dd><dt>評価回数</dt><dd class=\"rated-count\">(\\d+)</dd><dt>総合点</dt><dd class=\"score-count\">(\\d+)</dd></dl><script>pixiv\\.context\\.rated = (true|false);</script><div class=\"rating\"><div class=\"rate\"></div><div class=\"star\"></div><div class=\"status\"></div></div></section></div><ul class=\"meta\"><li>(.*?)</li><li>(.*?)</li><li>(<ul class=\"tools\">(<li>.*?</li>)+</ul>)?</li>(<li class=\"r-18\">R-18</li>)?(<li><a href=\"/tags.php\\?tag=%E3%82%AA%E3%83%AA%E3%82%B8%E3%83%8A%E3%83%AB\" class=\"original-works _ui-tooltip\" data-tooltip=\"オリジナル作品一覧\">オリジナル</a></li>)?</ul><h1 class=\"title\">(.*?)</h1><p class=\"caption\">(.*?)</p><div class=\"expand\"><span class=\"button\">続きを見る ▾</span></div><div class=\"collapse\"><span class=\"button\">閉じる ▴</span></div></div></section>");
    private final static Pattern ILLUST_MODE_PATTERN = Pattern.compile("<div class=\"works_display\"><a href=\"member_illust.php\\?mode=(big|manga)&illust_id=[\\d]+\" target=\"_blank\"><img src=\"(.*?)\" alt=\".*?\" title=\".*?\" border=\"0\"></a>");
    private final static Pattern ILLUST_TAG_PATTERN = Pattern.compile("<li class=\"tag\"><a href=\"/tags.php\\?tag=.*?\" class=\"portal\">c</a><a href=\"/search.php\\?s_mode=s_tag_full&word=.*?\" class=\"text\">(.*?)</a>");
    private final static Pattern LI_PATTERN = Pattern.compile("<li>(.*?)</li>");

    private final static Pattern BIG_IMAGE_PATTERN = Pattern.compile("(http://i[\\d]\\.pixiv\\.net/img[0-9]+/img/[^/]*?/\\d+.(png|jpe?g|gif))");
    private final static Pattern MANGA_IMAGE_PATTERN = Pattern.compile("data-src=\"(http://i[\\d]\\.pixiv\\.net/img[0-9]+/img/[^/]*?/\\d+_p[\\d]+.(png|jpe?g|gif))\"");

    private final static Pattern MEMBERNAME_PATTERN = Pattern.compile("<a href=\"/member.php\\?id=([0-9]+)\"><h1 class=\"name\">(.*?)</h1></a>");
    private final static Pattern MEMBERILLUST_PATTERN = Pattern.compile("<a href=\"/member_illust.php\\?mode=medium&illust_id=(\\d+)\" class=\"work\"><img src=\"(.*?)\" class=\"_thumbnail\"><h1 class=\"title\" title=\"(.*?)\">.*?</h1>");
    private final static Pattern MEMBERILLUST_NEXT_PATTERN = Pattern.compile("<a href=\"\\?id=[\\d]+&p=([\\d]+)\" rel=\"next\" class=\"_button\" title=\"次へ\">");

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36";
    public static final int EOF = -1;

    private final CommonHttpClient HTTP = new CommonHttpClient(USER_AGENT);

    public void loadCookie(String path) {
        this.HTTP.loadCookie(new File(path));
    }

    public final String getHtml(String url) {
        String html = this.HTTP.getForHtml(url);
        this.HTTP.setReferer(url);
        this.HTTP.saveCookie();
        return html;
    }

    public final byte[] getByteArray(String url) throws IOException {
        HttpResponse response = this.HTTP.get(url);
        return EntityUtils.toByteArray(response.getEntity());

    }

    public final String[] splitCsv(String line) {
        List<String> splited = new ArrayList<String>();
        StringBuilder strB = new StringBuilder();
        char strChar = 0;
        boolean isString = false;

        for (int i = 0; i < line.length(); i++) {
            strChar = line.charAt(i);
            if (strChar == "\"".charAt(0)) {
                isString = isString ? false : true;
            } else if (!isString && strChar == ",".charAt(0)) {
                splited.add(strB.toString());
                strB = new StringBuilder();
            } else {
                strB.append(strChar);
            }
        }
        splited.add(strB.toString());
        return splited.toArray(new String[] {});
    }

    public final String replaceReturn(String text) {
        return text.replaceAll("\\r\\n[\\t\\s]*|\\r[\\t\\s]*|\\n[\\t\\s]*", "");
    }

    public String post(String url, List<BasicNameValuePair> nvps) {
        String html = this.HTTP.postForHtml(url, nvps);
        this.HTTP.setReferer(url);
        this.HTTP.saveCookie();
        return html;
    }

    protected final Log log = LogFactory.getLog(this.getClass());

    protected boolean login = false;

    @Value("${pixiv.userId}")
    private String userId;

    @Value("${pixiv.password}")
    private String pass;

    @Value("${pixiv.cookiePath}")
    private String cookiePath;

    @Value("${pixiv.imageDir}")
    private String imageDir;

    public boolean doLogin() {
        if (!this.checkLogin()) {
            String path = "http://www.pixiv.net/login.php";
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair("mode", "login"));
            nvps.add(new BasicNameValuePair("return_to", "/"));
            nvps.add(new BasicNameValuePair("pixiv_id", this.userId));
            nvps.add(new BasicNameValuePair("pass", this.pass));
            nvps.add(new BasicNameValuePair("skip", "1"));

            String html = this.post(path, nvps);
            this.login = StringUtils.isBlank(html);
            if (this.login) {
                this.log.info("login ok");
            }
        }
        return this.login;
    }

    private boolean checkLogin() {
        this.loadCookie(this.cookiePath);
        String path = "http://www.pixiv.net/mypage.php";
        String html = this.getHtml(path);
        if (LOGIN_PATTERN.matcher(html).find()) {
            this.login = false;
        } else {
            this.login = true;
        }
        return this.login;
    }

    public void getIllust(PixivIllust illustBean) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=%d",
                                   illustBean.getId());
        String html = this.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.replaceReturn(html);

        this.findWorkInfo(illustBean, html);
        this.findIllustTags(illustBean, html);

        this.findIllustMode(illustBean, html);

    }

    public void getIllustBig(PixivIllust illustBean) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member_illust.php?mode=big&illust_id=%d",
                                   illustBean.getId());
        String html = this.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.replaceReturn(html);

        Matcher matcher = BIG_IMAGE_PATTERN.matcher(html);
        if (matcher.find()) {
            String imageUrl = matcher.group(1);
            this.downloadBig(illustBean, imageUrl);
        }
    }

    public void downloadBig(PixivIllust illustBean, String imageUrl) {
        try {
            String[] fragments = StringUtils.split(imageUrl, "/");
            String fileName = String.format("%d/%s",
                                            illustBean.getId(),
                                            fragments[fragments.length - 1]);
            File file = new File(this.imageDir, fileName);
            byte[] data = this.HTTP.getForBytes(imageUrl);
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getIllustManga(PixivIllust illustBean) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member_illust.php?mode=manga&illust_id=%d",
                                   illustBean.getId());
        String html = this.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.replaceReturn(html);

        Matcher matcher = MANGA_IMAGE_PATTERN.matcher(html);
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            this.downloadManga(illustBean, imageUrl);
        }
    }

    public void downloadManga(PixivIllust illustBean, String imageUrl) {
        try {
            String[] fragments = StringUtils.split(imageUrl, "/");
            String fileName = String.format("%d/%d/%s",
                                            illustBean.getUserId(),
                                            illustBean.getId(),
                                            fragments[fragments.length - 1]);
            File file = new File(this.imageDir, fileName);
            byte[] data = this.HTTP.getForBytes(imageUrl);
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void findWorkInfo(PixivIllust illustBean, String html) {
        Matcher matcher = WORKINFO_PATTERN.matcher(html);
        if (matcher.find()) {

            String viewCount = matcher.group(1);
            String ratedCount = matcher.group(2);
            String scoreCount = matcher.group(3);
            String rated = matcher.group(4);
            String date = matcher.group(5);
            String size = matcher.group(6);
            List<String> tools = this.splitListItem(matcher.group(7));
            String R = matcher.group(9);
            String original = matcher.group(10);
            String title = matcher.group(11);
            String caption = matcher.group(12);
            caption = StringUtils.replacePattern(caption,
                                                 "<a href=\"/jump.php\\?.*?\" target=\"_blank\">(.*?)</a>",
                                                 "$1");

            this.log.info(String.format("閲覧数 : %s", viewCount));
            this.log.info(String.format("評価回数: %s", ratedCount));
            this.log.info(String.format("総合点: %s", scoreCount));
            this.log.info(String.format("rated: %s", rated));
            this.log.info(String.format("date: %s", date));
            this.log.info(String.format("size: %s", size));
            for (String tool : tools) {
                this.log.info(String.format("tool: %s", tool));
            }
            this.log.info(String.format("R: %s",
                                        StringUtils.isNotBlank(R) ? "yes"
                                                : "no"));
            this.log.info(String.format("original: %s",
                                        StringUtils.isNotBlank(original) ? "yes"
                                                : "no"));

            illustBean.setTitle(title);
            illustBean.setCaption(caption);
        }
    }

    private List<String> splitListItem(String html) {
        List<String> items = new ArrayList<String>();
        Matcher matcher = LI_PATTERN.matcher(html);
        while (matcher.find()) {
            String item = matcher.group(1);
            items.add(item);
        }
        return items;
    }

    private void findIllustMode(PixivIllust illustBean, String html) {
        Matcher matcher = ILLUST_MODE_PATTERN.matcher(html);
        while (matcher.find()) {
            String mode = matcher.group(1);
            illustBean.setMode(mode);
            if ("big".equals(mode)) {
                this.getIllustBig(illustBean);
            } else if ("manga".equals(mode)) {
                this.getIllustManga(illustBean);
            } else {
                this.log.debug(html);
            }
        }
    }

    public void findIllustTags(PixivIllust illustBean, String html) {
        Matcher matcher = null;
        matcher = ILLUST_TAG_PATTERN.matcher(html);
        while (matcher.find()) {
            String tag = matcher.group(1);
            this.log.info("tag:" + tag);
        }
    }

    public Map<String, Object> getPixivUserIllust(Long userId, int page) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member_illust.php?id=%d&p=%d",
                                   userId,
                                   page);
        String html = this.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.replaceReturn(html);

        List<PixivIllust> illustList = this.findMemberIllust(userId, html);
        int p = this.findNextPage(MEMBERILLUST_NEXT_PATTERN, html);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("illustList", illustList);
        model.put("nextPage", p);

        return model;
    }

    private List<PixivIllust> findMemberIllust(Long userId, String html) {
        List<PixivIllust> illustList = new ArrayList<PixivIllust>();
        Matcher matcher = null;
        matcher = MEMBERILLUST_PATTERN.matcher(html);
        while (matcher.find()) {
            PixivIllust illustBean = new PixivIllust();
            illustBean.setUserId(userId);
            illustBean.setId(Long.valueOf(matcher.group(1)));
            illustBean.setTitle(matcher.group(3));

            this.getIllust(illustBean);

            illustList.add(illustBean);
        }
        return illustList;
    }

    private int findNextPage(Pattern pattern, String html) {
        Matcher matcher = MEMBERILLUST_NEXT_PATTERN.matcher(html);
        if (matcher.find()) {
            String nextPage = matcher.group(1);
            return Integer.valueOf(nextPage);
        }
        return EOF;
    }

    public PixivUser getPixivUser(Long userId) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        String url = String.format("http://www.pixiv.net/member.php?id=%d",
                                   userId);
        String html = this.getHtml(url);
        html = StringEscapeUtils.unescapeHtml4(html);
        html = this.replaceReturn(html);

        PixivUser user = this.findUser(html);
        return user;
    }

    private PixivUser findUser(String html) {
        Matcher matcher = MEMBERNAME_PATTERN.matcher(html);
        if (matcher.find()) {
            PixivUser user = new PixivUser();
            user.setId(Long.valueOf(matcher.group(1)));
            user.setName(matcher.group(2));
            return user;
        }
        return null;
    }
}
