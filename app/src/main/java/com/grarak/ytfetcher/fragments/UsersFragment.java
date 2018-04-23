package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.CompoundButton;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.user.UserServer;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewAdapter;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;
import com.grarak.ytfetcher.views.recyclerview.UserItem;

import java.util.List;

public class UsersFragment extends RecyclerViewFragment<UserItem.ViewHolder, TitleFragment> {

    private UserServer userServer;

    @Override
    protected RecyclerViewAdapter<UserItem.ViewHolder> createAdapter() {
        return new UserItem.UserAdapter(getItems());
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (userServer != null) return;

        userServer = new UserServer(getActivity());

        showProgress();
        userServer.list(getUser(), 0, new UserServer.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (!isAdded()) return;
                dismissProgress();
                for (User user : users) {
                    addItem(new UserItem(getUser().admin, user, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            buttonView.setOnCheckedChangeListener(null);

                            User newUser = new User();
                            newUser.apikey = getUser().apikey;
                            newUser.name = user.name;
                            newUser.verified = isChecked;

                            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = this;
                            userServer.setVerification(newUser, new GenericCallback() {
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
            }

            @Override
            public void onFailure(int code) {
                if (!isAdded()) return;
                dismissProgress();
            }
        });
    }

    @Override
    protected void initItems(List<RecyclerViewItem<UserItem.ViewHolder>> recyclerViewItems) {
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
