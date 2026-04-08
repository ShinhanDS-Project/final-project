package com.merge.final_project.user.users;

import com.merge.final_project.user.users.dto.support.EmailResponseDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;

public interface UserService {
    //1. 로그인 용도
    public String login(UserLoginRequestDTO dto);

    //2.로그인 -부가기능 1: 아이디 찾기 (핸드폰 번호+ 이름)
    public EmailResponseDTO findEmail(String phone, String name);
    //3.로그인- 부가기능 2: 비밀번호 재설정 ( 로그인전에 비밀번호를 잃어버렸을 경우에 )

    //4.마이페이지 -부가기능 : 비밀번호 변경 (마이페이지에서 변경하는 경우)

    //4. 마이페이지 -1. 개인정보 불러오기 :
    //5. 마이페이지 -1. 계정별 기부 결과 불러오기:
    //6. 기부내역 -1. 기부 증서 조회하기
    //7. 마이페이지 -2. 찜 등록
    //8. 마이페이지 -3. 찜 해제
    //9. 마이페이지 4- 찜 조회

}
