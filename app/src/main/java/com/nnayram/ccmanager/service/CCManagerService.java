package com.nnayram.ccmanager.service;

import android.content.Context;

import com.nnayram.ccmanager.core.ResultWrapper;
import com.nnayram.ccmanager.model.CCInstallmentPayment;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    /**
     * Add new Transaction
     * @param transaction
     * @return resultWrapper
     */
    public ResultWrapper<CcTransaction> addTransaction(CcTransaction transaction) {
        ResultWrapper<CcTransaction> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        errorMessages.addAll(validate(transaction));
        if (errorMessages.isEmpty()) {
            try {
                long id = 0;
                switch (CreditTransactionType.valueOf(transaction.getType())) {
                    case PAYMENT:
                        List<Long> creditIds = new ArrayList<>();
                        for (CcTransaction tran : transaction.getTranCreditInstallmentsForEdit()) {
                            creditIds.add(tran.getId());
                        }
                        String[] creditArray = creditIds.isEmpty() ? null : StringUtils.join(creditIds, ",").split(",");
                        id = repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getDescription(),
                                transaction.getAmount(),
                                creditArray);

                        // check for installments that are not active, update database if any
                        for (CcInstallment i : getInstallments(false)) {
                            if (BooleanUtils.isFalse(i.isActive())) {
                                repository.updateInstallment(i.getId(), i.getActive());
                            }
                        }
                        break;

                    case CREDIT_INST:
                        id = repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getInstallment().getDescription(),
                                transaction.getAmount(),
                                transaction.getInstallment().getId());
                        break;

                    default:
                        // for CREDIT, CREDIT_CHARGE
                        id = repository.insertTransaction(transaction.getTranDate(),
                                transaction.getType(),
                                transaction.getDescription(),
                                transaction.getAmount());
                        break;
                }

                transaction.setId(id);
                resultWrapper.setEntity(transaction);
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }
        return resultWrapper;
    }

    /**
     * Delete existing transaction
     * @param ccTransaction
     * @return resultWrapper
     */
    public ResultWrapper<CcTransaction> deleteTransaction(CcTransaction ccTransaction) {
        ResultWrapper<CcTransaction> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        if (ccTransaction == null || ccTransaction.getId() == null)
            errorMessages.add("Object is null");

        if (errorMessages.isEmpty()) {
            try {
                repository.deleteTransaction(ccTransaction.getId(), ccTransaction.getType());

                if (Arrays.asList(CreditTransactionType.CREDIT_INST.name(), CreditTransactionType.PAYMENT.name()).contains(ccTransaction.getType())) {
                    // update Installments active property
                    for (CcInstallment i : getInstallments(false)) {
                        repository.updateInstallment(i.getId(), i.getActive());
                    }
                }
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }

        return resultWrapper;
    }

    /**
     * Add new Installment
     * @param ccInstallment
     * @return resultWrapper
     */
    public ResultWrapper<CcInstallment> addInstallment(CcInstallment ccInstallment) {
        ResultWrapper<CcInstallment> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        errorMessages.addAll(validate(ccInstallment));
        if (errorMessages.isEmpty()) {
            try {
                long id = repository.insertInstallment(ccInstallment.getDate(),
                    ccInstallment.getDescription(),
                    ccInstallment.getPrincipalAmount(),
                    ccInstallment.getMonthsToPay(),
                    ccInstallment.getMonthlyAmortization(),
                    ccInstallment.getStartDate(),
                    ccInstallment.getEndDate(),
                    1);

                ccInstallment.setId(id);
                resultWrapper.setEntity(ccInstallment);
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }
        return resultWrapper;
    }

    /**
     * Delete existing installment
     * @param ccInstallment
     * @return resultWrapper
     */
    public ResultWrapper<CcInstallment> deleteInstallment(CcInstallment ccInstallment) {
        ResultWrapper<CcInstallment> resultWrapper = new ResultWrapper<>();
        List<String> errorMessages = new ArrayList<>();
        resultWrapper.setErrorMessages(errorMessages);

        if (ccInstallment == null || ccInstallment.getId() == null)
            errorMessages.add("Object is null");

        if (errorMessages.isEmpty()) {
            try {
                repository.deleteInstallment(ccInstallment.getId());
            } catch (Exception e) {
                errorMessages.add(e.getMessage());
            }
        }

        return resultWrapper;
    }

    /**
     * Current balance display
     * @return BigDecimal
     */
    public BigDecimal getOutstandingBalance() {
        try {
            return repository.getOutstandingBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     *
     * @return list of all ccTransaction
     */
    public List<CcTransaction> getAllTransaction() {
        try {
            return repository.getTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @return list of all description
     */
    public List<String> getAllDescription() {
        try {
            return repository.getAllDescription();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @return list of all Transaction CREDIT_INST without payment
     */
    public List<CcTransaction> getAllCreditWithoutPayment() {
        try {
            return repository.getTranCreditsWoPayment();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @return list of all active installment
     */
    public List<CcInstallment> getInstallments(boolean isActive) {
        try {
            List<CcInstallment> installments = new ArrayList<>();
            if (isActive) {
                installments.addAll(repository.getActiveInstallments());
            } else {
                installments.addAll(repository.getInstallments());
            }

            for (CcInstallment i : installments) {
                // compute for total payment made
                BigDecimal totalPaymentMade = BigDecimal.ZERO;
                for (CCInstallmentPayment payment : i.getPaidInstallmentPayments()) {
                    totalPaymentMade = totalPaymentMade.add(payment.getAmount());
                }
                i.setTotalPaymentMade(totalPaymentMade);

                // compute for remaining payment
                i.setRemainingPayment(i.getPrincipalAmount().subtract(totalPaymentMade));

                // compute for remaining months
                Months d = Months.monthsBetween(LocalDate.fromDateFields(i.getStartDate()), LocalDate.now());
                i.setRemainingMonths(i.getMonthsToPay() == null ? 0 : (i.getMonthsToPay() - d.getMonths()));

                // set status to 0 if remaining payment is zero
                i.setActive(i.getRemainingPayment().compareTo(BigDecimal.ZERO));
            }

            return installments;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
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
