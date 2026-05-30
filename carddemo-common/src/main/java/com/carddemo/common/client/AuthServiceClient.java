package com.carddemo.common.client;

import com.carddemo.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${carddemo.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @GetMapping("/api/users/{userId}")
    UserDto getUser(@PathVariable String userId);
}
