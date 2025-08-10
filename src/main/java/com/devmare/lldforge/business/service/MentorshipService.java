package com.devmare.lldforge.business.service;

import com.devmare.lldforge.business.dto.MentorshipSessionResponseDto;

import java.util.List;

public interface MentorshipService {

    List<MentorshipSessionResponseDto> getMentorshipSessionByStudent();
}
