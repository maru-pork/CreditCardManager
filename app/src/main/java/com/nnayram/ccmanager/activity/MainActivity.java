package com.nnayram.ccmanager.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.core.DateUtil;
import com.nnayram.ccmanager.core.NumberUtil;
import com.nnayram.ccmanager.core.ResultWrapper;
import com.nnayram.ccmanager.db.DBHelper;
import com.nnayram.ccmanager.model.CcInstallment;
import com.nnayram.ccmanager.model.CcTransaction;
import com.nnayram.ccmanager.model.CreditTransactionType;
import com.nnayram.ccmanager.model.InstallmentCheckbox;
import com.nnayram.ccmanager.service.CCManagerService;
import com.nnayram.ccmanager.view.AddTransactionDialog;
import com.nnayram.ccmanager.view.Pageable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private CCManagerService service;

    private TableLayout tblMain;
    private TableRow trTranHeader, trTranRow;
    private TextView tvTranRowID, tvTranRow, tvPageCount, tvBalance;
    private LinearLayout lytLineSeparator;

    private Pageable<CcTransaction> pageableTran;
    private List<CcTransaction> tranList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize service
        service = new CCManagerService(DBHelper.getInstance(getBaseContext()), getBaseContext());
        // set layout
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        // init components
        tblMain = (TableLayout) findViewById(R.id.tbl_cc_main);
        trTranHeader = (TableRow) findViewById(R.id.tr_cc_tranHeader);
        tvTranRowID = (TextView) findViewById(R.id.tv_cc_tranRow);
        tvPageCount = (TextView) findViewById(R.id.tv_cc_tranPageCount);
        tvBalance = (TextView) findViewById(R.id.tv_cc_balance);

        Button btnNext = (Button) findViewById(R.id.btn_next);
        Button btnPrev = (Button) findViewById(R.id.btn_previous);
        Button btnAddTran = (Button) findViewById(R.id.btn_add_tran);
        Button btnInstallment = (Button) findViewById(R.id.btn_installment);

        // init listeners
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnAddTran.setOnClickListener(this);
        btnInstallment.setOnClickListener(this);

        // init data
        refreshDisplay();
    }

    private void refreshDisplay() {
        tvBalance.setText(NumberUtil.format().format(NumberUtil.getBigDecimalIfExists(service.getOutstandingBalance())));

        tranList = new ArrayList<>();
        tranList.addAll(service.getAllTransaction());
        pageableTran = new Pageable<>(tranList);
        pageableTran.setPageSize(10);
        pageableTran.setPage(1);
        tvPageCount.setText(getString(R.string.page_of, pageableTran.getPage(), pageableTran.getMaxPages()));

        refreshMainTable();
    }

    private void refreshMainTable() {
        tblMain.removeAllViews();
        tblMain.addView(trTranHeader);
        for (final CcTransaction cc : pageableTran.getListForPage()) {
            trTranRow = new TableRow(this);
            trTranRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
            trTranRow.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setMessage("Are you sure you want to remove transaction: " + cc.getType() +"[P"+cc.getFormattedAmount()+"]?");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResultWrapper<CcTransaction> resultWrapper = service.deleteTransaction(cc);
                            if(validateResultWrapper(resultWrapper)) {
                                Toast.makeText(MainActivity.this, "Successfully deleted transaction.", Toast.LENGTH_LONG).show();
                            }
                            refreshDisplay();
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Close", null);
                    alertDialogBuilder.show();
                    return false;
                }
            });

            // Transaction Date
            tvTranRow = new TextView(this);
            tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
            tvTranRow.setGravity(Gravity.CENTER_HORIZONTAL);
            tvTranRow.setText(DateFormat.format(DateUtil.DATE_PATTERN, cc.getTranDate()));
            trTranRow.addView(tvTranRow);

            // Transaction Type
            tvTranRow = new TextView(this);
            tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
            tvTranRow.setGravity(Gravity.CENTER_HORIZONTAL);
            tvTranRow.setText(CreditTransactionType.valueOf(cc.getType()).getDisplayName());
            trTranRow.addView(tvTranRow);

            // Description
            tvTranRow = new TextView(this);
            tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
            tvTranRow.setGravity(Gravity.LEFT);
            tvTranRow.setText(cc.getDescription());
            trTranRow.addView(tvTranRow);

            // Amount
            tvTranRow = new TextView(this);
            tvTranRow.setLayoutParams(tvTranRowID.getLayoutParams());
            tvTranRow.setGravity(Gravity.RIGHT);
            tvTranRow.setText(String.valueOf(cc.getFormattedAmount()));
            trTranRow.addView(tvTranRow);

            // Line Separator
            lytLineSeparator = new LinearLayout(this);
            lytLineSeparator.setOrientation(LinearLayout.VERTICAL);
            lytLineSeparator.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            lytLineSeparator.setBackgroundColor(Color.parseColor("#5e7974"));

            tblMain.addView(trTranRow);
            tblMain.addView(lytLineSeparator);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                pageableTran.setPage(pageableTran.getNextPage());
                if (!tranList.isEmpty()) {
                    trTranRow.removeAllViews();
                }
                refreshMainTable();
                tvPageCount.setText("Page " + pageableTran.getPage() + " of " + pageableTran.getMaxPages());
                break;

            case R.id.btn_previous:
                pageableTran.setPage(pageableTran.getPreviousPage());
                if (!tranList.isEmpty()) {
                    trTranRow.removeAllViews();
                }
                refreshMainTable();
                tvPageCount.setText("Page " + pageableTran.getPage() + " of " + pageableTran.getMaxPages());
                break;

            case R.id.btn_add_tran:
                new AddTransactionDialog(this) {
                    @Override
                    public void setAddOnClickAction(CcTransaction ccTransaction) {
                        ResultWrapper<CcTransaction> resultWrapper = service.addTransaction(ccTransaction);
                        if (validateResultWrapper(resultWrapper)){
                            Toast.makeText(MainActivity.this, "Successfully added new transaction.", Toast.LENGTH_LONG).show();
                            dismiss();
                            refreshDisplay();
                        }
                    }

                    @Override
                    public List<String> getAllDescription() {
                        return service.getAllDescription();
                    }

                    @Override
                    public ArrayList<InstallmentCheckbox> getAllCreditInst() {
                        ArrayList<InstallmentCheckbox> installmentList = new ArrayList<>();
                        for (CcTransaction tran : service.getAllCreditWithoutPayment()) {
                            installmentList.add(new InstallmentCheckbox(
                                    tran.getDescription() + " ["+ tran.getFormattedAmount() +"]", tran, true));
                        }
                        return installmentList;
                    }

                    @Override
                    public List<CcInstallment> getAllInstallment() {
                        return service.getAllActiveInstallment();
                    }
                }.show();
                break;

            case R.id.btn_installment:
                startActivity(new Intent(this, InstallmentActivity.class));
                break;
        }
    }

}
