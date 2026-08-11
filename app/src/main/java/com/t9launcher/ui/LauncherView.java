package com.t9launcher.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.t9launcher.apps.AppRepository;
import com.t9launcher.apps.InstalledAppRepository;
import com.t9launcher.data.LauncherSettingsStore;
import com.t9launcher.data.SharedPreferencesLauncherSettingsStore;
import com.t9launcher.input.DrawerTextInput;
import com.t9launcher.input.LauncherKey;
import com.t9launcher.model.LauncherConfiguration;
import com.t9launcher.system.LauncherActions;

import static com.t9launcher.model.LauncherConfiguration.ACTION_CONTACTS;
import static com.t9launcher.model.LauncherConfiguration.ACTION_MESSAGING;
import static com.t9launcher.model.LauncherConfiguration.ACTION_NONE;
import static com.t9launcher.model.LauncherConfiguration.HOME_KEYS_DIALER;
import static com.t9launcher.model.LauncherConfiguration.HOME_KEYS_QUICK_ACTION;

@SuppressLint("ViewConstructor")
public final class LauncherView extends View {
    private static final int SETTINGS_ROW_COUNT = 7;
    private static final int SETTING_HOME_KEY_BEHAVIOR = 4;
    private static final int SETTING_SWIPE_LEFT_TO_RIGHT = 5;
    private static final int SETTING_SWIPE_RIGHT_TO_LEFT = 6;
    private static final float SETTINGS_FIRST_BASELINE_DP = 104f;
    private static final float SETTINGS_SECTION_GAP_DP = 34f;
    private static final float SETTINGS_BOTTOM_PADDING_DP = 44f;
    private static final int PICKER_HOME_SLOT = 0;
    private static final int PICKER_SWIPE_LEFT_TO_RIGHT = 1;
    private static final int PICKER_SWIPE_RIGHT_TO_LEFT = 2;
    private static final long DRAWER_LAUNCH_DEBOUNCE_MS = 300L;

    private static final class HomeLayout {
        final float clockSizeSp;
        final float dateSizeSp;
        final float clockBaselineDp;
        final float dateBaselineDp;
        final float dividerDp;
        final float firstRowTopDp;
        final float firstRowBaselineDp;
        final float rowHeightDp;
        final float rowStepDp;

        HomeLayout(float clockSizeSp, float dateSizeSp, float clockBaselineDp,
                   float dateBaselineDp, float dividerDp, float firstRowTopDp,
                   float firstRowBaselineDp, float rowHeightDp, float rowStepDp) {
            this.clockSizeSp = clockSizeSp;
            this.dateSizeSp = dateSizeSp;
            this.clockBaselineDp = clockBaselineDp;
            this.dateBaselineDp = dateBaselineDp;
            this.dividerDp = dividerDp;
            this.firstRowTopDp = firstRowTopDp;
            this.firstRowBaselineDp = firstRowBaselineDp;
            this.rowHeightDp = rowHeightDp;
            this.rowStepDp = rowStepDp;
        }
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final LauncherActions actions;
    private final LauncherSettingsStore settingsStore;
    private final AppRepository appRepository;
    private final List<ActivityInfo> apps = new ArrayList<>();
    private LauncherScreen screen = LauncherScreen.HOME;
    private int selected = 0;
    private int settingsSelection = 0;
    private int homeOffset = 0;
    private int drawerOffset = 0;
    private int settingsOffset = 0;
    private long settingsHintScrollStartedAt = 0L;
    private int bindingSlot = 0;
    private int pickerTarget = PICKER_HOME_SLOT;
    private int pickerSelection = 0;
    private int pickerOffset = 0;
    private long lastDrawerLaunchAt = 0L;
    private int homeCount = 4;
    private int wallpaperIndex = 0;
    private int fontSizeSp = 14;
    private boolean showStatusBar = true;
    private int homeKeyBehavior = HOME_KEYS_QUICK_ACTION;
    private int swipeLeftToRightAction = ACTION_CONTACTS;
    private int swipeRightToLeftAction = ACTION_MESSAGING;
    private final int[] bindings = new int[9];
    private final DrawerTextInput drawerTextInput;
    private boolean locked;
    private boolean silent;
    private float touchDownX;
    private float touchDownY;
    private float touchLastY;
    private LauncherScreen touchScreen;
    private int touchIndex = -1;
    private boolean touchMoved;
    private boolean holdTriggered;
    private final Handler touchHandler = new Handler();
    private Runnable holdAction;
    private final int amber = Color.rgb(255, 180, 84);

