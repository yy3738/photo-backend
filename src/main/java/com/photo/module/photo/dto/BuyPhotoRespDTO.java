package com.photo.module.photo.dto;

import lombok.Data;

/**
 * 购买作品响应
 */
@Data
public class BuyPhotoRespDTO {

    private String orderId;
    private Integer pointsSpent;
    private Integer remainingPoints;
}
