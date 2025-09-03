package com.academy.api.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StudentQueryServiceTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentQueryService studentQueryService;

    @BeforeEach
    void setUp() {
        studentRepository.save(new Student("김철수", 1));
        studentRepository.save(new Student("이영희", 2));
        studentRepository.save(new Student("박민수", 3));
        studentRepository.save(new Student("최영수", 1));
    }

    @Test
    @DisplayName("이름에 '수'가 포함된 학생을 찾을 수 있다")
    void findByNameContaining() {
        // when
        List<Student> result = studentQueryService.findByNameContaining("수");

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("김철수", "박민수", "최영수");
    }

    @Test
    @DisplayName("이름에 '영'이 포함된 학생을 찾을 수 있다")
    void findByNameContainingYoung() {
        // when
        List<Student> result = studentQueryService.findByNameContaining("영");

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Student::getName)
                .containsExactlyInAnyOrder("이영희", "최영수");
    }

}