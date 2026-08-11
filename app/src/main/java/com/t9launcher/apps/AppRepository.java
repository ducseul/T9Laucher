package com.t9launcher.apps;

import android.content.pm.ActivityInfo;

import java.util.List;

public interface AppRepository {
    List<ActivityInfo> loadLaunchableApps();
    List<ActivityInfo> filterAndSort(List<ActivityInfo> apps, CharSequence query);
    String label(ActivityInfo app);
}
