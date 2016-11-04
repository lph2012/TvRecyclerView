package com.lp.recyclerview4tvlibrary.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.lp.recyclerview4tvlibrary.effect.RecyclerViewEffect;
import com.lp.recyclerview4tvlibrary.utils.ViewUtils;


/**
 * Created by lph on 2016/10/20.
 */
public class TvRecyclerView extends RecyclerView {

    private ItemListener mItemListener;
    private final int DEFAULT_EDGE = ViewUtils.dpToPx(getContext(), 80);
    private int mRightEdge = DEFAULT_EDGE;
    private int mLeftEdge = DEFAULT_EDGE;
    private int mUpEdge = DEFAULT_EDGE;
    private int mDownEdge = DEFAULT_EDGE;

    private final static float DEFAULT_SCALE = 1.1f;
    private float mScale = DEFAULT_SCALE;

    private int mSaveFocusPosition = -1;
    private FocusFrameView mFocusFrameView;
    private RecyclerViewEffect mRecyclerViewEffect;

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setChildrenDrawingOrderEnabled(true);
        setWillNotDraw(true);
        setHasFixedSize(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setClipChildren(false);
        setClipToPadding(false);
        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);

        mItemListener = new ItemListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnItemListener != null) {
                    return mOnItemListener.onItemLongClick(TvRecyclerView.this, view, getChildLayoutPosition(view));
                }
                return false;
            }

            @Override
            public void onFocusChange(View view, boolean b) {
                if (null != mOnItemListener) {
                    if (view != null) {
                        view.setSelected(b);
                        if (b) {
                            mSaveFocusPosition = getChildLayoutPosition(view);
                            //当返回true表示用户处理，不需要RecyclerView自动处理
                            boolean result = mOnItemListener.onItemSelected(TvRecyclerView.this, view, getChildLayoutPosition(view));
                            if (!result) {
                                mRecyclerViewEffect.setFocusView(view, mScale);
                            }
                        } else {
                            boolean result = mOnItemListener.onItemPreSelected(TvRecyclerView.this, view, getChildLayoutPosition(view));
                            if (!result) {
                                mRecyclerViewEffect.setUnFocusView(view);
                            }
                        }
                    }
                }
            }

            @Override
            public void onClick(View itemView) {
                if (null != mOnItemListener) {
                    mOnItemListener.onItemClick(TvRecyclerView.this, itemView, getChildLayoutPosition(itemView));
                }
            }
        };

    }

    int position = 0;

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if (null != view) {
            position = getChildAdapterPosition(view) - getFirstVisiblePosition();
            if (position < 0) {
                return i;
            } else {
                if (i == childCount - 1) {//这是最后一个需要刷新的item
                    if (position > i) {
                        position = i;
                    }
                    return position;
                }
                if (i == position) {//这是原本要在最后一个刷新的item
                    return childCount - 1;
                }
            }
        }
        return i;
    }


    @Override
    public void onChildAttachedToWindow(View child) {
        if (!ViewCompat.hasOnClickListeners(child)) {
            child.setOnClickListener(mItemListener);
        }
        child.setOnLongClickListener(mItemListener);
        if (child.getOnFocusChangeListener() == null) {
            child.setOnFocusChangeListener(mItemListener);
        }

    }


    public int getRightEdge() {
        return mRightEdge;
    }

    public int getLeftEdge() {
        return mLeftEdge;
    }

    public void setmLeftEdge(int mLeftEdge) {
        this.mLeftEdge = mLeftEdge;
    }

    public void setmRightEdge(int mRightEdge) {
        this.mRightEdge = mRightEdge;
    }

    public void setmUpEdge(int mUpEdge) {
        this.mUpEdge = mUpEdge;
    }

    public void setmDownEdge(int mDownEdge) {
        this.mDownEdge = mDownEdge;
    }

    public int getUpEdge() {
        return mUpEdge;
    }

    public int getDownEdge() {
        return mDownEdge;
    }

    public interface OnItemListener {
        /**
         * 焦点离开
         *
         * @param parent   TvRecyclerView
         * @param itemView 失去焦点的itemView
         * @param position 失去焦点item的位置
         * @return true表示用户处理焦点移动, TvRecyclerView不再处理焦点移动, 否则用户不再处理焦点移动，交给TvRecyclerView处理焦点移动
         */
        boolean onItemPreSelected(TvRecyclerView parent, View itemView, int position);

        /**
         * 获取焦点
         *
         * @param parent   TvRecyclerView
         * @param itemView 获取焦点的itemView
         * @param position 获取焦点item的位置
         * @return true表示用户处理焦点移动, TvRecyclerView不再处理焦点移动, 否则用户不再处理焦点移动，交给TvRecyclerView处理焦点移动
         */
        boolean onItemSelected(TvRecyclerView parent, View itemView, int position);

        /**
         * 处理一些偏差
         *
         * @param parent   TvRecyclerView
         * @param itemView 获取焦点的itemView
         * @param position 获取焦点item的位置
         * @return true表示用户处理焦点移动, TvRecyclerView不再处理焦点移动, 否则用户不再处理焦点移动，交给TvRecyclerView处理焦点移动
         */
        boolean onReviseFocusFollow(TvRecyclerView parent, View itemView, int position);

        void onItemClick(TvRecyclerView parent, View itemView, int position);

        boolean onItemLongClick(TvRecyclerView parent, View itemView, int position);
    }

    private interface ItemListener extends OnClickListener, OnFocusChangeListener, OnLongClickListener {
    }


    private OnItemListener mOnItemListener;

    public void setOnItemListener(OnItemListener mOnItemListener) {
        this.mOnItemListener = mOnItemListener;
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(0));
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        /**
         * 重写这个方法，可以控制焦点框距离父容器的距离,以及由于recyclerView的滚动，产生的偏移量，导致焦点框错位，这里可以记录滑动偏移量。
         */

        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();


        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();


        final int offScreenLeft = Math.min(0, childLeft - parentLeft);
        final int offScreenTop = Math.min(0, childTop - parentTop);
        final int offScreenRight = Math.max(0, childRight - parentRight);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);

        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }
        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        int dy = offScreenTop != 0 ? offScreenTop
                : Math.min(childTop - parentTop, offScreenBottom);
        mOffset = isVertical() ? dy : dx;
        if (dx != 0 || dy != 0) {
            if (dx > 0) {
                dx = dx + getRightEdge();
            } else {
                dx = dx - getLeftEdge();
            }

            if (dy > 0) {
                dy = dy + getUpEdge();
            } else {
                dy = dy - getDownEdge();
            }
            mOffset = isVertical() ? dy : dx;
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            return true;
        }
        postInvalidate();
        return false;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        //用来微调位置
        if (RecyclerView.SCROLL_STATE_IDLE == state) {
            mOffset = -1;
            boolean result = mOnItemListener.onReviseFocusFollow(this, getFocusedChild(), getChildLayoutPosition(getFocusedChild()));
            if (!result) {
                mRecyclerViewEffect.setFocusView(getFocusedChild(), mScale);
            }
        }
    }

    private int mOffset;

    /**
     * 获取选中ITEM的滚动偏移量
     *
     * @return 预选中的item由于recyclerView的滚动，产生的偏移量，主要用于修正焦点框
     */
    public int getSelectedItemScrollOffset() {
        return mOffset;
    }

    public boolean isVertical() {
        LayoutManager lm = getLayoutManager();
        if (lm instanceof GridLayoutManager) {
            return ((GridLayoutManager) getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL;
        }
        if (lm instanceof LinearLayoutManager) {
            LinearLayoutManager llm = (LinearLayoutManager) lm;
            return llm.getOrientation() == LinearLayoutManager.VERTICAL;
        }
        if (lm instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager sglm = (StaggeredGridLayoutManager) lm;
            return sglm.getOrientation() == StaggeredGridLayoutManager.VERTICAL;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            recoverFocus();
        }
    }

    private void recoverFocus() {
        View saveFocusView = getLayoutManager().findViewByPosition(mSaveFocusPosition);
        if (saveFocusView != null) {
            saveFocusView.requestFocus();
        }
    }

    /**
     * 做了一些焦点记录处理，焦点记录处理只在一下四个方法做了处理
     * 若是需要更新数据，请使用notifyItemRangeXXX()的方法，
     * 否则更新后，可能会出现焦点框位置错乱.
     */
    public static abstract class TvAdapter extends Adapter<TvViewHolder> {
        protected RecyclerView.AdapterDataObserver mDataObserver;
        protected TvRecyclerView mRecyclerView;

        public TvAdapter() {
            mDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                }

                @Override
                public void onItemRangeInserted(final int positionStart, int itemCount) {
                    registerRecoverFocus(positionStart);
                    super.onItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    registerRecoverFocus(positionStart - itemCount);
                    super.onItemRangeRemoved(positionStart, itemCount);
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    registerRecoverFocus(toPosition);
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    registerRecoverFocus(positionStart);
                    super.onItemRangeChanged(positionStart, itemCount);
                }
            };

            registerAdapterDataObserver(mDataObserver);
        }

        /**
         * 注册事件，更新完毕RecyclerView重新获取焦点
         *
         * @param focusPosition 恢复焦点的位置
         */
        private void registerRecoverFocus(final int focusPosition) {
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View addView = mRecyclerView.getLayoutManager().findViewByPosition(focusPosition);
                    if (addView != null) {
                        addView.requestFocus();
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            mRecyclerView = (TvRecyclerView) recyclerView;
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            if (mDataObserver != null) {
                unregisterAdapterDataObserver(mDataObserver);
            }
        }

        /**
         * 由于在StaggeredGridLayout布局中，更新完数据后RecyclerView有时候会发生滑动，导致焦点框错位，所以使用这个方法更新数据
         *
         * @param start   开始位置
         * @param count   更新数据的数目
         * @param payLoad 是否全部更新
         */
        public void notifyItemRangeChangedWrapper(int start, int count, Object payLoad) {
            if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                for (int i = start; i < start + count; i++) {
                    ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                    if (holder != null) {
                        Object data = getData(i);
                        if (data != null) {
                            //TvViewHolder的子类，重写setData(data)方法,进行相应的数据更新
                            ((TvViewHolder) holder).setData(data);
                        }
                    }
                }
            } else {
                notifyItemRangeChanged(start, count, payLoad);
            }
        }

        public void notifyItemRangeChangedWrapper(int start, int count) {
            notifyItemRangeChangedWrapper(start, count, null);
        }

        public void notifyItemChangedWrapper(int position, Object payLoad) {
            notifyItemRangeChangedWrapper(position, 1, payLoad);
        }

        public void notifyItemChangedWrapper(int position) {
            notifyItemRangeChangedWrapper(position, 1, null);
        }

        /**
         * 用来获取需要更新的数据
         * @param start 当前更新的位置
         * @return 当前更新的数据
         */
        protected abstract Object getData(int start);
    }

    public static class TvViewHolder extends ViewHolder {
        protected View mContainer;

        public TvViewHolder(View itemView) {
            super(itemView);
            mContainer = itemView;
        }

        public void setData(Object obj) {

        }

        @SuppressWarnings("unchecked")
        public <K extends View> K getView(int resId) {
            return (K) mContainer.findViewById(resId);
        }
    }

    /**
     * 必须执行此方法
     *
     * @param view         焦点框VIew
     * @param frameResId   焦点框图片效果资源id
     * @param framePadding 可以缩放焦点框
     */
    public void initFrame(FocusFrameView view, int frameResId, int framePadding) {
        mFocusFrameView = view;
        mRecyclerViewEffect = new RecyclerViewEffect();
        mFocusFrameView.setEffect(mRecyclerViewEffect);
        mRecyclerViewEffect.setFocusResource(frameResId);
        mFocusFrameView.setFocusFramePadding(framePadding);
    }

    public void initFrame(FocusFrameView view, Drawable frameDrawable, int framePadding) {
        mFocusFrameView = view;
        mRecyclerViewEffect = new RecyclerViewEffect();
        mFocusFrameView.setEffect(mRecyclerViewEffect);
        mRecyclerViewEffect.setFocusDrawable(frameDrawable);
        mFocusFrameView.setFocusFramePadding(framePadding);
    }

}
