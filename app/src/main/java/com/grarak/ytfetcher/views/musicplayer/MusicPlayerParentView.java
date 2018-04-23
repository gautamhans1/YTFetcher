package com.grarak.ytfetcher.views.musicplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.MusicManager;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

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

    public void onFetch(YoutubeSearchResult result) {
        headerView.onFetch(result);
        playerView.onFetch(result);
    }

    public void onFailure(YoutubeSearchResult result) {
        headerView.onFailure(result);
        playerView.onFailure(result);
    }

    public void onPlay(YoutubeSearchResult result) {
        headerView.onPlay(result);
        playerView.onPlay(result);
    }

    public void onPause(YoutubeSearchResult result) {
        headerView.onPause(result);
        playerView.onPause(result);
    }

    public void onNoMusic() {
        headerView.onNoMusic();
        playerView.onNoMusic();
    }
}
