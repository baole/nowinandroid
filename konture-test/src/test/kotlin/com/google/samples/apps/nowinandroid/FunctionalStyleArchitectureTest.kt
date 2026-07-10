package com.google.samples.apps.nowinandroid

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class FunctionalStyleArchitectureTest {

    @Test
    fun `use cases must reside in domain package, end with UseCase, and have invoke function`() {
        Konture.scope
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue("UseCases must be located in domain packages and implement functional operator invoke") { cls ->
                val isInDomain = cls.packageName.contains(".domain")
                val hasInvoke = cls.functions.any { it.name == "invoke" }
                isInDomain && hasInvoke
            }
    }

    @Test
    fun `design system classes must belong to the correct corporate package namespace`() {
        // Create scope from core designsystem module to demonstrate scopeFromModule
        Konture.scopeFromModule(":core:designsystem")
            .classes()
            .assertTrue("Classes in :core:designsystem must reside under standard namespace") { cls ->
                cls.packageName.startsWith("com.google.samples.apps.nowinandroid.core.designsystem")
            }
    }

    @Test
    fun `viewmodel naming convention enforcement`() {
        Konture.scope
            .classes()
            .withAnnotationOf("HiltViewModel")
            .assertTrue("Classes annotated with @HiltViewModel must end with ViewModel suffix") { cls ->
                cls.name.endsWith("ViewModel")
            }
    }
}
