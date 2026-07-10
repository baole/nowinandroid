package com.google.samples.apps.nowinandroid

import io.github.baole.konture.*
import org.junit.jupiter.api.Test

class FeatureDecouplingArchitectureTest {

    @Test
    fun `core sub-layers must adhere to a strict unidirectional dependency layout`() {
        Konture.layered {
            // Define sub-layers within core components
            val data = layer("data") definedBy "..core.data.."
            val database = layer("database") definedBy "..core.database.."
            val network = layer("network") definedBy "..core.network.."

            where(database) {
                mayOnlyBeAccessedByLayers(data)
            }
            where(network) {
                mayOnlyBeAccessedByLayers(data)
            }
        }
    }

    @Test
    fun `network layers and database layers must remain strictly isolated from each other`() {
        Konture.classes()
            .that().resideInAPackage("..core.network..")
            .should().onlyDependOnClassesInAnyPackage(
                "..core.network..",
                "..core.model..",
                "..core.common..",
                "kotlin..",
                "java..",
                "kotlinx..",
                "retrofit2..",
                "okhttp3..",
                "dagger..",
                "javax.inject..",
                ""
            )
            .check()

        Konture.classes()
            .that().resideInAPackage("..core.database..")
            .should().onlyDependOnClassesInAnyPackage(
                "..core.database..",
                "..core.model..",
                "..core.common..",
                "kotlin..",
                "java..",
                "kotlinx..",
                "androidx.room..",
                "dagger..",
                "javax.inject.."
            )
            .check()
    }
}
