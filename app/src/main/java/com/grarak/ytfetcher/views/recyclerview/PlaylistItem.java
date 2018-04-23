package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistName;

public class PlaylistItem extends RecyclerViewItem<RecyclerView.ViewHolder> {

    private PlaylistName playlistName;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public PlaylistItem(PlaylistName playlistName,
                        CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.playlistName = playlistName;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_playlist;
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View inflatedView) {
        return new RecyclerView.ViewHolder(inflatedView) {
        };
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        TextView title = viewHolder.itemView.findViewById(R.id.title);
        SwitchCompat switchCompat = viewHolder.itemView.findViewById(R.id.public_switch);

        title.setText(playlistName.name);
        switchCompat.setChecked(playlistName.isPublic);

        switchCompat.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
