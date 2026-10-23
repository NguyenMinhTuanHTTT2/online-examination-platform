package com.tuan.exam.service.interfaces;

import com.tuan.exam.dto.response.SubjectResponse;
import com.tuan.exam.entity.Subject;

import java.util.List;

public interface SubjectService {
    List<SubjectResponse> getAllSubjects();
    SubjectResponse createSubject(Subject subject);
    // Lấy chi tiết môn học theo mã (Code)
    SubjectResponse getSubjectByCode(String code);
}
