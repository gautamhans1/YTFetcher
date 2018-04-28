package com.grarak.ytfetcher.views.musicplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.MusicManager;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class MusicPlayerParentView extends FrameLayout {

    private MusicPlayerHeaderView headerView;
    private MusicPlayerView playerView;

    public MusicPlayerParentView(Context context) {
        this(context, null);
    }

    public MusicPlayerParentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicPlayerParentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MusicPlayerParentView, defStyleAttr, 0);
        int collapsedHeight = a.getDimensionPixelSize(R.styleable.MusicPlayerParentView_collapsedHeight,
                getResources().getDimensionPixelSize(R.dimen.musicview_height));

        a.recycle();

        playerView = new MusicPlayerView(context);
        addView(playerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        headerView = new MusicPlayerHeaderView(context);
        addView(headerView, LayoutParams.MATCH_PARENT, collapsedHeight);
    }

    public void setMusicManager(MusicManager musicManager) {
        headerView.setMusicManager(musicManager);
        playerView.setMusicManager(musicManager);
    }

    public void setCollapsed(boolean collapsed) {
        headerView.setVisibility(collapsed ? VISIBLE : INVISIBLE);
    }

    public void onFetch(List<YoutubeSearchResult> results, int position) {
        headerView.onFetch(results, position);
        playerView.onFetch(results, position);
    }

    public void onFailure(List<YoutubeSearchResult> results, int position) {
        headerView.onFailure(results, position);
        playerView.onFailure(results, position);
    }

    public void onPlay(List<YoutubeSearchResult> results, int position) {
        headerView.onPlay(results, position);
        playerView.onPlay(results, position);
    }

    public void onPause(List<YoutubeSearchResult> results, int position) {
        headerView.onPause(results, position);
        playerView.onPause(results, position);
    }

    public void onNoMusic() {
        headerView.onNoMusic();
        playerView.onNoMusic();
    }
}
