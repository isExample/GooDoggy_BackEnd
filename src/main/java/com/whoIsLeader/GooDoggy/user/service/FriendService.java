package com.whoIsLeader.GooDoggy.user.service;

import com.whoIsLeader.GooDoggy.subscription.DTO.PersonalRes;
import com.whoIsLeader.GooDoggy.subscription.service.PersonalService;
import com.whoIsLeader.GooDoggy.user.DTO.FriendRes;
import com.whoIsLeader.GooDoggy.user.entity.FriendEntity;
import com.whoIsLeader.GooDoggy.user.entity.UserEntity;
import com.whoIsLeader.GooDoggy.user.repository.FriendRepository;
import com.whoIsLeader.GooDoggy.user.repository.UserRepository;
import com.whoIsLeader.GooDoggy.util.BaseException;
import com.whoIsLeader.GooDoggy.util.BaseResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private UserRepository userRepository;
    private FriendRepository friendRepository;

    private UserService userService;
    private PersonalService personalService;

    public FriendService(UserRepository userRepository, FriendRepository friendRepository, UserService userService, PersonalService personalService){
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;

        this.userService = userService;
        this.personalService = personalService;
    }

    public void requestFriend(String id, HttpServletRequest request) throws BaseException {
        UserEntity user = this.userService.getSessionUser(request);
        if(user.getId().equals(id)){
            throw new BaseException(BaseResponseStatus.INVALID_FRIEND_REQUEST);
        }
        Optional<UserEntity> optional2 = this.userRepository.findById(id);
        if(optional2.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_ID);
        }
        if(optional2.get().getStatus().equals("inactive")){
            throw new BaseException(BaseResponseStatus.INACTIVE_USER);
        }
        Optional<FriendEntity> optionalReq = this.friendRepository.findByReqUserIdxAndResUserIdx(user, optional2.get());
        Optional<FriendEntity> optionalRes = this.friendRepository.findByReqUserIdxAndResUserIdx(optional2.get(), user);
        if(!optionalReq.isEmpty()){
            if(optionalReq.get().getStatus().equals("inactive")){
                throw new BaseException(BaseResponseStatus.EXIST_USER_REQUEST);
            }
            else{
                throw new BaseException(BaseResponseStatus.ALREADY_FRIEND);
            }
        }
        if(!optionalRes.isEmpty()){
            if(optionalRes.get().getStatus().equals("inactive")){
                throw new BaseException(BaseResponseStatus.EXIST_FRIEND_REQUEST);
            }
            else{
                throw new BaseException(BaseResponseStatus.ALREADY_FRIEND);
            }
        }
        FriendEntity friendEntity = FriendEntity.builder()
                .userEntity1(user)
                .userEntity2(optional2.get())
                .status("inactive")
                .build();
        try{
            this.friendRepository.save(friendEntity);
        } catch (Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_INSERT_ERROR);
        }
    }

    public String acceptFriend(Long friendIdx, HttpServletRequest request) throws BaseException{
        UserEntity user = this.userService.getSessionUser(request);
        Optional<FriendEntity> optional = this.friendRepository.findByFriendIdx(friendIdx);
        if(optional.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_FRIENDIDX);
        }
        try{
            optional.get().changeStatus("active");
            this.friendRepository.save(optional.get());
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_PATCH_ERROR);
        }
        return optional.get().getReqUserIdx().getId();
    }

    public String rejectFriend(Long friendIdx, HttpServletRequest request) throws BaseException{
        UserEntity user = this.userService.getSessionUser(request);
        Optional<FriendEntity> optional = this.friendRepository.findByFriendIdx(friendIdx);
        if(optional.isEmpty()){
            throw new BaseException(BaseResponseStatus.NON_EXIST_FRIENDIDX);
        }
        try{
            this.friendRepository.delete(optional.get());
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_DELETE_ERROR);
        }
        return optional.get().getReqUserIdx().getId();
    }

    public void deleteFriend(List<Long> friendIdxList, HttpServletRequest request) throws BaseException{
        UserEntity user = this.userService.getSessionUser(request);
        for(Long temp : friendIdxList){
            Optional<FriendEntity> friendEntity = this.friendRepository.findByFriendIdx(temp);
            if(friendEntity.isEmpty()){
                throw new BaseException(BaseResponseStatus.NON_EXIST_FRIENDIDX);
            }
            try{
                this.friendRepository.delete(friendEntity.get());
            } catch (Exception e) {
                throw new BaseException(BaseResponseStatus.DATABASE_DELETE_ERROR);
            }
        }
    }

    public List<FriendRes.FriendInfo> getFriendList(HttpServletRequest request) throws BaseException{ //?????? ?????? ??????
        UserEntity user = this.userService.getSessionUser(request);

        List<FriendEntity> friendEntityList = this.friendRepository.findAllByReqUserIdxOrResUserIdx(user, user);
        List<FriendRes.FriendInfo> friendInfoList = new ArrayList<>();
        for(FriendEntity temp : friendEntityList){
            if(temp.getStatus().equals("active")){
                FriendRes.FriendInfo friendInfo = new FriendRes.FriendInfo();
                if(temp.getReqUserIdx().equals(user)){
                    friendInfo.setFriendIdx(temp.getResUserIdx().getUserIdx());
                }
                else{
                    friendInfo.setFriendIdx(temp.getReqUserIdx().getUserIdx());
                }
                friendInfo.setProfileImg(temp.getReqUserIdx().getProfileImg());
                if(temp.getResUserIdx().getId().equals(user.getId())){
                    friendInfo.setId(temp.getReqUserIdx().getId());
                    friendInfo.setBriefInfo(getBriefSubscription(temp.getReqUserIdx()));
                }
                else{
                    friendInfo.setId(temp.getResUserIdx().getId());
                    friendInfo.setBriefInfo(getBriefSubscription(temp.getResUserIdx()));
                }
                friendInfoList.add(friendInfo);
            }
        }
        return friendInfoList;
    }

    public FriendRes.BriefInfo getBriefSubscription(UserEntity user) throws  BaseException{
        List<PersonalRes.subscription> subscriptionList = this.personalService.getSubscriptionList(user);
        int count = subscriptionList.size();
        List<FriendRes.SubInfo> subInfoList= new ArrayList<>();
        for(int i = count - 1; (i >= 0) && (i >= count - 3); i--) {
            FriendRes.SubInfo subInfo = new FriendRes.SubInfo();
            subInfo.setServiceName(subscriptionList.get(i).getServiceName());
            subInfo.setProfileImg(subscriptionList.get(i).getProfileImg());
            subInfoList.add(subInfo);
        }
        FriendRes.BriefInfo briefInfo = new FriendRes.BriefInfo();
        briefInfo.setNum(Long.valueOf(count));
        briefInfo.setSubInfoList(subInfoList);
        return briefInfo;
    }

    public List<FriendRes.FriendInfo> getReqFriendList(HttpServletRequest request) throws  BaseException{ //????????? ??????
        UserEntity user = this.userService.getSessionUser(request);

        List<FriendEntity> friendEntityList = this.friendRepository.findAllByResUserIdx(user);
        List<FriendRes.FriendInfo> friendInfoList = new ArrayList<>();
        for(FriendEntity temp : friendEntityList){
            if(temp.getStatus().equals("inactive")){
                FriendRes.FriendInfo friendInfo = new FriendRes.FriendInfo();
                friendInfo.setFriendIdx(temp.getFriendIdx());
                friendInfo.setId(temp.getReqUserIdx().getId());
                friendInfo.setProfileImg(temp.getReqUserIdx().getProfileImg());
                friendInfoList.add(friendInfo);
            }
        }
        return friendInfoList;
    }

    public List<FriendRes.FriendInfo> getResFriendList(HttpServletRequest request) throws  BaseException{ //????????? ??????
        UserEntity user = this.userService.getSessionUser(request);

        List<FriendEntity> friendEntityList = this.friendRepository.findAllByReqUserIdx(user);
        List<FriendRes.FriendInfo> friendInfoList = new ArrayList<>();
        for(FriendEntity temp : friendEntityList){
            if(temp.getStatus().equals("inactive")){
                FriendRes.FriendInfo friendInfo = new FriendRes.FriendInfo();
                friendInfo.setFriendIdx(temp.getFriendIdx());
                friendInfo.setId(temp.getResUserIdx().getId());
                friendInfo.setProfileImg(temp.getResUserIdx().getProfileImg());
                friendInfoList.add(friendInfo);
            }
        }
        return friendInfoList;
    }
}
