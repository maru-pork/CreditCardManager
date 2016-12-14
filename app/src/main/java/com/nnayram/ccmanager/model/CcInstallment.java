package com.nnayram.ccmanager.model;

import com.nnayram.ccmanager.core.NumberUtil;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private Integer active;
    private List<CcTransaction> tranCreditsWithPayment;

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

    public List<CcTransaction> getTranCreditsWithPayment() {
        return tranCreditsWithPayment;
    }

    public List<CcTransaction> getTranCreditsWithPaymentForEdit() {
        if (tranCreditsWithPayment == null)
            return new ArrayList<>();
        return tranCreditsWithPayment;
    }

    public void setTranCreditsWithPayment(List<CcTransaction> tranCreditsWithPayment) {
        this.tranCreditsWithPayment = tranCreditsWithPayment;
    }

    public BigDecimal getRemainingPayment() {
        BigDecimal totalPayment = BigDecimal.ZERO;
        for (CcTransaction transaction : getTranCreditsWithPaymentForEdit()) {
            totalPayment = totalPayment.add(transaction.getAmount());
        }
        return NumberUtil.getBigDecimalIfExists(principalAmount).subtract(totalPayment);
    }

    public String getFormattedRemainingPayment() {
        return NumberUtil.format().format(getRemainingPayment());
    }

    public Integer getRemainingMonths() {
        if (getStartDate() == null) {
            return getMonthsToPay();
        }
        Months d = Months.monthsBetween(LocalDate.fromDateFields(getStartDate()), LocalDate.now());
        return getMonthsToPay() == null ? 0 : (getMonthsToPay() - d.getMonths());
    }

    @Override
    public String toString() {
        return description+" ["+ getFormattedRemainingPayment() +"]";
    }
}
