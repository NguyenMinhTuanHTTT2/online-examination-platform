package com.tuan.exam.service.impl;

import com.tuan.exam.dto.response.SubjectResponse;
import com.tuan.exam.entity.Subject;
import com.tuan.exam.repository.SubjectRepository;
import com.tuan.exam.service.interfaces.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã: " + code));
        return mapToResponse(subject);
    }

    @Override
    public SubjectResponse createSubject(Subject subject) {
        // Kiểm tra xem mã môn học đã tồn tại trong database chưa
        if (subjectRepository.existsByCode(subject.getCode())) {
            throw new RuntimeException("Mã môn học '" + subject.getCode() + "' đã tồn tại!");
        }

        Subject savedSubject = subjectRepository.save(subject);
        return mapToResponse(savedSubject);
    }

    // Hàm phụ trợ giúp map từ Entity sang Response DTO để tránh lặp code
    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .description(subject.getDescription())
                .build();
    }
}