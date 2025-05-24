package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.dto.MentorApplicationRequestDto;
import com.devmare.lldforge.business.dto.MentorApplicationResponseDto;
import com.devmare.lldforge.business.dto.UpdateMentorApplicationRequestDto;
import com.devmare.lldforge.business.service.EmailService;
import com.devmare.lldforge.business.service.MentorApplicationService;
import com.devmare.lldforge.data.entity.MentorApplication;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.enums.MentorApplicationStatus;
import com.devmare.lldforge.data.enums.Role;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.MentorApplicationRepository;
import com.devmare.lldforge.data.repository.UserRepository;
import com.devmare.lldforge.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static com.devmare.lldforge.data.utils.AppUtils.isValidEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorApplicationServiceImpl implements MentorApplicationService {

    private final MentorApplicationRepository mentorApplicationRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;
    private final EmailService emailService;

//    @Autowired
//    private RedissonClient redissonClient;
//
//    @Value("${app.mentor-lock.wait-time}")
//    private Integer waitTime;
//
//    @Value("${app.mentor-lock.lease-time}")
//    private Integer leaseTime;

    @Override
    @Transactional
    public MentorApplicationResponseDto applyForMentorApplication(MentorApplicationRequestDto mentorApplicationRequestDto) {
        User user = customOAuth2UserService.getCurrentAuthenticatedUser();

        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            log.warn("User [{}] attempted to apply without email verification", user.getId());
            throw new AppInfoException("Please verify your email before applying to become a mentor", HttpStatus.FORBIDDEN);
        }

        log.info("User [{}] is applying for mentor application", user.getId());

        if (!isValidEmail(mentorApplicationRequestDto.getEmail())) {
            log.warn("Invalid email format: {}", mentorApplicationRequestDto.getEmail());
            throw new AppInfoException("Invalid email format", HttpStatus.BAD_REQUEST);
        }

        MentorApplication mentorApplication = MentorApplication.builder()
                .email(mentorApplicationRequestDto.getEmail())
                .experience(mentorApplicationRequestDto.getExperience())
                .reviewedAt(null)
                .reviewedBy(null)
                .build();
        mentorApplication = mentorApplicationRepository.save(mentorApplication);

        // Send confirmation email
        emailService.sendTemplateEmail(
                user.getEmail(),
                "Your Mentor Application has been received",
                "mentor-application-submitted",
                Map.of("name", user.getName())
        );


        log.info("Mentor application submitted successfully for user [{}]", user.getId());
        return MentorApplicationResponseDto.fromEntity(mentorApplication);
    }

    @Override
    public MentorApplicationResponseDto getMentorApplicationByUserId() {
        User user = customOAuth2UserService.getCurrentAuthenticatedUser();
        log.info("Fetching mentor application for user [{}]", user.getId());

        Optional<MentorApplication> optionalMentorApplication = mentorApplicationRepository.findByUser(user);
        return optionalMentorApplication.map(MentorApplicationResponseDto::fromEntity).orElse(null);
    }

    @Override
