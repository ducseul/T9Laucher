# T9 Launcher Drawer — design spec

## Assumptions

- Màn hình mục tiêu là thiết bị Android 640 × 960 px, có thanh trạng thái hệ thống cao khoảng 56 px.
- Drawer phải dùng tốt bằng D-pad, giữ D-pad để cuộn nhanh, đồng thời chạm, giữ chạm và vuốt cảm ứng.
- Nội dung thật là danh sách ứng dụng có thể dài; Telegram phải hiện cùng các app launcher khác.
- Không dùng icon trang trí hoặc ảnh vì Drawer là công cụ tìm/mở ứng dụng; bỏ ảnh không làm mất thông tin.
- Giữ nền tối, chữ sáng và accent hổ phách hiện tại để không làm đứt nhận diện của T9 Launcher.

## Bản chất yêu cầu

Drawer hiện tại có đủ chức năng cơ bản nhưng nhịp dọc chưa tạo được cảm giác “danh sách có chủ ý”: ô tìm kiếm sát danh sách, khoảng cách giữa các hàng phụ thuộc trực tiếp vào cỡ chữ, và trạng thái chọn là một mảng nền lớn dễ làm giao diện nặng. Thiết kế mới phải làm rõ ba lớp: vùng tìm kiếm, danh sách ứng dụng, và trạng thái chọn. Spacing phải đủ rộng để đọc và chạm chính xác trên màn 640 px, nhưng vẫn cho thấy đủ số lượng app để người dùng không phải cuộn quá nhiều. Vì đây là launcher cho thiết bị có bàn phím vật lý, trạng thái focus cần nhìn thấy ngay ở khoảng cách cầm máy 30–45 cm và không được tạo hiệu ứng gray-out toàn màn. Tương tác cảm ứng và D-pad phải dùng chung một chỉ báo chọn, tránh hai mô hình trạng thái khác nhau.

## Form derivation

- Vai trò kể chuyện: màn công cụ truy cập nhanh, không phải dashboard hay trang giới thiệu.
- Khoảng cách người xem: điện thoại ở 30–45 cm; chữ app tối thiểu tương đương 24–28 px ở prototype.
- Nhiệt độ thị giác: bình tĩnh, rõ, hơi “feature phone”, có một accent ấm.
- Sức chứa: mục tiêu 9–12 app nhìn thấy tùy hướng; spacing phải có quy luật và vẫn cuộn trơn.
- Motif riêng: nhịp phím T9 — các hàng đều như nhịp phím, focus là một “con trỏ phần cứng” chứ không phải card trang trí.

## Ba hướng

1. **Balanced Rhythm** — baseline dễ triển khai: header gọn, search riêng, hàng 64 px, khoảng hở 4 px; focus nền hổ phách tối và vạch trái.
2. **Quiet Lanes** — thoáng nhất: tiêu đề lớn, search dạng underline, hàng 72 px chia bằng hairline; focus dùng chữ hổ phách + rail bên trái, gần như không dùng card.
3. **T9 Index Rail** — giàu cấu trúc hơn: rail chữ cái bên trái, hàng 58 px, khoảng hở 8 px giữa các cụm; focus là khung sáng mảnh, giúp định vị khi cuộn dài.

Mọi phương án dùng cùng dữ liệu và hỗ trợ click, giữ click, phím ↑/↓, Enter và nhập tìm kiếm để so sánh công bằng.
