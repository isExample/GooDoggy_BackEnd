package com.whoIsLeader.GooDoggy.subscription.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.whoIsLeader.GooDoggy.subscription.DTO.GroupRes;
import com.whoIsLeader.GooDoggy.subscription.DTO.MemberRes;
import com.whoIsLeader.GooDoggy.subscription.entity.Category;
import com.whoIsLeader.GooDoggy.subscription.entity.GroupEntity;
import com.whoIsLeader.GooDoggy.subscription.entity.UserGroupEntity;
import com.whoIsLeader.GooDoggy.subscription.repository.GroupRepository;
import com.whoIsLeader.GooDoggy.subscription.repository.UserGroupRepository;
import com.whoIsLeader.GooDoggy.user.entity.UserEntity;
import com.whoIsLeader.GooDoggy.user.repository.UserRepository;
import com.whoIsLeader.GooDoggy.util.BaseException;
import com.whoIsLeader.GooDoggy.util.BaseResponseStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private UserGroupRepository userGroupRepository;

    private UserGroupService userGroupService;

    public MemberService(UserRepository userRepository, GroupRepository groupRepository, UserGroupService userGroupService,
                         UserGroupRepository userGroupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;

        this.userGroupService = userGroupService;
    }

    public List<MemberRes.subscription> getSubscriptionList(HttpServletRequest request) throws BaseException {
        HttpSession session = request.getSession(false);
        if(session == null){
            throw new BaseException(BaseResponseStatus.NON_EXIST_SESSION);
        }
        Long userIdx = (Long)session.getAttribute("LOGIN_USER");
        Optional<UserEntity> optional = this.userRepository.findByUserIdx(userIdx);
        if(optional.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_USERIDX);
        }
        if(optional.get().getStatus().equals("inactive")){
            throw new BaseException(BaseResponseStatus.INACTIVE_USER);
        }
        List<GroupEntity> groupEntityList = groupRepository.findAllByStatus("active");
        List<MemberRes.subscription> subscriptionList = new ArrayList<>();
        for(GroupEntity temp : groupEntityList){
            if(!this.userGroupRepository.findByUserIdxAndGroupIdx(optional.get(), temp).isEmpty()){
                continue;
            }
            if(temp.getJoinNum() < temp.getTargetNum()){
                MemberRes.subscription subscription = new MemberRes.subscription();
                subscription.setGroupIdx(temp.getGroupIdx());
                subscription.setServiceName(temp.getServiceName());
                subscription.setPrice(temp.getPrice());
                subscription.setFirstDayOfPayment(temp.getFirstDayOfPayment());
                subscription.setJoinNum(temp.getJoinNum());
                subscription.setTargetNum(temp.getTargetNum());
                subscription.setCategory(temp.getCategory());
                subscriptionList.add(subscription);
            }
        }
        if(subscriptionList.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_MEMBER);
        }
        return subscriptionList;
    }

    public List<MemberRes.subscription> getCategorizedList(Category category, HttpServletRequest request) throws BaseException {
        List<MemberRes.subscription> tempList = getSubscriptionList(request);
        List<MemberRes.subscription> subscriptionList = new ArrayList<>();
        for(MemberRes.subscription temp : tempList){
            if(temp.getCategory().equals(category)){
                subscriptionList.add(temp);
            }
        }
        if(subscriptionList.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_MEMBER);
        }
        return subscriptionList;
    }

    public void joinGroupSubscription(Long groupIdx, HttpServletRequest request) throws BaseException {
        HttpSession session = request.getSession(false);
        if(session == null){
            throw new BaseException(BaseResponseStatus.NON_EXIST_SESSION);
        }
        Long userIdx = (Long)session.getAttribute("LOGIN_USER");
        Optional<UserEntity> optional = this.userRepository.findByUserIdx(userIdx);
        if(optional.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_USERIDX);
        }
        if(optional.get().getStatus().equals("inactive")){
            throw new BaseException(BaseResponseStatus.INACTIVE_USER);
        }

        Optional<GroupEntity> groupEntity = this.groupRepository.findByGroupIdx(groupIdx);
        if(groupEntity.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_GROUPIDX);
        }

        Optional<UserGroupEntity> optional1 = this.userGroupRepository.findByUserIdxAndGroupIdx(optional.get(), groupEntity.get());
        if(!optional1.isEmpty()){
            throw new BaseException(BaseResponseStatus.ALREADY_JOINED);
        }

        UserGroupEntity userGroupEntity = UserGroupEntity.builder()
                .userEntity(optional.get())
                .groupEntity(groupEntity.get())
                .build();

        if(groupEntity.get().getJoinNum() >= groupEntity.get().getTargetNum()){
            throw new BaseException(BaseResponseStatus.FAILED_TO_JOIN_GROUP);
        }
        groupEntity.get().setJoinNum(groupEntity.get().getJoinNum() + 1);
        try{
            this.userGroupRepository.save(userGroupEntity);
            this.groupRepository.save(groupEntity.get());
        } catch (Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_INSERT_ERROR);
        }

    }
}