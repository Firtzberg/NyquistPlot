package com.hrca.nyquist.nyquistplot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hrca.nyquist.customs.editablecustoms.EditablePolynomialView;


public class PolynomialActivity extends Activity {

    public static final String EXTRA_POLYNOMIAL = "polynomial";
    public static final String PARCELABLE_POLYNOMIAL_EDITOR = "polynomialEditor";
    protected EditablePolynomialView epv;
    protected int identifier;
    protected boolean numerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polynomial);
        epv = (EditablePolynomialView) findViewById(R.id.polynomial);
        this.identifier = getIntent().getIntExtra(InputActivity.EXTRA_POLYNOMIAL_IDENTIFIER, -1);
        this.numerator = getIntent().getBooleanExtra(InputActivity.EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, true);
        if( savedInstanceState == null ) {
            if(getIntent().getParcelableExtra(EXTRA_POLYNOMIAL) != null){
                this.epv.pv.onRestoreInstanceState(getIntent().getParcelableExtra(EXTRA_POLYNOMIAL));
            }
            else {
                this.epv.pv.add();
                this.epv.pv.add();
            }
        }
        this.epv.pv.setTextSize(30);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(PARCELABLE_POLYNOMIAL_EDITOR, this.epv.pv.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.epv.pv.onRestoreInstanceState(savedInstanceState.getParcelable(PARCELABLE_POLYNOMIAL_EDITOR));
    }

    public void done(View v){
        Intent myIntent = new Intent();
        myIntent.putExtra(EXTRA_POLYNOMIAL, this.epv.pv.onSaveInstanceState());
        myIntent.putExtra(InputActivity.EXTRA_POLYNOMIAL_IDENTIFIER, this.identifier);
        myIntent.putExtra(InputActivity.EXTRA_POLYNOMIAL_CHAIN_IDENTIFIER, this.numerator);
        setResult(RESULT_OK, myIntent);
        this.finish();
    }

    public void remove(View view) {
        this.epv.pv.remove();
    }

    public void add(View view) {
        this.epv.pv.add();
    }
}
