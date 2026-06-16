package com.smartestate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("facility_bookings")
public class FacilityBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long facilityId;
    private Long slotId;
    private LocalDate bookingDate;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Facility facility;

    @TableField(exist = false)
    private FacilityTimeSlot slot;
}
