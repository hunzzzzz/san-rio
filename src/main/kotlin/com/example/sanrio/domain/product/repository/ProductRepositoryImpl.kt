package com.example.sanrio.domain.product.repository

import com.example.sanrio.domain.product.dto.response.ProductResponse
import com.example.sanrio.domain.product.dto.response.ProductSortCondition
import com.example.sanrio.domain.product.model.CharacterName
import com.example.sanrio.domain.product.model.ProductStatus
import com.example.sanrio.domain.product.model.QProduct
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Description
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ProductRepositoryCustom {
    private val product = QProduct.product

    @Description("상품 목록을 조회하는 메서드")
    override fun getProducts(
        pageable: Pageable,
        status: ProductStatus?,
        characterName: CharacterName?,
        sort: ProductSortCondition?
    ) =
        BooleanBuilder()
            .let {
                status?.let { status -> it.and(product.status.eq(status)) }
                it
            }.let {
                characterName?.let { characterName -> it.and(product.characterName.eq(characterName)) }
                it
            }.let {
                PageImpl(
                    getContents(
                        pageable = pageable,
                        whereClause = it ?: BooleanBuilder(),
                        sort = getOrderCondition(sort = sort, product = product)
                    ),
                    pageable,
                    getTotalElements()
                )
            }

    @Description("페이지에 포함될 상품 데이터를 가져오는 내부 메서드")
    private fun getContents(pageable: Pageable, whereClause: BooleanBuilder, sort: OrderSpecifier<*>) =
        jpaQueryFactory.select(
            Projections.constructor(
                ProductResponse::class.java,
                product.id,
                product.status,
                product.name,
                product.price,
                product.characterName,
                product.createdAt
            )
        ).from(product)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(sort)
            .fetch()

    @Description("전체 상품 개수를 가져오는 내부 메서드")
    private fun getTotalElements() =
        jpaQueryFactory.select(product.count()).from(product).fetchOne() ?: 0L

    @Description("정렬 조건을 판단하는 내부 메서드")
    private fun getOrderCondition(sort: ProductSortCondition?, product: QProduct) =
        when (sort) {
            ProductSortCondition.HIGH_PRICE -> product.price.desc()
            ProductSortCondition.LOW_PRICE -> product.price.asc()
            else -> product.id.desc()
        }
}