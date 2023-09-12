package team.moebius.disposer.dto;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class RespToken {

    private String tokenKey;

    private String createTime;

    public RespToken() {
    }

    public RespToken(String tokenKey, Long createTime) {
        this.tokenKey = tokenKey;
        this.createTime = String.valueOf(createTime);
    }
}
