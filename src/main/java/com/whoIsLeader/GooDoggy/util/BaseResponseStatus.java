package com.whoIsLeader.GooDoggy.util;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    SUCCESS(true, 1000, "요청에 성공하였습니다."),
    TEST(false, 1001, "catch문 테스팅"),

    DUPLICATE_ID(false, 2000, "아이디가 중복되었습니다."),
    DISMATCH_PASSWORD(false, 2001, "비밀번호가 일치하지 않습니다."),
    INVALID_EMAIL(false, 2002, "이메일 형식이 잘못되었습니다."),
    NON_EXIST_ID(false, 2003, "아이디가 존재하지 않습니다."),
    FAILED_TO_LOGIN(false, 2004, "로그인 처리를 실패하였습니다."),
    NON_EXIST_SESSION(false, 2005, "유저 세션이 존재하지 않습니다."),
    INACTIVE_USER(false,2006,"비활성화 처리된 유저입니다."),
    NON_EXIST_USERIDX(false, 2007, "유저 인덱스가 존재하지 않습니다."),

    DATABASE_INSERT_ERROR(false, 6000, "데이터베이스 저장 오류가 발생하였습니다.")
    ;

    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
