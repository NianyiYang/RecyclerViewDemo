/*
 * *
 *  * 通联数据机密
 *  * --------------------------------------------------------------------
 *  * 通联数据股份公司版权所有 © 2013-2017
 *  *
 *  * 注意：本文所载所有信息均属于通联数据股份公司资产。本文所包含的知识和技术概念均属于
 *  * 通联数据产权，并可能由中国、美国和其他国家专利或申请中的专利所覆盖，并受商业秘密或
 *  * 版权法保护。
 *  * 除非事先获得通联数据股份公司书面许可，严禁传播文中信息或复制本材料。
 *  *
 *  * DataYes CONFIDENTIAL
 *  * --------------------------------------------------------------------
 *  * Copyright © 2013-2017 DataYes, All Rights Reserved.
 *  *
 *  * NOTICE: All information contained herein is the property of DataYes
 *  * Incorporated. The intellectual and technical concepts contained herein are
 *  * proprietary to DataYes Incorporated, and may be covered by China, U.S. and
 *  * Other Countries Patents, patents in process, and are protected by trade
 *  * secret or copyright law.
 *  * Dissemination of this information or reproduction of this material is
 *  * strictly forbidden unless prior written permission is obtained from DataYes.
 *
 */

package com.yny.recyclerviewdemo.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yny.recyclerviewdemo.R;

import java.util.HashMap;
import java.util.Map;

/**
 * ：rong.mo
 * @version 1.0
 * CListView
 * ：类描述
 * ：2015-12-30 下午3:29:16
 */
public class CListView extends ListView implements OnScrollListener {

    /** listview高度 */
    private int maxHeight_ = -1;

    public int getMaxHeight() {
        return maxHeight_;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight_ = maxHeight;
    }

    /**
     * 刷新列表的超时时间
     */
    private final static int CLISTVIEW_REFRESH_TIMEOUT = 15000;

    private final static int RELEASE_To_REFRESH = 0;//放手刷新
    private final static int PULL_To_REFRESH = 1;//下拉刷新
    public final static int EXECUTING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;//加载中

    private final static int RELEASE_TO_MORE = 5;//放手刷新
    private final static int PULL_UP_MORE = 6;//下拉刷新

    //UI组件
    private ImageView mIvShowBefore;
    private ProgressBar progressBar;
    private TextView tipsTextview;

    private ImageView footArrow;
    private ProgressBar footPb;
    private TextView footTips;
    private LinearLayout headView;
    private LinearLayout footView;

    // 实际的padding的距离与界面上偏移距离的比例
    private final static int RATIO = 3;
    private int headContentHeight;
    private int footContentHeight;
    private int start_down_Y;
    private boolean isRecored;//保证触摸的y值只记录一次
    private int firstItemIndex;
    private int state;

    private int remainItems;
    private int moreDownY;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;


    private OnRefreshListener refreshListener;
    private OnMoreListener moreListener;

    //超时倒计时
    private Handler mTimeoutDelay_ = new Handler();
    private Runnable mTimeoutDelayRunnable_;
    private Runnable mMoreDealyRunable_;
    private Runnable mRefreshDelayRunable;

    private boolean isBack;
    private boolean isMoreBack;
    private boolean isMoreRecored;
    private boolean isNoMore;
    private boolean mIsHeadView_ = false;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //刷新，更多的开关

    private boolean isRefreshEnable = true;
    private boolean moreEnable;

    private boolean isMoreEnable() {

        return moreEnable;
    }

    public void setMoreEnable(boolean moreEnable) {

        this.moreEnable = moreEnable;
    }

    public OnMoreListener getMoreListener() {

        return moreListener;
    }

    public void setMoreListener(OnMoreListener moreListener) {

        this.moreListener = moreListener;
    }

    public void setRefreshEnable(boolean isRefreshEnable) {

        this.isRefreshEnable = isRefreshEnable;
    }

    public void setonRefreshListener(OnRefreshListener refreshListener) {

        this.refreshListener = refreshListener;
    }

    /**
     * 刷新回调接口
     */
    public interface OnRefreshListener {

        void onRefresh();
    }

