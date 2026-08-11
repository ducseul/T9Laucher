package com.t9launcher.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.t9launcher.search.AppNameNormalizer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class InstalledAppRepository implements AppRepository {
    private final Context context;
    private final PackageManager packageManager;
    private final Collator labelCollator = Collator.getInstance(new Locale("vi", "VN"));

    public InstalledAppRepository(Context context) {
        this.context = context.getApplicationContext();
        this.packageManager = context.getPackageManager();
    }

    @Override
    public List<ActivityInfo> loadLaunchableApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        List<ActivityInfo> result = new ArrayList<>();
        for (ResolveInfo resolveInfo : found) {
            ActivityInfo app = resolveInfo.activityInfo;
            if (app != null && !app.packageName.equals(context.getPackageName())) result.add(app);
        }
        return result;
    }

    @Override
    public List<ActivityInfo> filterAndSort(List<ActivityInfo> apps, CharSequence query) {
        List<ActivityInfo> filtered = new ArrayList<>();
        String normalizedQuery = AppNameNormalizer.normalize(query);
        for (ActivityInfo app : apps) {
            if (normalizedQuery.isEmpty()
                    || AppNameNormalizer.normalize(label(app)).contains(normalizedQuery)) {
                filtered.add(app);
            }
        }
        Collections.sort(filtered, (left, right) -> {
            int byLabel = labelCollator.compare(label(left), label(right));
            if (byLabel != 0) return byLabel;
            int byPackage = left.packageName.compareToIgnoreCase(right.packageName);
            if (byPackage != 0) return byPackage;
            return left.name.compareToIgnoreCase(right.name);
        });
        return filtered;
    }

    @Override
    public String label(ActivityInfo app) {
        return app.loadLabel(packageManager).toString();
    }
}
