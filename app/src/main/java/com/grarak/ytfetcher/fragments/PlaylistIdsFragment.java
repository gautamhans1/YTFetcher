package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.grarak.ytfetcher.MainActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistId;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistIds;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistResults;
import com.grarak.ytfetcher.utils.server.playlist.PlaylistServer;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.views.recyclerview.PlaylistIdItem;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewAdapter;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistIdsFragment extends RecyclerViewFragment<PlaylistIdItem.ViewHolder, PlayFragment> {

    public static PlaylistIdsFragment newInstance(User user,
                                                  PlaylistResults playlistResults) {
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.USER_INTENT, user);
        args.putSerializable("playlistResults", playlistResults);
        PlaylistIdsFragment fragment = new PlaylistIdsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PlaylistResults playlistResults;
    private PlaylistServer server;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistResults = (PlaylistResults) getArguments().getSerializable("playlistResults");
        server = new PlaylistServer(getActivity());
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.fragment_playlist_id;
    }

    @Override
    protected RecyclerViewAdapter<PlaylistIdItem.ViewHolder> createAdapter() {
        return new PlaylistIdItem.Adapter(getItems());
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        touchHelper.attachToRecyclerView(getRecyclerView());
    }

    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
    });

    private void move(int oldPosition, int newPosition) {
        Collections.swap(playlistResults.songs, oldPosition, newPosition);
        Collections.swap(getItems(), oldPosition, newPosition);
        getRecyclerViewAdapter().notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    protected void initItems(List<RecyclerViewItem<PlaylistIdItem.ViewHolder>> recyclerViewItems) {
        for (YoutubeSearchResult result : playlistResults.songs) {
            recyclerViewItems.add(new PlaylistIdItem(result, new PlaylistIdItem.PlaylistLinkListener() {
                @Override
                public void onClick(PlaylistIdItem item) {
                    getMusicManager().play(result);
                }

                @Override
                public void onRemoveFromPlaylist(PlaylistIdItem item) {
                    PlaylistId playlistId = new PlaylistId();
                    playlistId.apikey = getUser().apikey;
                    playlistId.name = playlistResults.name;
                    playlistId.id = result.id;
                    server.deleteFromPlaylist(playlistId, new GenericCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            int index = playlistResults.songs.indexOf(result);
                            playlistResults.songs.remove(index);
                            getItems().remove(index);
                            getRecyclerViewAdapter().notifyItemRemoved(index);

                            if (getItems().size() == 0) {
                                removeForegroundFragment(PlaylistIdsFragment.this);
                            }
                        }

                        @Override
                        public void onFailure(int code) {
                            Utils.toast(R.string.server_offline, getActivity());
                        }
                    });
                }

                @Override
                public void onDelete(PlaylistIdItem item) {
                    if (deleteResult(result)) {
                        item.setDownloaded();
                    }
                }

                @Override
                public void onDownload(PlaylistIdItem item) {
                    queueDownload(result);
                }

                @Override
                public void onMoveUp(PlaylistIdItem item) {
                    int position = getItems().indexOf(item);
                    if (position > 0) {
                        move(position, position - 1);
                    }
                }

                @Override
                public void onMoveDown(PlaylistIdItem item) {
                    int position = getItems().indexOf(item);
                    if (position < getItems().size()) {
                        move(position, position + 1);
                    }
                }
            }));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        List<RecyclerViewItem<PlaylistIdItem.ViewHolder>> items = new ArrayList<>(getItems());
        for (RecyclerViewItem<PlaylistIdItem.ViewHolder> item : items) {
            ((PlaylistIdItem) item).setDownloaded();
        }
    }

    @Override
    protected void onDownloaded(YoutubeSearchResult result) {
        super.onDownloaded(result);

        List<RecyclerViewItem<PlaylistIdItem.ViewHolder>> items = new ArrayList<>(getItems());
        for (RecyclerViewItem<PlaylistIdItem.ViewHolder> item : items) {
            PlaylistIdItem playlistIdItem = (PlaylistIdItem) item;
            if (playlistIdItem.result.equals(result)) {
                playlistIdItem.setDownloaded();
            }
        }
    }

    @Override
    protected Class<PlayFragment> getTitleFragmentClass() {
        return PlayFragment.class;
    }

    @Override
    protected void setUpTitleFragment(PlayFragment fragment) {
        super.setUpTitleFragment(fragment);
        fragment.setText(playlistResults.name);
        fragment.setPlayListener(new PlayFragment.PlayListener() {
            @Override
            public void onPlay() {
                getMusicManager().play(playlistResults.songs, 0);
            }

            @Override
            public void onShuffle() {
                List<YoutubeSearchResult> results = new ArrayList<>(playlistResults.songs);
                Collections.shuffle(results);
                getMusicManager().play(results, 0);
            }

            @Override
            public void onDownload() {
                for (YoutubeSearchResult result : playlistResults.songs) {
                    queueDownload(result);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        server.close();

        PlaylistIds playlistIds = new PlaylistIds();
        playlistIds.apikey = getUser().apikey;
        playlistIds.name = playlistResults.name;
        playlistIds.ids = new ArrayList<>();
        for (YoutubeSearchResult result : playlistResults.songs) {
            playlistIds.ids.add(result.id);
        }
        server.setPlaylistIds(playlistIds, new GenericCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int code) {
            }
        });
    }
}
