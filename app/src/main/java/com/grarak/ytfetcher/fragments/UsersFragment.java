package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.playlist.Playlist;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistPublic;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistResults;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistServer;
import com.grarak.ytfetcher.utils.server.playlist.Playlists;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.user.UserServer;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;
import com.grarak.ytfetcher.views.recyclerview.ProgressItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;
import com.grarak.ytfetcher.views.recyclerview.UserItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersFragment extends RecyclerViewFragment<TitleFragment> {

    private UserServer userServer;
    private PlaylistServer playlistServer;
    private YoutubeServer youtubeServer;
    private int page;
    private boolean limitReached;
    private boolean loading;

    private User userPlaylist;
    private Playlists playlists;

    private final ProgressItem progressItem = new ProgressItem(this::loadNextPage);

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (userServer != null) return;

        userServer = new UserServer(getActivity());
        playlistServer = new PlaylistServer(getActivity());
        youtubeServer = new YoutubeServer(getActivity());
        loadNextPage();
    }

    @Override
    protected void initItems(List<RecyclerViewItem> recyclerViewItems) {
    }

    private void loadNextPage() {
        if (limitReached || loading) return;

        loading = true;
        userServer.list(getUser(), ++page, new UserServer.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (!isAdded()) return;

                removeItem(progressItem);
                if (users.size() == 0) {
                    limitReached = true;
                    return;
                }

                for (User user : users) {
                    addItem(new UserItem(getUser().admin, user, new UserItem.UserListener() {
                        @Override
                        public void onClick(UserItem item) {
                            if (user.name.equals(getUser().name)) return;

                            showProgress();

                            Playlist playlist = new Playlist();
                            playlist.apikey = getUser().apikey;
                            playlist.name = user.name;

                            playlistServer.listPublic(playlist, new PlaylistServer.PlaylistListCallback() {
                                @Override
                                public void onSuccess(Playlists playlists) {
                                    if (!isAdded()) return;
                                    dismissProgress();
                                    if (playlists.items.size() == 0) {
                                        Utils.toast(R.string.no_public_playlists, getActivity());
                                        return;
                                    }
                                    showPlaylists(user, playlists);
                                }

                                @Override
                                public void onFailure(int code) {
                                    if (!isAdded()) return;
                                    dismissProgress();
                                    Utils.toast(R.string.server_offline, getActivity());
                                }
                            });
                        }

                        @Override
                        public void onVerified(UserItem item, boolean verified) {
                            User newUser = new User();
                            newUser.apikey = getUser().apikey;
                            newUser.name = user.name;
                            newUser.verified = verified;

                            userServer.setVerification(newUser, new GenericCallback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onFailure(int code) {
                                    if (!isAdded()) return;
                                    item.setVerified(!verified);
                                }
                            });
                        }
                    }));
                }
                loading = false;
                addItem(progressItem);
            }

            @Override
            public void onFailure(int code) {
                if (!isAdded()) return;

                limitReached = true;
                removeItem(progressItem);
                Utils.toast(R.string.server_offline, getActivity());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (userPlaylist != null && playlists != null) {
            showPlaylists(userPlaylist, playlists);
        }
    }

    private void showPlaylists(User user, Playlists playlists) {
        userPlaylist = user;
        this.playlists = playlists;

        List<String> playlistNames = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistNames.add(playlist.name);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.public_playlists)
                .setItems(playlistNames.toArray(new String[playlistNames.size()]), (dialog, which) -> {
                    showProgress();

                    PlaylistPublic playlistPublic = new PlaylistPublic();
                    playlistPublic.apikey = getUser().apikey;
                    playlistPublic.name = user.name;
                    playlistPublic.playlist = playlistNames.get(which);
                    playlistServer.listPlaylistIdsPublic(playlistPublic, new PlaylistServer.PlayListIdsCallback() {
                        @Override
                        public void onSuccess(List<String> ids) {
                            if (ids.size() == 0) {
                                dismissProgress();
                                Utils.toast(R.string.no_songs, getActivity());
                                return;
                            }

                            Map<String, YoutubeSearchResult> results = new HashMap<>();
                            for (String id : ids) {
                                Youtube youtube = new Youtube();
                                youtube.apikey = playlistPublic.apikey;
                                youtube.id = id;

                                youtubeServer.getInfo(youtube, new YoutubeServer.YoutubeResultCallback() {
                                    @Override
                                    public void onSuccess(YoutubeSearchResult result) {
                                        synchronized (results) {
                                            results.put(id, result);

                                            if (results.size() == ids.size()) {
                                                if (!isAdded()) return;
                                                PlaylistResults playlistResults = new PlaylistResults();
                                                playlistResults.name = playlistPublic.playlist;
                                                playlistResults.songs = new ArrayList<>();
                                                for (String id : ids) {
                                                    playlistResults.songs.add(results.get(id));
                                                }

                                                playlistResults.save(getActivity());
                                                showPlaylist(playlistResults);
                                                dismissProgress();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(int code) {
                                        if (!isAdded()) return;

                                        youtubeServer.close();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(int code) {
                            if (!isAdded()) return;
                            dismissProgress();
                            Utils.toast(R.string.server_offline, getActivity());
                        }

                        private void showPlaylist(PlaylistResults playlistResults) {
                            showForegroundFragment(
                                    PlaylistIdsFragment.newInstance(getUser(), playlistResults, true));
                        }
                    });
                })
                .setOnDismissListener(dialog -> {
                    userPlaylist = null;
                    this.playlists = null;
                }).show();
    }

    @Override
    protected String getEmptyViewsMessage() {
        return getString(R.string.no_users);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (userServer != null) {
            userServer.close();
        }
    }
}
