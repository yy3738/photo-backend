package com.photo.mvc.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRechargeReq {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    private Integer amount;

    @NotBlank(message = "备注不能为空")
    private String remark;
}
