package com.t9launcher.system;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public final class KeyFilterPermissionController {
    private final Activity activity;
    private final Class<?> serviceClass;
    private boolean promptShown;

    public KeyFilterPermissionController(Activity activity, Class<?> serviceClass) {
        this.activity = activity;
        this.serviceClass = serviceClass;
    }

    public void maybeRequest() {
        if (promptShown || isEnabled() || activity.isFinishing()) return;
        promptShown = true;
        new AlertDialog.Builder(activity)
                .setTitle("Cho phép phím Hang Up")
                .setMessage("Android đang chặn phím Hang Up trước T9 Launcher. "
                        + "Hãy bật dịch vụ “T9 Launcher - Phím cứng” để phím 4 về Home "
                        + "hoặc khóa màn hình đúng ngữ cảnh.")
                .setPositiveButton("Mở Cài đặt", (dialog, which) ->
                        activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                .setNegativeButton("Để sau", null)
                .show();
    }

    private boolean isEnabled() {
        AccessibilityManager manager = (AccessibilityManager) activity.getSystemService(
                Activity.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> enabled = manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String packageName = activity.getPackageName();
        String serviceName = serviceClass.getName();
        for (AccessibilityServiceInfo info : enabled) {
            if (info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null) continue;
            if (packageName.equals(info.getResolveInfo().serviceInfo.packageName)
                    && serviceName.equals(info.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }
}
