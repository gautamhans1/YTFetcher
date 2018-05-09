package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

public abstract class RecyclerViewItem {

    @LayoutRes
    protected abstract int getLayoutXml();

    protected abstract void bindViewHolder(RecyclerView.ViewHolder viewHolder);
}
