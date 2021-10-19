<template>
  <div class="app-container">
    <el-card>
      <el-form ref="elForm" :model="formData" :rules="rules" size="medium" label-width="100px">
        <el-form-item label="支出事项" prop="detail">
          <el-input v-model="formData.detail" placeholder="请输入支出事项" clearable :style="{width: '100%'}"></el-input>
        </el-form-item>
        <el-form-item label="花费金额" prop="cost">
          <el-input-number v-model="formData.cost" placeholder="请输入花费金额"></el-input-number>
        </el-form-item>
        <el-form-item label="发票凭证" prop="bill">
          <el-upload ref="bill" :file-list="billfileList" :action="billAction" :before-upload="billBeforeUpload"
                     list-type="picture" accept="image/*">
            <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item size="large">
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-table
      v-loading="loading"
      :data="show.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
    >
      <el-table-column label="序号" type="index" align="center">
        <template slot-scope="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column label="支出事项" align="center" prop="detail" :show-overflow-tooltip="true"/>
      <el-table-column label="金额" align="center" prop="cost" :show-overflow-tooltip="true"/>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="pageNum" :limit.sync="pageSize"/>
  </div>
</template>

<script>
import {list, forceLogout} from "@/api/monitor/online";
import {
  amountShow,
  getSpentRecord,
  infoShow,
  supportInfoShow,
  updateBill,
  withdrawalAmount
} from "@/api/application/applicationInfo";

export default {
  components: {},
  props: [],
  data() {
    return {
      //用户数据
      supportUser: {},
      totalAmount: 0,
      totalWithdrawalAmount: 0,
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      pageNum: 1,
      pageSize: 10,
      // 查询参数

      formData: {
        detail: undefined,
        cost: undefined,
        bill: null,
      },
      rules: {
        detail: [{
          required: true,
          message: '请输入支出事项',
          trigger: 'blur'
        }],
        cost: [{
          required: true,
          message: '请输入花费金额',
          trigger: 'blur'
        }],
      },
      billAction: 'https://jsonplaceholder.typicode.com/posts/',
      billfileList: [],
      show: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      getSpentRecord().then(response => {
        this.list = response.data.detail;
        // console.log(this.list)
        this.total = this.list.length;
        this.loading = false;

        for (var i = 0; i < this.total; i++) {
          let detailArray = this.list[i].split(',')

          this.show[i] = {
            detail: detailArray[1],
            cost: detailArray[0]
          }
        }
      });
    },

    submitForm() {
      this.$refs['elForm'].validate(valid => {
        if (!valid) return;

        //获取用户花费总金额
        amountShow().then(response => {
          this.supportUser = response.data;
          // console.log(this.supportUser)
          this.totalAmount = this.supportUser.totalAmount;
          this.totalWithdrawalAmount = this.supportUser.totalWithdrawalAmount;

          // console.log("总花费金额: ", this.totalAmount);
          // console.log("本次支出: ", this.totalWithdrawalAmount);
          // console.log("总提现金额: ", this.supportUser.totalWithdrawalAmount);
          if (parseInt(this.supportUser.totalAmount) + parseInt(this.formData.cost) > this.supportUser.totalWithdrawalAmount) {

            this.$message({
              type: 'error',
              message: '金额错误！'
            });

          } else {
            updateBill(JSON.stringify(this.formData));
            this.$message({
              type: 'success',
              message: '提交成功！'
            });
            this.timer = setTimeout(() => {   //设置延迟执行
              this.$router.go(0);
            }, 1000);
          }
        });

      })
    },
    resetForm() {
      this.$refs['elForm'].resetFields()
    },
    billBeforeUpload(file) {
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
};
</script>
