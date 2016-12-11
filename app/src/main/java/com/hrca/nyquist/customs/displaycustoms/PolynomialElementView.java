package com.hrca.nyquist.customs.displaycustoms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hrca.nyquist.customs.PolynomialElementBaseView;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 17.1.2015..
 */
public class PolynomialElementView extends PolynomialElementBaseView<TextView> {

    private static ArrayList<PolynomialElementBaseView<TextView>> recycleList = new ArrayList<>();

    private PolynomialElementView(Context context) {
        this(context, null);
    }

    private PolynomialElementView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        this.reposition();
    }

    private String valueToText(double value){
        float tmp = (float)value;
        if(Math.abs(Math.round(tmp) - tmp) < 0.02){
            return Integer.toString((int)tmp);
        }
        else{
            return Float.toString(tmp);
        }
    }

    protected void reposition(){
        this.setVisibility((float)this.numerator == 0 ? GONE : VISIBLE);

        reSign();

        if(this.denominator == 1 && this.numerator == 1 && this.getExponent() != 0)
            this.fractalView.setVisibility(GONE);
        else {
            this.fractalView.setVisibility(VISIBLE);
            this.numeratorView.setText(this.valueToText(this.numerator));

            if(this.denominator == 1){
                this.denominatorView.setVisibility(GONE);
                this.fractionBarView.setVisibility(GONE);

            }
            else{
                this.denominatorView.setText(this.valueToText(this.denominator));
                this.fractionBarView.setVisibility(VISIBLE);
                this.denominatorView.setVisibility(VISIBLE);
            }
        }

        this.exponentView.setVisibility(this.getExponent() == 0 ? GONE : VISIBLE);
    }

    @Override
    public void setNumerator(double numerator) {
        if(this.sign ^ (numerator >= 0)) {
            this.sign = !this.sign;
            reSign();
        }
        if(!this.sign)
            numerator = -numerator;
        if(this.numerator == numerator)
            return;
        this.numerator = numerator;
        this.reposition();
    }

    @Override
    public void setDenominator(double denominator) {
        if(denominator == 0)
            denominator = 1;
        if(this.denominator == denominator)
            return;
        this.denominator = denominator;
        this.reposition();
    }

    @Override
    public void setExponent(int exponent) {
        if(this.getExponent() == exponent)
            return;
        if((this.getExponent() == 0) ^ (exponent == 0)){
            this.exponentView.setExponent(exponent);
            this.reposition();
        }
        else{
            this.exponentView.setExponent(exponent);
        }
    }

    @Override
    protected TextView getGeneric(Context context) {
        return new TextView(context);
    }

    @Override
    protected ArrayList<PolynomialElementBaseView<TextView>> getRecycleList() {
        return recycleList;
    }

    public static PolynomialElementView getUnused(Context context){
        PolynomialElementView pev;
        if(recycleList.size() > 0){
            pev = (PolynomialElementView)recycleList.get(recycleList.size() - 1);
            recycleList.remove(recycleList.size() - 1);
            return pev;
        }
        pev = new PolynomialElementView(context);
        return pev;
    }
}
