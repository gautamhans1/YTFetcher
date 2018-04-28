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

public class PlaylistIdItem extends RecyclerViewItem<PlaylistIdItem.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private AppCompatImageView menu;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    public interface PlaylistLinkListener {
        void onClick();

        void onDelete();
    }

    private final YoutubeSearchResult result;
    private final PlaylistLinkListener playlistLinkListener;

    public PlaylistIdItem(YoutubeSearchResult result,
                          PlaylistLinkListener playlistLinkListener) {
        this.result = result;
        this.playlistLinkListener = playlistLinkListener;
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
        viewHolder.title.setText(result.title);

        viewHolder.itemView.setOnClickListener(v -> playlistLinkListener.onClick());

        viewHolder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, 0, R.string.delete);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        playlistLinkListener.onDelete();
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    public static class Adapter extends RecyclerViewAdapter<ViewHolder> {

        public Adapter(List<RecyclerViewItem<ViewHolder>> recyclerViewItems) {
            super(recyclerViewItems);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            return new ViewHolder(LayoutInflater.from(
                    parent.getContext()).inflate(
                    R.layout.item_playlist_id, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

}
