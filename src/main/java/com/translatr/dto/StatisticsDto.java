package com.translatr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsDto {
    public long userCount;
    public long projectCount;
    public long activityCount;
}

