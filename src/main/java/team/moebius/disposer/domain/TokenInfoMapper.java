package team.moebius.disposer.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team.moebius.disposer.domain.TokenInfo;

@Slf4j
public class TokenInfoMapper {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public TokenInfoMapper() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static String toJson(TokenInfo tokenInfo) throws RuntimeException {
        try {
            return objectMapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static TokenInfo toTokenInfo(String jsonString) throws RuntimeException {
        try {
            return objectMapper.readValue(jsonString, TokenInfo.class);
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}