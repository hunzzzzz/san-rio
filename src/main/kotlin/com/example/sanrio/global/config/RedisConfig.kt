package com.example.sanrio.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int
) {
    @Description("Redis 연결을 위한 Connection 생성")
    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port)

    @Description("Redis 데이터 처리를 위한 템플릿 생성")
    @Bean
    fun redisTemplate() =
        RedisTemplate<String, Any>().let {
            // Redis 연결
            it.connectionFactory = redisConnectionFactory()

            // Key-Value 형태로 직렬화 수행
            it.keySerializer = StringRedisSerializer()
            it.valueSerializer = StringRedisSerializer()

            // Hash Key-Value 형태로 직렬화 수행
            it.hashKeySerializer = StringRedisSerializer()
            it.hashValueSerializer = StringRedisSerializer()

            // 기본적으로 직렬화를 수행
            it.setDefaultSerializer(StringRedisSerializer())

            it
        }
}