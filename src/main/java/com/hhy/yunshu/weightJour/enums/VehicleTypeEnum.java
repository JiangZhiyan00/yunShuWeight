package com.hhy.yunshu.weightJour.enums;

public enum VehicleTypeEnum {

    // 入口车型(11-一型货车,12-二型货车,13-三型货车,14-四型货车,15-五型货车,16-六型货车)
    TRUCK_0(0,"货车"),
    TRUCK_1(11,"一型货车"),
    TRUCK_2(12,"二型货车"),
    TRUCK_3(13,"三型货车"),
    TRUCK_4(14,"四型货车"),
    TRUCK_5(15,"五型货车"),
    TRUCK_6(16,"六型货车");

    private int key;
    private String value;

    VehicleTypeEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getTypeValue(int key) {
        for (VehicleTypeEnum m : VehicleTypeEnum.values()) {
            if (m.getKey() == key) {
                return m.getValue();
            }
        }
        return "未确定";
    }

    @Override
    public String toString() {
        return "VehicleTypeEnum{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}
