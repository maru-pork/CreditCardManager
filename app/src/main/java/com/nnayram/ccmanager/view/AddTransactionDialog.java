package com.nnayram.ccmanager.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
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

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.core.DateUtil;
import com.nnayram.ccmanager.core.InstallmentAdapter;
import com.nnayram.ccmanager.core.NumberUtil;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.model.InstallmentCheckbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Rufo on 11/28/2016.
 */
public abstract class AddTransactionDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Activity m;

    // common component
    private EditText etTranDate;
    private EditText etTranAmount;
    private Spinner spTranType;
    private AutoCompleteTextView acTvTranDesc;

    // credit_inst component
    private Spinner spInstallment;

    // payment component
    private TableRow trInclude;
    private ListView lvCreditInst;
    private InstallmentAdapter installmentAdapter;

    public AddTransactionDialog(Activity activity) {
        super(activity);
        this.m = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_cc_mngr_add);
        setTitle(m.getString(R.string.title_cc_add_transaction));
        setCancelable(false);

        /**
         * init components
         */
        // common
        etTranDate = (EditText) findViewById(R.id.et_cc_tranDate); // Transaction Date:
        spTranType = (Spinner) findViewById(R.id.sp_cc_tranType); // Transaction Type:
        acTvTranDesc = (AutoCompleteTextView) findViewById(R.id.at_cc_tranDesc); // Description:
        etTranAmount = (EditText) findViewById(R.id.et_cc_tranAmount); // Amount:

        // credit_inst
        spInstallment = (Spinner) findViewById(R.id.sp_cc_installment);

        // payment
        trInclude = (TableRow) findViewById(R.id.tr_cc_include);
        lvCreditInst = (ListView) findViewById(R.id.lv_cc_credit_inst);

        // buttons
        Button btnAdd = (Button) findViewById(R.id.btn_cc_add);
        Button btnCancel = (Button) findViewById(R.id.btn_cc_cancel);

        // init listeners
        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        etTranDate.setOnClickListener(this);
        spTranType.setOnItemSelectedListener(this);

        // init data
        etTranDate.setInputType(InputType.TYPE_NULL);
        etTranDate.setText(DateFormat.format(DateUtil.DATE_PATTERN, Calendar.getInstance().getTime()));
        spTranType.setAdapter(new ArrayAdapter<>(m, android.R.layout.simple_spinner_dropdown_item, Arrays.asList(CreditTransactionType.getAllTranDisplayName())));
        spInstallment.setAdapter(new ArrayAdapter<>(m, android.R.layout.simple_spinner_dropdown_item, getAllInstallment()));
        acTvTranDesc.setAdapter(new ArrayAdapter<>(m, R.layout.cc_description_info, R.id.tv_cc_description, getAllDescription()));

        installmentAdapter = new InstallmentAdapter(m, R.layout.cc_installment_info, getAllCreditInst());
        lvCreditInst.setAdapter(installmentAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);

        switch (parent.getId()) {
            case R.id.sp_cc_tranType:
                CreditTransactionType selectedTranType = CreditTransactionType.getTypeByDisplayName(item.toString());
                // set visibility
                trInclude.setVisibility((selectedTranType.compareTo(CreditTransactionType.PAYMENT) == 0
                        && !getAllCreditInst().isEmpty()) ? View.VISIBLE : View.GONE);
                spInstallment.setVisibility(selectedTranType.compareTo(CreditTransactionType.CREDIT_INST) == 0 ? View.VISIBLE : View.GONE);
                acTvTranDesc.setVisibility(selectedTranType.compareTo(CreditTransactionType.CREDIT_INST) == 0 ? View.GONE : View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally blank
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cc_add:
                CcTransaction ccTransaction = new CcTransaction();
                ccTransaction.setTranDate(DateUtil.convertStringToLocalDate(String.valueOf(etTranDate.getText()), DateUtil.DATE_PATTERN).toDate());
                ccTransaction.setType(CreditTransactionType.getTypeByDisplayName(String.valueOf(spTranType.getSelectedItem())).name());
                ccTransaction.setDescription(String.valueOf(acTvTranDesc.getText()));
                ccTransaction.setAmount(NumberUtil.getBigDecimalIfExists(String.valueOf(etTranAmount.getText())));

                // for CREDIT INST
                ccTransaction.setInstallment((CcInstallment) spInstallment.getSelectedItem());

                // for PAYMENT
                List<CcTransaction> installmentList = new ArrayList<>();
                for (InstallmentCheckbox ic : installmentAdapter.getInstallmentList()) {
                    if(ic.isSelected())
                        installmentList.add(ic.getTranCreditInst());
                }
                ccTransaction.setTranCreditInstallments(installmentList);

                setAddOnClickAction(ccTransaction);
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
                                etTranDate.setText(DateFormat.format(DateUtil.DATE_PATTERN, currentCalendar.getTime()));
                            }
                        },
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
                break;

            default:
                break;

        }
    }

    /**
     *
     * @param ccTransaction
     */
    public abstract void setAddOnClickAction(CcTransaction ccTransaction);

    /**
     *
     * @return list of all saved description
     */
    public abstract List<String> getAllDescription();

    /**
     *
     * @return list of all transaction credit_inst without payment
     */
    public abstract ArrayList<InstallmentCheckbox> getAllCreditInst();

    /**
     *
     * @return list of all active installment
     */
    public abstract List<CcInstallment> getAllInstallment();

}
