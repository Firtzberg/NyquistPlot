package com.hrca.nyquist.nyquistplot;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.ejml.data.Complex64F;


/**
 * TODO: document your custom view class.
 */
public class DiagramView extends SurfaceView implements SurfaceHolder.Callback {
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
    private static final int SIZE_DEFAULT_TEXT = (int)(12 * Resources.getSystem().getDisplayMetrics().scaledDensity);
    private static final float RELATIVE_CURVE_THICKNESS = 1.5F;
    private static final double FREQUENCY_DENSITY = 20;
    private static final double FREQUENCY_LOG_EXPANSION = 1.0;
    private static final float ADDITIONAL_SPACE_RATIO = 0.05F;
    public static final String PARCELABLE_MAX_KEY = "max";
    public static final String PARCELABLE_MIN_KEY = "min";
    public static final String PARCELABLE_REAL_KEY = "real";
    public static final String PARCELABLE_IMAGINARY_KEY = "imaginary";
    public static final String PARCELABLE_PIXEL_PER_UNIT_KEY = "ppu";
    private float pixelsPerUnit;
    private Complex64F min;
    private Complex64F max;
    private Complex64F defaultMax;
    private Complex64F defaultMin;
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
    private ScaleGestureDetector scaleDetector;
    private GestureDetector scrollDetector;

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
        this.scaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
        this.scrollDetector = new GestureDetector(context, new MyScrollListener());

        //subscribe to surface callbacks
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

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
            textSize = a.getDimensionPixelSize(R.styleable.DiagramView_size_text, SIZE_DEFAULT_TEXT);
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
        this.max = null;
        this.min = null;
        this.defaultMin = new Complex64F(-1, -1);
        this.defaultMax = new Complex64F(1, 1);

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

        if(defaultMax.imaginary > -defaultMin.imaginary)
            defaultMin.imaginary = -defaultMax.imaginary;
        if(defaultMin.imaginary < -defaultMax.imaginary)
            defaultMax.imaginary = -defaultMin.imaginary;

        // give a bit space
        double space = ADDITIONAL_SPACE_RATIO*(defaultMax.real - defaultMin.real);
        defaultMax.real += space;
        defaultMin.real -= space;
        space = ADDITIONAL_SPACE_RATIO*(defaultMax.imaginary - defaultMin.imaginary);
        defaultMax.imaginary += space;
        defaultMin.imaginary -= space;

