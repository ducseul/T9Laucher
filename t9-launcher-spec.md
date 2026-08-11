# T9 Launcher — Đặc tả tính năng

> **Bản làm rõ phần cứng — 2026-08-11**
>
> Launcher phải chạy trên hai profile màn hình nhỏ: **2.8 inch** và **3.5 inch**. Các kích thước pixel bên dưới là mốc thiết kế tham chiếu, không phải cam kết theo một model máy cụ thể.

## 0. Phạm vi phần cứng và nguyên tắc thiết kế

| Profile | Mốc hiển thị tham chiếu | Mật độ mục tiêu | Số app Home mặc định | Quy tắc giao diện |
|---|---:|---:|---:|---|
| **2.8 inch** | 240 × 320 px, portrait (QVGA) | mdpi hoặc gần mdpi | 4 | Ưu tiên chữ dễ đọc, ít dòng, không yêu cầu cuộn ngang |
| **3.5 inch** | 320 × 480 px, portrait (HVGA) | mdpi hoặc gần mdpi | 4 | Hiển thị đầy đủ 6–9 vị trí nếu còn đủ chiều cao |

Đây là thiết bị cấu hình thấp và tối thiểu. Vì vậy, mọi màn hình phải tuân thủ:

- Thiết kế theo chiều dọc, vùng nội dung chính không vượt quá chiều rộng khả dụng; không dùng carousel hoặc gesture bắt buộc.
- Kích thước vùng bấm tối thiểu khoảng **40 dp** cho phím cứng mô phỏng trên prototype và **44 dp** cho nút cảm ứng khi cần fallback.
- Chữ nội dung chính tối thiểu **14 sp** ở 3.5 inch và **15 sp** ở 2.8 inch; không dùng chữ mảnh. Tên app dài được cắt 1 dòng bằng ellipsis.
- 2.8 inch chỉ nên hiển thị 4 app ở Home; danh sách dài dùng cuộn dọc, không cố nhồi 9 dòng.
- Không phụ thuộc mạng, web font, ảnh nền bitmap lớn, blur, shadow động, video hoặc animation liên tục.
- Màu nền nên là màu phẳng/gradient tĩnh nhẹ; tương phản chữ và accent phải giữ được khi màn hình rẻ, độ sáng thấp.

### Mốc layout cần giữ ổn định

- Status strip: cao khoảng 20–24 dp.
- Đồng hồ Home: 28–38 sp tùy profile và cỡ chữ.
- Một dòng app: 36–44 dp; badge số, icon đơn sắc hoặc placeholder và tên app nằm trên cùng một dòng.
- D-pad/T9 là phương thức chính. Chạm màn hình chỉ là đường phụ để test prototype, không phải yêu cầu bắt buộc của sản phẩm.

### Giới hạn hiệu năng tối thiểu

- Cold start launcher: mục tiêu dưới **1.5 giây** trên thiết bị tham chiếu; không load toàn bộ icon app trước khi Drawer mở.
- RAM nền của launcher: mục tiêu dưới **64 MB**; không giữ bitmap màn hình lớn trong memory.
- UI thread không được chạy tác vụ I/O, quét package hoặc giải mã ảnh đồng bộ.
- Chuyển màn hình chỉ dùng animation ngắn dưới 250 ms; có thể tắt animation nếu máy giật.
- Danh sách app phải lazy-load và cache nhỏ; chỉ giữ dữ liệu cần cho tên, package, icon hiện tại.

### Điều chỉnh hành vi theo profile

- **2.8 inch:** mặc định 4 app Home, cỡ chữ “Vừa” nhưng dùng thông số lớn hơn 3.5 inch; nút và khoảng cách dọc giữ nguyên, danh sách cuộn dọc.
- **3.5 inch:** mặc định 4 app để màn hình thoáng; cho phép người dùng tăng đến 9 nếu tên app vẫn vừa một dòng.
- Các thao tác phím và thời gian long-press giữ nguyên trên cả hai profile để không tạo hai bộ quy tắc.
- Nếu độ phân giải thực tế thấp hơn mốc tham chiếu, ưu tiên theo thứ tự: **đọc được tên app → nhìn thấy trạng thái chọn → hiển thị đủ thao tác**. Không thu nhỏ chữ để giữ đủ nội dung.

Prototype HTML có bộ chuyển **2.8″ · QVGA / 3.5″ · HVGA** ở cột “Cấu hình demo”. Bộ chuyển này mô phỏng giới hạn chiều cao và mật độ của từng profile; nó không thay thế việc kiểm thử trên panel thật.

## 0.1. Quyết định triển khai Android cho phần cứng tối thiểu

