package com.nnayram.ccmanager;

import android.test.AndroidTestCase;

import com.nnayram.ccmanager.core.DateUtil;
import com.nnayram.ccmanager.core.NumberUtil;
import com.nnayram.ccmanager.db.DBHelper;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.service.CCManagerService;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Rufo on 11/12/2016.
 */
public class CCManagerServiceTest extends AndroidTestCase {

    private CCManagerService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = new CCManagerService(new DBHelper(mContext, true), mContext);
    }

    public void testTransactionValidation() {
        // TODO
    }

    public void testInstallmentValidation() {
        // TODO
    }

    public void testInstallment() {
        // for single installment
        service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("31990"), 12, new BigDecimal("2665"), new Date(), new Date(), 1));
        List<CcInstallment> activeInstallments = service.getInstallments(true);
        assertEquals(1, activeInstallments.size());

        service.addTransaction(constructTransaction("21-Apr-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("22-Apr-16", "PAYMENT","payment", "1333", service.getAllCreditWithoutPayment().get(0)));
        assertInstallment(new BigDecimal("1333.00"), new BigDecimal("30657.00"), true);

        service.addTransaction(constructTransaction("23-Apr-16", "INSTALLMENT", "laptop", "15000", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("24-Apr-16", "PAYMENT","payment", "15000", service.getAllCreditWithoutPayment().get(0)));
        assertInstallment(new BigDecimal("16333.00"), new BigDecimal("15657.00"), true);

        service.addTransaction(constructTransaction("25-Apr-16", "INSTALLMENT", "laptop", "15657", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("26-Apr-16", "PAYMENT","payment", "15657", service.getAllCreditWithoutPayment().get(0)));
        assertInstallment(new BigDecimal("31990.00"), new BigDecimal("0.00"), false);
    }

    private void assertInstallment(BigDecimal totalPaymentMade, BigDecimal remainingPayment, boolean isActive) {
        List<CcInstallment> installments = service.getInstallments(false);
        assertEquals(totalPaymentMade, installments.get(0).getTotalPaymentMade());
        assertEquals(remainingPayment, installments.get(0).getRemainingPayment());
        assertEquals(isActive, installments.get(0).isActive());
    }

    public void testMultipleInstallment() {
        CcInstallment installment1 = service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("12"), 12, new BigDecimal("1"), new Date(), new Date(), 1)).getEntity();
        CcInstallment installment2 = service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("24"), 12, new BigDecimal("2"), new Date(), new Date(), 1)).getEntity();
        CcInstallment installment3 = service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("36"), 12, new BigDecimal("3"), new Date(), new Date(), 1)).getEntity();
        assertInstallment(3, 3);

        service.addTransaction(constructTransaction("25-Apr-16", "INSTALLMENT", "laptop", "12", installment1));
        service.addTransaction(constructTransaction("26-Apr-16", "PAYMENT","payment", "12", service.getAllCreditWithoutPayment().get(0)));
        assertInstallment(2, 3);

        service.addTransaction(constructTransaction("25-Apr-16", "INSTALLMENT", "laptop", "24", installment2));
        service.addTransaction(constructTransaction("26-Apr-16", "PAYMENT","payment", "24", service.getAllCreditWithoutPayment().get(0)));
        assertInstallment(1, 3);
    }

    private void assertInstallment(int activeSize, int allSize) {
        assertEquals(activeSize, service.getInstallments(true).size());
        assertEquals(allSize, service.getInstallments(false).size());
    }

    public void testTran() {
        service.addTransaction(constructTransaction("6-Nov-15", "CREDIT", "Denture", "11660"));
        service.addTransaction(constructTransaction("19-Nov-15", "CREDIT", "Ever", "1850.3"));
        assertTransaction(2, new BigDecimal("13510.30"));
        service.addTransaction(constructTransaction("21-Nov-15", "PAYMENT", "Payment", "4000"));
        assertTransaction(3, new BigDecimal("9510.30"));
        service.addTransaction(constructTransaction("20-Nov-15", "CREDIT", "Eton", "140.5"));
        service.addTransaction(constructTransaction("25-Nov-15", "CREDIT", "NBS-North", "277.25"));
        service.addTransaction(constructTransaction("1-Dec-15", "CREDIT", "Ever", "189.75"));
        service.addTransaction(constructTransaction("2-Dec-15", "CREDIT", "Landmark", "349.75"));
        service.addTransaction(constructTransaction("2-Dec-15", "CREDIT", "Human", "249"));
        service.addTransaction(constructTransaction("8-Dec-15", "CREDIT", "Watsons", "280"));
        service.addTransaction(constructTransaction("8-Dec-15", "CREDIT", "NBS-Fairview", "443.75"));
        service.addTransaction(constructTransaction("11-Dec-15", "CHARGE", "Charge", "374.71"));
        assertTransaction(11, new BigDecimal("11815.01"));
        service.addTransaction(constructTransaction("15-Dec-15", "PAYMENT", "Payment", "11815.01"));
        assertTransaction(12, new BigDecimal("0.00"));
        service.addTransaction(constructTransaction("21-Dec-15", "CREDIT",  "Ever" ,"2882.65"));
        service.addTransaction(constructTransaction("18-Dec-15", "CREDIT", "Ever", "299.5"));
        service.addTransaction(constructTransaction("22-Dec-15", "CREDIT", "SM North", "300"));
        service.addTransaction(constructTransaction("22-Dec-15", "CREDIT", "NBS-North", "347"));
        service.addTransaction(constructTransaction("22-Dec-15", "CREDIT", "Landmark","833.25"));
        service.addTransaction(constructTransaction("22-Dec-15", "CREDIT", "Mercury-Trinoma", "293.5"));
        service.addTransaction(constructTransaction("22-Dec-15", "CREDIT", "Watsons","383"));
        assertTransaction(19, new BigDecimal("5338.90"));
        service.addTransaction(constructTransaction("13-Jan-16", "PAYMENT", "Payment", "5338.9"));
        assertTransaction(20, new BigDecimal("0.00"));
        service.addTransaction(constructTransaction("16-Jan-16", "CREDIT", "Ever", "1926.95"));
        service.addTransaction(constructTransaction("16-Jan-16", "CREDIT", "Mercury-Ever", "356.25"));
        service.addTransaction(constructTransaction("16-Jan-16", "CREDIT", "Bench", "140"));
        service.addTransaction(constructTransaction("25-Jan-16", "CREDIT", "Watsons", "299"));
        service.addTransaction(constructTransaction("26-Jan-15", "CREDIT", "Ever", "619.5"));
        service.addTransaction(constructTransaction("26-Jan-15", "CREDIT", "Mercury-Ever", "115.5"));
        assertTransaction(26, new BigDecimal("3457.20"));
        service.addTransaction(constructTransaction("27-Jan-16", "PAYMENT", "Payment", "300"));
        assertTransaction(27, new BigDecimal("3157.20"));
        service.addTransaction(constructTransaction("27-Jan-16", "CREDIT", "Mercury-Ever","66.5"));
        assertTransaction(28, new BigDecimal("3223.70"));
        service.addTransaction(constructTransaction("1-Feb-16", "PAYMENT", "Payment", "1200"));
        assertTransaction(29, new BigDecimal("2023.70"));
        service.addTransaction(constructTransaction("2-Feb-16", "CREDIT", "Mercury-Ayala", "261.5"));
        service.addTransaction(constructTransaction("9-Feb-16", "CREDIT", "Eton", "244.5"));
        assertTransaction(31, new BigDecimal("2529.70"));
    }

    public void testTranWithInstallment() {
        service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("31990"), 12, new BigDecimal("2665.833333333333"), new LocalDate("2017-02-14").toDate(), new LocalDate("2016-02-14").toDate(), 1)).getEntity();
        List<CcInstallment> activeInstallments = service.getInstallments(true);
        assertFalse(activeInstallments.isEmpty());

        service.addTransaction(constructTransaction("9-Feb-16", "CREDIT", "Previous-Balance", "2529.7"));
        //MARCH
        service.addTransaction(constructTransaction("15-Feb-16", "PAYMENT", "Payment", "3529.7"));
        assertTransaction(2, new BigDecimal("-1000.00"), 0);
        service.addTransaction(constructTransaction("16-Feb-16", "CREDIT", "Eton", "1059.3"));
        service.addTransaction(constructTransaction("17-Feb-16", "CREDIT", "Watsons", "240"));
        service.addTransaction(constructTransaction("27-Feb-16", "CREDIT", "Mercury - Ever", "237.5"));
        service.addTransaction(constructTransaction("27-Feb-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        assertTransaction(6, new BigDecimal("1869.80"), 1);
        service.addTransaction(constructTransaction("29-Feb-16", "PAYMENT", "Payment", "2333", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(7, new BigDecimal("-463.20"), 0);
        service.addTransaction(constructTransaction("29-Feb-16", "CREDIT", "Eton", "722.5"));
        service.addTransaction(constructTransaction("2-Mar-16", "CREDIT", "NBS Cubao", "109"));
        service.addTransaction(constructTransaction("2-Mar-16", "CREDIT", "NBS Cubao", "224.25"));
        //APRIL
        service.addTransaction(constructTransaction("16-Mar-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        assertTransaction(11, new BigDecimal("1925.55"), 1);
        service.addTransaction(constructTransaction("16-Mar-16", "PAYMENT", "Payment", "592.38", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(12, new BigDecimal("1333.17"), 0);
        service.addTransaction(constructTransaction("21-Mar-16", "CREDIT", "Eton", "1420.3"));
        service.addTransaction(constructTransaction("22-Mar-16", "CREDIT", "Eye Lounge", "4095"));
        service.addTransaction(constructTransaction("23-Mar-16", "CREDIT", "Mercury - G4", "237.5"));
        service.addTransaction(constructTransaction("31-Mar-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        assertTransaction(16, new BigDecimal("8418.97"), 1);
        service.addTransaction(constructTransaction("31-Mar-16", "PAYMENT", "Payment", "3967", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(17, new BigDecimal("4451.97"), 0);
        service.addTransaction(constructTransaction("31-Mar-16", "CREDIT", "Cebu Pacific", "1474.56"));
        service.addTransaction(constructTransaction("31-Mar-16", "CREDIT", "NBS - G4", "192.25"));
        service.addTransaction(constructTransaction("3-Apr-16", "CREDIT", "Landmark", "619.5"));
        service.addTransaction(constructTransaction("4-Apr-16", "CREDIT", "Eton", "812.05"));
        //MAY
        service.addTransaction(constructTransaction("14-Apr-16", "CREDIT", "SMDeptStore - Sandals", "999"));
        service.addTransaction(constructTransaction("17-Apr-16", "CREDIT", "Mercury-Trinoma", "58"));
        service.addTransaction(constructTransaction("17-Apr-16", "CREDIT", "SMDEPTStore - Bag", "2199.75"));
        service.addTransaction(constructTransaction("17-Apr-16", "CREDIT", "Ever", "1930"));
        service.addTransaction(constructTransaction("18-Apr-16", "INSTALLMENT", "laptop", "1332.49", activeInstallments.get(0)));
        assertTransaction(26, new BigDecimal("14069.57"), 1);
        service.addTransaction(constructTransaction("18-Apr-16", "PAYMENT","payment", "6216.99", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(27, new BigDecimal("7852.58"), 0);
        service.addTransaction(constructTransaction("19-Apr-16", "CREDIT","Eton", "468.25"));
        service.addTransaction(constructTransaction("21-Apr-16", "CREDIT","Eton", "116.5"));
        service.addTransaction(constructTransaction("05-May-16", "PAYMENT","payment", "2632.00"));
        service.addTransaction(constructTransaction("05-May-16", "CREDIT","Eton", "1009.20"));
        service.addTransaction(constructTransaction("06-May-16", "CREDIT","Eton", "341.45"));
        service.addTransaction(constructTransaction("09-May-16", "CREDIT","Cebu-Pac", "1298.72"));
        assertTransaction(33, new BigDecimal("8454.70"));
        // JUNE
        service.addTransaction(constructTransaction("13-May-16", "INSTALLMENT", "laptop", "1332.91", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("13-May-16", "PAYMENT","payment", "7029.84", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("14-May-16", "CREDIT","Eton", "1122.10"));
        service.addTransaction(constructTransaction("27-May-16", "CREDIT","Eton", "1187.20"));
        service.addTransaction(constructTransaction("29-May-16", "CREDIT","SMDeptStore", "800"));
        service.addTransaction(constructTransaction("29-May-16", "CREDIT","SMDeptStore", "2650.50"));
        service.addTransaction(constructTransaction("29-May-16", "CREDIT","Mercury Drug", "440.50"));
        service.addTransaction(constructTransaction("30-May-16", "INSTALLMENT", "laptop", "1332.92", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("30-May-16", "PAYMENT","payment", "1424.86", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("6-Jun-16", "CREDIT","Eton", "342.50"));
        assertTransaction(43, new BigDecimal("9208.63"));
        // JULY
        service.addTransaction(constructTransaction("16-Jun-16", "CREDIT","Makati", "237.5"));
        service.addTransaction(constructTransaction("17-Jun-16", "CREDIT","QC", "1502.85"));
        service.addTransaction(constructTransaction("17-Jun-16", "CREDIT","QC", "180"));
        service.addTransaction(constructTransaction("17-Jun-16", "CREDIT","QC", "279.75"));
        service.addTransaction(constructTransaction("20-Jun-16", "INSTALLMENT", "laptop", "1332.91", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("20-Jun-16", "PAYMENT","payment", "3700", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("21-Jun-16", "CREDIT","QC", "94"));
        service.addTransaction(constructTransaction("23-Jun-16", "CREDIT","Pasay", "1861.88"));
        service.addTransaction(constructTransaction("29-Jun-16", "CREDIT","QC", "858.2"));
        service.addTransaction(constructTransaction("29-Jun-16", "INSTALLMENT", "laptop", "1332.92", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("29-Jun-16", "PAYMENT","sm", "5509", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("1-Jul-16", "CREDIT","Paseo", "836"));
        service.addTransaction(constructTransaction("4-Jul-16", "CREDIT","QC", "241.5"));
        service.addTransaction(constructTransaction("5-Jul-16", "CREDIT","Makati", "175"));
        assertTransaction(57, new BigDecimal("8932.14"));
        // AUGUST
        service.addTransaction(constructTransaction("9-Jul-16", "CREDIT","Western", "5200"));
        service.addTransaction(constructTransaction("9-Jul-16", "CREDIT","Mercury Drug", "98"));
        service.addTransaction(constructTransaction("11-Jul-16", "CREDIT","Vue Oracle Exam", "7139.13"));
        service.addTransaction(constructTransaction("12-Jul-16", "INSTALLMENT", "laptop", "1332.91", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("12-Jul-16", "PAYMENT","payment", "20214.82", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("15-Jul-16", "CREDIT","Ever", "1990.4"));
        service.addTransaction(constructTransaction("16-Jul-16", "CREDIT","SM North", "499.75"));
        service.addTransaction(constructTransaction("18-Jul-16", "INSTALLMENT", "laptop", "1332.92", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("18-Jul-16", "PAYMENT","payment", "2665.83", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("26-Jul-16", "CREDIT","Mercury Drug", "175"));
        service.addTransaction(constructTransaction("29-Jul-16", "CREDIT","Eton", "946"));
        assertTransaction(68, new BigDecimal("4765.60"));
        //SEPTEMBER
        service.addTransaction(constructTransaction("12-Aug-16", "PAYMENT","payment", "4765.60"));
        assertTransaction(69, new BigDecimal("0.00"));
        service.addTransaction(constructTransaction("12-Aug-16", "CREDIT","Landmark", "769.7"));
        service.addTransaction(constructTransaction("14-Aug-16", "CREDIT","Landmark", "1609"));
        service.addTransaction(constructTransaction("15-Aug-16", "CREDIT","Eton", "1259.5"));
        service.addTransaction(constructTransaction("21-Aug-16", "CREDIT","Mercury Drug", "185.5"));
        service.addTransaction(constructTransaction("26-Aug-16", "CREDIT","Eton", "797.45"));
        service.addTransaction(constructTransaction("2-Sep-16", "CREDIT","Eton", "315.25"));
        service.addTransaction(constructTransaction("3-Sep-16", "CREDIT","CebuPac TravelMart", "4973"));
        service.addTransaction(constructTransaction("3-Sep-16", "CREDIT","Mercury", "362"));
        service.addTransaction(constructTransaction("10-Sep-16", "INSTALLMENT", "laptop", "2665.83", activeInstallments.get(0)));
        assertTransaction(78, new BigDecimal("12937.23"));
        //OCTOBER
        service.addTransaction(constructTransaction("9-Sep-16", "CREDIT","Eton", "1204.75"));
        service.addTransaction(constructTransaction("16-Sep-16", "PAYMENT","payment", "7821.69", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("17-Sep-16", "CREDIT","SM North", "383.25"));
        service.addTransaction(constructTransaction("17-Sep-16", "INSTALLMENT", "laptop", "2665.83", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("17-Sep-16", "PAYMENT","payment", "5200", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("20-Sep-16", "CREDIT","Ever", "2657.5"));
        service.addTransaction(constructTransaction("20-Sep-16", "CREDIT","Ever", "1030"));
        service.addTransaction(constructTransaction("28-Sep-16", "CREDIT","Mercury Drug", "175"));
        service.addTransaction(constructTransaction("30-Sep-16", "CREDIT","Ever", "646.5"));
        assertTransaction(87, new BigDecimal("8678.37"));
        //NOVEMBER
        service.addTransaction(constructTransaction("12-Oct-16", "CREDIT","Mercury Drug", "175"));
        service.addTransaction(constructTransaction("15-Oct-16", "CREDIT","Ever", "2060.5"));
        service.addTransaction(constructTransaction("17-Oct-16", "INSTALLMENT", "laptop", "1332.91", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("17-Oct-16", "PAYMENT","payment", "8728.91", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("26-Oct-16", "CREDIT","NBS G4", "144.75"));
        service.addTransaction(constructTransaction("29-Oct-16", "CREDIT","Ever", "2542.65"));
        service.addTransaction(constructTransaction("3-Nov-16", "CREDIT","Mercury Drug", "213.5"));
        service.addTransaction(constructTransaction("4-Nov-16", "INSTALLMENT", "laptop", "1332.92", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("4-Nov-16", "PAYMENT","payment", "4968.34", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("4-Nov-16", "CREDIT","Eton", "162"));
        assertTransaction(97, new BigDecimal("2945.35"));
        //DECEMBER - [assumptions]
        service.addTransaction(constructTransaction("11-Nov-16", "CREDIT","TicketWorld c1", "2172"));
        service.addTransaction(constructTransaction("11-Nov-16", "CREDIT","NBS G4", "140"));
        service.addTransaction(constructTransaction("14-Nov-16", "CREDIT","Mercury", "175"));
        service.addTransaction(constructTransaction("16-Nov-16", "INSTALLMENT", "laptop", "1332.91", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("16-Nov-16", "PAYMENT","payment", "4358.51", service.getAllCreditWithoutPayment().get(0)));
        service.addTransaction(constructTransaction("16-Nov-16", "PAYMENT","payment", "1086")); // payment by roiel
        assertTransaction(103, new BigDecimal("1320.75"));
        service.addTransaction(constructTransaction("22-Nov-16", "CREDIT","NBS G4", "695"));
        service.addTransaction(constructTransaction("25-Nov-16", "CREDIT","Enthuware", "502.29")); // $9.95
        service.addTransaction(constructTransaction("30-Nov-16", "CREDIT","Ever", "3151.6"));
        service.addTransaction(constructTransaction("2-Dec-16", "INSTALLMENT", "laptop", "1332.92", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("2-Dec-16", "PAYMENT","payment", "4368.34", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(108, new BigDecimal("2634.22")); // expected

        CcInstallment laptop = service.getInstallments(true).get(0);
        assertEquals(2, laptop.getRemainingMonths().intValue());
        assertEquals(new BigDecimal("5331.66"), laptop.getMonthlyAmortization().multiply(new BigDecimal(laptop.getRemainingMonths()))); // 2665.83*2=5331.66
        assertEquals(new BigDecimal("7997.70"), laptop.getRemainingPayment());
    }

    public void testDeleteTransaction() {
        service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("31990"), 12, new BigDecimal("2665.833333333333"), new LocalDate("2017-02-14").toDate(), new LocalDate("2016-02-14").toDate(), 1)).getEntity();
        List<CcInstallment> activeInstallments = service.getInstallments(true);
        assertEquals(1, activeInstallments.size());

        CcTransaction tranCreditInst = service.addTransaction(constructTransaction("27-Feb-16", "INSTALLMENT", "laptop", "31990", activeInstallments.get(0))).getEntity();
        CcTransaction tranPayment = service.addTransaction(constructTransaction("29-Feb-16", "PAYMENT", "Payment", "31990", service.getAllCreditWithoutPayment().get(0))).getEntity();

        assertEquals(0, service.getInstallments(true).size());

        service.deleteTransaction(tranPayment);
        service.deleteTransaction(tranCreditInst);
        assertEquals(1, service.getInstallments(true).size());
    }

    public void testTranWithMultiplePayment() {
        service.addInstallment(new CcInstallment(
                new Date(), "laptop", new BigDecimal("31990"), 12, new BigDecimal("2665.833333333333"), new Date(), new Date(), 1)).getEntity();
        List<CcInstallment> activeInstallments = service.getInstallments(true);

        service.addTransaction(constructTransaction("21-Apr-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("21-Apr-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        service.addTransaction(constructTransaction("21-Apr-16", "INSTALLMENT", "laptop", "1333", activeInstallments.get(0)));
        assertTransaction(3, new BigDecimal("3999.00"), 3);
        service.addTransaction(constructTransaction("18-Apr-16", "PAYMENT","payment", "1333", service.getAllCreditWithoutPayment().get(0)));
        assertTransaction(4, new BigDecimal("2666.00"), 2);
        service.addTransaction(constructTransaction("18-Apr-16", "PAYMENT","payment", "1"));
        assertTransaction(5, new BigDecimal("2665.00"), 2);
        service.addTransaction(constructTransaction("18-Apr-16", "PAYMENT","payment", "2665", service.getAllCreditWithoutPayment().get(0), service.getAllCreditWithoutPayment().get(1)));
        assertTransaction(6, new BigDecimal("0.00"), 0);
    }

    private CcTransaction constructTransaction(String date, String displayName, String description, String amount) {
        CcTransaction ccTransaction = new CcTransaction();
        ccTransaction.setTranDate(DateUtil.convertStringToLocalDate(date, DateUtil.DATE_PATTERN).toDate());
        ccTransaction.setType(CreditTransactionType.getTypeByDisplayName(displayName).name());
        ccTransaction.setDescription(description);
        ccTransaction.setAmount(NumberUtil.getBigDecimalIfExists(amount));

        System.out.println("Adding "+ ccTransaction.getType() +" transaction...");
        return ccTransaction;
    }

    private CcTransaction constructTransaction(String date, String displayName, String description, String amount, CcInstallment installment) {
        CcTransaction ccTransaction = constructTransaction(date,displayName, description, amount);
        ccTransaction.setInstallment(installment);
        return ccTransaction;
    }

    private CcTransaction constructTransaction(String date, String displayName, String description, String amount, CcTransaction... creditTransInstallment) {
        CcTransaction ccTransaction = constructTransaction(date,displayName, description, amount);
        ccTransaction.setTranCreditInstallments(Arrays.asList(creditTransInstallment));
        return ccTransaction;
    }

    private void assertTransaction(int size, BigDecimal balance) {
        // see list size
        assertEquals(size, service.getAllTransaction().size());

        //compute balance
        BigDecimal outstandingBalance = service.getOutstandingBalance();
        System.out.println("Outstanding Balance: " + outstandingBalance);
        assertEquals(balance, outstandingBalance);
    }

    private void assertTransaction(int size, BigDecimal balance, int activeCredits) {
        assertTransaction(size, balance);
        assertEquals(activeCredits, service.getAllCreditWithoutPayment().size());
    }
}
