package com.sprint.mission.discodeit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    /**
     * favicon.ico 요청을 처리하여 404 오류를 방지합니다.
     */
    @RequestMapping(value = "/favicon.ico", method = RequestMethod.GET)
    public ResponseEntity<Void> favicon() {
        // favicon이 없으면 204 No Content 반환
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


