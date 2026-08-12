# T9 Launcher

T9 Launcher là launcher Android tối giản dành cho điện thoại màn hình nhỏ có bàn phím vật lý T9 và cụm điều hướng D-pad. Ứng dụng ưu tiên thao tác bằng phím cứng, khởi động nhẹ và hiển thị dạng danh sách dễ đọc thay cho lưới biểu tượng truyền thống.

Phiên bản hiện tại được phát triển và kiểm thử chính trên **Doov R17 Pro**. Giao diện có hai profile thiết kế tham chiếu là **2.8 inch/QVGA** và **3.5 inch/HVGA**.

## Tính năng

- Hoạt động như Home launcher mặc định của Android.
- Home dạng danh sách với đồng hồ, ngày tháng và 1–9 vị trí ứng dụng tùy chỉnh.
- Mở nhanh ứng dụng bằng phím số `1`–`9`, hoặc chuyển các phím số sang chế độ mở trình quay số.
- Drawer liệt kê toàn bộ ứng dụng, có thể chuyển giữa dạng danh sách và lưới icon; hỗ trợ tìm kiếm không phân biệt dấu tiếng Việt và sắp xếp kết quả theo độ phù hợp.
- Điều hướng hoàn toàn bằng D-pad: `▲`, `▼`, `◀`, `▶`, `OK` và các phím góc.
- Mở nhanh Điện thoại, Danh bạ và Nhắn tin hệ thống.
- Vuốt ngang trên Home để mở hành động hoặc ứng dụng đã gán; vuốt lên để mở Drawer.
- Chuyển giữa chế độ Chuông và Rung bằng cách giữ phím `#` khoảng 5 giây.
- Khóa màn hình thật qua Android Device Admin.
- Tự khôi phục launcher khi desktop OEM `com.dp.op` của Doov cố mở đè.
- Lưu cục bộ màu nền, cỡ chữ, trạng thái thanh thông báo, số app Home, gán phím và hành động vuốt.
- Không cần kết nối mạng và không yêu cầu quyền gọi điện trực tiếp; ứng dụng chỉ mở dialer với số được điền sẵn.

## Bố trí phím mặc định

```text
 [1]   ▲   [2]
  ◀    OK   ▶
 [3]   ▼   [4]
```

| Phím | Hành vi |
|---|---|
| Góc `1` | Mở Drawer; trong Cấu hình: **Lưu và đóng** |
| Góc `2` hoặc `Back` | Quay lại; trong Cấu hình: **Thoát** về Home |
| Góc `3` / `Call` | Mở ứng dụng Điện thoại |
| Góc `4` / `Hang Up` | Từ màn hình con về Home; tại Home khóa màn hình |
| `▲ ▼ ◀ ▶` | Di chuyển mục đang chọn; trong Cấu hình, `◀ ▶` chuyển tab và `▲ ▼` chọn tùy chọn |
| `OK` | Mở hoặc xác nhận mục đang chọn |
| Số `1`–`9` tại Home | Mở app đã gán hoặc mở dialer, tùy cấu hình |
| Số `4` / `6` trong Cấu hình | Giảm / tăng nhanh giá trị đang chọn |
| Số `0` tại Home | Mở dialer với số `0` |
| `*` trong Drawer | Xóa một ký tự tìm kiếm |
| `#` trong Drawer | Xóa toàn bộ nội dung tìm kiếm |
| Giữ `#` khoảng 5 giây | Chuyển Chuông ↔ Rung |

Các keycode đang được nhận gồm phím số Android chuẩn, D-pad, `MENU`/`SOFT_LEFT`, `SOFT_RIGHT`, `CALL`/`BUTTON_L1` và `ENDCALL`/`BUTTON_R1`. Riêng firmware Doov R17 Pro có thể phát phím Hang Up dưới dạng Home intent; ứng dụng đã có xử lý riêng cho trường hợp này.

## Thiết bị hỗ trợ

