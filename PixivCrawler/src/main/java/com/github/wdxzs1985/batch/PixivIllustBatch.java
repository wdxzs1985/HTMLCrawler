package com.github.wdxzs1985.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.wdxzs1985.service.PixivHttpService;

public class PixivIllustBatch implements Runnable {

    @Value("${PixivIllust.illustId}")
    private String illustId;

    @Autowired
    private final PixivHttpService service = null;

    @Override
    public void run() {
        if (this.service.doLogin()) {
            // this.service.getIllust(this.illustId);
        }
    }

}
