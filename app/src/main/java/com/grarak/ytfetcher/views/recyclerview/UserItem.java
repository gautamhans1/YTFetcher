package com.grarak.ytfetcher.views.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.user.User;

import java.util.List;

public class UserItem extends RecyclerViewItem<UserItem.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private View admin;
        private SwitchCompat verifiedSwitch;

        private ViewHolder(View item) {
            super(item);
            title = item.findViewById(R.id.title);
            admin = item.findViewById(R.id.admin_view);
            verifiedSwitch = item.findViewById(R.id.verified);
        }
    }

    private boolean isAdmin;
    private User user;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public UserItem(boolean isAdmin,
                    User user,
                    CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.isAdmin = isAdmin;
        this.user = user;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    protected int getLayoutXml() {
        return 0;
    }

    @Override
    protected ViewHolder createViewHolder(View inflatedView) {
        return null;
    }

    @Override
    protected void bindViewHolder(ViewHolder viewHolder) {
        viewHolder.title.setText(user.name);
        viewHolder.admin.setVisibility(user.admin ? View.VISIBLE : View.GONE);
        viewHolder.verifiedSwitch.setChecked(user.verified);

        if (isAdmin && !user.admin) {
            viewHolder.verifiedSwitch.setVisibility(View.VISIBLE);
            viewHolder.verifiedSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        } else {
            viewHolder.verifiedSwitch.setVisibility(View.INVISIBLE);
        }
    }

    public static class UserAdapter extends RecyclerViewAdapter<ViewHolder> {

        public UserAdapter(List<RecyclerViewItem<ViewHolder>> recyclerViewItems) {
            super(recyclerViewItems);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            return new ViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_user, parent, false));
        }
    }
}
