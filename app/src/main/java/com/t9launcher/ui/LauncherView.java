package com.t9launcher.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
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

import com.t9launcher.BuildConfig;
import com.t9launcher.R;
import com.t9launcher.apps.AppRepository;
import com.t9launcher.apps.InstalledAppRepository;
import com.t9launcher.data.LauncherSettingsStore;
import com.t9launcher.data.SharedPreferencesLauncherSettingsStore;
import com.t9launcher.input.DrawerTextInput;
import com.t9launcher.input.LauncherKey;
import com.t9launcher.model.DrawerGridNavigator;
import com.t9launcher.model.LauncherConfiguration;
import com.t9launcher.system.LauncherActions;

import static com.t9launcher.model.LauncherConfiguration.ACTION_CONTACTS;
import static com.t9launcher.model.LauncherConfiguration.ACTION_MESSAGING;
import static com.t9launcher.model.LauncherConfiguration.ACTION_NONE;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_ALIGNMENT_CENTER;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_ALIGNMENT_LEFT;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_ALIGNMENT_RIGHT;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_STYLE_CLASSIC;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_STYLE_EIGHT_SEGMENT;
import static com.t9launcher.model.LauncherConfiguration.CLOCK_STYLE_VERTICAL;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_FONT_SIZE_SP;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_ALIGNMENT;
import static com.t9launcher.model.LauncherConfiguration.DEFAULT_CLOCK_STYLE;
import static com.t9launcher.model.LauncherConfiguration.DRAWER_LAYOUT_GRID;
import static com.t9launcher.model.LauncherConfiguration.DRAWER_LAYOUT_LIST;
import static com.t9launcher.model.LauncherConfiguration.HOME_KEYS_DIALER;
import static com.t9launcher.model.LauncherConfiguration.HOME_KEYS_QUICK_ACTION;
import static com.t9launcher.model.LauncherConfiguration.MAX_CLOCK_FONT_SIZE_SP;
import static com.t9launcher.model.LauncherConfiguration.MAX_DRAWER_GRID_COLUMNS;
import static com.t9launcher.model.LauncherConfiguration.MAX_DRAWER_GRID_ICON_CORNER_RADIUS_DP;
import static com.t9launcher.model.LauncherConfiguration.MAX_DRAWER_GRID_ICON_SIZE_DP;
import static com.t9launcher.model.LauncherConfiguration.MAX_DRAWER_GRID_ROWS;
import static com.t9launcher.model.LauncherConfiguration.MIN_CLOCK_FONT_SIZE_SP;
import static com.t9launcher.model.LauncherConfiguration.MIN_DRAWER_GRID_COLUMNS;
import static com.t9launcher.model.LauncherConfiguration.MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP;
import static com.t9launcher.model.LauncherConfiguration.MIN_DRAWER_GRID_ICON_SIZE_DP;
import static com.t9launcher.model.LauncherConfiguration.MIN_DRAWER_GRID_ROWS;

@SuppressLint("ViewConstructor")
public final class LauncherView extends View {
    private static final int SETTINGS_ROW_COUNT = 16;
    private static final int SETTING_CLOCK_STYLE = 2;
    private static final int SETTING_CLOCK_ALIGNMENT = 3;
    private static final int SETTING_CLOCK_FONT_SIZE = 4;
    private static final int SETTING_HOME_COUNT = 5;
    private static final int SETTING_STATUS_BAR = 6;
    private static final int SETTING_ANIMATIONS = 7;
    private static final int SETTING_DRAWER_LAYOUT = 8;
    private static final int SETTING_DRAWER_GRID_COLUMNS = 9;
    private static final int SETTING_DRAWER_GRID_ROWS = 10;
    private static final int SETTING_DRAWER_GRID_ICON_SIZE = 11;
    private static final int SETTING_DRAWER_GRID_ICON_CORNER_RADIUS = 12;
    private static final int SETTING_HOME_KEY_BEHAVIOR = 13;
    private static final int SETTING_SWIPE_LEFT_TO_RIGHT = 14;
    private static final int SETTING_SWIPE_RIGHT_TO_LEFT = 15;
    private static final int SETTINGS_TAB_DISPLAY = 0;
    private static final int SETTINGS_TAB_DRAWER = 1;
    private static final int SETTINGS_TAB_HOME_CONTROLS = 2;
    private static final int SETTINGS_TAB_HOME_APPS = 3;
    private static final int SETTINGS_TAB_AUTHOR = 4;
    private static final int SETTINGS_TAB_COUNT = 5;
    private static final String[] SETTINGS_TAB_TITLES = {
            "HIỂN THỊ", "DRAWER", "ĐIỀU KHIỂN HOME", "ỨNG DỤNG HOME", "TÁC GIẢ"
    };
    private static final float SETTINGS_TAB_TOP_DP = 82f;
    private static final float SETTINGS_TAB_BOTTOM_DP = 114f;
    private static final float SETTINGS_TAB_HORIZONTAL_PADDING_DP = 12f;
    private static final float SETTINGS_TAB_OVERFLOW_ICON_WIDTH_DP = 24f;
    private static final float SETTINGS_FIRST_BASELINE_DP = 148f;
    private static final float SETTINGS_BOTTOM_PADDING_DP = 44f;
    private static final float DRAWER_BOTTOM_PADDING_DP = 20f;
    private static final float DRAWER_BACK_LABEL_EXTRA_GAP_SP = 5f;
    private static final int PICKER_HOME_SLOT = 0;
    private static final int PICKER_SWIPE_LEFT_TO_RIGHT = 1;
    private static final int PICKER_SWIPE_RIGHT_TO_LEFT = 2;
    private static final long DRAWER_LAUNCH_DEBOUNCE_MS = 300L;
    private static final long DRAWER_ANIMATION_DURATION_MS = 240L;
    private static final long SETTINGS_REVEAL_DURATION_MS = 220L;
    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_DRAWER_ENTER = 1;
    private static final int ANIMATION_DRAWER_EXIT = 2;
    private static final int ANIMATION_SETTINGS_REVEAL = 3;
    private static final int[] SEGMENT_DIGIT_MASKS = {
            0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07, 0x7f, 0x6f
    };
    private static final int PLAYFUL_DIGIT_SPRITE_HEIGHT_PX = 256;
    private static final int[] PLAYFUL_DIGIT_X_PX = {
            0, 163, 262, 431, 591, 752, 904, 1071, 1276, 1461
    };
    private static final int[] PLAYFUL_DIGIT_WIDTH_PX = {
            159, 95, 165, 156, 157, 148, 163, 201, 181, 168
    };

    private static final class HomeLayout {
        final float clockSizeSp;
        final float dateSizeSp;
        final float clockTopDp;
        final float clockBottomDp;
        final float clockBaselineDp;
        final float dateBaselineDp;
        final float dividerDp;
        final float firstRowTopDp;
        final float firstRowBaselineDp;
        final float rowHeightDp;
        final float rowStepDp;

