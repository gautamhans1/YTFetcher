package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistName;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistNamesList;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistServer;
import com.grarak.ytfetcher.views.recyclerview.PlaylistHeaderItem;
import com.grarak.ytfetcher.views.recyclerview.PlaylistItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends RecyclerViewFragment<RecyclerView.ViewHolder, AddFragment>
        implements AddFragment.OnConfirmListener {

    private List<String> playlists = new ArrayList<>();
    private PlaylistServer server;

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (server != null) return;

        server = new PlaylistServer(getActivity());

        showProgress();
        server.listNames(getUser().apikey, new PlaylistServer.PlaylistNameListCallback() {
            @Override
            public void onSuccess(PlaylistNamesList playlistNamesList) {
                addViews(playlistNamesList);
            }

            @Override
            public void onFailure(int code) {
                addViews(PlaylistNamesList.restore(getActivity()));
            }

            private void addViews(PlaylistNamesList playlistNamesList) {
                if (!isAdded()) return;
                dismissProgress();
                if (playlistNamesList == null) {
                    playlistNamesList = PlaylistNamesList.restore(getActivity());
                }
                playlists.clear();
                for (PlaylistName playlistName : playlistNamesList) {
                    addItem(playlistName);
                }
                playlistNamesList.save(getActivity());
            }
        });
    }

    private void addItem(PlaylistName playlistName) {
        if (itemsSize() == 0) {
            addItem(new PlaylistHeaderItem());
        }

        playlists.add(playlistName.name);
        addItem(new PlaylistItem(playlistName, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setOnCheckedChangeListener(null);
                playlistName.apikey = getUser().apikey;
                playlistName.isPublic = isChecked;

                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = this;
                server.setPublic(playlistName, new GenericCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        buttonView.setOnCheckedChangeListener(onCheckedChangeListener);
                    }

                    @Override
                    public void onFailure(int code) {
                        if (!isAdded()) return;
                        buttonView.setChecked(!isChecked);
                        buttonView.setOnCheckedChangeListener(onCheckedChangeListener);
                    }
                });
            }
        }));
    }

    @Override
    protected void initItems(List<RecyclerViewItem<RecyclerView.ViewHolder>> recyclerViewItems) {
    }

    @Override
    public void onConfirm(CharSequence text) {
        if (text.toString().isEmpty()) return;

        PlaylistName playlistName = new PlaylistName();
        playlistName.name = text.toString();
        playlistName.apikey = getUser().apikey;

        if (playlists.contains(text.toString())) {
            Utils.toast(R.string.playlist_already_exists, getActivity());
            return;
        }

        showProgress();
        server.createName(playlistName, new GenericCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                dismissProgress();
                addItem(playlistName);
            }

            @Override
            public void onFailure(int code) {
                if (!isAdded()) return;
                dismissProgress();
                if (code == Status.PlaylistAlreadyExists) {
                    Utils.toast(R.string.playlist_already_exists, getActivity());
                } else {
                    Utils.toast(R.string.server_offline, getActivity());
                }
            }
        });
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
