<template>
  <div class="app-container">
    <!--动态将图片轮播图的容器高度设置成与图片一致-->
    <el-carousel :height="bannerHeight + 'px'">
      <!--遍历图片地址,动态生成轮播图-->
      <el-carousel-item v-for="item in img_list" :key="item">
        <img :src="item" alt="">
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
import {initFundingBlock, myFundShow} from "@/api/application/myFund";

export default {
  name: "Banner",
  data() {
    return {
      // 图片地址数组
      img_list: [
        require("../assets/images/login-background.jpg"),
        require("../assets/images/login-background2.jpg"),
        require("../assets/images/login-background3.jpg"),
      ],
      // 图片父容器高度
      bannerHeight: 1000,
      // 浏览器宽度
      screenWidth: 0,
    }
  },
  created() {
    this.initBlock();
  },
  methods: {
    setSize: function () {
      // 通过浏览器宽度(图片宽度)计算高度
      this.bannerHeight = 800 / 1920 * this.screenWidth;
    },
    initBlock() {
      myFundShow().then(response => {
        let data = response.data;
        // console.log(data)
        if (data.Score === -1) {

          initFundingBlock().then(response => {
            //TODO: DO NOTHING
            this.$router.go(0);
          })
        }
      });
    },
  },
  mounted() {
    // 首次加载时,需要调用一次
    this.screenWidth = window.innerWidth;
    this.setSize();
    // 窗口大小发生改变时,调用一次
    window.onresize = () => {
      this.screenWidth = window.innerWidth;
      this.setSize();
    }
  },

}
</script>

<style scoped>
.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 300px;
  margin: 0;
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n+1) {
  background-color: #d3dce6;
}

img {
  /*设置图片宽度和浏览器宽度一致*/
  width: 100%;
  height: inherit;
}
</style>
　　
