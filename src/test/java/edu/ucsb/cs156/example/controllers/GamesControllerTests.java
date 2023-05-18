package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Game;
import edu.ucsb.cs156.example.repositories.GameRepository;

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

@WebMvcTest(controllers = GamesController.class)
@Import(TestConfig.class)
public class GamesControllerTests extends ControllerTestCase {

        @MockBean
        GameRepository gameRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsbdiningcommons/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/games/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/games/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/games?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdiningcommons/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/games/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/games/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Game game = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();

                when(gameRepository.findById(eq(7L))).thenReturn(Optional.of(game));

                // act
                MvcResult response = mockMvc.perform(get("/api/games?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(gameRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(game);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(gameRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/games?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(gameRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Game with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_games() throws Exception {

                // arrange

                Game game1 = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();


                Game game2 = Game.builder()
                                .name("League of Legends")
                                .description("Play as 5 and destroy the enemy nexus")
                                .genre("moba")
                                .build();

                ArrayList<Game> expectedGames = new ArrayList<>();
                expectedGames.addAll(Arrays.asList(game1, game2));

                when(gameRepository.findAll()).thenReturn(expectedGames);

                // act
                MvcResult response = mockMvc.perform(get("/api/games/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(gameRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedGames);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                Game game1 = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();

                when(gameRepository.save(eq(game1))).thenReturn(game1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/games/post?name=the Legend of Zelda&description=Play as link and save the princess&genre=open world")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(gameRepository, times(1)).save(game1);
                String expectedJson = mapper.writeValueAsString(game1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Game game1 = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();

                when(gameRepository.findById(eq(15L))).thenReturn(Optional.of(game1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/games?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(gameRepository, times(1)).findById(15L);
                verify(gameRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Game with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(gameRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/games?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(gameRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Game with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                Game gameOG = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();


                Game gameEdited = Game.builder()
                                .name("League of Legends")
                                .description("Play as 5 and destroy the enemy nexus")
                                .genre("moba")
                                .build();

                String requestBody = mapper.writeValueAsString(gameEdited);

                when(gameRepository.findById(eq(67L))).thenReturn(Optional.of(gameOG));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/games?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(gameRepository, times(1)).findById(67L);
                verify(gameRepository, times(1)).save(gameEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                Game gameEdited = Game.builder()
                                .name("the Legend of Zelda")
                                .description("Play as link and save the princess")
                                .genre("open world")
                                .build();

                String requestBody = mapper.writeValueAsString(gameEdited);

                when(gameRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/games?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(gameRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Game with id 67 not found", json.get("message"));

        }
}