Mặc định chọn **Android View + ViewBinding + RecyclerView**, một Activity duy nhất và một state holder nhẹ. Jetpack Compose chỉ nên dùng nếu bản build đo thực tế vẫn đạt các ngưỡng cold start/RAM ở trên; không xem Compose là yêu cầu bắt buộc của sản phẩm.

- Tách `LauncherActivity` (bắt phím và vòng đời) khỏi `LauncherState` (trạng thái Home, Drawer, Dialer, Settings và lock).
- Render Home/Drawer bằng view tái sử dụng; không tạo lại toàn bộ cây view khi chỉ đổi highlight.
- Nạp package/app icon sau khi Home đã vẽ xong. Icon lỗi hoặc chưa nạp dùng placeholder đơn sắc.
- Lưu cấu hình bằng DataStore Preferences hoặc SharedPreferences nếu cần tối giản dependency; không dùng Room cho vài chục giá trị cấu hình.
- Tắt hardware acceleration chỉ khi đo trên panel cụ thể cho thấy cần thiết; trước đó ưu tiên loại bỏ blur, alpha animation và bitmap lớn.
- Android 11+ là mốc tương thích; bản Doov R17 Pro đang dùng được kiểm tra có thể là Android 13 tùy SKU. Key mapping của 4 phím góc vẫn phải có màn hình debug trên máy thật vì mỗi OEM có thể trả keycode khác nhau.

## 0.2. Baseline thực tế: Doov R17 Pro

Profile mặc định của bản đầu tiên là **3.5 inch / Doov R17 Pro**. Không hard-code độ phân giải: các listing/manual công khai của cùng tên máy đang ghi nhận cả 480 × 854 và 640 × 960, trong khi kích thước màn hình khoảng 3.54 inch. App phải lấy `displayMetrics` lúc chạy và giữ cùng layout theo `dp/sp`.

- Ưu tiên kiểm thử trên màn hình thật: Home, Drawer, long-press T9, D-pad góc, lock và mở app.
- Profile 2.8 inch vẫn giữ trong mockup/spec để làm regression floor, nhưng không tối ưu trải nghiệm chính trước khi R17 Pro ổn định.
- Nếu R17 Pro trả keycode khác với mapping chuẩn, dùng màn hình debug keycode trước khi thêm mapping OEM riêng.

## 0.3. Cấu hình bằng cảm ứng trên Home

- Nhấn giữ khoảng **700 ms** vào vùng trống bên dưới danh sách app trên Home sẽ mở “Cấu hình launcher”. Nhấn vào một dòng app không được mở app trong thời gian giữ.
- Dùng ▲▼ để chọn dòng, OK để đổi giá trị, Back để lưu và quay về Home.
- Có thể đổi: màu/wallpaper tĩnh, cỡ chữ dạng số 12–24 sp, số lượng app Home từ 1–9 và app được gán cho từng phím 1–9 (bao gồm “Chưa gán”).
- Cấu hình lưu cục bộ bằng Preferences; không cần mạng và không yêu cầu quyền hệ thống.

Tài liệu mô tả toàn bộ hành vi của launcher dựa trên prototype `t9-launcher-mockup.html`, dùng làm tham chiếu khi lên thiết kế kỹ thuật / giao việc dev.

**Thiết bị mục tiêu:** Android 11, màn hình 2.8" hoặc 3.5", bàn phím vật lý T9 (12 phím) + cụm điều hướng 3x3 (4 phím góc + 4 mũi tên + phím giữa OK). Mọi hành vi phải dùng được trên profile 2.8" trước.

**Triết lý:** tối giản — chỉ 5 màn hình (Home, Danh sách app, Gọi điện, Cấu hình, Khoá màn hình), không cuộn ngang, không lưới icon, mọi thao tác đều thực hiện được bằng phím cứng.

---

## 1. Cụm điều hướng (D-pad)

```
 [1]   ▲   [2]
 ◀    OK    ▶
 [3]   ▼   [4]
```

| Phím | Chức năng |
|---|---|
| **1** (góc trên-trái) | Mở Danh sách app (Drawer) |
| **2** (góc trên-phải) | Back — quay lại / đóng màn hiện tại |
| **3** (góc dưới-trái) | Mở nhanh màn hình Gọi điện, sẵn sàng bấm số |
| **4** (góc dưới-phải) | **2 bước:** nếu đang ở màn khác Home → về Home trước. Nếu đã ở Home → khoá màn hình |
| **▲ / ▼** | Di chuyển lựa chọn (highlight) giữa các item trong danh sách hiện tại |
| **◀ / ▶** | Ở Home/Drawer: di chuyển lựa chọn như ▲▼. Ở màn Cấu hình: chuyển tab |
| **OK (giữa)** | Mở/xác nhận mục đang chọn |

## 2. Bàn phím T9 (12 phím)

