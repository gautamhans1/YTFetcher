package com.grarak.ytfetcher.views.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.user.User;

public class UserItem extends RecyclerViewItem {

    public interface UserListener {
        void onClick(UserItem item);

        void onVerified(UserItem item, boolean verified);
    }

    private final boolean isAdmin;
    private final User user;
    private final UserListener userListener;
    private SwitchCompat verifiedSwitch;

    public UserItem(boolean isAdmin,
                    User user,
                    UserListener userListener) {
        this.isAdmin = isAdmin;
        this.user = user;
        this.userListener = userListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_user;
    }

    private CompoundButton.OnCheckedChangeListener verifiedSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            userListener.onVerified(UserItem.this, isChecked);
        }
    };

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        TextView title = viewHolder.itemView.findViewById(R.id.title);
        View admin = viewHolder.itemView.findViewById(R.id.admin_view);
        verifiedSwitch = viewHolder.itemView.findViewById(R.id.verified);

        title.setText(user.name);
        admin.setVisibility(user.admin ? View.VISIBLE : View.GONE);
        verifiedSwitch.setChecked(user.verified);

        viewHolder.itemView.setOnClickListener(v -> userListener.onClick(this));

        if (isAdmin && !user.admin) {
            verifiedSwitch.setVisibility(View.VISIBLE);
            verifiedSwitch.setOnCheckedChangeListener(verifiedSwitchListener);
        } else {
            verifiedSwitch.setVisibility(View.INVISIBLE);
        }
    }

    public void setVerified(boolean verified) {
        verifiedSwitch.setOnCheckedChangeListener(null);
        verifiedSwitch.setChecked(verified);
        verifiedSwitch.setOnCheckedChangeListener(verifiedSwitchListener);
    }
}
