package com.hrca.nyquist.nyquistplot;

import com.hrca.nyquist.customs.TransferFunctionBaseView;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;

import java.util.ArrayList;

/**
 * Created by hrvoje on 31.07.15..
 */
public class TFCalculator implements TFCalculatorInterface{

    protected final int astatism;
    protected final double[] numeratorVector;
    protected final double[] denominatorVector;

    public TFCalculator(int astatism, double[] numeratorVector, double[] denominatorVector){
        this.astatism = astatism;
        this.numeratorVector = numeratorVector;
        this.denominatorVector = denominatorVector;
    }

    private static Complex64F calculatePolynomialValue(double frequency, double[] coefficients){
        int index = coefficients.length - 1;
        double resultReal = coefficients[index];
        double resultImaginary = 0;
        double tmp;
        for (index --; index >= 0; index --) {
            tmp = resultReal;
            resultReal = -resultImaginary*frequency + coefficients[index];
            resultImaginary = tmp * frequency;
        }
        return new Complex64F(resultReal, resultImaginary);
    }

    public Complex64F calculateZero(){
        Complex64F zero = new Complex64F(0, 0);
        if(astatism == 0){
            zero.real = numeratorVector[0] / denominatorVector[0];
        }
        else if(astatism < 0){
            switch (astatism % 4 + 4){
                case 0:
                    zero.real = Double.POSITIVE_INFINITY;
                    break;
                case 1:
                    zero.imaginary = Double.NEGATIVE_INFINITY;
                    break;
                case 2:
                    zero.real = Double.NEGATIVE_INFINITY;
                    break;
                case 3:
                    zero.imaginary = Double.POSITIVE_INFINITY;
                    break;
            }
        }
        return zero;
    }

    public Complex64F calculateTFValue(double frequency){
        Complex64F value;
        Complex64F numerator = calculatePolynomialValue(frequency, this.numeratorVector);
        Complex64F denominator = calculatePolynomialValue(frequency, this.denominatorVector);
        double temp = denominator.getMagnitude2();
        value = new Complex64F();
        if(temp == 0) {
            value.real = Double.POSITIVE_INFINITY;
        }
        else{
            value.real = numerator.real*denominator.real + numerator.imaginary*denominator.imaginary;
            value.real /= temp;
            value.imaginary = numerator.imaginary*denominator.real - numerator.real*denominator.imaginary;
            value.imaginary /= temp;

            temp = Math.pow(frequency, astatism);
            value.imaginary *= temp;
            value.real *= temp;

            int a = astatism % 4;
            if(a < 0) a += 4;
            for(; a > 0; a--){
                temp = value.real;
                value.real = -value.imaginary;
                value.imaginary = temp;
            }
        }

        return value;
    }

    public Complex64F calculateInfinite() {
        Complex64F infinite = new Complex64F(0, 0);
        int a = astatism + numeratorVector.length - denominatorVector.length;
        if(a > 0){
            double first = numeratorVector[numeratorVector.length - 1];
            double temp = 0;
            if(numeratorVector.length > 1)
                temp = numeratorVector[numeratorVector.length - 2];
            switch (a % 4){
                case 0:
                    infinite.real = first * Double.POSITIVE_INFINITY;
                    if(temp == 0)
                        infinite.imaginary = 0;
                    else
                        infinite.imaginary = temp * Double.NEGATIVE_INFINITY;
                    break;
                case 1:
                    infinite.imaginary = first * Double.POSITIVE_INFINITY;
                    if(a == 1){
                        if(numeratorVector.length > 1) {
                            infinite.real = numeratorVector[numeratorVector.length - 2]
                                    / denominatorVector[denominatorVector.length - 1];
                        } else{
                            infinite.real = 0;
                        }
                    }
                    else if(temp == 0)
                        infinite.real = 0;
                    else
                        infinite.real = temp * Double.POSITIVE_INFINITY;
                    break;
                case 2:
                    infinite.real = first * Double.NEGATIVE_INFINITY;
                    if(temp == 0)
                        infinite.imaginary = 0;
                    else
                        infinite.imaginary = temp * Double.POSITIVE_INFINITY;
                    break;
                case 3:
                    infinite.imaginary = Double.NEGATIVE_INFINITY;
                    if(temp == 0)
                        infinite.real = 0;
                    else
                        infinite.real = temp * Double.NEGATIVE_INFINITY;
                    break;
            }
        }else if (a == 0){
            infinite.real = numeratorVector[numeratorVector.length - 1]
                    / denominatorVector[denominatorVector.length - 1];
        }
        return infinite;
    }
}
