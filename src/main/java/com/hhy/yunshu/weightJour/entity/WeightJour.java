package com.hhy.yunshu.weightJour.entity;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hhy.yunshu.weightJour.enums.ColorEnum;
import com.hhy.yunshu.weightJour.enums.VehicleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * 超重车信息
 * @author JiangZhiyan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightJour implements Serializable {

	private static final long serialVersionUID = -1584189215994593293L;

	/**
	 * 流水号
	 */
	private String serialNumber;
	/**
	 * 收费站名称
	 */
	private String station;
	/**
	 * 违法类型(固定超重)
	 */
	private final String illegalType = "超重";
	/**
	 * 车辆类型名
	 */
	private String vehicleType;
	/**
	 * 车牌颜色
	 */
	private String color;
	/**
	 * 车牌号码
	 */
	private String license;
	/**
	 * 称重数据(千克)
	 */
	private int weightKg;
	/**
	 * 称重数据(吨)
	 */
	private BigDecimal weightTon;
	/**
	 * 现场图片
	 */
	private List<String> images;
	/**
	 * 备注
	 */
	private final String remark = "自动导入";
	/**
	 * 管理中心名
	 */
	private String manageCenter;
	/**
	 * 发现时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date findTime;
	/**
	 * 是否通知执法部门(默认:否)
	 */
	private final String notify = "否";
	/**
	 * 审核意见(默认:已审核)
	 */
	private final String auditOpinion = "已审核";
	/**
	 * 辅助字段(例:'22年02月蛟川入口管控')
	 */
	private String assistField;
	/**
	 * 日期
	 */
	private int date;
	/**
	 * 时分秒
	 */
	private String hourMinuteSecond;
	/**
	 * 自动导入特有字段
	 */
	private final int importNum = 666;


	public void fillFields(){
		if (StrUtil.isNotBlank(color)) {
			this.color = ColorEnum.getColorValue(Integer.parseInt(color));
		}
		if (StrUtil.isNotBlank(vehicleType)) {
			this.vehicleType = VehicleTypeEnum.getTypeValue(Integer.parseInt(vehicleType));
		}
		this.weightTon = BigDecimal.valueOf(weightKg / 1000).setScale(2, RoundingMode.HALF_UP);
		if (date != 0 && StrUtil.isNotBlank(hourMinuteSecond)) {
			this.findTime = DateUtil.parse(date + hourMinuteSecond,"yyyyMMddHHmmss");
			if (StrUtil.isNotBlank(station)) {
				this.assistField = DateUtil.format(findTime,"yy年MM月" + station + "入口管控自动导入");
			}
		}
	}
}
