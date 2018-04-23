package com.grarak.ytfetcher.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewAdapter;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerViewFragment<VH extends RecyclerView.ViewHolder, TF extends BaseFragment>
        extends BaseFragment {

    private List<RecyclerViewItem<VH>> items = new ArrayList<>();
    private RecyclerViewAdapter<VH> adapter;

    private BottomNavigationView bottomNavigationView;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView messageView;
    private View progressView;
    private int firstVisibleItem;

    private int progressCount;

    private TF titleFragment;
    private View titleContent;

    protected @LayoutRes
    int getLayoutXml() {
        return R.layout.fragment_recyclerview;
    }

    protected RecyclerViewAdapter<VH> createAdapter() {
        return new RecyclerViewAdapter<>(items);
    }

    protected abstract LinearLayoutManager createLayoutManager();

    private OnScrollListener onScrollListener = new OnScrollListener();

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        private int scrollDistance;
        private int bottomTranslation;

        private int getScrollDistance() {
            return recyclerView.computeVerticalScrollOffset();
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            scrollDistance = getScrollDistance();
            bottomTranslation += dy;
            setTranslations();

            firstVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
        }

        private void setTranslations() {
            titleContent.setTranslationY(-scrollDistance / 2);

            if (bottomNavigationView == null) {
                return;
            }
            if (bottomTranslation > bottomNavigationView.getHeight()) {
                bottomTranslation = bottomNavigationView.getHeight();
            } else if (bottomTranslation < 0) {
                bottomTranslation = 0;
            }
            bottomNavigationView.setTranslationY(bottomTranslation);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (bottomNavigationView == null || newState != 0 || bottomTranslation == 0) {
                return;
            }

            animateBottomNavigation(
                    layoutManager.findLastVisibleItemPosition() == itemsSize() - 1
                            || bottomTranslation < bottomNavigationView.getHeight() * 0.5f);
        }

        private void animateBottomNavigation(boolean show) {
            ValueAnimator animator = ValueAnimator.ofInt(bottomTranslation,
                    show ? 0 : bottomNavigationView.getHeight());
            animator.addUpdateListener(animation -> {
                bottomTranslation = (int) animation.getAnimatedValue();
                if (bottomNavigationView != null) {
                    bottomNavigationView.setTranslationY(bottomTranslation);
                }
            });
            animator.start();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        bottomNavigationView = getBottomNavigationView();

        View rootView = inflater.inflate(getLayoutXml(), container, false);

        recyclerView = rootView.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        messageView = rootView.findViewById(R.id.message);
        progressView = rootView.findViewById(R.id.progress);
        messageView.setText(getEmptyViewsMessage());

        titleContent = rootView.findViewById(R.id.content_title);
        titleFragment = getTitleFragment();
        if (titleFragment == null && getTitleFragmentClass() != null) {
            titleFragment = (TF) Fragment.instantiate(getActivity(), getTitleFragmentClass().getName());
        }
        if (titleFragment != null) {
            setUpTitleFragment(titleFragment);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.content_title, titleFragment, "title_fragment").commit();
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("progress_visible")) {
                showProgress();
            } else {
                dismissProgress();
            }
        }

        init(savedInstanceState);
        if (items.size() == 0) {
            initItems(items);
        }
        messageView.setVisibility(
                progressView.getVisibility() == View.INVISIBLE &&
                        itemsSize() == 0 ? View.VISIBLE : View.INVISIBLE);

        recyclerView.setLayoutManager(layoutManager = createLayoutManager());
        recyclerView.setAdapter(adapter = createAdapter());
        return rootView;
    }

    @Override
    public void onViewFinished() {
        int leftPadding = recyclerView.getPaddingLeft();
        int rightPadding = recyclerView.getPaddingRight();
        if (Utils.isLandscape(getActivity())) {
            leftPadding = getResources().getDimensionPixelSize(R.dimen.recyclerview_padding);
            rightPadding = leftPadding;
        }

        recyclerView.setPadding(
                leftPadding,
                recyclerView.getPaddingTop() + titleContent.getHeight(),
                rightPadding,
                recyclerView.getPaddingBottom()
        );

        recyclerView.scrollToPosition(firstVisibleItem);
        recyclerView.addOnScrollListener(onScrollListener);

        recyclerView.setOnTouchListener((v, event) -> {
            if (progressView.getVisibility() == View.INVISIBLE) {
                titleContent.dispatchTouchEvent(event);
            }
            return false;
        });
    }

    protected TF getTitleFragment() {
        return (TF) getChildFragmentManager().findFragmentByTag("title_fragment");
    }

    protected Class<TF> getTitleFragmentClass() {
        return null;
    }

    protected void setUpTitleFragment(TF fragment) {
    }

    protected abstract void init(Bundle savedInstanceState);

    protected abstract void initItems(List<RecyclerViewItem<VH>> items);

    protected void addItem(RecyclerViewItem<VH> item) {
        items.add(item);
        if (adapter != null) {
            adapter.notifyItemInserted(items.size() - 1);
        }
        messageView.setVisibility(View.INVISIBLE);
    }

    protected void clearItems() {
        items.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        messageView.setVisibility(View.VISIBLE);
    }

    public void showProgress() {
        synchronized (this) {
            progressCount++;
            recyclerView.setVisibility(View.INVISIBLE);
            messageView.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.VISIBLE);
        }
    }

    public void dismissProgress() {
        synchronized (this) {
            progressCount--;
            if (progressCount <= 0) {
                recyclerView.setVisibility(View.VISIBLE);
                messageView.setVisibility(
                        progressView.getVisibility() == View.INVISIBLE &&
                                itemsSize() == 0 ? View.VISIBLE : View.INVISIBLE);
                progressView.setVisibility(View.INVISIBLE);
                progressCount = 0;
            }
        }
    }

    protected List<RecyclerViewItem<VH>> getItems() {
        return items;
    }

    public int itemsSize() {
        return items.size();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("progress_visible", progressView.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onViewPagerResume() {
        super.onViewPagerResume();

        if (titleFragment != null) {
            titleFragment.onViewPagerResume();
        }
    }

    @Override
    public void onViewPagerPause() {
        super.onViewPagerPause();

        onScrollListener.animateBottomNavigation(true);
        if (titleFragment != null) {
            titleFragment.onViewPagerPause();
        }
    }

    @Override
    public boolean onBackPressed() {
        return titleFragment != null && titleFragment.onBackPressed();
    }

    protected String getEmptyViewsMessage() {
        return null;
    }
}
