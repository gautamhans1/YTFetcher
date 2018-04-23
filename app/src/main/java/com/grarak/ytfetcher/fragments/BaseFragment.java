package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver;

import com.grarak.ytfetcher.MainActivity;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public class BaseFragment extends Fragment {

    private User user;

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

    public void playTrack(YoutubeSearchResult youtubeSearchResult) {
        ((MainActivity) getActivity()).playTrack(youtubeSearchResult);
    }
}