| Mức hỗ trợ | Thiết bị/profile | Ghi chú |
|---|---|---|
| Đã kiểm thử thực tế | **Doov R17 Pro**, màn hình khoảng 3.5 inch | Đã kiểm thử phím vật lý Hang Up, khóa màn hình, Device Admin và cơ chế khôi phục khi desktop OEM mở đè |
| Profile mục tiêu | Android có màn hình **2.8 inch**, portrait, khoảng 240 × 320 px/QVGA | Là ngưỡng thiết kế và regression; vẫn cần kiểm thử keycode trên model thật |
| Profile mục tiêu | Android có màn hình **3.5 inch**, portrait, khoảng 320 × 480 px/HVGA hoặc cao hơn | Không hard-code độ phân giải; layout sử dụng `dp`/`sp` và tự cuộn theo chiều cao |
| Tương thích nền tảng | Android **6.0 (API 23)** trở lên | `minSdk 23`; baseline sản phẩm và kiểm thử ưu tiên Android 11+ |
| Chưa chứng nhận | Điện thoại T9 Android của OEM khác | Có thể chạy nếu phím trả keycode chuẩn, nhưng các phím góc/Hang Up và desktop OEM phải được kiểm thử, bổ sung mapping nếu cần |

Ứng dụng chỉ hỗ trợ Android và giao diện portrait. Máy phổ thông không chạy Android, iOS, tablet/landscape và thiết bị không có bộ keycode phù hợp không nằm trong phạm vi hỗ trợ hiện tại. Điện thoại cảm ứng Android vẫn có thể cài đặt để thử giao diện, nhưng không phải thiết bị mục tiêu.

> Kích thước QVGA/HVGA là mốc thiết kế, không phải cam kết rằng mọi model có cùng kích thước màn hình đều tương thích phím cứng.

## Yêu cầu build

- Windows 11 và PowerShell.
- JDK 17.
- Android SDK Platform 35.
- Android SDK Build Tools 35.0.0.
- Kết nối Internet trong lần build đầu để Gradle tải plugin và dependency.
- ADB nếu muốn cài APK từ dòng lệnh.

Dự án đang dùng Android Gradle Plugin 8.5.2 và Gradle Wrapper 9.0-milestone-1. Không cần cài Gradle toàn cục; luôn dùng `gradlew.bat` đi kèm repo.

## Build trên Windows

### 1. Khai báo Android SDK

Tạo file `local.properties` ở thư mục gốc nếu chưa có:

```properties
sdk.dir=C\:\\Users\\<ten-nguoi-dung>\\AppData\\Local\\Android\\Sdk
```

Nếu Android SDK nằm ở vị trí khác, thay đường dẫn cho phù hợp. Không commit `local.properties` vì file chứa đường dẫn riêng của máy phát triển.

### 2. Chạy unit test và tạo APK debug

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

APK được tạo tại:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Build lại từ đầu khi cần:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

### 3. Tạo APK release

```powershell
.\gradlew.bat testReleaseUnitTest assembleRelease
```

Repo chưa khai báo signing config cho bản phát hành. APK release sinh ra mặc định là bản chưa ký; cần ký bằng keystore của đơn vị phát hành trước khi phân phối.

## Cài đặt và cấu hình thiết bị

### Cài trực tiếp từ file APK

Cách này dành cho người dùng nhận sẵn APK đã ký, không cần cài Android SDK hay build source code.

1. Chép file APK vào điện thoại bằng cáp USB, Bluetooth, thẻ nhớ hoặc tải từ nguồn phát hành tin cậy.
2. Mở file APK bằng ứng dụng **Tệp/File Manager**.
3. Nếu Android chặn cài đặt, chọn **Cài đặt → Cho phép từ nguồn này** cho đúng ứng dụng đang mở APK, sau đó quay lại và chọn **Cài đặt**.
4. Sau khi cài xong, nhấn phím **Home**. T9 Launcher là Home Activity nên trên một số máy nút **Mở** có thể không xuất hiện và ứng dụng cũng có thể không nằm trong Drawer.
5. Khi Android hỏi ứng dụng màn hình chính, chọn **T9 Launcher → Luôn luôn**. Nếu không thấy hộp chọn, vào **Cài đặt → Ứng dụng → Ứng dụng mặc định → Ứng dụng màn hình chính** và chọn **T9 Launcher**.

