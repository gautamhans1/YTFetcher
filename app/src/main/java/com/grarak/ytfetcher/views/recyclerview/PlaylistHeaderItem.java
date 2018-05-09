package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.RecyclerView;

import com.grarak.ytfetcher.R;

public class PlaylistHeaderItem extends RecyclerViewItem {

    @Override
    protected int getLayoutXml() {
        return R.layout.item_playlist_header;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
    }
}
