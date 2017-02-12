package com.influans.sp.service;

import com.google.common.collect.ImmutableList;
import com.influans.sp.ApplicationTest;
import com.influans.sp.builders.*;
import com.influans.sp.dto.StoryCreationDto;
import com.influans.sp.dto.StoryDto;
import com.influans.sp.entity.SessionEntity;
import com.influans.sp.entity.StoryEntity;
import com.influans.sp.entity.UserEntity;
import com.influans.sp.enums.UserRole;
import com.influans.sp.enums.WsTypes;
import com.influans.sp.exception.CustomErrorCode;
import com.influans.sp.exception.CustomException;
import com.influans.sp.repository.SessionRepository;
import com.influans.sp.repository.StoryRepository;
import com.influans.sp.repository.UserRepository;
import com.influans.sp.security.Principal;
import com.influans.sp.security.SecurityContext;
import com.influans.sp.websocket.WebSocketSender;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;

/**
 * @author hazem
 */
public class StoryServiceTest extends ApplicationTest {

    @Autowired
    private StoryService storyService;
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private SecurityContext securityContext;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Mockito.reset(webSocketSender);
        Mockito.reset(securityContext);
    }

    /**
     * @verifies throw an exception if session id is null or empty
     * @see StoryService#listStories(String)
     */
    @Test
    public void listStories_shouldThrowAnExceptionIfSessionIdIsNullOrEmpty() throws Exception {
        try {
            storyService.listStories(null);
            Assert.fail("shouldThrowAnExceptionIfSessionIdIsNullOrEmpty");
        } catch (CustomException e) {
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.BAD_ARGS);
        }
    }

    /**
     * @verifies throw an exception if session id is not valid
     * @see StoryService#listStories(String)
     */
    @Test
    public void listStories_shouldThrowAnExceptionIfSessionIdIsNotValid() throws Exception {
        try {
            storyService.listStories("invalid_session_id");
            Assert.fail("shouldThrowAnExceptionIfSessionIdIsNotValid");
        } catch (CustomException e) {
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.OBJECT_NOT_FOUND);
        }
    }

    /**
     * @verifies return stories related to the given session
     * @see StoryService#listStories(String)
     */
    @Test
    public void listStories_shouldReturnStoriesRelatedToTheGivenSession() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final List<StoryEntity> stories = ImmutableList.<StoryEntity>builder()
                .add(StoryEntityBuilder.builder()
                        .withSessionId(sessionId)
                        .withStoryId("story-1")
                        .build())
                .add(StoryEntityBuilder.builder()
                        .withSessionId(sessionId)
                        .withStoryId("story-2")
                        .build())
                .build();
        storyRepository.save(stories);

        // when
        final List<StoryDto> foundStories = storyService.listStories(sessionId);

        // then
        Assertions.assertThat(foundStories).hasSize(2);
    }

    /**
     * @verifies throw an exception if storyId is null or empty
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldThrowAnExceptionIfStoryIdIsNullOrEmpty() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        try {
            // when
            storyService.delete(null);
            Assert.fail("shouldThrowAnExceptionIfStoryIdIsNullOrEmpty");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.BAD_ARGS);
        }
    }

    /**
     * @verifies throw an exception if story does not exist
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldThrowAnExceptionIfStoryDoesNotExist() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));
        try {
            // when
            storyService.delete("invalid_story_id");
            Assert.fail("shouldThrowAnExceptionIfStoryDoesNotExist");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.OBJECT_NOT_FOUND);
        }
    }

    /**
     * @verifies delete a story
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldDeleteAStory() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withStoryId("story-1")
                .withSessionId(sessionId)
                .build();
        storyRepository.save(storyEntity);

        // when
        storyService.delete(storyEntity.getStoryId());

        // then
        Assertions.assertThat(storyRepository.exists(storyEntity.getStoryId())).isFalse();
    }

    /**
     * @verifies check that the user is authenticated as admin
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldCheckThatTheUserIsAuthenticatedAsAdmin() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.VOTER)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));
        try {
            // when
            storyService.delete("story_id");
            Assert.fail("shouldCheckThatTheUserIsAuthenticatedAsAdmin");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.PERMISSION_DENIED);
            Assertions.assertThat(e.getMessage()).startsWith("user has not session admin role");
        }
    }

    /**
     * @verifies check that the user is connected to the related session
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldCheckThatTheUserIsConnectedToTheRelatedSession() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final String storyId = "storyId";
        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withSessionId("other_session")
                .withStoryId(storyId)
                .build();
        storyRepository.save(storyEntity);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));
        try {
            // when
            storyService.delete(storyId);
            Assert.fail("shouldCheckThatTheUserIsConnectedToTheRelatedSession");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.PERMISSION_DENIED);
            Assertions.assertThat(e.getMessage()).contains("is not admin of session");
        }
    }

    /**
     * @verifies send a websocket notification
     * @see StoryService#delete(String)
     */
    @Test
    public void delete_shouldSendAWebsocketNotification() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withSessionId(sessionId)
                .withStoryId("story-1")
                .build();
        storyRepository.save(storyEntity);

        // when
        storyService.delete(storyEntity.getStoryId());

        //then
        verify(webSocketSender).sendNotification(storyEntity.getSessionId(), WsTypes.STORY_REMOVED, storyEntity.getStoryId());
    }

    /**
     * @verifies throw an exception if storyName is empty or null
     * @see StoryService#createStory(StoryCreationDto)
     */
    @Test
    public void createStory_shouldThrowAnExceptionIfStoryNameIsEmptyOrNull() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryCreationDto storyCreationDto = StoryCreationDtoBuilder.builder()
                .build();
        try {
            // when
            storyService.createStory(storyCreationDto);
            Assert.fail("shouldThrowAnExceptionIfStoryNameIsEmptyOrNull");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.BAD_ARGS);
        }
    }

    /**
     * @verifies throw an exception if storyName contains only spaces
     * @see StoryService#createStory(StoryCreationDto)
     */
    @Test
    public void createStory_shouldThrowAnExceptionIfStoryNameContainsOnlySpaces() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryCreationDto storyCreationDto = StoryCreationDtoBuilder.builder()
                .withStoryName("   ")
                .build();
        try {
            // when
            storyService.createStory(storyCreationDto);
            Assert.fail("shouldThrowAnExceptionIfStoryNameIsEmptyOrNull");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.BAD_ARGS);
        }
    }

    /**
     * @verifies check that the user is authenticated as admin
     * @see StoryService#createStory(StoryCreationDto)
     */
    @Test
    public void createStory_shouldCheckThatTheUserIsAuthenticatedAsAdmin() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.VOTER)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryCreationDto storyCreationDto = StoryCreationDtoBuilder.builder()
                .withStoryName("story_name")
                .build();
        try {
            // when
            storyService.createStory(storyCreationDto);
            Assert.fail("shouldCheckThatTheUserIsAuthenticatedAsAdmin");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.PERMISSION_DENIED);
            Assertions.assertThat(e.getMessage()).startsWith("user has not session admin role");
        }
    }

    /**
     * @verifies create a story related to the given withSessionId
     * @see StoryService#createStory(StoryCreationDto)
     */
    @Test
    public void createStory_shouldCreateAStoryRelatedToTheGivenSessionId() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryCreationDto storyCreationDto = StoryCreationDtoBuilder.builder()
                .withStoryName("story-name")
                .withOrder(2)
                .build();

        // when
        final StoryCreationDto createdStory = storyService.createStory(storyCreationDto);

        // then
        Assertions.assertThat(createdStory.getStoryId()).isNotNull();
        final StoryEntity storyEntity = storyRepository.findOne(createdStory.getStoryId());
        Assertions.assertThat(storyEntity).isNotNull();
        Assertions.assertThat(storyEntity.getStoryName()).isEqualTo(storyCreationDto.getStoryName());
        Assertions.assertThat(storyEntity.getOrder()).isEqualTo(storyCreationDto.getOrder());
    }

    /**
     * @verifies send a websocket notification
     * @see StoryService#createStory(StoryCreationDto)
     */
    @Test
    public void createStory_shouldSendAWebsocketNotification() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final StoryCreationDto storyCreationDto = StoryCreationDtoBuilder.builder()
                .withStoryName("story-name")
                .withOrder(2)
                .build();

        // when
        final StoryCreationDto createdStory = storyService.createStory(storyCreationDto);

        // then
        storyCreationDto.setStoryId(createdStory.getStoryId());
        verify(webSocketSender).sendNotification(sessionId, WsTypes.STORY_ADDED, storyCreationDto);
    }

    /**
     * @verifies throw an exception if storyId is empty or null
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldThrowAnExceptionIfStoryIdIsEmptyOrNull() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        try {
            // when
            storyService.endStory(null);
            Assert.fail("shouldThrowAnExceptionIfStoryIdIsEmptyOrNull");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.BAD_ARGS);
        }
    }

    /**
     * @verifies throw an exception if story does not exist
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldThrowAnExceptionIfStoryDoesNotExist() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        try {
            // when
            storyService.endStory("invalid_story_id");
            Assert.fail("shouldThrowAnExceptionIfStoryDoesNotExist");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.OBJECT_NOT_FOUND);
        }
    }

    /**
     * @verifies check that the user is authenticated as admin
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldCheckThatTheUserIsAuthenticatedAsAdmin() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.VOTER)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        try {
            // when
            storyService.endStory("story_id");
            Assert.fail("shouldCheckThatTheUserIsAuthenticatedAsAdmin");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.PERMISSION_DENIED);
            Assertions.assertThat(e.getMessage()).startsWith("user has not session admin role");
        }
    }

    /**
     * @verifies check that the user is connected to the related session
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldCheckThatTheUserIsConnectedToTheRelatedSession() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final String storyId = "storyId";
        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withStoryId(storyId)
                .withSessionId("other_session_id")
                .build();
        storyRepository.save(storyEntity);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        try {
            // when
            storyService.endStory(storyId);
            Assert.fail("shouldCheckThatTheUserIsConnectedToTheRelatedSession");
        } catch (CustomException e) {
            // then
            Assertions.assertThat(e.getCustomErrorCode()).isEqualTo(CustomErrorCode.PERMISSION_DENIED);
            Assertions.assertThat(e.getMessage()).contains("is not admin of session");
        }
    }

    /**
     * @verifies set story as ended
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldSetStoryAsEnded() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final String storyId = "storyId";
        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withStoryId(storyId)
                .withSessionId(sessionId)
                .build();
        storyRepository.save(storyEntity);

        // when
        storyService.endStory(storyId);

        // then
        final StoryEntity foundStory = storyRepository.findOne(storyId);
        Assertions.assertThat(foundStory.isEnded()).isTrue();
    }

    /**
     * @verifies send a websocket notification
     * @see StoryService#endStory(String)
     */
    @Test
    public void endStory_shouldSendAWebsocketNotification() throws Exception {
        // given
        final String sessionId = "sessionId";
        final SessionEntity sessionEntity = SessionEntityBuilder.builder()
                .withSessionId(sessionId)
                .build();
        sessionRepository.save(sessionEntity);

        final String username = "Leo";
        final UserEntity connectedUser = UserEntityBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withConnected(true)
                .build();
        userRepository.save(connectedUser);

        final Principal principal = PrincipalBuilder.builder()
                .withUsername(username)
                .withSessionId(sessionId)
                .withRole(UserRole.SESSION_ADMIN)
                .build();
        Mockito.when(securityContext.getAuthenticationContext()).thenReturn(Optional.of(principal));

        final String storyId = "storyId";
        final StoryEntity storyEntity = StoryEntityBuilder.builder()
                .withStoryId(storyId)
                .withSessionId(sessionId)
                .build();
        storyRepository.save(storyEntity);

        // when
        storyService.endStory(storyId);

        // then
        verify(webSocketSender).sendNotification(storyEntity.getSessionId(), WsTypes.STORY_ENDED, storyId);
    }
}
