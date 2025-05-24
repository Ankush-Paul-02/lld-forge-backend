package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.dto.MentorApplicationResponseDto;
import com.devmare.lldforge.business.dto.UpdateMentorApplicationRequestDto;
import com.devmare.lldforge.business.service.MentorApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MentorApplicationService mentorApplicationService;

    @GetMapping("/mentor-application/all")
    public ResponseEntity<DefaultResponseDto> getAllMentorApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Page<MentorApplicationResponseDto> applications = mentorApplicationService.getAllMentorApplications(page, size, sortBy, direction);
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("totalPages", applications.getTotalPages(),
                                "totalElements", applications.getTotalElements(),
                                "currentPage", applications.getNumber(),
                                "data", applications.getContent()
                        ),
                        "Mentor applications fetched successfully."
                )
        );
    }

    @PatchMapping("/mentor-application/{id}/under-review")
    public ResponseEntity<DefaultResponseDto> markAsUnderReview(@PathVariable Long id) {
        mentorApplicationService.markApplicationAsUnderReview(id);
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("id", id),
                        "Mentor application marked as under review successfully."
                )
        );
    }

    @PutMapping("/mentor-application/update")
    public ResponseEntity<DefaultResponseDto> updateMentorApplication(@Valid @RequestBody UpdateMentorApplicationRequestDto requestDto) {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", mentorApplicationService.updateMentorApplication(requestDto)),
                        "Mentor application updated successfully."
                )
        );
    }
}
