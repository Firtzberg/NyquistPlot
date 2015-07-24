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
    private static final float PIXELS_PER_DECADE = 60 * DENSITY_COEFFICIENT;
    private static final float PIXELS_PER_DB = 2.5F * DENSITY_COEFFICIENT;
    private static final float PIXELS_PER_DEGREE = 50/90F * DENSITY_COEFFICIENT;
    private static final float PIXELS_LEFT_PADDING = 50 * DENSITY_COEFFICIENT;
    private static final float PIXELS_TOP_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_RIGHT_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_BOTTOM_PADDING = 20 * DENSITY_COEFFICIENT;
    private static final float PIXELS_BETWEEN_DIAGRAMS = 15 * DENSITY_COEFFICIENT;
    private static final double FREQUENCY_DENSITY = 20;
    private static final float linesLength = 5;
    private static final float AMPLITUDE_STEP_DB = 20;
    private static final float PHASE_STEP_DEGREES = 45;
    private static final int COLOR_DEFAULT_BACKGROUND = Color.WHITE;
    private static final int COLOR_DEFAULT_LINES = Color.LTGRAY;
    private static final int COLOR_DEFAULT_AXIS = Color.BLACK;
    private static final int COLOR_DEFAULT_CURVE = Color.BLUE;
    private static final int COLOR_DEFAULT_TEXT = Color.BLACK;
    private static final float SIZE_DEFAULT_TEXT = 12 * Resources.getSystem().getDisplayMetrics().scaledDensity;
    private static final float RELATIVE_CURVE_THICKNESS = 1.5F;
    double[] numeratorVector;
    double[] denominatorVector;
    int astatism;
    private double minFrequency;
    private double maxFrequency;
    private double minAmplitude;
    private double maxAmplitude;
    private double minPhase;
    private double maxPhase;
    private int backgroundColor;
    private final Paint linesPaint;
    private final Paint axisPaint;
    private final Paint curvePaint;
    private final Paint textPaint;
    private final Complex64F reusableComplex = new Complex64F();

    public DiagramView(Context context) {
        this(context, null);
    }

    public DiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.linesPaint = new Paint();
        this.linesPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.axisPaint = new Paint();
        this.axisPaint.setStrokeWidth(DENSITY_COEFFICIENT);
        this.curvePaint = new Paint();
        this.curvePaint.setStrokeWidth(RELATIVE_CURVE_THICKNESS * DENSITY_COEFFICIENT);
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
        int linesColor;
        int textColor;
        float textSize;
        try {
            this.backgroundColor = a.getColor(R.styleable.DiagramView_background_color, COLOR_DEFAULT_BACKGROUND);
            linesColor = a.getColor(R.styleable.DiagramView_lines_color, COLOR_DEFAULT_LINES);
            axisColor = a.getColor(R.styleable.DiagramView_axis_color, COLOR_DEFAULT_AXIS);
            curveColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_CURVE);
            textColor = a.getColor(R.styleable.DiagramView_curve_color, COLOR_DEFAULT_TEXT);
            textSize = a.getFloat(R.styleable.DiagramView_size_text, SIZE_DEFAULT_TEXT);
        } finally {
            a.recycle();
        }
        this.linesPaint.setColor(linesColor);
        this.axisPaint.setColor(axisColor);
        this.curvePaint.setColor(curveColor);
        this.textPaint.setColor(textColor);
        this.textPaint.setTextSize(textSize);
    }

    public void draw(int astatism, double[] numeratorVector, double[] denominatorVector, double minFrequency, double maxFrequency){
        this.astatism = astatism;
        this.numeratorVector = numeratorVector;
        this.denominatorVector = denominatorVector;
        this.minFrequency = minFrequency - 1;
        this.maxFrequency = maxFrequency + 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                drawBode();
            }
        }).start();
    }

    private void drawBode(){
        long startTime = System.currentTimeMillis();
        long time = startTime;
        long end;
        double[] frequencies = getFrequencies();
        double amplitude;
        double[] amplitudes = new double[frequencies.length];
        double phase;
        double[] phases = new double[frequencies.length];
        Complex64F value;
        double m;
        for(int i = 0; i < frequencies.length; i++){
            value = calculatePolynomialValue(frequencies[i], this.numeratorVector);
            amplitude = value.getMagnitude();
            phase = Math.atan2(value.imaginary, value.real);
            value = calculatePolynomialValue(frequencies[i], this.denominatorVector);
            m = value.getMagnitude();
            if(m == 0) {
                amplitude = Double.POSITIVE_INFINITY;
            }
            else{
                amplitude /= value.getMagnitude();
            }
            phase -= Math.atan2(value.imaginary, value.real);
            phase *= 180/Math.PI;
            amplitude *= Math.pow(frequencies[i], this.astatism);
            phase += this.astatism * 90;
            if(amplitude < 0) {
                phase += 180;
                amplitude = -amplitude;
            }
            phase -= Math.floor((phase + 270) / 360)*360;

            amplitude = 20*Math.log10(amplitude);
            if(Double.isInfinite(amplitude)){
                amplitude = amplitudes[i-1];
            }
            amplitudes[i] = amplitude;
            if(i  == 0) {
                minAmplitude = maxAmplitude = amplitude;
            }
            else {
                if (amplitude > maxAmplitude)
                    maxAmplitude = amplitude;
                if (amplitude < minAmplitude)
                    minAmplitude = amplitude;
            }

            phases[i] = phase;
            if(phase > maxPhase)
                maxPhase = phase;
            if(phase < minPhase)
                minPhase = phase;

            frequencies[i] = Math.log10(frequencies[i]);
        }
        this.maxAmplitude = Math.ceil(this.maxAmplitude / AMPLITUDE_STEP_DB) * AMPLITUDE_STEP_DB;
        this.minAmplitude = Math.floor(this.minAmplitude / AMPLITUDE_STEP_DB) * AMPLITUDE_STEP_DB;
        this.maxPhase = Math.ceil(this.maxPhase / PHASE_STEP_DEGREES) * PHASE_STEP_DEGREES;
        this.minPhase = Math.floor(this.minPhase / PHASE_STEP_DEGREES) * PHASE_STEP_DEGREES;
        end = System.currentTimeMillis();
        Log.d("Time", "Calculation time: " + Long.toString(end - time) + " milisekunds");
        time = end;
        View parent = (View)this.getParent();
        this.getLayoutParams().height = (int)getPhaseY(this.minPhase) + (int)PIXELS_BOTTOM_PADDING;
        this.getLayoutParams().width = (int)getX(this.maxFrequency) + (int)PIXELS_RIGHT_PADDING;
        parent.getLayoutParams().height = (int)getPhaseY(this.minPhase) + (int)PIXELS_BOTTOM_PADDING;
        parent.getLayoutParams().width = (int)getX(this.maxFrequency) + (int)PIXELS_RIGHT_PADDING;
        parent.requestLayout();
        SurfaceHolder sh = this.getHolder();
        Canvas canvas = null;
        Log.d("Meanwhile", "Waiting for canvas");
        try {
            Thread.sleep(20);
            while((canvas = sh.lockCanvas()) == null) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        Log.d("Time", "Waiten for canvas: " + Long.toString(end - time) + " milisekunds");
        time = end;
        canvas.drawColor(backgroundColor);
        drawAmplitudeVerticals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitudn verticals drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawAmplitudeHorizontals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitudne horizontals drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawAmplitudeAxis(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Amplitude axises drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawPhaseHorizontals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase horzontals drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawPhaseVerticals(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase verticals drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawPhaseAxis(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Phase axises drawn in " + Long.toString(end - time) + " milisekunds");
        time = end;
        drawCurves(canvas, frequencies, amplitudes, phases);
        end = System.currentTimeMillis();
        Log.d("Time", "Curves drawn in " + Long.toString(end - time) + " milisekunds");
        Log.d("Time", "Total drawing time " + Long.toString(end - startTime) + " milisekunds");
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

    private void drawAmplitudeHorizontals(Canvas canvas){
        float y;
        float x;
        float width = getX(this.maxFrequency) - getX(this.minFrequency);
        float[] pts = new float[4*(int)(width/linesLength/2)];
        float current = (int)Math.floor(this.minAmplitude/ AMPLITUDE_STEP_DB)* AMPLITUDE_STEP_DB;
        if(current < this.minAmplitude)
            current += AMPLITUDE_STEP_DB;
        while(current <= this.maxAmplitude) {
            y = getAmplitudeY(current);
            x = getX(this.minFrequency);
            canvas.drawText(Integer.toString((int)current) + "dB", 10, y + textPaint.getTextSize()/2, textPaint);
            for(int i = 0; 4*i < pts.length; i ++){
                pts[4*i] = x += linesLength;
                pts[4*i + 1] = y;
                pts[4*i + 2] = x += linesLength;
                pts[4*i + 3] = y;
            }
            canvas.drawLines(pts, linesPaint);
            current += AMPLITUDE_STEP_DB;
        }
    }

    private void drawPhaseHorizontals(Canvas canvas){
        float y;
        float x;
        float width = getX(this.maxFrequency) - getX(this.minFrequency);
        float[] pts = new float[4*(int)(width/linesLength/2)];
        float current = (int)Math.floor(this.minPhase/ PHASE_STEP_DEGREES)* PHASE_STEP_DEGREES;
        if(current < this.minPhase)
            current += PHASE_STEP_DEGREES;
        while(current <= this.maxPhase) {
            y = getPhaseY(current);
            x = getX(this.minFrequency);
            canvas.drawText(Integer.toString((int)current) + "°", 10, y + textPaint.getTextSize()/2, textPaint);
            for(int i = 0; 4*i < pts.length; i ++){
                pts[4*i] = x += linesLength;
                pts[4*i + 1] = y;
                pts[4*i + 2] = x += linesLength;
                pts[4*i + 3] = y;
            }
            canvas.drawLines(pts, linesPaint);
            current += PHASE_STEP_DEGREES;
        }
    }

    private void drawAmplitudeVerticals(Canvas canvas){
        double[] decimals = new double[]{0, Math.log10(2), Math.log10(3), Math.log10(4), Math.log10(5)};
        double start = Math.floor(this.minFrequency);
        double decadeStart;
        float x;
        float y;
        float height = getAmplitudeY(this.minAmplitude) - getAmplitudeY(this.maxAmplitude);
        float[] pts = new float[4*(int)(height/linesLength/2)];
        for(double decimal : decimals){
            decadeStart = start;
            if(decadeStart + decimal < this.minFrequency)
                decadeStart++;
            while(decadeStart + decimal <= this.maxFrequency) {
                x = getX(decadeStart + decimal);
                y = getAmplitudeY(this.maxAmplitude);
                if(decimal == 0){
                    canvas.drawText("10^" + (int)decadeStart, x - 12, getAmplitudeY(this.minAmplitude) + textPaint.getTextSize(), textPaint);
                }
                for(int i = 0; 4*i < pts.length; i ++){
                    pts[4*i] = x;
                    pts[4*i + 1] = y += linesLength;
                    pts[4*i + 2] = x;
                    pts[4*i + 3] = y += linesLength;
                }
                canvas.drawLines(pts, this.linesPaint);
                decadeStart++;
            }
        }
    }

    private void drawPhaseVerticals(Canvas canvas){
        double[] decimals = new double[]{0, Math.log10(2), Math.log10(3), Math.log10(4), Math.log10(5)};
        double start = Math.floor(this.minFrequency);
        double decadeStart;
        float x;
        float y;
        float height = getPhaseY(this.minPhase) - getPhaseY(this.maxPhase);
        float[] pts = new float[4*(int)(height/linesLength/2)];
        for(double decimal : decimals){
            decadeStart = start;
            if(decadeStart + decimal < this.minFrequency)
                decadeStart++;
            while(decadeStart + decimal <= this.maxFrequency) {
                x = getX(decadeStart + decimal);
                y = getPhaseY(this.maxPhase);
                if(decimal == 0){
                    canvas.drawText("10^" + (int)decadeStart, x - 12, getPhaseY(0) + textPaint.getTextSize(), textPaint);
                }
                for(int i = 0; 4*i < pts.length; i ++){
                    pts[4*i] = x;
                    pts[4*i + 1] = y += linesLength;
                    pts[4*i + 2] = x;
                    pts[4*i + 3] = y += linesLength;
                }
                canvas.drawLines(pts, this.linesPaint);
                decadeStart++;
            }
        }
    }

    private void drawAmplitudeAxis(Canvas canvas) {
        canvas.drawLine(getX(this.minFrequency), getAmplitudeY(this.minAmplitude), getX(this.maxFrequency), getAmplitudeY(this.minAmplitude), this.axisPaint);
        canvas.drawLine(getX(this.minFrequency), getAmplitudeY(this.maxAmplitude), getX(this.minFrequency), getAmplitudeY(this.minAmplitude), this.axisPaint);
    }

    private void drawPhaseAxis(Canvas canvas) {
        canvas.drawLine(getX(this.minFrequency), getPhaseY(0), getX(this.maxFrequency), getPhaseY(0), this.axisPaint);
        canvas.drawLine(getX(this.minFrequency), getPhaseY(this.maxPhase), getX(this.minFrequency), getPhaseY(this.minPhase), this.axisPaint);
    }

    private void drawCurves(Canvas canvas, double[] frequenciesLog10, double[] amplitudes, double[] phases){
        int length = frequenciesLog10.length - 1;
        float[] points = new float[4*length];
        int i;
        points[0] = getX(frequenciesLog10[0]);
        points[1] = getAmplitudeY(amplitudes[0]);
        for(i = 0; i < length - 1; i ++){
            points[4*i + 2] = points[4*i + 4] = getX(frequenciesLog10[i]);
            points[4*i + 3] = points[4*i + 5] = getAmplitudeY(amplitudes[i]);
        }
        points[4*i + 2] = getX(frequenciesLog10[i]);
        points[4*i + 3] = getAmplitudeY(amplitudes[i]);
        canvas.drawLines(points, this.curvePaint);

        points[1] = getPhaseY(phases[0]);
        for(i = 0; i < length - 1; i ++){
            points[4*i + 3] = points[4*i + 5] = getPhaseY(phases[i]);
        }
        points[4*i + 3] = getPhaseY(phases[i]);
        canvas.drawLines(points, this.curvePaint);
    }

    private Complex64F calculatePolynomialValue(double frequency, double[] coefficients){
        int index = coefficients.length - 1;
        double resultReal = coefficients[index];
        double resultImaginary = 0;
        double tmp;
        for (index --; index >= 0; index --) {
            tmp = resultReal;
            resultReal = -resultImaginary*frequency + coefficients[index];
            resultImaginary = tmp * frequency;
        }
        this.reusableComplex.real = resultReal;
        this.reusableComplex.imaginary = resultImaginary;
        return this.reusableComplex;
    }

    private float getX(double frequencyLog10){
        return (float)(frequencyLog10 - this.minFrequency)*PIXELS_PER_DECADE + PIXELS_LEFT_PADDING;
    }

    private float getAmplitudeY(double amplitude){
        return (float)(this.maxAmplitude - amplitude)*PIXELS_PER_DB + PIXELS_TOP_PADDING;
    }

    private float getPhaseY(double phase){
        return getAmplitudeY(this.minAmplitude) + (float)(this.maxPhase - phase)*PIXELS_PER_DEGREE + PIXELS_BETWEEN_DIAGRAMS;
    }
}
