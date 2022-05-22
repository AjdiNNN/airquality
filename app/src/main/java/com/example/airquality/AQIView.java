package com.example.airquality;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class AQIView extends View
{
    private int aqi;
    Point center;
    RectF outer_rect;
    RectF inner_rect;
    Path path;
    Paint fill;
    Path transparentPath;
    Paint transparentFill;
    int inner_radius = 180;
    int outer_radius = 220;
    int arc_sweep;
    int arc_ofset = 120;
    int centerW;
    int centerH;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerW = w/2;
        centerH = h/2;
        center = new Point(centerW,centerH);

        outer_rect = new RectF(center.x-outer_radius, center.y-outer_radius, center.x+outer_radius, center.y+outer_radius);
        inner_rect = new RectF(center.x-inner_radius, center.y-inner_radius, center.x+inner_radius, center.y+inner_radius);
        super.onSizeChanged(w, h, oldw, oldh);
    }
    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.AQIView, defStyle, 0);

        aqi = a.getInt(
                R.styleable.AQIView_aqi,0);
        a.recycle();

        path = new Path();
        fill = new Paint();
        //invalidate();

        transparentPath = new Path();
        transparentFill = new Paint();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        fill.setColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));
        arc_sweep = Math.min(aqi, 300);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        transparentFill.setAlpha(75);
        transparentPath.arcTo(outer_rect, arc_ofset, 300);
        transparentPath.arcTo(inner_rect, arc_ofset + 300, -300);
        transparentPath.close();

        canvas.drawPath(transparentPath, transparentFill);


        path.arcTo(outer_rect, arc_ofset, arc_sweep);
        path.arcTo(inner_rect, arc_ofset + arc_sweep, -arc_sweep);
        path.close();

        canvas.drawPath(path, fill);

    }
    public AQIView(Context context) {
        super(context);
        init(null, 0);
    }

    public AQIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AQIView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    public void setAqi(int aqi) {
        this.aqi = 70;
        invalidate();
    }
}