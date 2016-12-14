package com.nnayram.ccmanager;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.nnayram.ccmanager.db.DBHelper;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.service.CcManagerRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;

/**
 * Created by Rufo on 11/10/2016.
 */
public class CCManagerRepositoryTest extends AndroidTestCase {

    private CcManagerRepository repository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        repository = new DBHelper(mContext, true);
    }

    public void testCreateDB() {
        DBHelper dbHelper = new DBHelper(mContext, true);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue(db.isOpen());
        db.close();
    }

    /*
     simple transaction
     */
    public void testAddTranCredit() throws Exception {
        CcTransaction tran = new CcTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", new BigDecimal("12.50"));
        long result = repository.insertTransaction(tran.getTranDate(), tran.getType(), tran.getDescription(), tran.getAmount());
        assertTrue(result!=0);
        assertEqualsTranObject(tran, repository.getTransaction(result));
    }

    public void testAddTranCreditCharge() throws Exception {
        CcTransaction tran = new CcTransaction(new Date(), CreditTransactionType.CREDIT_CHARGE.name(), "charge", new BigDecimal("1.00"));
        long result = repository.insertTransaction(tran.getTranDate(), tran.getType(), tran.getDescription(), tran.getAmount());
        assertTrue(result!=0);
        assertEqualsTranObject(tran, repository.getTransaction(result));
    }

    public void testAddTranPaymentWithoutInstallment() throws Exception {
        CcTransaction tran = new CcTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("1.00"));
        long result = repository.insertTransaction(tran.getTranDate(), tran.getType(), tran.getDescription(), tran.getAmount(), null);
        assertTrue(result!=0);
        assertEqualsTranObject(tran, repository.getTransaction(result));
    }

    /*
     transaction with installment
     */
    public void testAddTranCreditInst() throws Exception {
        assertEquals(0, repository.getAllActiveInstallment().size());

        // add INSTALLMENT first
        long id = repository.insertInstallment(new Date(), "Electroworld-SM", new BigDecimal("31990.00"), 12, new BigDecimal("2665.83"), new Date(), new Date(), 1);
        assertEquals(1, repository.getAllActiveInstallment().size());
        CcInstallment installment = repository.getInstallment(id);
        assertNotNull(installment);

        // insert Transaction CREDIT_INST
        CcTransaction tran = new CcTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), installment.getDescription(), new BigDecimal("2665.83"), installment);
        long result = repository.insertTransaction(tran.getTranDate(), tran.getType(), tran.getDescription(), tran.getAmount(), tran.getInstallment().getId());
        assertEqualsTranObject(tran, repository.getTransaction(result));
        assertEquals(1, repository.getAllCreditWithoutPayment().size());
    }

    public void testInsertPaymentWithInstallment() throws Exception {
        // add INSTALLMENT first
        long installmentId = repository.insertInstallment(new Date(), "Electroworld-SM", new BigDecimal("31990.00"), 12, new BigDecimal("2665.83"), new Date(), new Date(), 1);
        CcInstallment installment = repository.getInstallment(installmentId);
        assertNotNull(installment);

        // insert Transaction CREDIT_INST
        long id = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment", new BigDecimal("10.00"), installment.getId());
        assertNotNull(repository.getTransaction(id));
        assertEquals(1, repository.getAllCreditWithoutPayment().size());
        assertEquals(0, repository.getAllPayment(installmentId).size());

        // insert PAYMENT
        CcTransaction trPayment = new CcTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("1.00"));
        long result = repository.insertTransaction(trPayment.getTranDate(), trPayment.getType(), trPayment.getDescription(), trPayment.getAmount(), new String[]{String.valueOf(id)});
        assertEqualsTranObject(trPayment, repository.getTransaction(result));
        assertEquals(0, repository.getAllCreditWithoutPayment().size());
        assertEquals(1, repository.getAllPayment(installmentId).size());
    }

    public void testInsertPaymentWithMultipleInstallment() throws Exception {
        // add INSTALLMENT first
        long installmentId1 = repository.insertInstallment(new Date(), "Electroworld-SM1", new BigDecimal("31991.00"), 12, new BigDecimal("2661.83"), new Date(), new Date(), 1);
        CcInstallment installment1 = repository.getInstallment(installmentId1);
        assertNotNull(installment1);

        long installmentId2 = repository.insertInstallment(new Date(), "Electroworld-SM2", new BigDecimal("31992.00"), 12, new BigDecimal("2662.83"), new Date(), new Date(), 1);
        CcInstallment installment2 = repository.getInstallment(installmentId2);
        assertNotNull(installment2);

        long installmentId3 = repository.insertInstallment(new Date(), "Electroworld-SM3", new BigDecimal("31993.00"), 12, new BigDecimal("2662.83"), new Date(), new Date(), 1);
        CcInstallment installment3 = repository.getInstallment(installmentId3);
        assertNotNull(installment3);

        // insert Transaction CREDIT_INST for every installment
        long id1 = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment1", new BigDecimal("10.00"), installment1.getId());
        long id2 = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment2", new BigDecimal("20.00"), installment2.getId());
        long id3 = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment3", new BigDecimal("30.00"), installment3.getId());
        assertEquals(3, repository.getAllCreditWithoutPayment().size());
        assertEquals(0, repository.getAllPayment(installmentId1).size());
        assertEquals(0, repository.getAllPayment(installmentId2).size());
        assertEquals(0, repository.getAllPayment(installmentId3).size());

        // pay only for installment1 and installment2
        CcTransaction trPayment = new CcTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("100.00"));
        repository.insertTransaction(trPayment.getTranDate(), trPayment.getType(), trPayment.getDescription(), trPayment.getAmount(), new String[]{String.valueOf(id1), String.valueOf(id2)});
        assertEquals(1, repository.getAllCreditWithoutPayment().size());
        assertEquals(1, repository.getAllPayment(installmentId1).size());
        assertEquals(1, repository.getAllPayment(installmentId2).size());
        assertEquals(0, repository.getAllPayment(installmentId3).size());

        // pay for installment3
        trPayment = new CcTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("500.00"));
        repository.insertTransaction(trPayment.getTranDate(), trPayment.getType(), trPayment.getDescription(), trPayment.getAmount(), new String[]{String.valueOf(id3)});
        assertEquals(0, repository.getAllCreditWithoutPayment().size());
        assertEquals(1, repository.getAllPayment(installmentId1).size());
        assertEquals(1, repository.getAllPayment(installmentId2).size());
        assertEquals(1, repository.getAllPayment(installmentId3).size());
    }

    public void testInsertInstallment() throws Exception {
        CcInstallment inst = new CcInstallment(new Date(), "Electroworld-SM", new BigDecimal("31990.00"), 12, new BigDecimal("2665.83"), new Date(), new Date(), 1);
        long result = repository.insertInstallment(inst.getDate(), inst.getDescription(), inst.getPrincipalAmount(),
                inst.getMonthsToPay(), inst.getMonthlyAmortization(), inst.getStartDate(), inst.getEndDate(), inst.getActive());
        assertTrue(result!=0);

        CcInstallment actual = repository.getInstallment(result);
        assertNotNull(actual.getId());
        assertEquals(inst.getDate(), actual.getDate());
        assertEquals(inst.getDescription(), actual.getDescription());
        assertEquals(inst.getPrincipalAmount(), actual.getPrincipalAmount());
        assertEquals(inst.getMonthsToPay(), actual.getMonthsToPay());
        assertEquals(inst.getMonthlyAmortization(), actual.getMonthlyAmortization());
        assertEquals(inst.getStartDate(), actual.getStartDate());
        assertEquals(inst.getEndDate(), actual.getEndDate());
        assertEquals(inst.getActive(), actual.getActive());
        assertEquals(inst.getActive() == 1, actual.isActive());
    }

    public void testDeleteTransaction() throws Exception {
        long installmentId = repository.insertInstallment(new Date(), "Electroworld-SM1", new BigDecimal("31991.00"), 12, new BigDecimal("2661.83"), new Date(), new Date(), 1);

        long idCredit = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", new BigDecimal("12.50"));
        long idCreditCharge = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_CHARGE.name(), "credit charge", new BigDecimal("130"));
        long idPayment = repository.insertTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("130"), null);
        long idCreditInst = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment", new BigDecimal("130"), installmentId);
        long idPaymentInst = repository.insertTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment with installment", new BigDecimal("130"), new String[]{String.valueOf(idCreditInst)});

        assertNotNull(repository.getTransaction(idCredit));
        assertNotNull(repository.getTransaction(idCreditCharge));
        assertNotNull(repository.getTransaction(idPayment));
        assertNotNull(repository.getTransaction(idCreditInst));
        assertNotNull(repository.getTransaction(idPaymentInst));

        repository.deleteTransaction(idCredit, CreditTransactionType.CREDIT.name());
        assertNull(repository.getTransaction(idCredit));
        assertNotNull(repository.getTransaction(idCreditCharge));
        assertNotNull(repository.getTransaction(idPayment));
        assertNotNull(repository.getTransaction(idCreditInst));
        assertNotNull(repository.getTransaction(idPaymentInst));

        repository.deleteTransaction(idCreditCharge, CreditTransactionType.CREDIT_CHARGE.name());
        assertNull(repository.getTransaction(idCreditCharge));
        assertNotNull(repository.getTransaction(idPayment));
        assertNotNull(repository.getTransaction(idCreditInst));
        assertNotNull(repository.getTransaction(idPaymentInst));

        repository.deleteTransaction(idPayment, CreditTransactionType.PAYMENT.name());
        assertNull(repository.getTransaction(idPayment));
        assertNotNull(repository.getTransaction(idCreditInst));
        assertNotNull(repository.getTransaction(idPaymentInst));

        // delete CREDIT_INST with associated payment
        try {
            repository.deleteTransaction(idCreditInst, CreditTransactionType.CREDIT_INST.name());
        } catch (Exception e) {
            assertEquals("Cannot delete given id. Payment column has value.", e.getMessage());
        }

        assertEquals(1, repository.getPaymentInstallment(idPaymentInst,CreditTransactionType.PAYMENT.name()).size());
        repository.deleteTransaction(idPaymentInst, CreditTransactionType.PAYMENT.name());
        assertEquals(0, repository.getPaymentInstallment(idPaymentInst,CreditTransactionType.PAYMENT.name()).size());
        assertNull(repository.getTransaction(idPaymentInst));
        assertNotNull(repository.getTransaction(idCreditInst));

        assertEquals(1, repository.getPaymentInstallment(idCreditInst,CreditTransactionType.CREDIT_INST.name()).size());
        repository.deleteTransaction(idCreditInst, CreditTransactionType.CREDIT_INST.name());
        assertEquals(0, repository.getPaymentInstallment(idCreditInst,CreditTransactionType.CREDIT_INST.name()).size());
        assertNull(repository.getTransaction(idCreditInst));

        // delete CREDIT_INST without associated payment
        idCreditInst = repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment", new BigDecimal("130"), installmentId);
        assertEquals(1, repository.getPaymentInstallment(idCreditInst,CreditTransactionType.CREDIT_INST.name()).size());
        repository.deleteTransaction(idCreditInst, CreditTransactionType.CREDIT_INST.name());
        assertEquals(0, repository.getPaymentInstallment(idCreditInst,CreditTransactionType.CREDIT_INST.name()).size());
        assertNull(repository.getTransaction(idCreditInst));
    }

    /*
    retrieving transactions
     */
    public void testGetAllSimpleTransaction() throws Exception {
        Map<CreditTransactionType, CcTransaction> transactionMap = new HashMap<>();
        transactionMap.put(CreditTransactionType.CREDIT, new CcTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", new BigDecimal("12.50")));
        transactionMap.put(CreditTransactionType.CREDIT_INST, new CcTransaction(new Date(), CreditTransactionType.CREDIT_INST.name(), "credit installment", new BigDecimal("10.00"), new CcInstallment(1L)));
        transactionMap.put(CreditTransactionType.CREDIT_CHARGE, new CcTransaction(new Date(), CreditTransactionType.CREDIT_CHARGE.name(), "charge", new BigDecimal("1.00")));
        transactionMap.put(CreditTransactionType.PAYMENT, new CcTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("1.00"), null));

        // insert transactions
        for (Map.Entry<CreditTransactionType, CcTransaction> entry : transactionMap.entrySet()) {
            if (CreditTransactionType.CREDIT_INST.name().equals(entry.getValue().getType())) {
                repository.insertTransaction(entry.getValue().getTranDate(),entry.getValue().getType(),entry.getValue().getDescription(),entry.getValue().getAmount(),entry.getValue().getInstallment().getId());
            } else if (CreditTransactionType.PAYMENT.name().equals(entry.getValue().getType())) {
                repository.insertTransaction(entry.getValue().getTranDate(),entry.getValue().getType(),entry.getValue().getDescription(),entry.getValue().getAmount(),null);
            } else {
                repository.insertTransaction(entry.getValue().getTranDate(),entry.getValue().getType(),entry.getValue().getDescription(),entry.getValue().getAmount());
            }
        }

        List<CcTransaction> transactionList = repository.getAllTransaction();
        for (CcTransaction tran : transactionList) {
            assertEqualsTranObject(transactionMap.get(CreditTransactionType.valueOf(tran.getType())), tran);
        }
    }

    public void testGetAllDescriptions() throws Exception {
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "ever", new BigDecimal("12.50"));
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "nbs", new BigDecimal("12.50"));
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "eton", new BigDecimal("130"));
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "eton", new BigDecimal("80"));
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_CHARGE.name(), "charge", new BigDecimal("130"));

        List<String> descriptions = repository.getAllDescription();
        assertEquals(4, descriptions.size());
        assertTrue(descriptions.containsAll(Arrays.asList(new String[]{"ever", "eton", "nbs", "charge"})));
    }

    private void assertEqualsTranObject(CcTransaction expected, CcTransaction actual) {
        assertNotNull(actual.getId());
        assertEquals(expected.getTranDate(), actual.getTranDate());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getAmount(), actual.getAmount());
        if (actual.getInstallment() != null)
            assertEquals(expected.getInstallment().getId(), actual.getInstallment().getId());
    }

    /*
    transactions
     */
    public void testOutstandingBalance() throws Exception {
         // insert three credit transaction
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", ONE);
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT_CHARGE.name(), "charge", TEN);
        assertEquals(new BigDecimal("11.00"), repository.sum(CreditTransactionType.getCredit()));
        assertEqualsOutstandingBalance(new BigDecimal("11.00"));

        // insert three payment transaction
        repository.insertTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", ONE, null);
        repository.insertTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", TEN, null);
        assertEquals(new BigDecimal("11.00"), repository.sum(CreditTransactionType.getPayment()));
        assertEqualsOutstandingBalance(new BigDecimal("0.00"));

        // insert credit transaction
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", ONE);
        assertEqualsOutstandingBalance(new BigDecimal("1.00"));

        // insert credit transaction
        repository.insertTransaction(new Date(), CreditTransactionType.CREDIT.name(), "credit", new BigDecimal("12.5"));
        assertEqualsOutstandingBalance(new BigDecimal("13.50"));

        // insert excess payment
        repository.insertTransaction(new Date(), CreditTransactionType.PAYMENT.name(), "payment", new BigDecimal("14.5"), null);
        assertEqualsOutstandingBalance(new BigDecimal("-1.00"));
    }

    private void assertEqualsOutstandingBalance(BigDecimal expectedBalance) {
        BigDecimal totalCredit = repository.sum(CreditTransactionType.getCredit());
        BigDecimal totalPayment = repository.sum(CreditTransactionType.getPayment());

        // check for outstanding balance
        assertEquals(expectedBalance, totalCredit.subtract(totalPayment));
        assertEquals(expectedBalance, repository.getOutstandingBalance());
    }


}
