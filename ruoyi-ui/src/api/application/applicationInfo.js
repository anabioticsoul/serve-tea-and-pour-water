import request from "@/utils/request";
import user from "@/store/modules/user";

//提交申请信息接口
export function info(data) {
  return request({
    url: '/insert',
    method: 'post',
    data: data
  })
}

//展示申请信息接口
export function infoShow() {
  return request({
    //TODO:
    url: '/get',

    method: 'post',
  })
}


//资助明细显示接口: 资助人、资助金额、资助时间、总金额
export function supportInfoShow() {
  return request({
    url: '/getFundsInfo',
    method: 'post',
  })
}

//提现资金花费情况显示接口: 用户的总花费金额、总提现金额
export function amountShow() {
  return request({
    url: '/getTotalAmount',
    method: 'post',
  })
}

//申请提现：输入提现金额
export function withdrawalAmount(Amount) {
  return request({
    url: '/updateWithdrawalInfo2/' + Amount,
    method: 'get',
  })
}


//提交发票接口
export function updateBill(data) {
  return request({
    url: '/updateCostDetail',
    method: 'post',
    data: data
  })
}


//查看支出明细接口:事项、总金额
export function getSpentRecord() {
  return request({
    url: '/getCostDetail',
    method: 'post',
  })
}

//获取所有求助者信息：
export function getAllApplicationInfo() {
  return request({
    url: '/getAllUserInfo',
    method: 'post',
  })
}

//捐款接口
export function updateFundsInfo(data) {
  return request({
    url: '/updateFundsInfo',
    method: 'post',
    data: data
  })
}
