<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="求助ID" prop="userinfo">
        <el-input
          v-model="queryParams.userinfo"
          placeholder="请输入求助ID"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery('queryForm')">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">

      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['info:show:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="showList">

      <el-table-column label="求助ID" align="center" prop="userId"/>
      <el-table-column label="求助标题" align="center" prop="applicationTitle"/>
      <el-table-column label="筹款金额" align="center" prop="target"/>
      <el-table-column label="求助时间" align="center" prop="time"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleUpdate(scope.row)"
          >详情
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleDonation(scope.row)"
          >捐款
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

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

    <el-dialog title="捐助信息" :visible.sync="dialogFormVisible">
      <el-form ref="fundForm" :model="formData" size="medium" label-width="150px"
               label-position="right">
        <el-form-item label="捐助金额" prop="donation">
          <el-input v-model="formData.donation" placeholder="请输入捐助金额" show-word-limit clearable
                    prefix-icon='el-icon-coin' :style="{width: '50%'}"></el-input>
        </el-form-item>
        <el-form-item size="large">
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetQuery('fundForm')">重置</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

  </div>
</template>

<script>
import {exportShow} from "@/api/helpinfo/information/show";
import {getAllApplicationInfo, updateFundsInfo} from "@/api/application/applicationInfo";
import {fundPersonShow} from "@/api/application/myFund";

export default {
  name: "Show",


  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 求助信息表格数据
      showList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userinfo: null,
        applicationtitle: null,
      },
      // 表单参数
      form: {},
      application: [],
      donation: {},

      //详情按钮数据
      fundPersonDetail: undefined,

      fund_name: undefined,
      // fund_phone: undefined,
      fund_phone: [],
      // fund_id_card: undefined,
      fund_id_card: [],
      fund_vcl_asset: undefined,
      fund_location: undefined,
      fund_user_info: undefined,
      fund_medical_record: [],

      //对话框显示
      dialogTableVisible: false,
      dialogFormVisible: false,

      formData: {
        donation: undefined,
        fundingId: undefined,
      }

    };
  },
  created() {
    this.getList();
  },
  methods: {

    /** 查询求助信息列表 */
    // getList() {
    //   this.loading = true;
    //   getAllApplicationInfo(this.queryParams).then(response => {
    //     this.showList = response.data;
    //     this.total = this.showList.length;
    //     console.log(this.showList)
    //     console.log(this.total)
    //     this.loading = false;
    //   });
    // },
    getList() {
      this.loading = true;
      getAllApplicationInfo().then(response => {
        this.showList = response.data.application;
        this.total = this.showList.length;
        this.loading = false;
      });
    },

    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    submitForm() {
      this.$refs['fundForm'].validate(valid => {
        if (!valid) return
        // TODO 提交表单
        updateFundsInfo(JSON.stringify(this.formData)).then(({}) => {
          console.log(this.formData)
          this.$message({
            type: 'success',
            message: '谢谢您献出的一份爱心！'
          });
        });
        this.dialogFormVisible = false
      })
    },
    resetForm(object) {
      this.$refs[object].resetFields()
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      fundPersonShow(this.queryParams.userinfo).then(response => {
        this.form = response.data;
        //this.open = true;
        //this.title = "详细信息";
        this.showFundDialog(this.form);
      });
    },
    /** 重置按钮操作 */
    resetQuery(object) {
      this.resetForm(object);
      //this.handleQuery();
    },


    /** 详情按钮操作 */
    handleUpdate(row) {
      //this.reset();
      const userId = row.userId
      fundPersonShow(userId).then(response => {
        this.form = response.data;
        //this.open = true;
        //this.title = "详细信息";
        this.showFundDialog(this.form);
      });
    },
    showFundDialog(data) {
      this.fund_name = data.name;
      // this.fund_phone = data.phone;

      for (let i = 0; i < 11; i++) {
        this.fund_phone[i] = '*'
        if (i < 3 || i > 6) {
          this.fund_phone[i] = data.phone[i]
        }
      }
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


    handleDonation(row) {
      //this.reset();
      const userId = row.userId
      this.formData.fundingId = userId
      this.dialogFormVisible = true;
    },


    // /** 捐款按钮操作 */
    // OPEN() {
    //   this.$prompt('捐款金额', '提示', {
    //     confirmButtonText: '确定',
    //     cancelButtonText: '取消',
    //   }).then(({ value }) => {
    //     this.$message({
    //       type: 'success',
    //       message: '谢谢您献出的一份爱心！'
    //     });
    //     this.donation.fundingId = this.row.userId
    //     this.donation.fundingSum = value
    //     console.log(this.donation)
    //     updateFundsInfo(this.donation).then(({}) => {
    //       this.$message({
    //         type: 'success',
    //         message: '捐款成功'
    //       });
    //     });
    //   }).catch(() => {
    //     this.$message({
    //       type: 'info',
    //       message: '取消输入'
    //     });
    //   });
    // },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$confirm('是否确认导出所有求助信息数据项?', "提醒", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.exportLoading = true;
        return exportShow(queryParams);
      }).then(response => {
        this.download(response.msg);
        this.exportLoading = false;
      }).catch(() => {
      });
    }
  }
};
</script>
