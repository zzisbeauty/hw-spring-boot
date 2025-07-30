package com.hanwei.home.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiStartConfigController {

    @RequestMapping("/")
    public String home(){
        return "Welcome to Smart-WaterAI";
    }

}
