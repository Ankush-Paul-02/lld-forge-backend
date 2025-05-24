package com.devmare.lldforge.business.service;

import com.devmare.lldforge.business.dto.MentorApplicationRequestDto;
import com.devmare.lldforge.business.dto.MentorApplicationResponseDto;
import com.devmare.lldforge.business.dto.UpdateMentorApplicationRequestDto;
import org.springframework.data.domain.Page;

public interface MentorApplicationService {

    MentorApplicationResponseDto applyForMentorApplication(MentorApplicationRequestDto mentorApplicationRequestDto);

    MentorApplicationResponseDto getMentorApplicationByUserId();

    Page<MentorApplicationResponseDto> getAllMentorApplications(int page, int size, String sortBy, String direction);

    void markApplicationAsUnderReview(Long applicationId);

    MentorApplicationResponseDto updateMentorApplication(UpdateMentorApplicationRequestDto requestDto);
}
