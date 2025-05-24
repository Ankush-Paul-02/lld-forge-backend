package com.devmare.lldforge.configuration;

import org.springframework.beans.factory.annotation.Value;

//@EnableCaching
//@Configuration
public class RedisConfiguration {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * This bean defines how we interact with Redis.
     * RedisTemplate is used to perform Redis operations such as set/get.
     */
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        /// Connect to redis
//        template.setConnectionFactory(connectionFactory);
//
//        ///  Serializer that converts objects to JSON and vice versa
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
//
//        ///  Setting serializers for keys and values
//        template.setDefaultSerializer(serializer);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }

//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(30)) /// Cached data will expire after 10 minutes
//                .disableCachingNullValues()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(
//                                new GenericJackson2JsonRedisSerializer()
//                        )
//                );
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(configuration)
//                .build();
//    }

    /**
     * Redisson is a Redis-based library that supports distributed locks and other advanced features.
     * This bean provides a RedissonClient to use things like distributed locks.
     */
//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        String address = "redis://" + redisHost + ":" + redisPort;
//
//        config.useSingleServer()
//                .setAddress(address)
//                .setPassword(redisPassword);
//
//        return Redisson.create(config);
//    }
}
