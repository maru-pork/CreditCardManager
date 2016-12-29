package com.nnayram.ccmanager.service;

import com.nnayram.ccmanager.model.CCInstallmentPayment;
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
     * Create Transaction CREDIT and CREDIT_CHARGE
     * @param date
     * @param type
     * @param description
     * @param amount
     * @return the id of newly inserted Transaction
     * @throws IllegalArgumentException Invalid Type
     */
    long insertTransaction(Date date, String type, String description, BigDecimal amount) throws Exception;

    /**
     * Create Transaction CREDIT_INST
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
     * Create Transaction PAYMENT
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
     * Delete Transaction and automatically set to 0 associated rows from InstallmentPayment tbl
     * @param id
     * @param type
     * @return the number of rows deleted
     */
    long deleteTransaction(long id, String type) throws Exception;

    /**
     * Create Installment
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

    /**
     *
     * @param id
     * @param active
     * @return
     */
    long updateInstallment(Long id, Integer active) throws Exception;

    /**
     *
     * @param id
     * @return
     */
    long deleteInstallment(Long id) throws Exception;

    /**
     * Retrieve Transaction by id
     * @param id
     * @return CcTransaction
     */
    CcTransaction getTransaction(long id) throws Exception;

    /**
     * Retrieve Installment by id
     * @param id
     * @return CcInstallment
     */
    CcInstallment getInstallment(long id) throws Exception;

    /**
     * if ids are empty retrieve all transaction from Transaction tbl, else retrieve all transaction by ids
     * @return list of ccTransaction
     */
    List<CcTransaction> getTransactions(String... ids) throws Exception;

    /**
     * Retrieve all installment from Installment tbl
     * @return list of CCInstallment
     */
    List<CcInstallment> getInstallments() throws Exception;

    /**
     * Retrieve all active installment from Installment tbl
     * @return list of CCInstallment
     */
    List<CcInstallment> getActiveInstallments() throws Exception;

    /**
     * Retrieve all Transaction CREDIT_INST without associated payment
     * @return ids from PaymentInstallment.COLUMN_TRAN_CREDIT_INST
     */
    List<CcTransaction> getTranCreditsWoPayment() throws Exception;

    /**
     * Retrieve all paid payment of given installment id
     * @param id of installment
     * @return list of all InstallmentPayment
     */
    List<CCInstallmentPayment> getInstallmentPayments(Long id) throws Exception;

    /**
     * used for testing
     * @param id
     * @param type
     * @return ids from PaymentInstallment.COLUMN_INSTALLMENT
     */
    List<Long> getPaymentInstallment(Long id, String type);

    /**
     * Summation by types
     * @param types
     * @return sum
     */
    BigDecimal sum(String[] types) throws Exception;

    /**
     * Total credit - Total payment
     * @return outstanding balance
     */
    BigDecimal getOutstandingBalance() throws Exception;

    /**
     * Retrieve at most ten descriptions from Transaction tbl
     * @return list of String
     */
    List<String> getAllDescription() throws Exception;
}
