package com.hrca.nyquist.customs.editablecustoms;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.hrca.nyquist.customs.PolynomialBaseView;


/**
 * TODO: document your custom view class.
 */
public class EditablePolynomialView extends HorizontalScrollView {
    public final PolynomialBaseView<EditablePolynomialElementView> pv;

    public EditablePolynomialView(Context context) {
        this(context, null);
    }

    public EditablePolynomialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.pv = new PolynomialBaseView<EditablePolynomialElementView>(context) {

            @Override
            protected EditablePolynomialElementView getGeneric() {
                return EditablePolynomialElementView.getUnused(getContext());
            }
        };
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.pv.setLayoutParams(lp);
        this.addView(this.pv);
    }
}
