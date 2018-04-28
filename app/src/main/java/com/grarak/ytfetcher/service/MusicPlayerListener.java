package com.grarak.ytfetcher.service;

import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public interface MusicPlayerListener {
    void onConnected();

    void onFetchingSong(List<YoutubeSearchResult> results, int position);

    void onFailure(List<YoutubeSearchResult> results, int position);

    void onPlay(List<YoutubeSearchResult> results, int position);

    void onPause(List<YoutubeSearchResult> results, int position);
}
