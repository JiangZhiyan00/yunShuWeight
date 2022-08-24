package com.hhy.yunshu.monitorResult.entity;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hhy.yunshu.constants.Constants;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控结果实体类
 * @author JiangZhiyan
 */
public class MonitorResult implements Serializable {

    private static final long serialVersionUID = 6205582793250963061L;

    /**
     * 主键id
     */
    private String id;

    /**
     * 监控名
     */
    private String monitorName;

    /**
     * 说明
     */
    private String monitorDesc;

    /**
     * 监控结果
     */
    private int result;

    /**
     * 监控结果提示信息
     */
    private String message;

    /**
     * 运行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date runTime;

    /**
     * 是否已经发过短信提醒
     */
    private int noticeFlag;

    private MonitorResult(String id, String monitorName, String monitorDesc, int result, String message, Date runTime, int noticeFlag) {
        this.id = id;
        this.monitorName = monitorName;
        this.monitorDesc = monitorDesc;
        this.result = result;
        this.message = message;
        this.runTime = runTime;
        this.noticeFlag = noticeFlag;
    }

    public String getId() {
        return id;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getMonitorDesc() {
        return monitorDesc;
    }

    public int getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Date getRunTime() {
        return runTime;
    }

    public int getNoticeFlag() {
        return noticeFlag;
    }

    public static MonitorResult success(String monitorName, String monitorDesc, String message) {
        return new MonitorResult(IdUtil.fastSimpleUUID(),monitorName,monitorDesc,1,message,new Date(),0);
    }

    public static MonitorResult success(String monitorName, String monitorDesc) {
        return success(monitorName,monitorDesc, Constants.SUCCESS);
    }

    public static MonitorResult success(String monitorName) {
        return success(monitorName,monitorName);
    }

    public static MonitorResult fail(String monitorName, String monitorDesc, String message) {
        return new MonitorResult(IdUtil.fastSimpleUUID(),monitorName,monitorDesc,2,message,new Date(),0);
    }

    public static MonitorResult fail(String monitorName, String message) {
        return fail(monitorName,monitorName,message);
    }

    public static MonitorResult error(String monitorName, String monitorDesc, String message) {
        return new MonitorResult(IdUtil.fastSimpleUUID(),monitorName,monitorDesc,3,message,new Date(),0);
    }

    public static MonitorResult error(String monitorName, String message) {
        return error(monitorName,monitorName,message);
    }
}
