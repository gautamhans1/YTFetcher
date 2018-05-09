package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public class MusicItem extends RecyclerViewItem {

    public interface MusicListener {
        void onClick(MusicItem musicItem);

        void onAddPlaylist(MusicItem musicItem);

        void onDelete(MusicItem musicItem);

        void onDownload(MusicItem musicItem);
    }

    private View downloaded;

    public final YoutubeSearchResult result;
    private final MusicListener musicListener;
    private final boolean grid;

    public MusicItem(YoutubeSearchResult result,
                     MusicListener musicListener,
                     boolean grid) {
        this.result = result;
        this.musicListener = musicListener;
        this.grid = grid;
    }

    @Override
    protected int getLayoutXml() {
        return grid ? R.layout.item_music_grid : R.layout.item_music;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ImageView thumbnail = viewHolder.itemView.findViewById(R.id.thumbnail);
        downloaded = viewHolder.itemView.findViewById(R.id.downloaded_text);
        TextView title = viewHolder.itemView.findViewById(R.id.title);
        TextView summary = viewHolder.itemView.findViewById(R.id.summary);
        AppCompatImageView menu = viewHolder.itemView.findViewById(R.id.menu);

        viewHolder.itemView.setOnClickListener(v -> musicListener.onClick(this));
        viewHolder.itemView.setOnLongClickListener(v -> {
            menu.performClick();
            return true;
        });
        title.setText(result.title);
        summary.setText(result.duration);

        Glide.with(thumbnail)
                .load(result.thumbnail)
                .into(thumbnail);

        menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu1 = popupMenu.getMenu();
            menu1.add(0, 0, 0, R.string.add_playlist);
            if (downloaded.getVisibility() == View.VISIBLE) {
                menu1.add(0, 1, 0, R.string.delete);
            } else {
                menu1.add(0, 2, 0, R.string.download);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        musicListener.onAddPlaylist(this);
                        return true;
                    case 1:
                        musicListener.onDelete(this);
                        return true;
                    case 2:
                        musicListener.onDownload(this);
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        setDownloaded();
    }

    public void setDownloaded() {
        if (downloaded != null) {
            downloaded.setVisibility(result.getDownloadPath(
                    downloaded.getContext()).exists() ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
