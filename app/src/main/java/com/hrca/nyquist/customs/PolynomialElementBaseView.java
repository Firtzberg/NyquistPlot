package com.hrca.nyquist.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrca.nyquist.nyquistplot.R;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 17.1.2015..
 */
public abstract class PolynomialElementBaseView<T extends TextView> extends RelativeLayout{

    public static final String PARCELABLE_NUMERATOR_KEY = "numerator";
    public static final String PARCELABLE_DENOMINATOR_KEY = "denominator";
    public static final String PARCELABLE_EXPONENT_KEY = "exponent";
    protected double numerator;
    protected double denominator;
    protected boolean sign;
    protected boolean showSign;
    protected final SimpleExponentView exponentView;
    protected final T numeratorView;
    protected final T denominatorView;
    protected final View fractionBarView;
    protected final TextView signView;
    protected final LinearLayout fractalView;


    protected PolynomialElementBaseView(Context context) {
        this(context, null);
    }

    protected PolynomialElementBaseView(Context context, AttributeSet attrs){
        super(context, attrs);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PolynomialElementBaseView,
                0, 0);

        this.sign = true;
        int exponent = 0;
        try {
            exponent = a.getInteger(R.styleable.PolynomialElementBaseView_exponent, 0);
            this.numerator = a.getFloat(R.styleable.PolynomialElementBaseView_numerator, 1);
            this.denominator = a.getFloat(R.styleable.PolynomialElementBaseView_denominator, 1);
            this.showSign = a.getBoolean(R.styleable.PolynomialElementBaseView_show_sign, true);
        } finally {
            a.recycle();
        }

        if(this.numerator < 0){
            this.sign = false;
            this.numerator = -this.numerator;
        }
        this.exponentView = new SimpleExponentView(context);
        this.numeratorView = this.getGeneric(context);
        this.denominatorView = this.getGeneric(context);
        this.fractionBarView = new View(context);
        this.signView = new TextView(context);
        this.fractalView = new LinearLayout(context);

        int id = 1;

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.numeratorView.setLayoutParams(llp);
        this.numeratorView.setText(Double.toString(this.numerator));
        this.numeratorView.setId(++id);

        llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3);
        this.fractionBarView.setLayoutParams(llp);
        this.fractionBarView.setId(++id);
        this.fractionBarView.setBackgroundColor(Color.BLACK);

        llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.denominatorView.setLayoutParams(llp);
        this.denominatorView.setText(Double.toString(this.denominator));
        this.denominatorView.setId(++id);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL);
        this.signView.setLayoutParams(rlp);
        this.signView.setId(++id);
        this.addView(this.signView);

        rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL);
        rlp.addRule(RelativeLayout.RIGHT_OF, this.signView.getId());
        this.fractalView.setLayoutParams(rlp);
        this.fractalView.setId(++id);
        this.fractalView.setOrientation(LinearLayout.VERTICAL);
        this.fractalView.addView(this.numeratorView);
        this.fractalView.addView(this.fractionBarView);
        this.fractalView.addView(this.denominatorView);
        this.fractalView.setPadding(5, 0, 5, 0);
        this.addView(this.fractalView);

        rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL);
        rlp.addRule(RelativeLayout.RIGHT_OF, this.fractalView.getId());
        this.exponentView.setLayoutParams(rlp);
        this.exponentView.setExponent(exponent);
        this.exponentView.setId(++id);
        this.exponentView.setVisibility(exponent == 0 ? GONE : VISIBLE);
        this.addView(this.exponentView);

        reSign();
    }

    public void setShowSign(boolean show){
        if(this.showSign == show)
            return;
        this.showSign = show;
        this.reSign();
    }

    protected void reSign(){
        this.signView.setVisibility(!this.sign | this.showSign ? VISIBLE : GONE);
        this.signView.setText(this.sign ? "+" : "-");
    }

    public double getNumerator() {
        if(this.sign)
            return this.numerator;
        return -this.numerator;
    }

    public abstract void setNumerator(double numerator);
    public double getDenominator() {
        return denominator;
    }
    public abstract void setDenominator(double denominator);
    public int getExponent(){
        return this.exponentView.getExponent();
    }
    public abstract void setExponent(int exponent);
    protected abstract T getGeneric(Context context);

    public void setTextSize(float size) {
        this.signView.setTextSize(size);
        this.numeratorView.setTextSize(size);
        this.denominatorView.setTextSize(size);
        this.exponentView.setTextSize(size);
    }

    protected abstract ArrayList<PolynomialElementBaseView<T>> getRecycleList();

    public void recycle(){
        this.getRecycleList().add(this);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt(PARCELABLE_EXPONENT_KEY, this.getExponent());
        bundle.putDouble(PARCELABLE_NUMERATOR_KEY, this.getNumerator());
        bundle.putDouble(PARCELABLE_DENOMINATOR_KEY, this.getDenominator());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.setExponent(bundle.getInt(PARCELABLE_EXPONENT_KEY));
            this.setNumerator(bundle.getDouble(PARCELABLE_NUMERATOR_KEY));
            this.setDenominator(bundle.getDouble(PARCELABLE_DENOMINATOR_KEY));
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
