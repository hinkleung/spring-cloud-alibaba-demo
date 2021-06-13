package com.itmuch.usercenter.domain.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRespDTO {

    private Integer id;

    private String avatarUrl;

    private Integer bonus;

    private String wxNickname;
}
