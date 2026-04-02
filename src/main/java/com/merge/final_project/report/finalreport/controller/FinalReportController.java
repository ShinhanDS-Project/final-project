package com.merge.final_project.report.finalreport.controller;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finalReport")
@Log4j2
public class FinalReportController {

    @GetMapping({"/","/list"})
    public String list(){

        log.info("list..............");

        return "finalReport/list";
    }
}
