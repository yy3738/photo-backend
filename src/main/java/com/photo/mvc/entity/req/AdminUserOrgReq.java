package com.photo.mvc.entity.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户组织分配请求")
public class AdminUserOrgReq {

    @Schema(description = "组织ID")
    private Long orgId;
}
