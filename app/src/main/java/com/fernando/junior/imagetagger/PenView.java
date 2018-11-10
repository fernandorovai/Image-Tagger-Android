package com.fernando.junior.imagetagger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class PenView extends View {
    Paint mPaintPoint;
    Paint mPaintPath;
    ArrayList<Point> mPointTrack = new ArrayList<>();
    ArrayList<Path> mPaths = new ArrayList<>();

    public PenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPaintPath = new Paint();
        this.mPaintPath.setARGB(128, 255, 0, 0);
        this.mPaintPath.setStyle(Paint.Style.FILL);

        this.mPaintPoint = new Paint();
        this.mPaintPoint.setColor(Color.BLUE);
        this.mPaintPoint.setStrokeWidth(6);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(Point point : mPointTrack){
            canvas.drawPoint(point.x, point.y, mPaintPoint);
        }

        for(Path path : mPaths){
            canvas.drawPath(path, mPaintPath);
        }
    }

    Path pointsToPath(ArrayList<Point> points){
        Path path = new Path();
        for (int idx = 0; idx < points.size(); idx++) {
            Point pt = points.get(idx);
            if (idx == 0) {
                path.moveTo(pt.x, pt.y);
            } else {
                path.lineTo(pt.x, pt.y);
            }
        }
        return path;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        Log.d(TAG, "onTouchEvent: Points" + mPointTrack.toString());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPointTrack.clear();
                mPointTrack.add(new Point((int) event.getX(), (int) event.getY()));
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mPointTrack.add(new Point((int) event.getX(), (int) event.getY()));
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                this.mPaths.add(this.pointsToPath(new ArrayList<>(mPointTrack)));
                mPointTrack.clear();
                break;
        }
        return true;
    }
}
