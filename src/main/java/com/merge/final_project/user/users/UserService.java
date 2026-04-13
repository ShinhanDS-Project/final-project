package com.merge.final_project.user.users;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import com.merge.final_project.user.verify.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    //4. 마이페이지 -1. 개인정보 불러오기 :
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
    //6. 기부내역 -1. 기부 증서 조회하기
    //7. 마이페이지 -2. 찜 등록
    //8. 마이페이지 -3. 찜 해제
    //9. 마이페이지 4- 찜 조회

}
