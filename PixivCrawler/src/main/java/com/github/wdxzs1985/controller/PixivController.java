package com.github.wdxzs1985.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.wdxzs1985.domain.PixivIllust;
import com.github.wdxzs1985.domain.PixivUser;
import com.github.wdxzs1985.service.PixivHttpService;
import com.github.wdxzs1985.service.PixivIllustService;
import com.github.wdxzs1985.service.PixivUserService;

@Controller
public class PixivController {

    @Autowired
    private final PixivUserService pixivUserService = null;
    @Autowired
    private final PixivIllustService pixivIllustService = null;
    @Autowired
    private final PixivHttpService httpSerivce = null;

    @RequestMapping("/")
    public String index() {
        //
        return "index";
    }

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public String subscribe(Long userId, String email, RedirectAttributes redirectModel) {
        if (userId == null) {
            return "index";
        }
        PixivUser user = this.httpSerivce.getPixivUser(userId);
        if (user == null) {
            return "index";
        }
        this.pixivUserService.subscribe(user, email);
        redirectModel.addAttribute("userId", userId);
        return "redirect:/user/{userId}";
    }

    @RequestMapping("/user")
    public String user(Model model) {
        List<PixivUser> userList = this.pixivUserService.getUserList(0, 10);
        model.addAttribute("userList", userList);
        return "user/index";
    }

    @RequestMapping("/user/{userId}")
    public String user(@PathVariable Long userId, Model model) {
        PixivUser userBean = this.pixivUserService.getUser(userId);
        model.addAttribute("userBean", userBean);

        List<PixivIllust> illustList = this.pixivIllustService.getUserIllustList(userId,
                                                                                 0,
                                                                                 10);
        model.addAttribute("illustList", illustList);
        return "user/view";
    }

    @RequestMapping("/illust")
    public String illust(Model model) {
        //
        List<PixivIllust> illustList = this.pixivIllustService.getIllustList(0,
                                                                             10);
        model.addAttribute("illustList", illustList);
        return "illust/index";
    }

    @RequestMapping("/illust/{illustId}")
    public String illust(@PathVariable Long illustId, Model model) {
        PixivIllust illustBean = this.pixivIllustService.getIllust(illustId);
        model.addAttribute("illustBean", illustBean);
        return "illust/view";
    }

}
