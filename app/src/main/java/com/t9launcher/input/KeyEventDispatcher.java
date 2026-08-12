package com.t9launcher.input;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public final class KeyEventDispatcher {
    public interface Listener {
        void onKey(LauncherKey key, boolean longPress);
        boolean shouldDelegateDigitInput();
    }

    private final Handler handler;
    private final Listener listener;
    private LauncherKey heldKey;
    private long heldSince;
    private boolean longFired;
    private boolean deferredShortPress;
    private Runnable longPressAction;

    public KeyEventDispatcher(Handler handler, Listener listener) {
        this.handler = handler;
        this.listener = listener;
    }

    /** Returns false when the Activity should pass the event to the focused Android view. */
    public boolean dispatch(KeyEvent event) {
        LauncherKey key = PhysicalKeyMapper.fromKeyCode(event.getKeyCode());
        if (key == null || (listener.shouldDelegateDigitInput() && key.isDigit())) return false;

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (key == heldKey && (deferredShortPress || key.dispatchesOnRelease())) {
                if (deferredShortPress) fireLongPressWhenDue(key);
                return true;
            }
            cancelLongPressTimer();
            heldKey = key;
            longFired = false;
            heldSince = SystemClock.uptimeMillis();
            deferredShortPress = key.supportsLongPress();
            if (deferredShortPress) {
                longPressAction = () -> fireLongPressWhenDue(key);
                handler.postDelayed(longPressAction, key.longPressDelayMs());
            } else if (!key.dispatchesOnRelease()) {
                listener.onKey(key, false);
            }
            Log.d("T9Keys", "down key=" + key + " code=" + event.getKeyCode());
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() > 0) {
            if (deferredShortPress && key == heldKey) fireLongPressWhenDue(key);
            else if (!deferredShortPress && key.isDirectional()) listener.onKey(key, false);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_UP && key == heldKey) {
            fireLongPressWhenDue(key);
            cancelLongPressTimer();
            if ((deferredShortPress && !longFired) || key.dispatchesOnRelease()) {
                listener.onKey(key, false);
            }
            Log.d("T9Keys", "up key=" + key + " heldMs="
                    + (SystemClock.uptimeMillis() - heldSince) + " long=" + longFired);
            heldKey = null;
            deferredShortPress = false;
            return true;
        }
        return true;
    }

    public void cancel() {
        cancelLongPressTimer();
        heldKey = null;
        deferredShortPress = false;
    }

    private void fireLongPressWhenDue(LauncherKey key) {
        if (longFired || !deferredShortPress || key != heldKey) return;
        long elapsed = SystemClock.uptimeMillis() - heldSince;
        long required = key.longPressDelayMs();
        if (elapsed < required) {
            if (longPressAction != null) {
                handler.removeCallbacks(longPressAction);
                handler.postDelayed(longPressAction, required - elapsed);
            }
            return;
        }
        longFired = true;
        Log.d("T9Keys", "long key=" + key + " heldMs=" + elapsed);
        listener.onKey(key, true);
    }

    private void cancelLongPressTimer() {
        if (longPressAction != null) handler.removeCallbacks(longPressAction);
        longPressAction = null;
    }
}
