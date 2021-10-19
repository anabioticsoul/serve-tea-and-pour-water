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
      <el-progress :text-inside="true" :stroke-width="26" :percentage="percentage1"></el-progress>
      <div style="line-height: 40px">
        <el-row>
          <el-col :span="8">
            <div class="grid-content bg-purple">总花费金额: {{ supportUser.totalAmount }}</div>
          </el-col>
          <el-col :span="8">
            <div class="grid-content bg-purple-light">总提现金额: {{ supportUser.totalWithdrawalAmount }}</div>
          </el-col>
        </el-row>
      </div>
      <el-progress :text-inside="true" :stroke-width="26" :percentage="percentage2"></el-progress>
      <div v-if="percentage2 < 100" class="pull-right" style="margin-top: 10px;margin-bottom: 10px">
        <el-button type="primary" @click="open" disabled>申请提现</el-button>
      </div>
      <div v-else class="pull-right" style="margin-top: 10px;margin-bottom: 10px">
        <el-button type="primary" @click="open">申请提现</el-button>
      </div>

    </el-card>
    <el-table
      v-loading="loading"
      :data="person.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
    >
      <el-table-column label="序号" type="index" align="center">
        <template slot-scope="scope">
          <span>{{(pageNum - 1) * pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column label="资助ID" align="center" prop="userId" :show-overflow-tooltip="true">
      </el-table-column>
      <el-table-column label="资助金额" align="center" prop="fundingAmount" :show-overflow-tooltip="true">
      </el-table-column>
      <el-table-column label="资助时间" align="center" prop="fundingTime" width="180">
      </el-table-column>
      <!--      <template v-for="(p,index) in person">-->
      <!--        -->
      <!--      <el-table-column label="资助ID" align="center" :show-overflow-tooltip="true">{{ p[0] }}</el-table-column>-->
      <!--      <el-table-column label="资助金额" align="center" :show-overflow-tooltip="true">{{ p[1] }}</el-table-column>-->
      <!--      <el-table-column label="资助时间" align="center" width="180"> {{ p[2] }}-->
      <!--&lt;!&ndash;        <template slot-scope="scope">-->
      <!--          <span>{{ parseTime(scope.row.fundingTime) }}</span>-->
      <!--        </template>&ndash;&gt;-->
      <!--      </el-table-column>-->
      <!--      </template>-->
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="pageNum" :limit.sync="pageSize"/>
  </div>
</template>

<script>
import {list, forceLogout} from "@/api/monitor/online";
import {amountShow, infoShow, supportInfoShow, withdrawalAmount} from "@/api/application/applicationInfo";

export default {

  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      pageNum: 1,
      pageSize: 10,
      // 查询参数
      user: {},
      supportUser: {},
      percentage1: undefined,
      percentage2: undefined,
      withdrawalAmount: 0,
      obtainFunds: 0,
      totalWithdrawalAmount: 0,
      person: [],
      personSplit: []
    };
  },
  created() {
    this.getList();
    this.getUser();
    this.getSupportUser();
  },
  methods: {
    /** 查询捐助明细列表 */
    getList() {
      this.loading = true;
      supportInfoShow().then(response => {
        this.obtainFunds = response.data.fundSum;
        this.list = response.data.fundingPerson;
        this.total = this.list.length;
        this.loading = false;
        for (var i = 0; i < this.total; i++) {
          this.personSplit = this.list[i].split(',')
          this.person[i] = {
            userId: this.personSplit[0],
            fundingAmount: this.personSplit[1],
            fundingTime: this.personSplit[2]
          }
        }
      });
    },
    getUser() {
      infoShow().then(response => {
        this.user = response.data;
        this.percentage1 = this.obtainFunds / this.user.target * 100;
      });
    },
    getSupportUser() {
      // console.log("123")
      amountShow().then(response => {
        this.supportUser = response.data;
        // console.log("this.supportUser")
        // console.log(this.supportUser)
        this.totalWithdrawalAmount = this.supportUser.totalWithdrawalAmount;
        this.percentage2 = this.supportUser.totalAmount / this.supportUser.totalWithdrawalAmount * 100;
      });
    },
    open() {
      this.$prompt('请输入提现金额', '提现申请', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(({value}) => {

        this.withdrawalAmount = value

        // console.log("本次提现: ", this.withdrawalAmount);
        // console.log("总提现金额: ", this.totalWithdrawalAmount);
        // console.log("筹到金额: ", this.obtainFunds);
        // console.log(parseInt(this.withdrawalAmount) + parseInt(this.totalWithdrawalAmount));
        if (parseInt(this.withdrawalAmount) + parseInt(this.totalWithdrawalAmount) > this.obtainFunds) {
          this.$message({
            type: 'error',
            message: '您的余额不足'
          });
        } else {
          withdrawalAmount(this.withdrawalAmount).then(({}) => {
            this.$message({
              type: 'success',
              message: '提现成功'
            });
          });
          this.$message({
            type: 'success',
            message: '本次提现金额为: ' + value
          });
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消输入'
        });
      });

    }

  }
};
</script>


