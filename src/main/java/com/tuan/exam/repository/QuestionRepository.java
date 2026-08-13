package com.tuan.exam.repository;

import com.tuan.exam.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    @EntityGraph(attributePaths = {"answers"})
    List<Question> findByQuizId(Long quizId);
}
