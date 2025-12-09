package com.ssafy.yamyam_coach.repository.food;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import com.ssafy.yamyam_coach.domain.food.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.ssafy.yamyam_coach.repository.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

class FoodRepositoryTest extends IntegrationTestSupport {

    @Autowired
    FoodRepository foodRepository;

    @DisplayName("음식을 저장할 수 있다.")
    @Test
    void insert() {
        //given
        Food food = createDummyFood();

        //when
        foodRepository.insert(food);
        Food findFood = foodRepository.findById(food.getId()).orElse(null);

        //then
        assertThat(findFood).isNotNull();
        assertFoodEquals(findFood, food);
    }

    @DisplayName("이름에 '닭'이 포함된 모든 음식을 조회할 수 있다.")
    @Test
    void findByNameLike() {
        //given
        List<Food> dummyFoods = createDummyFoods10();
        dummyFoods.forEach(dummyFood -> foodRepository.insert(dummyFood));

        //when
        List<Food> findFoods = foodRepository.findByNameLike("닭");

        //then
        assertThat(findFoods).hasSize(3)
                .extracting("name")
                .containsExactlyInAnyOrder("닭가슴살", "닭다리살", "닭안심");
    }

    @DisplayName("전달된 음식 ID 중 실제 DB에 존재하는 개수를 반환한다.")
    @Test
    void countExistingIds() {
        //given
        List<Food> dummyFoods = createDummyFoods10();
        dummyFoods.forEach(food -> foodRepository.insert(food));

        List<Long> existingIds = List.of(
                dummyFoods.get(0).getId(),
                dummyFoods.get(1).getId(),
                dummyFoods.get(2).getId()
        );

        // 존재하지 않는 ID 추가
        List<Long> mixedIds = new ArrayList<>(existingIds);
        mixedIds.add(999999L);
        mixedIds.add(888888L);

        //when
        int count = foodRepository.countExistingIds(new HashSet<>(mixedIds));

        //then
        assertThat(count).isEqualTo(3);
    }

    @DisplayName("모든 ID가 존재할 때 전체 개수를 반환한다.")
    @Test
    void countExistingIds_allExist() {
        //given
        List<Food> dummyFoods = createDummyFoods10();
        dummyFoods.forEach(food -> foodRepository.insert(food));

        List<Long> allIds = dummyFoods.stream()
                .map(Food::getId)
                .toList();

        //when
        int count = foodRepository.countExistingIds(new HashSet<>(allIds));

        //then
        assertThat(count).isEqualTo(10);
    }

    @DisplayName("모든 ID가 존재하지 않을 때 0을 반환한다.")
    @Test
    void countExistingIds_noneExist() {
        //given
        List<Long> nonExistingIds = List.of(999999L, 888888L, 777777L);

        //when
        int count = foodRepository.countExistingIds(new HashSet<>(nonExistingIds));

        //then
        assertThat(count).isEqualTo(0);
    }

    @DisplayName("빈 set을 전달하면 0을 반환한다.")
    @Test
    void countExistingIds_emptyList() {
        //given
        List<Long> emptyIds = List.of();

        //when
        int count = foodRepository.countExistingIds(new HashSet<>(emptyIds));

        //then
        assertThat(count).isEqualTo(0);
    }

    private void assertFoodEquals(Food actual, Food expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getCategory()).isEqualTo(expected.getCategory());
        assertThat(actual.getBaseUnit()).isEqualTo(expected.getBaseUnit());
        assertThat(actual.getEnergyPer100()).isEqualTo(expected.getEnergyPer100());
        assertThat(actual.getProteinPer100()).isEqualTo(expected.getProteinPer100());
        assertThat(actual.getFatPer100()).isEqualTo(expected.getFatPer100());
        assertThat(actual.getCarbohydratePer100()).isEqualTo(expected.getCarbohydratePer100());
        assertThat(actual.getSugarPer100()).isEqualTo(expected.getSugarPer100());
        assertThat(actual.getSodiumPer100()).isEqualTo(expected.getSodiumPer100());
        assertThat(actual.getCholesterolPer100()).isEqualTo(expected.getCholesterolPer100());
        assertThat(actual.getSaturatedFatPer100()).isEqualTo(expected.getSaturatedFatPer100());
        assertThat(actual.getTransFatPer100()).isEqualTo(expected.getTransFatPer100());
    }
}