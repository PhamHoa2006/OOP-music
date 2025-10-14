SUGGEST:{
	ID của Song, Playlist, Comment, User... cần phải có cú pháp khác nhau. 
	
		Song: S-<YYYYMMDD>-<base36Epoch>-<rnd4>
		Ví dụ: S-20251011-ktl9x7-4f3a
	
		Playlist: PL-<YYYYMMDD>-<base36Epoch>-<rnd4>
		Ví dụ: PL-20251011-ktl9x7-a92b
	
		User: U-<base36Epoch>-<rnd6> (người có thể đăng ký mọi lúc, không cần date đầu)
		Ví dụ: U-j2x1q9-1a7c9b
		
		Comment: C-<playlistShort>-<base36Epoch>-<rnd4> (gắn link tới playlist bằng phần playlistShort)
	
	
}

UPDATE:{
	- Thêm class Comment trong Playlist, methods: add, remove, get..
	
}

NOTE:{
	Một số lỗi như phương thức getDuration, sort(Song), ... liên quan tới Song class vẫn lỗi vì class Song chưa được hoàn thiện.
	Một số tính năng chỉ demo hoặc chưa hoạt động đúng, sẽ cần hoàn thiện thêm.

}