| Thao tác | Ngữ cảnh | Kết quả |
|---|---|---|
| Nhấn nhanh phím số 1–9 | Home | Mở app đã gán ở vị trí số đó |
| **Giữ** phím số bất kỳ (~500ms) | Mọi màn hình | Mở nhanh màn Gọi điện, điền sẵn số vừa giữ — đây là **hotkey chính**: T9 + số → vào thẳng Dialer |
| Nhấn phím số theo kiểu multi-tap | Danh sách app (Drawer) | Gõ chữ để lọc danh sách theo tên (bỏ dấu khi so khớp) |
| Phím `*` | Drawer | Xoá lùi 1 ký tự trong ô tìm kiếm |
| Phím `#` (nhấn nhanh) | Drawer | Xoá toàn bộ ô tìm kiếm |
| **Giữ** phím `#` | Mọi màn hình | Bật / tắt chế độ im lặng |

## 3. Màn hình chính (Home)

- Đồng hồ giờ:phút (font đơn cách) + thứ, ngày ở phía trên
- Danh sách app đã gán, mỗi dòng gồm: số thứ tự (badge) + icon + tên
- Cấu hình được:
  - **Số lượng app hiển thị**: 1–9
  - **Căn danh sách**: trái hoặc phải
- Vị trí chưa gán app hiển thị "Chưa gán", không mở gì khi chọn
- Item đang chọn có viền nhấn màu accent, tự cuộn vào khung nhìn khi dùng ▲▼◀▶

## 4. Danh sách app (Drawer)

- Danh sách dọc thuần: icon + tên, không chia nhóm, không lưới
- Ô tìm kiếm ở đầu, gõ bằng T9 multi-tap; không có kết quả → hiện "Không tìm thấy app"
- Chọn item bằng ▲▼◀▶, mở bằng OK hoặc chạm trực tiếp

## 5. Màn hình Gọi điện (Dialer)

- Vào từ: giữ phím số (điền sẵn số đó), hoặc phím góc **3**
- Hiển thị số đang nhập, nút Xoá (⌫) và nút Gọi trên màn hình
- Back (**2**) đóng màn, quay về Home

## 6. Cấu hình riêng của launcher

**Cách vào:** giữ 5 giây vào một vị trí **trống** trên Home (không phải app) → mở màn Cấu hình.

Gồm 3 tab, chuyển bằng **◀ ▶**:

| Tab | Điều khiển | Hành vi |
|---|---|---|
| **Gán phím** | ▲▼ chọn vị trí 1–9 · OK để đổi | Mỗi lần OK sẽ chuyển vòng qua app kế tiếp trong danh sách (kể cả để "Trống") |
| **Hình nền** | ▲▼ chọn preset | Áp dụng ngay khi cuộn (live preview), gồm các nền màu/gradient tối để chữ luôn đọc được |
| **Cỡ chữ** | ▲▼ chọn giá trị số 12–24 sp | Áp dụng ngay lên tên app và preview cấu hình |

## 7. Khoá màn hình

- Kích hoạt bằng phím **4** khi đang ở Home (nếu đang ở màn khác, bấm lần 1 chỉ đưa về Home)
- Overlay đen phủ toàn màn hình, chặn mọi input, chỉ hiện đồng hồ + gợi ý "Nhấn phím bất kỳ để mở khoá"
- Bất kỳ phím nào (D-pad hoặc T9) cũng mở khoá

## 8. Chế độ im lặng

- Bật/tắt bằng cách giữ phím `#`, hoạt động từ mọi màn hình
- Có icon 🔇 hiển thị ở thanh trạng thái khi đang bật, kèm toast xác nhận

---

# Đề xuất công nghệ triển khai (Android 11)

## Nền tảng & ngôn ngữ
- **Kotlin** + **Jetpack Compose** cho toàn bộ UI. Màn hình nhỏ, ít thành phần động — Compose giúp quản lý state (danh sách, lựa chọn, tab Cấu hình) gọn hơn nhiều so với View + XML truyền thống, và dễ làm animation trượt giữa các "view" giống bản HTML.
- Nếu team quen View system hơn và muốn tối ưu bộ nhớ/khởi động trên thiết bị cấu hình thấp, dùng **View + ViewBinding + RecyclerView** cũng hợp lý — 3.5" màn hình yếu nên launcher cần khởi động rất nhanh, đây là điểm cân nhắc chính khi chọn Compose vs View.

## Vai trò Launcher
- Khai báo Activity gốc với `category.HOME` + `category.DEFAULT` trong `AndroidManifest.xml` để người dùng đặt làm launcher mặc định.
- Không cố gắng intercept phím Home vật lý — bản thân app **là** launcher nên phím Home hệ thống tự động quay về nó.

