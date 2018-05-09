package com.grarak.ytfetcher.views.recyclerview.settings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.views.recyclerview.RecyclerViewItem;

public class ButtonItem extends RecyclerViewItem {
    private CharSequence text;
    private int textColor;
    private int backgroundColor;
    private View.OnClickListener onClickListener;

    private RecyclerView.ViewHolder viewHolder;

    public ButtonItem(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    protected int getLayoutXml() {
        return R.layout.item_button;
    }

    @Override
    protected void bindViewHolder(RecyclerView.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;

        setup();
    }

    public void setText(CharSequence text) {
        this.text = text;
        setup();
    }

    public void setTextColor(int color) {
        textColor = color;
        setup();
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        setup();
    }

    private void setup() {
        if (viewHolder != null) {
            TextView textView = viewHolder.itemView.findViewById(R.id.text);
            textView.setText(text);
            if (textColor != 0) {
                textView.setTextColor(textColor);
            }
            if (backgroundColor != 0) {
                textView.setBackgroundColor(backgroundColor);
            }

            viewHolder.itemView.setOnClickListener(onClickListener);
        }
    }
}
