package com.sprint.mission.discodeit.config.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;


/**
 * WebSocket 채널 인터셉터 클래스
 * STOMP 메시지 송수신 과정에서 로깅 및 모니터링 기능을 처리한다
 * 
 * 주요 기능:
 * - WebSocket 연결/해제 이벤트 로깅
 * - 메시지 송수신 로깅 및 모니터링
 * - 기본적인 세션 관리
 */
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

	/* 설명. ChannelInterceptor 인터페이스:
	 *  ChannelInterceptor는 Spring Messaging 프레임워크에서 제공하는 인터페이스로,
	 *  메시지 채널을 통해 전송되는 메시지들을 가로채고 처리할 수 있는 기능을 제공한다.
	 *  이 인터페이스를 구현하면 메시지가 전송되기 전(preSend), 전송된 후(postSend), 그리고
	 *  전송 완료 후(afterSendCompletion) 등의 시점에서 추가적인 로직을 실행할 수 있다.
	 *  
	 *  이 프로젝트에서는 WebSocket 연결/해제 로깅과 기본적인 모니터링 기능을 제공한다.
	 */

    private static final Logger log = LoggerFactory.getLogger(WebSocketInterceptor.class);

    /**
     * 메시지가 채널로 전송되기 전에 호출되는 메서드
     * 기본적인 로깅 및 모니터링 작업을 수행한다
     * 
     * @param message 전송될 메시지
     * @param channel 메시지가 전송될 채널
     * @return 처리된 메시지 (null 반환 시 메시지 전송 중단)
     */
    /*
     * ChannelInterceptor의 핵심 메서드로, 모든 STOMP 메시지가 이 메서드를 거쳐간다
     * CONNECT, SUBSCRIBE, SEND, DISCONNECT 등 모든 STOMP 명령을 가로채서 처리할 수 있다
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // 1. STOMP 헤더 접근자 생성: 메시지에서 STOMP 헤더 정보를 추출하여 접근할 수 있도록 한다
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            log.warn("[WebSocket 인터셉터] STOMP 헤더 접근자를 생성할 수 없음");
            return message;
        }

        // 2. STOMP 명령 타입 확인: 메시지의 종류(CONNECT, SUBSCRIBE, SEND, DISCONNECT 등)를 확인한다
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }
        log.debug("[WebSocket 인터셉터] STOMP 명령 처리 시작: {}", command);

        // 3. STOMP 명령별 처리: 각 명령에 따라 다른 로직을 실행한다
        switch (command) {
            // CONNECT: 클라이언트가 서버에 연결할 때 사용하는 명령
			case CONNECT:
                handleConnect(accessor);
                break;
                
            // SUBSCRIBE: 클라이언트가 특정 토픽을 구독할 때 사용하는 명령
            case SUBSCRIBE:
                handleSubscribe(accessor);
                break;
                
            // SEND: 클라이언트가 서버에 메시지를 전송할 때 사용하는 명령
            case SEND:
                handleSend(accessor);
                break;
                
            // DISCONNECT: 클라이언트가 서버와의 연결을 끊을 때 사용하는 명령
            case DISCONNECT:
                handleDisconnect(accessor);
                break;
                
            // 처리되지 않은 명령(UNSUBSCRIBE, ACK, NACK 등)일 경우 로그 출력
            default:
                log.debug("[WebSocket 인터셉터] 처리되지 않은 STOMP 명령: {}", command);
        }

        return message;
    }

    /**
     * 메시지 전송 후 호출되는 메서드
     * 전송 성공/실패에 대한 후처리 작업을 수행한다
     * 
     * @param message 전송된 메시지
     * @param channel 메시지가 전송된 채널
     * @param sent 전송 성공 여부
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (!sent) {
            log.warn("[WebSocket 전송 후처리] 메시지 전송 실패: {}", message.getHeaders());
        } else {
            log.debug("[WebSocket 전송 후처리] 메시지 전송 성공");
        }
    }

    /**
     * 전송 완료 후 호출되는 메서드
     * 최종 정리 작업을 수행한다
     * 
     * @param message 전송이 완료된 메시지
     * @param channel 메시지가 전송된 채널
     * @param sent 전송 성공 여부
     * @param ex 전송 중 발생한 예외 (있는 경우)
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            log.error("[WebSocket 전송 완료] 메시지 전송 중 예외 발생", ex);
        }
    }

    /**
     * WebSocket 연결(CONNECT) 명령을 처리한다
     * CONNECT 프레임의 네이티브 헤더에서 userId를 추출해 StompPrincipal로 바인딩하여
     * 이후 @MessageMapping 메서드의 Principal 파라미터로 식별 정보를 전달한다.
     *
     * @param accessor STOMP 헤더 접근자
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        log.info("[WebSocket 연결] 새로운 WebSocket 연결 시도");

        // 1. CONNECT 프레임의 네이티브 헤더에서 userId 추출
        String userId = accessor.getFirstNativeHeader("userId");

        if (userId == null || userId.trim().isEmpty()) {
            // 학습용 프로젝트이므로 헤더 누락 시 연결은 허용하되 경고 로그를 남긴다
            log.warn("[WebSocket 연결] CONNECT 프레임에 userId 헤더가 없어 Principal 주입을 건너뛴다");
        } else {
            // 2. StompPrincipal을 생성해 세션에 바인딩 (이후 Principal 파라미터로 참조 가능)
            StompPrincipal principal = new StompPrincipal(userId.trim());
            accessor.setUser(principal);
            log.info("[WebSocket 연결] Principal 주입 완료 - userId: {}", principal.getName());
        }

        // 3. 기본적인 세션 정보 저장
        accessor.getSessionAttributes().put("connectTime", System.currentTimeMillis());

        log.info("[WebSocket 연결] WebSocket 연결 성공");
    }

    /**
     * 구독(SUBSCRIBE) 명령을 처리한다
     * 구독 이벤트를 로깅한다
     * 
     * @param accessor STOMP 헤더 접근자
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        log.info("[WebSocket 구독] 구독 요청: destination={}", destination);
    }

    /**
     * 메시지 전송(SEND) 명령을 처리한다
     * 메시지 전송 이벤트를 로깅한다
     * 
     * @param accessor STOMP 헤더 접근자
     */
    private void handleSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        log.debug("[WebSocket 전송] 메시지 전송: destination={}", destination);
    }

    /**
     * 연결 해제(DISCONNECT) 명령을 처리한다
     * 연결 해제 이벤트를 로깅한다
     * 
     * @param accessor STOMP 헤더 접근자
     */
    private void handleDisconnect(StompHeaderAccessor accessor) {
        // 연결 시간 계산
        Long connectTime = (Long) accessor.getSessionAttributes().get("connectTime");
        long sessionDuration = connectTime != null ? 
            System.currentTimeMillis() - connectTime : 0;
        
        log.info("[WebSocket 연결해제] 연결 해제 - 세션 지속시간: {}ms", sessionDuration);
        
        // 세션 정리
        accessor.getSessionAttributes().clear();
    }



}