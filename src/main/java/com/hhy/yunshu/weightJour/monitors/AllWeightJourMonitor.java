package com.hhy.yunshu.weightJour.monitors;

import com.hhy.yunshu.base.api.IBaseHandler;
import com.hhy.yunshu.monitorResult.entity.MonitorResult;
import com.hhy.yunshu.monitorResult.mapper.IMonitorResultMapper;
import com.hhy.yunshu.utils.ApiUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 全管理中心超重车监控
 * @author JiangZhiyan
 */
@Component
public class AllWeightJourMonitor implements IBaseHandler {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ValueOperations<String,Object> valueOperations;

    @Autowired
    private IMonitorResultMapper monitorResultMapper;

    private static final String SCHEMA_CODE = "incontrolrecord";

    private static final String MONITOR_NAME = "云枢_全管理中心超重车(167:station_info:weight_jour)";

    /**
     * redis的key
     */
    private static final String YUNSHU_OVERWEIGHT_MONITOR_KEY = "YUNSHU:OVERWEIGHT:MONITOR:ALL";

    @Override
    @XxlJob(value = "allWeightJourMonitor")
    public void doHandle() {
        MonitorResult monitorResult = MonitorResult.success(MONITOR_NAME);
        try {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(YUNSHU_OVERWEIGHT_MONITOR_KEY))) {
                valueOperations.set(YUNSHU_OVERWEIGHT_MONITOR_KEY, 0);
            }
            // 上次监控存的数据
            int existNum = Integer.parseInt(Objects.requireNonNull(valueOperations.get(YUNSHU_OVERWEIGHT_MONITOR_KEY)).toString());
            XxlJobHelper.log("上次监控云枢全管理中心超重车数据条数:" + existNum + "条");
            ApiUtils api = new ApiUtils(SCHEMA_CODE);
            List<Map<String, Object>> allFormData = api.getFormData(this.getFilters(),0,Integer.MAX_VALUE);
            XxlJobHelper.log("此时云枢全管理中心超重车数据条数:" + allFormData.size() + "条");
            if (existNum > allFormData.size()) {
                monitorResult = MonitorResult.fail(MONITOR_NAME,"此时云枢全管理中心超重车数据条数:"
                        + allFormData.size() + ",小于上次监控云枢全管理中心超重车数据条数:" + existNum);
            }
            valueOperations.set(YUNSHU_OVERWEIGHT_MONITOR_KEY,Math.max(existNum,allFormData.size()));
        }catch (Exception e) {
            XxlJobHelper.log("发生异常:" + e.getMessage());
            monitorResult = MonitorResult.error(MONITOR_NAME,e.getMessage());
        }finally {
            monitorResultMapper.saveMonitorResult(monitorResult);
        }
    }

    /**
     * 筛选超重车数据
     */
    private List<Map<String, Object>> getFilters() {
        List<Map<String,Object>> filters = new ArrayList<>(1);
        Map<String,Object> filterMap = new HashMap<String,Object>(5) {
            {
                put("op","Eq");
                put("propertyCode","Number1660206852768");
                put("propertyType",0);//TODO 数字类型不确定是不是0
                put("propertyValue",666);
                put("propertyValueName","");
            }
        };
        filters.add(filterMap);
        return filters;
    }
}
