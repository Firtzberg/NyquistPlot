package com.hrca.nyquist.nyquistplot;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.ejml.data.Complex64F;


/**
 * TODO: document your custom view class.
 */
public class DiagramView extends SurfaceView {
    private static final float DENSITY_COEFFICIENT = Resources.getSystem().getDisplayMetrics().density;
    private static final float PIXELS_LEFT_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_TOP_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_RIGHT_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_BOTTOM_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float linesLength = 5 * DENSITY_COEFFICIENT;
    private static final int COLOR_DEFAULT_BACKGROUND = Color.WHITE;
    private static final int COLOR_DEFAULT_LINES = Color.LTGRAY;
    private static final int COLOR_DEFAULT_AXIS = Color.BLACK;
    private static final int COLOR_DEFAULT_CURVE = Color.BLUE;
    private static final int COLOR_DEFAULT_SECONDARY_CURVE = Color.RED;
    private static final int COLOR_DEFAULT_UNIT_CIRCLE = Color.GREEN;
    private static final int COLOR_DEFAULT_TEXT = Color.BLACK;
    private static final float SIZE_DEFAULT_TEXT = 12 * Resources.getSystem().getDisplayMetrics().scaledDensity;
    private static final float RELATIVE_CURVE_THICKNESS = 1.5F;
    private static final double FREQUENCY_DENSITY = 20;
    private static final double FREQUENCY_LOG_EXPANSION = 1.0;
    private float pixelsPerUnit;
    double[] numeratorVector;
    double[] denominatorVector;
    int astatism;
    private double minFrequency;
    private double maxFrequency;
    private Complex64F min;
    private Complex64F max;
    private int backgroundColor;
    private final Paint backgroundPaint;
    private final Paint linesPaint;
    private final Paint axisPaint;
    private final Paint curvePaint;
    private final Paint secondaryCurvePaint;
    private final Paint unitCirclePaint;
    private final Paint textPaint;

    public DiagramView(Context context) {
        this(context, null);
    }

