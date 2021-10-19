<template>
  <div class="app-container">
    <el-card>
      <el-form ref="elForm" :model="formData" :rules="rules" size="medium" label-width="150px"
               label-position="right">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" show-word-limit clearable
                    prefix-icon='el-icon-user' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" :maxlength="11" show-word-limit clearable
                    prefix-icon='el-icon-mobile-phone' :style="{width: '50%'}"></el-input>
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" :maxlength="18" show-word-limit clearable
                    prefix-icon='el-icon-postcard' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="家庭住址" prop="location">
          <el-input v-model="formData.location" placeholder="请输入家庭住址" clearable
                    prefix-icon='el-icon-location-outline' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="求助标题" prop="applicationTitle">
          <el-input v-model="formData.applicationTitle" placeholder="请输入求助标题" clearable
                    prefix-icon='el-icon-help' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="银行卡号" prop="bankCard">
          <el-input v-model="formData.receiveAccount" placeholder="请输入银行卡号" clearable
                    prefix-icon='el-icon-money' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="目标筹款金额" prop="target">
          <el-input-number v-model="formData.target" placeholder="请输入目标筹款金额"></el-input-number>
        </el-form-item>

        <el-form-item label="基本信息" prop="userInfo">
          <el-input v-model="formData.userInfo" type="textarea" placeholder="请输入基本信息"
                    :autosize="{minRows: 4, maxRows: 4}" :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item label="患病信息" prop="medicalRecord">
          <el-cascader v-model="formData.medicalRecord" :options="medicalRecordOptions"
                       :props="medicalRecordProps" :style="{width: '50%'}" placeholder="请选择患病信息"
                       clearable></el-cascader>
        </el-form-item>
        <el-form-item label="诊断结果" prop="medicalPhoto">
          <el-upload ref="medicalPhoto" :file-list="medicalPhotofileList" :action="medicalPhotoAction"
                     :before-upload="medicalPhotoBeforeUpload" list-type="picture" accept="image/*"
                     name="medicalPhoto" :style="{width: '50%'}" :auto-upload="false">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="家庭年收入" prop="annualIncome">
          <el-input-number v-model="formData.annualIncome" placeholder="请输入家庭年收入"></el-input-number>
        </el-form-item>
        <el-form-item label="房产情况" prop="housePrice">
          <el-input-number v-model="formData.housePrice" placeholder="请输入房产情况"></el-input-number>
        </el-form-item>
        <el-form-item label="房产证" prop="housePhoto">
          <el-upload ref="housePhoto" :file-list="housePhotofileList" :action="housePhotoAction"
                     :before-upload="housePhotoBeforeUpload" list-type="picture" accept="image/*" name="housePhoto"
                     :style="{width: '50%'}">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="车产情况" prop="carPrice">
          <el-input-number v-model="formData.carPrice" placeholder="请输入车产情况"></el-input-number>
        </el-form-item>
        <el-form-item label="车本" prop="carPhoto">
          <el-upload ref="carPhoto" :file-list="carPhotofileList" :action="carPhotoAction"
                     :before-upload="carPhotoBeforeUpload" list-type="picture" accept="image/*" name="carPhoto"
                     :style="{width: '50%'}">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="家庭金融资产" prop="finance">
          <el-input-number v-model="formData.finance" placeholder="请输入家庭金融资产"></el-input-number>
        </el-form-item>
        <el-form-item label="其他平台筹款情况" prop="otherFunds">
          <el-input-number v-model="formData.otherFunds" placeholder="请输入其他平台筹款情况"></el-input-number>
        </el-form-item>

        <el-form-item label="保险情况" prop="insurance">
          <el-checkbox-group v-model="formData.insurance" size="medium">
            <el-checkbox v-for="(item, index) in insuranceOptions" :key="index" :label="item.value"
                         :disabled="item.disabled">{{item.label}}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="治疗进展" prop="treatmentProcess">
          <el-input v-model="formData.treatmentProcess" type="textarea" placeholder="请输入治疗进展"
                    :autosize="{minRows: 4, maxRows: 4}" :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item size="large">
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script>

import {info} from "@/api/application/applicationInfo";
import {username} from "@/views/login.vue"
import Cookies from "js-cookie";

