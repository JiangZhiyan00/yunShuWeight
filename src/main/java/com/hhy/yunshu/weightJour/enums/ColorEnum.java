package com.hhy.yunshu.weightJour.enums;

public enum ColorEnum {

    //车牌颜色(0-蓝，1-黄，2-黑，3-白，4-渐变绿，5-黄绿双拼，6-蓝白渐变，9-未确定)
    BLUE(0,"蓝色"),
    YELLOW(1,"黄色"),
    BLACK(2,"黑色"),
    WHITE(3,"白色"),
    GREEN(4,"渐变绿色"),
    YELLOW_GREEN(5,"黄绿双拼色"),
    BLUE_WHITE(6,"蓝白渐变色"),
    UNKNOWN(9,"未确定");

    private int key;
    private String value;

    ColorEnum(int key, String value) {
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

    public static String getColorValue(int key) {
        for (ColorEnum m : ColorEnum.values()) {
            if (m.getKey() == key) {
                return m.getValue();
            }
        }
        return "未确定";
    }

    @Override
    public String toString() {
        return "ColorEnum{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}
