package com.mxxdone.miniproject.util;

import com.mxxdone.miniproject.domain.Role;
import com.mxxdone.miniproject.domain.User;
import com.mxxdone.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("dev")
@Slf4j
public class TestDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    // "String..." : 전달받는 인자의 개수가 자유로움, 배열로 처리
    public void run(String... args) {

        // 1. 테스트용 사용자 확보 (작성자 정보)
        User testUser = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .nickname("관리자")
                        .email("test@test.com")
                        .build()));

        Long userId = testUser.getId(); // 작성자의 ID 추출

        // 2. category 테이블의 모든 NOT NULL 컬럼 채워서 생성
        jdbcTemplate.execute("""
                    INSERT INTO category (name, display_order, slug)
                    SELECT '테스트용', 1, 'test-category'
                    WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '테스트용')
                """);

        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM category WHERE name = '테스트용' LIMIT 1", Long.class);

        // 3. 기존 데이터 개수 확인
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post", Integer.class);

        // 4. 기존 데이터가 10만개 이하라면(테스트 데이터 10만개 삽입이 미완인 경우)
        if (count == null || count < 100000) {
            log.info(">>> 데이터 개수가 {}개입니다. 10만 건으로 재설정하기 위해 초기화를 시작합니다.", count);

            // 기존 데이터 싹 비우기 (ID 값도 1번부터 시작하게 초기화)
            jdbcTemplate.execute("TRUNCATE TABLE post RESTART IDENTITY CASCADE");

            long startTime = System.currentTimeMillis();

            String sql = "INSERT INTO post (title, content, user_id, category_id, is_deleted, like_count, view_count, created_at) " +
                    "VALUES (?, ?, ?, ?, false, 0, 0, NOW())";
            String longContent = getTestText(); // 테스트할 텍스트 가져오기

            // 1,000건씩 100번 끊어서 처리 (총 10만 건)
            for (int i = 0; i < 100; i++) {
                List<Object[]> batchArgs = new ArrayList<>();

                for (int j = 0; j < 1000; j++) {
                    int index = (i * 1000) + j;

                    // 인덱스 테스트를 위해 마지막 줄 끝에 숫자로 차별화
                    String contentWithNo = longContent + " No." + index;

                    // SQL의 '?' 순서대로 데이터를 배열에 담기
                    batchArgs.add(new Object[]{
                            "교통사고처리 특례법 No." + index,
                            contentWithNo,
                            userId,
                            categoryId
                    });
                }
                jdbcTemplate.batchUpdate(sql, batchArgs);

                if (i % 10 == 0) {
                    log.info(">>> {}% 완료... ({}건 삽입 완료)", i, (i * 1000));
                }
            }

            long endTime = System.currentTimeMillis();
            log.info(">>> 10만 건 삽입 완료, 소요 시간: {}ms", (endTime - startTime));
        } else {
            log.info(">>> 이미 데이터가 충분합니다.");
        }
    }

    private String getTestText() {
        return """
                교통사고처리 특례법 제3조(처벌의 특례)
                ② 차의 교통으로 제1항의 죄 중 업무상과실치상죄(業務上過실致傷罪) 또는 중과실치상죄(重過실致傷罪)와 「도로교통법」 제151조의 죄[7]를 범한 운전자에 대하여는 피해자의 명시적인 의사에 반하여 공소(公訴)를 제기할 수 없다. 다만, 차의 운전자가 제1항의 죄 중 업무상과실치상죄 또는 중과실치상죄를 범하고도 피해자를 구호(救護)하는 등 「도로교통법」 제54조제1항에 따른 조치를 하지 아니하고 도주하거나 피해자를 사고 장소로부터 옮겨 유기(遺棄)하고 도주한 경우, 같은 죄를 범하고 「도로교통법」 제44조제2항을 위반하여 음주측정 요구에 따르지 아니한 경우(운전자가 채혈 측정을 요청하거나 동의한 경우는 제외한다)와 다음 각 호의 어느 하나에 해당하는 행위로 인하여 같은 죄를 범한 경우에는 그러하지 아니하다.
                
                1. 「도로교통법」 제5조에 따른 신호기가 표시하는 신호 또는 교통정리를 하는 경찰공무원등의 신호를 위반하거나 통행금지 또는 일시정지를 내용으로 하는 안전표지가 표시하는 지시를 위반하여 운전한 경우
                2. 「도로교통법」 제13조제3항을 위반하여 중앙선을 침범하거나 같은 법 제62조를 위반하여 횡단, 유턴 또는 후진한 경우
                3. 「도로교통법」 제17조제1항 또는 제2항에 따른 제한속도를 시속 20킬로미터 초과하여 운전한 경우
                4. 「도로교통법」 제21조제1항, 제22조, 제23조에 따른 앞지르기의 방법ㆍ금지시기ㆍ금지장소 또는 끼어들기의 금지를 위반하거나 같은 법 제60조제2항에 따른 고속도로에서의 앞지르기 방법을 위반하여 운전한 경우
                5. 「도로교통법」 제24조에 따른 철길건널목 통과방법을 위반하여 운전한 경우
                6. 「도로교통법」 제27조제1항에 따른 횡단보도에서의 보행자 보호의무를 위반하여 운전한 경우
                7. 「도로교통법」 제43조, 「건설기계관리법」 제26조 또는 「도로교통법」 제96조를 위반하여 운전면허 또는 건설기계조종사면허를 받지 아니하거나 국제운전면허증을 소지하지 아니하고 운전한 경우.
                8. 「도로교통법」 제44조제1항을 위반하여 술에 취한 상태에서 운전을 하거나 같은 법 제45조를 위반하여 약물의 영향으로 정상적으로 운전하지 못할 우려가 있는 상태에서 운전한 경우
                9. 「도로교통법」 제13조제1항을 위반하여 보도(步道)가 설치된 도로의 보도를 침범하거나 같은 법 제13조제2항에 따른 보도 횡단방법을 위반하여 운전한 경우
                10. 「도로교통법」 제39조제3항에 따른 승객의 추락 방지의무를 위반하여 운전한 경우
                11. 「도로교통법」 제12조제3항에 따른 어린이 보호구역에서 같은 조 제1항에 따른 조치를 준수하고 어린이의 안전에 유의하면서 운전하여야 할 의무를 위반하여 어린이의 신체를 상해(傷害)에 이르게 한 경우
                12. 「도로교통법」 제39조제4항을 위반하여 자동차의 화물이 떨어지지 아니하도록 필요한 조치를 하지 아니하고 운전한 경우
                """;
    }
}