package com.t9launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;

import com.t9launcher.input.KeyEventDispatcher;
import com.t9launcher.input.LauncherKey;
import com.t9launcher.system.AndroidLauncherActions;
import com.t9launcher.system.KeyFilterPermissionController;
import com.t9launcher.ui.LauncherView;

public final class MainActivity extends Activity implements KeyEventDispatcher.Listener {
    private static final String EXTRA_FROM_HOME_KEY = "android.intent.extra.FROM_HOME_KEY";
    private static final long HOME_INTENT_FOREGROUND_GRACE_MS = 1000L;
    private static boolean launcherForeground;
    private static MainActivity foregroundActivity;

    private LauncherView launcher;
    private KeyEventDispatcher keyEventDispatcher;
    private AndroidLauncherActions launcherActions;
    private KeyFilterPermissionController keyFilterPermission;
    private long lastPausedAt;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        launcherActions = new AndroidLauncherActions(this);
        keyFilterPermission = new KeyFilterPermissionController(
                this, T9KeyAccessibilityService.class);
        keyEventDispatcher = new KeyEventDispatcher(new Handler(), this);
        launcher = new LauncherView(this, launcherActions);

        setContentView(launcher);
        launcherActions.setStatusBarVisible(launcher.shouldShowStatusBar());
        handleAction(getIntent(), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        foregroundActivity = this;
        launcherForeground = true;
        if (launcher != null) {
            launcherActions.setStatusBarVisible(launcher.shouldShowStatusBar());
            launcher.refreshApps();
        }
        keyFilterPermission.maybeRequest();
    }

    @Override
    protected void onPause() {
        lastPausedAt = SystemClock.uptimeMillis();
        launcherForeground = false;
        if (foregroundActivity == this) foregroundActivity = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (keyEventDispatcher != null) keyEventDispatcher.cancel();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        long elapsedSincePause = lastPausedAt == 0L
                ? Long.MAX_VALUE : SystemClock.uptimeMillis() - lastPausedAt;
        boolean recentlyForeground = launcherForeground
                || elapsedSincePause <= HOME_INTENT_FOREGROUND_GRACE_MS;
        handleAction(intent, recentlyForeground);
    }

    public static boolean dispatchCorner4FromAccessibility() {
        MainActivity activity = foregroundActivity;
        if (!launcherForeground || activity == null || activity.launcher == null) return false;
        activity.launcher.onKey(LauncherKey.CORNER_4, false);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (keyEventDispatcher == null || !keyEventDispatcher.dispatch(event)) {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

    @Override
    public void onKey(LauncherKey key, boolean longPress) {
        if (launcher != null) launcher.onKey(key, longPress);
    }

    @Override
    public boolean shouldDelegateDigitInput() {
        return launcher != null && launcher.isDrawerTextInputActive();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (launcherActions != null) launcherActions.handleActivityResult(requestCode, resultCode);
    }

    private void handleAction(Intent intent, boolean wasLauncherForeground) {
        if (intent == null || launcher == null) return;
        if (!Intent.ACTION_MAIN.equals(intent.getAction())
                || !intent.hasCategory(Intent.CATEGORY_HOME)) return;

        boolean fromHomeKey = intent.getBooleanExtra(EXTRA_FROM_HOME_KEY, false);
        if (fromHomeKey) {
            Log.w("T9Keys", "Home-key intent recentForeground=" + wasLauncherForeground
                    + " homeScreen=" + launcher.isHomeScreen());
        }
        if (wasLauncherForeground && fromHomeKey && launcher.isHomeScreen()) {
            Log.w("T9Keys", "Hang Call Home intent; locking device or requesting admin");
            launcherActions.lockDeviceOrRequestAdmin();
            return;
        }
        launcher.goHome();
    }
}
