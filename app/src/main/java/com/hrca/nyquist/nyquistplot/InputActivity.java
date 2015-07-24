package com.hrca.nyquist.nyquistplot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.hrca.nyquist.customs.displaycustoms.TransferFunctionView;
import com.hrca.nyquist.customs.editablecustoms.EditableTransferFunctionView;


public class InputActivity extends Activity {
    public static final String EXTRA_TRANSFER_FUNCTION = "transferFunction";
    public static final String EXTRA_POLYNOMIAL_IDENTIFIER = "polynomialIdentifier";
    public static final String EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER = "polynomialChainIdentifier";
    public static final String EXTRA_DISPLAY_ERROR_MESSAGE_R_ID = "displayErrorMessageRID";
    public static final int REQUEST_CODE_EDIT_POLYNOMIAL = 123;
    public static final int REQUEST_CODE_HISTORY = 234;
    public static final int REQUEST_CODE_DISPLAY = 345;
    EditableTransferFunctionView transferFunctionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        this.transferFunctionView = (EditableTransferFunctionView) findViewById(R.id.view2);

        this.transferFunctionView.setOnPolynomialClickListener(new EditableTransferFunctionView.OnPolynomialClickListener() {
            @Override
            public void onPolynomialClick(boolean numerator, int identifier, Parcelable savedInstanceState) {
                Intent i = new Intent();
                i.setClass(InputActivity.this, PolynomialActivity.class);
                i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, numerator);
                i.putExtra(EXTRA_POLYNOMIAL_IDENTIFIER, identifier);
                i.putExtra(PolynomialActivity.EXTRA_POLYNOMIAL, savedInstanceState);
                startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
            }
        });
        this.transferFunctionView.setTextSize(20);
    }

    public void addNumeratorButtonClick(View v) {
        Intent i = new Intent();
        i.setClass(InputActivity.this, PolynomialActivity.class);
        i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, true);
        startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
    }

    public void addDenominatorButtonClick(View v) {
        Intent i = new Intent();
        i.setClass(InputActivity.this, PolynomialActivity.class);
        i.putExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, false);
        startActivityForResult(i, REQUEST_CODE_EDIT_POLYNOMIAL);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION, this.transferFunctionView.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        this.transferFunctionView.onRestoreInstanceState(savedInstanceState.getParcelable(TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION));
    }

    public void reset(View v){
        this.transferFunctionView.reset();
    }

    public void historyClick(View v){
        startActivityForResult(new Intent(this, HistoryActivity.class), REQUEST_CODE_HISTORY);
    }

    public void nyquist(View v){
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(EXTRA_TRANSFER_FUNCTION, this.transferFunctionView.onSaveInstanceState());
        startActivityForResult(i, REQUEST_CODE_DISPLAY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_POLYNOMIAL) {
            if (resultCode == RESULT_OK && data != null) {
                Parcelable x = data.getParcelableExtra(PolynomialActivity.EXTRA_POLYNOMIAL);
                boolean chainIdentifier = data.getBooleanExtra(EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, true);
                int identifier = data.getIntExtra(EXTRA_POLYNOMIAL_IDENTIFIER, -1);
                if (x == null)
                    return;
                this.transferFunctionView.updatePolynomial(chainIdentifier, identifier, x);
            }
            return;
        }
        if (requestCode == REQUEST_CODE_HISTORY) {
            if(resultCode == RESULT_OK && data == null){
                this.transferFunctionView.reset();
                return;
            }
            if (resultCode == RESULT_OK && data != null) {
                Parcelable x = data.getParcelableExtra(EXTRA_TRANSFER_FUNCTION);
                if (x != null) {
                    this.transferFunctionView.onRestoreInstanceState(x);
                }
            }
            return;
        }
        if(requestCode == REQUEST_CODE_DISPLAY) {
            if(data != null){
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, null)
                        .setMessage(data.getIntExtra(EXTRA_DISPLAY_ERROR_MESSAGE_R_ID, R.string.numerator_zero))
                        .show();
            }
        }
    }
}
