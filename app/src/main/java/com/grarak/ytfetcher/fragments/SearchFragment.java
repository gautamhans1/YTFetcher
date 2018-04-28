package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;
import com.grarak.ytfetcher.views.recyclerview.MusicItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewAdapter;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.List;

public class SearchFragment extends RecyclerViewFragment<MusicItem.ViewHolder, AddFragment>
        implements AddFragment.OnOpenListener, AddFragment.OnConfirmListener {

    private String title;
    private YoutubeServer server;

    @Override
    protected RecyclerViewAdapter<MusicItem.ViewHolder> createAdapter() {
        return new MusicItem.MusicAdapter(getItems(), false);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (server != null) return;

        server = new YoutubeServer(getActivity());
    }

    @Override
    protected void initItems(List<RecyclerViewItem<MusicItem.ViewHolder>> recyclerViewItems) {
    }

    @Override
    public void onViewFinished() {
        super.onViewFinished();

        onConfirm(Settings.getLastSearch(getActivity()));
    }

    @Override
    public void onOpen(AddFragment fragment) {
        if (title != null) {
            fragment.setEditText(title);
        }
    }

    @Override
    public void onConfirm(CharSequence text) {
        if (text.toString().isEmpty() || text.toString().equals(title)) {
            return;
        }

        Youtube youtube = new Youtube();
        youtube.apikey = getUser().apikey;
        title = youtube.searchquery = text.toString();
        getTitleFragment().setText(title);

        server.close();
        Settings.setLastSearch(getActivity(), title);
        showProgress();
        server.search(youtube, new YoutubeServer.YoutubeResultsCallback() {
            @Override
            public void onSuccess(List<YoutubeSearchResult> youtubeSearchResults) {
                if (!isAdded()) return;
                dismissProgress();
                clearItems();

                for (YoutubeSearchResult result : youtubeSearchResults) {
                    addItem(new MusicItem(result, new MusicItem.MusicListener() {
                        @Override
                        public void onClick(MusicItem musicItem) {
                            getMusicManager().play(result);
                        }

                        @Override
                        public void onAddPlaylist(MusicItem musicItem) {
                            showPlaylistDialog(result);
                        }
                    }));
                }
            }

            @Override
            public void onFailure(int code) {
                if (!isAdded()) return;
                dismissProgress();
                clearItems();
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
        String title = this.title;
        if (title == null) {
            title = getString(R.string.search);
        }
        fragment.setText(title);
        fragment.setHint(getText(R.string.query));
        fragment.setImageResource(R.drawable.ic_search);
        fragment.setOnOpenListener(this);
        fragment.setOnConfirmListener(this);
    }

    @Override
    protected String getEmptyViewsMessage() {
        return getString(R.string.no_songs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.close();
        }
    }
}
