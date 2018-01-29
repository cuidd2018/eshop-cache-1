package com.zxb.eshop.inventory.model;

/**
 * Created by xuery on 2018/1/26.
 */
public class ShopInfo {

    private Long id;
    private String name;
    private Integer level;
    private Double goodCommentRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Double getGoodCommentRate() {
        return goodCommentRate;
    }

    public void setGoodCommentRate(Double goodCommentRate) {
        this.goodCommentRate = goodCommentRate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":")
                .append(id);
        sb.append(",\"name\":\"")
                .append(name).append('\"');
        sb.append(",\"level\":")
                .append(level);
        sb.append(",\"goodCommentRate\":")
                .append(goodCommentRate);
        sb.append('}');
        return sb.toString();
    }
}
