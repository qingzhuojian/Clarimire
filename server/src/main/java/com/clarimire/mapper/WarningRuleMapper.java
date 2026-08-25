package com.clarimire.mapper;

import com.clarimire.entity.WarningRule;
import org.apache.ibatis.annotations.Param;

public interface WarningRuleMapper {
    WarningRule selectByIndicator(@Param("indicator") String indicator);
}