    /**
     * 更多回调接口
     */
    public interface OnMoreListener {

        void onMore();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //当前的状态

    private int moreState;

    public int onMoreState() {

        return moreState;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //是否拦截触摸移动

    private boolean mInterceptMove_ = false;

    public void setInterceptMove(boolean v) {

        mInterceptMove_ = v;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //是否禁止滚动

    private boolean isForbidScroll_ = false;

    public void setIsForbidScroll(boolean v) {
        isForbidScroll_ = v;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //滚动事件

    private OnCListViewScrollYListener mListViewScrollYListener_;
    //ClistView移动到顶部或者底部的事件
    private IOnCListViewOverScrollListener mOverScrollListener;

    //滚动状态变化事件
    private IOnClistViewScrollYStateChanged mScrollStateChangeListence;

    public void setCListViewScrollYListener(OnCListViewScrollYListener l) {

        mListViewScrollYListener_ = l;
    }

    public void setOverScrollListener(IOnCListViewOverScrollListener mOverScrollListener) {
        this.mOverScrollListener = mOverScrollListener;
    }

    /**
     * ClistView滚动条变化事件
     */
    public interface OnCListViewScrollYListener {

        void scrollYChanged(int scrollY);
    }

    /**
     * ClistView移动到顶部或者底部的事件
     */
    public interface IOnCListViewOverScrollListener {

        void onCListviewOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY);
    }

    /**
     * ClistView滚动条状态变化事件
     */
    public interface IOnClistViewScrollYStateChanged {

        void CListViewScrollStateChanged(int state);
    }

    public void setScrollStateChangeListence(IOnClistViewScrollYStateChanged mScrollStateChangeListence) {
        this.mScrollStateChangeListence = mScrollStateChangeListence;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //判断是否已经全部加载

    private Map<String, String> mItemHeights_ = new HashMap<>();
    private IsCListViewAllItemsLoadedCallBack mIsAllItemsLoaded_;

    public void setCListViewAllItemsLoadedCallBack(IsCListViewAllItemsLoadedCallBack l) {

        mIsAllItemsLoaded_ = l;
    }

    public interface IsCListViewAllItemsLoadedCallBack {

        boolean isAllItemsLoaded();
    }

    private boolean isAllItemsLoaded() {

        if (mIsAllItemsLoaded_ != null)
            return mIsAllItemsLoaded_.isAllItemsLoaded();

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CListView(Context context, AttributeSet attrs) {

        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        setFadingEdgeLength(0);
        //setCacheColorHint(context.getResources().getColor(#00000000));
        headView = (LinearLayout) inflater.inflate(R.layout.list_head_new, null);
        measureView(headView);
        headContentHeight = headView.getMeasuredHeight();
        headView.setPadding(0, -headContentHeight, 0, 0);
        headView.invalidate();
        addHeaderView(headView, null, false);

        footView = (LinearLayout) inflater.inflate(R.layout.list_foot, null);
        measureView(footView);
        footContentHeight = footView.getMeasuredHeight();
        footView.setPadding(0, -footContentHeight, 0, 0);
        footView.invalidate();
        addFooterView(footView, null, false);

        mIvShowBefore = (ImageView) headView.findViewById(R.id.iv_before_);
        progressBar = (ProgressBar) headView
                .findViewById(R.id.head_progressBar);
        tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);

        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);
        initFootView();
        setOnScrollListener(this);
        state = DONE;
        moreState = DONE;

        this.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    /**
     * @param view
     */
    public void addSecondHeaderViewToTop(View view) {

        if (this.getHeaderViewsCount() <= 1) {
            this.removeHeaderView(headView);
            this.addHeaderView(view, null, false);
            this.addHeaderView(headView, null, false);
            mIsHeadView_ = true;
        } else {

            this.addHeaderView(view, null, false);
        }
    }

    private void initFootView() {

        footArrow = (ImageView) footView.findViewById(R.id.foot_arrowImageView);
        footPb = (ProgressBar) footView.findViewById(R.id.foot_progressBar);
        footTips = (TextView) footView.findViewById(R.id.foot_tipsTextView);
    }

    private void measureView(View child) {

        ViewGroup.LayoutParams p = child.getLayoutParams();

        if (p == null) {

            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;

        if (lpHeight > 0) {

            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {

            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        try {
            super.dispatchDraw(canvas);
        } catch (IndexOutOfBoundsException e) {
            // samsung error
        }
    }

    @Override
    public void onScroll(AbsListView v, int firstVisiableItem,
                         int visibleItemCount, int totalItemCount) {

        firstItemIndex = firstVisiableItem;
        remainItems = totalItemCount - firstVisiableItem - visibleItemCount;

        View firstView = this.getChildAt(0);

        if (firstView != null && mListViewScrollYListener_ != null) {

            mItemHeights_.put(String.valueOf(firstVisiableItem), String.valueOf(firstView.getMeasuredHeight()));

            int scrollY = Math.abs(firstView.getTop());

            if (firstVisiableItem > 1) {

                for (int i = 0; i < firstVisiableItem - 1; ++i) {

                    if (mItemHeights_.containsKey(String.valueOf(i))) {

                        scrollY += Integer.valueOf(mItemHeights_.get(String.valueOf(i)));
                    }
                }
            }

            mListViewScrollYListener_.scrollYChanged(scrollY);
        }
    }

    /**
     * 获取ClistViewScrollY
     *
     * @return
     */
    public int getClistViewScrollY() {

        int scrollY = 0;
        View firstView = this.getChildAt(0);

        if (firstView != null) {

            mItemHeights_.put(String.valueOf(firstItemIndex), String.valueOf(firstView.getMeasuredHeight()));
            scrollY = Math.abs(firstView.getTop());

            if (firstItemIndex > 1) {

                for (int i = 0; i < firstItemIndex - 1; ++i) {

                    if (mItemHeights_.containsKey(String.valueOf(i))) {

                        scrollY += Integer.valueOf(mItemHeights_.get(String.valueOf(i)));
                    }
                }
            }
        }

        return scrollY;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {

        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

        if (mOverScrollListener != null)
            mOverScrollListener.onCListviewOverScrolled(getScrollX(), getClistViewScrollY(), clampedX, clampedY);

        if (moreEnable && clampedY && mIsAllItemsLoaded_ != null && !mIsAllItemsLoaded_.isAllItemsLoaded() && this.getFirstVisiblePosition() > 0) {

            moreState = EXECUTING;
            changeFootViewByState();
            setSelection(this.getLastVisiblePosition());

            if (mMoreDealyRunable_ == null) {

                mMoreDealyRunable_ = new Runnable() {
                    @Override
                    public void run() {

                        onMore();
                    }
                };
            }

            mTimeoutDelay_.removeCallbacks(mMoreDealyRunable_);
            //这里加一个500毫秒的延时，让loading持续更久一点
            mTimeoutDelay_.postDelayed(mMoreDealyRunable_, 500);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        // TODO Auto-generated method stub

        if (mScrollStateChangeListence != null)
            mScrollStateChangeListence.CListViewScrollStateChanged(arg1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (firstItemIndex == 0)
                    start_down_Y = (int) ev.getY();

                interceptTouchEventInternal(ev);

                break;

            case MotionEvent.ACTION_MOVE:

                if (mInterceptMove_)
                    return false;

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        interceptTouchEventInternal(event);

        if (this.getChildCount() >= 2) {

            if (isRefreshEnable) {
                refreshTouch(event);
            }

            if (isMoreEnable() && !isNoMore && getAdapter() != null && this.getFirstVisiblePosition() != 0) {

                moreTouch(event);
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 只重写该方法，达到使ListView适应ScrollView的效果
     */
    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (maxHeight_ > -1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight_,
                    MeasureSpec.AT_MOST);
        }
        if (isForbidScroll_) {

            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                    MeasureSpec.AT_MOST);

            super.onMeasure(widthMeasureSpec, expandSpec);

        } else {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void refreshTouch(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:

                int tempY = (int) event.getY();

                if (yHasIntercept && firstItemIndex == 0) {

                    isRecored = true;
                }

                if (state != EXECUTING && isRecored && state != LOADING) {
                    // 可以松手去刷新了
                    if (state == RELEASE_To_REFRESH) {

                        setSelection(0);
                        // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                        if (((tempY - start_down_Y) / RATIO < headContentHeight)
                                && (tempY - start_down_Y) > 0) {

                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();

                        } else if (tempY - start_down_Y <= 0) {// 一下子推到顶了

                            state = DONE;
                            changeHeaderViewByState();

                        } else {// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
                            // 不用进行特别的操作，只用更新paddingTop的值就行了
                        }
                    }

                    if (state == PULL_To_REFRESH) {

                        if (!mIsHeadView_) {

                            setSelection(0);
                        }
                        // 下拉到可以进入RELEASE_TO_REFRESH的状态
                        if ((tempY - start_down_Y) / RATIO >= headContentHeight) {

                            state = RELEASE_To_REFRESH;
                            isBack = true;
                            changeHeaderViewByState();

                        } else if (tempY - start_down_Y <= 0) {// 上推到顶了

                            state = DONE;
                            changeHeaderViewByState();
                        }
                    }

                    // done状态下
                    if (state == DONE) {

                        if (tempY - start_down_Y > 0) {

                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();
                        }
                    }

                    // 更新headView的paddingTop
                    if (state == RELEASE_To_REFRESH || state == PULL_To_REFRESH) {

                        headView.setPadding(0, (tempY - start_down_Y) / RATIO
                                - headContentHeight, 0, 0);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                if (state == PULL_To_REFRESH) {

                    state = DONE;
                    changeHeaderViewByState();
                }

                if (state == RELEASE_To_REFRESH) {

                    state = EXECUTING;
                    changeHeaderViewByState();

                    if (mRefreshDelayRunable == null) {

                        mRefreshDelayRunable = new Runnable() {
                            @Override
                            public void run() {

                                onRefresh();
                            }
                        };
                    }

                    mTimeoutDelay_.removeCallbacks(mRefreshDelayRunable);
                    mTimeoutDelay_.postDelayed(mRefreshDelayRunable, 500);
                }

                isRecored = false;
                isBack = false;
                break;

            default:

                break;
        }
    }

    private void moreTouch(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (!isMoreRecored && remainItems <= 0) {

                    isMoreRecored = true;
                    moreDownY = (int) event.getY();
                }

                break;

            case MotionEvent.ACTION_MOVE:

                int tempY = (int) event.getY();

                if (!isMoreRecored && remainItems <= 0) {

                    isMoreRecored = true;
                    moreDownY = tempY;
                }

                if (moreState != EXECUTING && isMoreRecored && moreState != LOADING) {

                    if (moreState == RELEASE_TO_MORE) {

                        setSelection(getAdapter().getCount() - 1);

                        if (tempY - moreDownY < 0 &&
                                (moreDownY - tempY) / RATIO < footContentHeight) {

                            moreState = PULL_UP_MORE;
                            changeFootViewByState();

                        } else if (tempY - moreDownY >= 0) {

                            moreState = DONE;
                            changeFootViewByState();
                        }
                    }

                    if (moreState == PULL_UP_MORE) {

                        setSelection(getAdapter().getCount() - 1);

                        if ((moreDownY - tempY) / RATIO >= footContentHeight) {

                            moreState = RELEASE_TO_MORE;
                            isMoreBack = true;
                            changeFootViewByState();

                        } else if (tempY - moreDownY >= 0) {

                            moreState = DONE;
                            changeFootViewByState();
                        }
                    }

                    if (moreState == DONE) {

                        if (tempY - moreDownY < 0) {

                            moreState = PULL_UP_MORE;
                            changeFootViewByState();
                        }
                    }

                    if (moreState == PULL_UP_MORE || moreState == RELEASE_TO_MORE) {

                        int topPadding = footView.getPaddingTop();
                        int bottomPadding = (moreDownY - tempY) / RATIO;

                        if (topPadding <= 0) {

                            topPadding = this.getMeasuredHeight() - footView.getTop() + footContentHeight / 2;
                        }

                        if (topPadding < 0)
                            topPadding = 0;

                        footView.setPadding(0, topPadding, 0, bottomPadding - footContentHeight);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:

                if (moreState == PULL_UP_MORE || isAllItemsLoaded()) {

                    moreState = DONE;
                    changeFootViewByState();

                } else if (moreState == RELEASE_TO_MORE) {

                    moreState = EXECUTING;
                    changeFootViewByState();
                    onMore();
                }

                isMoreRecored = false;
                isMoreBack = false;

                break;

            case MotionEvent.ACTION_CANCEL:

                moreState = DONE;
                changeFootViewByState();

                break;

            default:
                break;
        }
    }

    /**
     * @return void
     * ：当状态改变时候，调用该方法，以更新界面
     * ：2014-6-12 下午1:54:30
     * ：Morong
     * Update Date    :
     * Update Author  : Morong
     */
    private void changeHeaderViewByState() {

        switch (state) {

            case RELEASE_To_REFRESH:

                mIvShowBefore.setVisibility(VISIBLE);
                progressBar.setVisibility(View.GONE);
                tipsTextview.setVisibility(View.VISIBLE);
                tipsTextview.setText(getResources().getString(R.string.loosen_refresh));

                break;

            case PULL_To_REFRESH:

                progressBar.setVisibility(View.GONE);
                tipsTextview.setVisibility(View.VISIBLE);
                mIvShowBefore.setVisibility(VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {

                    isBack = false;
                    tipsTextview.setText(getResources().getString(R.string.drop_down_refresh));

                } else {

                    tipsTextview.setText(getResources().getString(R.string.drop_down_refresh));
                }

                break;

            case EXECUTING:

                headView.setPadding(0, 0, 0, 0);
                progressBar.setVisibility(View.VISIBLE);
                mIvShowBefore.setVisibility(GONE);
                tipsTextview.setText(getResources().getString(R.string.refreshing_new));

                break;
            case DONE:

                headView.setPadding(0, -1 * headContentHeight, 0, 0);
                progressBar.setVisibility(View.GONE);
                mIvShowBefore.setImageResource(R.drawable.loading16);
                tipsTextview.setText(getResources().getString(R.string.drop_down_refresh));

                break;
        }
    }

    private void changeFootViewByState() {

        switch (moreState) {

            case RELEASE_TO_MORE:

                footArrow.setVisibility(View.VISIBLE);
                footPb.setVisibility(View.GONE);
                footTips.setVisibility(View.VISIBLE);

                footArrow.clearAnimation();

                if (!isAllItemsLoaded()) {

                    footArrow.startAnimation(animation);
                }

                footTips.setText(getResources().getString(
                        isAllItemsLoaded() ? R.string.is_All_Items_Loaded : R.string.loosen_more));

                break;

            case PULL_UP_MORE:

                footPb.setVisibility(View.GONE);
                footTips.setVisibility(View.VISIBLE);
                footArrow.clearAnimation();
                footArrow.setVisibility(View.VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isMoreBack) {

                    isMoreBack = false;

                    footArrow.clearAnimation();

                    if (!isAllItemsLoaded()) {

                        footArrow.startAnimation(reverseAnimation);
                    }

                    footTips.setText(getResources().getString(
                            isAllItemsLoaded() ? R.string.is_All_Items_Loaded : R.string.drop_up_more));
                } else {

                    footTips.setText(getResources().getString(
                            isAllItemsLoaded() ? R.string.is_All_Items_Loaded : R.string.drop_up_more));
                }

                break;

            case EXECUTING:

                int topPadding = footView.getPaddingTop();

                if (topPadding <= 0)
                    topPadding = this.getMeasuredHeight() - footView.getTop() + footContentHeight / 2;

                int bottomPadding = 0;
                View lastItem = this.getChildAt(this.getChildCount() - 1);

                if (lastItem != null) {

                    if (lastItem != footView) {

                        if (lastItem.getMeasuredHeight() > 0)
                            bottomPadding = lastItem.getMeasuredHeight() / 2;

                    } else {

                        lastItem = this.getChildAt(this.getChildCount() - 2);

                        if (lastItem != null) {

                            if (lastItem.getMeasuredHeight() > 0)
                                bottomPadding = lastItem.getMeasuredHeight() / 2;
                        }
                    }
                }

                bottomPadding -= footContentHeight / 2;

                if (bottomPadding < 0)
                    bottomPadding = 0;

                footView.setPadding(0, topPadding, 0, bottomPadding);
                footPb.setVisibility(View.VISIBLE);
                footArrow.clearAnimation();
                footArrow.setVisibility(View.GONE);
                footTips.setText(getResources().getString(R.string.refreshing));

                break;

            case DONE:

                footView.setPadding(0, -1 * footContentHeight, 0, 0);
                footPb.setVisibility(View.GONE);
                footArrow.clearAnimation();
                footArrow.setImageResource(R.drawable.list_arrow);
                footArrow.setRotation(-180);

                footTips.setText(getResources().getString(
                        isAllItemsLoaded() ? R.string.is_All_Items_Loaded : R.string.drop_up_more));

                break;
        }
    }

    //开始刷新超时计算
    private void startDelayTimeout() {

        if (mTimeoutDelayRunnable_ == null) {

            mTimeoutDelayRunnable_ = new Runnable() {
                @Override
                public void run() {

                    if (state == EXECUTING) {

                        state = DONE;
                        changeHeaderViewByState();
                        //DYToast.makeText(App.getInstance().getApplicationContext(), "刷新列表失败!", Toast.LENGTH_SHORT).show();
                    }

                    if (moreState == EXECUTING) {

                        moreState = DONE;
                        changeFootViewByState();
                        //DYToast.makeText(App.getInstance().getApplicationContext(), "加载更多失败!", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        mTimeoutDelay_.removeCallbacks(mTimeoutDelayRunnable_);
        mTimeoutDelay_.postDelayed(mTimeoutDelayRunnable_, CLISTVIEW_REFRESH_TIMEOUT);
    }

    private void onRefresh() {

        if (refreshListener != null) {

            refreshListener.onRefresh();

            startDelayTimeout();
        }

        retMore();
    }

    public void setNoMore(String note) {

        isNoMore = true;
        footView.setPadding(0, 0, 0, 0);
        footTips.setText(note);
        footArrow.setVisibility(View.INVISIBLE);
        footPb.setVisibility(View.GONE);
        footTips.setTextColor(0xFF999999);
    }

    private void retMore() {

        isNoMore = false;
        footView.setPadding(0, -1 * footContentHeight, 0, 0);
        footPb.setVisibility(View.GONE);
        footArrow.clearAnimation();
        footArrow.setImageResource(R.drawable.list_arrow);
        footTips.setText(getResources().getString(
                isAllItemsLoaded() ? R.string.is_All_Items_Loaded : R.string.drop_up_more));
        footTips.setTextColor(0xff333333);

    }

    private void onMore() {

        if (moreListener != null) {

            moreListener.onMore();

            startDelayTimeout();
        }
    }

    public void onRefreshComplete() {

        state = DONE;
        changeHeaderViewByState();
    }

    public void onMoreComplete() {

        moreState = DONE;

        if (!isNoMore) {

            changeFootViewByState();
        }
    }

    //private GestureDetector mGestureDetector;
    private float startX, startY;
    private boolean /*limitDetector,*/yHasIntercept, xHasIntercept;

    private static int scaledTouchSlop = -1;
    private static int scaledPagingSlop = -1;

    private void interceptTouchEventInternal(MotionEvent event) {

        if (scaledTouchSlop < 0)
            scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (scaledPagingSlop < 0)
            scaledPagingSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();

        float movedX, movedY;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                yHasIntercept = false;
                xHasIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                movedX = Math.abs(event.getX() - startX);
                movedY = Math.abs(event.getY() - startY);

                if ((movedY > scaledTouchSlop || movedY > scaledPagingSlop) &&
                        !xHasIntercept) {
                    yHasIntercept = true;
                }
                if (movedX > scaledPagingSlop && !yHasIntercept) {
                    xHasIntercept = true;
                }
                break;
        }
    }

}
