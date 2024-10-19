package com.ajustadoati.sc.adapter.rest;

import com.ajustadoati.sc.adapter.rest.assemblers.UserModelAssembler;
import com.ajustadoati.sc.adapter.rest.dto.request.CreateUserRequest;
import com.ajustadoati.sc.application.mapper.UserMapper;
import com.ajustadoati.sc.application.mapper.UserMapperImpl;
import com.ajustadoati.sc.application.service.CustomUserDetailsService;
import com.ajustadoati.sc.application.service.JwtTokenProvider;
import com.ajustadoati.sc.application.service.UserService;
import com.ajustadoati.sc.config.TestSecurityConfig;
import com.ajustadoati.sc.config.properties.JwtProviderProperties;
import com.ajustadoati.sc.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static com.ajustadoati.sc.util.TestFileUtils.getJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * User controller class
 *
 * @author rojasric
 */
@Import(TestSecurityConfig.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private JwtProviderProperties jwtProviderProperties;

  @TestConfiguration
  static class TestConfig {

    @Bean
    public UserModelAssembler userModelAssembler() {
      return new UserModelAssembler(userMapper());
    }

    @Bean
    public UserMapper userMapper() {
      return new UserMapperImpl();
    }

  }
  @Test
  public void testCreateUser() throws Exception {
    //given
    var requestContent = getJson("dto/request/createUserRequest.json");

    var mockUser = User
        .builder()
        .userId(1)
        .email("johndoe@example.com")
        .numberId("14447876")
        .lastName("Doe")
        .firstName("John")
        .mobileNumber("2719806")
        .build();

    when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockUser);

    //then
    mockMvc
        .perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestContent))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(HAL_JSON_VALUE))
        .andExpect(content().json(getJson("dto/response/createUserResponse.json")));
  }

  @Test
  public void shouldReturnUser() throws Exception {
    //given
    var mockUser = User
        .builder()
        .userId(1)
        .email("johndoe@example.com")
        .numberId("14447876")
        .lastName("Doe")
        .firstName("John")
        .mobileNumber("2719806")
        .build();

    when(userService.getUserById(any(Integer.class))).thenReturn(mockUser);

    //then
    mockMvc
        .perform(get("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(HAL_JSON_VALUE))
        .andExpect(content().json(getJson("dto/response/getUserResponse.json")));
        //.andExpect(MockMvcResultMatchers.jsonPath("collection[0].id", equalTo(1)));
  }

  @Test
  public void testGetAllUsers() throws Exception {
    //given
    var user1 = User
        .builder()
        .userId(1)
        .firstName("John")
        .lastName("Doe")
        .build();

    var user2 = User
        .builder()
        .userId(2)
        .firstName("Jane")
        .lastName("Doe")
        .build();

    Page<User> usersPage = new PageImpl<>(Arrays.asList(user1, user2));
    when(userService.getAllUsers(any())).thenReturn(usersPage);


    //then
    mockMvc
        .perform(get("/api/users")
            .param("page", "0")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(HAL_JSON_VALUE))
        .andExpect(content().json(getJson("dto/response/getAllUsersResponse.json")));

  }

}