Khi cập nhật, có thể mở APK mới và cài đè. APK mới phải được ký bằng cùng keystore với bản đang cài; nếu chữ ký khác, Android sẽ yêu cầu gỡ bản cũ trước và cấu hình launcher hiện tại có thể bị mất.

### Thiết lập bắt buộc sau khi cài

1. Vào **Cài đặt → Trợ năng/Khả năng tiếp cận → Dịch vụ đã cài đặt**, bật **T9 Launcher - Phím cứng** và chấp thuận cảnh báo của Android. Dịch vụ này nhận phím Hang Up và phát hiện desktop OEM Doov mở đè; cấu hình hiện tại không đọc nội dung cửa sổ.
2. Nhấn Home một lần nữa và xác nhận T9 Launcher vẫn là ứng dụng màn hình chính mặc định.
3. Tại Home của T9, bấm phím góc `4` để khóa màn hình. Ở lần đầu, chấp thuận quyền **Device Admin** cho T9 Launcher.
4. Nếu giữ `#` không chuyển được Chuông/Rung, cấp quyền truy cập **Không làm phiền** theo màn hình Cài đặt Android được mở ra.
5. Để nhập tìm kiếm bằng phím số trong Drawer, chọn một IME hỗ trợ bàn phím T9 vật lý, ví dụ QinVN. Launcher nhận văn bản từ IME, không tự triển khai bộ gõ multi-tap.
6. Khởi động lại điện thoại, mở một ứng dụng bất kỳ rồi nhấn Home/Hang Up để kiểm tra T9 trở lại và vẫn nhận phím cứng.

> **Lưu ý trên Doov R17 Pro:** APK thông thường không có quyền vô hiệu hóa desktop hệ thống `com.dp.op`. Dịch vụ trợ năng của T9 phát hiện desktop OEM xuất hiện rồi đưa T9 trở lại, vì vậy desktop gốc vẫn có thể lóe lên trong thời gian rất ngắn. Nếu chưa chọn T9 làm Home mặc định hoặc chưa bật dịch vụ trợ năng, cơ chế khôi phục này không hoạt động.

### Cài bằng ADB

Cách này dành cho kỹ thuật viên hoặc nhà phát triển. Kết nối thiết bị đã bật USB debugging, sau đó chạy:

```powershell
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell cmd package set-home-activity com.t9launcher/.MainActivity
```

Lệnh `set-home-activity` chỉ chọn T9 làm Home mặc định; nó không tự cấp Device Admin, quyền Không làm phiền hoặc bật dịch vụ trợ năng. Các quyền này vẫn phải được người dùng xác nhận trên điện thoại.

### Khắc phục sau khi cài

- **Nhấn Home vẫn về launcher cũ:** mở phần **Ứng dụng màn hình chính** và chọn lại T9 Launcher.
- **Doov thỉnh thoảng về desktop gốc:** kiểm tra **T9 Launcher - Phím cứng** còn được bật trong Trợ năng. Nếu đã bật nhưng không phục hồi, tắt/bật lại dịch vụ rồi khởi động lại máy.
- **Không cài đè được APK mới:** kiểm tra APK có cùng chữ ký với bản đang cài hay không. Chỉ gỡ bản cũ sau khi đã ghi lại cấu hình cần giữ.
- **Không gỡ được T9 Launcher:** tắt quyền Device Admin của T9 trong phần **Ứng dụng quản trị thiết bị** trước khi gỡ.

### Chuyển về launcher gốc hoặc gỡ T9

Thực hiện đúng thứ tự sau để dịch vụ khôi phục của T9 không kéo màn hình trở lại:

1. Tắt **T9 Launcher - Phím cứng** trong phần Trợ năng.
2. Vào **Ứng dụng mặc định → Ứng dụng màn hình chính** và chọn launcher gốc. Trên Doov R17 Pro, launcher OEM là `com.dp.op/.desk.DeskMainActivity`.
3. Nếu muốn gỡ ứng dụng, tắt thêm quyền **Device Admin** của T9 Launcher.
4. Gỡ T9 Launcher như một ứng dụng Android thông thường.

