import request from '@/utils/request'

// 注册方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// export function initFundingBlock(id){
//   return request({
//     url:'/initFunding',
//     method: 'get',
//     params:{
//       userId:id
//     }
//   })
// }
//
// export function testFunction(){
//  // this.$alert("test");
//   alert("test")
//   console.log("test")
// }
