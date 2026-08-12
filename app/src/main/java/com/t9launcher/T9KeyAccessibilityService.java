package com.t9launcher;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public final class T9KeyAccessibilityService extends AccessibilityService {
    private static final String OEM_DESKTOP_PACKAGE = "com.dp.op";
    private static final long[] OEM_RECOVERY_DELAYS_MS = {300L, 1000L, 2500L};
    private static volatile T9KeyAccessibilityService connectedService;
    private final Handler recoveryHandler = new Handler(Looper.getMainLooper());
    private final Runnable restoreLauncherAction = this::restoreLauncherHome;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        connectedService = this;
    }

    public static boolean openNotifications() {
        T9KeyAccessibilityService service = connectedService;
        return service != null && service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != KeyEvent.KEYCODE_ENDCALL) return false;
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (!MainActivity.dispatchCorner4FromAccessibility()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence packageName = event.getPackageName();
        if (!OEM_DESKTOP_PACKAGE.equals(String.valueOf(packageName))) return;

        Log.w("T9Keys", "OEM UI opened; restoring T9 Launcher");
        recoveryHandler.removeCallbacks(restoreLauncherAction);
        restoreLauncherHome();
        for (long delay : OEM_RECOVERY_DELAYS_MS) {
            recoveryHandler.postDelayed(restoreLauncherAction, delay);
        }
    }

    private void restoreLauncherHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        recoveryHandler.removeCallbacks(restoreLauncherAction);
    }

    @Override
    public void onDestroy() {
        recoveryHandler.removeCallbacks(restoreLauncherAction);
        if (connectedService == this) connectedService = null;
        super.onDestroy();
    }
}
