package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewTreeObserver;

import com.grarak.ytfetcher.MainActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.MusicManager;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistId;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistServer;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class BaseFragment extends Fragment {

    private User user;

    private PlaylistServer playlistServer;
    private YoutubeSearchResult resultToAddPlaylist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playlistServer = new PlaylistServer(getActivity());
        if (savedInstanceState != null) {
            resultToAddPlaylist = (YoutubeSearchResult) savedInstanceState
                    .getSerializable("resultToAddPlaylist");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resultToAddPlaylist != null) {
            showPlaylistDialog(resultToAddPlaylist);
        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (getActivity() == null) return;
                onViewFinished();
            }
        });
    }

    public void onViewFinished() {
    }

    protected User getUser() {
        if (user == null) {
            user = (User) getArguments().getSerializable(MainActivity.USER_INTENT);
        }
        return user;
    }

    public BottomNavigationView getBottomNavigationView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getBottomNavigationView();
        }
        return null;
    }

    public void onViewPagerResume() {
    }

    public void onViewPagerPause() {
    }

    public boolean onBackPressed() {
        return false;
    }

    public MusicManager getMusicManager() {
        return ((MainActivity) getActivity()).getMusicManager();
    }

    public void showForegroundFragment(Fragment fragment) {
        ((MainActivity) getActivity()).showForegroundFragment(fragment);
    }

    public void removeForegroundFragment(Fragment fragment) {
        ((MainActivity) getActivity()).removeForegroundFragment(fragment);
    }

    public void setAvailablePlaylists(List<String> playlists) {
        ((MainActivity) getActivity()).setAvailablePlaylists(playlists);
    }

    public List<String> getAvailablePlaylists() {
        return ((MainActivity) getActivity()).getAvailablePlaylists();
    }

    public void showPlaylistDialog(YoutubeSearchResult result) {
        if (getAvailablePlaylists().size() == 0) {
            Utils.toast(R.string.no_playlists, getActivity());
            return;
        }

        resultToAddPlaylist = result;
        String[] playlists = getAvailablePlaylists().toArray(new String[getAvailablePlaylists().size()]);
        new AlertDialog.Builder(getActivity()).setItems(playlists, (dialog, which) -> {
            PlaylistId playlistId = new PlaylistId();
            playlistId.apikey = getUser().apikey;
            playlistId.name = playlists[which];
            playlistId.id = result.id;
            playlistServer.addToPlaylist(playlistId, new GenericCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int code) {
                    if (code == Status.PlaylistIdAlreadyExists) {
                        Utils.toast(R.string.already_in_playlist, getActivity());
                    } else {
                        Utils.toast(R.string.failed_add_playlist, getActivity());
                    }
                }
            });
        }).setOnDismissListener(dialog -> resultToAddPlaylist = null).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("resultToAddPlaylist", resultToAddPlaylist);
    }
}
