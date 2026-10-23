package com.company.service.auth;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.company.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controller_must_not_touch_persistence = noClasses()
            .that().resideInAPackage("com.company.service.auth.controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..mapper..", "..repository..")
            .because("Controller 只能调用 Service");

    @ArchTest
    static final ArchRule business_must_not_touch_starter_internals = noClasses()
            .that().resideInAPackage("com.company.service.auth.service..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.data.redis..",
                    "com.baomidou.mybatisplus..",
                    "redis.clients..")
            .because("中间件客户端只能由 company-*-starter 封装，业务层不得直接依赖");

    @ArchTest
    static final ArchRule no_feign_client_outside_contract = noClasses()
            .that().resideInAPackage("com.company.service.auth..")
            .should().beAnnotatedWith("org.springframework.cloud.openfeign.FeignClient")
            .because("Feign 契约只能定义在 company-api 中");
}
