package com.study.querydsl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.querydsl.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQuerydslRepository {
    List<Member> findByUserName(String userName);
}