//    @Cacheable(
//            value = "mentorApplications",
//            key = "'page_' + #page + '_size_' + #size + '_sort_' + #sortBy + '_dir_' + #direction"
//    )
    public Page<MentorApplicationResponseDto> getAllMentorApplications(int page, int size, String sortBy, String direction) {
        log.info("Fetching mentor applications: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return mentorApplicationRepository.findAll(pageRequest).map(MentorApplicationResponseDto::fromEntity);
    }

    @Override
//    @CacheEvict(value = "mentorApplications", allEntries = true)
    public void markApplicationAsUnderReview(Long applicationId) {
        User user = customOAuth2UserService.getCurrentAuthenticatedUser();
        log.info("Admin [{}] marking application [{}] as under review", user.getId(), applicationId);

        Optional<MentorApplication> optionalMentorApplication = mentorApplicationRepository.findById(applicationId);
        if (optionalMentorApplication.isEmpty()) {
            log.error("Mentor application [{}] not found", applicationId);
            throw new AppInfoException("Mentor application not found", HttpStatus.NOT_FOUND);
        }
        MentorApplication mentorApplication = optionalMentorApplication.get();

        if (Boolean.TRUE.equals(mentorApplication.getIsUnderReview())) {
            log.warn("Application [{}] is already under review", applicationId);
            throw new AppInfoException("Application is already under review", HttpStatus.BAD_REQUEST);
        }

        mentorApplication.setIsUnderReview(true);
        mentorApplication.setReviewedAt(Instant.now().getEpochSecond());
        mentorApplication.setReviewedBy(user);
        mentorApplicationRepository.save(mentorApplication);
        log.info("Application [{}] marked as under review by admin [{}]", applicationId, user.getId());
    }

    @Override
//    @CacheEvict(value = "mentorApplications", allEntries = true)
    public MentorApplicationResponseDto updateMentorApplication(UpdateMentorApplicationRequestDto requestDto) {
        User admin = customOAuth2UserService.getCurrentAuthenticatedUser();

        /// Unique key per application
//        String lockKey = "mentor:app:lock:" + requestDto.getId();
//        RLock lock = redissonClient.getLock(lockKey);

        log.info("Admin [{}] attempting to update mentor application [{}]", admin.getId(), requestDto.getId());

        try {
            /*
              1. Try to get the lock
              2. If none else is holding it, my system get the lock immediately and move forward
              3. If someone else is already holding it, I've to wait up to 10 seconds for them to release it
              4. If after 1 seconds the lock is still not available, my system will fail
             */
//            boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

//            if (!isLocked) {
//                log.warn("Could not acquire lock for application [{}]", requestDto.getId());
//                throw new AppInfoException("System busy, try again later", HttpStatus.TOO_MANY_REQUESTS);
//            }

            Optional<MentorApplication> optionalMentorApplication = mentorApplicationRepository.findById(requestDto.getId());
            if (optionalMentorApplication.isEmpty()) {
                throw new AppInfoException("Mentor application not found", HttpStatus.NOT_FOUND);
            }

            MentorApplication mentorApplication = optionalMentorApplication.get();
            boolean isValidStatus = Arrays.stream(MentorApplicationStatus.values())
                    .anyMatch(status -> status.name().equalsIgnoreCase(requestDto.getStatus()));
            if (!isValidStatus) {
                log.error("Invalid status [{}] provided for application [{}]", requestDto.getStatus(), requestDto.getId());
                throw new AppInfoException("Invalid status update", HttpStatus.BAD_REQUEST);
            }

            if (mentorApplication.getUser() == null) {
                throw new AppInfoException("User not found", HttpStatus.NOT_FOUND);
            }

            Optional<User> optionalStudent = userRepository.findById(mentorApplication.getUser().getId());
            if (optionalStudent.isEmpty()) {
                log.error("Student user [{}] not found", mentorApplication.getUser().getId());
                throw new AppInfoException("Student user not found", HttpStatus.NOT_FOUND);
            }
            User student = optionalStudent.get();

            // Update the application status
            MentorApplicationStatus newStatus = MentorApplicationStatus.valueOf(requestDto.getStatus().toUpperCase());
            mentorApplication.setStatus(newStatus);

            // Set rejection reason if applicable
            if (newStatus == MentorApplicationStatus.REJECTED) {
                if (requestDto.getReason() == null) {
                    log.error("No rejection reason provided for application [{}]", requestDto.getId());
                    throw new AppInfoException("Reason not found", HttpStatus.NOT_FOUND);
                }
                mentorApplication.setRejectionReason(requestDto.getReason());
                log.info("Application [{}] rejected with reason: {}", requestDto.getId(), requestDto.getReason());
            } else if (newStatus == MentorApplicationStatus.APPROVED) {
                student.setRole(Role.MENTOR);
                userRepository.save(student);
                log.info("Student [{}] promoted to MENTOR", student.getId());
            }

            // Set review metadata
            mentorApplication.setReviewedAt(Instant.now().getEpochSecond());
            mentorApplication.setReviewedBy(admin);
            mentorApplication.setIsUnderReview(false);

            // Save updated application
            mentorApplication = mentorApplicationRepository.save(mentorApplication);
            log.info("Mentor application [{}] successfully updated by admin [{}]", requestDto.getId(), admin.getId());

            return MentorApplicationResponseDto.fromEntity(mentorApplication);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while locking mentor application [{}]", requestDto.getId(), e);
            throw new AppInfoException("Something went wrong while updating the application. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
//            if (lock.isHeldByCurrentThread()) {
//                lock.unlock();
//                log.debug("Lock released for application [{}]", requestDto.getId());
//            }
            log.info("Redis disabled for now.");
        }
    }
}