    public LauncherView(Context c, LauncherActions actions) {
        this(c, actions, new SharedPreferencesLauncherSettingsStore(c),
                new InstalledAppRepository(c));
    }

    LauncherView(Context c, LauncherActions actions,
                 LauncherSettingsStore settingsStore, AppRepository appRepository) {
        super(c);
        this.actions = actions;
        this.settingsStore = settingsStore;
        this.appRepository = appRepository;
        drawerTextInput = new DrawerTextInput(this, new DrawerTextInput.Listener() {
            @Override
            public void onQueryChanged() {
                selected = 0;
                drawerOffset = 0;
                invalidate();
            }

            @Override
            public void onSubmit() {
                launchSelected();
            }
        });
        d = getResources().getDisplayMetrics().density;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }
        for (int i = 0; i < bindings.length; i++) bindings[i] = i;
        loadPrefs();
        loadApps();
        silent = actions.isVibrateMode();
    }

    private float dp(float n) {
        return n * d;
    }

    public void refreshApps() {
        loadApps();
        if (screen == LauncherScreen.DRAWER) {
            int count = drawerApps().size();
            selected = count == 0 ? 0 : Math.min(selected, count - 1);
        }
        invalidate();
    }

    public boolean shouldShowStatusBar() {
        return showStatusBar;
    }

    public void goHome() {
        if (screen == LauncherScreen.SETTINGS || screen == LauncherScreen.APP_PICKER) savePrefs();
        locked = false;
        screen = LauncherScreen.HOME;
        settingsHintScrollStartedAt = 0L;
        drawerTextInput.clear();
        drawerTextInput.refresh(false);
        invalidate();
    }

