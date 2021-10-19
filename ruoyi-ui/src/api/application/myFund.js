import request from "@/utils/request";

export function myFundShow() {
  return request({
    url: '/getFunding',
    method: 'post',
  })
}

//get获取其他人信息
export function fundPersonShow(id) {
  return request({
    url: '/get',
    method: 'post',
    params: {
      userId: id
    }
  })
}

export function fundPersonCostShow(id) {
  return request({
    url: '/getCostDetail',
    method: 'post',
    params: {
      userId: id
    }
  })
}

export function initFundingBlock(id) {
  return request({
    url: '/initFunding',
    method: 'get',
    params: {
      userId: id
    }
  })
}

