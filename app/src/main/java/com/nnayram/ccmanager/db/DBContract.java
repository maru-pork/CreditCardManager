package com.nnayram.ccmanager.db;

import android.provider.BaseColumns;

/**
 * Created by Rufo on 11/10/2016.
 */
public class DBContract {

    private DBContract() {

    }

    public static abstract class Transaction implements BaseColumns {
        public static final String TABLE_NAME = "ccTransaction";
        public static final String COLUMN_DATE = "tran_date";
        public static final String COLUMN_TYPE = "tran_type";
        public static final String COLUMN_DESC = "tran_description";
        public static final String COLUMN_AMOUNT = "tran_amount";
        public static final String COLUMN_INSTALLMENT = "installment_id";
    }

    public static abstract class Installment implements BaseColumns {
        public static final String TABLE_NAME = "ccInstallment";
        public static final String COLUMN_DATE = "inst_date";
        public static final String COLUMN_DESC = "inst_description";
        public static final String COLUMN_PRINCIPAL_AMOUNT = "inst_principal_amount";
        public static final String COLUMN_MONTHS_TO_PAY = "inst_months_to_pay";
        public static final String COLUMN_MONTHLY_AMO = "inst_monthly_amo";
        public static final String COLUMN_START_DATE = "inst_start_date";
        public static final String COLUMN_END_DATE = "inst_end_date";
        public static final String COLUMN_ACTIVE = "inst_active";
    }

    public static abstract class PaymentInstallment {
        public static final String TABLE_NAME = "ccPayment_installment";
        public static final String COLUMN_INSTALLMENT = "ccInstallment_id";
        public static final String COLUMN_TRAN_CREDIT_INST = "ccTransaction_credit_id";
        public static final String COLUMN_TRAN_PAYMENT = "ccTransaction_payment_id";
    }

}
