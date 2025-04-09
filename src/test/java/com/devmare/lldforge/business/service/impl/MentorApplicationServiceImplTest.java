package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.dto.MentorApplicationRequestDto;
import com.devmare.lldforge.business.dto.MentorApplicationResponseDto;
import com.devmare.lldforge.business.dto.UpdateMentorApplicationRequestDto;
import com.devmare.lldforge.business.service.EmailService;
import com.devmare.lldforge.data.entity.MentorApplication;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.enums.MentorApplicationStatus;
import com.devmare.lldforge.data.enums.Role;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.MentorApplicationRepository;
import com.devmare.lldforge.data.repository.UserRepository;
import com.devmare.lldforge.security.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorApplicationServiceImplTest {

    // Mocking dependencies
    @Mock
    private MentorApplicationRepository mentorApplicationRepository;
    @Mock
    private CustomOAuth2UserService customOAuth2UserService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private EmailService emailService;
    @Mock
    private RLock rLock;

    @InjectMocks
    private MentorApplicationServiceImpl mentorApplicationService;

    private User user;

    // Prepare a reusable user object for tests
    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        user = new User();
        user.setId("user123");
        user.setName("Ankan");
        user.setEmail("ankan@example.com");
        user.setIsEmailVerified(true);

        // Inject waitTime and leaseTime manually
        Field waitTimeField = MentorApplicationServiceImpl.class.getDeclaredField("waitTime");
        waitTimeField.setAccessible(true);
        waitTimeField.set(mentorApplicationService, 1); // set to 1 second or any desired value

        Field leaseTimeField = MentorApplicationServiceImpl.class.getDeclaredField("leaseTime");
        leaseTimeField.setAccessible(true);
        leaseTimeField.set(mentorApplicationService, 10); // set to 10 seconds or any desired value

    }

    @Test
    void applyForMentorApplication_shouldSaveAndReturnDto() {
        // Arrange
        MentorApplicationRequestDto requestDto = new MentorApplicationRequestDto("ankan@example.com", "2 years");
        MentorApplication savedApplication = MentorApplication.builder()
                .id("app123")
                .studentId(user.getId())
                .email("ankan@example.com")
                .experience("2 years")
                .status(MentorApplicationStatus.PENDING)
                .appliedAt(System.currentTimeMillis() / 1000)
                .isUnderReview(false)
                .rejectionReason(null)
                .reviewedAt(null)
                .reviewedBy(null)
                .build();

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(mentorApplicationRepository.save(any(MentorApplication.class)))
                .thenReturn(savedApplication);

        // Act
        MentorApplicationResponseDto responseDto = mentorApplicationService.applyForMentorApplication(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals("ankan@example.com", responseDto.getEmail());
        assertEquals("app123", responseDto.getId());
        assertEquals(MentorApplicationStatus.PENDING, responseDto.getStatus());
        verify(emailService, times(1)).sendTemplateEmail(eq("ankan@example.com"), anyString(), anyString(), anyMap());
    }

    @Test
    void getMentorApplicationByUserId_shouldReturnDtoIfExists() {
        // Arrange
        MentorApplication application = MentorApplication.builder()
                .id("app123")
                .studentId("user123")
                .build();

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(mentorApplicationRepository.findByStudentId("user123")).thenReturn(Optional.of(application));

        // Act
        MentorApplicationResponseDto result = mentorApplicationService.getMentorApplicationByUserId();

        // Assert
        assertNotNull(result);
        assertEquals("app123", result.getId());
    }

    @Test
    void getAllMentorApplications_shouldReturnPagedList() {
        // Arrange
        List<MentorApplication> applications = List.of(new MentorApplication(), new MentorApplication());
        Page<MentorApplication> page = new PageImpl<>(applications);

        when(mentorApplicationRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        Page<MentorApplicationResponseDto> result = mentorApplicationService.getAllMentorApplications(0, 10, "appliedAt", "DESC");

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void markApplicationAsUnderReview_shouldUpdateApplication() {
        // Arrange
        MentorApplication application = MentorApplication.builder()
                .id("app456")
                .isUnderReview(false)
                .build();
        MentorApplication savedApplication = MentorApplication.builder()
                .id("app456")
                .isUnderReview(true)
                .reviewedAt(System.currentTimeMillis() / 1000)
                .reviewedBy(user.getId())
                .build();

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(user);
        when(mentorApplicationRepository.findById("app456")).thenReturn(Optional.of(application));
        when(mentorApplicationRepository.save(any())).thenReturn(savedApplication);

        // Act
        mentorApplicationService.markApplicationAsUnderReview("app456");

        // Assert
        assertTrue(application.getIsUnderReview());
        assertNotNull(application.getReviewedAt());
        assertEquals(user.getId(), application.getReviewedBy());
        verify(mentorApplicationRepository).save(application);
    }

    @Test
    void updateMentorApplication_shouldUpdateStatusToApproved() throws InterruptedException {
        // Arrange
        String applicationId = "app789";
        String studentId = "user123";
        String adminId = "admin456";

        UpdateMentorApplicationRequestDto dto =
                new UpdateMentorApplicationRequestDto(applicationId, "approved", null);

        MentorApplication app = MentorApplication.builder()
                .id(applicationId)
                .studentId(studentId)
                .status(MentorApplicationStatus.PENDING)
                .isUnderReview(true)
                .build();

        User student = new User();
        student.setId(studentId);
        student.setEmail("student@example.com");
        student.setName("Student Name");
        student.setRole(Role.STUDENT);

        User admin = new User();
        admin.setId(adminId);

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mentorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);
        when(mentorApplicationRepository.save(any(MentorApplication.class))).thenReturn(app);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act
        MentorApplicationResponseDto result = mentorApplicationService.updateMentorApplication(dto);

        // Assert
        assertNotNull(result);
        assertEquals(MentorApplicationStatus.APPROVED, result.getStatus());
        assertEquals(Role.MENTOR, student.getRole());
        assertEquals(adminId, app.getReviewedBy());
        assertNotNull(app.getReviewedAt());
        assertFalse(app.getIsUnderReview());

        verify(userRepository).save(student);
        verify(mentorApplicationRepository).save(app);
        verify(emailService).sendTemplateEmail(eq(student.getEmail()), eq("Congratulations! Your Mentor Application has been Approved"), eq("mentor-application-approved"), anyMap());
        verify(rLock).unlock(); // Ensure lock is released
    }

    @Test
    void updateMentorApplication_shouldUpdateStatusToRejectedWithReason() throws InterruptedException {
        // Arrange
        String applicationId = "app999";
        String studentId = "user456";
        String adminId = "admin789";
        String rejectionReason = "Not enough experience";

        UpdateMentorApplicationRequestDto dto =
                new UpdateMentorApplicationRequestDto(applicationId, "rejected", rejectionReason);

        MentorApplication app = MentorApplication.builder()
                .id(applicationId)
                .studentId(studentId)
                .status(MentorApplicationStatus.PENDING)
                .isUnderReview(true)
                .build();

        User student = new User();
        student.setId(studentId);
        student.setEmail("student2@example.com");
        student.setName("Another Student");
        student.setRole(Role.STUDENT);

        User admin = new User();
        admin.setId(adminId);

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mentorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(mentorApplicationRepository.save(any(MentorApplication.class))).thenReturn(app);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act
        MentorApplicationResponseDto result = mentorApplicationService.updateMentorApplication(dto);

        // Assert
        assertNotNull(result);
        assertEquals(MentorApplicationStatus.REJECTED, result.getStatus());
        assertEquals(rejectionReason, app.getRejectionReason());
        assertEquals(adminId, app.getReviewedBy());
        assertNotNull(app.getReviewedAt());
        assertFalse(app.getIsUnderReview());

        verify(mentorApplicationRepository).save(app);
        verify(emailService).sendTemplateEmail(eq(student.getEmail()), eq("Your Mentor Application has been reviewed"), eq("mentor-application-rejected"), anyMap());
        verify(rLock).unlock();
    }

    @Test
    void updateMentorApplication_shouldThrowExceptionIfLockFails() throws InterruptedException {
        // Arrange
        String applicationId = "app111";
        UpdateMentorApplicationRequestDto dto = new UpdateMentorApplicationRequestDto(applicationId, "approved", null);

        User admin = new User();
        admin.setId("admin999");

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act and Assert
        AppInfoException exception = assertThrows(AppInfoException.class, () -> mentorApplicationService.updateMentorApplication(dto));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatus());
        verify(rLock, never()).unlock();
    }

    @Test
    void updateMentorApplication_shouldThrowExceptionIfApplicationNotFound() throws InterruptedException {
        // Arrange
        String applicationId = "nonExistentApp";
        UpdateMentorApplicationRequestDto dto = new UpdateMentorApplicationRequestDto(applicationId, "approved", null);

        User admin = new User();
        admin.setId("admin888");

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mentorApplicationRepository.findById(applicationId)).thenReturn(Optional.empty());
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act and Assert
        AppInfoException exception = assertThrows(AppInfoException.class, () -> mentorApplicationService.updateMentorApplication(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(rLock).unlock();
    }

    @Test
    void updateMentorApplication_shouldThrowExceptionForInvalidStatus() throws InterruptedException {
        // Arrange
        String applicationId = "app222";
        UpdateMentorApplicationRequestDto dto = new UpdateMentorApplicationRequestDto(applicationId, "invalid_status", null);
        MentorApplication app = MentorApplication.builder().id(applicationId).build();
        User admin = new User();
        admin.setId("admin111");

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mentorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act and Assert
        AppInfoException exception = assertThrows(AppInfoException.class, () -> mentorApplicationService.updateMentorApplication(dto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(rLock).unlock();
    }

    @Test
    void updateMentorApplication_shouldThrowExceptionIfRejectionReasonMissing() throws InterruptedException {
        // Arrange
        String applicationId = "app333";
        UpdateMentorApplicationRequestDto dto = new UpdateMentorApplicationRequestDto(applicationId, "rejected", null);
        MentorApplication app = MentorApplication.builder().id(applicationId).build();
        User admin = new User();
        admin.setId("admin222");

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mentorApplicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act and Assert
        AppInfoException exception = assertThrows(AppInfoException.class, () -> mentorApplicationService.updateMentorApplication(dto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Reason not found", exception.getMessage());
        verify(rLock).unlock();
    }

    @Test
    void updateMentorApplication_shouldHandleInterruptedException() throws InterruptedException {
        // Arrange
        String applicationId = "app555";
        UpdateMentorApplicationRequestDto dto = new UpdateMentorApplicationRequestDto(applicationId, "approved", null);
        User admin = new User();
        admin.setId("admin333");

        when(customOAuth2UserService.getCurrentAuthenticatedUser()).thenReturn(admin);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("Lock interrupted"));

        // Act and Assert
        AppInfoException exception = assertThrows(AppInfoException.class, () -> mentorApplicationService.updateMentorApplication(dto));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Something went wrong while updating the application. Please try again.", exception.getMessage());
        verify(rLock, never()).unlock();
        assertTrue(Thread.currentThread().isInterrupted());
    }
}