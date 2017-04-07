package com.yny.recyclerviewdemo.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yny.recyclerviewdemo.R;

/**
 * Created by nianyi.yang on 2017/4/7.
 */

public class DYRefreshView extends FrameLayout implements CanRefresh {

    private CharSequence completeStr = "刷新完成";
    private CharSequence refreshingStr = "数据加载中...";
    private CharSequence pullStr = "下拉立即刷新";
    private CharSequence releaseStr = "松开以刷新";

    private ImageView mLoader;
    private TextView mText;
    private ProgressBar mProgressBar;


    public DYRefreshView(Context context) {
        this(context, null);
    }

    public DYRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DYRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_refresh, null);
        addView(v);
    }


    public CharSequence getReleaseStr() {
        return releaseStr;
    }

    public void setReleaseStr(CharSequence releaseStr) {
        this.releaseStr = releaseStr;
    }

    public CharSequence getPullStr() {
        return pullStr;
    }

    public void setPullStr(CharSequence pullStr) {
        this.pullStr = pullStr;
    }

    public CharSequence getRefreshingStr() {
        return refreshingStr;
    }

    public void setRefreshingStr(CharSequence refreshingStr) {
        this.refreshingStr = refreshingStr;
    }

    public CharSequence getCompleteStr() {
        return completeStr;
    }

    public void setCompleteStr(CharSequence completeStr) {
        this.completeStr = completeStr;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLoader = (ImageView) findViewById(R.id.loader);
        mText = (TextView) findViewById(R.id.text);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }


    @Override
    public void onReset() {

    }

    @Override
    public void onPrepare() {
        mText.setText(pullStr);
    }


    @Override
    public void onComplete() {
        mText.setText(completeStr);
    }

    @Override
    public void onRelease() {
        mText.setText(refreshingStr);
    }

    @Override
    public void onPositionChange(float currentPercent) {

        if (currentPercent < 1) {
            mText.setText(pullStr);
        } else {
            mText.setText(releaseStr);
        }
    }

    @Override
    public void setIsHeaderOrFooter(boolean isHead) {
        if (isHead) {
            mLoader.setVisibility(VISIBLE);
            mProgressBar.setVisibility(GONE);
        } else {
            mLoader.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
        }
    }
}
