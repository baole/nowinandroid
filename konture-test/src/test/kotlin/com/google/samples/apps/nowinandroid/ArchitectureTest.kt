package com.google.samples.apps.nowinandroid

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class ArchitectureTest {

    @Test
    fun `no circular dependencies allowed in the entire module graph`() {
        Konture.assertNoCycles()
    }

    @Test
    fun `feature modules must remain completely decoupled`() {
        Konture.modules()
            .that().haveNameMatching(":feature:**")
            .should().mustNotDependOn(":app")
            .andShould().onlyDependOn(":core:**", ":feature:**:api", ":ui-test-hilt-manifest")
            .check()
    }

    @Test
    fun `features must not bypass repositories to access databases or network directly`() {
        Konture.modules()
            .that().haveNameMatching(":feature:**")
            .should().mustNotDependOn(":core:database")
            .andShould().mustNotDependOn(":core:network")
            .check()
    }

    @Test
    fun `core model must remain a pure leaf dependency`() {
        Konture.modules()
            .that().haveNamePath(":core:model")
            .should().onlyDependOn()
            .check()
    }

    @Test
    fun `repositories must be declared as interfaces`() {
        Konture.classes()
            .that().inPackage("..data.repository..")
            .and().nameEndsWith("Repository")
            .and().named { !it.startsWith("OfflineFirst") && !it.startsWith("Default") && !it.startsWith("Composite") }
            .should().beInterfaces()
            .check()
    }

    @Test
    fun `composables must not accept repository or database parameters`() {
        Konture.functions()
            .that { hasAnnotation("Composable") && modulePath.startsWith(":feature:") }
            .should {
                noneParameterMatches("accepts a repository parameter directly") { param ->
                    param.type.endsWith("Repository") || param.type.endsWith("Database")
                }
            }
            .check()
    }

    @Test
    fun `feature API modules must not depend on any feature implementation module`() {
        Konture.modules()
            .that().haveNameMatching(":feature:**:api")
            .should().mustNotDependOn(":feature:**:impl")
            .check()
    }

    @Test
    fun `feature implementation modules must not depend on other feature implementation modules`() {
        Konture.modules()
            .that().haveNameMatching(":feature:**:impl")
            .should {
                val currentFeature = path.removePrefix(":feature:").substringBefore(":")
                for (dep in dependencies) {
                    if (dep.configuration.startsWith("test") || dep.configuration.startsWith("androidTest")) continue
                    if (dep.targetPath.startsWith(":feature:") && dep.targetPath.endsWith(":impl")) {
                        val targetFeature = dep.targetPath.removePrefix(":feature:").substringBefore(":")
                        if (targetFeature != currentFeature) {
                            addViolation("Feature implementation module $path should not depend on other feature implementation module: ${dep.targetPath}")
                        }
                    }
                }
            }
            .check()
    }

    @Test
    fun `viewmodels must be annotated with HiltViewModel`() {
        Konture.classes()
            .that().nameEndsWith("ViewModel")
            .and().named { !it.contains("Test") && it != "ViewModel" }
            .should().annotatedWith("HiltViewModel")
            .check()
    }

    @Test
    fun `viewmodels must not depend on android framework or context`() {
        Konture.classes()
            .that().nameEndsWith("ViewModel")
            .and().named { !it.contains("Test") && it != "ViewModel" }
            .should {
                val forbiddenImports = listOf("android.content.Context", "android.view.View", "android.app.Activity")
                for (imp in imports) {
                    if (forbiddenImports.any { imp.startsWith(it) }) {
                        addViolation("ViewModel $fqName should not reference Android framework class: $imp")
                    }
                }
            }
            .check()
    }

    @Test
    fun `use cases must reside in domain package and implement invoke operator`() {
        Konture.classes()
            .that().nameEndsWith("UseCase")
            .and().named { !it.contains("Test") }
            .should {
                if (!packageName.contains(".domain")) {
                    addViolation("UseCase $fqName should reside in a domain package, but resides in '$packageName'")
                }
                val hasInvoke = functions.any { it.name == "invoke" }
                if (!hasInvoke) {
                    addViolation("UseCase $fqName must define an 'invoke' operator function")
                }
            }
            .check()
    }
}
