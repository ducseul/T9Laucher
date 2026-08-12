package com.t9launcher.system;

import android.content.pm.ActivityInfo;

public interface LauncherActions {
    void setStatusBarVisible(boolean visible);
    void lockDeviceOrRequestAdmin();
    boolean isVibrateMode();
    boolean toggleRingerMode();
    void openDialer();
    void openDialer(String initialNumber);
    void openAlarms();
    void openContacts();
    void openMessaging();
    void launch(ActivityInfo app);
}
