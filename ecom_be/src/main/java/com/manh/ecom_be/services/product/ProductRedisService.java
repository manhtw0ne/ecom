package com.manh.ecom_be.services.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manh.ecom_be.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRedisService implements InterfaceProductRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper redisObjectMapper;

    @Value("${spring.data.redis.use-redis-cache}")
    private boolean useRedisCache;

    private String getKeyFrom(String keyword, Long categoryId, PageRequest pageRequest) {
        String sortDir = pageRequest.getSort().getOrderFor("id")
                .getDirection() == Sort.Direction.ASC ? "asc" : "desc";
        return String.format("all_products_%s_%d_%d_%d_%s", keyword, categoryId,
                pageRequest.getPageNumber(), pageRequest.getPageSize(), sortDir);
    }

    @Override
    public List<ProductResponse> getAllProducts(String keyword, Long categoryId,
                                                PageRequest pageRequest)
        throws JsonProcessingException {
        if (!useRedisCache) return null;

        String key = getKeyFrom(keyword, categoryId, pageRequest);
        String json = (String) redisTemplate.opsForValue().get(key);
        return json != null
                ? redisObjectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {})
                    : null;
    }

    @Override
    public void saveAllProducts(List<ProductResponse> products, String keyword, Long categoryId, PageRequest pageRequest)
        throws JsonProcessingException {
        String key = getKeyFrom(keyword, categoryId, pageRequest);
        String json = redisObjectMapper.writeValueAsString(products);
        redisTemplate.opsForValue().set(key, json);
    }

    @Override
    public void clear() {
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();}
}


