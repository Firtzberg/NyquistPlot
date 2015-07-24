package com.hrca.nyquist.customs.editablecustoms;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.hrca.nyquist.customs.PolynomialElementBaseView;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 17.1.2015..
 */
public class EditablePolynomialElementView extends PolynomialElementBaseView<EditText> {

    private static ArrayList<PolynomialElementBaseView<EditText>> recycleList = new ArrayList<>();

    private EditablePolynomialElementView(Context context) {
        this(context, null);
    }

    private EditablePolynomialElementView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        this.numeratorView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        this.numeratorView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    numerator = Double.parseDouble(s.toString());
                } catch (NumberFormatException nfe){
                    numerator = 1;
                }
            }
        });
        this.denominatorView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_CLASS_NUMBER);
        this.denominatorView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    denominator = Double.parseDouble(s.toString());
                } catch (NumberFormatException nfe) {
                    denominator = 1;
                }
            }
        });

        this.signView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sign = !sign;
                reSign();
            }
        });
        reSign();
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
        this.numeratorView.setText(Float.toString((float)numerator));
    }

    @Override
    public void setDenominator(double denominator) {
        if(denominator == 0)
            denominator = 1;
        if(this.denominator == denominator)
            return;
        this.denominator = denominator;
        this.denominatorView.setText(Float.toString((float)denominator));
    }

    @Override
    public void setExponent(int exponent) {
        if(this.getExponent() == exponent)
            return;
        if((this.getExponent() == 0) ^ (exponent == 0)){
            this.exponentView.setExponent(exponent);
            this.exponentView.setVisibility(exponent == 0 ? GONE : VISIBLE);
        }
        else{
            this.exponentView.setExponent(exponent);
        }
    }

    @Override
    protected EditText getGeneric(Context context) {
        return new EditText(context);
    }

    @Override
    protected ArrayList<PolynomialElementBaseView<EditText>> getRecycleList() {
        return recycleList;
    }

    public static EditablePolynomialElementView getUnused(Context context){
        EditablePolynomialElementView editablePolynomialElementView;
        if(recycleList.size() > 0){
            editablePolynomialElementView = (EditablePolynomialElementView)recycleList.get(recycleList.size() - 1);
            recycleList.remove(recycleList.size() - 1);
            return editablePolynomialElementView;
        }
        editablePolynomialElementView = new EditablePolynomialElementView(context);
        return editablePolynomialElementView;
    }
}
