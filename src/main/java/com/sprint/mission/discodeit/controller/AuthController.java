package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

		private final AuthService authService;

		@PostMapping(path = "login")
		public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest loginRequest) {
				UserDto user = authService.login(loginRequest);
				return ResponseEntity.status(HttpStatus.OK).body(user);
		}

		@GetMapping(path = "csrf-token")
		public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
				String tokenValue = csrfToken.getToken();
				log.debug("CSRF 토큰 요청: {}", tokenValue);
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
		}
}
