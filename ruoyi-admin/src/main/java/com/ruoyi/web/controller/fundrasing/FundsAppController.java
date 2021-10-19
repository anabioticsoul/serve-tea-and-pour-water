package com.ruoyi.web.controller.fundrasing;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.framework.web.domain.server.Sys;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.impl.SysRoleServiceImpl;
import com.ruoyi.web.service.FundsApplicationService;
import com.ruoyi.web.service.fundingUserService;
import com.ruoyi.web.solidity.FundingUser;
import com.ruoyi.web.solidity.FundsApplicationCipher;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class FundsAppController {
    @Resource
    private FundsApplicationService fundsApplicationService;
    @Resource
    private fundingUserService fundingUserService;
    @Resource
    private SysRoleServiceImpl roleService;
    @Autowired
    private ISysUserService userService;

    /**
     * 获取区块高度
     *
     * @return 区块高度
     */
    @RequestMapping("/getHeight")
    public AjaxResult getHeight() {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        BigInteger res = fundsApplicationService.getHeight(client);
        System.out.println(res);
        return AjaxResult.success("区块高度查询成功", res);
    }

    /**
     * 发布新的求助信息,初始化一个资助申请块，一个资助统计块，一个资助提现块，一个花费明细块
     *
     * @return 区块内容
     */
    @RequestMapping("/insert")
    public AjaxResult insertFundsInfo(@RequestBody String data) {
        JSONObject fundsRawJson = JSONObject.parseObject(data);
        JSONObject fundsJson = new JSONObject();
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        //财产计算，生成一个新的json字符串
        String userId;
        userId = fundsRawJson.getString("userId");
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
            fundsJson.put("userId", userId);
        } else {
            fundsJson.put("userId", userId);
        }
        fundsJson.put("name", fundsRawJson.getString("name"));
        fundsJson.put("phone", fundsRawJson.getString("phone"));
        fundsJson.put("idCard", fundsRawJson.getString("idCard"));
        fundsJson.put("location", fundsRawJson.getString("location"));
        fundsJson.put("applicationTitle", fundsRawJson.getString("applicationTitle"));
        fundsJson.put("userInfo", fundsRawJson.getString("userInfo"));
        fundsJson.put("medicalRecord", fundsRawJson.getString("medicalRecord"));


        //int asset=fundsRawJson.getIntValue("annulIncome")+fundsRawJson.getIntValue("housePrice")+fundsRawJson.getIntValue("carPrice")+fundsRawJson.getIntValue("finance");
        int asset = Integer.parseInt(fundsRawJson.getString("annualIncome")) + Integer.parseInt(fundsRawJson.getString("housePrice")) + Integer.parseInt(fundsRawJson.getString("carPrice")) + Integer.parseInt(fundsRawJson.getString("finance")) + Integer.parseInt(fundsRawJson.getString("otherFunds"));
        fundsJson.put("asset", String.valueOf(asset));
        System.err.println("asset: " + asset);

        //获取当前时间
        fundsJson.put("receiveAccount", fundsRawJson.getString("receiveAccount"));
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fundsJson.put("time", sdf.format(d));
        fundsJson.put("target", fundsRawJson.getString("target"));

        try {
            fundsApplicationService.insertFundsInfo(fundsJson, funds, client);//初始化资助申请块
            fundsApplicationService.initFundsWithdrawal(fundsJson.getString("userId"), funds);//初始化提现块
            fundsApplicationService.initFundsCompute(fundsJson.getString("userId"), funds);//初始化资助统计块
            fundsApplicationService.initCostDetail(userId, funds);//初始化花费明细块
            SysUser newUser = userService.selectUserByUserName(userId);
            Long uid = newUser.getUserId();
            roleService.updateUserRole((long) 3, uid);
            return AjaxResult.success("求助信息发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("求助信息发布失败");
        }
    }

    /**
     * 删除求助信息
     *
     * @param userId 用户名字
     * @return 设置区块内容
     */
    @RequestMapping("/delete")
    public AjaxResult removeApplication(@RequestParam(required = false) String userId) {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        try {
            fundsApplicationService.removeApplication(userId, funds);
            return AjaxResult.success("求助信息删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("求助信息删除失败");
        }
    }

    /**
     * 更新用户的求助信息
     *
     * @param userId 用户id
     * @param data   待更新的求助信息
     * @return 更新执行结果
     */
    @RequestMapping("/update")
    public AjaxResult updateApplication(@RequestParam(required = false) String userId, @RequestBody String data) {
        JSONObject updateJson = JSONObject.parseObject(data);
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        try {
            fundsApplicationService.updataApplication(userId, updateJson, funds);
            return AjaxResult.success("求助信息更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("求助信息更新失败");
        }
    }

    /**
     * 获取求助者的求助信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    @RequestMapping("get")
    public AjaxResult getUserApplicationInfo(@RequestParam(required = false) String userId) throws ContractException, IOException {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        JSONObject ApplicationInfo = fundsApplicationService.getUserApplicationInfo(userId, funds);
        if (ApplicationInfo == null) {
            return AjaxResult.error("该ID不存在");
        } else {
            return AjaxResult.success("求助信息查询成功", ApplicationInfo);
        }
    }

    @RequestMapping("getBySelf")
    public AjaxResult getUserApplicationInfoBySelf(@RequestParam(required = false) String userId) throws Exception {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        JSONObject ApplicationInfo = fundsApplicationService.getUserApplicationInfoBySelf(userId, funds);
        if (ApplicationInfo == null) {
            return AjaxResult.error("该ID不存在");
        } else {
            return AjaxResult.success("求助信息自查询成功", ApplicationInfo);
        }
    }

    /**
     * 更新筹款信息
     *
     * @param data 待更新的筹款信息
     * @return 执行结果
     */
    @RequestMapping("/updateFundsInfo")
    public AjaxResult updateFundsCompute(@RequestBody String data) {
        System.err.println("data: " + data);
        JSONObject updateFundsComputeJson = JSONObject.parseObject(data);
        String userId = updateFundsComputeJson.getString("userId");
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        BigInteger fundsSum = updateFundsComputeJson.getBigInteger("donation");
        String fundingId = updateFundsComputeJson.getString("fundingId");
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fundingTime = sdf.format(d);

        String fundNum = fundsSum.toString();

        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        FundingUser fundings = fundingUserService.loadContract(client, cryptoKeyPair);
        try {
//            fundsApplicationService.updateFundsCompute(userId, fundsSum, fundingId, fundNum,fundingTime,funds); //更新资助统计快信息
//            fundingUserService.updateFundingInfo(fundingId,fundsSum,userId,fundNum,fundingTime,fundings);//更新资助块信息

            fundsApplicationService.updateFundsCompute(fundingId, fundsSum, userId, fundNum, fundingTime, funds); //更新资助统计快信息
            fundingUserService.updateFundingInfo(userId, fundsSum, fundingId, fundNum, fundingTime, fundings);//更新资助块信息

            return AjaxResult.success("筹款信息更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("筹款信息更新失败");
        }
    }

    /**
     * 获取用户的筹款信息
     *
     * @param userId 用户id
     * @return 筹款信息
     */
    @RequestMapping("/getFundsInfo")
    public AjaxResult getUserFundsInfo(@RequestParam(required = false) String userId) throws ContractException {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        JSONObject fundsInfo = fundsApplicationService.getUserFundsInfo(userId, funds);
        return AjaxResult.success("查询用户筹款信息成功", fundsInfo);
    }

    /**
     * 更新用户的取款信息
     *
     * @param data 待更新取款信息
     * @return 命令执行结果
     */
    @RequestMapping("/updateWithdrawalInfo")
    public AjaxResult updateFundsWithdrawal(@RequestBody String data) {
        JSONObject fundsWithdrawalJson = JSONObject.parseObject(data);
        String userId = fundsWithdrawalJson.getString("userId");
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        BigInteger withdrawalAmount = fundsWithdrawalJson.getBigInteger("withdrawalAmount");
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        try {
            fundsApplicationService.updateFundsWithdrawal(userId, withdrawalAmount, funds);
            return AjaxResult.success("更新取款信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("更新取款信息失败");
        }
    }

    @GetMapping("/updateWithdrawalInfo2/{Amount}")
    public AjaxResult updateFundsWithdrawal2(@PathVariable("Amount") String Amount) {
        System.out.println("Amount" + Amount);
        String userId = fundsApplicationService.getUserId();
        BigInteger withdrawalAmount = new BigInteger(Amount);
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        try {
            fundsApplicationService.updateFundsWithdrawal(userId, withdrawalAmount, funds);
            return AjaxResult.success("更新取款信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("更新取款信息失败");
        }
    }

    /**
     * 获取求助者的取款信息
     *
     * @param userId 用户id
     * @return 取款信息:
     */
    @RequestMapping("/getWithdrawalInfo")
    public AjaxResult getFundsWithdrawal(@RequestParam(required = false) String userId) throws ContractException {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        JSONObject withdrawalInfo = fundsApplicationService.getUserFundsWithdrawal(userId, funds);
        return AjaxResult.success("查询用户取款信息成功", withdrawalInfo);
    }

    /**
     * 更新用户消费明细块
     *
     * @param data 待更新的用户消费信息
     * @return 更新结果
     */
    @RequestMapping("/updateCostDetail")
    public AjaxResult updateCostDetail(@RequestBody String data) {
        JSONObject costDetailJson = JSONObject.parseObject(data);
        String userId = costDetailJson.getString("userId");
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        BigInteger cost = costDetailJson.getBigInteger("cost");
        String detail = costDetailJson.getString("detail");
        String costStr = String.valueOf(cost);
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        try {
            fundsApplicationService.updateCostDetail(userId, cost, detail, costStr, funds);
            return AjaxResult.success("更新消费明细成功");
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("更新消费信息失败");
        }
    }

    /**
     * 查询用户的消费明细
     *
     * @param userId 用户id
     * @return 用户的消费明细 : 总花费、明细
     */
    @RequestMapping("/getCostDetail")
    public AjaxResult getUserCostDetail(@RequestParam(required = false) String userId) {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        try {
            JSONObject detail = fundsApplicationService.getUserCostDetail(userId, funds);
            //System.out.println(detail);
            if (detail == null) {
                return AjaxResult.error("查询消费明细失败");
            } else {
                return AjaxResult.success("查询消费明细成功", detail);
            }
        } catch (ContractException e) {
            e.printStackTrace();
            return AjaxResult.error("查询消费明细失败");
        }
    }

    /**
     * 查询所有用户的信息
     *
     * @return 所有用户的信息
     */
    @RequestMapping("/getAllUserInfo")
    public AjaxResult getAllUserInfo() throws IOException {
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        try {
            JSONObject allUserInfo = fundsApplicationService.getAllUserApplication(funds);
            return AjaxResult.success("查询用户求助信息成功", allUserInfo);
        } catch (ContractException e) {
            e.printStackTrace();
            return AjaxResult.error("查询用户求助信息失败");
        }
    }

    /**
     * 获取用户的总花费金额和总提现金额
     *
     * @param userId 用户Id
     * @return 用户的总花费金额和总提现金额
     */
    @RequestMapping("/getTotalAmount")
    public AjaxResult getTotalAmount(@RequestParam(required = false) String userId) {
        //System.out.println("getTotalAmount");
        BcosSDK bcosSDK = fundsApplicationService.createSDK();
        Client client = fundsApplicationService.createClient(bcosSDK);
        CryptoKeyPair cryptoKeyPair = fundsApplicationService.createKeyPair(client);
        FundsApplicationCipher funds = fundsApplicationService.loadContract(client, cryptoKeyPair);
        if (userId == null) {
            userId = fundsApplicationService.getUserId();
        }
        try {
            JSONObject detail = fundsApplicationService.getUserCostDetail(userId, funds);
            String totalAmount = detail.getString("costSum");
            JSONObject withdrawalInfo = fundsApplicationService.getUserFundsWithdrawal(userId, funds);
            String totalWithdrawalAmount = withdrawalInfo.getString("withdrawalSum");
            JSONObject result = new JSONObject();
            result.put("totalAmount", totalAmount);
            result.put("totalWithdrawalAmount", totalWithdrawalAmount);
            return AjaxResult.success("查询成功", result);
        } catch (ContractException e) {
            e.printStackTrace();
            return AjaxResult.error("查询失败");
        }
    }
}