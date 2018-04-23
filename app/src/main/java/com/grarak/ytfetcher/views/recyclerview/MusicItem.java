package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class MusicItem extends RecyclerViewItem<MusicItem.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView thumbnail;
        private final TextView title;
        private final TextView summary;
        private final AppCompatImageView menu;

        private ViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            menu = itemView.findViewById(R.id.menu);
        }
    }

    private YoutubeSearchResult result;
    private View.OnClickListener onClickListener;

    public MusicItem(YoutubeSearchResult result, View.OnClickListener onClickListener) {
        this.result = result;
        this.onClickListener = onClickListener;
    }

    @Override
    protected int getLayoutXml() {
        return 0;
    }

    @Override
    protected ViewHolder createViewHolder(View inflatedView) {
        return new ViewHolder(inflatedView);
    }

    @Override
    protected void bindViewHolder(ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.title.setText(result.title);
        viewHolder.summary.setText(result.duration);

        Glide.with(viewHolder.thumbnail)
                .load(result.thumbnail)
                .into(viewHolder.thumbnail);
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
                    grid ? R.layout.item_music_thumbnail_grid :
                            R.layout.item_music_thumbnail, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }
}
