package com.hrca.nyquist.customs.editablecustoms;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.hrca.nyquist.nyquistplot.R;
import com.hrca.nyquist.customs.PolynomialChainView;
import com.hrca.nyquist.customs.TransferFunctionBaseView;
import com.hrca.nyquist.customs.displaycustoms.PolynomialView;

/**
 * TODO: document your custom view class.
 */
public class EditableTransferFunctionView extends TransferFunctionBaseView {

    final ImageButton arrowUp;
    final ImageButton arrowDown;

    public EditableTransferFunctionView(Context context) {
        this(context, null);
    }

    public EditableTransferFunctionView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_editable_transfer_function);

        this.arrowUp = (ImageButton)findViewById(R.id.arrow_up);
        this.arrowDown = (ImageButton)findViewById(R.id.arrow_down);

        this.arrowUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                astatismView.up();
            }
        });
        this.arrowDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                astatismView.down();
            }
        });
        this.numeratorChainView.setOnPolynomialLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                numeratorChainView.remove((PolynomialView) v);
                return true;
            }
        });
        this.denominatorChainView.setOnPolynomialLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                denominatorChainView.remove((PolynomialView) v);
                return true;
            }
        });
        this.numeratorChainView.setOnPolynomialClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(v instanceof PolynomialView))
                    return;
                if(onPolynomialClickListener == null)
                    return;
                onPolynomialClickListener.onPolynomialClick(true,
                        numeratorChainView.indexOf(v), ((PolynomialView)v).onSaveInstanceState());
            }
        });
        this.denominatorChainView.setOnPolynomialClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(v instanceof PolynomialView))
                    return;
                if(onPolynomialClickListener == null)
                    return;
                onPolynomialClickListener.onPolynomialClick(false,
                        denominatorChainView.indexOf(v), ((PolynomialView)v).onSaveInstanceState());
            }
        });
        init(attrs);
    }

    public abstract interface OnPolynomialClickListener{
        public void onPolynomialClick(boolean numerator, int identifier, Parcelable savedInstanceState);
    }
    protected OnPolynomialClickListener onPolynomialClickListener = null;
    public void setOnPolynomialClickListener(OnPolynomialClickListener l){
        this.onPolynomialClickListener = l;
    }

    private void init(AttributeSet attrs) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EditableTransferFunctionView, 0, 0);
    }

    public void updatePolynomial(boolean chainIdentifier, int identifier, Parcelable x) {
        PolynomialChainView polynomialChainView;
        if(chainIdentifier)
            polynomialChainView = this.numeratorChainView;
        else polynomialChainView = this.denominatorChainView;
        polynomialChainView.updatePolynomial(identifier, x);
    }

    public void reset(){
        this.gainView.setText("1.0");
        this.astatismView.setAstatism(0);
        this.numeratorChainView.reset();
        this.denominatorChainView.reset();
    }
}
