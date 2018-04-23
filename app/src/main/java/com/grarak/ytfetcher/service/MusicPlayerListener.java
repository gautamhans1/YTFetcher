package com.grarak.ytfetcher.service;

import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public interface MusicPlayerListener {
    void onConnected();

    void onFetchingSong(YoutubeSearchResult result);

    void onFailure(YoutubeSearchResult result);

    void onPlay(YoutubeSearchResult result);

    void onPause(YoutubeSearchResult result);
}
