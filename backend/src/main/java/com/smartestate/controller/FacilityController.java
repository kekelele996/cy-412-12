package com.smartestate.controller;

import com.smartestate.common.Constants;
import com.smartestate.common.Result;
import com.smartestate.dto.FacilityRequest;
import com.smartestate.entity.Facility;
import com.smartestate.service.FacilityService;
import com.smartestate.utils.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/facilities")
public class FacilityController {
    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public Result<List<Facility>> list(@RequestParam(required = false) String status, HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityController list status=%s role=%s", status, role);
        return Result.ok(facilityService.list(status, role));
    }

    @GetMapping("/{id}")
    public Result<Facility> getById(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityController getById id=%s role=%s", id, role);
        return Result.ok(facilityService.getById(id, role));
    }

    @PostMapping
    public Result<Facility> create(@RequestBody FacilityRequest body, HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityController create name=%s role=%s", body.getName(), role);
        return Result.ok(facilityService.create(role, body));
    }

    @PutMapping("/{id}")
    public Result<Facility> update(@PathVariable Long id, @RequestBody FacilityRequest body, HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityController update id=%s role=%s", id, role);
        return Result.ok(facilityService.update(id, role, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute(Constants.CURRENT_USER_ROLE);
        LogUtil.info("FacilityController delete id=%s role=%s", id, role);
        facilityService.delete(id, role);
        return Result.ok();
    }
}
