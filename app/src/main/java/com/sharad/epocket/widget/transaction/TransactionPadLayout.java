package com.sharad.epocket.widget.transaction;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Sharad on 23-Jul-16.
 */

public class TransactionPadLayout extends ViewGroup {
    private int mRowCount;
    private int mColumnCount;

    public TransactionPadLayout(Context context) {
        this(context, null);
    }

    public TransactionPadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransactionPadLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                new int[] { android.R.attr.rowCount, android.R.attr.columnCount }, defStyle, 0);
        mRowCount = a.getInt(0, 1);
        mColumnCount = a.getInt(1, 1);

        a.recycle();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        final boolean isRTL = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        final int columnWidth =
                Math.round((float) (right - left - paddingLeft - paddingRight)) / mColumnCount;
        final int rowHeight =
                Math.round((float) (bottom - top - paddingTop - paddingBottom)) / mRowCount;

        int rowIndex = 0, columnIndex = 0;
        for (int childIndex = 0; childIndex < getChildCount(); ++childIndex) {
            final View childView = getChildAt(childIndex);
            if (childView.getVisibility() == View.GONE) {
                continue;
            }

            final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();

            final int childTop = paddingTop + lp.topMargin + rowIndex * rowHeight;
            final int childBottom = childTop - lp.topMargin - lp.bottomMargin + rowHeight;
            final int childLeft = paddingLeft + lp.leftMargin +
                    (isRTL ? (mColumnCount - 1) - columnIndex : columnIndex) * columnWidth;
            final int childRight = childLeft - lp.leftMargin - lp.rightMargin + columnWidth;

            final int childWidth = childRight - childLeft;
            final int childHeight = childBottom - childTop;
            if (childWidth != childView.getMeasuredWidth() ||
                    childHeight != childView.getMeasuredHeight()) {
                childView.measure(
                        MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
            }
            childView.layout(childLeft, childTop, childRight, childBottom);

            rowIndex = (rowIndex + (columnIndex + 1) / mColumnCount) % mRowCount;
            columnIndex = (columnIndex + 1) % mColumnCount;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
