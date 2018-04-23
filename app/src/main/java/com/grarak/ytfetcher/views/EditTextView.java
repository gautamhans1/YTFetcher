package com.grarak.ytfetcher.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.grarak.ytfetcher.R;

public class EditTextView extends TextInputLayout {

    private TextInputEditText textInputEditText;

    public EditTextView(Context context) {
        this(context, null);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        textInputEditText = new TextInputEditText(context);
        addView(textInputEditText, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.EditTextView, defStyleAttr, 0);

        textInputEditText.setSingleLine(a.getBoolean(
                R.styleable.EditTextView_android_singleLine, false));

        a.recycle();
    }

    public void setText(CharSequence text) {
        textInputEditText.setText(text);
    }

    public Editable getText() {
        return textInputEditText.getText();
    }

    public TextInputEditText getTextInputEditText() {
        return textInputEditText;
    }
}
