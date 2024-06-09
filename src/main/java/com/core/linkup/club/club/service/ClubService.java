package com.core.linkup.club.club.service;

import com.core.linkup.club.club.converter.ClubConverter;
import com.core.linkup.club.club.entity.*;
import com.core.linkup.club.club.repository.*;
import com.core.linkup.club.club.request.*;
import com.core.linkup.club.club.response.ClubApplicationResponse;
import com.core.linkup.club.club.response.ClubLikeResponse;
import com.core.linkup.club.club.response.ClubSearchResponse;
import com.core.linkup.club.clubmeeting.entity.ClubMeeting;
import com.core.linkup.club.clubmeeting.repository.ClubMeetingRepository;
import com.core.linkup.common.exception.BaseException;
import com.core.linkup.common.response.BaseResponseStatus;
import com.core.linkup.member.entity.Member;
import com.core.linkup.member.repository.MemberRepository;
import com.core.linkup.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubQuestionRepository clubQuestionRepository;
    private final ClubConverter clubConverter;
    private final ClubAnswerRepository clubAnswerRepository;
    private final ClubMeetingRepository clubMeetingRepository;
    private final ClubLikeRepository clubLikeRepository;

    //소모임 조회
    public ClubSearchResponse findClub(Long clubId, Member member) {
        Club club = validateClub(clubId);
        // 소모임 창설자
        Member clubHost = validateMember(club.getMemberId());

        List<ClubMember> clubMembers = clubMemberRepository.findByClubId(clubId);
        List<Long> memberIds = clubMembers.stream()
                .map(ClubMember::getMemberId)
                .collect(Collectors.toList());

        List<Member> members = memberRepository.findAllById(memberIds);

        List<ClubMeeting> clubMeetings = clubMeetingRepository.findByClubId(clubId);

        Map<Long, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        Boolean liked = clubLikeRepository.existsByClubIdAndMemberId(clubId, member.getId());

        return clubConverter.toClubResponse(club, clubHost, clubMembers, clubMeetings, memberMap, liked);
    }

    // 로그인 시 전체조회
    public Page<ClubSearchResponse> findClubs(Member member, Pageable pageable, String category) {
        Page<Club> clubs = clubRepository.findSearchClubs(category, pageable);
        List<Member> members = memberRepository.findAllById(clubs.stream()
                .map(Club::getMemberId)
                .collect(Collectors.toList()));
        Map<Long, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        List<ClubLike> clubLikes = clubLikeRepository.findAllByMemberId(member.getId());
        List<Long> clubLikeIds = clubLikes.stream().map(ClubLike::getClubId).toList();

        return clubs.map(club ->
                clubConverter.toClubResponses(
                        club, memberMap.get(club.getMemberId()), clubLikeIds.contains(club.getId())));
    }

    // 비로그인 시 전체조회
//    public Page<ClubSearchResponse> findClubs(Pageable pageable, ClubSearchRequest request){
        public Page<ClubSearchResponse> findClubs(Pageable pageable, String category){
        System.out.println(category);

            Page<Club> clubs = clubRepository.findSearchClubs(category, pageable);
            List<Member> members = memberRepository.findAllById(clubs.stream()
                    .map(Club::getMemberId)
                    .collect(Collectors.toList()));
            Map<Long, Member> memberMap = members.stream()
                    .collect(Collectors.toMap(Member::getId, Function.identity()));

            return clubs.map(club ->
                    clubConverter.toClubResponses(
                            club, memberMap.get(club.getMemberId())));
    }

    // 소모임 등록
    public ClubSearchResponse createClub(MemberDetails member, ClubCreateRequest request) {
        Long memberId = getMemberId(member);

        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_CLUB_OWNER));

        // 멤버십 확인
        if (!clubRepository.existsValidMembershipWithLocation(memberId)) {
            throw new BaseException(BaseResponseStatus.INVALID_MEMBERSHIP);
        }

        Club club = clubConverter.toClubEntity(request, member);
        Club savedClub = clubRepository.save(club);

        String location = null;
        if (request.officeBuildingLocation() != null) {
            location = request.officeBuildingLocation();
        }

        Long officeBuildingId = clubRepository.findOfficeBuildingIdByLocation(location);

        if (officeBuildingId != null) {
            savedClub.setOfficeBuildingId(officeBuildingId);
            // 업데이트된 클럽 저장
            savedClub = clubRepository.save(savedClub);
        }

