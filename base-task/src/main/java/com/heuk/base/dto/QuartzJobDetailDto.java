package com.heuk.base.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuartzJobDetailDto {
    private String jobClazz;
    private String jobName;
    private String jobGroup;
    private Map<String, Object> jobDataMap;
    private List<QuartzTriggerDetailDto> triggerDetailDtoList;
}