        Log.d("defaultMax", this.defaultMax.toString());
        Log.d("defaultMin", this.defaultMin.toString());
        invalidate();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        // enable invocations of onDraw after invalidation.
        setWillNotDraw(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
        //remove padding
        width -= PIXELS_LEFT_PADDING + PIXELS_RIGHT_PADDING;
        height -= PIXELS_TOP_PADDING + PIXELS_BOTTOM_PADDING;
        if(width <= 0 || height <= 0){
            // TODO: handle
        }

        // go to default frame location if previous frame location is not available
        if(this.min == null || this.max == null){
            this.max = new Complex64F(this.defaultMax.real,
                    this.defaultMax.imaginary);
            this.min = new Complex64F(this.defaultMin.real,
                    this.defaultMin.imaginary);

            // use the smaller value of pixelsPerUnit to show whole graph
            float defaultWidthInUnits = (float)(this.max.real - this.min.real);
            float defaultHeightInUnits = (float)(this.max.imaginary - this.min.imaginary);
            float ratioX = width / defaultWidthInUnits;
            float ratioY = height / defaultHeightInUnits;
            if (ratioX < ratioY){
                pixelsPerUnit = ratioX;
            }
            else {
                pixelsPerUnit = ratioY;
            }
        }

        // Keep center centered and pixelsPerUnit. Since size changed the min and max have to be stretched.
        float stretchFactor;
        Complex64F focus = new Complex64F((this.max.real + this.min.real) / 2,
                (this.max.imaginary + this.min.imaginary) / 2);
        float widthInUnits = (float)(this.max.real - this.min.real);
        float heightInUnits = (float)(this.max.imaginary - this.min.imaginary);
        float ratioX = width / widthInUnits;
        float ratioY = height / heightInUnits;

        //stretch real axis
        stretchFactor = ratioX/pixelsPerUnit;
        //Log.d("Stretch X", String.valueOf(stretchFactor));
        this.max.real = focus.real +
                stretchFactor * (this.max.real - focus.real);
        this.min.real = focus.real +
                stretchFactor * (this.min.real - focus.real);

        // stretch imaginary axis
        stretchFactor = ratioY/pixelsPerUnit;
        //Log.d("Stretch Y", String.valueOf(stretchFactor));
        this.max.imaginary = focus.imaginary +
                stretchFactor * (this.max.imaginary - focus.imaginary);
        this.min.imaginary = focus.imaginary +
                stretchFactor * (this.min.imaginary - focus.imaginary);
        invalidate();
    }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder){

    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(values == null){
            return;
        }
        long startTime = System.currentTimeMillis();
        long end;
        Log.d("Max", this.max.toString());
        Log.d("Min", this.min.toString());
        canvas.drawColor(backgroundColor);
        canvas.drawCircle(getX(0), getY(0), pixelsPerUnit, unitCirclePaint);
        drawVerticals(canvas);
        drawHorizontals(canvas);
        drawAxis(canvas);
        drawCurve(canvas, this.zero, this.values, this.infinite);
        cover(canvas);
        end = System.currentTimeMillis();
        Log.d("Time", "Total drawing time " + Long.toString(end - startTime) + " milliseconds");
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
        float imaginary = (float)Math.ceil(this.min.imaginary / step) * step;

        float width = getX(this.max) - getX(this.min);
        float value;
        float[] pts = new float[4*(int)(width / linesLength/2)];

        value = getX(this.min);
        for(int i = 0; i < pts.length; i += 2){
            pts[i] = value += linesLength;
        }

        double position = 0;
        float pixelPosition;
        if(this.max.real < position)
            position = this.max.real;
        if(this.min.real > position)
            position = this.min.real;

        pixelPosition = getX(position);
        // put the text left of the axis line if there is not enough place right.
        if(pixelPosition + textPaint.getTextSize()*(order + 1) > getX(this.max))
            textPaint.setTextAlign(Paint.Align.RIGHT);
        else
            textPaint.setTextAlign(Paint.Align.LEFT);

        for(; imaginary <= this.max.imaginary; imaginary += step) {
            value = getY(imaginary);
            canvas.drawText(Float.toString(imaginary).replaceAll("\\.?0*$", ""), pixelPosition, value, textPaint);
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

        double imaginaryPosition = 0;
        float pixelPosition;
        if(this.min.imaginary > imaginaryPosition){
            imaginaryPosition = this.min.imaginary;
        }
        else {
            if (this.max.imaginary < imaginaryPosition)
                imaginaryPosition = this.max.imaginary;
        }
        pixelPosition = getY(imaginaryPosition);
        // put the text above the axis line if there is not enough place below.
        if(pixelPosition - textPaint.getTextSize() < PIXELS_TOP_PADDING)
            pixelPosition += textPaint.getTextSize();
        textPaint.setTextAlign(Paint.Align.LEFT);

        for(; real <= this.max.real; real += step){
            value = getX(real);
            canvas.drawText(Float.toString(real).replaceAll("\\.?0*$", ""), value, pixelPosition, textPaint);
            for(int i = 0; i < pts.length; i += 2){
                pts[i] = value;
            }
            canvas.drawLines(pts, this.linesPaint);
        }
    }

    private void drawAxis(Canvas canvas) {
        double position = 0;
        if(this.max.imaginary < position)
            position = this.max.imaginary;
        if(this.min.imaginary > position)
            position = this.min.imaginary;
        canvas.drawLine(getX(this.min), getY(position),
                getX(this.max), getY(position),
                this.axisPaint);
        position = 0;
        if(this.max.real < position)
            position = this.max.real;
        if(this.min.real > position)
            position = this.min.real;
        canvas.drawLine(getX(position), getY(this.max),
                getX(position), getY(this.min),
                this.axisPaint);
    }

    private void cover(Canvas canvas){
        canvas.drawRect(0, 0,
                PIXELS_LEFT_PADDING, getHeight(),
                this.backgroundPaint);
        canvas.drawRect(0, 0,
                getWidth(), PIXELS_TOP_PADDING,
                this.backgroundPaint);
        canvas.drawRect(0, getY(min),
                getWidth(), getHeight(),
                this.backgroundPaint);
        canvas.drawRect(getX(max), 0,
                getWidth(), getHeight(),
                this.backgroundPaint);
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
        if (value.real > defaultMax.real)
            defaultMax.real = value.real;
        if (value.real < defaultMin.real)
            defaultMin.real = value.real;
        if (value.imaginary > defaultMax.imaginary)
            defaultMax.imaginary = value.imaginary;
        if (value.imaginary < defaultMin.imaginary)
            defaultMin.imaginary = value.imaginary;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // prevent parent groupView from scrolling
        this.getParent().requestDisallowInterceptTouchEvent(true);

        // first check for scaling
        this.scaleDetector.onTouchEvent(event);

        // if scaling is in progress (event is consumed) do not do other things.
        if(this.scaleDetector.isInProgress())
            return true;

        // if there is no scaling handle scrolling
        this.scrollDetector.onTouchEvent(event);

        // always consume the event
        return true;
    }

    public void move(Complex64F offset){
        this.max.real = this.max.real + offset.real;
        this.max.imaginary = this.max.imaginary + offset.imaginary;
        this.min.real = this.min.real + offset.real;
        this.min.imaginary = this.min.imaginary + offset.imaginary;
        invalidate();
    }

    public void zoom(double zoomFactor, Complex64F focus){
        if(zoomFactor <= 0)
            return;
        this.max.real = focus.real + zoomFactor * (this.max.real - focus.real);
        this.max.imaginary = focus.imaginary + zoomFactor * (this.max.imaginary - focus.imaginary);
        this.min.real = focus.real + zoomFactor * (this.min.real - focus.real);
        this.min.imaginary = focus.imaginary + zoomFactor * (this.min.imaginary - focus.imaginary);
        this.pixelsPerUnit /= zoomFactor;
        invalidate();
    }

    public void reset(){
        boolean isOverview = false;
        if((float)this.max.real == (float)this.defaultMax.real &&
                (float)this.min.real == (float)this.defaultMin.real)
            isOverview = true;
        if((float)this.max.imaginary == (float)this.defaultMax.imaginary &&
                (float)this.min.imaginary == (float)this.defaultMin.imaginary)
            isOverview = true;

        // forget previous frame location
        this.max = null;
        this.min = null;

        if (!isOverview){
            // recalculate frame location
            surfaceChanged(this.getHolder(), 0, this.getWidth(), this.getHeight());
        }
        else{
            // zoom to unit circle
            Complex64F maxHolder = this.defaultMax;
            Complex64F minHolder = this.defaultMin;
            this.defaultMax = new Complex64F(1+ADDITIONAL_SPACE_RATIO,1+ADDITIONAL_SPACE_RATIO);
            this.defaultMin = new Complex64F(-1-ADDITIONAL_SPACE_RATIO,-1-ADDITIONAL_SPACE_RATIO);
            // recalculate frame location
            surfaceChanged(this.getHolder(), 0, this.getWidth(), this.getHeight());
            this.defaultMax = maxHolder;
            this.defaultMin = minHolder;
        }
    }

    private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Don't scale if the change is less than 2%
            if(Math.abs(detector.getScaleFactor() - 1) < 0.02)
                return false;

            // Calculate focus point in pixels without padding
            double focusX = detector.getFocusX() - PIXELS_LEFT_PADDING;
            double focusY = detector.getFocusY() - PIXELS_TOP_PADDING;
            // Calculate focus point relative to size of displayed diagram
            focusX /= getWidth() - PIXELS_RIGHT_PADDING - PIXELS_LEFT_PADDING;
            focusY /= getHeight() - PIXELS_TOP_PADDING - PIXELS_BOTTOM_PADDING;
            // Calculate focus point in displayed units
            focusX = DiagramView.this.min.real +
                    focusX * (DiagramView.this.max.real - DiagramView.this.min.real);
            focusY = DiagramView.this.min.imaginary +
                    focusY * (DiagramView.this.max.imaginary - DiagramView.this.min.imaginary);

            DiagramView.this.zoom(1/detector.getScaleFactor(), new Complex64F(focusX, focusY));
            return true;
        }
    }

    private class MyScrollListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e){
            DiagramView.this.reset();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY){
            // Calculate offset relative to size of displayed diagram
            double offsetX = distanceX / (getWidth() - PIXELS_RIGHT_PADDING - PIXELS_LEFT_PADDING);
            double offsetY = - distanceY / (getHeight() - PIXELS_TOP_PADDING - PIXELS_BOTTOM_PADDING);

            // Don't scale if the change is less than 2%
//            if((offsetX*offsetX + offsetY*offsetY) < 0.02*0.02)
//                return false;

            // Calculate offset in displayed units
            offsetX = offsetX * (DiagramView.this.max.real - DiagramView.this.min.real);
            offsetY = offsetY * (DiagramView.this.max.imaginary - DiagramView.this.min.imaginary);

            DiagramView.this.move(new Complex64F(offsetX, offsetY));
            return true;
        }
    }

    private Parcelable saveComplex64F(Complex64F complex){
        Bundle bundle = new Bundle();
        bundle.putDouble(PARCELABLE_REAL_KEY, complex.real);
        bundle.putDouble(PARCELABLE_IMAGINARY_KEY, complex.imaginary);
        return bundle;
    }

    private Complex64F restoreComplex64F(Parcelable state){
        Complex64F complex = null;
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            complex = new Complex64F(bundle.getDouble(PARCELABLE_REAL_KEY),
                    bundle.getDouble(PARCELABLE_IMAGINARY_KEY));
        }
        return complex;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putFloat(PARCELABLE_PIXEL_PER_UNIT_KEY, this.pixelsPerUnit);
        bundle.putParcelable(PARCELABLE_MAX_KEY, saveComplex64F(this.max));
        bundle.putParcelable(PARCELABLE_MIN_KEY, saveComplex64F(this.min));
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.pixelsPerUnit = bundle.getFloat(PARCELABLE_PIXEL_PER_UNIT_KEY);
            this.max = restoreComplex64F(bundle.getParcelable(PARCELABLE_MAX_KEY));
            this.min = restoreComplex64F(bundle.getParcelable(PARCELABLE_MIN_KEY));
            state = bundle.getParcelable("instanceState");
        }
        if(state != null)
            super.onRestoreInstanceState(state);
    }
}
