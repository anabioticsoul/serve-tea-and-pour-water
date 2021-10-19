import request from '@/utils/request'

// 查询求助信息列表
export function listShow(query) {
  return request({
    url: '/helpinfo/show/list',
    method: 'post',
    params: query
  })
}

// 查询求助信息详细
export function getShow(userinfo) {
  return request({
    url: '/helpinfo/show/list' + userinfo,
    method: 'post'
  })
}


// 导出求助信息
export function exportShow(query) {
  return request({
    url: '/helpinfo/show/list',
    method: 'post',
    params: query
  })
}


