package com.sky.controller.admin;

import com.sky.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public Result<String> home() {
        return Result.success("Sky Take Out Backend API");
    }
}