        HomeLayout(float clockSizeSp, float dateSizeSp,
                   float clockTopDp, float clockBottomDp, float clockBaselineDp,
                   float dateBaselineDp, float dividerDp, float firstRowTopDp,
                   float firstRowBaselineDp, float rowHeightDp, float rowStepDp) {
            this.clockSizeSp = clockSizeSp;
            this.dateSizeSp = dateSizeSp;
            this.clockTopDp = clockTopDp;
            this.clockBottomDp = clockBottomDp;
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
    private final Bitmap playfulDigitSprite;
    private final List<ActivityInfo> apps = new ArrayList<>();
    private LauncherScreen screen = LauncherScreen.HOME;
    private int selected = 0;
    private int settingsSelection = 0;
    private int settingsTab = SETTINGS_TAB_DISPLAY;
    private float settingsTabScrollPx = 0f;
    private final int[] settingsTabSelections = {
            0, SETTING_DRAWER_LAYOUT, SETTING_HOME_KEY_BEHAVIOR,
            SETTINGS_ROW_COUNT, -1
    };
    private int homeOffset = 0;
    private int drawerOffset = 0;
    private int settingsOffset = 0;
    private long settingsHintScrollStartedAt = 0L;
    private long settingsAuthorScrollStartedAt = 0L;
    private int bindingSlot = 0;
    private int pickerTarget = PICKER_HOME_SLOT;
    private int pickerSelection = 0;
    private int pickerOffset = 0;
    private long lastDrawerLaunchAt = 0L;
    private int homeCount = 4;
    private int wallpaperIndex = 0;
    private int fontSizeSp = 14;
    private int clockFontSizeSp = DEFAULT_CLOCK_FONT_SIZE_SP;
    private int clockStyle = DEFAULT_CLOCK_STYLE;
    private int clockAlignment = DEFAULT_CLOCK_ALIGNMENT;
    private boolean showStatusBar = true;
    private boolean animationsEnabled = true;
    private int drawerLayout = DRAWER_LAYOUT_LIST;
    private int drawerGridColumns = 4;
    private int drawerGridRows = 5;
    private int drawerGridIconSizeDp = 40;
    private int drawerGridIconCornerRadiusDp = 8;
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
    private int touchSettingsTab = -1;
    private int touchSettingsTabDirection = 0;
    private boolean touchClock;
    private boolean touchMoved;
    private boolean holdTriggered;
    private int activeAnimation = ANIMATION_NONE;
    private long animationStartedAt;
    private float settingsRevealCenterX;
    private float settingsRevealCenterY;
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
        playfulDigitSprite = BitmapFactory.decodeResource(
                getResources(), R.drawable.playful_digits);
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

    public boolean isHomeScreen() {
        return screen == LauncherScreen.HOME;
    }

    public void goHome() {
        if (screen == LauncherScreen.SETTINGS || screen == LauncherScreen.APP_PICKER) savePrefs();
        locked = false;
        if (screen == LauncherScreen.DRAWER && animationsEnabled) {
            if (activeAnimation != ANIMATION_DRAWER_EXIT) {
                startAnimation(ANIMATION_DRAWER_EXIT);
            }
            return;
        }
        finishGoHome();
    }

    private void finishGoHome() {
        activeAnimation = ANIMATION_NONE;
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
        if (screen == LauncherScreen.DRAWER
                && (activeAnimation == ANIMATION_DRAWER_ENTER
                || activeAnimation == ANIMATION_DRAWER_EXIT)) {
            drawAnimatedDrawer(c);
            return;
        }
        if (screen == LauncherScreen.SETTINGS
                && activeAnimation == ANIMATION_SETTINGS_REVEAL) {
            drawSettingsReveal(c);
            return;
        }
        if (screen == LauncherScreen.HOME) drawHome(c);
        else if (screen == LauncherScreen.DRAWER) drawDrawer(c);
        else if (screen == LauncherScreen.SETTINGS) drawSettings(c);
        else if (screen == LauncherScreen.APP_PICKER) drawAppPicker(c);
        else drawHome(c);
    }

    private void drawAnimatedDrawer(Canvas c) {
        float progress = animationProgress(DRAWER_ANIMATION_DURATION_MS);
        if (progress >= 1f) {
            if (activeAnimation == ANIMATION_DRAWER_EXIT) {
                finishGoHome();
                drawHome(c);
            } else {
                activeAnimation = ANIMATION_NONE;
                drawDrawer(c);
            }
            return;
        }

        float eased = smoothStep(progress);
        float visible = activeAnimation == ANIMATION_DRAWER_EXIT ? 1f - eased : eased;
        int drawerSelection = selected;
        drawHome(c);
        selected = drawerSelection;

        float centerX = getWidth() / 2f;
        float centerY = getHeight();
        float maxRadius = (float) Math.hypot(
                Math.max(centerX, getWidth() - centerX), centerY);
        Path reveal = new Path();
        reveal.addCircle(centerX, centerY, maxRadius * visible, Path.Direction.CW);
        int saveCount = c.save();
        c.clipPath(reveal);
        c.drawColor(backgroundColor());
        drawStatus(c);
        drawDrawer(c);
        c.restoreToCount(saveCount);
        drawRevealEdge(c, centerX, centerY, maxRadius * visible);
        postInvalidateOnAnimation();
    }

    private void drawSettingsReveal(Canvas c) {
        float progress = animationProgress(SETTINGS_REVEAL_DURATION_MS);
        if (progress >= 1f) {
            activeAnimation = ANIMATION_NONE;
            drawSettings(c);
            return;
        }

        drawHome(c);
        float eased = smoothStep(progress);
        float farthestX = Math.max(settingsRevealCenterX,
                getWidth() - settingsRevealCenterX);
        float farthestY = Math.max(settingsRevealCenterY,
                getHeight() - settingsRevealCenterY);
        float radius = (float) Math.hypot(farthestX, farthestY) * eased;
        Path reveal = new Path();
        reveal.addCircle(settingsRevealCenterX, settingsRevealCenterY,
                radius, Path.Direction.CW);
        int saveCount = c.save();
        c.clipPath(reveal);
        c.drawColor(backgroundColor());
        drawStatus(c);
        drawSettings(c);
        c.restoreToCount(saveCount);
        drawRevealEdge(c, settingsRevealCenterX, settingsRevealCenterY, radius);
        postInvalidateOnAnimation();
    }

    private void drawRevealEdge(Canvas c, float centerX, float centerY, float radius) {
        if (radius <= 0f) return;
        p.setStyle(Paint.Style.STROKE);

        p.setStrokeWidth(dp(18f));
        p.setColor(Color.argb(22, 255, 244, 225));
        c.drawCircle(centerX, centerY, radius, p);

        p.setStrokeWidth(dp(12f));
        p.setColor(Color.argb(46, 255, 226, 185));
        c.drawCircle(centerX, centerY, radius, p);

        p.setStrokeWidth(dp(7f));
        p.setColor(Color.argb(92, 255, 202, 132));
        c.drawCircle(centerX, centerY, radius, p);

        p.setStrokeWidth(dp(2f));
        p.setColor(Color.argb(205, 255, 180, 84));
        c.drawCircle(centerX, centerY, radius, p);
        p.setStyle(Paint.Style.FILL);
    }

    private float animationProgress(long durationMs) {
        return Math.min(1f, Math.max(0f,
                (SystemClock.uptimeMillis() - animationStartedAt) / (float) durationMs));
    }

    private static float smoothStep(float value) {
        return value * value * (3f - 2f * value);
    }

    private void startAnimation(int animation) {
        activeAnimation = animation;
        animationStartedAt = SystemClock.uptimeMillis();
        postInvalidateOnAnimation();
    }

    private void openDrawer() {
        screen = LauncherScreen.DRAWER;
        selected = 0;
        drawerOffset = 0;
        drawerTextInput.clear();
        drawerTextInput.refresh(true);
        if (animationsEnabled) startAnimation(ANIMATION_DRAWER_ENTER);
        else {
            activeAnimation = ANIMATION_NONE;
            invalidate();
        }
    }

    private void openSettingsAt(float centerX, float centerY) {
        screen = LauncherScreen.SETTINGS;
        settingsHintScrollStartedAt = 0L;
        settingsTab = SETTINGS_TAB_DISPLAY;
        settingsSelection = 0;
        settingsTabSelections[settingsTab] = settingsSelection;
        settingsOffset = 0;
        settingsRevealCenterX = Math.max(0f, Math.min(getWidth(), centerX));
        settingsRevealCenterY = Math.max(0f, Math.min(getHeight(), centerY));
        if (animationsEnabled) startAnimation(ANIMATION_SETTINGS_REVEAL);
        else {
            activeAnimation = ANIMATION_NONE;
            invalidate();
        }
    }

    private void text(Canvas c, String s, float x, float y, float size, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setTextScaleX(1f);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(size));
        p.setColor(color);
        c.drawText(s, x, y, p);
    }

