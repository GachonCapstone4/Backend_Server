package com.emailagent.dto.request.inbox;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReplyActionRequest {

    @NotBlank
    private String action; // SEND | EDIT_SEND | SKIP

    private String content; // EDIT_SEND일 때만 사용 (수정된 본문)
}
