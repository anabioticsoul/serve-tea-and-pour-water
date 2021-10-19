package com.ruoyi.common.core.domain.entity;

public class supportInfo {
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getFundingAmount() {
        return fundingAmount;
    }

    public void setFundingAmount(int fundingAmount) {
        this.fundingAmount = fundingAmount;
    }

    public String getFundingTime() {
        return fundingTime;
    }

    public void setFundingTime(String fundingTime) {
        this.fundingTime = fundingTime;
    }

    private int fundingAmount;
    private String fundingTime;
}
