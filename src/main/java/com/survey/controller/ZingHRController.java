//package com.survey.controller;
//
//import com.survey.serviceImpl.ZingHRFetchService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/zing")
//@RequiredArgsConstructor
//public class ZingHRController {
//
//    private final ZingHRFetchService service;
//
//    @GetMapping("/fetch-now")
//    public String fetchNow() {
//        service.fetchEmployeesAndSend();
//        return "ZingHR fetch triggered!";
//    }
//}


package com.survey.controller;

import com.survey.serviceImpl.ZingHRFetchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zing")
@RequiredArgsConstructor
public class ZingHRController {

    private final ZingHRFetchService service;

    @GetMapping("/fetch-now")
    public String fetchNow() {
        service.fetchEmployeesAndSend();
        return "ZingHR fetch triggered!";
    }
}
