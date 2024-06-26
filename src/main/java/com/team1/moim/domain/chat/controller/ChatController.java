package com.team1.moim.domain.chat.controller;

import com.team1.moim.domain.chat.dto.request.ChatRequest;
import com.team1.moim.domain.chat.dto.response.ChatResponse;
import com.team1.moim.domain.chat.service.ChatService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * MessageMapping("/chat/{roomNumber}") 클라이언트가 전송하는 메시지 중, destination이 "/chat/{roomNumber}"인 메시지가 오면 해당 메서드가 처리한다.
     * <p>
     * SendTo("/sub/chat/{roomNumber}") 메서드 실행 결과를 "/sub/chat/{roomNumber}"로 전달하도록 한다. 이때 사용되는 주소 앞에는 WebSocketConfig의
     * configureMessageBroker에서 enableSimpleBroker에 설정한 "/sub"이 붙는다. 즉, 채팅방 번호를 매개변수로 받아서 해당 채팅방을 구독하고 있는 사용자들에게 메시지를
     * 브로드캐스트하게 된다.
     * <p>
     * 클라이언트는 "/pub/chat/{roomNumber}"로 메시지를 보내서 서버에 있는 해당 메서드를 호출. 서버는 ChatService에서 메시지를 처리한 뒤 그 결과를
     * "/sub/chat/{roomNumber}"로 브로드캐스트한다.
     */
    @MessageMapping("/chat/{roomNumber}")
    @SendTo("/sub/chat/{roomNumber}")
    public ChatResponse broadcasting(ChatRequest chatRequest,
                                     @DestinationVariable(value = "roomNumber") Long chatRoom) {
        log.info("Chat message received in room {}: {}", chatRoom, chatRequest);
        return chatService.save(chatRequest);
    }

    @GetMapping("/api/chat/messages/{roomId}")
    @ResponseBody
    public ResponseEntity<ApiSuccessResponse<List<ChatResponse>>> getMessages(
            HttpServletRequest servletRequest,
            @PathVariable Long roomId) {
        log.info("ID {}번 채팅룸 채팅 내역 로딩 시작", roomId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servletRequest.getServletPath(),
                        chatService.getMessages(roomId)));
    }
}