    public boolean isDrawerTextInputActive() {
        return screen == LauncherScreen.DRAWER && !locked;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return isDrawerTextInputActive();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (!isDrawerTextInputActive()) return null;
        return drawerTextInput.createInputConnection(outAttrs);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isDrawerTextInputActive()) return super.onKeyDown(keyCode, event);
        return drawerTextInput.onKeyDown(keyCode, event);
    }

    private void loadApps() {
        apps.clear();
        apps.addAll(appRepository.loadLaunchableApps());
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
        if (screen == LauncherScreen.HOME) drawHome(c);
        else if (screen == LauncherScreen.DRAWER) drawDrawer(c);
        else if (screen == LauncherScreen.SETTINGS) drawSettings(c);
        else if (screen == LauncherScreen.APP_PICKER) drawAppPicker(c);
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
        Date now = new Date();
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);
        HomeLayout layout = homeLayout();
        p.setTextAlign(Paint.Align.CENTER);
        mono(c, time, getWidth() / 2f, dp(layout.clockBaselineDp),
                layout.clockSizeSp, Color.rgb(243, 239, 231));
        text(c, vietnameseDate(now), getWidth() / 2f, dp(layout.dateBaselineDp),
                layout.dateSizeSp,
                Color.rgb(139, 138, 144));
        p.setTextAlign(Paint.Align.LEFT);
        p.setColor(Color.rgb(43, 43, 47));
        c.drawRect(dp(12), dp(layout.dividerDp), getWidth() - dp(12),
                dp(layout.dividerDp + 1), p);

        selected = Math.max(0, Math.min(homeCount - 1, selected));
        int visible = homeVisibleRows(layout);
        homeOffset = keepSelectionVisible(selected, homeCount, visible, homeOffset);
        int count = Math.min(visible, homeCount - homeOffset);
        for (int row = 0; row < count; row++) {
            int slot = homeOffset + row;
            float rowTopDp = layout.firstRowTopDp + row * layout.rowStepDp;
            float y = dp(layout.firstRowBaselineDp + row * layout.rowStepDp);
            if (slot == selected) {
                p.setColor(amber);
                c.drawRoundRect(new RectF(0, dp(rowTopDp), dp(3),
                                dp(rowTopDp + layout.rowHeightDp)),
                        dp(1.5f), dp(1.5f), p);
            }
            mono(c, String.valueOf(slot + 1), dp(14), y, 12, amber);
            text(c, appLabel(bindings[slot]), dp(48), y, fontSizeSp,
                    slot == selected ? amber : Color.rgb(243, 239, 231));
        }

        text(c, "Danh sách", dp(12), getHeight() - dp(8), 12, amber);
    }

    private HomeLayout homeLayout() {
        float clockSizeSp = Math.min(36, fontSizeSp + 5);
        float dateSizeSp = Math.max(12, fontSizeSp - 5);

        p.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        p.setTextSize(dp(clockSizeSp));
        Paint.FontMetrics clockMetrics = p.getFontMetrics();
        float clockTopDp = clockMetrics.top / d;
        float clockBottomDp = clockMetrics.bottom / d;
        float clockBaselineDp = 28f - clockTopDp;

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(dateSizeSp));
        Paint.FontMetrics dateMetrics = p.getFontMetrics();
        float dateTopDp = dateMetrics.top / d;
        float dateBottomDp = dateMetrics.bottom / d;
        float dateBaselineDp = clockBaselineDp + clockBottomDp + 6f - dateTopDp;
        float dividerDp = dateBaselineDp + dateBottomDp + 10f;

        p.setTextSize(dp(fontSizeSp));
        Paint.FontMetrics appMetrics = p.getFontMetrics();
        float appTopDp = appMetrics.top / d;
        float appBottomDp = appMetrics.bottom / d;
        float appHeightDp = appBottomDp - appTopDp;
        float rowHeightDp = Math.max(40f, appHeightDp + 12f);
        float rowStepDp = rowHeightDp + 8f;
        float firstRowTopDp = dividerDp + 12f;
        float firstRowBaselineDp = firstRowTopDp
                + (rowHeightDp - appHeightDp) / 2f - appTopDp;

        return new HomeLayout(clockSizeSp, dateSizeSp, clockBaselineDp,
                dateBaselineDp, dividerDp, firstRowTopDp, firstRowBaselineDp,
                rowHeightDp, rowStepDp);
    }

    private int homeVisibleRows(HomeLayout layout) {
        float availableDp = getHeight() / d - 20f - layout.firstRowTopDp;
        if (availableDp <= layout.rowHeightDp) return 1;
        return Math.max(1, (int) Math.floor(
                (availableDp - layout.rowHeightDp) / layout.rowStepDp) + 1);
    }

    private float homeListBottomDp(HomeLayout layout) {
        int count = Math.min(homeVisibleRows(layout), homeCount);
        if (count == 0) return layout.firstRowTopDp;
        return layout.firstRowTopDp + (count - 1) * layout.rowStepDp + layout.rowHeightDp;
    }

    private String vietnameseDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String weekday = dayOfWeek == Calendar.SUNDAY ? "Chủ Nhật" : "Thứ " + dayOfWeek;
        return weekday + " ngày " + new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
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
        text(c, drawerTextInput.isEmpty() ? "Tìm app…" : drawerTextInput.text().toString(), 78f, 94f,
                Math.max(14, Math.min(20, fontSizeSp - 3)),
                drawerTextInput.isEmpty() ? Color.rgb(170, 169, 173) : Color.WHITE);
        p.setTextAlign(Paint.Align.RIGHT);
        text(c, "Quay lại", getWidth() - dp(12), getHeight() - dp(8), 12, amber);
        p.setTextAlign(Paint.Align.LEFT);

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
        float bottomPadding = 20f;
        float available = getHeight() / d - bottomPadding - drawerListTopDp();
        return Math.max(1, (int) Math.floor((available + 4f / d) / drawerRowStepDp()));
    }

    private void drawSettings(Canvas c) {
        mono(c, "CẤU HÌNH LAUNCHER", dp(16), dp(44), fontSizeSp, amber);
        drawSettingsHint(c);
        String[] rows = {"Màu / wallpaper", "Cỡ chữ", "Số app ở Home",
                "Hiển thị Thanh thông báo", "Phím số ở Home",
                "Vuốt trái → phải", "Vuốt phải → trái"};
        int total = rows.length + homeCount;
        settingsSelection = Math.max(0, Math.min(total - 1, settingsSelection));
        settingsOffset = keepSettingsSelectionVisible(settingsSelection, total, settingsOffset);
        int end = settingsVisibleEnd(settingsOffset, total);
        for (int i = settingsOffset; i < end; i++) {
            float y = settingsRowBaselineDp(i, settingsOffset);
            if (isSettingsSectionStart(i)) drawSettingsLegend(c, settingsSectionTitle(i), y);
            if (i == settingsSelection) {
                p.setColor(amber);
                c.drawRoundRect(new RectF(0, dp(y - fontSizeSp - 6), dp(3), dp(y + 6)),
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

    private void drawSettingsHint(Canvas c) {
        String hint = "▲▼ chọn · ◀▶ chỉnh giá trị · OK mở/chọn · Back lưu";
        float sizeSp = 14f;
        float leftPx = dp(16);
        float rightPx = getWidth() - dp(16);
        float availablePx = Math.max(0f, rightPx - leftPx);

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(sizeSp));
        p.setColor(Color.GRAY);
        p.setTextAlign(Paint.Align.LEFT);
        float overflowPx = Math.max(0f, p.measureText(hint) - availablePx);
        float offsetPx = 0f;

        if (overflowPx > 0f) {
            long now = SystemClock.uptimeMillis();
            if (settingsHintScrollStartedAt == 0L) settingsHintScrollStartedAt = now;
            long startPauseMs = 1200L;
            long endPauseMs = 900L;
            long travelMs = Math.max(1L, (long) (overflowPx / dp(22f) * 1000f));
            long cycleMs = startPauseMs + travelMs + endPauseMs;
            long elapsedMs = (now - settingsHintScrollStartedAt) % cycleMs;
            if (elapsedMs > startPauseMs) {
                offsetPx = elapsedMs >= startPauseMs + travelMs
                        ? overflowPx
                        : overflowPx * (elapsedMs - startPauseMs) / travelMs;
            }
            postInvalidateDelayed(16L);
        }

        int saveCount = c.save();
        c.clipRect(leftPx, dp(54), rightPx, dp(76));
        c.drawText(hint, leftPx - offsetPx, dp(70), p);
        c.restoreToCount(saveCount);
    }

    private int settingsRowStepDp() {
        return Math.max(38, fontSizeSp + 22);
    }

    private boolean isSettingsSectionStart(int index) {
        return index == 0 || index == SETTING_HOME_KEY_BEHAVIOR
                || index == SETTING_SWIPE_LEFT_TO_RIGHT || index == SETTINGS_ROW_COUNT;
    }

    private String settingsSectionTitle(int index) {
        if (index == 0) return "HIỂN THỊ";
        if (index == SETTING_HOME_KEY_BEHAVIOR) return "BÀN PHÍM T9 HOME";
        if (index == SETTING_SWIPE_LEFT_TO_RIGHT) return "CỬ CHỈ HOME";
        return "ỨNG DỤNG HOME";
    }

    private float settingsRowBaselineDp(int index, int offset) {
        float baseline = SETTINGS_FIRST_BASELINE_DP;
        int rowStep = settingsRowStepDp();
        for (int current = offset; current <= index; current++) {
            if (isSettingsSectionStart(current)) baseline += SETTINGS_SECTION_GAP_DP;
            if (current == index) return baseline;
            baseline += rowStep;
        }
        return baseline;
    }

    private int settingsVisibleEnd(int offset, int total) {
        float bottom = getHeight() / d - SETTINGS_BOTTOM_PADDING_DP;
        int end = offset;
        while (end < total) {
            float baseline = settingsRowBaselineDp(end, offset);
            if (end > offset && baseline + 8f > bottom) break;
            end++;
        }
        return end;
    }

    private int keepSettingsSelectionVisible(int selection, int total, int currentOffset) {
        int offset = Math.max(0, Math.min(Math.max(0, total - 1), currentOffset));
        if (selection < offset) offset = selection;
        float bottom = getHeight() / d - SETTINGS_BOTTOM_PADDING_DP;
        while (offset < selection
                && settingsRowBaselineDp(selection, offset) + 8f > bottom) {
            offset++;
        }
        return offset;
    }

    private void drawSettingsLegend(Canvas c, String title, float rowBaselineDp) {
        float legendBaselineDp = rowBaselineDp - fontSizeSp - 12f;
        int legendSizeSp = Math.max(9, Math.min(12, fontSizeSp - 8));
        mono(c, title, dp(18), dp(legendBaselineDp), legendSizeSp,
                Color.rgb(156, 139, 116));
        float lineStartPx = dp(18) + p.measureText(title) + dp(10);
        if (lineStartPx < getWidth() - dp(18)) {
            p.setColor(Color.rgb(55, 51, 47));
            c.drawRect(lineStartPx, dp(legendBaselineDp - 4),
                    getWidth() - dp(18), dp(legendBaselineDp - 3), p);
        }
    }

    private void drawAppPicker(Canvas c) {
        String title;
        if (pickerTarget == PICKER_SWIPE_LEFT_TO_RIGHT) title = "VUỐT TRÁI → PHẢI";
        else if (pickerTarget == PICKER_SWIPE_RIGHT_TO_LEFT) title = "VUỐT PHẢI → TRÁI";
        else title = "CHỌN APP CHO PHÍM " + (bindingSlot + 1);
        mono(c, title, dp(16), dp(44), fontSizeSp, amber);
        text(c, "▲▼ chọn · OK xác nhận · Back huỷ", dp(16), dp(70),
                Math.max(10, fontSizeSp - 4), Color.GRAY);
        int total = pickerItemCount();
        pickerSelection = Math.max(0, Math.min(total - 1, pickerSelection));
        int rowStep = Math.max(38, fontSizeSp + 22);
        int visible = visibleRows(104, rowStep, 18);
        pickerOffset = keepSelectionVisible(pickerSelection, total, visible, pickerOffset);
        int end = Math.min(total, pickerOffset + visible);
        for (int index = pickerOffset; index < end; index++) {
            int y = 104 + (index - pickerOffset) * rowStep;
            String label = pickerItemLabel(index);
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
        if (row == 3) return showStatusBar ? "[x]" : "[ ]";
        if (row == SETTING_HOME_KEY_BEHAVIOR) {
            return homeKeyBehavior == HOME_KEYS_QUICK_ACTION ? "Quick action" : "Quay số";
        }
        if (row == SETTING_SWIPE_LEFT_TO_RIGHT) return actionLabel(swipeLeftToRightAction);
        return actionLabel(swipeRightToLeftAction);
    }

    private String actionLabel(int action) {
        if (action == ACTION_CONTACTS) return "Danh bạ";
        if (action == ACTION_MESSAGING) return "Nhắn tin";
        if (action == ACTION_NONE) return "Tắt";
        return appLabel(action);
    }

    private int pickerItemCount() {
        return apps.size() + (pickerTarget == PICKER_HOME_SLOT ? 1 : 3);
    }

    private String pickerItemLabel(int index) {
        if (pickerTarget == PICKER_HOME_SLOT) {
            return index == 0 ? "Chưa gán" : appLabel(apps.get(index - 1));
        }
        if (index == 0) return "Tắt";
        if (index == 1) return "Danh bạ hệ thống";
        if (index == 2) return "Nhắn tin hệ thống";
        return appLabel(apps.get(index - 3));
    }

    private int pickerSelectionForAction(int action) {
        if (action == ACTION_NONE) return 0;
        if (action == ACTION_CONTACTS) return 1;
        if (action == ACTION_MESSAGING) return 2;
        return action >= 0 && action < apps.size() ? action + 3 : 0;
    }

    private int actionForPickerSelection() {
        if (pickerSelection == 0) return ACTION_NONE;
        if (pickerSelection == 1) return ACTION_CONTACTS;
        if (pickerSelection == 2) return ACTION_MESSAGING;
        return pickerSelection - 3;
    }

    private String appLabel(int index) {
        if (index >= 0 && index < apps.size()) return appLabel(apps.get(index));
        return "Chưa gán";
    }

    private String appLabel(ActivityInfo app) {
        return appRepository.label(app);
    }

    private List<ActivityInfo> drawerApps() {
        return appRepository.filterAndSort(apps, drawerTextInput.text());
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
        LauncherConfiguration configuration = settingsStore.load();
        homeCount = configuration.homeCount;
        wallpaperIndex = configuration.wallpaperIndex;
        fontSizeSp = configuration.fontSizeSp;
        showStatusBar = configuration.showStatusBar;
        homeKeyBehavior = configuration.homeKeyBehavior;
        swipeLeftToRightAction = configuration.swipeLeftToRightAction;
        swipeRightToLeftAction = configuration.swipeRightToLeftAction;
        System.arraycopy(configuration.bindings, 0, bindings, 0, bindings.length);
    }

    private void savePrefs() {
        settingsStore.save(new LauncherConfiguration(
                homeCount, wallpaperIndex, fontSizeSp, showStatusBar,
                homeKeyBehavior, swipeLeftToRightAction, swipeRightToLeftAction,
                bindings));
    }

    private boolean adjustSelectedSetting(int delta) {
        if (settingsSelection == 0) {
            wallpaperIndex = (wallpaperIndex + delta + 4) % 4;
        } else if (settingsSelection == 1) {
            fontSizeSp = Math.max(12, Math.min(36, fontSizeSp + delta));
        } else if (settingsSelection == 2) {
            homeCount = Math.max(1, Math.min(9, homeCount + delta));
        } else if (settingsSelection == SETTING_HOME_KEY_BEHAVIOR) {
            homeKeyBehavior = homeKeyBehavior == HOME_KEYS_QUICK_ACTION
                    ? HOME_KEYS_DIALER : HOME_KEYS_QUICK_ACTION;
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
            actions.setStatusBarVisible(showStatusBar);
        }
        else if (settingsSelection == SETTING_HOME_KEY_BEHAVIOR) {
            homeKeyBehavior = homeKeyBehavior == HOME_KEYS_QUICK_ACTION
                    ? HOME_KEYS_DIALER : HOME_KEYS_QUICK_ACTION;
        }
        else if (settingsSelection == SETTING_SWIPE_LEFT_TO_RIGHT
                || settingsSelection == SETTING_SWIPE_RIGHT_TO_LEFT) {
            pickerTarget = settingsSelection == SETTING_SWIPE_LEFT_TO_RIGHT
                    ? PICKER_SWIPE_LEFT_TO_RIGHT : PICKER_SWIPE_RIGHT_TO_LEFT;
            int current = settingsSelection == SETTING_SWIPE_LEFT_TO_RIGHT
                    ? swipeLeftToRightAction : swipeRightToLeftAction;
            pickerSelection = pickerSelectionForAction(current);
            pickerOffset = 0;
            screen = LauncherScreen.APP_PICKER;
        }
        else {
            pickerTarget = PICKER_HOME_SLOT;
            bindingSlot = settingsSelection - SETTINGS_ROW_COUNT;
            int current = bindings[bindingSlot];
            pickerSelection = current >= 0 && current < apps.size() ? current + 1 : 0;
            pickerOffset = 0;
            screen = LauncherScreen.APP_PICKER;
        }
        savePrefs();
        invalidate();
    }

    public void onKey(LauncherKey key, boolean hold) {
        if (key == LauncherKey.CORNER_3) {
            locked = false;
            savePrefs();
            actions.openDialer();
            return;
        }
        if (locked) {
            locked = false;
            invalidate();
            return;
        }
        if (hold && key == LauncherKey.POUND) {
            silent = actions.toggleRingerMode();
            invalidate();
            return;
        }
        if (key == LauncherKey.BACK || key == LauncherKey.CORNER_2) {
            if (screen == LauncherScreen.APP_PICKER) {
                screen = LauncherScreen.SETTINGS;
                invalidate();
                return;
            }
            if (screen != LauncherScreen.HOME) {
                if (screen == LauncherScreen.SETTINGS) savePrefs();
                screen = LauncherScreen.HOME;
                drawerTextInput.clear();
                drawerTextInput.refresh(false);
                invalidate();
            }
            return;
        }
        if (screen == LauncherScreen.SETTINGS
                && (key == LauncherKey.LEFT || key == LauncherKey.RIGHT)) {
            if (adjustSelectedSetting(key == LauncherKey.LEFT ? -1 : 1)) return;
        }
        if (screen == LauncherScreen.SETTINGS
                && (key == LauncherKey.UP || key == LauncherKey.LEFT)) {
            int limit = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = (settingsSelection - 1 + limit) % limit;
            invalidate();
            return;
        }
        if (screen == LauncherScreen.SETTINGS
                && (key == LauncherKey.DOWN || key == LauncherKey.RIGHT)) {
            int limit = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = (settingsSelection + 1) % limit;
            invalidate();
            return;
        }
        if (screen == LauncherScreen.APP_PICKER
                && (key == LauncherKey.UP || key == LauncherKey.LEFT)) {
            int limit = pickerItemCount();
            pickerSelection = (pickerSelection - 1 + limit) % limit;
            invalidate();
            return;
        }
        if (screen == LauncherScreen.APP_PICKER
                && (key == LauncherKey.DOWN || key == LauncherKey.RIGHT)) {
            int limit = pickerItemCount();
            pickerSelection = (pickerSelection + 1) % limit;
            invalidate();
            return;
        }
        if ((screen == LauncherScreen.HOME || screen == LauncherScreen.DRAWER)
                && (key == LauncherKey.UP || key == LauncherKey.LEFT)) {
            int limit = screen == LauncherScreen.HOME
                    ? homeCount : Math.max(1, drawerApps().size());
            selected = (selected - 1 + limit) % limit;
            invalidate();
            return;
        }
        if ((screen == LauncherScreen.HOME || screen == LauncherScreen.DRAWER)
                && (key == LauncherKey.DOWN || key == LauncherKey.RIGHT)) {
            int limit = screen == LauncherScreen.HOME
                    ? homeCount : Math.max(1, drawerApps().size());
            selected = (selected + 1) % limit;
            invalidate();
            return;
        }
        if (key == LauncherKey.OK) {
            if (screen == LauncherScreen.HOME) launchSlot();
            else if (screen == LauncherScreen.DRAWER) launchSelected();
            else if (screen == LauncherScreen.SETTINGS) changeSetting();
            else if (screen == LauncherScreen.APP_PICKER) confirmPickerSelection();
            return;
        }
        if (key == LauncherKey.CORNER_1) {
            screen = LauncherScreen.DRAWER;
            selected = 0;
            drawerOffset = 0;
            drawerTextInput.refresh(true);
            invalidate();
            return;
        }
        if (key == LauncherKey.CORNER_4) {
            if (screen == LauncherScreen.HOME) actions.lockDeviceOrRequestAdmin();
            else goHome();
            return;
        }
        if (screen == LauncherScreen.HOME && key.isDigit()) {
            int digit = key.digit();
            if (homeKeyBehavior == HOME_KEYS_DIALER || digit == 0) {
                actions.openDialer(String.valueOf(digit));
            } else if (digit <= homeCount) {
                selected = digit - 1;
                launchSlot();
            }
            return;
        }
        if (screen == LauncherScreen.DRAWER) {
            if (key == LauncherKey.STAR) drawerTextInput.deleteCharacter();
            else if (key == LauncherKey.POUND) {
                drawerTextInput.clear();
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
        if (selected < 0 || selected >= filtered.size()) return;
        long now = SystemClock.uptimeMillis();
        if (now - lastDrawerLaunchAt < DRAWER_LAUNCH_DEBOUNCE_MS) return;
        lastDrawerLaunchAt = now;
        launch(filtered.get(selected));
    }

    private void launch(ActivityInfo app) {
        actions.launch(app);
    }

    private void launchSwipeAction(int action) {
        if (action == ACTION_CONTACTS) {
            actions.openContacts();
        } else if (action == ACTION_MESSAGING) {
            actions.openMessaging();
        } else if (action >= 0 && action < apps.size()) {
            launch(apps.get(action));
        }
    }

    private void confirmPickerSelection() {
        if (pickerTarget == PICKER_HOME_SLOT) {
            bindings[bindingSlot] = pickerSelection - 1;
            settingsSelection = bindingSlot + SETTINGS_ROW_COUNT;
        } else if (pickerTarget == PICKER_SWIPE_LEFT_TO_RIGHT) {
            swipeLeftToRightAction = actionForPickerSelection();
            settingsSelection = SETTING_SWIPE_LEFT_TO_RIGHT;
        } else {
            swipeRightToLeftAction = actionForPickerSelection();
            settingsSelection = SETTING_SWIPE_RIGHT_TO_LEFT;
        }
        savePrefs();
        screen = LauncherScreen.SETTINGS;
        invalidate();
    }

    private int touchedIndex(float yPx) {
        float y = yPx / d;
        if (screen == LauncherScreen.HOME) {
            HomeLayout layout = homeLayout();
            float relative = y - layout.firstRowTopDp;
            int row = (int) Math.floor(relative / layout.rowStepDp);
            if (row < 0 || relative - row * layout.rowStepDp > layout.rowHeightDp) return -1;
            int index = homeOffset + row;
            return index >= 0 && index < homeCount ? index : -1;
        }
        if (screen == LauncherScreen.DRAWER) {
            float relative = y - drawerListTopDp();
            int row = (int) Math.floor(relative / drawerRowStepDp());
            if (row < 0 || relative - row * drawerRowStepDp() > drawerRowHeightDp()) return -1;
            int index = drawerOffset + row;
            return index < drawerApps().size() ? index : -1;
        }
        if (screen == LauncherScreen.SETTINGS) {
            int total = SETTINGS_ROW_COUNT + homeCount;
            int end = settingsVisibleEnd(settingsOffset, total);
            for (int index = settingsOffset; index < end; index++) {
                float baseline = settingsRowBaselineDp(index, settingsOffset);
                if (y >= baseline - fontSizeSp - 9f && y <= baseline + 8f) return index;
            }
            return -1;
        }
        if (screen == LauncherScreen.APP_PICKER) {
            int rowStep = Math.max(38, fontSizeSp + 22);
            float firstTop = 104 - fontSizeSp - 9;
            int row = (int) Math.floor((y - firstTop) / rowStep);
            if (row < 0) return -1;
            int index = pickerOffset + row;
            return index < pickerItemCount() ? index : -1;
        }
        return -1;
    }

    private void selectTouchedIndex(LauncherScreen touchedScreen, int index) {
        if (index < 0) return;
        if (touchedScreen == LauncherScreen.HOME || touchedScreen == LauncherScreen.DRAWER) {
            selected = index;
        } else if (touchedScreen == LauncherScreen.SETTINGS) {
            settingsSelection = index;
        } else if (touchedScreen == LauncherScreen.APP_PICKER) {
            pickerSelection = index;
        }
        invalidate();
    }

    private void activateTouchedIndex(LauncherScreen touchedScreen) {
        if (screen != touchedScreen) return;
        if (screen == LauncherScreen.HOME) launchSlot();
        else if (screen == LauncherScreen.DRAWER) launchSelected();
        else if (screen == LauncherScreen.SETTINGS) changeSetting();
        else if (screen == LauncherScreen.APP_PICKER) confirmPickerSelection();
    }

    private void moveTouchSelection(LauncherScreen touchedScreen, int delta) {
        if (delta == 0 || screen != touchedScreen) return;
        if (screen == LauncherScreen.HOME) {
            selected = Math.max(0, Math.min(homeCount - 1, selected + delta));
            HomeLayout layout = homeLayout();
            homeOffset = keepSelectionVisible(selected, homeCount,
                    homeVisibleRows(layout), homeOffset);
        } else if (screen == LauncherScreen.DRAWER) {
            int total = drawerApps().size();
            if (total == 0) return;
            selected = Math.max(0, Math.min(total - 1, selected + delta));
            drawerOffset = keepSelectionVisible(selected, total,
                    drawerVisibleRows(), drawerOffset);
        } else if (screen == LauncherScreen.SETTINGS) {
            int total = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = Math.max(0, Math.min(total - 1, settingsSelection + delta));
            settingsOffset = keepSettingsSelectionVisible(
                    settingsSelection, total, settingsOffset);
        } else if (screen == LauncherScreen.APP_PICKER) {
            int total = pickerItemCount();
            pickerSelection = Math.max(0, Math.min(total - 1, pickerSelection + delta));
            int rowStep = Math.max(38, fontSizeSp + 22);
            pickerOffset = keepSelectionVisible(pickerSelection, total,
                    visibleRows(104, rowStep, 18), pickerOffset);
        }
        invalidate();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (touchIndex >= 0) activateTouchedIndex(touchScreen);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
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
            } else if (screen == LauncherScreen.HOME
                    && event.getY() > dp(homeListBottomDp(homeLayout()))) {
                holdAction = () -> {
                    holdTriggered = true;
                    screen = LauncherScreen.SETTINGS;
                    settingsHintScrollStartedAt = 0L;
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
            float threshold = dp(touchScreen == LauncherScreen.HOME
                    ? 24 : Math.max(24, (fontSizeSp + 22) / 2f));
            if (Math.abs(event.getX() - touchDownX) >= dp(18)
                    || Math.abs(event.getY() - touchDownY) >= dp(18)) {
                if (holdAction != null) touchHandler.removeCallbacks(holdAction);
                holdAction = null;
                touchMoved = true;
            }
            if (touchScreen == LauncherScreen.HOME
                    && Math.abs(event.getX() - touchDownX)
                    > Math.abs(event.getY() - touchDownY)) {
                return true;
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
            float dx = event.getX() - touchDownX;
            float dy = event.getY() - touchDownY;
            if (!locked && !holdTriggered && screen == touchScreen
                    && touchScreen == LauncherScreen.HOME && Math.abs(dx) >= dp(80)
                    && Math.abs(dx) > Math.abs(dy)) {
                launchSwipeAction(dx > 0
                        ? swipeLeftToRightAction : swipeRightToLeftAction);
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            if (!locked && !holdTriggered && screen == touchScreen
                    && touchScreen == LauncherScreen.HOME && touchIndex < 0
                    && dy < -dp(80)) {
                screen = LauncherScreen.DRAWER;
                selected = 0;
                drawerOffset = 0;
                drawerTextInput.clear();
                drawerTextInput.refresh(true);
                invalidate();
                return true;
            }
            if (!holdTriggered && !touchMoved && touchIndex >= 0) {
                performClick();
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
