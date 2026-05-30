package com.mainframe.carddemo.common.client;

import com.mainframe.carddemo.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "carddemo-auth-service", url = "${feign.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @GetMapping("/api/users/{userId}")
    UserDto getUserById(@PathVariable("userId") String userId);
}
