package com.grarak.ytfetcher.service;

import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public interface DownloadListener {
    void onFinish(YoutubeSearchResult result);

    void onProgress(YoutubeSearchResult result, int progress);
}
