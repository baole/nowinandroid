package com.google.samples.apps.nowinandroid

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class DeclarationSyntacticArchitectureTest {

    @Test
    fun `dagger injected fields must not have private visibility`() {
        // Hilt/Dagger cannot inject into private fields.
        Konture.properties()
            .that().haveAnnotationOf("Inject")
            .should().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL, Visibility.PROTECTED)
            .check()
    }

    @Test
    fun `repository interface functions must be asynchronous or reactive`() {
        // Enforces asynchronous practices: repository functions must either be suspending functions or return a Flow
        Konture.functions()
            .that { modulePath.startsWith(":core:data") && className?.endsWith("Repository") == true }
            .should {
                val isSuspend = Modifier.SUSPEND in modifiers
                val returnsFlow = returnType.endsWith("Flow") || returnType.startsWith("Flow<") || returnType.contains(".Flow")
                if (!isSuspend && !returnsFlow) {
                    addViolation("Repository function $name in $className must be a suspend function or return a Flow/StateFlow, but was synchronous.")
                }
            }
            .check()
    }

    @Test
    fun `jetpack compose preview functions should remain non-public`() {
        // Enforce preview composables to remain private or internal, preventing them from bloating public module APIs.
        Konture.functions()
            .that {
                hasAnnotation("Preview") &&
                !declaration.name.startsWith("ViewTogglePreview") &&
                !declaration.name.startsWith("TopicDetailPlaceholderPreview")
            }
            .should().haveAnyVisibility(Visibility.PRIVATE, Visibility.INTERNAL)
            .check()
    }
}
