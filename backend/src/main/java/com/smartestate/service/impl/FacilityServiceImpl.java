package com.smartestate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartestate.common.ErrorCode;
import com.smartestate.constants.FacilityConstants;
import com.smartestate.constants.LogTemplates;
import com.smartestate.constants.UserConstants;
import com.smartestate.dto.FacilityRequest;
import com.smartestate.entity.Facility;
import com.smartestate.mapper.FacilityMapper;
import com.smartestate.service.FacilityService;
import com.smartestate.service.OperationLogService;
import com.smartestate.utils.LogUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacilityServiceImpl implements FacilityService {
    private final FacilityMapper facilityMapper;
    private final OperationLogService operationLogService;

    public FacilityServiceImpl(FacilityMapper facilityMapper, OperationLogService operationLogService) {
        this.facilityMapper = facilityMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public List<Facility> list(String status, String role) {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<Facility>().orderByDesc(Facility::getCreatedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Facility::getStatus, status);
        } else if (UserConstants.RESIDENT.equals(role)) {
            wrapper.eq(Facility::getStatus, FacilityConstants.FACILITY_ACTIVE);
        }
        List<Facility> facilities = facilityMapper.selectList(wrapper);
        LogUtil.info(LogTemplates.FACILITY_LIST, role);
        return facilities;
    }

    @Override
    public Facility getById(Long id, String role) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_NOT_FOUND.format(id, role));
        }
        return facility;
    }

    @Override
    public Facility create(String role, FacilityRequest request) {
        Facility facility = new Facility();
        facility.setName(request.getName());
        facility.setDescription(request.getDescription());
        facility.setImage(request.getImage());
        facility.setLocation(request.getLocation());
        facility.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : FacilityConstants.FACILITY_ACTIVE);
        facility.setCreatedAt(LocalDateTime.now());
        facility.setUpdatedAt(LocalDateTime.now());
        facilityMapper.insert(facility);
        operationLogService.record(null, role, "facility.create", "Facility", facility.getId(),
                String.format(LogTemplates.FACILITY_CREATE, facility.getId(), facility.getName(), role));
        return facility;
    }

    @Override
    public Facility update(Long id, String role, FacilityRequest request) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_NOT_FOUND.format(id, role));
        }
        if (StringUtils.hasText(request.getName())) {
            facility.setName(request.getName());
        }
        if (request.getDescription() != null) {
            facility.setDescription(request.getDescription());
        }
        if (request.getImage() != null) {
            facility.setImage(request.getImage());
        }
        if (request.getLocation() != null) {
            facility.setLocation(request.getLocation());
        }
        if (StringUtils.hasText(request.getStatus())) {
            facility.setStatus(request.getStatus());
        }
        facility.setUpdatedAt(LocalDateTime.now());
        facilityMapper.updateById(facility);
        operationLogService.record(null, role, "facility.update", "Facility", id,
                String.format(LogTemplates.FACILITY_UPDATE, id, role));
        return facility;
    }

    @Override
    public void delete(Long id, String role) {
        Facility facility = facilityMapper.selectById(id);
        if (facility == null) {
            throw new IllegalArgumentException(ErrorCode.FACILITY_NOT_FOUND.format(id, role));
        }
        facilityMapper.deleteById(id);
        operationLogService.record(null, role, "facility.delete", "Facility", id,
                String.format(LogTemplates.FACILITY_DELETE, id, role));
    }
}
