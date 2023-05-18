package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Grocery;
import edu.ucsb.cs156.example.repositories.GroceryRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = GroceriesController.class)
@Import(TestConfig.class)
public class GroceriesControllerTests extends ControllerTestCase {

        @MockBean
        GroceryRepository groceryRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/groceries/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/groceries/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/groceries/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/groceries?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/groceries/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/groceries/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/groceries/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Grocery grocery = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();

                when(groceryRepository.findById(eq(7L))).thenReturn(Optional.of(grocery));

                // act
                MvcResult response = mockMvc.perform(get("/api/groceries?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(groceryRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(grocery);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(groceryRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/groceries?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(groceryRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Grocery with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_groceries() throws Exception {

                // arrange

                Grocery grocery1 = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();


                Grocery grocery2 = Grocery.builder()
                                .name("Apple")
                                .price("0.50")
                                .expiration("05-18-23")
                                .build();

                ArrayList<Grocery> expectedGroceries = new ArrayList<>();
                expectedGroceries.addAll(Arrays.asList(grocery1, grocery2));

                when(groceryRepository.findAll()).thenReturn(expectedGroceries);

                // act
                MvcResult response = mockMvc.perform(get("/api/groceries/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(groceryRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedGroceries);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_grocery() throws Exception {
                // arrange


                Grocery grocery1 = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();

                when(groceryRepository.save(eq(grocery1))).thenReturn(grocery1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/groceries/post?name=Banana&price=0.29&expiration=05-17-23")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(groceryRepository, times(1)).save(grocery1);
                String expectedJson = mapper.writeValueAsString(grocery1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_grocery() throws Exception {
                // arrange


                Grocery grocery1 = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();

                when(groceryRepository.findById(eq(15L))).thenReturn(Optional.of(grocery1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/groceries?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(groceryRepository, times(1)).findById(15L);
                verify(groceryRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Grocery with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_grocery_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(groceryRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/groceries?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(groceryRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Grocery with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_grocery() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                Grocery groceryOrig = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();

                Grocery groceryEdited = Grocery.builder()
                                .name("Peach")
                                .price("0.28")
                                .expiration("05-19-23")
                                .build();

                String requestBody = mapper.writeValueAsString(groceryEdited);

                when(groceryRepository.findById(eq(67L))).thenReturn(Optional.of(groceryOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/groceries?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(groceryRepository, times(1)).findById(67L);
                verify(groceryRepository, times(1)).save(groceryEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_grocery_that_does_not_exist() throws Exception {
                // arrange


                Grocery groceryEdited = Grocery.builder()
                                .name("Banana")
                                .price("0.29")
                                .expiration("05-17-23")
                                .build();

                String requestBody = mapper.writeValueAsString(groceryEdited);

                when(groceryRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/groceries?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(groceryRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Grocery with id 67 not found", json.get("message"));

        }
}
