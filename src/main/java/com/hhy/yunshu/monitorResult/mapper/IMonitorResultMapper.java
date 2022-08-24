package com.hhy.yunshu.monitorResult.mapper;

import com.hhy.yunshu.monitorResult.entity.MonitorResult;
import org.springframework.stereotype.Repository;

@Repository
public interface IMonitorResultMapper {

    /**
     * 保存监控结果
     * @param monitorResult 监控结果信息
     */
    void saveMonitorResult(MonitorResult monitorResult);
}
