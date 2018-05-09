package com.grarak.ytfetcher.views.recyclerview;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public class DownloadItem extends RecyclerViewItem {
    public interface DownloadListener {
        void onClick(DownloadItem item);

        void onLongClick(DownloadItem item);

        void onDelete(DownloadItem item);
    }

    private final YoutubeSearchResult result;
    private final DownloadListener downloadListener;

    private RecyclerView.ViewHolder viewHolder;
    private boolean selected;

    public DownloadItem(YoutubeSearchResult result, DownloadListener downloadListener) {
        this.result = result;
        this.downloadListener = downloadListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_download;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;

        TextView text = viewHolder.itemView.findViewById(R.id.title);
        text.setText(result.title);

        viewHolder.itemView.findViewById(R.id.menu).setOnClickListener(v -> {
            if (selected) return;

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, 0, R.string.delete);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        downloadListener.onDelete(this);
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        viewHolder.itemView.setOnClickListener(v -> downloadListener.onClick(this));
        viewHolder.itemView.setOnLongClickListener(v -> {
            downloadListener.onLongClick(this);
            return true;
        });

        setup();
    }

    public void toogleSelection() {
        selected = !selected;
        setup();
    }

    private void setup() {
        if (viewHolder != null) {
            if (selected) {
                viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(
                        viewHolder.itemView.getContext(), R.color.semi_transparent));
            } else {
                viewHolder.itemView.setBackgroundColor(0);
            }
        }
    }
}
