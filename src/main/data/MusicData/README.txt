file .json để lưu metadata (URL, tên bài, nghệ sĩ, thời lượng,...) để chạy UI.

Khi chạy Main:
-Tạo cửa sổ JavaFX -> Khởi tạo controller để thao tác với JavaFX.
-Controller được khởi tạo sẽ nhận các giá trị từ Main (Main lấy metadata từ các file data/json) như Songlist, Playlist, ListenHistory, FavouriteSong, ...
-Các dữ liệu này được controller dùng để khởi tạo UI và là data để user thao tác.
+Playlist, History,... hiện các thông tin cơ bản metadata về bài nhạc như tên bài, nghệ sĩ, thời gian,...
+Khi click vào một bài nào đó. Lấy dữ liệu bài hát trong data/AllSongList qua metadata SongPath. Dữ liệu X đó -> đối tượng Media -> đối tượng MediaPlayer.
+Sử dụng đối tượng MediaPlayer của JavaFX để phát nhạc.
+...
-Sau khi thoát ứng dụng stop() sẽ lưu các metadata mới đã thay đổi vào lại json. 