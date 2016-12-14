package com.nnayram.ccmanager.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Rufo on 8/6/2016.
 */
public enum CreditTransactionType {

    CREDIT("CREDIT","[+]"), CREDIT_INST("INSTALLMENT","[+]"), CREDIT_CHARGE("CHARGE","[+]"), PAYMENT("PAYMENT", "[-]");

    private String displayName;
    private String operation;
    CreditTransactionType(String displayName, String operation){
        this.displayName = displayName;
        this.operation = operation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOperation() {
        return operation;
    }

    public static CreditTransactionType getTypeByDisplayName(String displayName) {
        String[] name = displayName.split(" ");
        for (CreditTransactionType type : CreditTransactionType.values()) {
            if (type.getDisplayName().equals(name[0])) {
                return  type;
            }
        }
        return null;
    }

    public static String[] getCredit() {
        return new String[]{CREDIT.name(), CREDIT_INST.name(), CREDIT_CHARGE.name()};
    }

    public static String[] getPayment() {
        return new String[]{PAYMENT.name()};
    }

    public static String[] getAllTranDisplayName() {
        return new String[]{CREDIT.getDisplayName()+ " " +CREDIT.getOperation(),
                CREDIT_CHARGE.getDisplayName()+ " " +CREDIT_CHARGE.getOperation(),
                CREDIT_INST.getDisplayName()+ " " +CREDIT_INST.getOperation(),
                PAYMENT.getDisplayName()+ " " +PAYMENT.getOperation()};
    }

}
