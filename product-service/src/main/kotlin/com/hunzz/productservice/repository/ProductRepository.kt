package com.hunzz.productservice.repository

import com.hunzz.productservice.model.cassandra.Product
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : CassandraRepository<Product, UUID>