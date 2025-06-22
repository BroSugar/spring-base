package com.heuk.base.dto;

import lombok.Data;

@Data
public class QuartzUpdateParam {
    private String jobName;
    private String jobGroup;
    private String cron;
}
