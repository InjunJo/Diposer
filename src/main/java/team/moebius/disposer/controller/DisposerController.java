package team.moebius.disposer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import team.moebius.disposer.domain.TokenInfo;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.dto.ReqReceive;
import team.moebius.disposer.dto.ReqRetrieval;
import team.moebius.disposer.dto.RespReceive;
import team.moebius.disposer.dto.RespToken;
import team.moebius.disposer.entity.Token;
import team.moebius.disposer.service.TokenCommandService;
import team.moebius.disposer.service.TokenQueryService;
import team.moebius.disposer.util.DateTimeSupporter;

@RestController
@RequiredArgsConstructor
public class DisposerController {

    private final TokenCommandService tokenCommandService;
    private final TokenQueryService tokenQueryService;

    @PostMapping("/distribute")
    public ResponseEntity<RespToken> distribute(@RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqDistribution reqDistribution) {

        long createTime = DateTimeSupporter.getNowUnixTime();

        String tokenKey = tokenCommandService.generateToken(
            userId,
            roomId,
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount(),
            createTime
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new RespToken(tokenKey,createTime));
    }

    @PostMapping("/receive")
    public ResponseEntity<RespReceive> receive(@RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqReceive reqReceive){

        Token token = tokenQueryService.checkIsPresentAndGetToken(roomId, reqReceive.getTokenKey(),
            reqReceive.getCreateTime());

        long shareAmount = tokenCommandService.provideShare(
            userId,
            token,
            DateTimeSupporter.getNowUnixTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new RespReceive(shareAmount));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<TokenInfo> RetrieveDistributionInfo(@RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqRetrieval reqRetrieval){

        TokenInfo tokenInfo = tokenQueryService.provideTokenInfo(
            userId,
            roomId,
            reqRetrieval.getTokenKey(),
            reqRetrieval.getCreateTime(),
            DateTimeSupporter.getNowUnixTime()
        );

        return ResponseEntity.ok(tokenInfo);
    }

}
