package com.hrca.nyquist.customs;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 17.1.2015..
 */
public abstract class PolynomialBaseView<T extends PolynomialElementBaseView> extends LinearLayout {
    public final static String PARCELABLE_ELEMENT_LIST_KEY = "polynomialElementList";

    protected final ArrayList<T> list;
    protected float textSize = 20;
    public PolynomialBaseView(Context context){
        this(context, null);
    }
    public PolynomialBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(HORIZONTAL);
        list = new ArrayList<>();
    }

    protected abstract T getGeneric();

    public void add(){
        T element = this.getGeneric();
        LinearLayout.LayoutParams llp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_VERTICAL;
        element.setLayoutParams(llp);
        element.setTextSize(this.textSize);
        element.setExponent(list.size());

        element.setShowSign(false);
        if(list.size() > 0)
            list.get(list.size()-1).setShowSign(true);
        list.add(element);

        this.addView(element, 0);
    }

    public double[] getCoefficients(){
        PolynomialElementBaseView element;
        double[] coefficients = new double[this.list.size()];
        for(int i = 0; i < this.list.size(); i ++){
            element = this.list.get(i);
            coefficients[i] = element.getNumerator() / element.getDenominator();
        }
        return coefficients;
    }

    public void remove(){
        if(list.size()<1)
            return;
        T element = list.get(list.size()-1);
        list.remove(list.size() - 1);
        this.removeView(element);
        element.recycle();
        if(list.size() > 0)
            list.get(list.size()-1).setShowSign(false);
    }

    public void setTextSize(float size){
        textSize = size;
        for(int i = 0; i < this.list.size(); i ++)
            this.list.get(i).setTextSize(size);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        Parcelable[] states = new Bundle[list.size()];
        for(int i = 0; i < list.size(); i ++){
            states[i] = list.get(i).onSaveInstanceState();
        }
        bundle.putParcelableArray(PARCELABLE_ELEMENT_LIST_KEY, states);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        this.removeAllViews();
        for(int i = 0;  i < this.list.size(); i ++)
            this.list.get(i).recycle();
        this.list.clear();

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable[] states = bundle.getParcelableArray(PARCELABLE_ELEMENT_LIST_KEY);
            for(int i = 0; i < states.length; i ++){
                this.add();
                this.list.get(i).onRestoreInstanceState(states[i]);
            }
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
