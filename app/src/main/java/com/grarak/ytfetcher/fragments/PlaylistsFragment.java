package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.playlist.Playlist;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistResults;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistServer;
import com.grarak.ytfetcher.utils.server.playlist.Playlists;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;
import com.grarak.ytfetcher.views.recyclerview.PlaylistHeaderItem;
import com.grarak.ytfetcher.views.recyclerview.PlaylistItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistsFragment extends RecyclerViewFragment<AddFragment>
        implements AddFragment.OnConfirmListener {

    private PlaylistServer server;

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (server != null) return;

        server = new PlaylistServer(getActivity());
        fetchPlaylists();
    }

    private void fetchPlaylists() {
        showProgress();
        server.list(getUser().apikey, new PlaylistServer.PlaylistListCallback() {
            @Override
            public void onSuccess(Playlists playlists) {
                addViews(playlists);
            }

            @Override
            public void onFailure(int code) {
                addViews(Playlists.restore(getActivity()));
            }

            private void addViews(Playlists playlists) {
                if (!isAdded()) return;
                clearItems();
                dismissProgress();
                if (playlists == null) return;

                for (Playlist playlist : playlists) {
                    addItem(playlist);
                }
                playlists.save(getActivity());
            }
        });
    }

    private void addItem(Playlist playlist) {
        if (itemsSize() == 0) {
            addItem(new PlaylistHeaderItem());
        }

        addItem(new PlaylistItem(playlist, new PlaylistItem.PlaylistListener() {
            @Override
            public void onClick(PlaylistItem item) {
                showPlaylist(playlist);
            }

            @Override
            public void onPublic(PlaylistItem item, boolean isPublic) {
                playlist.apikey = getUser().apikey;
                playlist.isPublic = isPublic;

                server.setPublic(playlist, new GenericCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int code) {
                        if (!isAdded()) return;
                        item.setPublic(!isPublic);
                    }
                });
            }

            @Override
            public void onDelete(PlaylistItem item) {
                playlist.apikey = getUser().apikey;
                server.delete(playlist, new GenericCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        removeItem(item);

                        if (getItems().size() == 1) {
                            clearItems();
                        }
                    }

                    @Override
                    public void onFailure(int code) {
                        Utils.toast(R.string.server_offline, getActivity());
                    }
                });
            }
        }));

        List<String> playlists = new ArrayList<>();
        for (RecyclerViewItem item : getItems()) {
            if (item instanceof PlaylistItem) {
                playlists.add(((PlaylistItem) item).playlist.name);
            }
        }
        setAvailablePlaylists(playlists);
    }

    @Override
    protected void initItems(List<RecyclerViewItem> recyclerViewItems) {
    }

    @Override
    public void onConfirm(CharSequence text) {
        if (text.toString().isEmpty()) return;

        Playlist playlist = new Playlist();
        playlist.name = text.toString();
        playlist.apikey = getUser().apikey;

        showProgress();
        server.create(playlist, new GenericCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                dismissProgress();
                addItem(playlist);
            }

            @Override
            public void onFailure(int code) {
                if (!isAdded()) return;
                dismissProgress();
                if (code == Status.PlaylistIdAlreadyExists) {
                    Utils.toast(R.string.playlist_already_exists, getActivity());
                } else {
                    Utils.toast(R.string.server_offline, getActivity());
                }
            }
        });
    }

    private void showPlaylist(Playlist playlist) {
        showProgress();

        playlist.apikey = getUser().apikey;
        server.listPlaylistIds(playlist, new PlaylistServer.PlayListIdsCallback() {
            @Override
            public void onSuccess(List<String> ids) {
                if (ids.size() == 0) {
                    dismissProgress();
                    Utils.toast(R.string.no_songs, getActivity());
                    return;
                }

                YoutubeServer server = new YoutubeServer(getActivity());
                Map<String, YoutubeSearchResult> results = new HashMap<>();
                for (String id : ids) {
                    Youtube youtube = new Youtube();
                    youtube.apikey = playlist.apikey;
                    youtube.id = id;

                    server.getInfo(youtube, new YoutubeServer.YoutubeResultCallback() {
                        @Override
                        public void onSuccess(YoutubeSearchResult result) {
                            synchronized (results) {
                                results.put(id, result);

                                if (results.size() == ids.size()) {
                                    if (!isAdded()) return;
                                    PlaylistResults playlistResults = new PlaylistResults();
                                    playlistResults.name = playlist.name;
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

                PlaylistResults playlistResults = PlaylistResults.restore(playlist.name, getActivity());
                if (playlistResults != null) {
                    showPlaylist(playlistResults);
                } else {
                    Utils.toast(R.string.server_offline, getActivity());
                }
                dismissProgress();
            }

            private void showPlaylist(PlaylistResults playlistResults) {
                showForegroundFragment(
                        PlaylistIdsFragment.newInstance(getUser(), playlistResults, false));
            }
        });
    }

    @Override
    public void onRemoveForeground() {
        super.onRemoveForeground();
        fetchPlaylists();
    }

    @Override
    protected Class<AddFragment> getTitleFragmentClass() {
        return AddFragment.class;
    }

    @Override
    protected void setUpTitleFragment(AddFragment fragment) {
        super.setUpTitleFragment(fragment);
        fragment.setText(getString(R.string.your_playlists));
        fragment.setHint(getString(R.string.name));
        fragment.setOnConfirmListener(this);
    }

    @Override
    protected String getEmptyViewsMessage() {
        return getString(R.string.no_playlists);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.close();
        }
    }
}