    public DiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.backgroundPaint = new Paint();
        this.linesPaint = new Paint();
        this.linesPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.axisPaint = new Paint();
        this.axisPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.curvePaint = new Paint();
        this.curvePaint.setStrokeWidth(RELATIVE_CURVE_THICKNESS * DENSITY_COEFFICIENT);
        this.secondaryCurvePaint = new Paint();
        this.secondaryCurvePaint.setStrokeWidth(RELATIVE_CURVE_THICKNESS * DENSITY_COEFFICIENT);
        this.unitCirclePaint = new Paint();
        this.unitCirclePaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.unitCirclePaint.setStyle(Paint.Style.STROKE);
        this.textPaint = new Paint();
        this.textPaint.setStrokeWidth(DENSITY_COEFFICIENT);

        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DiagramView,
                0, 0);
        int axisColor;
        int curveColor;
        int secondaryCurveColor;
        int unitCircleColor;
        int linesColor;
        int textColor;
        float textSize;
        try {
            this.backgroundColor = a.getColor(R.styleable.DiagramView_background_color, COLOR_DEFAULT_BACKGROUND);
            linesColor = a.getColor(R.styleable.DiagramView_lines_color, COLOR_DEFAULT_LINES);
            axisColor = a.getColor(R.styleable.DiagramView_axis_color, COLOR_DEFAULT_AXIS);
            curveColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_CURVE);
            secondaryCurveColor = a.getColor(R.styleable.DiagramView_secondary_curve_color, COLOR_DEFAULT_SECONDARY_CURVE);
            unitCircleColor = a.getColor(R.styleable.DiagramView_unit_circle_color, COLOR_DEFAULT_UNIT_CIRCLE);
            textColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_TEXT);
            textSize = a.getFloat(R.styleable.DiagramView_size_text, SIZE_DEFAULT_TEXT);
        } finally {
            a.recycle();
        }
        this.backgroundPaint.setColor(this.backgroundColor);
        this.linesPaint.setColor(linesColor);
        this.axisPaint.setColor(axisColor);
        this.curvePaint.setColor(curveColor);
        this.secondaryCurvePaint.setColor(secondaryCurveColor);
        this.unitCirclePaint.setColor(unitCircleColor);
        this.textPaint.setColor(textColor);
        this.textPaint.setTextSize(textSize);
    }

    public void draw(int astatism, double[] numeratorVector, double[] denominatorVector, double minFrequency, double maxFrequency){
        this.astatism = astatism;
        this.numeratorVector = numeratorVector;
        this.denominatorVector = denominatorVector;
        this.minFrequency = minFrequency - FREQUENCY_LOG_EXPANSION;
        this.maxFrequency = maxFrequency + FREQUENCY_LOG_EXPANSION;
        new Thread(new Runnable() {
            @Override
            public void run() {
                drawNyquist();
            }
        }).start();
    }

    private void drawNyquist(){
        this.min = new Complex64F(0, 0);
        this.max = new Complex64F(0, 0);
        long startTime = System.currentTimeMillis();
        long time = startTime;
        long end;
        double[] frequencies = getFrequencies();
        Complex64F[] values = new Complex64F[frequencies.length];
        Complex64F value;
        Complex64F numerator = null;
        Complex64F denominator = null;
        double temp;
        int a;

        Complex64F zero = new Complex64F(0, 0);
        if(astatism == 0){
            zero.real = this.numeratorVector[0] / this.denominatorVector[0];
            adjustBorders(zero);
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
        ResultActivity activity = (ResultActivity)getContext();
        activity.setZero(zero);

        for(int i = 0; i < frequencies.length; i++){
            numerator = calculatePolynomialValue(frequencies[i], this.numeratorVector, numerator);
            denominator = calculatePolynomialValue(frequencies[i], this.denominatorVector, denominator);
            temp = denominator.getMagnitude2();
            value = new Complex64F();
            if(temp == 0) {
                value.real = Double.POSITIVE_INFINITY;
            }
            else{
                value.real = numerator.real*denominator.real + numerator.imaginary*denominator.imaginary;
                value.real /= temp;
                value.imaginary = numerator.imaginary*denominator.real - numerator.real*denominator.imaginary;
                value.imaginary /= temp;

                temp = Math.pow(frequencies[i], this.astatism);
                value.imaginary *= temp;
                value.real *= temp;

                a = astatism % 4;
                if(a < 0) a += 4;
                for(; a > 0; a--){
                    temp = value.real;
                    value.real = -value.imaginary;
                    value.imaginary = temp;
                }
            }

            if(complex64FIsInfinite(value)){
                value = values[i-1];
            }

            values[i] =value;
        }

        temp = FREQUENCY_LOG_EXPANSION * FREQUENCY_DENSITY / 2;
        for(int i = (int)temp; i < frequencies.length - temp; i ++){
            adjustBorders(values[i]);
        }

        Complex64F infinite = new Complex64F(0, 0);
        a = this.astatism + this.numeratorVector.length - this.denominatorVector.length;
        if(a > 0){
            double first = this.numeratorVector[this.numeratorVector.length - 1];
            temp = 0;
            if(this.numeratorVector.length > 1)
                temp = this.numeratorVector[this.numeratorVector.length - 2];
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
                        infinite.real = this.numeratorVector[this.numeratorVector.length - 2]
                            / this.denominatorVector[this.denominatorVector.length - 1];
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
            infinite.real = this.numeratorVector[this.numeratorVector.length - 1]
                    / this.denominatorVector[this.denominatorVector.length - 1];
            adjustBorders(infinite);
        }
        activity.setInfinite(infinite);

        if(max.imaginary > -min.imaginary)
            min.imaginary = -max.imaginary;
        if(min.imaginary < -max.imaginary)
            max.imaginary = -min.imaginary;

        float width = (float)(max.real - min.real);
        max.real += width*0.05F;
        min.real -= width*0.05F;
        width *= 1.1F;
        float height = (float)(max.imaginary - min.imaginary);
        max.imaginary += height*0.05F;
        min.imaginary -= height*0.05F;
        height *= 1.1F;
        if(width == 0 && height == 0){
            max.real = max.imaginary = 1;
            min.real = min.imaginary = -1;
            width = height = 2;
        }
        else {
            if(width == 0){
                max.real = height/2;
                min.real = -height/2;
                width = height;
            }
            if(height == 0){
                max.imaginary = width/2;
                min.imaginary = -width/2;
                height = width;
            }
        }
        a = getContext().getResources().getDisplayMetrics().widthPixels;
        a -= PIXELS_LEFT_PADDING + PIXELS_RIGHT_PADDING;
        a -= 2 * getResources().getDimension(R.dimen.activity_horizontal_margin);
        if(width > height){
            pixelsPerUnit = a/height;
        }
        else{
            pixelsPerUnit = a/width;
        }
        end = System.currentTimeMillis();
        Log.d("Time", "Calculation time: " + Long.toString(end - time) + " milliseconds");
        time = end;
        View parent = (View)this.getParent();
        this.getLayoutParams().height = (int)getY(this.min) + (int)PIXELS_BOTTOM_PADDING;
        this.getLayoutParams().width = (int)getX(this.max) + (int)PIXELS_RIGHT_PADDING;
        parent.getLayoutParams().height = (int)getY(this.min) + (int)PIXELS_BOTTOM_PADDING;
        parent.getLayoutParams().width = (int)getX(this.max) + (int)PIXELS_RIGHT_PADDING;
        parent.requestLayout();
        SurfaceHolder sh = this.getHolder();
        Canvas canvas;
        Log.d("Meanwhile", "Waiting for canvas");
        try {
            Thread.sleep(20);
            while((canvas = sh.lockCanvas()) == null) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        end = System.currentTimeMillis();
        Log.d("Time", "Waited for canvas: " + Long.toString(end - time) + " milliseconds");
        canvas.drawColor(backgroundColor);
        canvas.drawCircle(getX(0), getY(0), pixelsPerUnit, unitCirclePaint);
        drawVerticals(canvas);
        drawHorizontals(canvas);
        drawAxis(canvas);
        drawCurve(canvas, zero, values, infinite);
        cover(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Total drawing time " + Long.toString(end - startTime) + " milliseconds");
        sh.unlockCanvasAndPost(canvas);
    }

    private double[] getFrequencies(){
        double step = 1/FREQUENCY_DENSITY;
        int total = (int)((maxFrequency - minFrequency)*FREQUENCY_DENSITY) + 1;
        double[] result = new double[total];
        double current = minFrequency;
        for(int i = 0 ; i < total; i++, current += step){
            result[i] = Math.pow(10, current);
        }
        return result;
    }

    private void drawHorizontals(Canvas canvas){
        double span = this.max.imaginary - this.min.imaginary;
        int order = (int)Math.floor(Math.log10(span));
        double base = 1;
        if(order > 0)
            for(int i = 0; i < order; i ++)
                base *= 10;
        else {
            order = -order;
            for (int i = 0; i < order; i++)
                base /= 10;
        }
        float step;
        if(span < 2 * base){
            step = 0.5F * (float)base;
        } else {
            step = (float)base;
        }
        float imaginary = (float)Math.ceil(this.min.imaginary/step)*step;

        float width = getX(this.max) - getX(this.min);
        float x;
        float y;
        float[] pts = new float[4*(int)(width/linesLength/2)];

        x = getX(this.min);
        for(int i = 0; 4*i < pts.length; i ++){
            pts[4*i] = x += linesLength;
            pts[4*i + 2] = x += linesLength;
        }

        for(; imaginary <= this.max.imaginary; imaginary += step) {
            y = getY(imaginary);
            canvas.drawText(Float.toString(imaginary).replaceAll("\\.?0*$", ""), getX(0) + 12, y + textPaint.getTextSize(), textPaint);
            for(int i = 0; 4*i < pts.length; i ++){
                pts[4*i + 1] = y;
                pts[4*i + 3] = y;
            }
            canvas.drawLines(pts, this.linesPaint);
        }
    }

    private void drawVerticals(Canvas canvas){
        double span = this.max.real - this.min.real;
        int order = (int)Math.floor(Math.log10(span));
        double base = 1;
        if(order > 0)
            for(int i = 0; i < order; i ++)
                base *= 10;
        else {
            order = -order;
            for (int i = 0; i < order; i++)
                base /= 10;
        }
        float step;
        if(span < 2 * base){
            step = 0.5F * (float)base;
        } else {
            step = (float)base;
        }
        float real = (float)Math.ceil(this.min.real/step)*step;

        float height = getY(this.min) - getY(this.max);
        float x;
        float y;
        float[] pts = new float[4*(int)(height/linesLength/2)];

        y = getY(this.max);
        for(int i = 0; 4*i < pts.length; i ++){
            pts[4*i + 1] = y += linesLength;
            pts[4*i + 3] = y += linesLength;
        }

        for(; real <= this.max.real; real += step){
            x = getX(real);
            canvas.drawText(Float.toString(real).replaceAll("\\.?0*$", ""), x + 12, getY(0) + textPaint.getTextSize(), textPaint);
            for(int i = 0; 4*i < pts.length; i ++){
                pts[4*i] = x;
                pts[4*i + 2] = x;
            }
            canvas.drawLines(pts, this.linesPaint);
        }
    }

    private void drawAxis(Canvas canvas) {
        canvas.drawLine(getX(this.min), getY(0), getX(this.max), getY(0), this.axisPaint);
        canvas.drawLine(getX(0), getY(this.max), getX(0), getY(this.min), this.axisPaint);
    }

    private void cover(Canvas canvas){
        canvas.drawRect(0, 0, PIXELS_LEFT_PADDING, getY(min), this.backgroundPaint);
        canvas.drawRect(0, 0, getX(max), PIXELS_TOP_PADDING, this.backgroundPaint);
        canvas.drawRect(0, getY(min), getX(max) + PIXELS_RIGHT_PADDING, getY(min) + PIXELS_BOTTOM_PADDING, this.backgroundPaint);
        canvas.drawRect(getX(max), 0, this.getLayoutParams().width, this.getLayoutParams().height, this.backgroundPaint);
    }

    private void drawCurve(Canvas canvas, Complex64F zero, Complex64F[] values, Complex64F infinite){
        int totalPoints = values.length - 1;
        if(!complex64FIsInfinite(zero))
            totalPoints ++;
        if(!complex64FIsInfinite(infinite))
            totalPoints ++;
        float[] points = new float[4 * totalPoints];

        int pointCounter = 0;
        if(!complex64FIsInfinite(zero)){
            points[0] = getX(zero);
            points[1] = getY(zero);
            points[2] = getX(values[0]);
            points[3] = getY(values[0]);
            pointCounter ++;
        }

        int valueCounter = 0;
        points[4*pointCounter + 0] = getX(values[valueCounter]);
        points[4*pointCounter + 1] = getY(values[valueCounter]);
        for(valueCounter ++; valueCounter < values.length - 1; pointCounter ++, valueCounter ++){
            points[4*pointCounter + 2] = points[4*pointCounter + 4] = getX(values[valueCounter]);
            points[4*pointCounter + 3] = points[4*pointCounter + 5] = getY(values[valueCounter]);
        }
        points[4*pointCounter + 2] = getX(values[valueCounter]);
        points[4*pointCounter + 3] = getY(values[valueCounter]);

        if(!complex64FIsInfinite(infinite)){
            pointCounter ++;
            points[4*pointCounter + 0] = getX(values[valueCounter]);
            points[4*pointCounter + 1] = getY(values[valueCounter]);
            points[4*pointCounter + 2] = getX(infinite);
            points[4*pointCounter + 3] = getY(infinite);
        }

        canvas.drawLines(points, this.curvePaint);

        float axis = getY(0);
        for(pointCounter = 0; pointCounter < totalPoints; pointCounter ++){
            points[4*pointCounter + 1] = 2 * axis - points[4*pointCounter + 1];
            points[4*pointCounter + 3] = 2 * axis - points[4*pointCounter + 3];
        }
        canvas.drawLines(points, this.secondaryCurvePaint);
    }

    private Complex64F calculatePolynomialValue(double frequency, double[] coefficients, Complex64F reusableComplex){
        int index = coefficients.length - 1;
        double resultReal = coefficients[index];
        double resultImaginary = 0;
        double tmp;
        for (index --; index >= 0; index --) {
            tmp = resultReal;
            resultReal = -resultImaginary*frequency + coefficients[index];
            resultImaginary = tmp * frequency;
        }
        if(reusableComplex == null){
            return new Complex64F(resultReal, resultImaginary);
        }
        reusableComplex.real = resultReal;
        reusableComplex.imaginary = resultImaginary;
        return reusableComplex;
    }

    private float getX(double real){
        return (float)(real - this.min.real) * pixelsPerUnit + PIXELS_LEFT_PADDING;
    }

    private float getY(double imaginary){
        return (float)(this.max.imaginary - imaginary) * pixelsPerUnit + PIXELS_TOP_PADDING;
    }

    private float getX(Complex64F value){
        return getX(value.real);
    }

    private float getY(Complex64F value){
        return getY(value.imaginary);
    }

    private static boolean complex64FIsInfinite(Complex64F complex64F){
        return Double.isInfinite(complex64F.real) || Double.isInfinite(complex64F.imaginary);
    }

    private void adjustBorders(Complex64F value){
        if (value.real > max.real)
            max.real = value.real;
        if (value.real < min.real)
            min.real = value.real;
        if (value.imaginary > max.imaginary)
            max.imaginary = value.imaginary;
        if (value.imaginary < min.imaginary)
            min.imaginary = value.imaginary;
    }
}
