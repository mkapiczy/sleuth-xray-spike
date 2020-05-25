package pl.mkapiczy.sleuthxrayspike.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
public class SleuthTestController {
    @GetMapping
    public String getTest() {
        log.info("Log from controller");
        return "test";
    }

}
