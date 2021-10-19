package com.ruoyi.web.service;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.config.FiscoBcos;
import com.ruoyi.web.solidity.FundingUser;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

@Service
public class fundingUserService {
    @Resource
    private FiscoBcos fiscoBcos;

    /**
     * 生成一些必要的参数
     *
     * @return BcosSDK, Client, CryptoKeyPair
     */
    public BcosSDK createSDK() {
        return fiscoBcos.getBcosSDK();
    }

    public Client createClient(BcosSDK bcosSDK) {
        return bcosSDK.getClient(1);
    }

    public CryptoKeyPair createKeyPair(Client client) {
        return client.getCryptoSuite().getCryptoKeyPair();
    }

    //直接部署合约
    public String deployContract(FundingUser funds, Client client, CryptoKeyPair cryptoKeyPair) throws ContractException {
        funds = funds.deploy(client, cryptoKeyPair);
        return funds.getContractAddress();
    }

    //TODO 修改合约地址
    //调用已经部署好的合约
    public FundingUser loadContract(Client client, CryptoKeyPair cryptoKeyPair) {
        //加载合约
        return FundingUser.load("0x054772a3cd26134776a34b8acd388854c1d09540", client, cryptoKeyPair);
    }

    /**
     * 获取当前登陆用户的userId
     *
     * @return userId
     */
    public String getUserId() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        return user.getUserName();
    }

    /**
     * 获取区块高度
     *
     * @return 区块高度
     */
    public BigInteger getHeight(Client client) {
        BlockNumber blockNumber = client.getBlockNumber();
        return blockNumber.getBlockNumber();
    }

    /**
     * 初始化用户资助列表
     *
     * @param userId 用户id
     * @return
     */
    public TransactionReceipt initFundingInfo(String userId, FundingUser funds) {
        return funds.initFundingInfo(userId);
    }

    /**
     * 更新用户的资助信息
     *
     * @param userId      用户id
     * @param fundsNumInt 资助金额
     * @param fundingId   资助人的id
     * @param fundsNumStr 资助金额
     * @param fundingTime 资助时间
     * @return
     */
    public TransactionReceipt updateFundingInfo(String userId, BigInteger fundsNumInt, String fundingId, String fundsNumStr, String fundingTime, FundingUser funds) {
        return funds.updateFundingInfo(userId, fundsNumInt, fundingId, fundsNumStr, fundingTime);
    }

    /**
     * 获取资助者的当前资助数据
     *
     * @param userId 资助者的id
     * @return
     */
    public JSONObject getUserFundingInfo(String userId, FundingUser funds) throws ContractException {
        Tuple4<String, String, String, BigInteger> fundingInfo = funds.getUserFundingInfo(userId);
        String allFundingUser = fundingInfo.getValue1();
        String allFundingNumStr = fundingInfo.getValue2();
        String fundingTime = fundingInfo.getValue3();
        BigInteger score = fundingInfo.getValue4();
        JSONObject allFundingInfo = new JSONObject();
        allFundingInfo.put("Score", score);
        String[] fundingUsers = allFundingUser.split(",");
        String[] fundingNums = allFundingNumStr.split(",");
        String[] fundingTimes = fundingTime.split(",");
        String[] person = new String[fundingUsers.length];
        for (int i = 0; i < fundingUsers.length; i++) {
            String tempStr = fundingUsers[i] + "," + fundingNums[i] + "," + fundingTimes[i];
            person[i] = tempStr;
        }
        allFundingInfo.put("Person", person);
        return allFundingInfo;
    }
}
