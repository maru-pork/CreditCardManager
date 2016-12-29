package com.nnayram.ccmanager.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.core.DateUtil;
import com.nnayram.ccmanager.core.ResultWrapper;
import com.nnayram.ccmanager.db.DBHelper;
import com.nnayram.ccmanager.model.CCInstallmentPayment;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.service.CCManagerService;
import com.nnayram.ccmanager.view.AddInstallmentDialog;

import java.util.List;

/**
 * Created by Rufo on 12/3/2016.
 */
public class InstallmentActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private CCManagerService service;

    private Spinner spInstallment;
    private Spinner spOperation;

    private TextView tvDescription;
    private TextView tvPrincipalAmount;
    private TextView tvMonthlyAmortization;
    private TextView tvDuration;
    private TextView tvAmountPaid;
    private TextView tvRemainingBalance;

    private TableLayout tblPayment;
    private TableRow trTranHeader, trTranRow;
    private TextView tvTranRowID, tvTranRow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = new CCManagerService(DBHelper.getInstance(getBaseContext()), getBaseContext());
        setContentView(R.layout.activity_installment);
        init();
    }

    private void init() {
        spInstallment = (Spinner) findViewById(R.id.sp_cc_installment);
        spOperation = (Spinner) findViewById(R.id.sp_op);

        tvDescription = (TextView) findViewById(R.id.tv_inst_desc);
        tvPrincipalAmount = (TextView) findViewById(R.id.tv_inst_principal_amt);
        tvMonthlyAmortization = (TextView) findViewById(R.id.tv_inst_mo_amo);
        tvDuration = (TextView) findViewById(R.id.tv_inst_duration);
        tvAmountPaid = (TextView) findViewById(R.id.tv_inst_amount_paid);
        tvRemainingBalance = (TextView) findViewById(R.id.tv_inst_remaining_bal);

        tblPayment = (TableLayout) findViewById(R.id.tbl_inst_payment);
        trTranHeader = (TableRow) findViewById(R.id.tr_inst_header);
        tvTranRowID = (TextView) findViewById(R.id.tv_inst_tranRow);

        // init data
        refreshInstallmentAdapter();

        // add listener
        spInstallment.setOnItemSelectedListener(this);
        ((Button) findViewById(R.id.btn_back)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_op)).setOnClickListener(this);
    }

    private void refreshInstallmentAdapter() {
        spInstallment.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, service.getInstallments(false)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.btn_op:
                if ("ADD".equals(spOperation.getSelectedItem())) {
                    new AddInstallmentDialog(this){
                        @Override
                        public void setAddOnClickAction(CcInstallment ccInstallment) {
                            ResultWrapper<CcInstallment> resultWrapper = service.addInstallment(ccInstallment);
                            if(validateResultWrapper(resultWrapper)) {
                                Toast.makeText(InstallmentActivity.this, "Successfully added new installment.", Toast.LENGTH_LONG).show();
                                dismiss();
                                refreshInstallmentAdapter();
                            }
                        }
                    }.show();

                } else
                if ("DELETE".equals(spOperation.getSelectedItem())) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(InstallmentActivity.this);
                    alertDialogBuilder.setMessage("Are you sure you want to remove installment?");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResultWrapper<CcInstallment> resultWrapper = service.deleteInstallment((CcInstallment) spInstallment.getSelectedItem());
                            if(validateResultWrapper(resultWrapper)) {
                                Toast.makeText(InstallmentActivity.this, "Successfully deleted installment.", Toast.LENGTH_LONG).show();
                            }
                            refreshInstallmentAdapter();
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Close", null);
                    alertDialogBuilder.show();

                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.sp_cc_installment:
                CcInstallment installment = (CcInstallment) item;
                tvDescription.setText(installment.getDescription());
                tvPrincipalAmount.setText(installment.getFormattedPrincipalAmount());
                tvMonthlyAmortization.setText(installment.getFormattedMonthlyAmortization()
                        + " [" + String.valueOf(installment.getMonthsToPay()) + " mos]");
                tvDuration.setText(DateFormat.format(DateUtil.DATE_PATTERN, installment.getStartDate())
                        + " to " + DateFormat.format(DateUtil.DATE_PATTERN, installment.getEndDate()));
                tvAmountPaid.setText(installment.getFormattedTotalPaymentMade());
                tvRemainingBalance.setText(installment.getFormattedRemainingPayment()
                        + " [" + installment.getRemainingMonths() + " mos]");

                tblPayment.removeAllViews();
                tblPayment.addView(trTranHeader);

                List<CCInstallmentPayment> payments = installment.getPaidInstallmentPayments();
                for (int x=0; x<payments.size(); x++) {
                    trTranRow = new TableRow(this);
                    trTranRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

                    tvTranRow = new TextView(this);
                    tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
                    tvTranRow.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvTranRow.setText(String.valueOf(x+1));
                    trTranRow.addView(tvTranRow);

                    tvTranRow = new TextView(this);
                    tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
                    tvTranRow.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvTranRow.setText(DateFormat.format(DateUtil.DATE_PATTERN, payments.get(x).getDate()));
                    trTranRow.addView(tvTranRow);

                    tvTranRow = new TextView(this);
                    tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
                    tvTranRow.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvTranRow.setText(payments.get(x).getFormattedAmount());
                    trTranRow.addView(tvTranRow);

                    tblPayment.addView(trTranRow);
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
}
