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
import android.widget.TextView;
import com.hrca.nyquist.nyquistplot.R;


/**
 * TODO: document your custom view class.
 */
public class AstatismView extends LinearLayout {
    public static final String PARCELABLE_ASTATISM_KEY = "astatism";
    protected boolean sign;
    protected final TextView numeratorView;
    protected final View fractionBarView;
    protected final TextView denominatorView;
    protected final SimpleExponentView exponentView;

    public AstatismView(Context context) {
        this(context, null);
    }

    public AstatismView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        this.exponentView = new SimpleExponentView(context);
        this.numeratorView = new TextView(context);
        this.denominatorView = new TextView(context);
        this.fractionBarView = new View(context);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.numeratorView.setLayoutParams(llp);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.numeratorView.setText("1");

        llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.fractionBarView.setLayoutParams(llp);
        this.fractionBarView.setBackgroundColor(Color.BLACK);

        llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.denominatorView.setLayoutParams(llp);
        this.denominatorView.setText("1");

        llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        this.exponentView.setLayoutParams(llp);

        this.addView(this.numeratorView);
        this.addView(this.fractionBarView);
        this.addView(this.denominatorView);

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AstatismView, 0, 0);
        int astatism = -1;
        try {
            astatism = a.getInteger(R.styleable.AstatismView_astatism, 0);
        } finally {
            a.recycle();
        }

        this.setAstatism(astatism);
    }

    public int getAstatism(){
        if(this.sign)
            return this.exponentView.getExponent();
        return -this.exponentView.getExponent();
    }

    public void setAstatism(int astatism){
        int old = this.getAstatism();
        boolean removed = (old == 0);
        if(!removed) {
            if (astatism < 1 && old > 0) {
                this.removeView(this.exponentView);
                this.addView(this.numeratorView, 0);
                removed = true;
            }
            if (astatism > -1 & old < 0) {
                this.removeView(this.exponentView);
                this.addView(this.denominatorView);
                removed = true;
            }
        }

        if(removed){
            if(astatism > 0){
                this.removeView(this.numeratorView);
                this.addView(this.exponentView, 0);
            }
            if(astatism < 0){
                this.removeView(this.denominatorView);
                this.addView(this.exponentView);
            }
        }

        this.sign = !(astatism < 0);
        if(!this.sign)
            astatism = -astatism;
        this.exponentView.setExponent(astatism);
    }

    public void up(){
        this.setAstatism(this.getAstatism() + 1);
    }

    public void down(){
        this.setAstatism(this.getAstatism() - 1);
    }

    public void setTextSize(float size){
        this.numeratorView.setTextSize(size);
        this.denominatorView.setTextSize(size);
        this.exponentView.setTextSize(size);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt(PARCELABLE_ASTATISM_KEY, this.getAstatism());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.setAstatism(bundle.getInt(PARCELABLE_ASTATISM_KEY));
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
