package com.ruoyi.web.controller.fundrasing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.crypto.Entity.userCipher;
import com.ruoyi.web.service.fundingUserService;
import com.ruoyi.web.service.sqlService;
import com.ruoyi.web.solidity.FundingUser;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class fundingUserController {
    @Resource
    private fundingUserService fundingUserService;

    /**
     * 初始化资助块
     *
     * @param userId 用户id
     */
    //todo 考虑一下在哪里实现
    @RequestMapping("/initFunding")
    public AjaxResult initFundingInfo(@RequestParam(required = false) String userId) {
        BcosSDK bcosSDK = fundingUserService.createSDK();
        Client client = fundingUserService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundingUserService.createKeyPair(client);
        FundingUser funds = fundingUserService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundingUserService.getUserId();
        }
        fundingUserService.initFundingInfo(userId, funds);
        return AjaxResult.success("资助块初始化成功");
    }

    /**
     * 更新资助块
     *
     * @param data 待更新数据
     * @return 更新执行结果
     */
    //已整合到updateFundsCompute接口下
    @RequestMapping("/updateFunding")
    public AjaxResult updateFundingInfo(@RequestBody String data) {
        BcosSDK bcosSDK = fundingUserService.createSDK();
        Client client = fundingUserService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundingUserService.createKeyPair(client);
        FundingUser funds = fundingUserService.loadContract(client, cryptoKeyPair);
        JSONObject fundingInfo = JSON.parseObject(data);
        String userId = fundingInfo.getString("userId");
        if (userId == null) {
            userId = fundingUserService.getUserId();
        }
        BigInteger fundsNumInt = fundingInfo.getBigInteger("fundsNumInt");
        String fundingId = fundingInfo.getString("fundingId");
        String fundsNumStr = fundingInfo.getString("fundsNumStr");
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fundingTime = sdf.format(d);
        fundingUserService.updateFundingInfo(userId, fundsNumInt, fundingId, fundsNumStr, fundingTime, funds);
        return AjaxResult.success("资助块更新成功");
    }

    @RequestMapping("/getFunding")
    public AjaxResult getFundingInfo(@RequestParam(required = false) String userId) {
        BcosSDK bcosSDK = fundingUserService.createSDK();
        Client client = fundingUserService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundingUserService.createKeyPair(client);
        FundingUser funds = fundingUserService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundingUserService.getUserId();
        }
        try {
            JSONObject fundingInfo = fundingUserService.getUserFundingInfo(userId, funds);
            return AjaxResult.success("资助数据查询成功", fundingInfo);
        } catch (ContractException e) {
            e.printStackTrace();
            return AjaxResult.error("资助数据查询失败");
        }
    }
//    @Resource
//    private sqlService sqlservice;
//    @RequestMapping("/getSql")
//    public AjaxResult getSql(String userId)throws IOException {
//        userCipher userInfo=sqlservice.getCipher("0485");
//        return AjaxResult.success("查询成功",userInfo);
//    }
}
