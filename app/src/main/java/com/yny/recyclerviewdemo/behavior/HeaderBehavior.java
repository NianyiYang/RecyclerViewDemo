package com.yny.recyclerviewdemo.behavior;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.yny.recyclerviewdemo.R;

import java.lang.ref.WeakReference;

/**
 * 头部 behavior
 * Created by nianyi.yang on 2017/4/18.
 */

public class HeaderBehavior extends CoordinatorLayout.Behavior<View> {

    private WeakReference<View> dependentView;
    // 渐变效果
    private ArgbEvaluator argbEvaluator;

    public HeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        argbEvaluator = new ArgbEvaluator();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.scroll_header) {
            dependentView = new WeakReference<>(dependency);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        Resources resources = getDependentView().getResources();
        final float progress = 1.f - Math.abs(dependency.getTranslationY() / (dependency.getHeight()
                - dp2px(resources, 50)));

        // Translation
        final float collapsedOffset = dp2px(resources, 5);
        final float initOffset = dp2px(resources, 130);
        final float translateY = collapsedOffset + (initOffset - collapsedOffset) * progress;
        child.setTranslationY(translateY);

        // Background
        child.setBackgroundColor((int) argbEvaluator.evaluate(
                progress,
                ContextCompat.getColor(getDependentView().getContext(), R.color.colorStartBackground),
                ContextCompat.getColor(getDependentView().getContext(), R.color.colorEndBackground)));

        // Margins
        final float collapsedMargin = dp2px(resources, 5);
        final float initMargin = dp2px(resources, 20);
        final int margin = (int) (collapsedMargin + (initMargin - collapsedMargin) * progress);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.setMargins(margin, 0, margin, 0);
        child.setLayoutParams(lp);

        return true;
    }

    private View getDependentView() {
        return dependentView.get();
    }

    public float dp2px(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics());
    }

}
