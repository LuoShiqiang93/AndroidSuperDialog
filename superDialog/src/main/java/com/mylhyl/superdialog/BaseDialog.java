package com.mylhyl.superdialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mylhyl.superdialog.auto.AutoUtils;
import com.mylhyl.superdialog.view.Controller;

import java.io.Serializable;

/**
 * Created by hupei on 2016/3/8 13:25.
 */
abstract class BaseDialog extends DialogFragment {

    public abstract View createView();

    protected Controller.Params mParams;

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = createView();
        builder.setView(view);
        Dialog dialog = builder.create();
        mParams.mDialogFragment = this;
        dialog.setCanceledOnTouchOutside(mParams.mCancelable);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null)
            setDialogGravity(dialog);//设置对话框布局
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 设置对话框底部显示
     *
     * @param dialog
     */
    private void setDialogGravity(Dialog dialog) {
        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams wlp = window.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);//获取屏幕宽
        wlp.width = (int) (dm.widthPixels * mParams.mWidth);//宽度按屏幕大小的百分比设置
        wlp.gravity = mParams.mGravity;
        //如果是底部显示，则向上移20px
        if (wlp.gravity == Gravity.BOTTOM) {
            mParams.y = 20;
        }
        wlp.x = AutoUtils.scaleValue(mParams.x);
        wlp.y = AutoUtils.scaleValue(mParams.y);

        //边距
        if (mParams.mPadding != null) {
            int[] padding = mParams.mPadding;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.getDecorView().setPadding(padding[0], padding[1], padding[2], padding[3]);
        }
        //动画
        if (mParams.mAnimStyle != 0) {
            window.setWindowAnimations(mParams.mAnimStyle);
        }
        if (mParams.isDimEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        if (mParams.mConfigDialog != null) {
            mParams.mConfigDialog.onConfig(dialog, window, wlp, dm);
        }
        window.setAttributes(wlp);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(this, tag);
        transaction.commitAllowingStateLoss();// 防止按home键后，show的时候调用父类中的commit方法异常
    }

    public static class Builder<T extends Builder> implements Serializable {
        protected FragmentActivity mActivity;
        protected Controller.Params mParams;

        Builder(FragmentActivity activity) {
            mActivity = activity;
            mParams = new Controller.Params();
        }

        public Context getContext() {
            return mActivity;
        }

        /**
         * 设置对话框位置
         * {@link Gravity#CENTER 默认}
         *
         * @param gravity
         */
        public T setGravity(int gravity) {
            mParams.mGravity = gravity;
            return (T) this;
        }

        /**
         * 设置对话框透明度
         *
         * @param alpha 0.0 - 1.0
         * @return
         */
        public T setAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
            mParams.mAlpha = alpha;
            return (T) this;
        }

        /**
         * 设置对话框背景色
         *
         * @param color
         * @return
         */
        public T setBackgroundColor(@ColorInt int color) {
            mParams.mBackgroundColor = color;
            return (T) this;
        }

        /**
         * 设置对话框圆角
         *
         * @param radius
         * @return
         */
        public T setRadius(int radius) {
            mParams.mRadius = radius;
            return (T) this;
        }

        /**
         * 设置对话框点击外部关闭
         *
         * @param cancel
         * @return
         */
        public T setCanceledOnTouchOutside(boolean cancel) {
            mParams.mCancelable = cancel;
            return (T) this;
        }

        /**
         * 设置对话框宽度
         *
         * @param width 0.0 - 1.0
         * @return
         */
        public T setWidth(@FloatRange(from = 0.0, to = 1.0) float width) {
            mParams.mWidth = width;
            return (T) this;
        }

        /**
         * 设置边距
         *
         * @param left   px
         * @param top    px
         * @param right  px
         * @param bottom px
         * @return
         */
        public T setPadding(int left, int top, int right, int bottom) {
            mParams.mPadding = new int[]{AutoUtils.scaleValue(left), AutoUtils.scaleValue(top)
                    , AutoUtils.scaleValue(right), AutoUtils.scaleValue(bottom)};
            return (T) this;
        }

        /**
         * 动画弹出对话框,style动画资源
         *
         * @param animStyle
         * @return
         */
        public T setWindowAnimations(int animStyle) {
            mParams.mAnimStyle = animStyle;
            return (T) this;
        }

        /**
         * 列表距离下方按钮的间距
         *
         * @param bottomMargin px
         * @return
         */
        public T setItemsBottomMargin(int bottomMargin) {
            mParams.mItemsBottomMargin = bottomMargin;
            return (T) this;
        }

        /**
         * 指定位置显示
         *
         * @param x
         * @param y
         */
        public T setShowAtLocation(int x, int y) {
            return setShowAtLocation(mParams.mGravity, x, y);
        }

        /**
         * 指定位置显示
         *
         * @param gravity {@link Gravity}
         * @param x
         * @param y
         */
        public T setShowAtLocation(int gravity, int x, int y) {
            mParams.mGravity = gravity;
            mParams.x = x;
            mParams.y = y;
            return (T) this;
        }

        /**
         * 设置背景是否昏暗，默认true
         *
         * @param dimEnabled
         * @return
         */
        public T setDimEnabled(boolean dimEnabled) {
            mParams.isDimEnabled = dimEnabled;
            return (T) this;
        }

        public T setConfigDialog(SuperDialog.ConfigDialog configDialog) {
            mParams.mConfigDialog = configDialog;
            return (T) this;
        }

        void checkBuilderParams() {
            if (mParams.mProviderContent == null) {
                throw new IllegalArgumentException("message is empty, please set");
            }
            if (mParams.mProviderContent == null) {
                throw new IllegalArgumentException("alpha is 0.0 to 1.0, please setAlpha");
            }
            if (mParams.mWidth < 0 || mParams.mWidth > 1) {
                throw new IllegalArgumentException("width is 0.0 to 1.0, please setWidth");
            }
        }
    }
}
