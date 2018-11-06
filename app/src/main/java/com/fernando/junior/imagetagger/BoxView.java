package com.fernando.junior.imagetagger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import static android.content.ContentValues.TAG;
import static java.lang.Math.abs;

/**
 * Created by junior on 03/11/18.
 */

public class BoxView extends View {

    public class Corner{
        int radius = 40;
        int id;
        Point cornerPt;

        public Corner(Point cornerPt, int id){
            this.cornerPt = new Point(cornerPt.x, cornerPt.y);
            this.id = id;
        }
    }

    public class Rect {
        int id;
        int startX;
        int startY;
        int endX;
        int endY;
        int width;
        int height;
        float centerX;
        float centerY;
        ArrayList<Corner> corners = new ArrayList<>(4);

        public Rect(int startX, int startY, int endX, int endY, int id ){
            int tempVar;
            if(startX > endX) {
                tempVar = startX;
                startX = endX;
                endX = tempVar;
            }
            if(startY > endY) {
                tempVar = startY;
                startY = endY;
                endY = tempVar;
            }


            this.id      = id;
            this.startX  = startX;
            this.startY  = startY;
            this.endX    = endX;
            this.endY    = endY;
            this.width   = endX - startX;
            this.height  = endY - startY;
            this.centerX = startX + width/2.f;
            this.centerY = startY + height/2.f;
            this.corners.add(new Corner(new Point(startX, startY), 0));
            this.corners.add(new Corner(new Point(endX, startY), 1));
            this.corners.add(new Corner(new Point(startX, endY), 2));
            this.corners.add(new Corner(new Point(endX, endY), 3));
        }
        public ArrayList<Corner> GetCorners(){
            return this.corners;
        }
    }

    ArrayList<Rect> mRectCoords = new ArrayList<>();

    int mStartX;
    int mStartY;
    int mEndX;
    int mEndY;
    int mMinimalWidth=100;
    int mMinimalHeight=100;
    int mSelectedBox=-1;
    int mSelectedCorner=-1;
    Point mLastTouch = new Point();

    Paint mPaint = new Paint();
    Paint mPaintTemp = new Paint();
    Paint mPaintCorner = new Paint();
    Paint mPaintSelected = new Paint();
    public BoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaintTemp.setColor(Color.RED);
        mPaintTemp.setStrokeWidth(5);
        mPaintTemp.setStyle(Paint.Style.STROKE);

        mPaintCorner.setColor(Color.YELLOW);
        mPaintCorner.setStyle(Paint.Style.FILL);

        mPaintSelected.setColor(Color.RED);
        mPaintSelected.setStrokeWidth(5);
        mPaintSelected.setPathEffect(new DashPathEffect(new float[] { 5, 2, 2 }, 0));
        mPaintSelected.setStyle(Paint.Style.STROKE);
        setFocusable(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        Integer test = (this.mStartX - this.mEndX);
//        Log.d(TAG, "onDraw: " + test.toString());

        for(int idx=0; idx<mRectCoords.size(); idx++) {
            canvas.drawRect(mRectCoords.get(idx).startX,
                            mRectCoords.get(idx).startY,
                            mRectCoords.get(idx).endX,
                            mRectCoords.get(idx).endY,
                            mPaint);
        }

        if (abs(this.mStartX - this.mEndX) > mMinimalWidth && abs(this.mStartY - this.mEndY) > mMinimalHeight && mSelectedBox == -1) {
            canvas.drawRect(this.mStartX,
                    this.mStartY,
                    this.mEndX,
                    this.mEndY,
                    mPaintTemp);
        }


        if(mSelectedBox != -1){
            canvas.drawRect(mRectCoords.get(mSelectedBox).startX,
                    mRectCoords.get(mSelectedBox).startY,
                    mRectCoords.get(mSelectedBox).endX,
                    mRectCoords.get(mSelectedBox).endY,
                    mPaintSelected);
            ArrayList<Corner> corners = mRectCoords.get(mSelectedBox).GetCorners();
            for(int cornerIdx=0; cornerIdx<corners.size(); cornerIdx++){
                canvas.drawCircle(corners.get(cornerIdx).cornerPt.x, corners.get(cornerIdx).cornerPt.y, corners.get(cornerIdx).radius, mPaintCorner);
            }
        }

    }

