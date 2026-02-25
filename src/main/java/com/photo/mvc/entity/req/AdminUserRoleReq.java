package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户角色分配请求")
public class AdminUserRoleReq {

    @Schema(description = "角色ID列表")
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
