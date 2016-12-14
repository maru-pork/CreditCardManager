package com.nnayram.ccmanager.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.core.InstallmentAdapter;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.model.InstallmentCheckbox;
import com.nnayram.ccmanager.model.PaymentType;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Rufo on 11/9/2016.
 */
public abstract class TransactionAddDialog extends Dialog implements AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener, View.OnClickListener {
    private Activity m;

    private TableRow trPaymentType, trTranAmount, trPrincipalAmount, trMonthlyAmortization, trMonthsToPay, trUntil, trInclude;
    private EditText etTranDate, etTranAmount, etPrincipalAmount, etMonthlyAmortization, etMonthsToPay, etUntil;
    private AutoCompleteTextView acTvTranDesc;
    private Spinner spTranType, spPaymentType;
    private ListView lvInstallments;
    private InstallmentAdapter installmentAdapter;

    public TransactionAddDialog(Activity activity) {
        super(activity);
        this.m = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cc_mngr_add);
        setTitle(m.getString(R.string.title_cc_add_transaction));
        setCancelable(false);

        /*
         init components
          */
        // common
        etTranDate = (EditText) findViewById(R.id.et_cc_tranDate); // Transaction Date:
        spTranType = (Spinner) findViewById(R.id.sp_cc_tranType); // Transaction Type:
        acTvTranDesc = (AutoCompleteTextView) findViewById(R.id.at_cc_tranDesc); // Description
        etTranAmount = (EditText) findViewById(R.id.et_cc_tranAmount); // Amount

        // specific for PAYMENT

        // specific for INSTALLMENT

        trPaymentType = (TableRow) findViewById(R.id.tr_cc_include);
/*        trTranAmount = (TableRow) findViewById(R.id.tr_cc_tranAmount);
        trPrincipalAmount = (TableRow) findViewById(R.id.tr_cc_principalAmount);
        trMonthlyAmortization = (TableRow) findViewById(R.id.tr_cc_monthlyAmortization);
        trMonthsToPay = (TableRow) findViewById(R.id.tr_cc_monthsToPay);
        trUntil = (TableRow) findViewById(R.id.tr_cc_until);*/
        trInclude = (TableRow) findViewById(R.id.tr_cc_include);

        etPrincipalAmount = (EditText) findViewById(R.id.et_cc_principalAmount);
        etMonthlyAmortization = (EditText) findViewById(R.id.et_cc_monthlyAmortization);
        etMonthsToPay = (EditText) findViewById(R.id.et_cc_monthsToPay);
        etUntil = (EditText) findViewById(R.id.et_cc_end_date);

        spPaymentType = (Spinner) findViewById(R.id.sp_cc_installment);
        lvInstallments = (ListView) findViewById(R.id.lv_cc_credit_inst);

        // buttons
        Button btnAdd = (Button) findViewById(R.id.btn_cc_add);
        Button btnCancel = (Button) findViewById(R.id.btn_cc_cancel);

        // setup visibility

        // setup components data display
        etTranDate.setInputType(InputType.TYPE_NULL);
        etTranDate.setText(DateFormat.format("MM/dd/yy", Calendar.getInstance().getTime()));

        // setup adapter
        acTvTranDesc.setAdapter(new ArrayAdapter<>(m, R.layout.cc_description_info, R.id.tv_cc_description, m.getResources().getStringArray(R.array.arr_cc_desc)));

