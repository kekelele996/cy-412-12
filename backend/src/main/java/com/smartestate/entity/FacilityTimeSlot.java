package com.smartestate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("facility_time_slots")
public class FacilityTimeSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long facilityId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer weekday;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Integer bookedCount;

    @TableField(exist = false)
    private Boolean available;
}
