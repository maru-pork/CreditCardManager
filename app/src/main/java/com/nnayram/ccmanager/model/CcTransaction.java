package com.nnayram.ccmanager.model;

import com.nnayram.ccmanager.core.NumberUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rufo on 11/9/2016.
 */
public class CcTransaction {
    private Long id;
    private Date tranDate;
    private String type;
    private String description;
    private BigDecimal amount;
    private CcInstallment installment;
    private List<CcTransaction> tranCreditInstallments;

    public CcTransaction() {
    }

    // constructor for CreditTransactionType : CREDIT, CREDIT_CHARGE, PAYMENT without installment
    public CcTransaction(Date tranDate, String type, String description, BigDecimal amount) {
        this.tranDate = tranDate;
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    // constructor for CreditTransactionType : CREDIT_INST
    public CcTransaction(Date tranDate, String type, String description, BigDecimal amount, CcInstallment installment) {
        this.tranDate = tranDate;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.installment = installment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTranDate() {
        return tranDate;
    }

    public void setTranDate(Date tranDate) {
        this.tranDate = tranDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public CcInstallment getInstallment() {
        return installment;
    }

    public CcInstallment getInstallmentForEdit() {
        if (installment == null)
            return new CcInstallment();
        return installment;
    }

    public void setInstallment(CcInstallment installment) {
        this.installment = installment;
    }

    public void setInstallment(Long id) {
        if (id == null || id ==0) {
            this.installment = null;
        } else {
            this.installment = new CcInstallment(id);
        }
    }

    public List<CcTransaction> getTranCreditInstallments() {
        return tranCreditInstallments;
    }

    public List<CcTransaction> getTranCreditInstallmentsForEdit() {
        if (tranCreditInstallments == null)
            return new ArrayList<>();
        return tranCreditInstallments;
    }

    public void setTranCreditInstallments(List<CcTransaction> tranCreditInstallments) {
        this.tranCreditInstallments = tranCreditInstallments;
    }

}
