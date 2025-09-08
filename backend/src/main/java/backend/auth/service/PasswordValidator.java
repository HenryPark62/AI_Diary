package backend.auth.service;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    /**
     * 비밀번호 유효성 검사
     * - 길이: 8~12자
     * - 특수문자 1개 이상 포함
     * - 2가지 이상 조합 (숫자+특수문자, 영어+특수문자, 숫자+영어+특수문자 등)
     * - 공백 불가
     * - 이메일의 영어 4자리와 겹치지 않도록
     */
    public boolean isValidPassword(String password, String email) {
        if (password == null || email == null) {
            return false;
        }

        // 기본 길이 체크
        if (password.length() < 8 || password.length() > 12) {
            return false;
        }

        // 공백 체크
        if (password.contains(" ")) {
            return false;
        }

        // 특수문자 포함 체크
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return false;
        }

        // 이메일에서 영어 4자리 추출 및 겹치는지 체크
        String emailLocal = email.split("@")[0];
        String emailAlpha = emailLocal.replaceAll("[^a-zA-Z]", "");
        if (emailAlpha.length() >= 4) {
            String emailPrefix = emailAlpha.substring(0, 4).toLowerCase();
            if (password.toLowerCase().contains(emailPrefix)) {
                return false;
            }
        }

        // 조합 체크 (최소 2가지 조합)
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        int combinationCount = 0;
        if (hasDigit) combinationCount++;
        if (hasLetter) combinationCount++;
        if (hasSpecial) combinationCount++;

        return combinationCount >= 2;
    }

    public String getPasswordRules() {
        return "비밀번호는 다음 조건을 만족해야 합니다:\n" +
                "- 8~12자 길이\n" +
                "- 특수문자 1개 이상 포함\n" +
                "- 숫자, 영어, 특수문자 중 2가지 이상 조합\n" +
                "- 공백 불가\n" +
                "- 이메일의 영어 4자리와 겹치지 않아야 함";
    }
}