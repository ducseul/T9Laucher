package com.t9launcher.data;

import com.t9launcher.model.LauncherConfiguration;

public interface LauncherSettingsStore {
    LauncherConfiguration load();
    void save(LauncherConfiguration configuration);
}
