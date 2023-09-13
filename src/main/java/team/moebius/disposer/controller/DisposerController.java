package team.moebius.disposer.controller;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import team.moebius.disposer.domain.DistributionInfo;
import team.moebius.disposer.dto.DistributionTokenDto;
import team.moebius.disposer.dto.ReqDistribution;
import team.moebius.disposer.dto.ReqReceive;
import team.moebius.disposer.dto.ReqRetrieval;
import team.moebius.disposer.dto.RespReceive;
import team.moebius.disposer.dto.RespToken;
import team.moebius.disposer.entity.RecipientResult;
import team.moebius.disposer.service.RecipientCommandService;
import team.moebius.disposer.service.DistributionCommandService;
import team.moebius.disposer.service.DistributionQueryService;
import team.moebius.disposer.service.RecipientQueryService;
import team.moebius.disposer.util.DateTimeSupporter;

@RestController
@RequiredArgsConstructor
public class DisposerController {

    private final DistributionCommandService distributionCommandService;
    private final DistributionQueryService distributionQueryService;
    private final RecipientCommandService recipientCommandService;
    private final RecipientQueryService recipientQueryService;
    private static final long CHECK_INTERVAL = 10 * 60 * 1000;

    @PostMapping("/distribute")
    public ResponseEntity<RespToken> distribute(@RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqDistribution reqDistribution) {

        long createTime = DateTimeSupporter.getNowUnixTime();

        DistributionTokenDto distributionToken = distributionCommandService.distribute(
            userId,
            roomId,
            reqDistribution.getAmount(),
            reqDistribution.getRecipientCount(),
            createTime
        );

        recipientCommandService.generateRecipients(distributionToken, reqDistribution.getAmount(),
            reqDistribution.getRecipientCount());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RespToken(distributionToken.getTokenKey(), createTime));
    }

    @PostMapping("/receive")
    public ResponseEntity<RespReceive> receive(@RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqReceive reqReceive) {

        DistributionTokenDto distributionTokenDto = distributionQueryService.getDistributionToken(
            roomId,
            reqReceive.getTokenKey(),
            reqReceive.getCreateTime()
        );

        long shareAmount = recipientCommandService.provideShare(
            userId,
            distributionTokenDto,
            DateTimeSupporter.getNowUnixTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new RespReceive(shareAmount));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<DistributionInfo> RetrieveDistributionInfo(
        @RequestHeader("X-USER-ID") Long userId,
        @RequestHeader("X-ROOM-ID") String roomId, @RequestBody ReqRetrieval reqRetrieval) {

        long readRequestTime = DateTimeSupporter.getNowUnixTime();

        DistributionTokenDto distributionTokenDto = distributionQueryService.getDistributionToken(
            roomId,
            reqRetrieval.getTokenKey(),
            reqRetrieval.getCreateTime()
        );

        recipientQueryService.checkValidRequest(userId, distributionTokenDto,readRequestTime);

        DistributionInfo distributionInfo =
            recipientQueryService.getDistributionInfo(distributionTokenDto,readRequestTime);

        return ResponseEntity.ok(distributionInfo);
    }
    @Scheduled(fixedRate = CHECK_INTERVAL)
    void savePreComputedResult(){

        long nowUnixTime = DateTimeSupporter.getNowUnixTime();

        Set<DistributionTokenDto> expiredDistributionTokens =
            distributionQueryService.findExpiredDistributionTokens(nowUnixTime);

        List<RecipientResult> recipientResults =
            recipientQueryService.mapTokensToRecipientResults(expiredDistributionTokens);

        recipientCommandService.savePreComputedResult(recipientResults);
    }

}
