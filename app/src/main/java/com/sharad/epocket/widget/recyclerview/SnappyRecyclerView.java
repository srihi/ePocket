package com.sharad.epocket.widget.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;

/**
 * Created by Sharad on 08-Jun-16.
 */
public final class SnappyRecyclerView extends RecyclerView {
    private boolean mSnapEnabled = false;
    private boolean mUserScrolling = false;
    private boolean mScrolling = false;
    private boolean mShowIndicator = false;
    private int mScrollState;
    private long lastScrollTime = 0;
    private Handler mHandler = new Handler();

    private Paint mPaintActive;
    private Paint mPaintInactive;

    private boolean mScaleUnfocusedViews = false;

    private final static int MINIMUM_SCROLL_EVENT_OFFSET_MS = 20;

    public SnappyRecyclerView(Context context) {
        super(context);
        init();
    }

    public SnappyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaintActive = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintActive.setStyle(Paint.Style.FILL);
        mPaintActive.setColor(Color.WHITE);

        mPaintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintInactive.setStyle(Paint.Style.FILL);
        mPaintInactive.setColor(Color.WHITE);
        mPaintInactive.setAlpha(60);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(getAdapter().getItemCount() > 0) {
            for (int i = 0; i < getAdapter().getItemCount(); i++) {
                Point p = getIndicatorCenter(i);
                canvas.drawCircle(p.x, p.y, 6, (i == getChildAdapterPosition(getCenterView())) ? mPaintActive : mPaintInactive);
            }
        }
    }

    public Point getIndicatorCenter(int index) {
        Point point = new Point();
        int indicatorWidth = 24;
        int width = (getAdapter().getItemCount()-1) * indicatorWidth;
        int posX = (getWidth() - width) / 2;
        point.set(posX + index*indicatorWidth, (int)(getBottom()*0.9));
        return point;
    }

    /**
     * Enable snapping behaviour for this recyclerView
     * @param enabled enable or disable the snapping behaviour
     */
    public void setSnapEnabled(boolean enabled) {
        mSnapEnabled = enabled;

        if (enabled) {
            addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left == oldLeft && right == oldRight && top == oldTop && bottom == oldBottom) {
                        removeOnLayoutChangeListener(this);
                        updateViews();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollToView(getChildAt(0));
                            }
                        }, 20);
                    }
                }
            });

            setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    updateViews();
                    super.onScrolled(recyclerView, dx, dy);
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    /** if scroll is caused by a touch (scroll touch, not any touch) **/
                    if (newState == SCROLL_STATE_TOUCH_SCROLL) {
                        /** if scroll was initiated already, this is not a user scrolling, but probably a tap, else set userScrolling **/
                        if (!mScrolling) {
                            mUserScrolling = true;
                        }
                    } else if (newState == SCROLL_STATE_IDLE) {
                        if (mUserScrolling) {
                            scrollToView(getCenterView());
                        }

                        mUserScrolling = false;
                        mScrolling = false;
                    } else if (newState == SCROLL_STATE_FLING) {
                        mScrolling = true;
                    }

                    mScrollState = newState;
                }
            });
        } else {
            setOnScrollListener(null);
        }
    }

    /**
     * Enable snapping behaviour for this recyclerView
     * @param enabled enable or disable the snapping behaviour
     * @param scaleUnfocusedViews downScale the views which are not focused based on how far away they are from the center
     */
    public void setSnapEnabled(boolean enabled, boolean scaleUnfocusedViews) {
        this.mScaleUnfocusedViews = scaleUnfocusedViews;
        setSnapEnabled(enabled);
    }

    /**
     * Display page indicator at bottom of recyclerView
     * @param show show hide page indicator
     */
    public void showIndicator(boolean show) {
        mShowIndicator = show;
        updateViews();
    }

    private void updateViews() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            setMarginsForChild(child);

            if (mScaleUnfocusedViews) {
                float percentage = getPercentageFromCenter(child);
                float scale = 1f - (0.2f * percentage);

                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        }
    }

    /**
     *  Adds the margins to a childView so a view will still center even if it's only a single child
     * @param child childView to set margins for
     */
    private void setMarginsForChild(View child) {
        int lastItemIndex = getLayoutManager().getItemCount() - 1;
        int childIndex = getChildPosition(child);

        int startMargin = childIndex == 0 ? getMeasuredWidth() / 10 : 0;
        int endMargin = childIndex == lastItemIndex ? getMeasuredWidth() / 10 : 0;
        int topMargin = mShowIndicator ? (int)(getMeasuredHeight() * 0.1) : 0;
        int bottomMargin = mShowIndicator ? (int)(getMeasuredHeight() * 0.2) : 0;

        /** if sdk minimum level is 17, set RTL margins **/
        if (Build.VERSION.SDK_INT >= 17) {
            ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).setMarginStart(startMargin);
            ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).setMarginEnd(endMargin);
        }

        ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).setMargins(startMargin, topMargin, endMargin, bottomMargin);
        child.requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mSnapEnabled)
            return super.dispatchTouchEvent(event);

        long currentTime = System.currentTimeMillis();

        /** if touch events are being spammed, this is due to user scrolling right after a tap,
         * so set userScrolling to true **/
        if (mScrolling && mScrollState == SCROLL_STATE_TOUCH_SCROLL) {
            if ((currentTime - lastScrollTime) < MINIMUM_SCROLL_EVENT_OFFSET_MS) {
                mUserScrolling = true;
            }
        }

        lastScrollTime = currentTime;

        View targetView = getChildClosestToPosition((int)event.getX());

        if (!mUserScrolling) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (targetView != getCenterView()) {
                    scrollToView(targetView);
                    return true;
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (!mSnapEnabled)
            return super.onInterceptTouchEvent(e);

        View targetView = getChildClosestToPosition((int) e.getX());

        if (targetView != getCenterView()) {
            return true;
        }

        return super.onInterceptTouchEvent(e);
    }

    private View getChildClosestToPosition(int x) {
        if (getChildCount() <= 0)
            return null;

        int itemWidth = getChildAt(0).getMeasuredWidth();

        int closestX = 9999;
        View closestChild = null;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            int childCenterX = ((int)child.getX() + (itemWidth / 2));
            int xDistance = childCenterX - x;

            /** if child center is closer than previous closest, set it as closest  **/
            if (Math.abs(xDistance) < Math.abs(closestX)) {
                closestX = xDistance;
                closestChild = child;
            }
        }

        return closestChild;
    }

    private View getCenterView() {
        return getChildClosestToPosition(getMeasuredWidth() / 2);
    }

    private void scrollToView(View child) {
        if (child == null)
            return;

        stopScroll();

        int scrollDistance = getScrollDistance(child);

        if (scrollDistance != 0)
            smoothScrollBy(scrollDistance, 0);
    }

    private int getScrollDistance(View child) {
        int itemWidth = getChildAt(0).getMeasuredWidth();
        int centerX = getMeasuredWidth() / 2;

        int childCenterX = ((int)child.getX() + (itemWidth / 2));

        return childCenterX - centerX;
    }

    private float getPercentageFromCenter(View child) {
        float centerX = (getMeasuredWidth() / 2);
        float childCenterX = child.getX() + (child.getWidth() / 2);

        float offSet = Math.max(centerX, childCenterX) - Math.min(centerX, childCenterX);

        int maxOffset = (getMeasuredWidth() / 2) + child.getWidth();

        return (offSet / maxOffset);
    }

    public boolean isChildCenterView(View child) {
        return child == getCenterView();
    }

    public int getHorizontalScrollOffset() {
        return computeHorizontalScrollOffset();
    }

    public int getVerticalScrollOffset() {
        return computeVerticalScrollOffset();
    }

    public void smoothUserScrollBy(int x, int y) {
        mUserScrolling = true;
        smoothScrollBy(x, y);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }

}