    private void mono(Canvas c, String s, float x, float y, float size, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setTextScaleX(1f);
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
        p.setTextAlign(clockPaintAlign());
        drawHomeClock(c, time, layout);
        p.setTextAlign(clockPaintAlign());
        text(c, vietnameseDate(now), clockAnchorXPx(), dp(layout.dateBaselineDp),
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

        float shortcutLabelBaseline = getHeight() - dp(8);
        text(c, "Danh sách", dp(12), shortcutLabelBaseline, 12, amber);
        p.setTextAlign(Paint.Align.RIGHT);
        text(c, "Thông báo", getWidth() - dp(12), shortcutLabelBaseline, 12, amber);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawHomeClock(Canvas c, String time, HomeLayout layout) {
        if (clockStyle == CLOCK_STYLE_VERTICAL) {
            drawVerticalClock(c, time, layout);
        } else if (clockStyle == CLOCK_STYLE_EIGHT_SEGMENT) {
            drawEightSegmentClock(c, time, layout);
        } else {
            mono(c, time, clockAnchorXPx(), dp(layout.clockBaselineDp),
                    layout.clockSizeSp, Color.rgb(243, 239, 231));
        }
    }

    private Paint.Align clockPaintAlign() {
        if (clockAlignment == CLOCK_ALIGNMENT_LEFT) return Paint.Align.LEFT;
        if (clockAlignment == CLOCK_ALIGNMENT_RIGHT) return Paint.Align.RIGHT;
        return Paint.Align.CENTER;
    }

    private float clockAnchorXPx() {
        if (clockAlignment == CLOCK_ALIGNMENT_LEFT) return dp(16f);
        if (clockAlignment == CLOCK_ALIGNMENT_RIGHT) return getWidth() - dp(16f);
        return getWidth() / 2f;
    }

    private float clockGroupLeftPx(float groupWidthPx) {
        if (clockAlignment == CLOCK_ALIGNMENT_LEFT) return dp(16f);
        if (clockAlignment == CLOCK_ALIGNMENT_RIGHT) {
            return getWidth() - dp(16f) - groupWidthPx;
        }
        return (getWidth() - groupWidthPx) / 2f;
    }

    private void drawVerticalClock(Canvas c, String time, HomeLayout layout) {
        String digits = time.replace(":", "");
        float topPx = dp(layout.clockTopDp);
        float bottomPx = dp(layout.clockBottomDp);
        float heightPx = bottomPx - topPx;
        float digitGapPx = dp(1.5f);
        float pairGapPx = dp(5f);
        float widthScale = heightPx / PLAYFUL_DIGIT_SPRITE_HEIGHT_PX * 0.82f;
        float[] widthsPx = new float[4];
        float totalWidthPx = digitGapPx * 2f + pairGapPx;
        for (int index = 0; index < digits.length(); index++) {
            int digit = digits.charAt(index) - '0';
            widthsPx[index] = PLAYFUL_DIGIT_WIDTH_PX[digit] * widthScale;
            totalWidthPx += widthsPx[index];
        }
        float availableWidthPx = getWidth() - dp(32f);
        if (totalWidthPx > availableWidthPx) {
            float fitScale = availableWidthPx / totalWidthPx;
            totalWidthPx = digitGapPx * 2f + pairGapPx;
            for (int index = 0; index < widthsPx.length; index++) {
                widthsPx[index] *= fitScale;
                totalWidthPx += widthsPx[index];
            }
        }

        float xPx = clockGroupLeftPx(totalWidthPx);
        p.setShader(null);
        p.setFilterBitmap(true);
        p.setDither(true);
        for (int index = 0; index < digits.length(); index++) {
            int digit = digits.charAt(index) - '0';
            int color = index < 2
                    ? Color.rgb(246, 246, 244) : Color.rgb(205, 207, 209);
            p.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            Rect source = new Rect(
                    PLAYFUL_DIGIT_X_PX[digit], 0,
                    PLAYFUL_DIGIT_X_PX[digit] + PLAYFUL_DIGIT_WIDTH_PX[digit],
                    PLAYFUL_DIGIT_SPRITE_HEIGHT_PX);
            RectF destination = new RectF(xPx, topPx,
                    xPx + widthsPx[index], bottomPx);
            c.drawBitmap(playfulDigitSprite, source, destination, p);
            xPx += widthsPx[index];
            if (index == 0 || index == 2) xPx += digitGapPx;
            else if (index == 1) xPx += pairGapPx;
        }
        p.setColorFilter(null);
        p.setTextScaleX(1f);
        p.setTextAlign(clockPaintAlign());
    }

    private void drawEightSegmentClock(Canvas c, String time, HomeLayout layout) {
        String digits = time.replace(":", "");
        float topPx = dp(layout.clockTopDp);
        float heightPx = dp(layout.clockBottomDp - layout.clockTopDp);
        float digitWidthPx = heightPx * 0.48f;
        float thicknessPx = Math.max(dp(3f), heightPx * 0.085f);
        float digitGapPx = thicknessPx * 0.65f;
        float pairGapPx = digitGapPx * 1.6f;
        float colonWidthPx = thicknessPx * 1.2f;
        float totalWidthPx = digitWidthPx * 4f + digitGapPx * 2f
                + pairGapPx * 2f + colonWidthPx;
        float availableWidthPx = getWidth() - dp(32f);
        if (totalWidthPx > availableWidthPx) {
            float scale = availableWidthPx / totalWidthPx;
            heightPx *= scale;
            digitWidthPx *= scale;
            thicknessPx *= scale;
            digitGapPx *= scale;
            pairGapPx *= scale;
            colonWidthPx *= scale;
            totalWidthPx = availableWidthPx;
            topPx += (dp(layout.clockBottomDp - layout.clockTopDp) - heightPx) / 2f;
        }

        float xPx = clockGroupLeftPx(totalWidthPx);
        drawSegmentDigit(c, digits.charAt(0) - '0', xPx, topPx,
                digitWidthPx, heightPx, thicknessPx);
        xPx += digitWidthPx + digitGapPx;
        drawSegmentDigit(c, digits.charAt(1) - '0', xPx, topPx,
                digitWidthPx, heightPx, thicknessPx);
        xPx += digitWidthPx + pairGapPx;

        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(116, 219, 255));
        float radiusPx = colonWidthPx / 2f;
        c.drawCircle(xPx + radiusPx, topPx + heightPx * 0.34f, radiusPx, p);
        c.drawCircle(xPx + radiusPx, topPx + heightPx * 0.66f, radiusPx, p);
        xPx += colonWidthPx + pairGapPx;

        drawSegmentDigit(c, digits.charAt(2) - '0', xPx, topPx,
                digitWidthPx, heightPx, thicknessPx);
        xPx += digitWidthPx + digitGapPx;
        drawSegmentDigit(c, digits.charAt(3) - '0', xPx, topPx,
                digitWidthPx, heightPx, thicknessPx);
        p.setTextAlign(clockPaintAlign());
    }

    private void drawSegmentDigit(Canvas c, int digit, float xPx, float yPx,
                                  float widthPx, float heightPx, float thicknessPx) {
        int mask = digit >= 0 && digit < SEGMENT_DIGIT_MASKS.length
                ? SEGMENT_DIGIT_MASKS[digit] : 0;
        drawSegmentLayer(c, 0x7f, xPx, yPx, widthPx, heightPx, thicknessPx,
                Color.argb(34, 116, 219, 255));
        drawSegmentLayer(c, mask, xPx, yPx, widthPx, heightPx, thicknessPx,
                Color.rgb(116, 219, 255));
    }

    private void drawSegmentLayer(Canvas c, int mask, float xPx, float yPx,
                                  float widthPx, float heightPx, float thicknessPx,
                                  int color) {
        float radiusPx = thicknessPx / 2f;
        float horizontalLeftPx = xPx + thicknessPx * 0.65f;
        float horizontalRightPx = xPx + widthPx - thicknessPx * 0.65f;
        float middleTopPx = yPx + (heightPx - thicknessPx) / 2f;
        float upperTopPx = yPx + thicknessPx * 0.65f;
        float upperBottomPx = middleTopPx - thicknessPx * 0.15f;
        float lowerTopPx = middleTopPx + thicknessPx * 1.15f;
        float lowerBottomPx = yPx + heightPx - thicknessPx * 0.65f;

        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setColor(color);
        if ((mask & 0x01) != 0) c.drawRoundRect(new RectF(horizontalLeftPx, yPx,
                horizontalRightPx, yPx + thicknessPx), radiusPx, radiusPx, p);
        if ((mask & 0x02) != 0) c.drawRoundRect(new RectF(
                xPx + widthPx - thicknessPx, upperTopPx, xPx + widthPx, upperBottomPx),
                radiusPx, radiusPx, p);
        if ((mask & 0x04) != 0) c.drawRoundRect(new RectF(
                xPx + widthPx - thicknessPx, lowerTopPx, xPx + widthPx, lowerBottomPx),
                radiusPx, radiusPx, p);
        if ((mask & 0x08) != 0) c.drawRoundRect(new RectF(horizontalLeftPx,
                yPx + heightPx - thicknessPx, horizontalRightPx, yPx + heightPx),
                radiusPx, radiusPx, p);
        if ((mask & 0x10) != 0) c.drawRoundRect(new RectF(xPx, lowerTopPx,
                xPx + thicknessPx, lowerBottomPx), radiusPx, radiusPx, p);
        if ((mask & 0x20) != 0) c.drawRoundRect(new RectF(xPx, upperTopPx,
                xPx + thicknessPx, upperBottomPx), radiusPx, radiusPx, p);
        if ((mask & 0x40) != 0) c.drawRoundRect(new RectF(horizontalLeftPx,
                middleTopPx, horizontalRightPx, middleTopPx + thicknessPx),
                radiusPx, radiusPx, p);
    }

    private HomeLayout homeLayout() {
        float clockSizeSp = clockFontSizeSp;
        float dateSizeSp = LauncherConfiguration.dateFontSizeSp(clockFontSizeSp);
        float clockTopDp;
        float clockBottomDp;
        float clockBaselineDp;
        if (clockStyle == CLOCK_STYLE_CLASSIC) {
            p.setTypeface(Typeface.create("monospace", Typeface.BOLD));
            p.setTextScaleX(1f);
            p.setTextSize(dp(clockSizeSp));
            Paint.FontMetrics clockMetrics = p.getFontMetrics();
            clockTopDp = 28f;
            clockBaselineDp = clockTopDp - clockMetrics.top / d;
            clockBottomDp = clockBaselineDp + clockMetrics.bottom / d;
        } else {
            clockTopDp = 24f;
            float heightDp = clockStyle == CLOCK_STYLE_VERTICAL
                    ? Math.max(62f, clockSizeSp * 2.05f)
                    : Math.max(42f, clockSizeSp * 1.35f);
            clockBottomDp = clockTopDp + heightDp;
            clockBaselineDp = clockBottomDp;
        }

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextScaleX(1f);
        p.setTextSize(dp(dateSizeSp));
        Paint.FontMetrics dateMetrics = p.getFontMetrics();
        float dateTopDp = dateMetrics.top / d;
        float dateBottomDp = dateMetrics.bottom / d;
        float clockToDateGapDp = clockStyle == CLOCK_STYLE_VERTICAL ? 2f : 6f;
        float dateBaselineDp = clockBottomDp + clockToDateGapDp - dateTopDp;
        float dividerDp = dateBaselineDp + dateBottomDp + 10f;

        p.setTextSize(dp(fontSizeSp));
        Paint.FontMetrics appMetrics = p.getFontMetrics();
        float appTopDp = appMetrics.top / d;
        float appBottomDp = appMetrics.bottom / d;
        float appHeightDp = appBottomDp - appTopDp;
        float rowHeightDp = Math.max(28f, appHeightDp + 12f);
        float rowStepDp = rowHeightDp + Math.max(4f, fontSizeSp / 5f);
        float firstRowTopDp = dividerDp + 12f;
        float firstRowBaselineDp = firstRowTopDp
                + (rowHeightDp - appHeightDp) / 2f - appTopDp;

        return new HomeLayout(clockSizeSp, dateSizeSp,
                clockTopDp, clockBottomDp, clockBaselineDp,
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
        if (drawerLayout == DRAWER_LAYOUT_GRID) {
            drawDrawerGrid(c, filtered, listTop, inset);
            return;
        }
        drawDrawerList(c, filtered, listTop, inset, unit);
    }

    private void drawDrawerList(Canvas c, List<ActivityInfo> filtered,
                                float listTop, float inset, float unit) {
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

    private void drawDrawerGrid(Canvas c, List<ActivityInfo> filtered,
                                float listTopDp, float insetDp) {
        drawerOffset = DrawerGridNavigator.visibleOffset(
                selected, filtered.size(), drawerGridColumns, drawerGridRows, drawerOffset);
        int visible = drawerGridColumns * drawerGridRows;
        int end = Math.min(filtered.size(), drawerOffset + visible);
        float leftPx = dp(insetDp);
        float topPx = dp(listTopDp);
        float cellWidthPx = (getWidth() - leftPx * 2f) / drawerGridColumns;
        float cellHeightPx = drawerGridCellHeightPx();
        float labelSizeSp = Math.max(9f, Math.min(12f, fontSizeSp - 4f));

        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(labelSizeSp));
        Paint.FontMetrics labelMetrics = p.getFontMetrics();
        TextPaint labelPaint = new TextPaint(p);

        for (int index = drawerOffset; index < end; index++) {
            int position = index - drawerOffset;
            int row = position / drawerGridColumns;
            int column = position % drawerGridColumns;
            float cellLeftPx = leftPx + column * cellWidthPx;
            float cellTopPx = topPx + row * cellHeightPx;
            float cellRightPx = cellLeftPx + cellWidthPx;
            float cellBottomPx = cellTopPx + cellHeightPx;

            if (index == selected) {
                p.setColor(Color.rgb(73, 55, 35));
                c.drawRoundRect(new RectF(cellLeftPx + dp(2), cellTopPx + dp(2),
                                cellRightPx - dp(2), cellBottomPx - dp(2)),
                        dp(7), dp(7), p);
            }

            float labelBaselinePx = cellBottomPx - dp(4) - labelMetrics.bottom;
            float iconAreaBottomPx = labelBaselinePx + labelMetrics.top - dp(3);
            float iconSizePx = Math.max(6f, Math.min(dp(drawerGridIconSizeDp), Math.min(
                    cellWidthPx - dp(10), iconAreaBottomPx - cellTopPx - dp(6))));
            float iconLeftPx = cellLeftPx + (cellWidthPx - iconSizePx) / 2f;
            float iconTopPx = cellTopPx + Math.max(dp(4),
                    (iconAreaBottomPx - cellTopPx - iconSizePx) / 2f);
            drawAppIcon(c, filtered.get(index), iconLeftPx, iconTopPx, iconSizePx);

            String label = appLabel(filtered.get(index));
            p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            p.setTextSize(dp(labelSizeSp));
            CharSequence shortened = TextUtils.ellipsize(label, labelPaint,
                    Math.max(0f, cellWidthPx - dp(8)), TextUtils.TruncateAt.END);
            p.setTextAlign(Paint.Align.CENTER);
            text(c, shortened.toString(), (cellLeftPx + cellRightPx) / 2f,
                    labelBaselinePx, labelSizeSp,
                    index == selected ? amber : Color.rgb(245, 242, 236));
            p.setTextAlign(Paint.Align.LEFT);
        }
    }

    private void drawAppIcon(Canvas c, ActivityInfo app, float leftPx, float topPx,
                             float sizePx) {
        Drawable icon = appRepository.icon(app);
        if (icon != null) {
            icon.setBounds(Math.round(leftPx), Math.round(topPx),
                    Math.round(leftPx + sizePx), Math.round(topPx + sizePx));
            if (drawerGridIconCornerRadiusDp <= 0) {
                icon.draw(c);
                return;
            }
            int saveCount = c.save();
            Path clipPath = new Path();
            float radiusPx = Math.min(dp(drawerGridIconCornerRadiusDp), sizePx / 2f);
            clipPath.addRoundRect(new RectF(leftPx, topPx,
                            leftPx + sizePx, topPx + sizePx),
                    radiusPx, radiusPx, Path.Direction.CW);
            c.clipPath(clipPath);
            icon.draw(c);
            c.restoreToCount(saveCount);
            return;
        }
        p.setColor(Color.rgb(66, 66, 72));
        c.drawRoundRect(new RectF(leftPx, topPx, leftPx + sizePx, topPx + sizePx),
                dp(drawerGridIconCornerRadiusDp),
                dp(drawerGridIconCornerRadiusDp), p);
        String label = appLabel(app);
        if (!label.isEmpty()) {
            p.setTextAlign(Paint.Align.CENTER);
            text(c, label.substring(0, 1).toUpperCase(Locale.getDefault()),
                    leftPx + sizePx / 2f, topPx + sizePx * 0.68f,
                    Math.max(10f, sizePx / d * 0.42f), Color.WHITE);
            p.setTextAlign(Paint.Align.LEFT);
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
        float available = getHeight() / d - drawerBottomPaddingDp() - drawerListTopDp();
        return Math.max(1, (int) Math.floor((available + 4f / d) / drawerRowStepDp()));
    }

    private float drawerGridCellHeightPx() {
        float listTopPx = dp(drawerListTopDp());
        float availablePx = Math.max(1f,
                getHeight() - dp(drawerBottomPaddingDp()) - listTopPx);
        return availablePx / drawerGridRows;
    }

    private float drawerBottomPaddingDp() {
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        return DRAWER_BOTTOM_PADDING_DP
                + DRAWER_BACK_LABEL_EXTRA_GAP_SP * scaledDensity / d;
    }

    private void drawSettings(Canvas c) {
        mono(c, "CẤU HÌNH LAUNCHER", dp(16), dp(44), fontSizeSp, amber);
        drawSettingsHint(c);
        drawSettingsTabs(c);
        if (settingsTab == SETTINGS_TAB_AUTHOR) {
            drawSettingsAuthor(c);
            drawSettingsSoftKeys(c);
            return;
        }
        String[] rows = {"Màu / wallpaper", "Cỡ chữ", "Kiểu đồng hồ",
                "Căn Đồng hồ và ngày", "Cỡ chữ Đồng hồ", "Số app ở Home",
                "Hiển thị Thanh thông báo", "Có animation", "Kiểu Drawer",
                "Số cột Grid", "Số hàng Grid", "Kích thước icon Grid",
                "Bo góc icon Grid", "Phím số ở Home", "Vuốt trái → phải",
                "Vuốt phải → trái"};
        int total = rows.length + homeCount;
        settingsSelection = Math.max(0, Math.min(total - 1, settingsSelection));
        if (!isSettingsItemVisible(settingsSelection)) {
            settingsSelection = firstVisibleSettingsItem(total);
        }
        settingsTabSelections[settingsTab] = settingsSelection;
        if (!isSettingsItemVisible(settingsOffset)) {
            settingsOffset = firstVisibleSettingsItem(total);
        }
        settingsOffset = keepSettingsSelectionVisible(settingsSelection, total, settingsOffset);
        int end = settingsVisibleEnd(settingsOffset, total);
        for (int i = settingsOffset; i < end; i++) {
            if (!isSettingsItemVisible(i)) continue;
            float y = settingsRowBaselineDp(i, settingsOffset);
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
                    getHeight() - dp(44), fontSizeSp, amber);
        }
        drawSettingsSoftKeys(c);
    }

    private void drawSettingsAuthor(Canvas c) {
        float rowStepDp = settingsRowStepDp();
        float y = SETTINGS_FIRST_BASELINE_DP;
        text(c, "Tác giả", dp(18), dp(y), fontSizeSp, Color.WHITE);
        drawScrollingSettingsAuthor(c, y);
        y += rowStepDp;
        drawSettingsInfoRow(c, "Phiên bản",
                BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", y);
        y += rowStepDp;
        drawSettingsInfoRow(c, "Loại build", BuildConfig.BUILD_TYPE, y);
    }

    private void drawScrollingSettingsAuthor(Canvas c, float baselineDp) {
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(dp(fontSizeSp));
        float leftPx = dp(18) + p.measureText("Tác giả") + dp(16);
        float rightPx = getWidth() - dp(18);
        float availablePx = Math.max(0f, rightPx - leftPx);
        float textWidthPx = p.measureText(BuildConfig.BUILD_AUTHOR);
        float overflowPx = Math.max(0f, textWidthPx - availablePx);
        float offsetPx = 0f;

        if (overflowPx > 0f) {
            long now = SystemClock.uptimeMillis();
            if (settingsAuthorScrollStartedAt == 0L) settingsAuthorScrollStartedAt = now;
            long startPauseMs = 1000L;
            long endPauseMs = 900L;
            long travelMs = Math.max(1L, (long) (overflowPx / dp(22f) * 1000f));
            long cycleMs = startPauseMs + travelMs + endPauseMs;
            long elapsedMs = (now - settingsAuthorScrollStartedAt) % cycleMs;
            if (elapsedMs > startPauseMs) {
                offsetPx = elapsedMs >= startPauseMs + travelMs
                        ? overflowPx
                        : overflowPx * (elapsedMs - startPauseMs) / travelMs;
            }
            postInvalidateDelayed(16L);
        } else {
            settingsAuthorScrollStartedAt = 0L;
        }

        int saveCount = c.save();
        c.clipRect(leftPx, dp(baselineDp - fontSizeSp - 6f),
                rightPx, dp(baselineDp + 8f));
        p.setColor(amber);
        p.setTextAlign(Paint.Align.LEFT);
        c.drawText(BuildConfig.BUILD_AUTHOR, leftPx - offsetPx, dp(baselineDp), p);
        c.restoreToCount(saveCount);
    }

    private void drawSettingsInfoRow(Canvas c, String label, String value, float baselineDp) {
        text(c, label, dp(18), dp(baselineDp), fontSizeSp, Color.WHITE);
        p.setTextAlign(Paint.Align.RIGHT);
        text(c, value, getWidth() - dp(18), dp(baselineDp), fontSizeSp, amber);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawSettingsHint(Canvas c) {
        String hint = "◀▶ đổi tab · ▲▼ chọn · 4/6 giảm/tăng nhanh";
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

    private void drawSettingsSoftKeys(Canvas c) {
        text(c, "Lưu và đóng", dp(12), getHeight() - dp(8), 12, amber);
        p.setTextAlign(Paint.Align.RIGHT);
        text(c, "Thoát", getWidth() - dp(12), getHeight() - dp(8), 12, amber);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawSettingsTabs(Canvas c) {
        float bottomPx = dp(SETTINGS_TAB_BOTTOM_DP);
        float baselinePx = dp(SETTINGS_TAB_TOP_DP + 21f);
        float totalWidthPx = settingsTabsTotalWidthPx();
        boolean overflow = totalWidthPx > getWidth();
        float iconWidthPx = overflow ? dp(SETTINGS_TAB_OVERFLOW_ICON_WIDTH_DP) : 0f;
        float viewportLeftPx = iconWidthPx;
        float viewportRightPx = getWidth() - iconWidthPx;
        ensureSelectedSettingsTabVisible(totalWidthPx, viewportRightPx - viewportLeftPx);

        p.setColor(Color.rgb(55, 51, 47));
        c.drawRect(0, bottomPx - dp(1), getWidth(), bottomPx, p);

        int saveCount = c.save();
        c.clipRect(viewportLeftPx, dp(SETTINGS_TAB_TOP_DP),
                viewportRightPx, bottomPx);
        p.setTextAlign(Paint.Align.CENTER);
        float tabLeftPx = viewportLeftPx - settingsTabScrollPx;
        for (int tab = 0; tab < SETTINGS_TAB_COUNT; tab++) {
            float tabWidthPx = settingsTabWidthPx(tab);
            float centerX = tabLeftPx + tabWidthPx / 2f;
            mono(c, SETTINGS_TAB_TITLES[tab], centerX, baselinePx,
                    settingsTabTextSizeSp(),
                    tab == settingsTab ? amber : Color.rgb(156, 139, 116));
            if (tab == settingsTab) {
                p.setColor(amber);
                c.drawRoundRect(new RectF(tabLeftPx + dp(8), bottomPx - dp(3),
                                tabLeftPx + tabWidthPx - dp(8), bottomPx),
                        dp(1.5f), dp(1.5f), p);
            }
            tabLeftPx += tabWidthPx;
        }
        c.restoreToCount(saveCount);

        if (overflow) {
            if (settingsTab > SETTINGS_TAB_DISPLAY) {
                drawSettingsOverflowIcon(c, iconWidthPx / 2f, true);
            }
            if (settingsTab < SETTINGS_TAB_AUTHOR) {
                drawSettingsOverflowIcon(c, getWidth() - iconWidthPx / 2f, false);
            }
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawSettingsOverflowIcon(Canvas c, float centerXPx, boolean pointsLeft) {
        float centerYPx = dp((SETTINGS_TAB_TOP_DP + SETTINGS_TAB_BOTTOM_DP) / 2f);
        float halfWidthPx = dp(4f);
        float halfHeightPx = dp(6f);
        Path arrow = new Path();
        if (pointsLeft) {
            arrow.moveTo(centerXPx + halfWidthPx, centerYPx - halfHeightPx);
            arrow.lineTo(centerXPx - halfWidthPx, centerYPx);
            arrow.lineTo(centerXPx + halfWidthPx, centerYPx + halfHeightPx);
        } else {
            arrow.moveTo(centerXPx - halfWidthPx, centerYPx - halfHeightPx);
            arrow.lineTo(centerXPx + halfWidthPx, centerYPx);
            arrow.lineTo(centerXPx - halfWidthPx, centerYPx + halfHeightPx);
        }
        arrow.close();
        p.setColor(amber);
        c.drawPath(arrow, p);
    }

    private float settingsTabTextSizeSp() {
        return getWidth() / d < 300f ? 13f : 15f;
    }

    private float settingsTabWidthPx(int tab) {
        p.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        p.setTextSize(dp(settingsTabTextSizeSp()));
        return p.measureText(SETTINGS_TAB_TITLES[tab])
                + dp(SETTINGS_TAB_HORIZONTAL_PADDING_DP * 2f);
    }

    private float settingsTabsTotalWidthPx() {
        float widthPx = 0f;
        for (int tab = 0; tab < SETTINGS_TAB_COUNT; tab++) {
            widthPx += settingsTabWidthPx(tab);
        }
        return widthPx;
    }

    private float settingsTabStartPx(int targetTab) {
        float startPx = 0f;
        for (int tab = 0; tab < targetTab; tab++) startPx += settingsTabWidthPx(tab);
        return startPx;
    }

    private void ensureSelectedSettingsTabVisible(float totalWidthPx, float viewportWidthPx) {
        if (viewportWidthPx <= 0f) return;
        float maxScrollPx = Math.max(0f, totalWidthPx - viewportWidthPx);
        float selectedStartPx = settingsTabStartPx(settingsTab);
        float selectedEndPx = selectedStartPx + settingsTabWidthPx(settingsTab);
        settingsTabScrollPx = Math.max(0f, Math.min(maxScrollPx, settingsTabScrollPx));
        if (selectedStartPx < settingsTabScrollPx) {
            settingsTabScrollPx = selectedStartPx;
        } else if (selectedEndPx > settingsTabScrollPx + viewportWidthPx) {
            settingsTabScrollPx = selectedEndPx - viewportWidthPx;
        }
        settingsTabScrollPx = Math.max(0f, Math.min(maxScrollPx, settingsTabScrollPx));
    }

    private int settingsRowStepDp() {
        return Math.max(38, fontSizeSp + 22);
    }

    private boolean isSettingsItemVisible(int index) {
        if (settingsTabForItem(index) != settingsTab) return false;
        return drawerLayout == DRAWER_LAYOUT_GRID || index != SETTING_DRAWER_GRID_COLUMNS
                && index != SETTING_DRAWER_GRID_ROWS
                && index != SETTING_DRAWER_GRID_ICON_SIZE
                && index != SETTING_DRAWER_GRID_ICON_CORNER_RADIUS;
    }

    private int settingsTabForItem(int index) {
        if (index < SETTING_DRAWER_LAYOUT) return SETTINGS_TAB_DISPLAY;
        if (index < SETTING_HOME_KEY_BEHAVIOR) return SETTINGS_TAB_DRAWER;
        if (index < SETTINGS_ROW_COUNT) return SETTINGS_TAB_HOME_CONTROLS;
        return SETTINGS_TAB_HOME_APPS;
    }

    private int settingsTabStart(int tab) {
        if (tab == SETTINGS_TAB_DISPLAY) return 0;
        if (tab == SETTINGS_TAB_DRAWER) return SETTING_DRAWER_LAYOUT;
        if (tab == SETTINGS_TAB_HOME_CONTROLS) return SETTING_HOME_KEY_BEHAVIOR;
        return SETTINGS_ROW_COUNT;
    }

    private int firstVisibleSettingsItem(int total) {
        for (int index = settingsTabStart(settingsTab); index < total; index++) {
            if (isSettingsItemVisible(index)) return index;
        }
        return Math.max(0, Math.min(total - 1, settingsTabStart(settingsTab)));
    }

    private void selectSettingsTab(int tab) {
        if (settingsTab != SETTINGS_TAB_AUTHOR) {
            settingsTabSelections[settingsTab] = settingsSelection;
        }
        settingsTab = (tab + SETTINGS_TAB_COUNT) % SETTINGS_TAB_COUNT;
        if (settingsTab == SETTINGS_TAB_AUTHOR) settingsAuthorScrollStartedAt = 0L;
        int total = SETTINGS_ROW_COUNT + homeCount;
        if (settingsTab != SETTINGS_TAB_AUTHOR) {
            int candidate = Math.max(0,
                    Math.min(total - 1, settingsTabSelections[settingsTab]));
            settingsSelection = isSettingsItemVisible(candidate)
                    ? candidate : firstVisibleSettingsItem(total);
            settingsTabSelections[settingsTab] = settingsSelection;
            settingsOffset = firstVisibleSettingsItem(total);
            settingsOffset = keepSettingsSelectionVisible(
                    settingsSelection, total, settingsOffset);
        }
        float totalWidthPx = settingsTabsTotalWidthPx();
        float iconWidthPx = totalWidthPx > getWidth()
                ? dp(SETTINGS_TAB_OVERFLOW_ICON_WIDTH_DP) : 0f;
        ensureSelectedSettingsTabVisible(totalWidthPx,
                Math.max(0f, getWidth() - iconWidthPx * 2f));
        invalidate();
    }

    private void moveSettingsTab(int delta) {
        selectSettingsTab(settingsTab + delta);
    }

    private int nextSettingsSelection(int selection, int direction,
                                      int total, boolean wrap) {
        int candidate = selection;
        for (int checked = 0; checked < total; checked++) {
            candidate += direction;
            if (wrap) candidate = (candidate + total) % total;
            else if (candidate < 0 || candidate >= total) return selection;
            if (isSettingsItemVisible(candidate)) return candidate;
        }
        return selection;
    }

    private int moveSettingsSelection(int selection, int delta, int total) {
        int result = selection;
        int direction = delta < 0 ? -1 : 1;
        for (int step = 0; step < Math.abs(delta); step++) {
            int next = nextSettingsSelection(result, direction, total, false);
            if (next == result) break;
            result = next;
        }
        return result;
    }

    private float settingsRowBaselineDp(int index, int offset) {
        float baseline = SETTINGS_FIRST_BASELINE_DP;
        int rowStep = settingsRowStepDp();
        for (int current = offset; current <= index; current++) {
            if (!isSettingsItemVisible(current)) continue;
            if (current == index) return baseline;
            baseline += rowStep;
        }
        return baseline;
    }

    private int settingsVisibleEnd(int offset, int total) {
        float bottom = getHeight() / d - SETTINGS_BOTTOM_PADDING_DP;
        int end = offset;
        boolean hasVisibleItem = false;
        while (end < total) {
            if (!isSettingsItemVisible(end)) {
                end++;
                continue;
            }
            float baseline = settingsRowBaselineDp(end, offset);
            if (hasVisibleItem && baseline + 8f > bottom) break;
            hasVisibleItem = true;
            end++;
        }
        return end;
    }

    private int keepSettingsSelectionVisible(int selection, int total, int currentOffset) {
        int offset = Math.max(0, Math.min(Math.max(0, total - 1), currentOffset));
        if (!isSettingsItemVisible(offset)) {
            int next = nextSettingsSelection(offset, 1, total, false);
            offset = next == offset
                    ? nextSettingsSelection(offset, -1, total, false) : next;
        }
        if (selection < offset) offset = selection;
        float bottom = getHeight() / d - SETTINGS_BOTTOM_PADDING_DP;
        while (offset < selection
                && settingsRowBaselineDp(selection, offset) + 8f > bottom) {
            int next = nextSettingsSelection(offset, 1, total, false);
            if (next == offset) break;
            offset = next;
        }
        return offset;
    }

    private void drawAppPicker(Canvas c) {
        String title;
        if (pickerTarget == PICKER_SWIPE_LEFT_TO_RIGHT) title = "VUỐT TRÁI → PHẢI";
        else if (pickerTarget == PICKER_SWIPE_RIGHT_TO_LEFT) title = "VUỐT PHẢI → TRÁI";
        else title = "CHỌN APP CHO PHÍM " + (bindingSlot + 1);
        mono(c, title, dp(16), dp(44), fontSizeSp, amber);
        text(c, "▲▼ chọn · OK xác nhận · Back huỷ", dp(16), dp(70),
                Math.max(10, fontSizeSp - 4), Color.GRAY);
        List<ActivityInfo> pickerApps = pickerTarget == PICKER_HOME_SLOT
                ? sortedApps() : apps;
        int total = pickerItemCount();
        pickerSelection = Math.max(0, Math.min(total - 1, pickerSelection));
        int rowStep = Math.max(38, fontSizeSp + 22);
        int visible = visibleRows(104, rowStep, 18);
        pickerOffset = keepSelectionVisible(pickerSelection, total, visible, pickerOffset);
        int end = Math.min(total, pickerOffset + visible);
        for (int index = pickerOffset; index < end; index++) {
            int y = 104 + (index - pickerOffset) * rowStep;
            String label = pickerItemLabel(index, pickerApps);
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
        if (row == SETTING_CLOCK_STYLE) {
            if (clockStyle == CLOCK_STYLE_VERTICAL) return "Số dọc";
            if (clockStyle == CLOCK_STYLE_EIGHT_SEGMENT) return "Digital 8 thanh";
            return "Cơ bản";
        }
        if (row == SETTING_CLOCK_ALIGNMENT) {
            if (clockAlignment == CLOCK_ALIGNMENT_LEFT) return "Căn trái";
            if (clockAlignment == CLOCK_ALIGNMENT_RIGHT) return "Căn phải";
            return "Căn giữa";
        }
        if (row == SETTING_CLOCK_FONT_SIZE) return clockFontSizeSp + " sp";
        if (row == SETTING_HOME_COUNT) return String.valueOf(homeCount);
        if (row == SETTING_STATUS_BAR) return showStatusBar ? "[x]" : "[ ]";
        if (row == SETTING_ANIMATIONS) return animationsEnabled ? "[x]" : "[ ]";
        if (row == SETTING_DRAWER_LAYOUT) {
            return drawerLayout == DRAWER_LAYOUT_GRID ? "Grid" : "Danh sách";
        }
        if (row == SETTING_DRAWER_GRID_COLUMNS) return String.valueOf(drawerGridColumns);
        if (row == SETTING_DRAWER_GRID_ROWS) return String.valueOf(drawerGridRows);
        if (row == SETTING_DRAWER_GRID_ICON_SIZE) return drawerGridIconSizeDp + " dp";
        if (row == SETTING_DRAWER_GRID_ICON_CORNER_RADIUS) {
            return drawerGridIconCornerRadiusDp + " dp";
        }
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

    private String pickerItemLabel(int index, List<ActivityInfo> pickerApps) {
        if (pickerTarget == PICKER_HOME_SLOT) {
            return index == 0 ? "Chưa gán" : appLabel(pickerApps.get(index - 1));
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

    private List<ActivityInfo> sortedApps() {
        return appRepository.filterAndSort(apps, "");
    }

    private int sortedPickerSelectionForApp(int appIndex) {
        if (appIndex < 0 || appIndex >= apps.size()) return 0;
        ActivityInfo selectedApp = apps.get(appIndex);
        List<ActivityInfo> sorted = sortedApps();
        for (int index = 0; index < sorted.size(); index++) {
            if (sameApp(sorted.get(index), selectedApp)) return index + 1;
        }
        return 0;
    }

    private int appIndexForSortedPickerSelection() {
        if (pickerSelection <= 0) return ACTION_NONE;
        List<ActivityInfo> sorted = sortedApps();
        int sortedIndex = pickerSelection - 1;
        if (sortedIndex >= sorted.size()) return ACTION_NONE;
        ActivityInfo selectedApp = sorted.get(sortedIndex);
        for (int index = 0; index < apps.size(); index++) {
            if (sameApp(apps.get(index), selectedApp)) return index;
        }
        return ACTION_NONE;
    }

    private static boolean sameApp(ActivityInfo left, ActivityInfo right) {
        return left == right || left != null && right != null
                && TextUtils.equals(left.packageName, right.packageName)
                && TextUtils.equals(left.name, right.name);
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
        clockFontSizeSp = configuration.clockFontSizeSp;
        clockStyle = configuration.clockStyle;
        clockAlignment = configuration.clockAlignment;
        showStatusBar = configuration.showStatusBar;
        animationsEnabled = configuration.animationsEnabled;
        drawerLayout = configuration.drawerLayout;
        drawerGridColumns = configuration.drawerGridColumns;
        drawerGridRows = configuration.drawerGridRows;
        drawerGridIconSizeDp = configuration.drawerGridIconSizeDp;
        drawerGridIconCornerRadiusDp = configuration.drawerGridIconCornerRadiusDp;
        homeKeyBehavior = configuration.homeKeyBehavior;
        swipeLeftToRightAction = configuration.swipeLeftToRightAction;
        swipeRightToLeftAction = configuration.swipeRightToLeftAction;
        System.arraycopy(configuration.bindings, 0, bindings, 0, bindings.length);
    }

    private void savePrefs() {
        settingsStore.save(new LauncherConfiguration(
                homeCount, wallpaperIndex, fontSizeSp, clockFontSizeSp,
                clockStyle, clockAlignment,
                showStatusBar, animationsEnabled,
                drawerLayout, drawerGridColumns, drawerGridRows,
                drawerGridIconSizeDp, drawerGridIconCornerRadiusDp,
                homeKeyBehavior, swipeLeftToRightAction, swipeRightToLeftAction,
                bindings));
    }

    private boolean adjustSelectedSetting(int delta) {
        if (settingsSelection == 0) {
            wallpaperIndex = (wallpaperIndex + delta + 4) % 4;
        } else if (settingsSelection == 1) {
            fontSizeSp = Math.max(12, Math.min(36, fontSizeSp + delta));
        } else if (settingsSelection == SETTING_CLOCK_STYLE) {
            clockStyle = (clockStyle + delta + 3) % 3;
        } else if (settingsSelection == SETTING_CLOCK_ALIGNMENT) {
            clockAlignment = (clockAlignment + delta + 3) % 3;
        } else if (settingsSelection == SETTING_CLOCK_FONT_SIZE) {
            clockFontSizeSp = Math.max(MIN_CLOCK_FONT_SIZE_SP,
                    Math.min(MAX_CLOCK_FONT_SIZE_SP, clockFontSizeSp + delta));
        } else if (settingsSelection == SETTING_HOME_COUNT) {
            homeCount = Math.max(1, Math.min(9, homeCount + delta));
        } else if (settingsSelection == SETTING_ANIMATIONS) {
            animationsEnabled = !animationsEnabled;
        } else if (settingsSelection == SETTING_DRAWER_LAYOUT) {
            drawerLayout = drawerLayout == DRAWER_LAYOUT_GRID
                    ? DRAWER_LAYOUT_LIST : DRAWER_LAYOUT_GRID;
            drawerOffset = 0;
        } else if (settingsSelection == SETTING_DRAWER_GRID_COLUMNS) {
            drawerGridColumns = Math.max(MIN_DRAWER_GRID_COLUMNS,
                    Math.min(MAX_DRAWER_GRID_COLUMNS, drawerGridColumns + delta));
            drawerOffset = 0;
        } else if (settingsSelection == SETTING_DRAWER_GRID_ROWS) {
            drawerGridRows = Math.max(MIN_DRAWER_GRID_ROWS,
                    Math.min(MAX_DRAWER_GRID_ROWS, drawerGridRows + delta));
            drawerOffset = 0;
        } else if (settingsSelection == SETTING_DRAWER_GRID_ICON_SIZE) {
            drawerGridIconSizeDp = Math.max(MIN_DRAWER_GRID_ICON_SIZE_DP,
                    Math.min(MAX_DRAWER_GRID_ICON_SIZE_DP,
                            drawerGridIconSizeDp + delta * 2));
        } else if (settingsSelection == SETTING_DRAWER_GRID_ICON_CORNER_RADIUS) {
            drawerGridIconCornerRadiusDp = Math.max(
                    MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP,
                    Math.min(MAX_DRAWER_GRID_ICON_CORNER_RADIUS_DP,
                            drawerGridIconCornerRadiusDp + delta));
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
        else if (settingsSelection == 1) fontSizeSp = fontSizeSp == 36 ? 12 : fontSizeSp + 1;
        else if (settingsSelection == SETTING_CLOCK_STYLE) {
            clockStyle = (clockStyle + 1) % 3;
        }
        else if (settingsSelection == SETTING_CLOCK_ALIGNMENT) {
            clockAlignment = (clockAlignment + 1) % 3;
        }
        else if (settingsSelection == SETTING_CLOCK_FONT_SIZE) {
            clockFontSizeSp = clockFontSizeSp == MAX_CLOCK_FONT_SIZE_SP
                    ? MIN_CLOCK_FONT_SIZE_SP : clockFontSizeSp + 1;
        }
        else if (settingsSelection == SETTING_HOME_COUNT) homeCount = homeCount % 9 + 1;
        else if (settingsSelection == SETTING_STATUS_BAR) {
            showStatusBar = !showStatusBar;
            actions.setStatusBarVisible(showStatusBar);
        }
        else if (settingsSelection == SETTING_ANIMATIONS) {
            animationsEnabled = !animationsEnabled;
        }
        else if (settingsSelection == SETTING_DRAWER_LAYOUT) {
            drawerLayout = drawerLayout == DRAWER_LAYOUT_GRID
                    ? DRAWER_LAYOUT_LIST : DRAWER_LAYOUT_GRID;
            drawerOffset = 0;
        }
        else if (settingsSelection == SETTING_DRAWER_GRID_COLUMNS) {
            drawerGridColumns = drawerGridColumns == MAX_DRAWER_GRID_COLUMNS
                    ? MIN_DRAWER_GRID_COLUMNS : drawerGridColumns + 1;
            drawerOffset = 0;
        }
        else if (settingsSelection == SETTING_DRAWER_GRID_ROWS) {
            drawerGridRows = drawerGridRows == MAX_DRAWER_GRID_ROWS
                    ? MIN_DRAWER_GRID_ROWS : drawerGridRows + 1;
            drawerOffset = 0;
        }
        else if (settingsSelection == SETTING_DRAWER_GRID_ICON_SIZE) {
            drawerGridIconSizeDp = drawerGridIconSizeDp == MAX_DRAWER_GRID_ICON_SIZE_DP
                    ? MIN_DRAWER_GRID_ICON_SIZE_DP : drawerGridIconSizeDp + 2;
        }
        else if (settingsSelection == SETTING_DRAWER_GRID_ICON_CORNER_RADIUS) {
            drawerGridIconCornerRadiusDp =
                    drawerGridIconCornerRadiusDp == MAX_DRAWER_GRID_ICON_CORNER_RADIUS_DP
                    ? MIN_DRAWER_GRID_ICON_CORNER_RADIUS_DP
                    : drawerGridIconCornerRadiusDp + 1;
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
            pickerSelection = sortedPickerSelectionForApp(current);
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
        if (screen == LauncherScreen.SETTINGS && key == LauncherKey.CORNER_1) {
            goHome();
            return;
        }
        if (screen == LauncherScreen.HOME
                && (key == LauncherKey.BACK || key == LauncherKey.CORNER_2)) {
            actions.openNotifications();
            return;
        }
        if (key == LauncherKey.BACK || key == LauncherKey.CORNER_2) {
            if (screen == LauncherScreen.APP_PICKER) {
                screen = LauncherScreen.SETTINGS;
                invalidate();
                return;
            }
            if (screen != LauncherScreen.HOME) {
                goHome();
            }
            return;
        }
        if (screen == LauncherScreen.SETTINGS
                && (key == LauncherKey.LEFT || key == LauncherKey.RIGHT)) {
            moveSettingsTab(key == LauncherKey.LEFT ? -1 : 1);
            return;
        }
        if (screen == LauncherScreen.SETTINGS && settingsTab != SETTINGS_TAB_AUTHOR
                && (key == LauncherKey.DIGIT_4 || key == LauncherKey.DIGIT_6)) {
            if (adjustSelectedSetting(key == LauncherKey.DIGIT_4 ? -1 : 1)) return;
        }
        if (screen == LauncherScreen.SETTINGS && key == LauncherKey.UP) {
            if (settingsTab == SETTINGS_TAB_AUTHOR) return;
            int limit = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = nextSettingsSelection(
                    settingsSelection, -1, limit, true);
            settingsTabSelections[settingsTab] = settingsSelection;
            invalidate();
            return;
        }
        if (screen == LauncherScreen.SETTINGS && key == LauncherKey.DOWN) {
            if (settingsTab == SETTINGS_TAB_AUTHOR) return;
            int limit = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = nextSettingsSelection(
                    settingsSelection, 1, limit, true);
            settingsTabSelections[settingsTab] = settingsSelection;
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
        if (screen == LauncherScreen.DRAWER && drawerLayout == DRAWER_LAYOUT_GRID
                && (key == LauncherKey.UP || key == LauncherKey.DOWN
                || key == LauncherKey.LEFT || key == LauncherKey.RIGHT)) {
            moveGridSelection(key, drawerApps().size());
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
            else if (screen == LauncherScreen.SETTINGS
                    && settingsTab != SETTINGS_TAB_AUTHOR) changeSetting();
            else if (screen == LauncherScreen.APP_PICKER) confirmPickerSelection();
            return;
        }
        if (key == LauncherKey.CORNER_1) {
            openDrawer();
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

    private void moveGridSelection(LauncherKey key, int itemCount) {
        if (itemCount <= 0) {
            selected = 0;
            drawerOffset = 0;
            return;
        }
        selected = Math.max(0, Math.min(itemCount - 1, selected));
        if (key == LauncherKey.LEFT) {
            selected = DrawerGridNavigator.moveLeft(selected, itemCount);
        } else if (key == LauncherKey.RIGHT) {
            selected = DrawerGridNavigator.moveRight(selected, itemCount);
        } else if (key == LauncherKey.UP) {
            selected = DrawerGridNavigator.moveUp(selected, itemCount, drawerGridColumns);
        } else if (key == LauncherKey.DOWN) {
            selected = DrawerGridNavigator.moveDown(selected, itemCount, drawerGridColumns);
        }
        drawerOffset = DrawerGridNavigator.visibleOffset(selected, itemCount,
                drawerGridColumns, drawerGridRows, drawerOffset);
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
            bindings[bindingSlot] = appIndexForSortedPickerSelection();
            settingsSelection = bindingSlot + SETTINGS_ROW_COUNT;
        } else if (pickerTarget == PICKER_SWIPE_LEFT_TO_RIGHT) {
            swipeLeftToRightAction = actionForPickerSelection();
            settingsSelection = SETTING_SWIPE_LEFT_TO_RIGHT;
        } else {
            swipeRightToLeftAction = actionForPickerSelection();
            settingsSelection = SETTING_SWIPE_RIGHT_TO_LEFT;
        }
        settingsTab = settingsTabForItem(settingsSelection);
        settingsTabSelections[settingsTab] = settingsSelection;
        savePrefs();
        screen = LauncherScreen.SETTINGS;
        invalidate();
    }

    private boolean isInsideSettingsTabStrip(float yPx) {
        float yDp = yPx / d;
        return screen == LauncherScreen.SETTINGS
                && yDp >= SETTINGS_TAB_TOP_DP && yDp <= SETTINGS_TAB_BOTTOM_DP;
    }

    private int touchedSettingsTabDirection(float xPx, float yPx) {
        if (!isInsideSettingsTabStrip(yPx)) return 0;
        float totalWidthPx = settingsTabsTotalWidthPx();
        if (totalWidthPx <= getWidth()) return 0;
        float iconWidthPx = dp(SETTINGS_TAB_OVERFLOW_ICON_WIDTH_DP);
        if (xPx <= iconWidthPx && settingsTab > SETTINGS_TAB_DISPLAY) return -1;
        if (xPx >= getWidth() - iconWidthPx && settingsTab < SETTINGS_TAB_AUTHOR) return 1;
        return 0;
    }

    private int touchedSettingsTab(float xPx, float yPx) {
        if (!isInsideSettingsTabStrip(yPx)) return -1;
        float totalWidthPx = settingsTabsTotalWidthPx();
        boolean overflow = totalWidthPx > getWidth();
        float iconWidthPx = overflow ? dp(SETTINGS_TAB_OVERFLOW_ICON_WIDTH_DP) : 0f;
        if (xPx < iconWidthPx || xPx > getWidth() - iconWidthPx) return -1;
        float contentX = xPx - iconWidthPx + settingsTabScrollPx;
        float tabRightPx = 0f;
        for (int tab = 0; tab < SETTINGS_TAB_COUNT; tab++) {
            tabRightPx += settingsTabWidthPx(tab);
            if (contentX < tabRightPx) return tab;
        }
        return -1;
    }

    private int touchedIndex(float xPx, float yPx) {
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
            if (drawerLayout == DRAWER_LAYOUT_GRID) {
                float leftPx = 16f;
                float topPx = dp(drawerListTopDp());
                float availableWidthPx = getWidth() - leftPx * 2f;
                if (xPx < leftPx || xPx >= leftPx + availableWidthPx || yPx < topPx) {
                    return -1;
                }
                float cellWidthPx = availableWidthPx / drawerGridColumns;
                float cellHeightPx = drawerGridCellHeightPx();
                int column = (int) ((xPx - leftPx) / cellWidthPx);
                int row = (int) ((yPx - topPx) / cellHeightPx);
                if (column < 0 || column >= drawerGridColumns
                        || row < 0 || row >= drawerGridRows) return -1;
                int index = drawerOffset + row * drawerGridColumns + column;
                return index < drawerApps().size() ? index : -1;
            }
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
                if (!isSettingsItemVisible(index)) continue;
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

    private boolean isInsideHomeClock(float yPx) {
        if (screen != LauncherScreen.HOME) return false;
        float yDp = yPx / d;
        return yDp >= 20f && yDp <= homeLayout().dividerDp;
    }

    private void selectTouchedIndex(LauncherScreen touchedScreen, int index) {
        if (index < 0) return;
        if (touchedScreen == LauncherScreen.HOME || touchedScreen == LauncherScreen.DRAWER) {
            selected = index;
        } else if (touchedScreen == LauncherScreen.SETTINGS) {
            settingsSelection = index;
            settingsTabSelections[settingsTab] = settingsSelection;
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
            int itemDelta = drawerLayout == DRAWER_LAYOUT_GRID
                    ? delta * drawerGridColumns : delta;
            selected = Math.max(0, Math.min(total - 1, selected + itemDelta));
            if (drawerLayout == DRAWER_LAYOUT_GRID) {
                drawerOffset = DrawerGridNavigator.visibleOffset(selected, total,
                        drawerGridColumns, drawerGridRows, drawerOffset);
            } else {
                drawerOffset = keepSelectionVisible(selected, total,
                        drawerVisibleRows(), drawerOffset);
            }
        } else if (screen == LauncherScreen.SETTINGS) {
            int total = SETTINGS_ROW_COUNT + homeCount;
            settingsSelection = moveSettingsSelection(settingsSelection, delta, total);
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
        if (touchClock && touchScreen == LauncherScreen.HOME) actions.openAlarms();
        else if (touchIndex >= 0) activateTouchedIndex(touchScreen);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
            touchDownY = event.getY();
            touchLastY = event.getY();
            touchScreen = screen;
            touchSettingsTabDirection = touchedSettingsTabDirection(
                    event.getX(), event.getY());
            touchSettingsTab = touchSettingsTabDirection == 0
                    ? touchedSettingsTab(event.getX(), event.getY()) : -1;
            touchClock = touchSettingsTab < 0 && touchSettingsTabDirection == 0
                    && isInsideHomeClock(event.getY());
            touchIndex = touchSettingsTab >= 0 || touchSettingsTabDirection != 0
                    ? -1 : touchedIndex(event.getX(), event.getY());
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
                    openSettingsAt(touchDownX, touchDownY);
                };
                touchHandler.postDelayed(holdAction, 700);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float distance = event.getY() - touchLastY;
            float threshold = touchScreen == LauncherScreen.DRAWER
                    && drawerLayout == DRAWER_LAYOUT_GRID
                    ? drawerGridCellHeightPx() / 2f
                    : dp(touchScreen == LauncherScreen.HOME
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
                    && touchScreen == LauncherScreen.SETTINGS && Math.abs(dx) >= dp(80)
                    && Math.abs(dx) > Math.abs(dy)) {
                moveSettingsTab(dx < 0 ? 1 : -1);
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            if (!locked && !holdTriggered && screen == touchScreen
                    && touchScreen == LauncherScreen.HOME && touchIndex < 0
                    && dy < -dp(80)) {
                openDrawer();
                return true;
            }
            if (!holdTriggered && !touchMoved) {
                if (touchSettingsTabDirection != 0) {
                    moveSettingsTab(touchSettingsTabDirection);
                    performClick();
                } else if (touchSettingsTab >= 0) {
                    selectSettingsTab(touchSettingsTab);
                    performClick();
                } else if (touchClock) {
                    performClick();
                } else if (touchIndex >= 0) {
                    performClick();
                }
            }
            getParent().requestDisallowInterceptTouchEvent(false);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (holdAction != null) touchHandler.removeCallbacks(holdAction);
            holdAction = null;
            touchSettingsTab = -1;
            touchSettingsTabDirection = 0;
            touchClock = false;
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return true;
    }
}
