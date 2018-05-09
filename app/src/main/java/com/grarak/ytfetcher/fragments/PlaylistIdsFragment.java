package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.grarak.ytfetcher.MainActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.playlist.Playlist;
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

public class PlaylistIdsFragment extends RecyclerViewFragment<PlayFragment> {

    public static PlaylistIdsFragment newInstance(
            User user,
            PlaylistResults playlistResults,
            boolean readOnly) {
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.USER_INTENT, user);
        args.putSerializable("playlistResults", playlistResults);
        args.putBoolean("readOnly", readOnly);
        PlaylistIdsFragment fragment = new PlaylistIdsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PlaylistResults playlistResults;
    private boolean readOnly;
    private PlaylistServer server;

    private String saveName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistResults = (PlaylistResults) getArguments().getSerializable("playlistResults");
        readOnly = getArguments().getBoolean("readOnly");
        server = new PlaylistServer(getActivity());
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.fragment_playlist_id;
    }

    @Override
    protected RecyclerViewAdapter createAdapter() {
        return new PlaylistIdItem.Adapter(getItems());
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (!readOnly) {
            touchHelper.attachToRecyclerView(getRecyclerView());
        }
    }

    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
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
    protected void initItems(List<RecyclerViewItem> recyclerViewItems) {
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
                            removeItem(index);

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
            }, readOnly));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        List<RecyclerViewItem> items = new ArrayList<>(getItems());
        for (RecyclerViewItem item : items) {
            ((PlaylistIdItem) item).setDownloaded();
        }

        if (saveName != null) {
            showSaveDialog(saveName);
        }
    }

    @Override
    protected void onDownloaded(YoutubeSearchResult result) {
        super.onDownloaded(result);

        List<RecyclerViewItem> items = new ArrayList<>(getItems());
        for (RecyclerViewItem item : items) {
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
        fragment.setReadOnly(readOnly);
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

            @Override
            public void onSave() {
                showSaveDialog("");
            }
        });
    }

    private void showSaveDialog(String name) {
        saveName = name;

        FrameLayout layout = new FrameLayout(getActivity());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                16, getResources().getDisplayMetrics());
        layout.setPadding(padding, padding / 2, padding, padding / 2);

        EditText editText = new EditText(getActivity());
        layout.addView(editText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        editText.setText(saveName);
        editText.setSelection(saveName.length());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                saveName = s.toString();
            }
        });

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.name)
                .setView(layout)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Playlist playlist = new Playlist();
                    playlist.apikey = getUser().apikey;
                    playlist.name = editText.getText().toString();
                    server.create(playlist, new GenericCallback() {
                        @Override
                        public void onSuccess() {
                            server.setPlaylistIds(createPlaylistIds(playlist.name), new GenericCallback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onFailure(int code) {
                                    Utils.toast(R.string.server_offline, getActivity());
                                }
                            });
                        }

                        @Override
                        public void onFailure(int code) {
                            if (code == Status.PlaylistIdAlreadyExists) {
                                Utils.toast(R.string.playlist_already_exists, getActivity());
                            } else {
                                Utils.toast(R.string.server_offline, getActivity());
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(dialog -> saveName = null).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        server.close();

        if (!readOnly) {
            server.setPlaylistIds(createPlaylistIds(playlistResults.name), new GenericCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int code) {
                }
            });
        }
    }

    private PlaylistIds createPlaylistIds(String name) {
        PlaylistIds playlistIds = new PlaylistIds();
        playlistIds.apikey = getUser().apikey;
        playlistIds.name = name;
        playlistIds.ids = new ArrayList<>();
        for (YoutubeSearchResult result : playlistResults.songs) {
            playlistIds.ids.add(result.id);
        }
        return playlistIds;
    }
}
