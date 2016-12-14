package com.nnayram.ccmanager.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.core.DateUtil;
import com.nnayram.ccmanager.core.NumberUtil;
import com.nnayram.ccmanager.model.CcInstallment;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by Rufo on 12/3/2016.
 */
public abstract class AddInstallmentDialog extends Dialog implements View.OnClickListener, TextView.OnFocusChangeListener {

    private Activity m;

    private EditText etDate;
    private EditText etDescription;
    private EditText etPrincipalAmount;
    private EditText etMonthsToPay;
    private EditText etMonthlyAmortization;
    private EditText etEndDate;

    public AddInstallmentDialog(Activity activity) {
        super(activity);
        this.m = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cc_mngr_installment);
        setTitle(m.getString(R.string.title_cc_add_installment));
        setCancelable(false);

        /**
         * init components
         */
        etDate = (EditText) findViewById(R.id.et_cc_date);
        etDescription = (EditText) findViewById(R.id.et_cc_desc);
        etPrincipalAmount = (EditText) findViewById(R.id.et_cc_principalAmount);
        etMonthsToPay = (EditText) findViewById(R.id.et_cc_monthsToPay);
        etMonthlyAmortization = (EditText) findViewById(R.id.et_cc_monthlyAmortization);
        etEndDate = (EditText) findViewById(R.id.et_cc_end_date);
        Button btnAdd = (Button) findViewById(R.id.btn_cc_inst_add);
        Button btnCancel = (Button) findViewById(R.id.btn_cc_inst_cancel);

        /**
         * add listeners
         */
        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        etPrincipalAmount.setOnFocusChangeListener(this);
        etMonthsToPay.setOnFocusChangeListener(this);
        etDate.setOnClickListener(this);

        /**
         * init data
         */
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setText(DateFormat.format(DateUtil.DATE_PATTERN, Calendar.getInstance().getTime()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cc_inst_add:
                CcInstallment installment = new CcInstallment();
                installment.setDate(DateUtil.convertString(String.valueOf(etDate.getText())));
                installment.setDescription(String.valueOf(etDescription.getText().toString()));
                installment.setPrincipalAmount(NumberUtil.getBigDecimalIfExists(String.valueOf(etPrincipalAmount.getText())));
                installment.setMonthsToPay(NumberUtil.getIntegerIfExists(etMonthsToPay.getText().toString()));
                installment.setMonthlyAmortization(NumberUtil.getBigDecimalIfExists(String.valueOf(etMonthlyAmortization.getText())));
                installment.setStartDate(DateUtil.convertString(String.valueOf(etDate.getText())));
                installment.setEndDate(DateUtil.convertString(String.valueOf(etEndDate.getText())));

                setAddOnClickAction(installment);
                break;
            case R.id.btn_cc_inst_cancel:
                dismiss();
                break;
            case R.id.et_cc_date:
                final Calendar currentCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        m,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                currentCalendar.set(Calendar.YEAR, year);
                                currentCalendar.set(Calendar.MONTH, monthOfYear);
                                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                etDate.setText(DateFormat.format(DateUtil.DATE_PATTERN, currentCalendar.getTime()));
                            }
                        },
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_cc_monthsToPay:
                computeInstallment();
                break;
            case R.id.et_cc_principalAmount:
                computeInstallment();
                break;
        }
    }

    private void computeInstallment() {
        BigDecimal principalAmount = NumberUtil.getBigDecimalIfExists(etPrincipalAmount.getText().toString());
        BigDecimal monthsToPay = NumberUtil.getBigDecimalIfExists(etMonthsToPay.getText().toString());
        LocalDate startDate = DateUtil.convertStringToLocalDate(etDate.getText().toString(), DateUtil.DATE_PATTERN);

        if (monthsToPay.compareTo(BigDecimal.ZERO) != 0) {
            etEndDate.setText(startDate.plusMonths(monthsToPay.intValue()).toString(DateUtil.DATE_PATTERN));
            etMonthlyAmortization.setText(principalAmount.divide(monthsToPay, 2, BigDecimal.ROUND_HALF_UP).toPlainString());
        }
    }

    public abstract void setAddOnClickAction(CcInstallment ccInstallment);
}
