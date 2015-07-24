package com.hrca.nyquist.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hrca.nyquist.nyquistplot.R;


/**
 * TODO: document your custom view class.
 */
public class SimpleExponentView extends RelativeLayout {

    private final TextView baseView;
    private final TextView exponentView;
    private int exponent;

    public SimpleExponentView(Context context) {
        this(context, null);
    }

    public SimpleExponentView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.baseView = new TextView(context);
        this.exponentView = new TextView(context);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SimpleExponentView,
                0, 0);
        String base = "";
        try {
            this.exponent = a.getInteger(R.styleable.SimpleExponentView_simple_exponent, 0);
            base = a.getString(R.styleable.SimpleExponentView_simple_base);
        } finally {
            a.recycle();
        }

        if(base == null || base.equals(""))
            base= "s";
        this.exponentView.setText(String.valueOf(this.exponent));
        this.baseView.setText(base);

        this.baseView.setId(50);
        LayoutParams rlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        this.baseView.setLayoutParams(rlp);
        this.addView(this.baseView);

        rlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.RIGHT_OF, this.baseView.getId());
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        this.exponentView.setLayoutParams(rlp);
        this.addView(this.exponentView);
    }

    public int getExponent() {
        return exponent;
    }

    public void setExponent(int exponent) {
        if(this.exponent == exponent)
            return;
        this.exponent = exponent;
        if(exponent == 1)
            this.exponentView.setVisibility(GONE);
        else {
            this.exponentView.setVisibility(VISIBLE);
            this.exponentView.setText(String.valueOf(exponent));
        }
    }

    public String getBase(){
        return this.baseView.getText().toString();
    }

    public void setBase(String base){
        if(this.baseView.getText().toString().equals(base))
            return;
        this.baseView.setText(base);
    }

    public void setTextSize(float size) {
        this.baseView.setTextSize(size);
        this.exponentView.setTextSize(size/2);
    }
}
