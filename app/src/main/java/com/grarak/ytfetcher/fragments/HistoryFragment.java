package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.grarak.ytfetcher.MainActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.history.HistoryServer;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;
import com.grarak.ytfetcher.views.recyclerview.MusicItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends RecyclerViewFragment<TitleFragment> {

    private HistoryServer server;

    public static HistoryFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.USER_INTENT, user);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.fragment_history;
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (server != null) return;

        showProgress();
        server = new HistoryServer(getActivity());
        server.get(getUser().apikey, new HistoryServer.HistoryCallback() {
            @Override
            public void onSuccess(List<String> history) {
                if (history.size() == 0) {
                    dismissProgress();
                    Utils.toast(R.string.no_history, getActivity());
                    return;
                }

                YoutubeServer server = new YoutubeServer(getActivity());
                Map<String, YoutubeSearchResult> results = new HashMap<>();
                for (String id : history) {
                    Youtube youtube = new Youtube();
                    youtube.apikey = getUser().apikey;
                    youtube.id = id;

                    server.getInfo(youtube, new YoutubeServer.YoutubeResultCallback() {
                        @Override
                        public void onSuccess(YoutubeSearchResult result) {
                            synchronized (results) {
                                results.put(id, result);

                                if (results.size() == history.size()) {
                                    if (!isAdded()) return;

                                    for (String id : history) {
                                        addItem(new MusicItem(results.get(id), new MusicItem.MusicListener() {
                                            @Override
                                            public void onClick(MusicItem musicItem) {
                                                getMusicManager().play(musicItem.result);
                                            }

                                            @Override
                                            public void onAddPlaylist(MusicItem musicItem) {
                                                showPlaylistDialog(musicItem.result);
                                            }

                                            @Override
                                            public void onDelete(MusicItem musicItem) {
                                                if (deleteResult(musicItem.result)) {
                                                    musicItem.setDownloaded();
                                                }
                                            }

                                            @Override
                                            public void onDownload(MusicItem musicItem) {
                                                queueDownload(musicItem.result);
                                            }
                                        }, false));
                                    }

                                    dismissProgress();
                                }
                            }
                        }

                        @Override
                        public void onFailure(int code) {
                            failure();
                            server.close();
                        }
                    });
                }
            }

            @Override
            public void onFailure(int code) {
                failure();
            }

            private void failure() {
                if (!isAdded()) return;
                Utils.toast(R.string.server_offline, getActivity());
                removeForegroundFragment(HistoryFragment.this);
            }
        });
    }

    @Override
    protected void initItems(List<RecyclerViewItem> items) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.close();
        }
    }
}