        // setup listener
        etMonthsToPay.setOnEditorActionListener(this);
        spTranType.setOnItemSelectedListener(this);
        spPaymentType.setOnItemSelectedListener(this);
        etTranDate.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.et_cc_monthsToPay:
                etMonthlyAmortization.setText(
                        computeMonthlyAmortization(new BigDecimal(etPrincipalAmount.getText().toString()), new BigDecimal(etMonthsToPay.getText().toString())));
                etUntil.setText(
                        computeUntil(LocalDate.parse(etTranDate.getText().toString(), DateTimeFormat.forPattern("MM/dd/yy")), Integer.valueOf(etMonthsToPay.getText().toString())));
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);

        switch (parent.getId()) {
            // TRANSACTION TYPE dropdown: CREDIT | INSTALLMENT | CHARGE | PAYMENT
            case R.id.sp_cc_tranType:
                CreditTransactionType selectedTranType = CreditTransactionType.valueOf(item.toString());
                trPaymentType.setVisibility(selectedTranType.compareTo(CreditTransactionType.PAYMENT) == 0 ? View.VISIBLE : View.GONE);
                // TODO
//                trTranAmount.setVisibility(selectedTranType.compareTo(CreditTransactionType.INSTALLMENT) == 0 ? View.GONE : View.VISIBLE);
//                trPrincipalAmount.setVisibility(selectedTranType.compareTo(CreditTransactionType.INSTALLMENT) == 0 ? View.VISIBLE : View.GONE);
//                trMonthlyAmortization.setVisibility(selectedTranType.compareTo(CreditTransactionType.INSTALLMENT) == 0 ? View.VISIBLE : View.GONE);
//                trMonthsToPay.setVisibility(selectedTranType.compareTo(CreditTransactionType.INSTALLMENT) == 0 ? View.VISIBLE : View.GONE);
//                trUntil.setVisibility(selectedTranType.compareTo(CreditTransactionType.INSTALLMENT) == 0 ? View.VISIBLE : View.GONE);
                trInclude.setVisibility(selectedTranType.compareTo(CreditTransactionType.PAYMENT) == 0 ? View.VISIBLE : View.GONE);

                if (selectedTranType.compareTo(CreditTransactionType.PAYMENT) == 0) {
                    acTvTranDesc.setText(spPaymentType.getSelectedItem().toString());
                    acTvTranDesc.setEnabled(false);

                    installmentAdapter = new InstallmentAdapter(m, R.layout.cc_installment_info, getInstallmentList());
                    lvInstallments.setAdapter(installmentAdapter);
                } else {
                    acTvTranDesc.setText(StringUtils.EMPTY);
                    acTvTranDesc.setEnabled(true);
                }
                break;

            // PAYMENT TYPE dropdown: CBC_INTERNET_PAYMENT | CHINABANK | SM_BILLS_PAYMENT | OTHERS
            case R.id.sp_cc_installment:
                PaymentType selectedPaymentType = PaymentType.valueOf(item.toString());
                if (selectedPaymentType.compareTo(PaymentType.Others) == 0) {
                    acTvTranDesc.setText(StringUtils.EMPTY);
                    acTvTranDesc.setEnabled(true);
                } else {
                    acTvTranDesc.setText(selectedPaymentType.name());
                    acTvTranDesc.setEnabled(false);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }

    private String computeMonthlyAmortization(BigDecimal principalAmount, BigDecimal monthsToPay) {
        return String.valueOf(principalAmount.divide(monthsToPay, 2, BigDecimal.ROUND_HALF_UP));
    }

    private String computeUntil(LocalDate tranDate, int monthsToPay) {
        return tranDate.plusMonths(monthsToPay).toString("MM/dd/yy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cc_add:
                String responseText = "";
                ArrayList<InstallmentCheckbox> countryList = installmentAdapter.getInstallmentList();
                for(int i=0;i<countryList.size();i++){
                    InstallmentCheckbox country = countryList.get(i);
                    if(country.isSelected()){
                        responseText += country.getName();
                    }
                }

                Toast.makeText(m,
                        responseText, Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_cc_cancel:
                dismiss();
                break;

            case R.id.et_cc_tranDate:
                final Calendar currentCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        m,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                currentCalendar.set(Calendar.YEAR, year);
                                currentCalendar.set(Calendar.MONTH, monthOfYear);
                                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                etTranDate.setText(DateFormat.format("MM/dd/yy", currentCalendar.getTime()));
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

    private ArrayList<InstallmentCheckbox> getInstallmentList() {
        ArrayList<InstallmentCheckbox> installmentList = new ArrayList<>();
        installmentList.add(new InstallmentCheckbox("Computer", false));
        installmentList.add(new InstallmentCheckbox("Desktop", false));
        installmentList.add(new InstallmentCheckbox("Cellphone", false));

        return installmentList;
    }
}
