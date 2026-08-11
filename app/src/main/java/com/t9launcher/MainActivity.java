package com.t9launcher;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.List;

public final class MainActivity extends Activity {
    private static final int REQUEST_DEVICE_ADMIN = 4104;
    private static boolean launcherForeground;
    private static MainActivity foregroundActivity;
    private final Handler handler = new Handler();
    private LauncherView launcher;
    private String heldKey;
    private long heldSince;
    private boolean longFired;
    private boolean deferredShortPress;
    private Runnable longPressAction;
    private boolean accessibilityPromptShown;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        launcher = new LauncherView(this);
        setContentView(launcher);
        setStatusBarVisible(launcher.shouldShowStatusBar());
        handleAction(getIntent());
    }

    @Override protected void onResume() {
        super.onResume();
        foregroundActivity = this;
        launcherForeground = true;
        if (launcher != null) {
            setStatusBarVisible(launcher.shouldShowStatusBar());
            launcher.refreshApps();
        }
        maybeRequestKeyFilterPermission();
    }

    @Override protected void onPause() {
        launcherForeground = false;
        if (foregroundActivity == this) foregroundActivity = null;
        super.onPause();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleAction(intent);
    }

    public static boolean dispatchCorner4FromAccessibility() {
        MainActivity activity = foregroundActivity;
        if (!launcherForeground || activity == null || activity.launcher == null) return false;
        activity.launcher.onKey("corner4", false);
        return true;
    }

    private void handleAction(Intent intent) {
        if (intent == null || launcher == null) return;
        if (Intent.ACTION_MAIN.equals(intent.getAction())
                && intent.hasCategory(Intent.CATEGORY_HOME)) launcher.goHome();
    }

    private boolean isKeyFilterEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> enabled = manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String packageName = getPackageName();
        String serviceName = T9KeyAccessibilityService.class.getName();
        for (AccessibilityServiceInfo info : enabled) {
            if (info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null) continue;
            if (packageName.equals(info.getResolveInfo().serviceInfo.packageName)
                    && serviceName.equals(info.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }

    private void maybeRequestKeyFilterPermission() {
        if (accessibilityPromptShown || isKeyFilterEnabled() || isFinishing()) return;
        accessibilityPromptShown = true;
        new AlertDialog.Builder(this)
                .setTitle("Cho phép phím Hang Up")
                .setMessage("Android đang chặn phím Hang Up trước T9 Launcher. Hãy bật dịch vụ “T9 Launcher - Phím cứng” để phím 4 về Home hoặc khóa màn hình đúng ngữ cảnh.")
                .setPositiveButton("Mở Cài đặt", (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .setNegativeButton("Để sau", null)
                .show();
    }

    public void setStatusBarVisible(boolean visible) {
        if (visible) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        String key = mapKey(event.getKeyCode());
        if (key == null) return super.dispatchKeyEvent(event);
        if (launcher != null && launcher.isDrawerTextInputActive()
                && isT9DigitKey(event.getKeyCode())) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (deferredShortPress && key.equals(heldKey)) {
                fireLongPressWhenDue(key);
                return true;
            }
            cancelLongPressTimer();
            heldKey = key; longFired = false;
            heldSince = SystemClock.uptimeMillis();
            deferredShortPress = supportsLongPress(key);
            if (deferredShortPress) {
                longPressAction = () -> fireLongPressWhenDue(key);
                handler.postDelayed(longPressAction, longPressDelayMs(key));
            } else {
                launcher.onKey(key, false);
            }
            Log.d("T9Keys", "down key=" + key + " code=" + event.getKeyCode());
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() > 0) {
            if (deferredShortPress && key.equals(heldKey)) fireLongPressWhenDue(key);
            else if (!deferredShortPress && isDirectional(key)) launcher.onKey(key, false);
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_UP && key.equals(heldKey)) {
            fireLongPressWhenDue(key);
            cancelLongPressTimer();
            if (deferredShortPress && !longFired) launcher.onKey(key, false);
            Log.d("T9Keys", "up key=" + key + " heldMs="
                    + (SystemClock.uptimeMillis() - heldSince) + " long=" + longFired);
            heldKey = null;
            deferredShortPress = false;
            return true;
        }
        return true;
    }

    private void fireLongPressWhenDue(String key) {
        if (longFired || !deferredShortPress || !key.equals(heldKey)) return;
        long elapsed = SystemClock.uptimeMillis() - heldSince;
        long required = longPressDelayMs(key);
        if (elapsed < required) {
            if (longPressAction != null) {
                handler.removeCallbacks(longPressAction);
                handler.postDelayed(longPressAction, required - elapsed);
            }
            return;
        }
        longFired = true;
        Log.d("T9Keys", "long key=" + key + " heldMs=" + elapsed);
        launcher.onKey(key, true);
    }

    private void cancelLongPressTimer() {
        if (longPressAction != null) handler.removeCallbacks(longPressAction);
        longPressAction = null;
    }

    public void lockDeviceOrRequestAdmin() {
        DevicePolicyManager policy = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, T9DeviceAdminReceiver.class);
        if (policy != null && policy.isAdminActive(admin)) {
            policy.lockNow();
            return;
        }
        Intent request = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        request.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        request.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Cho phép T9 Launcher khóa màn hình bằng phím corner 4.");
        startActivityForResult(request, REQUEST_DEVICE_ADMIN);
    }

    public boolean isVibrateMode() {
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        return audio != null && audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    public boolean toggleRingerMode() {
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audio == null) return false;
        int target = audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
                ? AudioManager.RINGER_MODE_NORMAL : AudioManager.RINGER_MODE_VIBRATE;
        try {
            audio.setRingerMode(target);
        } catch (SecurityException denied) {
            Toast.makeText(this, "Hãy cấp quyền Không làm phiền để đổi Chuông/Rung", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
        return audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    public void openSystemDialer() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
        } catch (ActivityNotFoundException missingDialer) {
            try {
                Intent dialer = new Intent(Intent.ACTION_MAIN);
                dialer.addCategory("android.intent.category.APP_DIALER");
                startActivity(dialer);
            } catch (ActivityNotFoundException missingPhoneApp) {
                Toast.makeText(this, "Không tìm thấy ứng dụng Điện thoại", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void openSystemContacts() {
        try {
            Intent contacts = new Intent(Intent.ACTION_MAIN);
            contacts.addCategory(Intent.CATEGORY_APP_CONTACTS);
            startActivity(explicitSystemHandler(contacts, "com.android.contacts"));
        } catch (ActivityNotFoundException missingContacts) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI));
            } catch (ActivityNotFoundException missingContactsApp) {
                Toast.makeText(this, "Không tìm thấy ứng dụng Danh bạ", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void openSystemMessaging() {
        try {
            Intent messaging = new Intent(Intent.ACTION_MAIN);
            messaging.addCategory(Intent.CATEGORY_APP_MESSAGING);
            startActivity(explicitSystemHandler(messaging, Telephony.Sms.getDefaultSmsPackage(this)));
        } catch (ActivityNotFoundException missingMessaging) {
            try {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")));
            } catch (ActivityNotFoundException missingMessagingApp) {
                Toast.makeText(this, "Không tìm thấy ứng dụng Nhắn tin", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Intent explicitSystemHandler(Intent baseIntent, String preferredPackage) {
        List<ResolveInfo> handlers = getPackageManager()
                .queryIntentActivities(baseIntent, PackageManager.MATCH_ALL);
        ResolveInfo best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ResolveInfo handler : handlers) {
            if (handler.activityInfo == null || handler.activityInfo.applicationInfo == null) continue;
            String packageName = handler.activityInfo.packageName;
            int score = 0;
            if (preferredPackage != null && preferredPackage.equals(packageName)) score += 100;
            if ((handler.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) score += 10;
            if (packageName.startsWith("com.android.")) score += 5;
            if (score > bestScore) {
                best = handler;
                bestScore = score;
            }
        }
        if (best == null) throw new ActivityNotFoundException();
        Intent explicit = new Intent(baseIntent);
        explicit.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        return explicit;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEVICE_ADMIN && resultCode == RESULT_OK) lockDeviceOrRequestAdmin();
    }

    private static String mapKey(int code) {
        if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) return String.valueOf(code - KeyEvent.KEYCODE_0);
        switch (code) {
            case KeyEvent.KEYCODE_STAR: return "*";
            case KeyEvent.KEYCODE_POUND: return "#";
            case KeyEvent.KEYCODE_DPAD_UP: return "up";
            case KeyEvent.KEYCODE_DPAD_DOWN: return "down";
            case KeyEvent.KEYCODE_DPAD_LEFT: return "left";
            case KeyEvent.KEYCODE_DPAD_RIGHT: return "right";
            case KeyEvent.KEYCODE_DPAD_CENTER: return "ok";
            case KeyEvent.KEYCODE_ENTER: return "ok";
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return "ok";
            case KeyEvent.KEYCODE_BACK: return "back";
            // Doov R17 Pro mtk-kpd.kl maps the four physical corner keys to these Android codes.
            case KeyEvent.KEYCODE_MENU: return "corner1";
            case KeyEvent.KEYCODE_SOFT_RIGHT: return "corner2";
            case KeyEvent.KEYCODE_CALL: return "corner3";
            case KeyEvent.KEYCODE_ENDCALL: return "corner4";
            // Keep common OEM alternatives as a fallback.
            case KeyEvent.KEYCODE_SOFT_LEFT: return "corner1";
            case KeyEvent.KEYCODE_BUTTON_L1: return "corner3";
            case KeyEvent.KEYCODE_BUTTON_R1: return "corner4";
            default: return null;
        }
    }

    private static boolean supportsLongPress(String key) {
        return key.equals("#");
    }

    private static long longPressDelayMs(String key) {
        return key.equals("#") ? 5000L : 500L;
    }

    private static boolean isDirectional(String key) {
        return key.equals("up") || key.equals("down")
                || key.equals("left") || key.equals("right");
    }

    private static boolean isT9DigitKey(int keyCode) {
        return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
    }
}
