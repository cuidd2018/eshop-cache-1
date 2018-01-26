package com.zxb.eshop.inventory.model;

/**
 * 库存model类
 * Created by xuery on 2018/1/23.
 */
public class ProductInventory {

    private Integer productId; //产品id

    private int inventoryCnt; //库存数

    public ProductInventory() {
    }

    public ProductInventory(Integer productId, int inventoryCnt) {
        this.productId = productId;
        this.inventoryCnt = inventoryCnt;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getInventoryCnt() {
        return inventoryCnt;
    }

    public void setInventoryCnt(int inventoryCnt) {
        this.inventoryCnt = inventoryCnt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"productId\":\"")
                .append(productId).append('\"');
        sb.append(",\"inventoryCnt\":")
                .append(inventoryCnt);
        sb.append('}');
        return sb.toString();
    }
}
