package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.RecyclerView;

import com.grarak.ytfetcher.R;

public class ProgressItem extends RecyclerViewItem {

    public interface ProgressListener {
        void onBind();
    }

    private final ProgressListener progressListener;

    public ProgressItem(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_progress;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        progressListener.onBind();
    }
}
