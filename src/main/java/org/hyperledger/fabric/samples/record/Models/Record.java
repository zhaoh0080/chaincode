/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.record.Models;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonProperty;

public final class Record {

    private Integer id;

    private String title;

    private Long userId ;

    private Integer linkId;

    private String category;

    private String integraltype;

    private BigDecimal amount;

    private BigDecimal balance;

    private Date createTime;

    private Date modifyTime;

    private Integer settlementRate;

    private Integer status;

    private String mark;

    private Integer pm;

    private Integer succsign;

//    public Record(@JsonProperty("id") Integer id, @JsonProperty("title") String title, @JsonProperty("userId") Long userId,
//                  @JsonProperty("linkId") Integer linkId, @JsonProperty("category") String category, @JsonProperty("integralType") String integralType,
//                  @JsonProperty("amount") BigDecimal amount, @JsonProperty("balance") BigDecimal balance, @JsonProperty("createTime") Date createTime,
//                  @JsonProperty("modifyTime") Date modifyTime, @JsonProperty("settlementRate") Integer settlementRate, @JsonProperty("status") Integer status,
//                  @JsonProperty("mark") String mark, @JsonProperty("pm") Integer pm, @JsonProperty("succSign") Integer succSign) {

    public Record() {
    }

    public Record(Integer id, String title, Long userId, Integer linkId, String category, String integraltype, BigDecimal amount, BigDecimal balance, Date createTime,
                  Date modifyTime, Integer settlementRate, Integer status, String mark, Integer pm, Integer succsign) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.linkId = linkId;
        this.category = category;
        this.integraltype = integraltype;
        this.amount = amount;
        this.balance = balance;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.settlementRate = settlementRate;
        this.status = status;
        this.mark = mark;
        this.pm = pm;
        this.succsign = succsign;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getLinkId() {
        return linkId;
    }

    public String getCategory() {
        return category;
    }

    public String getIntegraltype() {
        return integraltype;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public Integer getSettlementRate() {
        return settlementRate;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMark() {
        return mark;
    }

    public Integer getPm() {
        return pm;
    }

    public Integer getSuccsign() {
        return succsign;
    }

    //    @Override
//    public String toString() {
//        return "id=" + id + ", title=" + title + ", userId=" + userId
//                + ", linkId=" + linkId + ", category=" + category + ", integraltype=" + integraltype
//                + ", amount=" + amount + ", balance=" + balance + ", createTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime)
//                + ", modifyTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(modifyTime) + ", settlementRate=" + settlementRate + ", status=" + status
//                + ", mark=" + mark + ", pm=" + pm + ", succsign=" + succsign;
//
//    }
}
