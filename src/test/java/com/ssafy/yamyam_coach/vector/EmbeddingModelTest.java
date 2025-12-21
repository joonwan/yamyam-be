package com.ssafy.yamyam_coach.vector;

import com.ssafy.yamyam_coach.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddingModelTest extends IntegrationTestSupport {

    @Autowired
    private EmbeddingModel embeddingModel;

    @DisplayName("Embedding Model이 실제로 벡터를 생성하는지 확인")
    @Test
    void embeddingTest() {
        // given
        String text1 = "치킨 맛있어";
        String text2 = "로봇";
        String text3 = "음식";

        // when
        float[] vector1 = embeddingModel.embed(text1);
        float[] vector2 = embeddingModel.embed(text2);
        float[] vector3 = embeddingModel.embed(text3);

        // then
        System.out.println("=== Embedding 결과 ===");
        System.out.println("치킨 맛있어 벡터 차원: " + vector1.length);
        System.out.println("치킨 맛있어 첫 10개 값: ");
        printVector(vector1, 10);

        System.out.println("\n로봇 벡터 차원: " + vector2.length);
        System.out.println("로봇 첫 10개 값: ");
        printVector(vector2, 10);

        System.out.println("\n음식 벡터 차원: " + vector3.length);
        System.out.println("음식 첫 10개 값: ");
        printVector(vector3, 10);

        // 벡터가 모두 0이 아닌지 확인
        assertThat(vector1).isNotEmpty();
        assertThat(vector2).isNotEmpty();
        assertThat(vector3).isNotEmpty();

        // 모든 값이 0이 아닌지 확인
        boolean allZero1 = true;
        for (float v : vector1) {
            if (v != 0.0f) {
                allZero1 = false;
                break;
            }
        }
        assertThat(allZero1).isFalse();

        // 서로 다른 텍스트는 다른 벡터를 생성하는지 확인
        assertThat(vector1).isNotEqualTo(vector2);

        // 코사인 유사도 계산
        double similarity12 = cosineSimilarity(vector1, vector2);
        double similarity13 = cosineSimilarity(vector1, vector3);
        double similarity23 = cosineSimilarity(vector2, vector3);

        System.out.println("\n=== 코사인 유사도 ===");
        System.out.println("치킨 맛있어 vs 로봇: " + similarity12);
        System.out.println("치킨 맛있어 vs 음식: " + similarity13);
        System.out.println("로봇 vs 음식: " + similarity23);

        // 치킨과 음식이 치킨과 로봇보다 유사도가 높아야 함
        System.out.println("\n=== 검증 ===");
        System.out.println("치킨 vs 음식이 치킨 vs 로봇보다 유사도가 높은가? " + (similarity13 > similarity12));
    }

    private void printVector(float[] vector, int limit) {
        for (int i = 0; i < Math.min(limit, vector.length); i++) {
            System.out.printf("%.4f ", vector[i]);
        }
        System.out.println("...");
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
