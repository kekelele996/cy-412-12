package com.smartestate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartestate.entity.FacilityTimeSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FacilityTimeSlotMapper extends BaseMapper<FacilityTimeSlot> {

    @Select("SELECT s.*, " +
            "(SELECT COUNT(*) FROM facility_bookings b " +
            "WHERE b.slot_id = s.id AND b.booking_date = #{bookingDate} AND b.status = 'booked') AS booked_count " +
            "FROM facility_time_slots s " +
            "WHERE s.facility_id = #{facilityId} AND s.status = 'active' " +
            "AND (s.weekday IS NULL OR s.weekday = #{weekday}) " +
            "ORDER BY s.start_time")
    List<FacilityTimeSlot> selectSlotsWithBookedCount(@Param("facilityId") Long facilityId,
                                                      @Param("bookingDate") LocalDate bookingDate,
                                                      @Param("weekday") Integer weekday);
}
