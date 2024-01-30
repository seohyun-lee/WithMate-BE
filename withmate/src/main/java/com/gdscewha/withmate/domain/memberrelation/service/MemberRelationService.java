package com.gdscewha.withmate.domain.memberrelation.service;

import com.gdscewha.withmate.common.response.exception.ErrorCode;
import com.gdscewha.withmate.common.response.exception.MatchingException;
import com.gdscewha.withmate.common.response.exception.MemberRelationException;
import com.gdscewha.withmate.domain.matching.entity.Matching;
import com.gdscewha.withmate.domain.member.entity.Member;
import com.gdscewha.withmate.domain.member.repository.MemberRepository;
import com.gdscewha.withmate.domain.memberrelation.entity.MemberRelation;
import com.gdscewha.withmate.domain.memberrelation.repository.MemberRelationRepository;
import com.gdscewha.withmate.domain.relation.entity.Relation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MemberRelationService {

    private final MemberRelationRepository mRRepository;
    private final MemberRepository memberRepository;

    // Member의 모든 MR을 반환한다
    public List<MemberRelation> findAllMROfMember(Member member) {
        List<MemberRelation> mRList = mRRepository.findAllByMember(member);
        if (mRList != null && !mRList.isEmpty()) {
            return mRList;
        }
        return null; // 찾지 못했음
    }

    // Member에게 가장 최신인 MR 하나를 반환한다
    public MemberRelation findLastMROfMember(Member member) {
        List<MemberRelation> mRList = findAllMROfMember(member);
        if (mRList == null) {
            return null;
        }
        MemberRelation lastMR = mRList.get(mRList.size() - 1);
        if (lastMR.getRelation().getIsProceed() == true) { // 지속중이라면
            return lastMR;
        }
        return null; // 찾지 못했음
    }

    // MR 두 개 만들고 저장, 두 Member의 isRelationed도 true로 설정
    public void createMemberRelationPair(List<Matching> matchingList, Relation relation){
        if (matchingList.size() != 2)
            throw new MatchingException(ErrorCode.MATCHING_NOT_FOUND);

        Matching matching1 = matchingList.get(0);
        Member member1 = matching1.getMember();
        Matching matching2 = matchingList.get(1);
        Member member2 = matching1.getMember();
        
        MemberRelation myMR = MemberRelation.builder()
                .goal(matching1.getGoal())
                .category(matching1.getCategory())
                // message is nullable
                .member(member1)
                .relation(relation)
                .build();
        mRRepository.save(myMR);
        member1.setIsRelationed(true);
        memberRepository.save(member1);
        
        MemberRelation mateMR = MemberRelation.builder()
                .goal(matching2.getGoal())
                .category(matching2.getCategory())
                // message is nullable
                .member(member2)
                .relation(relation)
                .build();
        mRRepository.save(mateMR);
        member2.setIsRelationed(true);
        memberRepository.save(member2);
    }

    // Relation이 생성된 후, Relation으로 두 MR을 찾고 그중 메이트의 MR을 반환
    public MemberRelation findMROfMateByRelation(MemberRelation myMR, Relation relation) {
        List<MemberRelation> mRPair = mRRepository.findAllByRelation(relation);
        if (mRPair.size() != 2) {
            throw new MemberRelationException(ErrorCode.MEMBERRELATION_NOT_FOUND); // MR 쌍을 찾지 못했음
        }
        if (mRPair.get(0) != myMR)
            return mRPair.get(0);
        else
            return mRPair.get(1);
    }

    // Relation 삭제 시, Relation으로 두 MR을 찾고 둘의 isRelationed를 false로 변경
    public void changeIsRelationedOfMembers(Relation relation) {
        List<MemberRelation> mRPair = mRRepository.findAllByRelation(relation);
        if (mRPair.size() != 2) {
            throw new MemberRelationException(ErrorCode.MEMBERRELATION_NOT_FOUND);
        }
        Member member1 = mRPair.get(0).getMember();
        Member member2 = mRPair.get(1).getMember();
        member1.setIsRelationed(false);
        member2.setIsRelationed(false);
        memberRepository.save(member1);
        memberRepository.save(member2);
    }

    // Member의 Goal 업데이트
    public MemberRelation updateMRGoal(Member member, String newGoal) {
        MemberRelation memberRelation = findLastMROfMember(member);
        if (memberRelation == null)
            throw new MemberRelationException(ErrorCode.MEMBERRELATION_NOT_FOUND);
        memberRelation.setGoal(newGoal);
        return mRRepository.save(memberRelation);
    }
    // Member의 Message 업데이트
    public MemberRelation updateMRMessage(Member member, String newMessage) {
        MemberRelation memberRelation = findLastMROfMember(member);
        if (memberRelation == null)
            throw new MemberRelationException(ErrorCode.MEMBERRELATION_NOT_FOUND);
        memberRelation.setMessage(newMessage);
        return mRRepository.save(memberRelation);
    }
}
