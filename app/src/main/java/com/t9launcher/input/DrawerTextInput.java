package com.t9launcher.input;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public final class DrawerTextInput {
    public interface Listener {
        void onQueryChanged();
        void onSubmit();
    }

    private final View host;
    private final Listener listener;
    private final Editable query = new SpannableStringBuilder();

    public DrawerTextInput(View host, Listener listener) {
        this.host = host;
        this.listener = listener;
        Selection.setSelection(query, 0);
    }

    public CharSequence text() {
        return query;
    }

    public boolean isEmpty() {
        return query.length() == 0;
    }

    public void clear() {
        query.clear();
        Selection.setSelection(query, 0);
        listener.onQueryChanged();
    }

    public void deleteCharacter() {
        int start = Math.max(0, Selection.getSelectionStart(query));
        int end = Math.max(0, Selection.getSelectionEnd(query));
        if (start != end) {
            query.delete(Math.min(start, end), Math.max(start, end));
            Selection.setSelection(query, Math.min(start, end));
        } else if (start > 0) {
            int previous = Character.offsetByCodePoints(query, start, -1);
            query.delete(previous, start);
            Selection.setSelection(query, previous);
        }
        listener.onQueryChanged();
    }

    public InputConnection createInputConnection(EditorInfo attributes) {
        attributes.inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        attributes.imeOptions = EditorInfo.IME_ACTION_DONE
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        attributes.initialSelStart = Selection.getSelectionStart(query);
        attributes.initialSelEnd = Selection.getSelectionEnd(query);
        return new BaseInputConnection(host, true) {
            @Override
            public Editable getEditable() {
                return query;
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                boolean handled = super.commitText(text, newCursorPosition);
                listener.onQueryChanged();
                return handled;
            }

            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                boolean handled = super.setComposingText(text, newCursorPosition);
                listener.onQueryChanged();
                return handled;
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                boolean handled = super.deleteSurroundingText(beforeLength, afterLength);
                listener.onQueryChanged();
                return handled;
            }

            @Override
            public boolean performEditorAction(int actionCode) {
                if (actionCode == EditorInfo.IME_ACTION_DONE) {
                    listener.onSubmit();
                    return true;
                }
                return super.performEditorAction(actionCode);
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            listener.onSubmit();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_STAR) {
            deleteCharacter();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_POUND) {
            clear();
            return true;
        }
        int unicode = event.getUnicodeChar();
        if (unicode != 0 && (Character.isLetter(unicode) || Character.isWhitespace(unicode))) {
            replaceSelection(new String(Character.toChars(unicode)));
            return true;
        }
        // Raw number keys are consumed by the configured T9 IME (for example QinVN).
        return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
    }

    public void refresh(boolean active) {
        host.requestFocus();
        host.post(() -> {
            InputMethodManager input = (InputMethodManager) host.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (input == null) return;
            input.restartInput(host);
            if (active) {
                input.showSoftInput(host, InputMethodManager.SHOW_IMPLICIT);
            } else {
                input.hideSoftInputFromWindow(host.getWindowToken(), 0);
            }
        });
    }

    private void replaceSelection(CharSequence text) {
        int start = Math.max(0, Selection.getSelectionStart(query));
        int end = Math.max(0, Selection.getSelectionEnd(query));
        query.replace(Math.min(start, end), Math.max(start, end), text);
        Selection.setSelection(query, Math.min(start, end) + text.length());
        listener.onQueryChanged();
    }
}