    public int getSelectedCorner(Rect selectedRect, Point touchedPt){
        ArrayList<Corner> corners = selectedRect.GetCorners();
        double dist=-1;
        int cornerId=-1;
        for(int idx=0; idx<corners.size(); idx++){
            dist = Math.sqrt(Math.pow(touchedPt.x - corners.get(idx).cornerPt.x, 2) +
                    Math.pow(touchedPt.y - corners.get(idx).cornerPt.y, 2));

            if(dist<corners.get(idx).radius){
                cornerId = idx;
            }
        }
//        Log.d(TAG, "onTouchEvent: corner: " + Integer.toString(cornerId));

        return cornerId;
    }

    public int getSelectedBox(){
        double d=-1;
        double dist=-1;
        int boxId=-1;
        // Look for the shortest distance between box center and touched pos
        for(int idx=0; idx<mRectCoords.size(); idx++){
            dist = Math.sqrt(Math.pow(mRectCoords.get(idx).centerX - mEndX, 2) +
                   Math.pow(mRectCoords.get(idx).centerY - mEndY, 2));

            // start var
            if(d==-1) {
                d = dist;
                boxId = idx;
            }

            if(dist<d){
                d = dist;
                boxId = idx;
            }
        }

        // Check if touched pos is out of the box
        if(boxId != -1){
            if(mEndX > mRectCoords.get(boxId).endX || mEndY > mRectCoords.get(boxId).endY ||
                    mEndX < mRectCoords.get(boxId).startX || mEndY < mRectCoords.get(boxId).startY){
                boxId = -1;
            }
        }
        return boxId;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.mStartX = (int) event.getX();
                this.mStartY = (int) event.getY();

                this.mLastTouch.x = (int) event.getX();
                this.mLastTouch.y = (int) event.getY();
                // User has already selected a box and may want to change its size
                if(mSelectedBox != -1){
                    mSelectedCorner = getSelectedCorner(mRectCoords.get(mSelectedBox), new Point(this.mStartX, this.mStartY));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                this.mEndX = (int) event.getX();
                this.mEndY = (int) event.getY();

                if(mSelectedBox != -1){
                    int xDelta = (int) event.getX() - mLastTouch.x;
                    int yDelta = (int) event.getY() - mLastTouch.y;

                    mRectCoords.get(mSelectedBox).startX+= xDelta;
                    mRectCoords.get(mSelectedBox).endX+= xDelta;
                    mRectCoords.get(mSelectedBox).startY+= yDelta;
                    mRectCoords.get(mSelectedBox).endY+= yDelta;

                    mRectCoords.get(mSelectedBox).centerX = mRectCoords.get(mSelectedBox).centerX += xDelta;
                    mRectCoords.get(mSelectedBox).centerY = mRectCoords.get(mSelectedBox).centerY += yDelta;
//
                    mRectCoords.get(mSelectedBox).corners.get(0).cornerPt.x+=xDelta;
                    mRectCoords.get(mSelectedBox).corners.get(1).cornerPt.x+=xDelta;
                    mRectCoords.get(mSelectedBox).corners.get(2).cornerPt.x+=xDelta;
                    mRectCoords.get(mSelectedBox).corners.get(3).cornerPt.x+=xDelta;

                    mRectCoords.get(mSelectedBox).corners.get(0).cornerPt.y+=yDelta;
                    mRectCoords.get(mSelectedBox).corners.get(1).cornerPt.y+=yDelta;
                    mRectCoords.get(mSelectedBox).corners.get(2).cornerPt.y+=yDelta;
                    mRectCoords.get(mSelectedBox).corners.get(3).cornerPt.y+=yDelta;

                    mLastTouch.x = (int) event.getX();
                    mLastTouch.y = (int) event.getY();
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                this.mEndX = (int) event.getX();
                this.mEndY = (int) event.getY();

                if(mSelectedBox == -1) {
                    // If the rectangle is big enough, register as a new box
                    if (abs(this.mStartX - this.mEndX) > mMinimalWidth && abs(this.mStartY - this.mEndY) > mMinimalHeight) {
                        mRectCoords.add(new Rect(this.mStartX, this.mStartY, this.mEndX, this.mEndY, mRectCoords.size() + 1));
                        invalidate();
                        break;
                    }
                }

                // If the square is too small, user may be selecting a box
                mSelectedBox = getSelectedBox();

                invalidate();
                break;

            default:
                super.onTouchEvent(event);
                break;
        }
        return true;

    }
}

