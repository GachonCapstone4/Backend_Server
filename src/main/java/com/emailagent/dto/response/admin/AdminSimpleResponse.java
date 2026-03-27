package com.emailagent.dto.response.admin;

import com.emailagent.dto.response.auth.BaseResponse;

/**
 * content_type / result_code / result_req 공통 필드만 반환하는 응답
 * - 삭제 등 추가 필드가 없는 관리자 API에서 사용
 */
public class AdminSimpleResponse extends BaseResponse {
    public static final AdminSimpleResponse OK = new AdminSimpleResponse();
}
