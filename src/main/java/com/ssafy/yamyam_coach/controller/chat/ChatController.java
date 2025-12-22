package com.ssafy.yamyam_coach.controller.chat;

import com.ssafy.yamyam_coach.controller.chat.request.ChatRequest;
import com.ssafy.yamyam_coach.domain.user.User;
import com.ssafy.yamyam_coach.global.annotation.LoginUser;
import com.ssafy.yamyam_coach.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public String chat(@LoginUser User user, @RequestBody @Valid ChatRequest request) {
        return chatService.request(user.getId(), request.getBodySpecId(), request.getContent());
    }
}
