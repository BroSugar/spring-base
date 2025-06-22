package com.heuk.base.controller;

import com.heuk.base.dto.QuartzCreateParam;
import com.heuk.base.dto.QuartzDetailParam;
import com.heuk.base.dto.QuartzJobDetailDto;
import com.heuk.base.dto.QuartzUpdateParam;
import com.heuk.base.service.impl.QuartzServiceImpl;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/quartz")
public class QuartzController {
    private final QuartzServiceImpl quartzService;

    public QuartzController(
            QuartzServiceImpl quartzService
    ) {
        this.quartzService = quartzService;
    }

    @PostMapping("/addJob")
    public void addJob(@RequestBody QuartzCreateParam param) throws SchedulerException {
        quartzService.addJob(param);
    }

    @PostMapping("/updateJob")
    public void updateJob(@RequestBody QuartzUpdateParam param) throws SchedulerException {
        quartzService.updateJob(param);
    }

    @PostMapping("/pauseJob")
    public void pauseJob(@RequestBody QuartzDetailParam param) throws SchedulerException {
        quartzService.pauseJob(param);
    }

    @PostMapping("/resumeJob")
    public void resumeJob(@RequestBody QuartzDetailParam param) throws SchedulerException {
        quartzService.resumeJob(param);
    }

    @PostMapping("/deleteJob")
    public void deleteJob(@RequestBody QuartzDetailParam param) throws SchedulerException {
        quartzService.deleteJob(param);
    }

    @PostMapping("/jobList")
    public List<QuartzJobDetailDto> jobList() throws SchedulerException {
        return quartzService.jobList();
    }

    @PostMapping("/jobDetail")
    public QuartzJobDetailDto jobDetail(@RequestBody QuartzDetailParam param) throws SchedulerException {
        return quartzService.jobDetail(param);
    }
}
