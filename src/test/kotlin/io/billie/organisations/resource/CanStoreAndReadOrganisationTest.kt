package io.billie.organisations.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.util.data.Fixtures
import io.billie.organisations.viewmodel.Entity
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CanStoreAndReadOrganisationTest {

    @LocalServerPort
    private val port = 8080

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var template: JdbcTemplate

    @Test
    fun orgs() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/organisations")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    fun cannotStoreOrgWhenNameIsBlank() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonNameBlank())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenNameIsMissing() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonNoName())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenCountryCodeIsMissing() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonNoCountryCode())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenCountryCodeIsBlank() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonCountryCodeBlank())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenCountryCodeIsNotRecognised() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonCountryCodeIncorrect())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenNoLegalEntityType() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonNoLegalEntityType())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun cannotStoreOrgWhenNoContactDetails() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJsonNoContactDetails())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun canStoreOrg() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations").contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJson())
        )
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andReturn()

        val response = mapper.readValue(result.response.contentAsString, Entity::class.java)

        val org: Map<String, Any> = orgFromDatabase(response.id)
        assertDataMatches(org, Fixtures.bbcFixture(response.id))

        val contactDetailsId: UUID = UUID.fromString(org["contact_details_id"] as String)
        val contactDetails: Map<String, Any> = contactDetailsFromDatabase(contactDetailsId)
        assertDataMatches(contactDetails, Fixtures.bbcContactFixture(contactDetailsId))
    }

    fun assertDataMatches(reply: Map<String, Any>, assertions: Map<String, Any>) {
        for (key in assertions.keys) {
            MatcherAssert.assertThat(reply[key], IsEqual.equalTo(assertions[key]))
        }
    }

    private fun queryEntityFromDatabase(sql: String, id: UUID): MutableMap<String, Any> =
        template.queryForMap(sql, id)

    private fun orgFromDatabase(id: UUID): MutableMap<String, Any> =
        queryEntityFromDatabase("select * from organisations_schema.organisations where id = ?", id)

    private fun contactDetailsFromDatabase(id: UUID): MutableMap<String, Any> =
        queryEntityFromDatabase("select * from organisations_schema.contact_details where id = ?", id)

}