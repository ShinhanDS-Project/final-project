package com.merge.final_project.global.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GiveN Token API")
                        .description("블록체인 기반 기부 플랫폼 GiveN Token의 REST API 명세입니다.\n\n" +
                                "## 인증 방식\n" +
                                "- **관리자**: `POST /admin/auth/login` → Bearer 토큰 발급 후 Authorize 버튼에 입력\n" +
                                "- **기부단체**: `POST /api/foundation/login` → Bearer 토큰 발급 후 Authorize 버튼에 입력\n" +
                                "- **일반 사용자**: `POST /api/auth/login/user/local` → Bearer 토큰 발급 후 Authorize 버튼에 입력\n" +
                                "- **수혜자**: `POST /api/v1/beneficiary/signin` → httpOnly 쿠키로 발급")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // 태그 순서 직접 지정 (여기 선언 순서 = Swagger UI 표시 순서)
                // ── 관리자 ──
                .addTagsItem(new Tag().name("관리자 인증").description("관리자 로그인/로그아웃 API"))
                .addTagsItem(new Tag().name("관리자 대시보드").description("관리자 대시보드 요약·차트·로그 조회 API"))
                .addTagsItem(new Tag().name("관리자 기부단체 관리").description("관리자 기부단체 조회·승인·반려·활성화·비활성화 API"))
                .addTagsItem(new Tag().name("관리자 캠페인 관리").description("관리자 캠페인 승인/반려 API"))
                .addTagsItem(new Tag().name("관리자 활동 보고서 관리").description("관리자 활동 보고서 조회·승인·반려 API"))
                .addTagsItem(new Tag().name("관리자 회원 관리").description("관리자 회원 조회·활성화·비활성화 API"))
                .addTagsItem(new Tag().name("관리자 환금 관리").description("관리자 토큰 환금(현금화) 처리 API"))
                .addTagsItem(new Tag().name("관리자 지갑 관리").description("관리자 플랫폼 HOT 지갑 조회 API"))
                .addTagsItem(new Tag().name("관리자 활동 로그").description("관리자 활동 로그 조회 API"))
                .addTagsItem(new Tag().name("관리자 메일 발송 내역").description("관리자 이메일 발송 내역 조회 API"))
                .addTagsItem(new Tag().name("관리자 SSE 실시간 알림").description("관리자 대시보드 실시간 승인 요청 알림 SSE API"))
                // ── 기부단체 ──
                .addTagsItem(new Tag().name("기부단체").description("기부단체 가입·로그인·마이페이지·캠페인·정산·환금 API"))
                // ── 캠페인 ──
                .addTagsItem(new Tag().name("캠페인").description("캠페인 목록 조회·등록·상세 조회 API"))
                // ── 일반 사용자 ──
                .addTagsItem(new Tag().name("일반 사용자 인증").description("일반 사용자 로그인·로그아웃·소셜 정보 조회 API"))
                .addTagsItem(new Tag().name("일반 사용자 회원가입").description("로컬·소셜(Google) 회원가입 API"))
                .addTagsItem(new Tag().name("이메일 인증").description("회원가입 이메일 인증 코드 발송·확인 API"))
                .addTagsItem(new Tag().name("일반 사용자 마이페이지").description("사용자 마이페이지 부가기능 API"))
                // ── 수혜자 ──
                .addTagsItem(new Tag().name("수혜자").description("수혜자 회원가입·로그인·정보 조회·수정 API"))
                // ── 활동 보고서 ──
                .addTagsItem(new Tag().name("활동 보고서").description("수혜자 활동 보고서 조회·제출·수정 API"))
                // ── 기부/결제 ──
                .addTagsItem(new Tag().name("기부 내역").description("사용자 기부 내역 조회 API"))
                .addTagsItem(new Tag().name("결제").description("기부 결제(카카오페이 등) 준비·확인 API"))
                .addTagsItem(new Tag().name("환금(현금화)").description("기부단체 토큰 환금(현금화) 신청 API"))
                // ── 알림 ──
                .addTagsItem(new Tag().name("인앱 알림").description("사용자·기부단체·수혜자 인앱 알림 조회·읽음 처리 API"))
                // ── 블록체인 ──
                .addTagsItem(new Tag().name("블록체인 대시보드").description("블록체인 거래 내역·지갑·요약 조회 API (공개)"))
                .addTagsItem(new Tag().name("블록체인 전송").description("토큰 전송·결제 충전 API"))
                // ── 기타 ──
                .addTagsItem(new Tag().name("정산 테스트").description("정산 배치 수동 실행 API (개발·테스트용)"));
    }
}