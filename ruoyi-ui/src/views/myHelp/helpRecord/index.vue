<template>
  <!--  template内只能有一个root节点-->
  <div class="app-container">
    <!--  <el-table v-loading="loading" :data="showList" @selection-change="handleSelectionChange">-->

    <!--    <div label="爱心值">{{fundUser.Score}}</div>-->
    <el-table :data="person.slice((pageNum-1)*pageSize,pageNum*pageSize)">
      <el-table-column id="index" label="ID" align="center" prop="userId"/>
      <el-table-column label="金额" align="center" prop="fundingAmount"/>
      <el-table-column label="时间" align="center" prop="fundingTime"/>
      <!--    <el-table-column label="已经筹到" align="center" prop="obtainfunds"/>-->
      <el-table-column label=" " align="center"/>
      <el-table-column label=" " align="center" class-name="small-padding fixed-width">

        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="fundPersonInfo(scope.row)"
          >详情
          </el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="costDetailInfo(scope.row)"
          >明细
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="详细信息" :visible.sync="dialogTableVisible">

      <el-card
        style="font-size: 20px;font-family: 'Microsoft YaHei', DengXian, SimSun, 'Segoe UI', Tahoma, Helvetica, sans-serif">
        <el-descriptions class="margin-top" :column="2" border>
          <el-descriptions-item :span="1">
            <template slot="label">
              <i class="el-icon-user"></i>
              姓名
            </template>
            {{ fund_name }}
          </el-descriptions-item>
          <el-descriptions-item>
            <template slot="label">
              <i class="el-icon-mobile-phone"></i>
              手机号
            </template>
            {{ fund_phone.toString().replaceAll(',',"") }}
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template slot="label">
              <i class="el-icon-postcard"></i>
              身份证号
            </template>
            {{ fund_id_card.toString().replaceAll(',',"") }}
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template slot="label">
              <i class="el-icon-location-information"></i>
              家庭住址
            </template>
            {{ fund_location }}
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template slot="label">
              <i class="el-icon-document"></i>
              基本信息
            </template>
            {{ fund_user_info }}
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template slot="label">
              <i class="el-icon-postcard"></i>
              资产情况
            </template>
            {{ fund_vcl_asset }}
          </el-descriptions-item>
          <el-descriptions-item :span="2">
            <template slot="label">
              <i class="el-icon-first-aid-kit"></i>
              患病信息
            </template>
            {{ fund_medical_record.toString().split(',')[0].split('"')[1] }}
          </el-descriptions-item>

        </el-descriptions>
      </el-card>

    </el-dialog>

    <el-dialog title="花费明细" :visible.sync="dialogFormVisible">

      <el-card
        style="font-size: 20px;font-family: 'Microsoft YaHei', DengXian, SimSun, 'Segoe UI', Tahoma, Helvetica, sans-serif">
        <div>总花费：{{ detail_cost }}</div>
        <el-scrollbar>

          <el-table :data="details.slice((pageNum-1)*pageSize,pageNum*pageSize)">
            <el-table-column label="花费明细" align="center" prop="detail_s"/>
            <el-table-column label="金额" align="center" prop="cost_detail"/>
            <!--            <el-table-column label="花费明细" align="center" v-for="(detail,index) in details">-->
            <!--              <span>{{index}}:{{detail}}</span>-->
            <!--            </el-table-column>-->
          </el-table>

          <!--        <ul v-for="(detail,index) in details">-->
          <!--          <li>{{index}}:{{detail}}</li>-->
          <!--        </ul>-->
        </el-scrollbar>
      </el-card>

    </el-dialog>

    <pagination v-show="total > 0" :total="total" :page.sync="pageNum" :limit.sync="pageSize"/>

  </div>
</template>

<script>
import {fundPersonCostShow, fundPersonShow, myFundShow} from "@/api/application/myFund";


