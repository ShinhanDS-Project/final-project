package com.merge.final_project.user.users;

import com.merge.final_project.user.users.dto.MicroTrackingDTO;
import com.merge.final_project.user.users.dto.UserTransactionResponseDTO;
import com.merge.final_project.user.users.dto.UserWalletResponseDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import com.merge.final_project.user.users.dto.support.*;

import java.util.List;

public interface UserService {

    //1. 로그인 용도
    public String login(UserLoginRequestDTO dto);

    //2.로그인 -부가기능 1: 아이디 찾기 (핸드폰 번호+ 이름)
    public EmailResponseDTO findEmail(String phone, String name);
    //3. 비밀번호 기능
    void requestPasswordReset(ChangePasswordRequestDTO dto);

    void confirmPasswordCode(String email, String code);

    void editPassword(Long userNo, EditPasswordDTO dto);

    // 3단계: 로그인 전 최종 비밀번호 재설정

    void resetPassword(ChangeResetPasswordRequestDTO dto) ;

    MyInfoResponseDTO getMyInfo(Long userNo);

    void updateMyInfo(Long userNo, UpdateMyInfoRequestDTO dto);

  //  List<MyDonationResponseDTO> getMyDonations(Long userNo);

    MicroTrackingDTO showMicroTracking(Long campaignNo);
    //4. 마이페이지 -1. 개인정보 불러오기 : 지갑
    UserWalletResponseDTO showUserWalletInfo(Long userNo);
    List<UserTransactionResponseDTO> showWalletTokenTrans(Long userNo);

}
