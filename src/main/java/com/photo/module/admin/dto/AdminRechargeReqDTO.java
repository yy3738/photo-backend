package com.photo.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员充值积分请求
 */
@Data
public class AdminRechargeReqDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    private Integer amount;

    @NotBlank(message = "备注不能为空")
    private String remark;
}
