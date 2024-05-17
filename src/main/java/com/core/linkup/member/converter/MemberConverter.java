package com.core.linkup.member.converter;

import com.core.linkup.common.annotation.Converter;
import com.core.linkup.member.entity.Member;
import com.core.linkup.member.response.MemberResponse;
import com.core.linkup.security.Tokens;

@Converter
public class MemberConverter {

    public MemberResponse toMemberResponse(Member member) {
        return MemberResponse.builder()
                .name(member.getName())
                .username(member.getUsername())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .gender(member.getGender().getGenderInKor())
                .industry(member.getIndustry().getIndustryInKor())
                .occupation(member.getOccupation().getOccupationInKor())
                .birthday(member.getBirthday())
                .introduction(member.getIntroduction())
                .companyId(member.getCompanyId())
                .profileImage(member.getProfileImage())
                .tokens(null)
                .build();
    }

    public MemberResponse toMemberResponse(Member member, Tokens tokens) {
        return MemberResponse.builder()
                .name(member.getName())
                .username(member.getUsername())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .gender(member.getGender().getGenderInKor())
                .industry(member.getIndustry().getIndustryInKor())
                .occupation(member.getOccupation().getOccupationInKor())
                .birthday(member.getBirthday())
                .introduction(member.getIntroduction())
                .companyId(member.getCompanyId())
                .profileImage(member.getProfileImage())
                .tokens(tokens)
                .build();
    }
}
