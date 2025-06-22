package com.heuk.base.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QuartzCreateParam {
    private String jobClazz;
    private String jobName;
    private String jobGroup;
    private Map<String,Object> jobDataMap;
    private String cron;
    private String description;
}
