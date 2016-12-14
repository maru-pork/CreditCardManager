package com.nnayram.ccmanager.model;

/**
 * Created by Rufo on 12/3/2016.
 */
public class CCInstallmentPayment {

    private Long installmentId;
    private Long tranCreditId;
    private Long tranPaymentId;

    public Long getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(Long installmentId) {
        this.installmentId = installmentId;
    }

    public Long getTranCreditId() {
        return tranCreditId;
    }

    public void setTranCreditId(Long tranCreditId) {
        this.tranCreditId = tranCreditId;
    }

    public Long getTranPaymentId() {
        return tranPaymentId;
    }

    public void setTranPaymentId(Long tranPaymentId) {
        this.tranPaymentId = tranPaymentId;
    }
}
