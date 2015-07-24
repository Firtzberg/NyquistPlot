package com.hrca.nyquist.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.hrca.nyquist.nyquistplot.R;

/**
 * Created by hrvoje on 05.07.15..
 */
public abstract class TransferFunctionBaseView extends HorizontalScrollView {

    public static final String PARCELABLE_GAIN_KEY = "gain";
    public static final String PARCELABLE_ASTATISM_KEY = "astatismValue";
    public static final String PARCELABLE_NUMERATOR_KEY = "numeratorChain";
    public static final String PARCELABLE_DENOMINATOR_KEY = "denominatorChain";
    public static final String PARCELABLE_TRANSFER_FUNCTION = "transferFunction";

    protected final TextView gainView;
    protected final AstatismView astatismView;
    protected final PolynomialChainView numeratorChainView;
    protected final PolynomialChainView denominatorChainView;

    public TransferFunctionBaseView(Context context, int layout) {
        this(context, null, layout);
    }

    public TransferFunctionBaseView(Context context, AttributeSet attrs, int layout) {
        super(context, attrs);

        inflate(getContext(), layout, this);

        this.gainView = (TextView)findViewById(R.id.gain);
        this.astatismView = (AstatismView)findViewById(R.id.astatism);
        this.numeratorChainView = (PolynomialChainView)findViewById(R.id.numerator_chain);
        this.denominatorChainView = (PolynomialChainView)findViewById(R.id.denominator_chain);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TransferFunctionBaseView, 0, 0);
        float gain = 1;
        int astatism = -1;
        try {
            gain = a.getInteger(R.styleable.TransferFunctionBaseView_gain, 1);
            astatism = a.getInt(R.styleable.TransferFunctionBaseView_tf_astatism, -1);
        } finally {
            a.recycle();
        }
        this.gainView.setText(Double.toString(gain));
        this.astatismView.setAstatism(astatism);
        this.setTextSize(20);
    }

    public double getGain(){
        double gain = 1;
        try {
            gain = Double.parseDouble(this.gainView.getText().toString());
        }
        catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        return gain;
    }

    public void setGain(double gain){
        this.gainView.setText(Double.toString(gain));
    }

    public void setTextSize(float size){
        this.gainView.setTextSize(size);
        this.astatismView.setTextSize(size);
        this.numeratorChainView.setTextSize(size);
        this.denominatorChainView.setTextSize(size);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putDouble(PARCELABLE_GAIN_KEY, this.getGain());
        bundle.putParcelable(PARCELABLE_ASTATISM_KEY, this.astatismView.onSaveInstanceState());
        bundle.putParcelable(PARCELABLE_NUMERATOR_KEY, this.numeratorChainView.onSaveInstanceState());
        bundle.putParcelable(PARCELABLE_DENOMINATOR_KEY, this.denominatorChainView.onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.setGain(bundle.getDouble(PARCELABLE_GAIN_KEY));
            this.astatismView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_ASTATISM_KEY));
            this.numeratorChainView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_NUMERATOR_KEY));
            this.denominatorChainView.onRestoreInstanceState(bundle.getParcelable(PARCELABLE_DENOMINATOR_KEY));
            state = bundle.getParcelable("instanceState");
        }
        if(state != null)
            super.onRestoreInstanceState(state);
    }
}
