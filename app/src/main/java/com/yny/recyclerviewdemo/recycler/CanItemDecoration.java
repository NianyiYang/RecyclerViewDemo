package com.yny.recyclerviewdemo.recycler;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Decoration
 */
public class CanItemDecoration extends RecyclerView.ItemDecoration {
    private int height;
    private int width;
    private int rowSpan = 1;

    private boolean isReversed;
    private boolean isVertical;

    private boolean isHeader = true;
    private RecyclerView.LayoutManager mLayoutManager;

    public CanItemDecoration(RecyclerView.LayoutManager manager) {

        this.mLayoutManager = manager;
        initLayoutManager();
    }

    public CanItemDecoration setHeight(int height) {
        this.height = height;
        return this;
    }

    public CanItemDecoration setWidth(int width) {
        this.width = width;
        return this;
    }

    public CanItemDecoration setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
        return this;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        boolean relatedPosition;

        initLayoutManager();

        if (isHeader) {

            relatedPosition = parent.getChildLayoutPosition(view) < rowSpan;
        } else {

            int lastSum = 1;
            int itemCount = mLayoutManager.getItemCount();
            if (itemCount > 0 && rowSpan > 1) {

                lastSum = itemCount % rowSpan;

                if (lastSum == 0) {
                    lastSum = rowSpan;
                }
            }

            int count = itemCount - lastSum;

            int lastPosition = parent.getChildLayoutPosition(view);
            relatedPosition = lastPosition >= count;
        }

        int heightOffset = relatedPosition && isVertical ? height : 0;
        int widthOffset = relatedPosition && !isVertical ? width : 0;

        if (isHeader) {

            if (isReversed) {
                outRect.bottom = heightOffset;
                outRect.right = widthOffset;
            } else {
                outRect.top = heightOffset;
                outRect.left = widthOffset;
            }

        } else {

            if (isReversed) {
                outRect.top = heightOffset;
                outRect.left = widthOffset;
            } else {
                outRect.bottom = heightOffset;
                outRect.right = widthOffset;
            }
        }
    }

    /**
     * 通过layoutManager获取各种属性值
     */
    private void initLayoutManager() {

        if (mLayoutManager instanceof GridLayoutManager) {

            GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
            this.rowSpan = gridLayoutManager.getSpanCount();
            this.isReversed = gridLayoutManager.getReverseLayout();
            this.isVertical = gridLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

        } else if (mLayoutManager instanceof LinearLayoutManager) {

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;
            this.rowSpan = 1;
            this.isReversed = linearLayoutManager.getReverseLayout();
            this.isVertical = linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {

            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mLayoutManager;
            this.rowSpan = staggeredGridLayoutManager.getSpanCount();
            this.isReversed = staggeredGridLayoutManager.getReverseLayout();
            this.isVertical = staggeredGridLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;
        }

    }
}