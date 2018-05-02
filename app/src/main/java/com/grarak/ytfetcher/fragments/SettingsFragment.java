package com.grarak.ytfetcher.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.grarak.ytfetcher.LoginActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;
import com.grarak.ytfetcher.views.recyclerview.settings.ButtonItem;

import java.util.List;

public class SettingsFragment extends RecyclerViewFragment<RecyclerView.ViewHolder, TitleFragment> {

    private boolean signoutDialog;

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (signoutDialog) {
            signoutDialog();
        }
    }

    @Override
    protected void init(Bundle savedInstanceState) {
    }

    @Override
    protected void initItems(List<RecyclerViewItem<RecyclerView.ViewHolder>> recyclerViewItems) {
        ButtonItem signout = new ButtonItem();
        signout.setText(getString(R.string.signout));
        signout.setTextColor(Color.WHITE);
        signout.setBackgroundColor(Color.RED);
        signout.setOnClickListener(v -> signoutDialog());
        recyclerViewItems.add(signout);
    }

    private void signoutDialog() {
        signoutDialog = true;
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.sure_question)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    User.delete(getActivity());
                    getActivity().finish();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                })
                .setNegativeButton(R.string.no, null)
                .setOnDismissListener(dialog -> signoutDialog = false).show();
    }
}
