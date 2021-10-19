package com.ruoyi.web.service;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.config.FiscoBcos;
import com.ruoyi.web.crypto.Entity.User;
import com.ruoyi.web.crypto.Entity.userCipher;
import com.ruoyi.web.crypto.encryption.EncryptUtils;
import com.ruoyi.web.solidity.FundsApplicationCipher;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderInterface;
import org.fisco.bcos.sdk.transaction.codec.decode.TransactionDecoderService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Service
public class FundsApplicationService {
    @Autowired
    private FiscoBcos fiscoBcos;
    @Resource
    private sqlService sqlservice;

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
    public String deployContract(FundsApplicationCipher funds, Client client, CryptoKeyPair cryptoKeyPair) throws ContractException {
        funds = funds.deploy(client, cryptoKeyPair);
        return funds.getContractAddress();
    }

    //TODO 修改合约地址
    //调用已经部署好的合约
    public FundsApplicationCipher loadContract(Client client, CryptoKeyPair cryptoKeyPair) {
        //加载合约
        return FundsApplicationCipher.load("0xf676641a740ce29edfcfbaaa77d6ca5470a58a49", client, cryptoKeyPair);
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
     * 发布新的求助信息
     *
     * @param fundsJson 前端传来的json字符串，包含了用户的所有信息
     * @return
     */
    public TransactionReceipt insertFundsInfo(JSONObject fundsJson, FundsApplicationCipher funds, Client client) throws Exception {
        User user = new User();
        user.setUserId(fundsJson.getString("userId"));
        user.setName(fundsJson.getString("name"));
        user.setPhone(fundsJson.getString("phone"));
        user.setIdCard(fundsJson.getString("idCard"));
        user.setLocation(fundsJson.getString("location"));
        user.setApplicationTitle(fundsJson.getString("applicationTitle"));
        user.setUserInfo(fundsJson.getString("userInfo"));
        user.setSCDMedicalRecord(fundsJson.getString("medicalRecord"));
        user.setVCLAsset(fundsJson.getString("asset"));
        System.err.println("service asset: " + fundsJson.getString("asset"));
        System.err.println("user.getVCLAsset(): " + user.getVCLAsset());
        user.setReceiveAccount(fundsJson.getString("receiveAccount"));
        user.setTime(fundsJson.getString("time"));
        user.setTarget(fundsJson.getString("target"));

//        System.out.println(user);
        Map<String, String> encryptResult = EncryptUtils.encrypt(user);
        String userPrivateKey = encryptResult.get("userPrivateKey");
        String blindedCertificateSignature = encryptResult.get("blindedCertificateSignature");
        String certificateData = encryptResult.get("certificateData");
        String certificateTemplate = encryptResult.get("certificateTemplate");

        sqlservice.insertUserCipher(user.getUserId(), userPrivateKey, blindedCertificateSignature);
        TransactionReceipt receipt = funds.insertFundsInfo(user.getUserId(), certificateData, certificateTemplate);

//        System.out.println(userPrivateKey);
//        System.out.println(blindedCertificateSignature);

        CryptoSuite cryptoSuite = client.getCryptoSuite();
        TransactionDecoderInterface decoder = new TransactionDecoderService(cryptoSuite);
        TransactionResponse response = decoder.decodeReceiptWithValues(FundsApplicationCipher.ABI, "insertFundsInfo", receipt);
        System.out.println(response.getReturnObject());

        return receipt;
    }

    /**
     * 获取用户求助信息
     *
     * @param userId 求助用户的id
     * @return 求助用户的各项信息
     */
    public JSONObject getUserApplicationInfo(String userId, FundsApplicationCipher funds) throws ContractException, IOException {
        Tuple3<String, String, String> userInfoTuple = funds.getUserApplicationInfo(userId);
        String uid = userInfoTuple.getValue1();
        String certificateData = userInfoTuple.getValue2();
        String certificateTemplate = userInfoTuple.getValue3();
        if (uid.equals("该ID不存在")) {
            return null;
        } else {
            Map<String, String> userMap = new HashMap<>();
            userCipher usercipher = sqlservice.getCipher(uid);
            String userPrivateKey = usercipher.getUserPrivateKey();
            String blindedCertificateSignature = usercipher.getBlindedCertificateSignature();
            userMap.put("userPrivateKey", userPrivateKey);
            userMap.put("blindedCertificateSignature", blindedCertificateSignature);
            userMap.put("certificateData", certificateData);
            userMap.put("certificateTemplate", certificateTemplate);
            //需要披露的字段
            List<String> userInfoList = new ArrayList<>();
            userInfoList.add("name");
            userInfoList.add("phone");
            userInfoList.add("idCard");
            userInfoList.add("location");
            userInfoList.add("applicationTitle");
            userInfoList.add("userInfo");
            userInfoList.add("SCDMedicalRecord");
//            userInfoList.add("VCLAsset");
            userInfoList.add("receiveAccount");
            userInfoList.add("time");
            userInfoList.add("target");
            Map<String, String> userInfoMap = new HashMap<>();
            userInfoMap = EncryptUtils.selectiveDisclosure(userMap, userInfoList);
            //将披露结果转为json
            JSONObject userInfoJson = new JSONObject();
            userInfoJson.put("name", userInfoMap.get("name"));
            userInfoJson.put("phone", userInfoMap.get("phone"));
            userInfoJson.put("idCard", userInfoMap.get("idCard"));
            userInfoJson.put("location", userInfoMap.get("location"));
            userInfoJson.put("applicationTitle", userInfoMap.get("applicationTitle"));
            userInfoJson.put("userInfo", userInfoMap.get("userInfo"));
            userInfoJson.put("SCDMedicalRecord", userInfoMap.get("SCDMedicalRecord"));
            //userInfoJson.put("VCLAsset", userInfoMap.get("VCLAsset"));
            System.err.println(userInfoMap.get("VCLAsset"));
            System.err.println(VCLAsset(userMap));
            userInfoJson.put("VCLAsset", VCLAsset(userMap));
            userInfoJson.put("receiveAccount", userInfoMap.get("receiveAccount"));
            userInfoJson.put("time", userInfoMap.get("time"));
            userInfoJson.put("target", userInfoMap.get("target"));
            return userInfoJson;
        }
    }

    public JSONObject getUserApplicationInfoBySelf(String userId, FundsApplicationCipher funds) throws Exception {
        Tuple3<String, String, String> userInfoTuple = funds.getUserApplicationInfo(userId);
        String uid = userInfoTuple.getValue1();
        String certificateData = userInfoTuple.getValue2();
        String certificateTemplate = userInfoTuple.getValue3();
        if (uid.equals("该ID不存在")) {
            return null;
        } else {
            Map<String, String> userMap = new HashMap<>();
            userCipher usercipher = sqlservice.getCipher(uid);
            String userPrivateKey = usercipher.getUserPrivateKey();
            String blindedCertificateSignature = usercipher.getBlindedCertificateSignature();
            userMap.put("userPrivateKey", userPrivateKey);
            userMap.put("blindedCertificateSignature", blindedCertificateSignature);
            userMap.put("certificateData", certificateData);
            userMap.put("certificateTemplate", certificateTemplate);
            //需要披露的字段
            List<String> userInfoList = new ArrayList<>();
            userInfoList.add("name");
            userInfoList.add("phone");
            userInfoList.add("idCard");
            userInfoList.add("location");
            userInfoList.add("applicationTitle");
            userInfoList.add("userInfo");
            userInfoList.add("SCDMedicalRecord");
//            userInfoList.add("VCLAsset");
            userInfoList.add("receiveAccount");
            userInfoList.add("time");
            userInfoList.add("target");
            Map<String, String> userInfoMap = new HashMap<>();
            userInfoMap = EncryptUtils.selectiveDisclosure(userMap, userInfoList);
            //将披露结果转为json
            JSONObject userInfoJson = new JSONObject();
            userInfoJson.put("name", userInfoMap.get("name"));
            userInfoJson.put("phone", userInfoMap.get("phone"));
            userInfoJson.put("idCard", userInfoMap.get("idCard"));
            userInfoJson.put("location", userInfoMap.get("location"));
            userInfoJson.put("applicationTitle", userInfoMap.get("applicationTitle"));
            userInfoJson.put("userInfo", userInfoMap.get("userInfo"));
            userInfoJson.put("SCDMedicalRecord", userInfoMap.get("SCDMedicalRecord"));
            userInfoJson.put("VCLAsset", VCLAsset(userMap));
            System.err.println("userInfoMap.get(\"VCLAsset\"): " + userInfoMap.get("VCLAsset"));
            userInfoJson.put("receiveAccount", userInfoMap.get("receiveAccount"));
            userInfoJson.put("time", userInfoMap.get("time"));
            userInfoJson.put("target", userInfoMap.get("target"));
            return userInfoJson;
        }
    }

    /**
     * 更新求助信息
     *
     * @param userId 用户id
     * @return 更新执行结果
     */
    public TransactionReceipt updataApplication(String userId, JSONObject updataJson, FundsApplicationCipher funds) {
        List<String> applicationInfo = new ArrayList<>();
        User user = new User();
        user.setUserId(updataJson.getString("userId"));
        user.setName(updataJson.getString("name"));
        user.setPhone(updataJson.getString("phone"));
        user.setIdCard(updataJson.getString("idCard"));
        user.setLocation(updataJson.getString("location"));
        user.setApplicationTitle(updataJson.getString("applicationTitle"));
        user.setUserInfo(updataJson.getString("userInfo"));
        user.setSCDMedicalRecord(updataJson.getString("medicalRecord"));
        user.setVCLAsset(updataJson.getString("asset"));
        user.setReceiveAccount(updataJson.getString("receiveAccount"));
        user.setTime(updataJson.getString("time"));
        user.setTarget(updataJson.getString("target"));

        System.out.println(user);
        Map<String, String> encryptResult = EncryptUtils.encrypt(user);
        //TODO 前两个存到数据库，后两个存到区块链
        String userPrivateKey = encryptResult.get("userPrivateKey");
        String blindedCertificateSignature = encryptResult.get("blindedCertificateSignature");
        String certificateData = encryptResult.get("certificateData");
        String certificateTemplate = encryptResult.get("certificateTemplate");

        System.out.println(userPrivateKey);
        System.out.println(blindedCertificateSignature);
        TransactionReceipt receipt = funds.updateApplication(user.getUserId(), certificateData, certificateTemplate);

        return receipt;
    }

    /**
     * 删除求助信息
     *
     * @param userId 用户信息
     * @return 执行结果
     */
    public TransactionReceipt removeApplication(String userId, FundsApplicationCipher funds) {
        TransactionReceipt receipt = funds.removeAll(userId);
        return receipt;
    }

    /**
     * 初始化资助统计快
     *
     * @param userId 用户id
     * @return 执行结果
     */
    public TransactionReceipt initFundsCompute(String userId, FundsApplicationCipher funds) {
        return funds.initFundsCompute(userId);
    }

    /**
     * 有人捐款后，更新求助者的筹款信息
     *
     * @param userId    求助者id
     * @param fundsSum  捐款金额
     * @param fundingId 捐款者
     * @return 执行结果
     */
    public TransactionReceipt updateFundsCompute(String userId, BigInteger fundsSum, String fundingId, String fundsNum, String fundingTime, FundsApplicationCipher funds) {
        return funds.updateFundsCompute(userId, fundsSum, fundingId, fundsNum, fundingTime);
    }

    /**
     * 获取求助者当前的筹款信息
     *
     * @param userId 用户id
     * @return 筹款金额, 捐款者id, 捐款金额，捐款时间
     */
    public JSONObject getUserFundsInfo(String userId, FundsApplicationCipher funds) throws ContractException {
        Tuple4<BigInteger, String, String, String> userFundsInfoTuple = funds.getUserFundsInfo(userId);
        BigInteger fundSum = userFundsInfoTuple.getValue1();
        String allFundingId = userFundsInfoTuple.getValue2();
        String fundsNum = userFundsInfoTuple.getValue3();
        String fundingTime = userFundsInfoTuple.getValue4();
        if (allFundingId.equals("该用户不存在")) {
            return JSONObject.parseObject(allFundingId);
        } else {
            JSONObject userFundsInfoJson = new JSONObject();
            String[] fundingIds = allFundingId.split(",");
            String[] fundingNums = fundsNum.split(",");
            String[] fundingTimes = fundingTime.split(",");
            String[] fundingPerson = new String[fundingIds.length];
            if (allFundingId.equals("")) {
                System.out.println("yes");
                userFundsInfoJson.put("fundSum", fundSum);
                userFundsInfoJson.put("fundingPerson", "");
                System.out.println("length: " + userFundsInfoJson.getString("fundingPerson").length());
            } else {
                for (int i = 0; i < fundingIds.length; i++) {
                    String tempStr = fundingIds[i] + "," + fundingNums[i] + "," + fundingTimes[i];
                    fundingPerson[i] = tempStr;
                }
                userFundsInfoJson.put("fundSum", fundSum);
                userFundsInfoJson.put("fundingPerson", fundingPerson);
            }
            return userFundsInfoJson;
        }
    }

    /**
     * 初始化求助者提现块
     *
     * @param userId 求助者id
     * @return 执行结果
     */
    public TransactionReceipt initFundsWithdrawal(String userId, FundsApplicationCipher funds) {
        return funds.initFundsWithdrawal(userId);
    }

    /**
     * 求助者提现后，更新区块
     *
     * @param userId           用户id
     * @param withdrawalAmount 用户体现金额
     * @return 执行结果
     */
    public TransactionReceipt updateFundsWithdrawal(String userId, BigInteger withdrawalAmount, FundsApplicationCipher funds) {
        return funds.updateFundsWithdrawal(userId, withdrawalAmount);
    }

    /**
     * 获取用户的提款数据
     *
     * @param userId 用户id
     * @return 提款信息
     */
    public JSONObject getUserFundsWithdrawal(String userId, FundsApplicationCipher funds) throws ContractException {
        Tuple3<BigInteger, BigInteger, BigInteger> userFundsWithdrawalTuple = funds.getUserFundsWithdrawal(userId);
        BigInteger withdrawalAmount = userFundsWithdrawalTuple.getValue1();
        BigInteger balance = userFundsWithdrawalTuple.getValue2();
        BigInteger withdrawalSum = userFundsWithdrawalTuple.getValue3();
        JSONObject userFundsWithdrawalJson = new JSONObject();
        userFundsWithdrawalJson.put("withdrawalAmount", withdrawalAmount);
        userFundsWithdrawalJson.put("balance", balance);
        userFundsWithdrawalJson.put("withdrawalSum", withdrawalSum);
        return userFundsWithdrawalJson;
    }

    /**
     * 初始化花费明细块
     *
     * @param userId 用户id
     * @return
     */
    public TransactionReceipt initCostDetail(String userId, FundsApplicationCipher funds) {
        return funds.initCostDetail(userId);
    }

    /**
     * 更新用户明细快
     *
     * @param userId 用户id
     * @param cost   花费金额
     * @param detail 使用明细
     * @return
     */
    public TransactionReceipt updateCostDetail(String userId, BigInteger cost, String detail, String costStr, FundsApplicationCipher funds) {
        return funds.updateCostDetail(userId, cost, detail, costStr);
    }

    /**
     * 获取用户消费明细
     *
     * @param userId 用户id
     * @return 用户消费明细
     */
    public JSONObject getUserCostDetail(String userId, FundsApplicationCipher funds) throws ContractException {
        Tuple3<BigInteger, String, String> detailTuple = funds.getUserCostDetail(userId);
        BigInteger costSum = detailTuple.getValue1();
        String detail = detailTuple.getValue2();
        String costStr = detailTuple.getValue3();
        if (detail.equals("该ID不存在")) {
            return null;
        }
        String[] details = detail.split(",");
        String[] costStrs = costStr.split(",");
        String[] detailArray = new String[details.length];
        String temp;
        for (int i = 0; i < details.length; i++) {
            temp = costStrs[i] + "," + details[i];
            detailArray[i] = temp;
        }
        JSONObject result = new JSONObject();
        result.put("costSum", costSum);
        result.put("detail", detailArray);
        return result;
    }

    /**
     * 获得所有用户的求助信息
     *
     * @return 所有用户的求助信息
     */
    public JSONObject getAllUserApplication(FundsApplicationCipher funds) throws ContractException, IOException {
        List<String> allUserApplication = funds.getAllUserApplication();
        System.out.println(allUserApplication.get(0));
        JSONObject userApplicationInfo = new JSONObject();
        List<Map> list = new LinkedList<>();
        for (int i = 0; i < allUserApplication.size(); i++) {
            String tempStr = allUserApplication.get(i);
            String[] userInfo = tempStr.split("`");
//            System.err.println("userInfo length"+userInfo.length);
            if (userInfo.length <= 1) break;
            String userId = userInfo[0];
            String certificateData = userInfo[1];
            String certificateTemplate = userInfo[2];

            Map<String, String> userMap = new HashMap<>();
            userCipher usercipher = sqlservice.getCipher(userId);
            String userPrivateKey = usercipher.getUserPrivateKey();
            String blindedCertificateSignature = usercipher.getBlindedCertificateSignature();
            userMap.put("userPrivateKey", userPrivateKey);
            userMap.put("blindedCertificateSignature", blindedCertificateSignature);
            userMap.put("certificateData", certificateData);
            userMap.put("certificateTemplate", certificateTemplate);

            //需要披露的字段
            List<String> userInfoList = new ArrayList<>();
            userInfoList.add("name");
            userInfoList.add("location");
            userInfoList.add("applicationTitle");
            userInfoList.add("userInfo");
            userInfoList.add("SCDMedicalRecord");
//            userInfoList.add("VCLAsset");
            userInfoList.add("receiveAccount");
            userInfoList.add("time");
            userInfoList.add("target");
            Map<String, String> userInfoMap = new HashMap<>();
            userInfoMap = EncryptUtils.selectiveDisclosure(userMap, userInfoList);
            //将披露结果转为json
//            JSONObject userInfoJson = new JSONObject();
//            userInfoJson.put("userId",userId);
//            userInfoJson.put("name", userInfoMap.get("name"));
//            userInfoJson.put("location", userInfoMap.get("location"));
//            userInfoJson.put("applicationTitle", userInfoMap.get("applicationTitle"));
//            userInfoJson.put("userInfo", userInfoMap.get("userInfo"));
//            userInfoJson.put("SCDMedicalRecord", userInfoMap.get("SCDMedicalRecord"));
//            userInfoJson.put("VCLAsset", userInfoMap.get("VCLAsset"));
//            userInfoJson.put("receiveAccount", userInfoMap.get("receiveAccount"));
//            userInfoJson.put("time", userInfoMap.get("time"));
//            userInfoJson.put("target",userInfoMap.get("target"));
//            //userInfoJson.put("id", i);
//            userApplicationInfo.put("Application"+i, userInfoJson);
            Map<String, String> userInfoMap2 = new HashMap<>();
            userInfoMap2.put("userId", userId);
            userInfoMap2.put("name", userInfoMap.get("name"));
            userInfoMap2.put("location", userInfoMap.get("location"));
            userInfoMap2.put("applicationTitle", userInfoMap.get("applicationTitle"));
            userInfoMap2.put("userInfo", userInfoMap.get("userInfo"));
            userInfoMap2.put("SCDMedicalRecord", userInfoMap.get("SCDMedicalRecord"));
            userInfoMap2.put("VCLAsset", VCLAsset(userMap));
            userInfoMap2.put("receiveAccount", userInfoMap.get("receiveAccount"));
            userInfoMap2.put("time", userInfoMap.get("time"));
            userInfoMap2.put("target", userInfoMap.get("target"));
            list.add(userInfoMap2);
        }
        userApplicationInfo.put("application", list);
        return userApplicationInfo;
    }

    public String VCLAsset(Map<String, String> userMap) {
        if (EncryptUtils.verifyField(userMap, 50000)) {
//            System.out.println("Asset<50000");
            return "资产 < 50000";
        } else if (EncryptUtils.verifyField(userMap, 100000)) {
//            System.out.println("50000<Asset<100000");
            return "50000 < 资产 < 100000";
        } else if (EncryptUtils.verifyField(userMap, 200000)) {
//            System.out.println("100000<Asset<200000");
            return "100000 < 资产 < 200000";
        } else {
//            System.out.println("Asset>200000");
            return "资产 > 200000";
        }
    }
}