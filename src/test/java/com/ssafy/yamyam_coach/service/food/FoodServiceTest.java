package com.ssafy.yamyam_coach.service.food;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.food.Food;
import com.ssafy.yamyam_coach.repository.food.FoodRepository;
import com.ssafy.yamyam_coach.service.food.response.SearchFoodServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.ssafy.yamyam_coach.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class FoodServiceTest extends IntegrationTestSupport {

    @Autowired
    FoodService foodService;

    @Autowired
    FoodRepository foodRepository;

    @Nested
    @DisplayName("FindByNameLike")
    class FindByNameLike {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {

            @DisplayName("name 을 이용해서 이름에 name 이 포함되는 음식들을 조회할 수 있다.")
            @Test
            void findByNameLike() {
                // given
                List<Food> foods = createDummyFoods10();
                foods.forEach(foodRepository::insert);

                // when
                List<SearchFoodServiceResponse> nameLike닭 = foodService.searchFood("닭");

                // then
                assertThat(nameLike닭).hasSize(3)
                        .extracting(SearchFoodServiceResponse::getName)
                        .isSortedAccordingTo(String::compareTo);
            }

            @DisplayName("name 을 포함하는 이름을 가진 음식이 없을 경우 빈 리스트를 반환한다.")
            @Test
            void notExistsNameLike() {
                // given
                List<Food> foods = createDummyFoods10();
                foods.forEach(foodRepository::insert);

                // when
                List<SearchFoodServiceResponse> notExistsNameResult = foodService.searchFood("없는단어");

                // then
                assertThat(notExistsNameResult).isEmpty();
            }
        }

    }
}