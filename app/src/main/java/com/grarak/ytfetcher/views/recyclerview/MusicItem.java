package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Log;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class MusicItem extends RecyclerViewItem<MusicItem.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final View downloaded;
        private final TextView title;
        private final TextView summary;
        private final AppCompatImageView menu;

        private ViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            downloaded = itemView.findViewById(R.id.downloaded_text);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    public interface MusicListener {
        void onClick(MusicItem musicItem);

        void onAddPlaylist(MusicItem musicItem);

        void onDelete(MusicItem musicItem);

        void onDownload(MusicItem musicItem);
    }

    public YoutubeSearchResult result;
    private MusicListener musicListener;
    private ViewHolder viewHolder;

    public MusicItem(YoutubeSearchResult result, MusicListener musicListener) {
        this.result = result;
        this.musicListener = musicListener;
    }

    @Override
    protected int getLayoutXml() {
        return 0;
    }

    @Override
    protected ViewHolder createViewHolder(View inflatedView) {
        return null;
    }

    @Override
    protected void bindViewHolder(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;

        viewHolder.itemView.setOnClickListener(v -> musicListener.onClick(this));
        viewHolder.itemView.setOnLongClickListener(v -> {
            viewHolder.menu.performClick();
            return true;
        });
        viewHolder.title.setText(result.title);
        viewHolder.summary.setText(result.duration);

        Glide.with(viewHolder.thumbnail)
                .load(result.thumbnail)
                .into(viewHolder.thumbnail);

        viewHolder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, 0, R.string.add_playlist);
            if (viewHolder.downloaded.getVisibility() == View.VISIBLE) {
                menu.add(0, 1, 0, R.string.delete);
            } else {
                menu.add(0, 2, 0, R.string.download);
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
        if (viewHolder != null) {
            viewHolder.downloaded.setVisibility(result.getDownloadPath(
                    viewHolder.itemView.getContext()).exists() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public static class MusicAdapter extends RecyclerViewAdapter<ViewHolder> {

        private boolean grid;

        public MusicAdapter(List<RecyclerViewItem<ViewHolder>> recyclerViewItems,
                            boolean grid) {
            super(recyclerViewItems);
            this.grid = grid;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            return new ViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(
                    grid ? R.layout.item_music_grid :
                            R.layout.item_music, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }
}
