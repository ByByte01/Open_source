package com.dashan.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Fallbackcontroller {
    @ResponseBody
    @RequestMapping(value = "/fallbackcontroller")
    public ResponseEntity<String> fallBackController() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body( "超时限流")
                ;
    }
}
