package com.nnayram.ccmanager.model;

import com.nnayram.ccmanager.core.NumberUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Rufo on 11/10/2016.
 */
public class CcInstallment {

    private Long id;
    private Date date;
    private String description;
    private BigDecimal principalAmount;
    private Integer monthsToPay;
    private BigDecimal monthlyAmortization;
    private Date startDate;
    private Date endDate;
    private List<CCInstallmentPayment> paidInstallmentPayments;

    // values computed on service
    private Integer active;
    private BigDecimal remainingPayment;
    private BigDecimal totalPaymentMade;
    private Integer remainingMonths;

    public CcInstallment() {

    }

    public CcInstallment(Long id) {
        this.id = id;
    }

    public CcInstallment(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public CcInstallment(Date date, String description, BigDecimal principalAmount, Integer monthsToPay, BigDecimal monthlyAmortization, Date endDate, Date startDate, Integer active) {
        this.date = date;
        this.description = description;
        this.principalAmount = principalAmount;
        this.monthsToPay = monthsToPay;
        this.monthlyAmortization = monthlyAmortization;
        this.endDate = endDate;
        this.startDate = startDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public String getFormattedPrincipalAmount() {
        return NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(principalAmount));
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Integer getMonthsToPay() {
        return monthsToPay;
    }

    public void setMonthsToPay(Integer monthsToPay) {
        this.monthsToPay = monthsToPay;
    }

    public BigDecimal getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public void setMonthlyAmortization(BigDecimal monthlyAmortization) {
        this.monthlyAmortization = monthlyAmortization;
    }

    public String getFormattedMonthlyAmortization() {
        return NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(monthlyAmortization));
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getActive() {
        return active;
    }

    public boolean isActive() {
        if (active == null)
            return false;
        return active == 1;
    }

    public BigDecimal getRemainingPayment() {
        return remainingPayment;
    }

    public String getFormattedRemainingPayment() {
        return NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(remainingPayment));
    }

    public void setRemainingPayment(BigDecimal remainingPayment) {
        this.remainingPayment = remainingPayment;
    }

    public BigDecimal getTotalPaymentMade() {
        return totalPaymentMade;
    }

    public String getFormattedTotalPaymentMade() {
        return NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(totalPaymentMade));
    }

    public void setTotalPaymentMade(BigDecimal totalPaymentMade) {
        this.totalPaymentMade = totalPaymentMade;
    }

    public List<CCInstallmentPayment> getPaidInstallmentPayments() {
        return paidInstallmentPayments;
    }

    public void setPaidInstallmentPayments(List<CCInstallmentPayment> paidInstallmentPayments) {
        this.paidInstallmentPayments = paidInstallmentPayments;
    }

    public void setRemainingMonths(Integer remainingMonths) {
        this.remainingMonths = remainingMonths;
    }

    public Integer getRemainingMonths() {
        return remainingMonths;
    }

    @Override
    public String toString() {
        return description+" ["+ getFormattedRemainingPayment() +"]";
    }
}
