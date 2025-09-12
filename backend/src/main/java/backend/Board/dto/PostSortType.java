package backend.Board.dto;

public enum PostSortType {
    DATE,      // 날짜순 (기본)
    POPULAR,   // 인기순 (레딧 알고리즘)
    LIKES,     // 좋아요순
    VIEWS      // 조회수순
}