//        clubRepository.updateClubOfficeBuildingId(memberId, savedClub.getId());
//        Long officeBuildingId = officeRepository.findOfficeBuildingIdByLocation(club.getOfficeBuildingLocation());
//        club.setOfficeBuildingId(officeBuildingId);


        if (request.clubQuestions() != null && !request.clubQuestions().isEmpty()) {
            Club finalSavedClub = savedClub;
            List<ClubQuestion> questions = request.clubQuestions().stream()
                    .map(questionRequest -> clubConverter.toClubQuestionEntity(questionRequest, finalSavedClub))
                    .collect(Collectors.toList());
            clubQuestionRepository.saveAll(questions);
        }

        return clubConverter.toClubResponses(savedClub, creator);
    }

    //소모임 수정
    public ClubSearchResponse updateClub(MemberDetails member, Long clubId, ClubUpdateRequest updateRequest) {
        Long memberId = getMemberId(member);
        Club existingClub = validateClub(clubId);

        if (!existingClub.getMemberId().equals(memberId)) {
            throw new BaseException(BaseResponseStatus.INVALID_MEMBER);
        }

        Club updatedClub = clubConverter.updateClubEntity(existingClub, updateRequest, member);
        Club savedClub = clubRepository.save(updatedClub);
        Member creator = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_CLUB_OWNER));

        return clubConverter.toClubResponses(savedClub, creator);
    }

    private static Long getMemberId(MemberDetails member) {
        if (member == null) {
            throw new BaseException(BaseResponseStatus.UNREGISTERD_MEMBER);
        }
        return member.getId();
    }

    //소모임 삭제
    public void delete(MemberDetails member, Long clubId) {
        clubRepository.deleteById(clubId);
    }

    //소모임 가입
    public ClubApplicationResponse joinClub(Long memberId, Long clubId, ClubApplicationRequest request) {
        Club club = validateClub(clubId);
        Member member = validateMember(memberId);

        if (club.getMemberId().equals(memberId)) {
            throw new BaseException(BaseResponseStatus.OWNER_CANNOT_JOIN_CLUB);
        }

        Optional<ClubMember> existingClubMember = clubMemberRepository.findByClubIdAndMemberId(clubId, memberId);
        ClubMember clubMember;
        if (existingClubMember.isPresent()) {
            clubMember = existingClubMember.get();
        } else {
            clubMember = clubConverter.toClubMember(club, member, request);
            clubMemberRepository.save(clubMember);
        }

        List<ClubAnswer> answers = new ArrayList<>();
        if (request.getClubAnswers() != null && !request.getClubAnswers().isEmpty()) {
            answers = request.getClubAnswers().stream()
                    .map(answerRequest -> clubConverter.toClubAnswerEntity(answerRequest, memberId, clubId, clubMember.getId()))
                    .collect(Collectors.toList());
            clubAnswerRepository.saveAll(answers);
        }
        return clubConverter.toClubApplicationResponse(clubMember, answers, club);
    }

    // 소모임 가입 조회
    public List<ClubApplicationResponse> findClubApplications(MemberDetails member, Long clubId) {
        Long memberId = member != null ? member.getMember().getId() : null;
        Club club = validateClub(clubId);

        // 소모임을 생성한 사람인 경우 + 가입한 사람이 소모임 조회
        if (club.getMemberId().equals(memberId)) {
            return clubMemberRepository.findByClubId(clubId).stream()
                    .map(clubMember -> {
                        List<ClubAnswer> answers = clubAnswerRepository.findByMemberIdAndClubId(clubMember.getMemberId(), clubId);
                        return clubConverter.toClubApplicationResponse(clubMember, answers, club);
                    })
                    .collect(Collectors.toList());
        } else {
            ClubMember clubMember = clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_CLUB_MEMBER));

            List<ClubAnswer> answers = clubAnswerRepository.findByMemberIdAndClubId(memberId, clubId);
            return Collections.singletonList(clubConverter.toClubApplicationResponse(clubMember, answers, club));
        }
    }

    public List<ClubApplicationResponse> findMyClubApplicationList(MemberDetails member) {
        Long memberId = member.getMember().getId();
        List<ClubMember> clubMembers = clubMemberRepository.findByMemberId(memberId);

        return clubMembers.stream()
                .map(clubMember -> {
                    List<ClubAnswer> clubAnswers = clubAnswerRepository.findByMemberId(clubMember.getId());
                    Club club = validateClub(clubMember.getClubId());
                    return clubConverter.toClubApplicationResponse(clubMember, clubAnswers, club);
                })
                .collect(Collectors.toList());
    }

    private Member validateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.UNREGISTERD_MEMBER));
        return member;
    }

    private Club validateClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_CLUB_ID));
        return club;
    }

    public String likeClub(Long memberId, Long clubId) {
        boolean duplicate = clubRepository.existsByMemberIdAndClubId(memberId, clubId);

        if (duplicate) {
            clubRepository.deleteByMemberIdAndClubId(memberId, clubId);
            return "deleted";
        } else {
            ClubLike clubLike = clubConverter.toLikeEntity(memberId, clubId);
            clubLikeRepository.save(clubLike);
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_CLUB_ID));
            return "liked";
        }
    }

    public Page<ClubLikeResponse> findLikeClub(MemberDetails member, Pageable pageable) {
        Long memberId = member.getId();

        Page<ClubLike> clubLikes = clubRepository.findClubLikes(memberId, pageable);

        return clubLikes.map(clubLike -> {
            Club club = clubRepository.findById(clubLike.getClubId()).orElseThrow((null));
            ClubMeeting clubMeeting = clubMeetingRepository.findFirstByClubIdOrderByDateDesc(club.getId()).orElse(null);
            return clubConverter.toLikeResponse(clubLike, club, clubMeeting);
        });
    }

}