export default {
  components: {},
  props: [],
  data() {
    return {
      formData: {
        userId: undefined,
        name: '',
        phone: undefined,
        idCard: undefined,
        location: undefined,
        applicationTitle: undefined,
        receiveAccount: undefined,
        target: undefined,
        userInfo: undefined,
        medicalRecord: [1, 2],
        medicalPhoto: null,
        annualIncome: undefined,
        housePrice: undefined,
        housePhoto: null,
        carPrice: undefined,
        carPhoto: null,
        finance: undefined,
        otherFunds: undefined,
        insurance: [],
        treatmentProcess: undefined,
      },
      rules: {
        name: [{
          required: true,
          message: '请输入姓名',
          trigger: 'blur'
        }],
        phone: [{
          required: true,
          message: '请输入手机号',
          trigger: 'blur'
        }, {
          pattern: /^1(3|4|5|7|8|9)\d{9}$/,
          message: '手机号格式错误',
          trigger: 'blur'
        }],
        idCard: [{
          required: true,
          message: '请输入身份证号',
          trigger: 'blur'
        }],
        location: [{
          required: true,
          message: '请输入家庭住址',
          trigger: 'blur'
        }],
        applicationTitle: [{
          required: true,
          message: '请输入求助标题',
          trigger: 'blur'
        }],
        receiveAccount: [{
          required: true,
          message: '请输入银行卡号',
          trigger: 'blur'
        }],
        target: [{
          required: true,
          message: '请输入目标筹款金额',
          trigger: 'blur'
        }],
        userInfo: [{
          required: true,
          message: '请输入基本信息',
          trigger: 'blur'
        }],
        medicalRecord: [{
          required: true,
          type: 'array',
          message: '请至少选择一个medicalRecord',
          trigger: 'change'
        }],
        annualIncome: [{
          required: true,
          message: '请输入家庭年收入',
          trigger: 'blur'
        }],
        housePrice: [{
          required: true,
          message: '请输入房产情况',
          trigger: 'blur'
        }],
        carPrice: [{
          required: true,
          message: '请输入车产情况',
          trigger: 'blur'
        }],
        finance: [{
          required: true,
          message: '请输入家庭金融资产',
          trigger: 'blur'
        }],
        otherFunds: [{
          required: true,
          message: '请输入其他平台筹款情况',
          trigger: 'blur'
        }],
        insurance: [{
          required: true,
          type: 'array',
          message: '请至少选择一个insurance',
          trigger: 'change'
        }],
        treatmentProcess: [{
          required: true,
          message: '请输入治疗进展',
          trigger: 'blur'
        }],
      },
      medicalPhotoAction: 'http://127.0.0.1:8080/application/applicationInfo/photo',
      medicalPhotofileList: [],
      housePhotoAction: 'http://127.0.0.1:8080/application/applicationInfo/photo',
      housePhotofileList: [],
      carPhotoAction: 'http://127.0.0.1:8080/application/applicationInfo/photo',
      carPhotofileList: [],
      medicalRecordOptions: [{
        "label": "肿瘤",
        "value": "肿瘤",
        "id": 100,
        "children": [{
          "label": "良性肿瘤",
          "value": "良性肿瘤",
          "id": 101
        }, {
          "label": "恶性肿瘤",
          "value": "恶性肿瘤",
          "id": 102
        }]
      }, {
        "label": "心脑血管",
        "value": "心脑血管",
        "id": 103,
        "children": [{
          "label": "冠心病",
          "value": "冠心病",
          "id": 104
        }, {
          "label": "心肌梗死",
          "value": "心肌梗死",
          "id": 105
        }, {
          "label": "脑出血",
          "value": "脑出血",
          "id": 106
        }]
      },
        {
          "label": "感染病",
          "value": "感染病",
          "id": 107,
          "children": [{
            "label": "病毒性肝炎",
            "value": "病毒性肝炎",
            "id": 108
          },
            {
              "label": "肝炎肝硬化",
              "value": "肝炎肝硬化",
              "id": 109
            },

          ]
        },
        {
          "label": "血液恶性病",
          "value": "血液恶性病",
          "id": 110,
          "children": [{
            "label": "白血病",
            "value": "白血病",
            "id": 111
          }, {
            "label": "淋巴瘤",
            "value": "淋巴瘤",
            "id": 112
          }, {
            "label": "骨髓瘤",
            "value": "骨髓瘤",
            "id": 113
          },]
        }],
      insuranceOptions: [{
        "label": "财产保险",
        "value": 1
      }, {
        "label": "人身保险",
        "value": 2
      }, {
        "label": "社会保险",
        "value": 3
      }],
      medicalRecordProps: {
        "multiple": false
      },
    }
  },
  computed: {},
  watch: {},
  created() {
  },
  mounted() {
  },
  methods: {
    submitForm() {
      this.$refs['elForm'].validate(valid => {
        if (!valid) return
        // TODO 提交表单
        info(JSON.stringify(this.formData));
        this.$message({
          type: 'success',
          message: '提交成功！'
        });
        this.timer = setTimeout(() => {   //设置延迟执行
          this.$router.go(0);
        }, 1000);
      })
    },
    resetForm() {
      this.$refs['elForm'].resetFields();
      this.$message({
        type: 'success',
        message: '重置成功！'
      });
      this.timer = setTimeout(() => {   //设置延迟执行
        this.$router.go(0);
      }, 1000);

    },
    medicalPhotoBeforeUpload(file) {
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      let isAccept = new RegExp('image/*').test(file.type)
      if (!isAccept) {
        this.$message.error('应该选择image/*类型的文件')
      }
      return isRightSize && isAccept
    },
    housePhotoBeforeUpload(file) {
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      let isAccept = new RegExp('image/*').test(file.type)
      if (!isAccept) {
        this.$message.error('应该选择image/*类型的文件')
      }
      return isRightSize && isAccept
    },
    carPhotoBeforeUpload(file) {
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      let isAccept = new RegExp('image/*').test(file.type)
      if (!isAccept) {
        this.$message.error('应该选择image/*类型的文件')
      }
      return isRightSize && isAccept
    },
  }
}

</script>
<style>
.el-upload__tip {
  line-height: 1.2;
}

</style>