## Bắt phím cứng (D-pad + T9)
- Override `dispatchKeyEvent()` ở Activity gốc để bắt tất cả `KEYCODE_0`..`KEYCODE_9`, `KEYCODE_STAR`, `KEYCODE_POUND`, `KEYCODE_DPAD_UP/DOWN/LEFT/RIGHT/CENTER` trước khi hệ thống xử lý.
- Phân biệt nhấn ngắn / giữ: dùng `Handler.postDelayed` bắt đầu tại `ACTION_DOWN`, huỷ nếu `ACTION_UP` đến sớm hơn ngưỡng (~500ms cho quick-dial, có thể cấu hình). Không dùng `KeyEvent.getRepeatCount()` vì nó báo lặp liên tục chứ không tách rõ 2 hành vi.
- 4 phím góc thường map vào `KEYCODE_SOFT_LEFT/SOFT_RIGHT` hoặc `KEYCODE_BUTTON_*` tuỳ hãng sản xuất — **cần test trên thiết bị thật**, vì mapping không chuẩn hoá giữa các dòng máy T9-Android.

## Lưu trữ cấu hình
- **Jetpack DataStore (Preferences)** để lưu: số lượng app hiển thị, căn trái/phải, bindings (map vị trí → app), wallpaper đã chọn, cỡ chữ, trạng thái im lặng. Nhẹ, async, phù hợp vài chục key-value đơn giản — không cần Room vì không có dữ liệu quan hệ.

## Danh sách app & tìm kiếm
- Lấy danh sách app cài đặt qua `PackageManager.queryIntentActivities()` với `Intent.ACTION_MAIN` + `CATEGORY_LAUNCHER`.
- Bỏ dấu tiếng Việt khi lọc: `java.text.Normalizer.normalize(str, Normalizer.Form.NFD)` rồi loại bỏ dấu kết hợp, xử lý riêng ký tự "đ" — giống hàm `normalize()` trong prototype.
- Multi-tap T9: bảng ánh xạ số → chữ cái cố định, dùng `Handler`/timestamp để quyết định "cùng phím trong khoảng thời gian" = xoay vòng ký tự, khác phím hoặc quá giờ = ký tự mới.

## Gọi điện
- Mở Dialer với số điền sẵn: `Intent(Intent.ACTION_DIAL, Uri.parse("tel:$so"))`. Không cần quyền `CALL_PHONE` vì không tự gọi, chỉ điền sẵn số cho người dùng bấm Gọi.

## Chế độ im lặng
- Gọi thẳng `AudioManager.setRingerMode(RINGER_MODE_SILENT)` (cần `NotificationManager.isNotificationPolicyAccessGranted()` trên Android 6+ do chính sách Do Not Disturb) thay vì tự quản lý cờ riêng, để đồng bộ với trạng thái chuông thật của máy.

## Khoá màn hình
- Bản demo tự vẽ overlay là **khoá mềm** riêng của app (chặn thao tác trong launcher), phù hợp nếu mục tiêu chỉ là tránh bấm nhầm.
- Nếu cần khoá bảo mật thật (yêu cầu mở khoá bằng PIN/vân tay), nên gọi `KeyguardManager` / để hệ thống Android xử lý qua nút nguồn, tránh chồng chéo với keyguard gốc của OS.

## Hình nền & cỡ chữ
- Vì màn hình nhỏ và ưu tiên hiệu năng, nên dùng gradient/màu phẳng (`Drawable` hoặc `Brush` trong Compose) như prototype thay vì ảnh bitmap — tránh chi phí decode ảnh trên phần cứng yếu.
- Cỡ chữ: lưu số nguyên 12–24 sp trong Preferences/DataStore, áp dụng ngay lên tên app và preview, không hard-code từng nơi.

## Kiến trúc đề xuất
- **Single Activity** + Compose Navigation (hoặc `when(state.view)` đơn giản như prototype, vì chỉ có 6 màn hình cố định — không cần NavGraph phức tạp).
- **MVVM nhẹ**: một `LauncherViewModel` giữ toàn bộ state (giống object `state` trong bản HTML) bằng `StateFlow`, các Composable chỉ quan sát và gọi hàm trên ViewModel khi nhận phím — giúp logic bắt phím (ở Activity) tách biệt khỏi logic hiển thị.

## Rủi ro kỹ thuật cần lưu ý sớm
1. Mapping phím cứng (D-pad 4 góc) khác nhau giữa các hãng → nên làm màn "Kiểm tra phím" ẩn để log keycode thực tế khi test thiết bị mới.
2. Xin quyền Do-Not-Disturb access phải hướng dẫn người dùng vào Settings thủ công, không xin được qua dialog runtime permission thông thường — cần màn hướng dẫn riêng lúc onboarding.
3. Launcher phải khởi động rất nhanh (thường < 1–2s) trên phần cứng thấp — tránh nạp toàn bộ danh sách app + icon lúc cold start, nên lazy-load Drawer khi thực sự mở.
