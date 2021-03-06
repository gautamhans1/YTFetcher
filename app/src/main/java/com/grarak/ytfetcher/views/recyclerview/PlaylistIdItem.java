package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class PlaylistIdItem extends RecyclerViewItem {
    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final View downloaded;
        private final AppCompatImageView menu;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            downloaded = itemView.findViewById(R.id.downloaded_text);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    public interface PlaylistLinkListener {
        void onClick(PlaylistIdItem item);

        void onRemoveFromPlaylist(PlaylistIdItem item);

        void onDelete(PlaylistIdItem item);

        void onDownload(PlaylistIdItem item);

        void onMoveUp(PlaylistIdItem item);

        void onMoveDown(PlaylistIdItem item);
    }

    private ViewHolder viewHolder;

    public final YoutubeSearchResult result;
    private final PlaylistLinkListener playlistLinkListener;
    private final boolean readOnly;

    public PlaylistIdItem(YoutubeSearchResult result,
                          PlaylistLinkListener playlistLinkListener,
                          boolean readOnly) {
        this.result = result;
        this.playlistLinkListener = playlistLinkListener;
        this.readOnly = readOnly;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_playlist_id;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = (ViewHolder) viewHolder;
        this.viewHolder.title.setText(result.title);

        viewHolder.itemView.setOnClickListener(v -> playlistLinkListener.onClick(this));

        this.viewHolder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            if (!readOnly) {
                menu.add(0, 0, 0, R.string.remove_from_playlist);
            }
            if (this.viewHolder.downloaded.getVisibility() == View.VISIBLE) {
                menu.add(0, 1, 0, R.string.delete);
            } else {
                menu.add(0, 2, 0, R.string.download);
            }
            if (!readOnly) {
                menu.add(0, 3, 0, R.string.move_up);
                menu.add(0, 4, 0, R.string.move_down);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        playlistLinkListener.onRemoveFromPlaylist(this);
                        return true;
                    case 1:
                        playlistLinkListener.onDelete(this);
                        return true;
                    case 2:
                        playlistLinkListener.onDownload(this);
                        return true;
                    case 3:
                        playlistLinkListener.onMoveUp(this);
                        return true;
                    case 4:
                        playlistLinkListener.onMoveDown(this);
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
                    viewHolder.downloaded.getContext()).exists() ? View.VISIBLE : View.GONE);
        }
    }

    public static class Adapter extends RecyclerViewAdapter {

        public Adapter(List<RecyclerViewItem> items) {
            super(items);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(
                    R.layout.item_playlist_id, parent, false)) {
            };
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }
}
