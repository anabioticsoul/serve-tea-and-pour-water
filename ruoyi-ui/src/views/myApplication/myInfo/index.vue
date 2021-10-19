<template>
  <div class="app-container">
    <el-card>
      <div style="line-height: 60px;font-size: 25px">
        <i class="el-icon-bangzhu"></i>
        {{ user.applicationTitle }}
      </div>
      <div style="line-height: 40px">
        <el-row>
          <el-col :span="8">
            <div class="grid-content bg-purple">筹款金额: {{ user.target }}</div>
          </el-col>
          <el-col :span="8">
            <div class="grid-content bg-purple-light">已经筹到: {{ obtainFunds }}</div>
          </el-col>
          <el-col :span="8">
            <div class="grid-content bg-purple">帮助次数: {{ total }}</div>
          </el-col>
        </el-row>
      </div>
      <el-progress :text-inside="true" :stroke-width="26" :percentage="percentage"></el-progress>
    </el-card>
    <el-card
      style="font-size: 20px;font-family: 'Microsoft YaHei', DengXian, SimSun, 'Segoe UI', Tahoma, Helvetica, sans-serif">
      <el-descriptions class="margin-top" :column="2" label-style="width:120px;font-size:15px;color:#303133" border>
        <!--        :size="size"-->
        <el-descriptions-item :span="1">
          <template slot="label">
            <i class="el-icon-user"></i>
            姓名
          </template>
          {{ user.name }}
        </el-descriptions-item>
        <el-descriptions-item>
          <template slot="label">
            <i class="el-icon-mobile-phone"></i>
            手机号
          </template>
          {{ user.phone }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template slot="label">
            <i class="el-icon-postcard"></i>
            身份证号
          </template>
          {{ user.idCard }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template slot="label">
            <i class="el-icon-location-information"></i>
            家庭住址
          </template>
          {{ user.location }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template slot="label">
            <i class="el-icon-document"></i>
            基本信息
          </template>
          {{ user.userInfo }}
        </el-descriptions-item>
        <el-descriptions-item :span="2">
          <template slot="label">
            <i class="el-icon-first-aid-kit"></i>
            患病信息
          </template>
          {{ user.SCDMedicalRecord.split(',')[0].split('"')[1] }}
        </el-descriptions-item>
        <!--        <el-descriptions-item :span="2">
                  <template slot="label">
                    <i class="el-icon-document-checked"></i>
                    患病证明
                  </template>
                  {{ user.recordCipher }}
                </el-descriptions-item>-->
        <el-descriptions-item :span="2">
          <template slot="label">
            <i class="el-icon-money"></i>
            资产情况
          </template>
          {{ user.VCLAsset }}
        </el-descriptions-item>
        <!--        <el-descriptions-item :span="2">
                  <template slot="label">
                    <i class="el-icon-document-checked"></i>
                    资产证明
                  </template>
                  {{ user.assetCipher }}
                </el-descriptions-item>-->

      </el-descriptions>
    </el-card>

  </div>
</template>


<!--<template>
  <div class="app-container">
    <li class="list-group-item">
      <svg-icon icon-class="user" />求助标题
      <div class="pull-right">{{ user.applicationTitle }}</div>
    </li>
    <li class="list-group-item">
      <svg-icon icon-class="user" />筹款金额
      <div class="pull-right">{{ user.target }}</div>
    </li>
    <li class="list-group-item">
      <svg-icon icon-class="user" />已经筹到
      <div class="pull-right">{{ user.obtainFunds }}</div>
    </li>
    <li class="list-group-item">
      <svg-icon icon-class="user" />帮助次数
      <div class="pull-right">{{ user.helpNum }}</div>
    </li>

    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>个人信息</span>
          </div>
          <div>
            <ul class="list-group list-group-striped">
              <li class="list-group-item">
                <svg-icon icon-class="user" />姓名
                <div class="pull-right">{{ user.name }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="phone" />手机号码
                <div class="pull-right">{{ user.phone }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="email" />身份证号
                <div class="pull-right">{{ user.idCard }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />家庭住址
                <div class="pull-right">{{ user.location }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />基本信息
                <div class="pull-right">{{ user.location }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />患病信息
                <div class="pull-right">{{ user.medicalRecord }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />患病证明
                <div class="pull-right">{{ user.medicalRecord }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />资产情况
                <div class="pull-right">{{ user.recordCipher }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />资产证明
                <div class="pull-right">{{ user.location }}</div>
              </li>


            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>-->

<script>
import {infoShow, supportInfoShow} from "@/api/application/applicationInfo";

export default {

  data() {
    return {
      user: {},
      percentage: 0,
      obtainFunds: undefined,
      total: undefined
    };
  },
  created() {
    this.getUser();
    this.getobtainFunds();
  },
  methods: {
    getUser() {
      infoShow().then(response => {
        this.user = response.data;
        console.log(this.user)
        this.percentage = this.obtainFunds / this.user.target * 100;
      });
    },
    getobtainFunds() {
      supportInfoShow().then(response => {
        //console.log(response.data)
        this.obtainFunds = response.data.fundSum;
        this.total = response.data.fundingPerson.length;
      });
    },
  }
};
</script>
