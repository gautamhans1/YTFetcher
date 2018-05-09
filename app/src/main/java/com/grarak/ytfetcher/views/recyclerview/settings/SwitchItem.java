package com.grarak.ytfetcher.views.recyclerview.settings;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

public class SwitchItem extends RecyclerViewItem {

    public interface SwitchListener {
        void onCheckedChanged(boolean checked);
    }

    private CharSequence text;
    private boolean checked;

    private RecyclerView.ViewHolder viewHolder;

    private final SwitchListener switchListener;

    public SwitchItem(SwitchListener switchListener) {
        this.switchListener = switchListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_switch;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;

        SwitchCompat switchCompat = viewHolder.itemView.findViewById(R.id.switch_compat);
        viewHolder.itemView.setOnClickListener(v -> {
            checked = !switchCompat.isChecked();
            switchCompat.setChecked(checked);
            switchListener.onCheckedChanged(checked);
        });

        setup();
    }

    public void setText(CharSequence text) {
        this.text = text;
        setup();
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        setup();
    }

    private void setup() {
        if (viewHolder != null) {
            ((TextView) viewHolder.itemView.findViewById(
                    R.id.text)).setText(text);
            ((SwitchCompat) viewHolder.itemView.findViewById(
                    R.id.switch_compat)).setChecked(checked);
        }
    }
}
