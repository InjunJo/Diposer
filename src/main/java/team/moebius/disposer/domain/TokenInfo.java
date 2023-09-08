package team.moebius.disposer.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter @Builder
@ToString
public class TokenInfo {

    private final LocalDateTime distributeTime;

    private final long distributeAmount;

    private final long receiveTotalAmount;

    private final List<ReceiveInfo> receiveInfoList;


}
