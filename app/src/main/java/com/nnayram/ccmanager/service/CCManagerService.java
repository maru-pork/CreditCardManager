package com.nnayram.ccmanager.service;

import android.content.Context;

import com.nnayram.ccmanager.core.ResultWrapper;
import com.nnayram.ccmanager.db.DBContract;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rufo on 11/10/2016.
 */
public class CCManagerService {

    private CcManagerRepository repository;
    private Context context;

    public CCManagerService(CcManagerRepository repository, Context context) {
        this.repository = repository;
        this.context = context;
    }

    public ResultWrapper<CcTransaction> addTransaction(CcTransaction transaction) {
        ResultWrapper<CcTransaction> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        errorMessages.addAll(validate(transaction));
        if (errorMessages.isEmpty()) {
            try {
                switch (CreditTransactionType.valueOf(transaction.getType())) {
                    case PAYMENT:
                        List<Long> creditIds = new ArrayList<>();
                        for (CcTransaction tran : transaction.getTranCreditInstallmentsForEdit()) {
                            creditIds.add(tran.getId());
                        }
                        String[] creditArray = creditIds.isEmpty() ? null : StringUtils.join(creditIds, ",").split(",");
                        repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getDescription(),
                                transaction.getAmount(),
                                creditArray);
                        break;

                    case CREDIT_INST:
                        repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getInstallment().getDescription(),
                                transaction.getAmount(),
                                transaction.getInstallment().getId());
                        break;

                    default:
                        // for CREDIT, CREDIT_CHARGE
                        repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getDescription(),
                                transaction.getAmount());
                        break;
                }
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }
        return resultWrapper;
    }

    public ResultWrapper<CcTransaction> deleteTransaction(CcTransaction ccTransaction) {
        ResultWrapper<CcTransaction> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        if (ccTransaction == null || ccTransaction.getId() == null)
            errorMessages.add("Object is null");

        if (errorMessages.isEmpty()) {
            try {
                repository.deleteTransaction(ccTransaction.getId(), ccTransaction.getType());
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }

        return resultWrapper;
    }

    public ResultWrapper<CcInstallment> addInstallment(CcInstallment ccInstallment) {
        ResultWrapper<CcInstallment> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        errorMessages.addAll(validate(ccInstallment));
        if (errorMessages.isEmpty()) {
            try {
                repository.insertInstallment(ccInstallment.getDate(),
                    ccInstallment.getDescription(),
                    ccInstallment.getPrincipalAmount(),
                    ccInstallment.getMonthsToPay(),
                    ccInstallment.getMonthlyAmortization(),
                    ccInstallment.getStartDate(),
                    ccInstallment.getEndDate(),
                    1);
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }

        return resultWrapper;
    }

    public BigDecimal getOutstandingBalance() {
        return repository.getOutstandingBalance();
    }

    public List<CcTransaction> getAllTransaction() {
        return repository.getAllTransaction();
    }

    public List<String> getAllDescription() {
        return repository.getAllDescription();
    }

    public List<CcTransaction> getAllCreditWithoutPayment() {
        List<CcTransaction> transactions = new ArrayList<>();
        for (Long id : repository.getAllCreditWithoutPayment()) {
            transactions.add(repository.getTransaction(id));
        }

        return transactions;
    }

    public List<CcInstallment> getAllActiveInstallment() {
        List<CcInstallment> installments = repository.getAllActiveInstallment();

        // manually setTranCreditsWithPayment() per installment
        for (CcInstallment installment : installments) {
            List<CcTransaction> tranCreditsWithPayment = new ArrayList<>();
            for(Long creditId : repository.getAllPayment(installment.getId())) {
                tranCreditsWithPayment.add(repository.getTransaction(creditId));
            }
            installment.setTranCreditsWithPayment(tranCreditsWithPayment);
        }
        return installments;
    }

    private List<String> validate(CcTransaction ccTransaction) {
        List<String> errorMessages = new ArrayList<>();
        if (ccTransaction == null) {
            errorMessages.add("Object is null");
            return errorMessages;
        }

        if (StringUtils.isEmpty(ccTransaction.getType())) {
            errorMessages.add("Transaction Type is required");
            return errorMessages;
        }

        boolean isCreditInst = CreditTransactionType.CREDIT_INST.name().equals(ccTransaction.getType());
        if (isCreditInst && (ccTransaction.getInstallment() == null
                || ccTransaction.getInstallment().getId() == null))
            errorMessages.add("Installment is required");
        if (ccTransaction.getTranDate() == null)
            errorMessages.add("Date is required");
        if ((!isCreditInst && StringUtils.isEmpty(ccTransaction.getDescription()))
                || (isCreditInst && StringUtils.isEmpty(ccTransaction.getInstallmentForEdit().getDescription())))
            errorMessages.add("Description is required");
        if (ccTransaction.getAmount() == null || ccTransaction.getAmount().compareTo(BigDecimal.ZERO) == 0)
            errorMessages.add("Amount is required");

        return errorMessages;
    }

    private List<String> validate(CcInstallment ccInstallment) {
        List<String> errorMessages = new ArrayList<>();
        if (ccInstallment == null) {
            errorMessages.add("Object is null");
            return errorMessages;
        }

        if (ccInstallment.getDate() == null)
            errorMessages.add("Date is required");
        if (StringUtils.isEmpty(ccInstallment.getDescription()))
            errorMessages.add("Description is required");
        if (ccInstallment.getPrincipalAmount() == null
                || ccInstallment.getPrincipalAmount().compareTo(BigDecimal.ZERO) == 0)
            errorMessages.add("Principal Amount is required");
        if (ccInstallment.getMonthsToPay() == null
                || ccInstallment.getMonthsToPay() == 0)
            errorMessages.add("Months To Pay is required");
        if (ccInstallment.getMonthlyAmortization() == null
                || ccInstallment.getMonthlyAmortization().compareTo(BigDecimal.ZERO) == 0)
            errorMessages.add("Monthly Amortization is required");
        if (ccInstallment.getStartDate() == null)
            errorMessages.add("Start date is required");
        if (ccInstallment.getEndDate() == null)
            errorMessages.add("End date is required");

        return errorMessages;
    }
}
