package com.hhy.yunshu.weightJour.handler;

import cn.hutool.core.date.DateUtil;
import com.hhy.yunshu.base.api.IBaseHandler;
import com.hhy.yunshu.utils.ApiUtils;
import com.hhy.yunshu.utils.AutoIncrementNoUtils;
import com.hhy.yunshu.weightJour.entity.WeightJour;
import com.hhy.yunshu.weightJour.mapper.IWeightJourMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("unchecked")
public class AllWeightJourHandler implements IBaseHandler {

    private static final String SCHEMA_CODE = "incontrolrecord";

    /**
     * redis的key前缀
     */
    private static final String YUNSHU_OVERWEIGHT_PREFIX = "YUNSHU:OVERWEIGHT:ALL:";

    @Autowired
    private IWeightJourMapper weightJourMapper;

    @Autowired
    private AutoIncrementNoUtils autoIncrementNoUtils;

    @Override
    @XxlJob(value = "allWeightJourHandler")
    public void doHandle() {
        try {
            // 1.先查库
            List<WeightJour> weightJours = weightJourMapper.queryAllOverWeightList();
            XxlJobHelper.log("数据库数据共:"+weightJours.size()+"条");
            // 数据库数据去重
            weightJours = weightJours.parallelStream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(o -> o.getManageCenter() + o.getStation() + o.getLicense() + o.getDate()))), ArrayList::new));
            XxlJobHelper.log("去重后的数据库数据共:"+weightJours.size()+"条");

            // 2.获取云枢表单近15天已推送数据
            ApiUtils api = new ApiUtils(SCHEMA_CODE);
            List<Map<String, Object>> formData = api.getFormData(this.getFilters(), 0, Integer.MAX_VALUE);
            XxlJobHelper.log("近7天的云枢数据共:" + formData.size() + "条");

            // api.deleteData(formData.parallelStream().map(m -> (String) ((Map) m.get("data")).get("id")).toArray(String[]::new));
            // 3.与云枢原有数据比较并插入云枢表单
            long createNum = 0L;
            for (WeightJour weightJour : weightJours) {
                Optional<Map<String, Object>> any = formData.parallelStream().filter(fd -> {
                    Map<String,Object> data = (Map<String, Object>) fd.get("data");
                    return Objects.equals(data.get("Dropdown1660206610287").toString(), weightJour.getStation())
                            && Objects.equals(data.get("ShortText1660206831121").toString(), weightJour.getManageCenter())
                            && Objects.equals(data.get("ShortText1660206630982").toString(), weightJour.getLicense())
                            && Objects.equals(DateUtil.format(DateUtil.parse(data.get("Date1660206606203").toString(),"yyyy-MM-dd"),"yyyyMMdd"), String.valueOf(weightJour.getDate()));
                }).findAny();
                if (!any.isPresent()) {
                    weightJour.fillFields();
                    weightJour.setSerialNumber(autoIncrementNoUtils.getAutoIncrementNo(YUNSHU_OVERWEIGHT_PREFIX + weightJour.getAssistField(),weightJour.getAssistField(),4,DateUtil.nextMonth()));
                    // XxlJobHelper.log("新增的数据:" + weightJour);
                    api.createData(objToYunShuData(weightJour));
                    createNum++;
                }
            }
            XxlJobHelper.log("共创建:" + createNum + "条超重车数据");
        }catch (Exception e) {
            XxlJobHelper.log("发生异常:" + e.getMessage());
        }
    }

    /**
     * 将对象转为云枢要求的格式的Map
     * @param weightJour 超重车对象
     * @return 云枢要求的格式的Map
     */
    private Map<String, Object> objToYunShuData(WeightJour weightJour) {
        return new HashMap<String,Object>(17) {
            {
                put("id",null);//id
                put("Radio1660206658018",weightJour.getNotify());//是否通知执法部门
                put("ShortText1660206700603",weightJour.getAuditOpinion());//审核意见
                put("Dropdown1660206625424",weightJour.getIllegalType());//违法类型
                put("Dropdown1660206616475",weightJour.getVehicleType());//客货车型
                put("Date1660206606203",weightJour.getFindTime());//发现时间
                put("Dropdown1660206610287",weightJour.getStation());//收费站
                put("Dropdown1660206620147",weightJour.getColor());//车牌颜色
                put("ShortText1660206818078",weightJour.getAssistField());//辅助字段
                put("LongText1660206685833",weightJour.getRemark());//备注
                put("ShortText1660206598080",weightJour.getSerialNumber());//流水号
                put("Number1660206852768",weightJour.getImportNum());//自动导入特有字段
                put("version",0);//版本号
                // put("DeptSingle1660206821779",weightJour.getStation());//收费站
                put("ShortText1660206843833",weightJour.getDate());//称重日期
                // put("DeptSingle1660206826072",weightJour.getManageCenter());//管理中心
                put("ShortText1660206630982",weightJour.getLicense());//车牌号码
                put("ShortText1660206831121",weightJour.getManageCenter());//管理中心文本
                put("Number1660206635250",weightJour.getWeightTon());//称重数据(吨)
            }
        };
    }

    /**
     * 筛选云枢近15天的
     */
    private List<Map<String, Object>> getFilters() {
        List<Map<String,Object>> filters = new ArrayList<>(2);
        Map<String,Object> filterMap1 = new HashMap<String,Object>(5) {
            {
                put("op","Eq");
                put("propertyCode","Number1660206852768");
                put("propertyType",0);
                put("propertyValue",666);
                put("propertyValueName","");
            }
        };
        // 近15天的
        String propertyValue = DateUtil.format(DateUtil.offsetDay(new Date(),-7),"yyyy-MM-dd")
                + ";" + DateUtil.today();
        Map<String,Object> filterMap2 = new HashMap<String,Object>(4) {
            {
                put("propertyCode","Date1660206606203");
                put("propertyType",3);
                put("propertyValue",propertyValue);
                put("propertyValueName","");
            }
        };
        filters.add(filterMap1);
        filters.add(filterMap2);
        return filters;
    }

/*    public static void main(String[] args) {
        ApiUtils api = new ApiUtils(SCHEMA_CODE);
        while (true) {
            List<Map<String, Object>> formData = api.getFormData(Collections.emptyList(), 0, 5000);
            if (!formData.isEmpty()) {
                String[] ids = formData.parallelStream()
                        .map(m -> (String) ((Map) m.get("data")).get("id")).toArray(String[]::new);
                Map<String, Object> stringObjectMap = api.deleteData(ids);
                System.out.println(stringObjectMap);
            }else {
                break;
            }
        }
    }*/
}
