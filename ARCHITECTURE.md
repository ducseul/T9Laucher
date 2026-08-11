# T9 Launcher architecture

The app keeps a single Android activity, but separates framework integration from launcher logic.

## Packages

- `com.t9launcher`: Android entry points declared in the manifest.
- `com.t9launcher.ui`: custom canvas rendering, screen state, keyboard and touch navigation.
- `com.t9launcher.input`: typed launcher keys, physical key mapping, short/long-press dispatch.
- `com.t9launcher.model`: framework-independent launcher configuration.
- `com.t9launcher.apps`: installed-app query/search contract and Android implementation.
- `com.t9launcher.data`: settings-store contract and SharedPreferences implementation.
- `com.t9launcher.system`: system-action contract and Android implementations for dialer, contacts,
  messaging, ringer, status bar, device lock, and accessibility permission.
- `com.t9launcher.search`: framework-independent app-name normalization.

## Dependency rule

`MainActivity` wires dependencies and owns lifecycle only. `LauncherView` depends on the
`LauncherActions`, `AppRepository`, and `LauncherSettingsStore` contracts. Android-specific code
stays behind those contracts, so UI behavior can be changed or tested without adding more
responsibilities to the activity.

Use `LauncherKey` and `LauncherScreen` for control flow instead of string or integer sentinels.
Persisted integer values remain centralized in `LauncherConfiguration` for compatibility with
existing SharedPreferences data.

## Verification

Run:

```powershell
rtk .\gradlew.bat testDebugUnitTest assembleDebug
```

Unit tests cover the framework-independent configuration, key, and search behavior. Device-only
flows such as device-admin lock, OEM key delivery, dialer resolution, and accessibility prompts
still require validation on the target Doov device.
