package com.smartestate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("facilities")
public class Facility {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String image;
    private String location;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
