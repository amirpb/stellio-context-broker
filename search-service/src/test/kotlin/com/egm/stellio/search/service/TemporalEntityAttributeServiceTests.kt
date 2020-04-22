package com.egm.stellio.search.service

import com.egm.stellio.shared.util.loadAndParseSampleData
import com.egm.stellio.shared.util.loadSampleData
import com.github.jsonldjava.core.JsonLdOptions
import com.github.jsonldjava.core.JsonLdProcessor
import com.github.jsonldjava.utils.JsonUtils
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import java.time.OffsetDateTime

@SpringBootTest
@ActiveProfiles("test")
@Import(R2DBCConfiguration::class)
class TemporalEntityAttributeServiceTests {

    @Autowired
    private lateinit var temporalEntityAttributeService: TemporalEntityAttributeService

    init {
        Flyway.configure()
            .dataSource(MyPostgresqlContainer.instance.jdbcUrl, MyPostgresqlContainer.DB_USER, MyPostgresqlContainer.DB_PASSWORD)
            .load()
            .migrate()
    }

    @Test
    fun `it should create one entry for an entity with one temporal property`() {
        val rawEntity = loadSampleData()

        val temporalReferencesResults = temporalEntityAttributeService.createEntityTemporalReferences(rawEntity)

        StepVerifier.create(temporalReferencesResults)
            .expectNextMatches {
                it == 1
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `it should create two entries for an entity with two temporal properties`() {
        val rawEntity = loadSampleData("beehive_two_temporal_properties.jsonld")

        val temporalReferencesResults = temporalEntityAttributeService.createEntityTemporalReferences(rawEntity)

        StepVerifier.create(temporalReferencesResults)
            .expectNextMatches {
                it == 2
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `it should inject temporal string values into an entity`() {
        val rawEntity = loadAndParseSampleData("subscription.jsonld")
        val rawResults = listOf(
            listOf(
                mapOf(
                    "attribute_name" to "notification",
                    "value" to "urn:ngsi-ld:Beehive:1234",
                    "observed_at" to OffsetDateTime.parse("2020-03-25T08:29:17.965206+01:00")
                ),
                mapOf(
                    "attribute_name" to "notification",
                    "value" to "urn:ngsi-ld:Beehive:5678",
                    "observed_at" to OffsetDateTime.parse("2020-03-25T08:33:17.965206+01:00")
                )
            )
        )

        val enrichedEntity = temporalEntityAttributeService.injectTemporalValues(rawEntity, rawResults)
        val serializedEntity = JsonLdProcessor.compact(enrichedEntity.first, mapOf("@context" to enrichedEntity.second), JsonLdOptions())
        val finalEntity = JsonUtils.toPrettyString(serializedEntity)
        assertEquals(loadSampleData("expectations/subscription_with_notifications.jsonld").trim(), finalEntity)
    }
}