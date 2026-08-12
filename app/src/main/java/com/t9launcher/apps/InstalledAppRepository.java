package com.t9launcher.apps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.t9launcher.search.AppNameMatcher;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class InstalledAppRepository implements AppRepository {
    private final Context context;
    private final PackageManager packageManager;
    private final Collator labelCollator = Collator.getInstance(new Locale("vi", "VN"));
    private final LruCache<String, Drawable> iconCache = new LruCache<>(48);

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
        List<MatchedApp> matches = new ArrayList<>();
        for (ActivityInfo app : apps) {
            String appLabel = label(app);
            int score = AppNameMatcher.score(appLabel, query);
            if (score != AppNameMatcher.NO_MATCH) matches.add(new MatchedApp(app, appLabel, score));
        }
        Collections.sort(matches, (left, right) -> {
            int byScore = Integer.compare(right.score, left.score);
            if (byScore != 0) return byScore;
            int byLabel = labelCollator.compare(left.label, right.label);
            if (byLabel != 0) return byLabel;
            int byPackage = left.app.packageName.compareToIgnoreCase(right.app.packageName);
            if (byPackage != 0) return byPackage;
            return left.app.name.compareToIgnoreCase(right.app.name);
        });
        List<ActivityInfo> filtered = new ArrayList<>(matches.size());
        for (MatchedApp match : matches) filtered.add(match.app);
        return filtered;
    }

    @Override
    public String label(ActivityInfo app) {
        return app.loadLabel(packageManager).toString();
    }

    @Override
    public Drawable icon(ActivityInfo app) {
        String key = app.packageName + "/" + app.name;
        Drawable cached = iconCache.get(key);
        if (cached != null) return cached;
        Drawable loaded = app.loadIcon(packageManager);
        if (loaded != null) iconCache.put(key, loaded);
        return loaded;
    }

    private static final class MatchedApp {
        final ActivityInfo app;
        final String label;
        final int score;

        MatchedApp(ActivityInfo app, String label, int score) {
            this.app = app;
            this.label = label;
            this.score = score;
        }
    }
}
