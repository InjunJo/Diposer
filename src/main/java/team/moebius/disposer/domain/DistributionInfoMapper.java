package team.moebius.disposer.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributionInfoMapper {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public DistributionInfoMapper() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static String toJson(DistributionInfo distributionInfo) throws RuntimeException {
        try {
            return objectMapper.writeValueAsString(distributionInfo);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DistributionInfo toTokenInfo(String jsonString) throws RuntimeException {
        try {
            return objectMapper.readValue(jsonString, DistributionInfo.class);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}