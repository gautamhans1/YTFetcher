package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeCharts;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;
import com.grarak.ytfetcher.views.recyclerview.MusicItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewAdapter;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.List;

public class HomeFragment extends RecyclerViewFragment<MusicItem.ViewHolder, TitleFragment> {

    private YoutubeServer server;

    @Override
    protected RecyclerViewAdapter<MusicItem.ViewHolder> createAdapter() {
        return new MusicItem.MusicAdapter(getItems(), true);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (server != null) return;

        showProgress();
        server = new YoutubeServer(getActivity());

        Youtube youtube = new Youtube();
        youtube.apikey = getUser().apikey;
        server.getCharts(youtube, new YoutubeServer.YoutubeChartsCallback() {

            @Override
            public void onSuccess(YoutubeCharts youtubeCharts) {
                addViews(youtubeCharts);
            }

            @Override
            public void onFailure(int code) {
                addViews(null);
            }

            private void addViews(YoutubeCharts youtubeCharts) {
                if (!isAdded()) return;
                dismissProgress();
                if (youtubeCharts == null) {
                    youtubeCharts = YoutubeCharts.restore(getActivity());
                }
                for (YoutubeSearchResult result : youtubeCharts) {
                    addItem(new MusicItem(result, v -> playTrack(result)));
                }
                youtubeCharts.save(getActivity());
            }
        });
    }

    @Override
    protected void initItems(List<RecyclerViewItem<MusicItem.ViewHolder>> recyclerViewItems) {
    }

    @Override
    protected Class<TitleFragment> getTitleFragmentClass() {
        return TitleFragment.class;
    }

    @Override
    protected void setUpTitleFragment(TitleFragment fragment) {
        super.setUpTitleFragment(fragment);
        fragment.setText(getString(R.string.popular_music));
    }

    @Override
    protected String getEmptyViewsMessage() {
        return getString(R.string.charts_failed);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (server != null) {
            server.close();
        }
    }
}
