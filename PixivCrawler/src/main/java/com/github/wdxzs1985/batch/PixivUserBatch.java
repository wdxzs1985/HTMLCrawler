package com.github.wdxzs1985.batch;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.wdxzs1985.domain.PixivIllust;
import com.github.wdxzs1985.domain.PixivUser;
import com.github.wdxzs1985.service.PixivHttpService;
import com.github.wdxzs1985.service.PixivIllustService;
import com.github.wdxzs1985.service.PixivUserService;

@Component
public class PixivUserBatch {

    @Autowired
    private final PixivHttpService httpService = null;

    @Autowired
    private final PixivUserService userService = null;

    @Autowired
    private final PixivIllustService illustService = null;

    private final Log log = LogFactory.getLog(this.getClass());

    @Scheduled(fixedDelay = 3600000)
    public void run() {
        this.log.debug("PixivUserBatch.start");
        List<PixivUser> userList = this.userService.getUserList(0,
                                                                Integer.MAX_VALUE);
        if (CollectionUtils.isNotEmpty(userList)) {
            if (this.httpService.doLogin()) {
                for (PixivUser pixivUser : userList) {
                    long userId = pixivUser.getId();
                    int p = 1;
                    while (p != PixivHttpService.EOF) {
                        Map<String, Object> model = this.httpService.getPixivUserIllust(userId,
                                                                                        p);
                        List<PixivIllust> illustList = (List<PixivIllust>) model.get("illustList");
                        int count = this.illustService.bulkInsert(illustList);
                        if (count == illustList.size()) {
                            p = (int) model.get("nextPage");
                        } else {
                            p = PixivHttpService.EOF;
                        }
                    }
                }
            }
        }
        this.log.debug("PixivUserBatch.end");
    }
}
