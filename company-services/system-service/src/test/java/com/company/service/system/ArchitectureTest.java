package com.company.service.system;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 架构守卫：违规即构建失败，不依赖 Code Review 的记忆力。
 * system-service 的分层为 controller → service → repository → mapper。
 */
@AnalyzeClasses(packages = "com.company.service.system", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controller_must_not_touch_persistence = noClasses()
            .that().resideInAPackage("com.company.service.system.controller..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.company.service.system.mapper..",
                    "com.company.service.system.repository..")
            .because("Controller 只能调用 Service，持久化细节不允许上浮");

    @ArchTest
    static final ArchRule service_must_not_touch_persistence = noClasses()
            .that().resideInAPackage("com.company.service.system.service..")
            .should().dependOnClassesThat().resideInAnyPackage("com.company.service.system.mapper..")
            .because("Service 只能依赖 Repository，不得直接调用 Mapper");

    @ArchTest
    static final ArchRule business_must_not_touch_starter_internals = noClasses()
            .that().resideInAPackage("com.company.service.system.service..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.data.redis..",
                    "com.baomidou.mybatisplus..",
                    "redis.clients..")
            .because("中间件客户端只能由 company-*-starter 封装，业务层不得直接依赖");

    @ArchTest
    static final ArchRule no_feign_client_outside_contract = noClasses()
            .that().resideInAPackage("com.company.service.system..")
            .should().beAnnotatedWith("org.springframework.cloud.openfeign.FeignClient")
            .because("Feign 契约只能定义在 company-api 中");
}
