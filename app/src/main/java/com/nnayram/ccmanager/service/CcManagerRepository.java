package com.nnayram.ccmanager.service;

import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Rufo on 11/10/2016.
 */
public interface CcManagerRepository {

    /**
     * for CREDIT and CREDIT_CHARGE
     * @param date
     * @param type
     * @param description
     * @param amount
     * @throws IllegalArgumentException Invalid Type
     * @return the id of newly inserted Transaction
     */
    long insertTransaction(Date date, String type, String description, BigDecimal amount) throws Exception;

    /**
     * for CREDIT_INST
     * @param date
     * @param type
     * @param description
     * @param amount
     * @param installmentId
     * @return the id of newly inserted Transaction
     * @throws IllegalArgumentException Invalid Type
     */
    long insertTransaction(Date date, String type, String description, BigDecimal amount, long installmentId) throws Exception;

    /**
     * for PAYMENT
     * @param date
     * @param type
     * @param description
     * @param amount
     * @param tranCreditIds
     * @throws IllegalArgumentException Invalid Type
     * @return the id of newly inserted Transaction
     */
    long insertTransaction(Date date, String type, String description, BigDecimal amount, String[] tranCreditIds) throws Exception;

    /**
     *
     * @param id
     * @param type
     * @return the number of rows deleted
     * @throws IllegalArgumentException Invalid Type
     */
    long deleteTransaction(long id, String type) throws Exception;

    /**
     *
     * @param date
     * @param description
     * @param principalAmount
     * @param monthsToPay
     * @param monthlyAmo
     * @param startDate
     * @param endDate
     * @param active
     * @return the id of newly inserted Installment
     * @throws Exception
     */
    long insertInstallment(Date date, String description, BigDecimal principalAmount, int monthsToPay, BigDecimal monthlyAmo, Date startDate, Date endDate, int active) throws Exception;

    List<CcTransaction> getAllTransaction();

    List<CcInstallment> getAllInstallment();

    List<CcInstallment> getAllActiveInstallment();

    /**
     *
     * @return ids from PaymentInstallment.COLUMN_TRAN_CREDIT_INST
     */
    List<Long> getAllCreditWithoutPayment();

    /**
     *
     * @param installmentId
     * @return ids from PaymentInstallment.COLUMN_TRAN_CREDIT_INST
     */
    List<Long> getAllPayment(Long installmentId);

    /**
     *
     * @param id
     * @param type
     * @return ids from PaymentInstallment.COLUMN_INSTALLMENT
     */
    List<Long> getPaymentInstallment(Long id, String type);

    CcTransaction getTransaction(long id);

    CcInstallment getInstallment(long id);

    BigDecimal sum(String[] types);

    BigDecimal getOutstandingBalance();

    List<String> getAllDescription();
}
