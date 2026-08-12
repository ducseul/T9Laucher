package com.t9launcher.system;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.view.WindowManager;
import android.widget.Toast;

import com.t9launcher.T9DeviceAdminReceiver;
import com.t9launcher.T9KeyAccessibilityService;

import java.lang.reflect.Method;
import java.util.List;

public final class AndroidLauncherActions implements LauncherActions {
    private static final int REQUEST_DEVICE_ADMIN = 4104;
    private static final long OPEN_NOTIFICATIONS_DELAY_MS = 100L;

    private final Activity activity;

    public AndroidLauncherActions(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void setStatusBarVisible(boolean visible) {
        if (visible) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void openNotifications() {
        activity.getWindow().getDecorView().postDelayed(
                this::openNotificationsAfterKeyRelease, OPEN_NOTIFICATIONS_DELAY_MS);
    }

    private void openNotificationsAfterKeyRelease() {
        if (T9KeyAccessibilityService.openNotifications()) return;

        try {
            Object statusBar = activity.getSystemService("statusbar");
            if (statusBar == null) throw new IllegalStateException("Status bar service unavailable");
            Method expand = statusBar.getClass().getMethod("expandNotificationsPanel");
            expand.invoke(statusBar);
        } catch (ReflectiveOperationException | RuntimeException error) {
            Toast.makeText(activity, "Không thể mở thanh thông báo",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void lockDeviceOrRequestAdmin() {
        DevicePolicyManager policy = (DevicePolicyManager) activity.getSystemService(
                Activity.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(activity, T9DeviceAdminReceiver.class);
        if (policy != null && policy.isAdminActive(admin)) {
            policy.lockNow();
            return;
        }
        Intent request = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        request.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        request.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Cho phép T9 Launcher khóa màn hình bằng phím corner 4.");
        activity.startActivityForResult(request, REQUEST_DEVICE_ADMIN);
    }

    @Override
    public boolean isVibrateMode() {
        AudioManager audio = (AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);
        return audio != null && audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    @Override
    public boolean toggleRingerMode() {
        AudioManager audio = (AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);
        if (audio == null) return false;
        int target = audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
                ? AudioManager.RINGER_MODE_NORMAL : AudioManager.RINGER_MODE_VIBRATE;
        try {
            audio.setRingerMode(target);
        } catch (SecurityException denied) {
            Toast.makeText(activity,
                    "Hãy cấp quyền Không làm phiền để đổi Chuông/Rung",
                    Toast.LENGTH_LONG).show();
            activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
        return audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }

    @Override
    public void openDialer() {
        openDialer("");
    }

    @Override
    public void openDialer(String initialNumber) {
        String number = initialNumber == null ? "" : initialNumber;
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
        TelecomManager telecom = (TelecomManager) activity.getSystemService(
                Activity.TELECOM_SERVICE);
        String defaultDialer = telecom == null ? null : telecom.getDefaultDialerPackage();
        if (defaultDialer != null && !defaultDialer.isEmpty()) dialIntent.setPackage(defaultDialer);
        try {
            activity.startActivity(dialIntent);
        } catch (ActivityNotFoundException missingDefaultDialer) {
            try {
                dialIntent.setPackage(null);
                activity.startActivity(dialIntent);
            } catch (ActivityNotFoundException missingDialer) {
                try {
                    Intent dialer = new Intent(Intent.ACTION_MAIN);
                    dialer.addCategory("android.intent.category.APP_DIALER");
                    activity.startActivity(dialer);
                } catch (ActivityNotFoundException missingPhoneApp) {
                    Toast.makeText(activity, "Không tìm thấy ứng dụng Điện thoại",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void openAlarms() {
        try {
            activity.startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS));
        } catch (ActivityNotFoundException missingAlarmHandler) {
            try {
                Intent clock = new Intent(Intent.ACTION_MAIN);
                clock.addCategory("android.intent.category.APP_CLOCK");
                activity.startActivity(explicitSystemHandler(clock, "com.android.deskclock"));
            } catch (ActivityNotFoundException missingClockApp) {
                Toast.makeText(activity, "Không tìm thấy ứng dụng Đồng hồ",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void openContacts() {
        try {
            Intent contacts = new Intent(Intent.ACTION_MAIN);
            contacts.addCategory(Intent.CATEGORY_APP_CONTACTS);
            activity.startActivity(explicitSystemHandler(contacts, "com.android.contacts"));
        } catch (ActivityNotFoundException missingContacts) {
            try {
                activity.startActivity(new Intent(
                        Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI));
            } catch (ActivityNotFoundException missingContactsApp) {
                Toast.makeText(activity, "Không tìm thấy ứng dụng Danh bạ",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void openMessaging() {
        try {
            Intent messaging = new Intent(Intent.ACTION_MAIN);
            messaging.addCategory(Intent.CATEGORY_APP_MESSAGING);
            activity.startActivity(explicitSystemHandler(
                    messaging, Telephony.Sms.getDefaultSmsPackage(activity)));
        } catch (ActivityNotFoundException missingMessaging) {
            try {
                activity.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")));
            } catch (ActivityNotFoundException missingMessagingApp) {
                Toast.makeText(activity, "Không tìm thấy ứng dụng Nhắn tin",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void launch(ActivityInfo app) {
        Intent intent = new Intent();
        intent.setClassName(app.packageName, app.name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public void handleActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_DEVICE_ADMIN && resultCode == Activity.RESULT_OK) {
            lockDeviceOrRequestAdmin();
        }
    }

    private Intent explicitSystemHandler(Intent baseIntent, String preferredPackage) {
        List<ResolveInfo> handlers = activity.getPackageManager()
                .queryIntentActivities(baseIntent, PackageManager.MATCH_ALL);
        ResolveInfo best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ResolveInfo handler : handlers) {
            if (handler.activityInfo == null || handler.activityInfo.applicationInfo == null) continue;
            String packageName = handler.activityInfo.packageName;
            int score = 0;
            if (preferredPackage != null && preferredPackage.equals(packageName)) score += 100;
            if ((handler.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                score += 10;
            }
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
}
