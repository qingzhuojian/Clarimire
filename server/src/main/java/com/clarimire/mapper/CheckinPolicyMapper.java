package com.clarimire.mapper;

import com.clarimire.entity.CheckinPolicy;
import com.clarimire.entity.ReservoirLocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CheckinPolicyMapper {
    CheckinPolicy getPolicy();

    int updatePolicy(CheckinPolicy policy);

    List<ReservoirLocation> findAllLocations();

    ReservoirLocation findByReservoirName(String reservoirName);
}
