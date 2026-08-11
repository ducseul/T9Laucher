package com.t9launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class LauncherView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final List<ActivityInfo> apps = new ArrayList<>();
    private int screen = 0; // 0 home, 1 drawer, 3 settings, 4 app picker
    private int selected = 0;
    private int settingsSelection = 0;
    private int homeOffset = 0;
    private int drawerOffset = 0;
    private int settingsOffset = 0;
    private int bindingSlot = 0;
    private int pickerSelection = 0;
    private int pickerOffset = 0;
    private int homeCount = 4;
    private int wallpaperIndex = 0;
    private int fontSizeSp = 14;
    private boolean showStatusBar = true;
    private final int[] bindings = new int[9];
    private final Editable query = new SpannableStringBuilder();
    private boolean locked;
    private boolean silent;
    private float touchDownY;
    private float touchLastY;
    private int touchScreen;
    private int touchIndex = -1;
    private boolean touchMoved;
    private boolean holdTriggered;
    private final Handler touchHandler = new Handler();
    private Runnable holdAction;
    private final int amber = Color.rgb(255, 180, 84);

    public LauncherView(Context c) {
        super(c);
        d = getResources().getDisplayMetrics().density;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        Selection.setSelection(query, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }
        for (int i = 0; i < bindings.length; i++) bindings[i] = i;
        loadPrefs();
        loadApps();
        silent = ((MainActivity) c).isVibrateMode();
    }

    private float dp(float n) {
        return n * d;
    }

    public void refreshApps() {
        loadApps();
        if (screen == 1) {
            int count = drawerApps().size();
            selected = count == 0 ? 0 : Math.min(selected, count - 1);
        }
        invalidate();
    }

    public boolean shouldShowStatusBar() {
        return showStatusBar;
    }

    public void goHome() {
        if (screen == 3 || screen == 4) savePrefs();
        locked = false;
        screen = 0;
        query.clear();
        refreshTextInput();
        invalidate();
    }

    public boolean isDrawerTextInputActive() {
        return screen == 1 && !locked;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return isDrawerTextInputActive();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (!isDrawerTextInputActive()) return null;
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        outAttrs.initialSelStart = Selection.getSelectionStart(query);
        outAttrs.initialSelEnd = Selection.getSelectionEnd(query);
        return new BaseInputConnection(this, true) {
            @Override
            public Editable getEditable() {
                return query;
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                boolean handled = super.commitText(text, newCursorPosition);
                onQueryChanged();
                return handled;
            }

            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                boolean handled = super.setComposingText(text, newCursorPosition);
                onQueryChanged();
                return handled;
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                boolean handled = super.deleteSurroundingText(beforeLength, afterLength);
                onQueryChanged();
                return handled;
            }

            @Override
            public boolean performEditorAction(int actionCode) {
                if (actionCode == EditorInfo.IME_ACTION_DONE) {
                    launchSelected();
                    return true;
                }
                return super.performEditorAction(actionCode);
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isDrawerTextInputActive()) return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            deleteQueryCharacter();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_STAR) {
            deleteQueryCharacter();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_POUND) {
            query.clear();
            onQueryChanged();
            return true;
        }
        int unicode = event.getUnicodeChar();
        if (unicode != 0 && (Character.isLetter(unicode) || Character.isWhitespace(unicode))) {
            replaceQuerySelection(new String(Character.toChars(unicode)));
            return true;
        }
        // Raw number keys must be left for the configured T9 IME (for example QinVN).
        return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
    }

    private void replaceQuerySelection(CharSequence text) {
        int start = Math.max(0, Selection.getSelectionStart(query));
        int end = Math.max(0, Selection.getSelectionEnd(query));
        query.replace(Math.min(start, end), Math.max(start, end), text);
        Selection.setSelection(query, Math.min(start, end) + text.length());
        onQueryChanged();
    }

    private void deleteQueryCharacter() {
        int start = Math.max(0, Selection.getSelectionStart(query));
        int end = Math.max(0, Selection.getSelectionEnd(query));
        if (start != end) {
            query.delete(Math.min(start, end), Math.max(start, end));
            Selection.setSelection(query, Math.min(start, end));
        } else if (start > 0) {
            int previous = Character.offsetByCodePoints(query, start, -1);
            query.delete(previous, start);
            Selection.setSelection(query, previous);
        }
        onQueryChanged();
    }

    private void onQueryChanged() {
        selected = 0;
        drawerOffset = 0;
        invalidate();
    }

    private void refreshTextInput() {
        requestFocus();
        post(() -> {
            InputMethodManager input = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (input == null) return;
            input.restartInput(this);
            if (isDrawerTextInputActive()) {
                input.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            } else {
                input.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        });
    }

    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getContext().getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_ALL);
        apps.clear();
        for (ResolveInfo result : found) {
            ActivityInfo app = result.activityInfo;
            if (!app.packageName.equals(getContext().getPackageName())) apps.add(app);
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(backgroundColor());
        if (locked) {
            drawLock(c);
            return;
        }
        drawStatus(c);
        if (screen == 0) drawHome(c);
        else if (screen == 1) drawDrawer(c);
        else if (screen == 3) drawSettings(c);
        else if (screen == 4) drawAppPicker(c);
        else drawHome(c);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(size));
        p.setColor(color);
        c.drawText(s, x, y, p);
    }

    private void mono(Canvas c, String s, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        p.setTextSize(dp(size));
        p.setColor(color);
        c.drawText(s, x, y, p);
    }

    private void drawStatus(Canvas c) {
        if (silent) text(c, "RUNG", getWidth() - dp(44), dp(18), 8, amber);
    }

    private void drawHome(Canvas c) {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        p.setTextAlign(Paint.Align.CENTER);
        float clockSizeSp = Math.max(28, Math.min(38, fontSizeSp * 2));
        mono(c, time, getWidth() / 2f, dp(66), clockSizeSp, Color.rgb(243, 239, 231));
        text(c, new SimpleDateFormat("EEE, dd/MM", Locale.US).format(new Date()).toUpperCase(Locale.US),
                getWidth() / 2f, dp(86), 10, Color.rgb(139, 138, 144));
        p.setTextAlign(Paint.Align.LEFT);
        p.setColor(Color.rgb(43, 43, 47));
        c.drawRect(dp(12), dp(100), getWidth() - dp(12), dp(101), p);

        selected = Math.max(0, Math.min(homeCount - 1, selected));
        int visible = visibleRows(130, 48, 20);
        homeOffset = keepSelectionVisible(selected, homeCount, visible, homeOffset);
        int count = Math.min(visible, homeCount - homeOffset);
        for (int row = 0; row < count; row++) {
            int slot = homeOffset + row;
            float y = dp(130 + row * 48);
            if (slot == selected) {
                p.setColor(amber);
                c.drawRoundRect(new RectF(0, y - dp(18), dp(3), y + dp(2)),
                        dp(1.5f), dp(1.5f), p);
            }
            mono(c, String.valueOf(slot + 1), dp(14), y, 12, amber);
            text(c, appLabel(bindings[slot]), dp(48), y, fontSizeSp,
                    slot == selected ? amber : Color.rgb(243, 239, 231));
        }
    }

    private void drawDrawer(Canvas c) {
        float unit = 1f / d;
        float inset = 16f * unit;
        float searchTop = 56f * unit;
        float searchBottom = 114f * unit;
        float listTop = drawerListTopDp();
        mono(c, "TẤT CẢ ỨNG DỤNG", 30f, 39f,
                10, Color.rgb(141, 141, 146));
        p.setColor(Color.rgb(36, 36, 40));
        c.drawRoundRect(new RectF(dp(inset), dp(searchTop), getWidth() - dp(inset),
                dp(searchBottom)), 10f, 10f, p);
        mono(c, "T9", 35f, 94f, 15, amber);
        text(c, query.length() == 0 ? "Tìm app…" : query.toString(), 78f, 94f,
                Math.max(14, Math.min(20, fontSizeSp - 3)),
                query.length() == 0 ? Color.rgb(170, 169, 173) : Color.WHITE);

        List<ActivityInfo> filtered = drawerApps();
        if (filtered.isEmpty()) {
            selected = 0;
            drawerOffset = 0;
            text(c, "Không tìm thấy app", 30f, dp(listTop) + 38f,
                    fontSizeSp, Color.GRAY);
            return;
        }

        selected = Math.max(0, Math.min(filtered.size() - 1, selected));
        float rowHeight = drawerRowHeightDp();
        float rowStep = drawerRowStepDp();
        int visible = drawerVisibleRows();
        drawerOffset = keepSelectionVisible(selected, filtered.size(), visible, drawerOffset);
        int end = Math.min(filtered.size(), drawerOffset + visible);
        for (int index = drawerOffset; index < end; index++) {
            String label = appLabel(filtered.get(index));
            float rowTop = listTop + (index - drawerOffset) * rowStep;
            if (index == selected) {
                p.setColor(Color.rgb(73, 55, 35));
                c.drawRoundRect(new RectF(dp(inset), dp(rowTop),
                        getWidth() - dp(inset), dp(rowTop + rowHeight)),
                        9f, 9f, p);
                p.setColor(amber);
                c.drawRoundRect(new RectF(dp(inset), dp(rowTop + 14f * unit),
                        dp(inset) + 4f, dp(rowTop + rowHeight - 14f * unit)),
                        2f, 2f, p);
            }
            p.setTextSize(dp(fontSizeSp));
            Paint.FontMetrics metrics = p.getFontMetrics();
            float baselinePx = dp(rowTop)
                    + (dp(rowHeight) - (metrics.bottom - metrics.top)) / 2f - metrics.top;
            text(c, label, 36f, baselinePx, fontSizeSp,
                    index == selected ? amber : Color.rgb(245, 242, 236));
        }
    }

    private float drawerListTopDp() {
        return 136f / d;
    }

    private float drawerRowHeightDp() {
        return Math.max(64f / d, fontSizeSp + 14f / d);
    }

    private float drawerRowStepDp() {
        return drawerRowHeightDp() + 4f / d;
    }

    private int drawerVisibleRows() {
        float bottomPadding = 16f / d;
        float available = getHeight() / d - bottomPadding - drawerListTopDp();
        return Math.max(1, (int) Math.floor((available + 4f / d) / drawerRowStepDp()));
    }

    private void drawSettings(Canvas c) {
        mono(c, "CẤU HÌNH LAUNCHER", dp(16), dp(44), fontSizeSp, amber);
        text(c, "▲▼ chọn · ◀▶ chỉnh giá trị · OK mở/chọn · Back lưu", dp(16), dp(70),
                Math.max(10, fontSizeSp - 5), Color.GRAY);
        String[] rows = {"Màu / wallpaper", "Cỡ chữ", "Số app ở Home", "Hiển thị Thanh thông báo"};
        int rowStep = Math.max(38, fontSizeSp + 22);
        int total = rows.length + homeCount;
        settingsSelection = Math.max(0, Math.min(total - 1, settingsSelection));
        int visible = visibleRows(104, rowStep, 44);
        settingsOffset = keepSelectionVisible(settingsSelection, total, visible, settingsOffset);
        int end = Math.min(total, settingsOffset + visible);
        for (int i = settingsOffset; i < end; i++) {
            int y = 104 + (i - settingsOffset) * rowStep;
            if (i == settingsSelection) {
                p.setColor(amber);
                c.drawRoundRect(new RectF(0, dp(y - 18), dp(3), dp(y + 2)),
                        dp(1.5f), dp(1.5f), p);
            }
            if (i < rows.length) {
                text(c, rows[i], dp(18), dp(y), fontSizeSp,
                        i == settingsSelection ? amber : Color.WHITE);
                p.setTextAlign(Paint.Align.RIGHT);
                text(c, settingValue(i), getWidth() - dp(18), dp(y), fontSizeSp, amber);
                p.setTextAlign(Paint.Align.LEFT);
            } else {
                int slot = i - rows.length;
                mono(c, String.valueOf(slot + 1), dp(18), dp(y), fontSizeSp, amber);
                text(c, appLabel(bindings[slot]), dp(52), dp(y), fontSizeSp,
                        i == settingsSelection ? amber : Color.WHITE);
            }
        }
        if (settingsSelection == 1) {
            text(c, "Aa  Ví dụ cỡ chữ " + fontSizeSp + "sp", dp(18),
                    getHeight() - dp(22), fontSizeSp, amber);
        }
    }

    private void drawAppPicker(Canvas c) {
        mono(c, "CHỌN APP CHO PHÍM " + (bindingSlot + 1), dp(16), dp(44), fontSizeSp, amber);
        text(c, "▲▼ chọn · OK xác nhận · Back huỷ", dp(16), dp(70),
                Math.max(10, fontSizeSp - 4), Color.GRAY);
        int total = apps.size() + 1;
        pickerSelection = Math.max(0, Math.min(total - 1, pickerSelection));
        int rowStep = Math.max(38, fontSizeSp + 22);
        int visible = visibleRows(104, rowStep, 18);
        pickerOffset = keepSelectionVisible(pickerSelection, total, visible, pickerOffset);
        int end = Math.min(total, pickerOffset + visible);
        for (int index = pickerOffset; index < end; index++) {
            int y = 104 + (index - pickerOffset) * rowStep;
            String label = index == 0 ? "Chưa gán" : appLabel(apps.get(index - 1));
            if (index == pickerSelection) {
                p.setColor(Color.rgb(72, 58, 43));
                c.drawRoundRect(new RectF(dp(10), dp(y - fontSizeSp - 9),
                        getWidth() - dp(10), dp(y + 8)), dp(5), dp(5), p);
            }
            text(c, label, dp(20), dp(y), fontSizeSp,
                    index == pickerSelection ? amber : Color.WHITE);
        }
    }

    private void drawLock(Canvas c) {
        c.drawColor(Color.BLACK);
        p.setTextAlign(Paint.Align.CENTER);
        mono(c, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()),
                getWidth() / 2f, dp(130), 38, Color.WHITE);
        text(c, "Nhấn phím bất kỳ để mở khoá", getWidth() / 2f, dp(178), 10, Color.GRAY);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private int backgroundColor() {
        return new int[]{Color.rgb(22, 22, 24), Color.rgb(38, 19, 14),
                Color.rgb(8, 20, 24), Color.rgb(10, 24, 13)}[wallpaperIndex % 4];
    }

    private String settingValue(int row) {
        if (row == 0) return new String[]{"Than chì", "Hoàng hôn", "Đại dương", "Rừng"}[wallpaperIndex % 4];
        if (row == 1) return fontSizeSp + " sp";
        if (row == 2) return String.valueOf(homeCount);
        return showStatusBar ? "[x]" : "[ ]";
    }

    private String appLabel(int index) {
        if (index >= 0 && index < apps.size()) return appLabel(apps.get(index));
        return "Chưa gán";
    }

    private String appLabel(ActivityInfo app) {
        return app.loadLabel(getContext().getPackageManager()).toString();
    }

    private List<ActivityInfo> drawerApps() {
        List<ActivityInfo> filtered = new ArrayList<>();
        String normalizedQuery = norm(query.toString());
        for (ActivityInfo app : apps) {
            String label = appLabel(app);
            if (normalizedQuery.length() == 0 || norm(label).contains(normalizedQuery)) filtered.add(app);
        }
        return filtered;
    }

    private int visibleRows(int firstBaselineDp, int rowStepDp, int bottomPaddingDp) {
        float available = getHeight() / d - bottomPaddingDp - firstBaselineDp;
        return Math.max(1, (int) Math.floor(available / rowStepDp) + 1);
    }

    private static int keepSelectionVisible(int selection, int itemCount,
                                            int visibleCount, int currentOffset) {
        int maxOffset = Math.max(0, itemCount - visibleCount);
        int offset = Math.max(0, Math.min(maxOffset, currentOffset));
        if (selection < offset) offset = selection;
        else if (selection >= offset + visibleCount) offset = selection - visibleCount + 1;
        return Math.max(0, Math.min(maxOffset, offset));
    }

    private void loadPrefs() {
        SharedPreferences settings = getContext().getSharedPreferences("launcher", Context.MODE_PRIVATE);
        homeCount = Math.max(1, Math.min(9, settings.getInt("homeCount", 4)));
        wallpaperIndex = Math.max(0, Math.min(3, settings.getInt("wallpaper", 0)));
        fontSizeSp = Math.max(12, Math.min(36, settings.getInt("fontSizeSp", 14)));
        showStatusBar = settings.getBoolean("showStatusBar", true);
        for (int i = 0; i < bindings.length; i++) bindings[i] = settings.getInt("binding" + i, i);
    }

    private void savePrefs() {
        SharedPreferences.Editor editor = getContext()
                .getSharedPreferences("launcher", Context.MODE_PRIVATE).edit();
        editor.putInt("homeCount", homeCount)
                .putInt("wallpaper", wallpaperIndex)
                .putInt("fontSizeSp", fontSizeSp)
                .putBoolean("showStatusBar", showStatusBar);
        for (int i = 0; i < bindings.length; i++) editor.putInt("binding" + i, bindings[i]);
        editor.apply();
    }

    private boolean adjustSelectedSetting(int delta) {
        if (settingsSelection == 0) {
            wallpaperIndex = (wallpaperIndex + delta + 4) % 4;
        } else if (settingsSelection == 1) {
            fontSizeSp = Math.max(12, Math.min(36, fontSizeSp + delta));
        } else if (settingsSelection == 2) {
            homeCount = Math.max(1, Math.min(9, homeCount + delta));
        } else {
            return false;
        }
        savePrefs();
        invalidate();
        return true;
    }

    private void changeSetting() {
        if (settingsSelection == 0) wallpaperIndex = (wallpaperIndex + 1) % 4;
        else if (settingsSelection == 1) fontSizeSp = Math.min(36, fontSizeSp + 1);
        else if (settingsSelection == 2) homeCount = homeCount % 9 + 1;
        else if (settingsSelection == 3) {
            showStatusBar = !showStatusBar;
            ((MainActivity) getContext()).setStatusBarVisible(showStatusBar);
        }
        else {
            bindingSlot = settingsSelection - 4;
            int current = bindings[bindingSlot];
            pickerSelection = current >= 0 && current < apps.size() ? current + 1 : 0;
            pickerOffset = 0;
            screen = 4;
        }
        savePrefs();
        invalidate();
    }

    private static String norm(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase(Locale.US);
    }

    public void onKey(String key, boolean hold) {
        if (key.equals("corner3")) {
            locked = false;
            savePrefs();
            ((MainActivity) getContext()).openSystemDialer();
            return;
        }
        if (locked) {
            locked = false;
            invalidate();
            return;
        }
        if (hold && key.equals("#")) {
            silent = ((MainActivity) getContext()).toggleRingerMode();
            invalidate();
            return;
        }
        if (key.equals("back") || key.equals("corner2")) {
            if (screen == 4) {
                screen = 3;
                invalidate();
                return;
            }
            if (screen != 0) {
                if (screen == 3) savePrefs();
                screen = 0;
                query.clear();
                refreshTextInput();
                invalidate();
            }
            return;
        }
        if (key.equals("hangup")) {
            goHome();
            return;
        }
        if (screen == 3 && (key.equals("left") || key.equals("right"))) {
            if (adjustSelectedSetting(key.equals("left") ? -1 : 1)) return;
        }
        if (screen == 3 && (key.equals("up") || key.equals("left"))) {
            int limit = 4 + homeCount;
            settingsSelection = (settingsSelection - 1 + limit) % limit;
            invalidate();
            return;
        }
        if (screen == 3 && (key.equals("down") || key.equals("right"))) {
            int limit = 4 + homeCount;
            settingsSelection = (settingsSelection + 1) % limit;
            invalidate();
            return;
        }
        if (screen == 4 && (key.equals("up") || key.equals("left"))) {
            int limit = apps.size() + 1;
            pickerSelection = (pickerSelection - 1 + limit) % limit;
            invalidate();
            return;
        }
        if (screen == 4 && (key.equals("down") || key.equals("right"))) {
            int limit = apps.size() + 1;
            pickerSelection = (pickerSelection + 1) % limit;
            invalidate();
            return;
        }
        if ((screen == 0 || screen == 1) && (key.equals("up") || key.equals("left"))) {
            int limit = screen == 0 ? homeCount : Math.max(1, drawerApps().size());
            selected = (selected - 1 + limit) % limit;
            invalidate();
            return;
        }
        if ((screen == 0 || screen == 1) && (key.equals("down") || key.equals("right"))) {
            int limit = screen == 0 ? homeCount : Math.max(1, drawerApps().size());
            selected = (selected + 1) % limit;
            invalidate();
            return;
        }
        if (key.equals("ok")) {
            if (screen == 0) launchSlot();
            else if (screen == 1) launchSelected();
            else if (screen == 3) changeSetting();
            else if (screen == 4) {
                bindings[bindingSlot] = pickerSelection - 1;
                savePrefs();
                screen = 3;
                settingsSelection = bindingSlot + 4;
                invalidate();
            }
            return;
        }
        if (key.equals("corner1")) {
            screen = 1;
            selected = 0;
            drawerOffset = 0;
            refreshTextInput();
            invalidate();
            return;
        }
        if (key.equals("corner4")) {
            if (screen == 0) ((MainActivity) getContext()).lockDeviceOrRequestAdmin();
            else goHome();
            return;
        }
        if (screen == 0 && key.matches("[1-9]")) {
            selected = Math.min(homeCount - 1, Integer.parseInt(key) - 1);
            launchSlot();
            return;
        }
        if (screen == 1) {
            if (key.equals("*")) deleteQueryCharacter();
            else if (key.equals("#")) {
                query.clear();
                onQueryChanged();
            }
            return;
        }
    }

    private void launchSlot() {
        int index = selected < bindings.length ? bindings[selected] : -1;
        if (index >= 0 && index < apps.size()) launch(apps.get(index));
    }

    private void launchSelected() {
        List<ActivityInfo> filtered = drawerApps();
        if (selected >= 0 && selected < filtered.size()) launch(filtered.get(selected));
    }

    private void launch(ActivityInfo app) {
        Intent intent = new Intent();
        intent.setClassName(app.packageName, app.name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    private int touchedIndex(float yPx) {
        float y = yPx / d;
        if (screen == 0) {
            int row = (int) Math.floor((y - 105) / 48f);
            int index = homeOffset + row;
            return row >= 0 && index >= 0 && index < homeCount ? index : -1;
        }
        if (screen == 1) {
            float relative = y - drawerListTopDp();
            int row = (int) Math.floor(relative / drawerRowStepDp());
            if (row < 0 || relative - row * drawerRowStepDp() > drawerRowHeightDp()) return -1;
            int index = drawerOffset + row;
            return index < drawerApps().size() ? index : -1;
        }
        int rowStep = Math.max(38, fontSizeSp + 22);
        float firstTop = 104 - fontSizeSp - 9;
        int row = (int) Math.floor((y - firstTop) / rowStep);
        if (row < 0) return -1;
        if (screen == 3) {
            int index = settingsOffset + row;
            return index < 4 + homeCount ? index : -1;
        }
        if (screen == 4) {
            int index = pickerOffset + row;
            return index < apps.size() + 1 ? index : -1;
        }
        return -1;
    }

    private void selectTouchedIndex(int touchedScreen, int index) {
        if (index < 0) return;
        if (touchedScreen == 0 || touchedScreen == 1) selected = index;
        else if (touchedScreen == 3) settingsSelection = index;
        else if (touchedScreen == 4) pickerSelection = index;
        invalidate();
    }

    private void activateTouchedIndex(int touchedScreen) {
        if (screen != touchedScreen) return;
        if (screen == 0) launchSlot();
        else if (screen == 1) launchSelected();
        else if (screen == 3) changeSetting();
        else if (screen == 4) {
            bindings[bindingSlot] = pickerSelection - 1;
            savePrefs();
            screen = 3;
            settingsSelection = bindingSlot + 4;
            invalidate();
        }
    }

    private void moveTouchSelection(int touchedScreen, int delta) {
        if (delta == 0 || screen != touchedScreen) return;
        if (screen == 0) {
            selected = Math.max(0, Math.min(homeCount - 1, selected + delta));
            homeOffset = keepSelectionVisible(selected, homeCount,
                    visibleRows(130, 48, 20), homeOffset);
        } else if (screen == 1) {
            int total = drawerApps().size();
            if (total == 0) return;
            selected = Math.max(0, Math.min(total - 1, selected + delta));
            drawerOffset = keepSelectionVisible(selected, total,
                    drawerVisibleRows(), drawerOffset);
        } else if (screen == 3) {
            int total = 4 + homeCount;
            settingsSelection = Math.max(0, Math.min(total - 1, settingsSelection + delta));
            int rowStep = Math.max(38, fontSizeSp + 22);
            settingsOffset = keepSelectionVisible(settingsSelection, total,
                    visibleRows(104, rowStep, 44), settingsOffset);
        } else if (screen == 4) {
            int total = apps.size() + 1;
            pickerSelection = Math.max(0, Math.min(total - 1, pickerSelection + delta));
            int rowStep = Math.max(38, fontSizeSp + 22);
            pickerOffset = keepSelectionVisible(pickerSelection, total,
                    visibleRows(104, rowStep, 18), pickerOffset);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownY = event.getY();
            touchLastY = event.getY();
            touchScreen = screen;
            touchIndex = touchedIndex(event.getY());
            touchMoved = false;
            holdTriggered = false;
            getParent().requestDisallowInterceptTouchEvent(true);
            selectTouchedIndex(touchScreen, touchIndex);
            if (touchIndex >= 0) {
                holdAction = () -> {
                    holdTriggered = true;
                    activateTouchedIndex(touchScreen);
                };
                touchHandler.postDelayed(holdAction, 700);
            } else if (screen == 0 && event.getY() > dp(130 + homeCount * 48)) {
                holdAction = () -> {
                    holdTriggered = true;
                    screen = 3;
                    settingsSelection = 0;
                    settingsOffset = 0;
                    invalidate();
                };
                touchHandler.postDelayed(holdAction, 700);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float distance = event.getY() - touchLastY;
            float threshold = dp(touchScreen == 0 ? 24 : Math.max(24, (fontSizeSp + 22) / 2f));
            if (Math.abs(event.getY() - touchDownY) >= dp(18)) {
                if (holdAction != null) touchHandler.removeCallbacks(holdAction);
                holdAction = null;
                touchMoved = true;
            }
            if (touchIndex >= 0 && Math.abs(distance) >= threshold) {
                int steps = Math.max(1, (int) (Math.abs(distance) / threshold));
                moveTouchSelection(touchScreen, distance < 0 ? steps : -steps);
                touchLastY = event.getY();
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (holdAction != null) {
                touchHandler.removeCallbacks(holdAction);
                holdAction = null;
            }
            float dy = event.getY() - touchDownY;
            if (!locked && touchScreen == 0 && touchIndex < 0
                    && dy < -dp(80)) {
                screen = 1;
                selected = 0;
                drawerOffset = 0;
                query.clear();
                refreshTextInput();
                invalidate();
                return true;
            }
            if (!holdTriggered && !touchMoved && touchIndex >= 0) {
                activateTouchedIndex(touchScreen);
            }
            getParent().requestDisallowInterceptTouchEvent(false);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (holdAction != null) touchHandler.removeCallbacks(holdAction);
            holdAction = null;
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return true;
    }
}
