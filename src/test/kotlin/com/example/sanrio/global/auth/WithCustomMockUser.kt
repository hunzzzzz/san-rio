package com.example.sanrio.global.auth

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory::class)
annotation class WithCustomMockUser