Kỹ thuật viên có thể chọn lại launcher OEM của Doov bằng ADB sau khi đã tắt dịch vụ trợ năng của T9:

```powershell
adb shell cmd package set-home-activity com.dp.op/.desk.DeskMainActivity
```

## Cấu hình launcher

Tại Home, giữ khoảng **700 ms** vào vùng trống bên dưới danh sách ứng dụng để mở **Cấu hình Launcher**.

Màn hình Cấu hình được chia thành 6 tab:

- **Hiển thị:** màu/wallpaper, cỡ chữ 12–36 sp, số app Home và thanh thông báo.
- **Drawer:** kiểu danh sách/lưới, số cột, số hàng, kích thước và bo góc icon.
- **Phím T9:** chế độ **Quick action** hoặc **Quay số** cho phím số ở Home.
- **Cử chỉ:** hành động vuốt hai chiều, gồm tắt, Danh bạ, Nhắn tin hoặc một ứng dụng.
- **App Home:** gán từng vị trí Home cho một ứng dụng hoặc để trống.
- **Tác giả:** hiển thị tác giả `ducseul`, loại build và phiên bản được Gradle tự sinh theo định dạng `yy.MMdd-HHmm` khi tạo APK.

Dùng `◀ ▶` để chuyển tab, `▲ ▼` để chọn, phím số `4`/`6` để giảm/tăng nhanh và `OK` để đổi hoặc mở các mục cần danh sách chọn. Phím góc `1` **Lưu và đóng**, phím góc `2` **Thoát** về Home; hai nhãn này luôn hiện ở hai góc dưới màn hình Cấu hình. Các thay đổi được áp dụng và lưu ngay khi chỉnh nên Thoát không hoàn tác giá trị đã đổi. Dải tab tự cuộn để hiện trọn tab đang chọn; icon `◀`/`▶` ở hai mép cho biết vẫn còn tab bị khuất và cũng có thể chạm để chuyển. Trên màn hình cảm ứng còn có thể chạm tên tab hoặc vuốt ngang.

## Kiểm thử

Chạy toàn bộ unit test JVM:

```powershell
.\gradlew.bat test
```

Unit test hiện tập trung vào cấu hình launcher, mô hình phím và thuật toán chuẩn hóa/tìm kiếm tên ứng dụng. Các luồng phụ thuộc firmware như keycode OEM, chọn launcher mặc định, Device Admin, dialer, Accessibility Service và khóa màn hình phải được kiểm thử lại trên thiết bị thật.

Khi tích hợp thêm một dòng máy T9 mới, tối thiểu cần kiểm tra:

1. Keycode của toàn bộ số, `*`, `#`, D-pad, OK và bốn phím góc.
2. Hành vi phím Home/Hang Up ở Home và Drawer.
3. Mở Điện thoại, Danh bạ, Nhắn tin và ứng dụng bên thứ ba.
4. Khóa/mở màn hình và quyền Device Admin.
5. Drawer, bộ gõ T9, tìm kiếm tiếng Việt và cuộn danh sách.
6. Layout ở cỡ chữ nhỏ/lớn và 1/9 app Home.
7. Khả năng giữ T9 Launcher ở foreground trước desktop riêng của OEM.

## Cấu trúc dự án

```text
app/src/main/java/com/t9launcher/
├── MainActivity.java              # Vòng đời Activity và tiếp nhận sự kiện phím
├── ui/                            # Render Home, Drawer, Settings và xử lý tương tác
├── input/                         # Ánh xạ keycode, nhấn/giữ và input Drawer
├── apps/                          # Đọc, lọc và sắp xếp ứng dụng đã cài
├── data/                          # Lưu cấu hình bằng SharedPreferences
├── model/                         # Mô hình cấu hình độc lập Android framework
└── system/                        # Dialer, danh bạ, nhắn tin, rung và khóa máy
```

Xem thêm [ARCHITECTURE.md](ARCHITECTURE.md) để biết quy tắc phụ thuộc và [t9-launcher-spec.md](t9-launcher-spec.md) để tham khảo đặc tả sản phẩm ban đầu.
