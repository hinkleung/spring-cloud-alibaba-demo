package com.itmuch.contentcenter.domain.dto.content;

import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import lombok.Data;

@Data
public class ShareAuditDTO {

    private AuditStatusEnum auditStatusEnum;

    private String reason;

}
