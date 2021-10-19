package com.ruoyi.web.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.infoShowEntity;
import com.ruoyi.common.core.domain.entity.supportInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.function.EntityResponse.fromObject;


@RestController
public class testInfoController {

    @PostMapping("/test/applicationInfo")
    public void applicationInfo(@RequestBody String str) {
        System.out.println(str);
    }

    @PostMapping("/test/infoShow")
    public AjaxResult infoShow() {
        infoShowEntity entity = new infoShowEntity();
        entity.setName("Name");
        entity.setApplicationTitle("ApplicationTitle");
        entity.setAssetCipher("AssetCipher");
        entity.setAssetRange("AssetRange");
        entity.setPhone("phone");
        entity.setIdCard("idCard");
        entity.setHelpNum(0);
        entity.setObtainFunds(10);
        entity.setTarget(100);
        AjaxResult ajax = AjaxResult.success(entity);
        return ajax;
    }

    @PostMapping("/test/supportInfoShow")
    public AjaxResult supportInfoShow() {
        supportInfo support = new supportInfo();
        support.setUserId("123");
        support.setFundingAmount(123);
        support.setFundingTime("2021-10-12");
        AjaxResult ajax = AjaxResult.success(support);
        return ajax;
    }

    @PostMapping("/test/spentAmountShow")
    public AjaxResult spentAmountShow() {
        supportInfo support = new supportInfo();
        support.setUserId("123");
        support.setFundingAmount(123);
        support.setFundingTime("2021-10-12");
        AjaxResult ajax = AjaxResult.success(support);
        return ajax;
    }
}
