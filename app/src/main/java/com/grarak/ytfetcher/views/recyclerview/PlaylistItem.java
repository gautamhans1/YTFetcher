package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.playlist.Playlist;

public class PlaylistItem extends RecyclerViewItem<RecyclerView.ViewHolder> {

    public interface PlaylistListener {
        void onClick(PlaylistItem item);

        void onPublic(PlaylistItem item, boolean isPublic);

        void onDelete(PlaylistItem item);
    }

    private SwitchCompat publicSwitch;
    private final Playlist playlist;
    private final PlaylistListener playlistListener;

    public PlaylistItem(Playlist playlist, PlaylistListener playlistListener) {
        this.playlist = playlist;
        this.playlistListener = playlistListener;
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

    private CompoundButton.OnCheckedChangeListener publicSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            playlistListener.onPublic(PlaylistItem.this, isChecked);
        }
    };

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        TextView title = viewHolder.itemView.findViewById(R.id.title);
        publicSwitch = viewHolder.itemView.findViewById(R.id.public_switch);
        View menu = viewHolder.itemView.findViewById(R.id.menu);

        title.setText(playlist.name);
        publicSwitch.setChecked(playlist.isPublic);

        viewHolder.itemView.setOnClickListener(v -> playlistListener.onClick(this));
        publicSwitch.setOnCheckedChangeListener(publicSwitchListener);

        viewHolder.itemView.setOnLongClickListener(v -> {
            menu.performLongClick();
            return true;
        });
        menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu1 = popupMenu.getMenu();
            menu1.add(0, 0, 0, R.string.delete);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        playlistListener.onDelete(this);
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    public void setPublic(boolean isPublic) {
        publicSwitch.setOnCheckedChangeListener(null);
        publicSwitch.setChecked(isPublic);
        publicSwitch.setOnCheckedChangeListener(publicSwitchListener);
    }
}
