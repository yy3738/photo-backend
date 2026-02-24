package com.photo.mvc.entity.vo;

import lombok.Data;

@Data
public class BuyPhotoVO {

    private String orderId;
    private Integer pointsSpent;
    private Integer remainingPoints;
}
