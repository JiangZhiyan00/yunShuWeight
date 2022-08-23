package com.hhy.yunshu.weightJour.handler;

import com.hhy.yunshu.base.api.IBaseHandler;
import com.hhy.yunshu.utils.ApiUtils;
import com.hhy.yunshu.utils.AutoIncrementNoUtils;
import com.hhy.yunshu.weightJour.mapper.IWeightJourMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HuZhouWeightJourHandler implements IBaseHandler {

    private static final String SCHEMA_CODE = "";

    @Autowired
    private IWeightJourMapper weightJourMapper;

    @Autowired
    private AutoIncrementNoUtils autoIncrementNoUtils;

    @Override
    @XxlJob(value = "huZhouWeightJourHandler")
    public void doHandle() {
        ApiUtils apiUtils = new ApiUtils(SCHEMA_CODE);

    }
}
