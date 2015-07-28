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
    private Complex64F zero;
    private Complex64F[] values;
    private Complex64F infinite;

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

    public void setPoints(Complex64F zero, Complex64F[] values, Complex64F infinite){
        this.min = new Complex64F(0, 0);
        this.max = new Complex64F(0, 0);

        this.zero = zero;
        this.values = values;
        this.infinite = infinite;

        if(!ResultActivity.complex64FIsInfinite(this.zero)){
            adjustBorders(this.zero);
        }
        double temp = FREQUENCY_LOG_EXPANSION * FREQUENCY_DENSITY / 2;
        for(int i = (int)temp; i < values.length - temp; i ++){
            adjustBorders(values[i]);
        }
        if(!ResultActivity.complex64FIsInfinite(this.infinite)){
            adjustBorders(this.infinite);
        }

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
        int a = getContext().getResources().getDisplayMetrics().widthPixels;
        a -= PIXELS_LEFT_PADDING + PIXELS_RIGHT_PADDING;
        a -= 2 * getResources().getDimension(R.dimen.activity_horizontal_margin);
        if(width > height){
            pixelsPerUnit = a/height;
        }
        else{
            pixelsPerUnit = a/width;
        }
    }

    public void redraw(){
        if(values == null){
            return;
        }
        long startTime = System.currentTimeMillis();
        long time = startTime;
        long end;

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
        drawCurve(canvas, this.zero, this.values, this.infinite);
        cover(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Total drawing time " + Long.toString(end - startTime) + " milliseconds");
        sh.unlockCanvasAndPost(canvas);
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
        float value;
        float[] pts = new float[4*(int)(width/linesLength/2)];

        value = getX(this.min);
        for(int i = 0; i < pts.length; i += 2){
            pts[i] = value += linesLength;
        }

        for(; imaginary <= this.max.imaginary; imaginary += step) {
            value = getY(imaginary);
            canvas.drawText(Float.toString(imaginary).replaceAll("\\.?0*$", ""), getX(0) + 12, value + textPaint.getTextSize(), textPaint);
            for(int i = 1; i < pts.length; i += 2){
                pts[i] = value;
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
        float value;
        float[] pts = new float[4*(int)(height/linesLength/2)];

        value = getY(this.max);
        for(int i = 1; i < pts.length; i += 2){
            pts[i] = value += linesLength;
        }

        for(; real <= this.max.real; real += step){
            value = getX(real);
            canvas.drawText(Float.toString(real).replaceAll("\\.?0*$", ""), value + 12, getY(0) + textPaint.getTextSize(), textPaint);
            for(int i = 0; i < pts.length; i += 2){
                pts[i] = value;
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
        if(!ResultActivity.complex64FIsInfinite(zero))
            totalPoints ++;
        if(!ResultActivity.complex64FIsInfinite(infinite))
            totalPoints ++;
        float[] points = new float[4 * totalPoints];

        int pointCounter = 0;
        if(!ResultActivity.complex64FIsInfinite(zero)){
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

        if(!ResultActivity.complex64FIsInfinite(infinite)){
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
