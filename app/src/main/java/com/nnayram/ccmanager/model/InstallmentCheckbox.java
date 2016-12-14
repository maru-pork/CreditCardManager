package com.nnayram.ccmanager.model;

/**
 * Created by Rufo on 8/20/2016.
 */
public class InstallmentCheckbox {

    private String name;
    private boolean selected;
    private CcTransaction tranCreditInst;

    public InstallmentCheckbox(String name, CcTransaction tranCreditInst, boolean selected) {
        this.name = name;
        this.selected = selected;
        this.tranCreditInst = tranCreditInst;
    }

    public InstallmentCheckbox(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public CcTransaction getTranCreditInst() {
        return tranCreditInst;
    }

    public void setTranCreditInst(CcTransaction tranCreditInst) {
        this.tranCreditInst = tranCreditInst;
    }
}
