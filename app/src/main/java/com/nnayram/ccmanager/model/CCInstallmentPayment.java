package com.nnayram.ccmanager.model;

import com.nnayram.ccmanager.core.NumberUtil;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Rufo on 12/3/2016.
 */
public class CCInstallmentPayment {

    private Long id;
    private BigDecimal amount;
    private Date date;
    private CcInstallment installment;
    private CcTransaction tranCredit;
    private CcTransaction tranPayment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(amount));
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public CcInstallment getInstallment() {
        return installment;
    }

    public void setInstallment(CcInstallment installment) {
        this.installment = installment;
    }

    public CcTransaction getTranCredit() {
        return tranCredit;
    }

    public void setTranCredit(CcTransaction tranCredit) {
        this.tranCredit = tranCredit;
    }

    public CcTransaction getTranPayment() {
        return tranPayment;
    }

    public void setTranPayment(CcTransaction tranPayment) {
        this.tranPayment = tranPayment;
    }
}
