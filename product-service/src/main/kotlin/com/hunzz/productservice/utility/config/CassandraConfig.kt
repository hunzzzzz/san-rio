package com.hunzz.productservice.utility.config

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress

@Configuration
class CassandraConfig {
    @Value("\${spring.cassandra.contact-points}")
    private lateinit var contactPoints: String

    @Value("\${spring.cassandra.port}")
    private var port: Int = 9042

    @Value("\${spring.cassandra.local-datacenter}")
    private lateinit var localDatacenter: String

    @Value("\${spring.cassandra.keyspace-name}")
    private lateinit var keyspaceName: String

    @Bean
    fun cassandraSession(): CqlSession {
        return CqlSession.builder()
            .addContactPoint(InetSocketAddress.createUnresolved(contactPoints, port))
            .withLocalDatacenter(localDatacenter)
            .build()
            .apply {
                execute("CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};")
                execute("USE $keyspaceName;")
            }
    }
}