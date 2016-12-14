package com.nnayram.ccmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.service.CcManagerRepository;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.nnayram.ccmanager.core.DateUtil.getDateOrThrow;
import static com.nnayram.ccmanager.core.NumberUtil.getBigDecimalIfExists;
import static com.nnayram.ccmanager.core.NumberUtil.getBigDecimalOrThrow;
import static com.nnayram.ccmanager.db.DBContract.Installment;
import static com.nnayram.ccmanager.db.DBContract.PaymentInstallment;
import static com.nnayram.ccmanager.db.DBContract.Transaction;

/**
 * Created by Rufo on 11/10/2016.
 */
public class DBHelper extends SQLiteOpenHelper implements CcManagerRepository {

    private static boolean isTest;

    private static DBHelper sInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "cc_manager.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * constructor used for testing only
     * @param context
     * @param isTest
     */
    public DBHelper(Context context, boolean isTest) {
        super(context, null, null, DATABASE_VERSION);
        this.isTest = isTest;
    }

    public static synchronized DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Transaction.TABLE_NAME + " (" +
                Transaction._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Transaction.COLUMN_DATE + " INTEGER NOT NULL," +
                Transaction.COLUMN_TYPE + " TEXT NOT NULL," +
                Transaction.COLUMN_DESC + " TEXT NOT NULL," +
                Transaction.COLUMN_AMOUNT + " REAL NOT NULL," +
                Transaction.COLUMN_INSTALLMENT + " INTEGER);");
        db.execSQL("CREATE TABLE " + Installment.TABLE_NAME + " (" +
                Installment._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Installment.COLUMN_DATE + " INTEGER NOT NULL," +
                Installment.COLUMN_DESC + " TEXT NOT NULL," +
                Installment.COLUMN_PRINCIPAL_AMOUNT + " REAL NOT NULL," +
                Installment.COLUMN_MONTHS_TO_PAY + " INTEGER NOT NULL," +
                Installment.COLUMN_MONTHLY_AMO + " REAL NOT NULL," +
                Installment.COLUMN_START_DATE + " INTEGER NOT NULL," +
                Installment.COLUMN_END_DATE + " INTEGER NOT NULL," +
                Installment.COLUMN_ACTIVE + " INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE " + PaymentInstallment.TABLE_NAME + " ("
                + PaymentInstallment.COLUMN_INSTALLMENT + " INTEGER NOT NULL,"
                + PaymentInstallment.COLUMN_TRAN_CREDIT_INST + " INTEGER NOT NULL,"
                + PaymentInstallment.COLUMN_TRAN_PAYMENT + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "Updating database from version " + oldVersion  + " to " + newVersion + " which will destroy all data.");
        // drop table if upgraded
        db.execSQL("DROP TABLE IF EXISTS " + Transaction.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Installment.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PaymentInstallment.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public long insertTransaction(Date date, String type, String description, BigDecimal amount) {
        if (!Arrays.asList(new String[]{
                CreditTransactionType.CREDIT.name(),
                CreditTransactionType.CREDIT_CHARGE.name()}).contains(type))
            throw new IllegalArgumentException("Invalid type");

        SQLiteDatabase db = null;
        long result = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(Transaction.COLUMN_DATE, date.getTime());
            cv.put(Transaction.COLUMN_TYPE, type);
            cv.put(Transaction.COLUMN_DESC, description);
            cv.put(Transaction.COLUMN_AMOUNT, amount.toPlainString());

            result = db.insert(Transaction.TABLE_NAME, null, cv);
        } finally {
            close(db);
        }
        return result;
    }

    @Override
    public long insertTransaction(Date date, String type, String description, BigDecimal amount, long installmentId) {
        if (!CreditTransactionType.CREDIT_INST.name().equals(type))
            throw new IllegalArgumentException("Invalid type");

        SQLiteDatabase db = null;
        long result = 0;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put(Transaction.COLUMN_DATE, date.getTime());
            cv.put(Transaction.COLUMN_TYPE, type);
            cv.put(Transaction.COLUMN_DESC, description);
            cv.put(Transaction.COLUMN_AMOUNT, amount.toPlainString());
            cv.put(Transaction.COLUMN_INSTALLMENT, installmentId);
            result = db.insert(Transaction.TABLE_NAME, null, cv);

            cv = new ContentValues();
            cv.put(PaymentInstallment.COLUMN_INSTALLMENT, installmentId);
            cv.put(PaymentInstallment.COLUMN_TRAN_CREDIT_INST, result);
            long resultId = db.insert(PaymentInstallment.TABLE_NAME, null, cv);

            if (resultId != 0) db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            close(db);
        }
        return result;
    }

    @Override
    public long insertTransaction(Date date, String type, String description, BigDecimal amount, String[] tranCreditIds) {
        if (!CreditTransactionType.PAYMENT.name().equals(type))
            throw new IllegalArgumentException("Invalid type");

        SQLiteDatabase db = null;
        long result = 0;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            // insert Transaction
            ContentValues cv = new ContentValues();
            cv.put(Transaction.COLUMN_DATE, date.getTime());
            cv.put(Transaction.COLUMN_TYPE, type);
            cv.put(Transaction.COLUMN_DESC, description);
            cv.put(Transaction.COLUMN_AMOUNT, amount.toPlainString());
            result = db.insert(Transaction.TABLE_NAME, null, cv);

            // update PaymentInstallment
            long updatedRows = 1;
            if (!ArrayUtils.isEmpty(tranCreditIds)) {
                cv = new ContentValues();
                cv.put(PaymentInstallment.COLUMN_TRAN_PAYMENT, result);
                String whereClause = PaymentInstallment.COLUMN_TRAN_CREDIT_INST + " IN (" + TextUtils.join(",", Collections.nCopies(tranCreditIds.length, "?"))  + ")";
                updatedRows = db.update(PaymentInstallment.TABLE_NAME, cv, whereClause, tranCreditIds);
            }

            // update Installment

            if (updatedRows!=0) db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            close(db);
        }
        return result;
    }

    @Override
    public long deleteTransaction(long id, String type) {
        SQLiteDatabase db = null;
        long result = 0;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            String[] whereArgs = new String[]{String.valueOf(id)};
            result = db.delete(Transaction.TABLE_NAME, Transaction._ID+"=?", whereArgs);

            if (CreditTransactionType.PAYMENT.name().equals(type)) {
                // if PAYMENT: Update PaymentInstallment. Since the payment transaction was just deleted, set payment column to null.
                ContentValues cv = new ContentValues();
                cv.putNull(PaymentInstallment.COLUMN_TRAN_PAYMENT);

                String whereClause = PaymentInstallment.COLUMN_TRAN_PAYMENT+"=?";
                db.update(PaymentInstallment.TABLE_NAME, cv, whereClause, whereArgs);

            } else if(CreditTransactionType.CREDIT_INST.name().equals(type)) {
                // if CREDIT_INST: Delete PaymentInstallment where payment column has no value or else deletion not allowed.
                String whereClause = PaymentInstallment.COLUMN_TRAN_CREDIT_INST + "=? AND " + PaymentInstallment.COLUMN_TRAN_PAYMENT + " IS NULL";
                long deletedRows = db.delete(PaymentInstallment.TABLE_NAME, whereClause, whereArgs);

                if (deletedRows == 0)
                    throw new IllegalArgumentException("Current transaction is still referenced to other Payment transaction.");
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            close(db);
        }
        return result;
    }

    @Override
    public long insertInstallment(Date date, String description, BigDecimal principalAmount, int monthsToPay, BigDecimal monthlyAmo, Date startDate, Date endDate, int active) {
        SQLiteDatabase db = null;
        long result = 0;
        try {
            db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(Installment.COLUMN_DATE, date.getTime());
            cv.put(Installment.COLUMN_DESC, description);
            cv.put(Installment.COLUMN_PRINCIPAL_AMOUNT, principalAmount.toPlainString());
            cv.put(Installment.COLUMN_MONTHS_TO_PAY, monthsToPay);
            cv.put(Installment.COLUMN_MONTHLY_AMO, monthlyAmo.toPlainString());
            cv.put(Installment.COLUMN_START_DATE, startDate.getTime());
            cv.put(Installment.COLUMN_END_DATE, endDate.getTime());
            cv.put(Installment.COLUMN_ACTIVE, active);

            result = db.insert(Installment.TABLE_NAME, null, cv);
        } finally {
            close(db);
        }
        return result;
    }

    @Override
    public List<CcTransaction> getAllTransaction() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<CcTransaction> transactions = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            String orderBy = Transaction._ID + " DESC";
            cursor = db.query(Transaction.TABLE_NAME, null, null, null, null, null, orderBy);
            while (cursor.moveToNext()) {
                transactions.add(constructTransactionFrCursor(cursor));
            }
        } finally {
            close(db, cursor);
        }
        return transactions;
    }

    @Override
    public List<CcInstallment> getAllInstallment() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<CcInstallment> installments = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            String orderBy = Installment._ID + " DESC";
            cursor = db.query(Installment.TABLE_NAME, null, null, null, null, null, orderBy);
            while (cursor.moveToNext()) {
                installments.add(constructInstallmentFrCursor(cursor));
            }
        } finally {
            close(db, cursor);
        }
        return installments;
    }

    @Override
    public List<CcInstallment> getAllActiveInstallment() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<CcInstallment> installments = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            String orderBy = Installment._ID + " DESC";
            String selection = Installment.COLUMN_ACTIVE+"=?";
            String[] selectionArgs = new String[]{"1"};
            cursor = db.query(Installment.TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
            while (cursor.moveToNext()) {
                installments.add(constructInstallmentFrCursor(cursor));
            }
        } finally {
            close(db, cursor);
        }
        return installments;
    }

    @Override
    public List<Long> getAllCreditWithoutPayment() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Long> transactions = new ArrayList<>();
        try {
            db = this.getReadableDatabase();

            String selection = PaymentInstallment.COLUMN_TRAN_PAYMENT+" IS NULL";
            cursor = db.query(PaymentInstallment.TABLE_NAME, null, selection, null, null, null, null);
            while (cursor.moveToNext()) {
                transactions.add(cursor.getLong(cursor.getColumnIndex(PaymentInstallment.COLUMN_TRAN_CREDIT_INST)));
            }
        } finally {
            close(db, cursor);
        }
        return transactions;
    }

    @Override
    public List<Long> getAllPayment(Long installmentId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Long> transactions = new ArrayList<>();
        try {
            db = this.getReadableDatabase();

            String selection = PaymentInstallment.COLUMN_INSTALLMENT + "=? AND " + PaymentInstallment.COLUMN_TRAN_PAYMENT + " IS NOT NULL";
            String[] selectionArgs = new String[]{String.valueOf(installmentId)};
            cursor = db.query(PaymentInstallment.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                transactions.add(cursor.getLong(cursor.getColumnIndex(PaymentInstallment.COLUMN_TRAN_CREDIT_INST)));
            }
        } finally {
            close(db, cursor);
        }
        return transactions;
    }

    @Override
    public List<Long> getPaymentInstallment(Long id, String type) {
        if (!Arrays.asList(new String[]{
                CreditTransactionType.CREDIT_INST.name(),
                CreditTransactionType.PAYMENT.name()}).contains(type))
            throw new IllegalArgumentException("Invalid type");

        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Long> transactions = new ArrayList<>();
        try {
            db = this.getReadableDatabase();

            if (!Arrays.asList(new String[]{CreditTransactionType.CREDIT_INST.name(), CreditTransactionType.PAYMENT.name()}).contains(type))
                throw new IllegalArgumentException("Invalid type");

            String selectionColumn = CreditTransactionType.CREDIT_INST.name().equals(type) ? PaymentInstallment.COLUMN_TRAN_CREDIT_INST :
                    CreditTransactionType.PAYMENT.name().equals(type) ? PaymentInstallment.COLUMN_TRAN_PAYMENT : "";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            cursor = db.query(PaymentInstallment.TABLE_NAME, null, selectionColumn+"=?", selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                transactions.add(cursor.getLong(cursor.getColumnIndex(PaymentInstallment.COLUMN_INSTALLMENT)));
            }
        } finally {
            close(db, cursor);
        }
        return transactions;
    }

    @Override
    public CcTransaction getTransaction(long id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        CcTransaction transaction = null;
        try {
            db = this.getReadableDatabase();
            String selection = Transaction._ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(id)};
            cursor = db.query(Transaction.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                transaction = constructTransactionFrCursor(cursor);
            }
        } finally {
            close(db, cursor);
        }
        return transaction;
    }

    @Override
    public CcInstallment getInstallment(long id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        CcInstallment installment = null;
        try {
            db = this.getReadableDatabase();
            String selection = Installment._ID + "=?";
            String[] selectionArgs = new String[] {String.valueOf(id)};
            cursor = db.query(Installment.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                installment = constructInstallmentFrCursor(cursor);
            }
        } finally {
            close(db, cursor);
        }
        return installment;
    }

    @Override
    public BigDecimal sum(String[] types) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        BigDecimal result = null;
        try {
            db = this.getReadableDatabase();
            String colSum = "SUM(" + Transaction.COLUMN_AMOUNT +")";
            String[] cols = new String[]{colSum};
            String selection = Transaction.COLUMN_TYPE + " IN (" + TextUtils.join(",", Collections.nCopies(types.length, "?"))  + ")";

            cursor = db.query(Transaction.TABLE_NAME, cols, selection, types, null, null, null);
            if (cursor.moveToFirst()) {
                result = getBigDecimalIfExists(cursor.getFloat(cursor.getColumnIndex(colSum)));
            }
        } finally {
            close(db, cursor);
        }
        return result;
    }

    @Override
    public BigDecimal getOutstandingBalance() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        BigDecimal result = null;
        try {
            db = this.getReadableDatabase();

            String asTotalCredit = "totalCredit";
            String asTotalPayment = "totalPayment";

            String queryTotalCredit = "SELECT COALESCE(SUM("+ Transaction.COLUMN_AMOUNT +"), 0) as " + asTotalCredit + " FROM " + Transaction.TABLE_NAME +
                    " WHERE " + Transaction.COLUMN_TYPE + " IN('" + TextUtils.join("','", CreditTransactionType.getCredit()) + "')";
            String queryTotalPayment = "SELECT COALESCE(SUM("+ Transaction.COLUMN_AMOUNT +"), 0) as " + asTotalPayment + " FROM " + Transaction.TABLE_NAME +
                    " WHERE " + Transaction.COLUMN_TYPE + " IN('" + TextUtils.join("','", CreditTransactionType.getPayment())+ "')";

            String col = asTotalCredit + " - " + asTotalPayment;
            String[] cols = new String[]{col};
            String from = "(" + queryTotalCredit + ") c,(" + queryTotalPayment + ") p";
            String query = SQLiteQueryBuilder.buildQueryString(false, from, cols, null, null, null, null, null);
            /**
             * http://sqlfiddle.com/
             * SELECT totalCredit - totalPayment FROM
             *  (SELECT COALESCE(SUM(tran_amount), 0) as totalCredit FROM ccTransaction WHERE tran_type IN('CREDIT','CREDIT_INST','CREDIT_CHARGE')) c,
             *  (SELECT COALESCE(SUM(tran_amount), 0) as totalPayment FROM ccTransaction WHERE tran_type IN('PAYMENT')) p
             */
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                result = getBigDecimalIfExists(cursor.getFloat(cursor.getColumnIndex(col)));
            }
        } finally {
            close(db, cursor);
        }
        return result;
    }

    @Override
    public List<String> getAllDescription() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<String> descriptions = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            String[] cols = new String[]{Transaction.COLUMN_DESC};
            cursor = db.query(true, Transaction.TABLE_NAME, cols, null, null, null, null, null, "10");
            while (cursor.moveToNext()) {
                descriptions.add(cursor.getString(cursor.getColumnIndex(Transaction.COLUMN_DESC)));
            }
        } finally {
            close(db, cursor);
        }
        return descriptions;
    }

    private CcTransaction constructTransactionFrCursor(Cursor cursor) {
        CcTransaction transaction = new CcTransaction();
        transaction.setId(cursor.getLong(cursor.getColumnIndex(Transaction._ID)));
        transaction.setTranDate(getDateOrThrow(cursor.getLong(cursor.getColumnIndex(Transaction.COLUMN_DATE))));
        transaction.setType(cursor.getString(cursor.getColumnIndex(Transaction.COLUMN_TYPE)));
        transaction.setDescription(cursor.getString(cursor.getColumnIndex(Transaction.COLUMN_DESC)));
        transaction.setAmount(getBigDecimalOrThrow(cursor.getFloat(cursor.getColumnIndex(Transaction.COLUMN_AMOUNT))));
        transaction.setInstallment(cursor.getLong(cursor.getColumnIndex(Transaction.COLUMN_INSTALLMENT)));
        return transaction;
    }

    private CcInstallment constructInstallmentFrCursor(Cursor cursor) {
        CcInstallment installment = new CcInstallment();
        installment.setId(cursor.getLong(cursor.getColumnIndex(Installment._ID)));
        installment.setDate(getDateOrThrow(cursor.getLong(cursor.getColumnIndex(Installment.COLUMN_DATE))));
        installment.setDescription(cursor.getString(cursor.getColumnIndex(Installment.COLUMN_DESC)));
        installment.setPrincipalAmount(getBigDecimalOrThrow(cursor.getFloat(cursor.getColumnIndex(Installment.COLUMN_PRINCIPAL_AMOUNT))));
        installment.setMonthsToPay(cursor.getInt(cursor.getColumnIndex(Installment.COLUMN_MONTHS_TO_PAY)));
        installment.setMonthlyAmortization(getBigDecimalOrThrow(cursor.getFloat(cursor.getColumnIndex(Installment.COLUMN_MONTHLY_AMO))));
        installment.setStartDate(getDateOrThrow(cursor.getLong(cursor.getColumnIndex(Installment.COLUMN_START_DATE))));
        installment.setEndDate(getDateOrThrow(cursor.getLong(cursor.getColumnIndex(Installment.COLUMN_END_DATE))));
        installment.setActive(cursor.getInt(cursor.getColumnIndex(Installment.COLUMN_ACTIVE)));
        return installment;
    }

    /**
     * Closes database and cursor if they are not null.
     * @param sqLiteDatabase to be closed if not null
     * @param cursor         to be closed if not null
     */
    private synchronized void close(SQLiteDatabase sqLiteDatabase, Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
        close(sqLiteDatabase);
    }

    /**
     * Closes database if they are not null.
     * @param sqLiteDatabase
     */
    private synchronized void close(SQLiteDatabase sqLiteDatabase) {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            if (!isTest) sqLiteDatabase.close();
        }
    }
}
