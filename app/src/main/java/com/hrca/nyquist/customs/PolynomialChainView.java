package com.hrca.nyquist.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hrca.nyquist.nyquistplot.R;
import com.hrca.nyquist.customs.displaycustoms.PolynomialView;

import org.ejml.data.Complex64F;

import java.util.ArrayList;


/**
 * TODO: document your custom view class.
 */
public class PolynomialChainView extends LinearLayout {
    protected final static String PARCELABLE_POLYNOMIAL_LIST_KEY = "polynomialList";

    protected float textSize = 20;
    protected static final ArrayList<TextView> unusedTextViews = new ArrayList<>();
    private final ArrayList<TextView> usedTextViews;
    protected final ArrayList<PolynomialView> list;

    private OnClickListener onPolynomialClickListener = null;
    public void setOnPolynomialClickListener(OnClickListener l){
        this.onPolynomialClickListener = l;
        for(int i = 0; i < this.list.size(); i ++){
            if(this.onPolynomialClickListener != null)
                this.list.get(i).setOnClickListener(this.onPolynomialClickListener);
            else this.list.get(i).setClickable(false);
        }
    }

    private OnLongClickListener onPolynomialLongClickListener = null;
    public void setOnPolynomialLongClickListener(OnLongClickListener l){
        this.onPolynomialLongClickListener = l;
        for(int i = 0; i < this.list.size(); i ++){
            if(this.onPolynomialLongClickListener != null)
                this.list.get(i).setOnLongClickListener(this.onPolynomialLongClickListener);
            else this.list.get(i).setLongClickable(false);
        }
    }

    public PolynomialChainView(Context context) {
        this(context, null);
    }

    public PolynomialChainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.list = new ArrayList<>();
        this.usedTextViews = new ArrayList<>();
        this.setOne();

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        this.setOrientation(HORIZONTAL);
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PolynomialChainView, 0, 0);
        textSize = 20;
    }

    protected TextView getUnusedTextView(){
        TextView Tmp;
        if(unusedTextViews.size() > 0)
        {
            Tmp = unusedTextViews.get(unusedTextViews.size()-1);
            unusedTextViews.remove(unusedTextViews.size() - 1);
        }
        else{
            Tmp = new TextView(getContext());
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            llp.gravity = Gravity.CENTER_VERTICAL;
            Tmp.setLayoutParams(llp);
        }
        this.usedTextViews.add(Tmp);
        return Tmp;
    }

    protected void freeTextView(TextView tv){
        this.removeView(tv);
        unusedTextViews.add(tv);
        int index = this.usedTextViews.indexOf(tv);
        if(index < 0)
            return;
        this.usedTextViews.remove(index);
    }

    public double[][] getCoefficientArrays(){
        double[][] result = new double[this.list.size()][];
        for(int i = 0; i < this.list.size(); i ++){
            result[i] = this.list.get(i).getCoefficients();
        }
        return result;
    }

    public void add(PolynomialView pv){
        if(this.list.size() == 0) {
            if(this.usedTextViews.size() > 0)
                this.freeTextView(this.usedTextViews.get(0));
        }
        this.list.add(pv);

        this.addBracket(true);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_VERTICAL;
        pv.setLayoutParams(llp);
        pv.setTextSize(this.textSize);
        if(this.onPolynomialClickListener != null)
            pv.setOnClickListener(this.onPolynomialClickListener);
        else pv.setClickable(false);
        if(this.onPolynomialLongClickListener != null)
            pv.setOnLongClickListener(this.onPolynomialLongClickListener);
        else pv.setLongClickable(false);
        this.addView(pv);
        this.addBracket(false);
    }

    public void addRoots(ArrayList<Complex64F> roots){
        PolynomialView pv;
        for(int i = 0; i < roots.size(); i ++){
            if(roots.get(i) == null)
                continue;
            pv = PolynomialView.getUnused(this.getContext());
            pv.setRoot(roots.get(i));
            this.add(pv);
            if(!roots.get(i).isReal()){
                for(int j = i + 1; j < roots.size(); j ++){
                    if(roots.get(j).real == roots.get(i).real && roots.get(j).imaginary == -roots.get(i).imaginary) {
                        roots.remove(j);
                        break;
                    }
                }
            }
        }
    }

    public void remove(PolynomialView pv){
        int index = this.list.indexOf(pv);
        this.list.remove(pv);
        this.removeView(pv);
        if(index < 0)
            return;
        pv.recycle();
        if(this.usedTextViews.size() > 2*index+1){
            this.freeTextView(this.usedTextViews.get(2*index + 1));
            this.freeTextView(this.usedTextViews.get(2*index));
        }

        if(list.size() == 0)
            this.setOne();
    }

    private void setOne(){
        TextView oneView = this.getUnusedTextView();
        oneView.setText("1");
        oneView.setTextSize(this.textSize);
        this.addView(oneView);
    }

    private void addBracket(boolean open){
        TextView bracket = this.getUnusedTextView();
        if(open)
            bracket.setText("(");
        else bracket.setText(")");
        bracket.setTextSize(this.textSize);

        this.addView(bracket);
    }

    public void setTextSize(float size) {
        this.textSize = size;
        for(int i = 0; i < this.list.size(); i ++)
            this.list.get(i).setTextSize(size);
        if(this.usedTextViews.size() > 1)
        for(int i = 0; i < this.usedTextViews.size(); i ++)
            this.usedTextViews.get(i).setTextSize(size);
        else this.usedTextViews.get(0).setTextSize(size);
    }

    public void reset() {
        for(int i = this.list.size() - 1; i >= 0; i --){
            this.remove(this.list.get(i));
        }
    }

    public void updatePolynomial(int identifier, Parcelable polynomialState){
        if(identifier < 0 || identifier >= this.list.size()){
            PolynomialView polynomialView = PolynomialView.getUnused(getContext());
            polynomialView.onRestoreInstanceState(polynomialState);
            this.add(polynomialView);
            return;
        }
        this.list.get(identifier).onRestoreInstanceState(polynomialState);
    }

    public int size(){
        return this.list.size();
    }

    public int indexOf(View v){
        return this.list.indexOf(v);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        Parcelable[] states = new Bundle[list.size()];
        for(int i = 0; i < list.size(); i ++){
            states[i] = list.get(i).onSaveInstanceState();
        }
        bundle.putParcelableArray(PARCELABLE_POLYNOMIAL_LIST_KEY, states);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        reset();
        if(this.usedTextViews.size() > 0)
            this.freeTextView(this.usedTextViews.get(0));

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable[] states = bundle.getParcelableArray(PARCELABLE_POLYNOMIAL_LIST_KEY);
            PolynomialView element;
            if(states.length > 0) {
                for (Parcelable s : states) {
                    element = PolynomialView.getUnused(getContext());
                    element.onRestoreInstanceState(s);
                    this.add(element);
                }
            }
            else this.setOne();
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
