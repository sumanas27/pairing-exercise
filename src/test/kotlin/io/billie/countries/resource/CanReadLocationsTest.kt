package io.billie.countries.resource

import io.billie.util.matcher.IsUUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CanReadLocationsTest {

    @LocalServerPort
    private val port = 8080

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun notFoundForUnknownCountry() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/countries/xx/cities")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun canViewZWCities() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/countries/zw/cities")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Harare"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].country_code").value("ZW"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[25].name").value("Mazoe"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[25].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[25].country_code").value("ZW"))
    }

    @Test
    fun canViewBECities() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/countries/be/cities")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Brussels"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].country_code").value("BE"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(468))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[467].name").value("Alveringem"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[467].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[467].country_code").value("BE"))
    }

    @Test
    fun canViewCountries() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/countries")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Andorra"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].country_code").value("AD"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[239].name").value("Zimbabwe"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[239].id").value(IsUUID.isUuid()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[239].country_code").value("ZW"))
    }

}