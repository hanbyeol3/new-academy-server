package com.academy.api.student;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.academy.api.student.QStudent.student;

@Service
@RequiredArgsConstructor
public class StudentQueryService {

    private final JPAQueryFactory queryFactory;

    public List<Student> findByNameContaining(String name) {
        return queryFactory
                .selectFrom(student)
                .where(student.name.contains(name))
                .fetch();
    }

}