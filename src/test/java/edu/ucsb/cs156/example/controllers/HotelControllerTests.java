package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Hotel;
import edu.ucsb.cs156.example.repositories.HotelRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = HotelController.class)
@Import(TestConfig.class)
public class HotelControllerTests extends ControllerTestCase {
    @MockBean
    HotelRepository hotelRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/hotels/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/hotels/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/hotels/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/hotels?id=7"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/hotels/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/hotels/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/hotels/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    // // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange
            Hotel hotel = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            when(hotelRepository.findById(eq(7L))).thenReturn(Optional.of(hotel));

            // act
            MvcResult response = mockMvc.perform(get("/api/hotels?id=7"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(hotelRepository, times(1)).findById(eq(7L));
            String expectedJson = mapper.writeValueAsString(hotel);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(hotelRepository.findById(eq(7L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/hotels?id=7"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(hotelRepository, times(1)).findById(eq(7L));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("Hotel with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_hotels() throws Exception {

            // arrange
            Hotel hotel1 = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            Hotel hotel2 = Hotel.builder()
                            .name("The Ritz-Carlton")
                            .address("8301 Hollister Ave")
                            .description("Overlooking the Pacific Ocean")
                            .build();

            ArrayList<Hotel> expectedHotels = new ArrayList<>();
            expectedHotels.addAll(Arrays.asList(hotel1, hotel2));

            when(hotelRepository.findAll()).thenReturn(expectedHotels);

            // act
            MvcResult response = mockMvc.perform(get("/api/hotels/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(hotelRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedHotels);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_hotel() throws Exception {
            // arrange

            Hotel hotel1 = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            when(hotelRepository.save(eq(hotel1))).thenReturn(hotel1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/hotels/post?name=The Leta&address=5650 Calle Real&description=A chic stay in easygoing Goleta")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(hotelRepository, times(1)).save(hotel1);
            String expectedJson = mapper.writeValueAsString(hotel1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_hotel() throws Exception {
            // arrange

            Hotel hotel1 = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            when(hotelRepository.findById(eq(15L))).thenReturn(Optional.of(hotel1));

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/hotels?id=15")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(hotelRepository, times(1)).findById(15L);
            verify(hotelRepository, times(1)).delete(any());

            Map<String, Object> json = responseToJson(response);
            assertEquals("Hotel with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_hotel_and_gets_right_error_message()
                    throws Exception {
            // arrange

            when(hotelRepository.findById(eq(15L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            delete("/api/hotels?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(hotelRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Hotel with id 15 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_hotel() throws Exception {
            // arrange

            Hotel hotelOrig = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            Hotel hotelEdited = Hotel.builder()
                            .name("The Ritz-Carlton")
                            .address("8301 Hollister Ave")
                            .description("Overlooking the Pacific Ocean")
                            .build();

            String requestBody = mapper.writeValueAsString(hotelEdited);

            when(hotelRepository.findById(eq(67L))).thenReturn(Optional.of(hotelOrig));

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/hotels?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(hotelRepository, times(1)).findById(67L);
            verify(hotelRepository, times(1)).save(hotelEdited); // should be saved with correct user
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_hotel_that_does_not_exist() throws Exception {
            // arrange

            Hotel hotelEdited = Hotel.builder()
                            .name("The Leta")
                            .address("5650 Calle Real")
                            .description("A chic stay in easygoing Goleta")
                            .build();

            String requestBody = mapper.writeValueAsString(hotelEdited);

            when(hotelRepository.findById(eq(67L))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(
                            put("/api/hotels?id=67")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            // assert
            verify(hotelRepository, times(1)).findById(67L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("Hotel with id 67 not found", json.get("message"));

    }
}