package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.grarak.ytfetcher.R;

public class PlaylistHeaderItem extends RecyclerViewItem<RecyclerView.ViewHolder> {

    @Override
    protected int getLayoutXml() {
        return R.layout.item_playlist_header;
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View inflatedView) {
        return new RecyclerView.ViewHolder(inflatedView) {
        };
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
    }
}