export default {
  name: "index.vue",
  data() {
    return {
      //全局
      //kindness_value:0,
      // personId: [],
      // index: 0,

      // 表格数据
      list: [],
      pageNum: 1,
      pageSize: 10,

      total: 0,

      // 表格内容
      fundUser: {},
      person: [],
      personSplit: [],

      //详情按钮数据
      fundPersonDetail: undefined,

      fund_name: undefined,
      fund_phone: [],
      fund_id_card: [],
      fund_vcl_asset: undefined,
      fund_location: undefined,
      fund_user_info: undefined,
      fund_medical_record: [],

      //对话框显示
      dialogTableVisible: false,
      dialogFormVisible: false,
      //显示花费明细
      detail_cost: undefined,
      details: [],
    };
  },
  created() {
    this.getFundUser();
  },
  methods: {
    getFundUser() {
      myFundShow().then(response => {
        this.fundUser = response.data;
        //alert(this.fundUser)
        //console.log(this.fundUser)
        //this.kindness_value = this.fundUser.Score;
        //console.log(this.kindness_value)
        //console.log(this.fundUser.Person.length)
        this.total = this.fundUser.Person.length;

        for (var i = 0; i < this.total; i++) {

          this.personSplit = this.fundUser.Person[i].split(',')
          this.person[i] = {
            userId: this.personSplit[0],
            fundingAmount: this.personSplit[1],
            fundingTime: this.personSplit[2]
          }
          // this.personId[this.index] = this.person[i].userId;
          // this.index++;
          // alert(this.index)
          //alert(this.personId[i])
        }
      });
    },

    fundPersonInfo(row) {
      this.fundPersonDetail = row.data;
      // console.log(row)
      let userId = row.userId;
      //console.log(userId)
      // alert(this.fundPersonDetail)
      //alert(userId)
      fundPersonShow(userId).then(response => {
        let data = response.data;
        //console.log(temp)
        // alert(temp)
        this.showFundDialog(data);
      });
    },

    showFundDialog(data) {
      //console.log(data)
      this.fund_name = data.name;

      for (let i = 0; i < 11; i++) {
        this.fund_phone[i] = '*'
        if (i < 3 || i > 6) {
          this.fund_phone[i] = data.phone[i]
        }
      }
      //this.fund_phone = data.phone;
      // console.log(this.fund_phone)

      for (let i = 0; i < 18; i++) {
        this.fund_id_card[i] = '*'
        if (i < 5 || i > 13) {
          this.fund_id_card[i] = data.idCard[i]
        }
      }
      // this.fund_id_card = data.idCard;

      this.fund_vcl_asset = data.VCLAsset;
      this.fund_location = data.location;
      this.fund_user_info = data.userInfo;
      this.fund_medical_record = data.SCDMedicalRecord;

      // console.log(this.personInfo)
      //console.log(this.fund_medical_record)
      this.dialogTableVisible = true;
    },

    costDetailInfo(row) {
      this.fundPersonDetail = row.data;
      // console.log(row)
      let userId = row.userId;
      //console.log(userId)
      // alert(this.fundPersonDetail)
      //alert(userId)
      fundPersonCostShow(userId).then(response => {
        let data = response.data;
        //console.log(temp)
        // alert(temp)
        this.showCostDetailDialog(data);
      });
    },
    showCostDetailDialog(data) {
      //console.log(data)
      this.detail_cost = data.cost;
      //this.details = data.detail;
      console.log(data.detail);
      this.details = [];
      for (var i = 0; i < data.detail.length; i++) {
        let dataSplit = data.detail[i].split(',')
        // details : data.detail[i];
        // this.details = data.detail[i];
        // this.details[i] = null;
        this.details[i] = {
          detail_s: dataSplit[1],

          cost_detail: dataSplit[0]

        }
      }
      // console.log(this.details[0])
      this.dialogFormVisible = true;
    }
  },
}
</script>

<style scoped>

</style>
