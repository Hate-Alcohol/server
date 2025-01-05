package org.example.hatealcohol.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SuccessController {
    @GetMapping("/success")
    @ResponseBody
    public String success() {
        return "로그인 성공!";
    }
}