package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class RecyclerViewItem<VH extends RecyclerView.ViewHolder> {

    protected abstract @LayoutRes
    int getLayoutXml();

    protected abstract VH createViewHolder(View inflatedView);

    protected abstract void bindViewHolder(VH viewHolder);
}
