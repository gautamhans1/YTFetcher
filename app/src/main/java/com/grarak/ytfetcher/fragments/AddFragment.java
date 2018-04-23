package com.grarak.ytfetcher.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Utils;

import io.codetail.animation.ViewAnimationUtils;

public class AddFragment extends TitleFragment {

    public interface OnOpenListener {
        void onOpen(AddFragment fragment);
    }

    public interface OnConfirmListener {
        void onConfirm(CharSequence text);
    }

    private View editTextParent;
    private EditText editTextView;
    private FloatingActionButton add;

    private CharSequence hint;
    private OnOpenListener onOpenListener;
    private OnConfirmListener onConfirmListener;
    private @DrawableRes
    int imageResource;

    @Override
    protected int getLayoutXml() {
        return R.layout.fragment_add;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        editTextParent = rootView.findViewById(R.id.edittext_parent);
        editTextView = rootView.findViewById(R.id.edittext);
        editTextView.setHint(hint);
        editTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDone();
                return true;
            }
            return false;
        });

        rootView.findViewById(R.id.delete_btn).setOnClickListener(v -> editTextView.setText(""));

        add = rootView.findViewById(R.id.add);
        add.setOnClickListener(v -> {
            if (editTextParent.getVisibility() == View.INVISIBLE) {
                showEditText(true);
            } else {
                onDone();
            }
        });
        if (imageResource > 0) {
            add.setImageResource(imageResource);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("show")) {
                editTextParent.setVisibility(View.VISIBLE);
                Utils.showKeyboard(editTextView);
            }
            editTextView.setText(savedInstanceState.getCharSequence("text"));
        }
        return rootView;
    }

    private void onDone() {
        if (onConfirmListener != null) {
            onConfirmListener.onConfirm(editTextView.getText());
        }
        showEditText(false);
        editTextView.setText("");
    }

    private void showEditText(boolean show) {
        Animator animator = ViewAnimationUtils.createCircularReveal(editTextParent,
                editTextParent.getWidth(), 0, 0, editTextParent.getWidth());
        if (!show) {
            animator.setInterpolator((Interpolator) input -> Math.abs(input - 1f));
        }
        editTextParent.setVisibility(View.VISIBLE);
        editTextView.setText("");
        if (show) {
            if (onOpenListener != null) {
                onOpenListener.onOpen(this);
            }
            Utils.showKeyboard(editTextView);
        } else {
            Utils.hideKeyboard(editTextView);
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!show) {
                    editTextParent.setVisibility(View.INVISIBLE);
                }
            }
        });
        animator.setDuration(250);
        animator.start();
    }

    public void setEditText(CharSequence text) {
        if (editTextView != null) {
            editTextView.setText(text);
            editTextView.setSelection(text.length());
        }
    }

    public void setHint(CharSequence hint) {
        this.hint = hint;
        if (editTextView != null) {
            editTextView.setHint(hint);
        }
    }

    public void setOnOpenListener(OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }

    public void setImageResource(@DrawableRes int imageResource) {
        this.imageResource = imageResource;
        if (add != null) {
            add.setImageResource(imageResource);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("show", editTextParent.getVisibility() == View.VISIBLE);
        outState.putCharSequence("text", editTextView.getText());
    }

    @Override
    public void onViewPagerResume() {
        super.onViewPagerResume();

        if (editTextParent.getVisibility() == View.VISIBLE) {
            Utils.showKeyboard(editTextView);
        }
    }

    @Override
    public void onViewPagerPause() {
        super.onViewPagerPause();

        Utils.hideKeyboard(editTextView);
    }

    @Override
    public boolean onBackPressed() {
        if (editTextParent.getVisibility() == View.VISIBLE) {
            showEditText(false);
            return true;
        }
        return false;
    }
}
