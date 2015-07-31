package com.hrca.nyquist.nyquistplot;

import org.ejml.data.Complex64F;

/**
 * Created by hrvoje on 31.07.15..
 */
public interface TFCalculatorInterface {
    Complex64F calculateZero();
    Complex64F calculateTFValue(double frequency);
    Complex64F calculateInfinite();
}
