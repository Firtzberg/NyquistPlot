package com.hrca.nyquist.customs.displaycustoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.hrca.nyquist.nyquistplot.R;
import com.hrca.nyquist.customs.TransferFunctionBaseView;

import org.ejml.data.Complex64F;

import java.util.ArrayList;


/**
 * TODO: document your custom view class.
 */
public class TransferFunctionView extends TransferFunctionBaseView {

    private final LinearLayout container;
    private final LinearLayout mainFractal;

    public TransferFunctionView(Context context) {
        this(context, null);
    }

    public TransferFunctionView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_transfer_function);

        this.container = (LinearLayout)findViewById(R.id.tf_container);
        this.mainFractal = (LinearLayout)findViewById(R.id.main_fractal);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TransferFunctionView, 0, 0);
    }

    public int getAstatism(){
        return this.astatismView.getAstatism();
    }

    public void setAstatism(int astatism){
        this.astatismView.setAstatism(astatism);
        this.adjustAstatismVisibility();
    }

    public void adjustAstatismVisibility(){
        if(this.getAstatism() == 0){
            this.astatismView.setVisibility(GONE);
        }
        else {
            this.astatismView.setVisibility(VISIBLE);
        }
    }

    public void adjustMainFractalVisibility(){
        if(this.numeratorChainView.size() == 0 && this.denominatorChainView.size() == 0){
            this.mainFractal.setVisibility(GONE);
        }
        else {
            this.mainFractal.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setGain(double gain){
        super.setGain(gain);
        this.adjustGainVisibility();
    }

    public void adjustGainVisibility(){
        if(this.getGain() == 1 &&
                (this.mainFractal.getVisibility() == VISIBLE
                        || this.astatismView.getVisibility() == VISIBLE)){
            this.gainView.setVisibility(GONE);
        }
        else {
            this.gainView.setVisibility(VISIBLE);
        }
    }

    public void addNumeratorRoots(ArrayList<Complex64F>roots){
        this.numeratorChainView.addRoots(roots);
    }

    public void addDenominatorRoots(ArrayList<Complex64F> roots){
        this.denominatorChainView.addRoots(roots);
    }

    public double[][] getNumeratorCoefficientArrays(){
        return this.numeratorChainView.getCoefficientArrays();
    }

    public double[][] getDenominatorCoefficientArrays(){
        return this.denominatorChainView.getCoefficientArrays();
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener){
        this.container.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
        this.container.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        this.adjustAstatismVisibility();
        this.adjustMainFractalVisibility();
        this.adjustGainVisibility();
    }
}
