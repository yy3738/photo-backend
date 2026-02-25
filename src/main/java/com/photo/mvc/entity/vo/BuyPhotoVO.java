package com.photo.mvc.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "购买作品结果")
public class BuyPhotoVO {

    @Schema(description = "订单ID")
    private String orderId;
    @Schema(description = "消费积分")
    private Integer pointsSpent;
    @Schema(description = "剩余积分")
    private Integer remainingPoints;
}
