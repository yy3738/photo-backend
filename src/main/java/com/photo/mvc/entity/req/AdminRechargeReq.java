package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员积分充值请求")
public class AdminRechargeReq {

    @Schema(description = "目标用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "充值金额（元），1元=10积分")
    @NotNull(message = "充值金额不能为空")
    private Integer amount;

    @Schema(description = "充值备注")
    @NotBlank(message = "备注不能为空")
    private String remark;
}
