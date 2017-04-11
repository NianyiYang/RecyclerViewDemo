package com.yny.recyclerviewdemo.custom;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Arrays;

/**
 * 自动加载更多的视图
 * Created by nianyi.yang on 2017/4/11.
 */

public class DYRefreshFooter extends FrameLayout {

    // 是否关联RecyclerView
    private boolean isAttached;
    // RecyclerView是否颠倒
    private boolean isReversed;
    // RecyclerView是否水平
    private boolean isVertical;
    // 是否可加载更多
    private boolean isLoadEnable = true;
    // 是否加载完成
    private boolean isLoadComplete = true;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private OnLoadMoreListener mOnLoadMoreListener;

    //  给RecyclerView的头部底部加Decoration
    private CanItemDecoration mDecoration;

    public DYRefreshFooter(@NonNull Context context) {
        super(context);
    }

    public DYRefreshFooter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DYRefreshFooter(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 是否可以加载，没有更多时可以调用
     *
     * @param loadEnable boolean
     */
    public void setLoadEnable(boolean loadEnable) {
        isLoadEnable = loadEnable;
    }


    /**
     * 获取 mDecoration
     *
     * @return CanItemDecoration
     */
    public CanItemDecoration getDecoration() {
        return mDecoration;
    }


    /**
     * 从recyclerView移出
     */
    public void remove() {
        isAttached = false;

        if (mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
            mRecyclerView.removeOnChildAttachStateChangeListener(mOnAttachListener);

            if (mDecoration != null) {
                mRecyclerView.removeItemDecoration(mDecoration);
            }
        }
    }

    /**
     * 加载完成时调用，避免重复加载
     */
    public void loadMoreComplete() {
        this.isLoadComplete = true;
    }

    /**
     * 依附的方法
     *
     * @param recyclerView RecyclerView
     */
    public void attachTo(@NonNull final RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() == null) {
            throw new IllegalStateException("no LayoutManager.");
        }

        mRecyclerView = recyclerView;
        mLayoutManager = mRecyclerView.getLayoutManager();

        initLayoutManager();

        isAttached = true;

        if (mDecoration != null) {
            recyclerView.removeItemDecoration(mDecoration);
        }
        mDecoration = new CanItemDecoration(mLayoutManager).setIsHeader(false);
        recyclerView.addItemDecoration(mDecoration);

        recyclerView.removeOnScrollListener(mOnScrollListener);
        recyclerView.addOnScrollListener(mOnScrollListener);

        recyclerView.removeOnChildAttachStateChangeListener(mOnAttachListener);
        recyclerView.addOnChildAttachStateChangeListener(mOnAttachListener);
    }

    /**
     * 通过layoutManager获取各种属性值
     */
    private void initLayoutManager() {

        if (mLayoutManager instanceof GridLayoutManager) {

            GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
            this.isReversed = gridLayoutManager.getReverseLayout();
            this.isVertical = gridLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

        } else if (mLayoutManager instanceof LinearLayoutManager) {

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;
            this.isReversed = linearLayoutManager.getReverseLayout();
            this.isVertical = linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {

            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mLayoutManager;
            this.isReversed = staggeredGridLayoutManager.getReverseLayout();
            this.isVertical = staggeredGridLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;
        }

    }

    /**
     * 重写该方法，更新头部底部宽高
     *
     * @param changed boolean
     * @param l       int
     * @param t       int
     * @param r       int
     * @param b       int
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && isAttached) {

            if (mDecoration != null) {

                int vertical = 0;
                int horizontal = 0;
                if (getLayoutParams() instanceof MarginLayoutParams) {
                    final MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
                    vertical = layoutParams.topMargin + layoutParams.bottomMargin;
                    horizontal = layoutParams.leftMargin + layoutParams.rightMargin;
                }
                mDecoration.setHeight(getHeight() + vertical).setWidth(getWidth() + horizontal);
                mRecyclerView.invalidateItemDecorations();
            }

            onScrollChanged();
        }
        super.onLayout(changed, l, t, r, b);
    }

    /**
     * 滚动时移动头部底部
     */
    public void onScrollChanged() {
        boolean isVisibility = hasItems() && isLastRowVisible();
        translationXY(isVisibility);
    }

    private boolean hasItems() {
        return mRecyclerView.getAdapter() != null && mRecyclerView.getAdapter().getItemCount() != 0;
    }

    /**
     * 最后一项是否被滑动到显示出来的位置
     *
     * @return boolean
     */
    private boolean isLastRowVisible() {
        if (mLayoutManager instanceof GridLayoutManager) {

            GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
            return gridLayoutManager.findLastVisibleItemPosition() == mLayoutManager.getItemCount() - 1;

        } else if (mLayoutManager instanceof LinearLayoutManager) {

            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;
            return linearLayoutManager.findLastVisibleItemPosition() == mLayoutManager.getItemCount() - 1;

        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {

            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mLayoutManager;
            int[] positions = staggeredGridLayoutManager.findLastVisibleItemPositions(null);
            Arrays.sort(positions);
            return positions[staggeredGridLayoutManager.getSpanCount() - 1] >= mLayoutManager.getItemCount() - 1;
        }

        return false;
    }

    /**
     * 移动的方法
     *
     * @param isVisibility boolean
     */
    private void translationXY(boolean isVisibility) {
        setVisibility(isVisibility ? VISIBLE : INVISIBLE);

        if (isVisibility) {

            if (isLoadEnable && isLoadComplete && mOnLoadMoreListener != null) {
                mOnLoadMoreListener.onLoadMore();
                isLoadComplete = false;
            }

            int first = calculateTranslation();

            if (isVertical) {
                setTranslationY(first);
            } else {
                setTranslationX(first);
            }
        }
    }

    /**
     * 判断头部底部进行计算距离
     *
     * @return int
     */
    private int calculateTranslation() {
        return calculateTranslationXY(isReversed);
    }

    /**
     * 计算距离的方法
     *
     * @param isTop boolean
     * @return int
     */
    private int calculateTranslationXY(boolean isTop) {
        if (!isTop) {
            int offset = getScrollOffset();
            int base = getScrollRange() - getSize();
            return base - offset;

        } else {
            return -getScrollOffset();
        }
    }

    private int getScrollOffset() {
        return isVertical ? mRecyclerView.computeVerticalScrollOffset() : mRecyclerView.computeHorizontalScrollOffset();
    }

    private int getScrollRange() {
        return isVertical ? mRecyclerView.computeVerticalScrollRange() : mRecyclerView.computeHorizontalScrollRange();
    }

    private int getSize() {
        return isVertical ? getHeight() : getWidth();
    }

    // 滑动监听
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            onScrollChanged();
        }
    };

    // 依附监听
    private RecyclerView.OnChildAttachStateChangeListener mOnAttachListener = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {
            post(new Runnable() {
                @Override
                public void run() {

                    if (!mRecyclerView.isComputingLayout()) {
                        mRecyclerView.invalidateItemDecorations();
                    }
                    onScrollChanged();
                }
            });
        }
    };

    /**
     * 设置加载监听事件
     *
     * @param loadMoreListener OnLoadMoreListener
     */
    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
