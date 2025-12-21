# 식품 영양성분 데이터베이스

## 개요
한국 식품의 영양성분 정보를 제공하는 공공 데이터베이스 접근 방법 안내입니다. 실제 파일 다운로드는 로그인 또는 API 키가 필요합니다.

## 포함된 문서

| 파일명 | 설명 |
|--------|------|
| `식품영양성분DB_접근방법.md` | 주요 데이터베이스 접근 방법 총정리 |

## 주요 데이터베이스 목록

### 1. 전국통합식품영양성분정보 (원재료)

**출처**: 식품의약품안전처
**URL**: https://www.data.go.kr/data/15100064/standard.do

**데이터 규모**
- 원재료성 식품, 가공식품의 영양성분 정보
- 약 40개 영양소 항목

**주요 항목**
- 기본: 식품코드, 식품명
- 3대 영양소: 에너지, 단백질, 지방, 탄수화물
- 세부: 당류, 식이섬유
- 무기질: 칼슘, 철, 인, 칼륨, 나트륨
- 비타민: A, B1, B2, C, D
- 지질: 콜레스테롤, 포화지방산

**파일 포맷**: XLS, XML, JSON, RDF, CSV

**접근 방법**
- 그리드 다운로드: 5만 건 제한
- 전체 데이터: Open API 활용 (API 키 발급 필요)

---

### 2. 전국통합식품영양성분정보 (음식)

**출처**: 식품의약품안전처
**URL**: https://www.data.go.kr/data/15100070/standard.do

**데이터 규모**
- 조리된 음식의 영양성분 정보
- 국민건강영양조사 및 국가표준식품성분표 기반

**추가 항목**
- 원재료 데이터의 모든 항목 +
- 식품분류 (대/중/소/세분류)
- 1인분량 참고량
- 식품중량
- 데이터 생성방법 및 출처

**RAG 시스템 활용도**: ⭐⭐⭐⭐⭐ (최우선)
- 사용자가 입력하는 식단은 대부분 조리된 음식
- 음식명 → 영양성분 매칭에 최적화

---

### 3. 국가표준식품성분 데이터베이스 10.3

**출처**: 농촌진흥청
**공식 사이트**: http://koreanfood.rda.go.kr
**안내 페이지**: https://www.rda.go.kr/board/boardfarminfo.do?mode=view&prgId=day_farmprmninfoEntry&dataNo=100000802611

**데이터 규모**
- 식품 수: 3,330점
- 영양성분 종류: 130종 (가장 상세)
- 데이터 건수: 약 29만 건
- 데이터 결측률: 32% (2025년 개선)

**최신 업데이트 (10.3, 2025-04)**
- 신규 식품 84점 추가 (가공 닭가슴살, 칼슘 강화 쌀, 마시멜로 등)
- 101점 식품 영양정보 최신화

**다운로드 방법**
1. http://koreanfood.rda.go.kr 접속
2. 공지사항 > "국가표준식품성분 DB 10.3 다운로드 안내" 클릭
3. 파일 포맷: hwp, hwpx, pdf

**웹사이트 기능**
- 식품명 검색
- 영양가 계산기

**활용 사례**
- 질병관리청 국민건강통계
- 교육부 학교 급식 시스템
- 건강관리 앱

**RAG 시스템 활용도**: ⭐⭐⭐⭐ (높음)
- 가장 상세한 영양성분 정보 (130종)
- 원재료 단위 분석에 유용

---

### 4. 식품안전나라 - 나트륨·당류 정보

**출처**: 식품의약품안전처
**URL**: https://foodsafetykorea.go.kr

**제공 정보**
- 나트륨 정보
- 당 정보
- 삼삼한 밥상 (저염식 레시피)
- 미각 판정 도구

**접근 경로**: 메인 > 건강·영양 > 나트륨ㆍ당류 줄이기

**RAG 시스템 활용도**: ⭐⭐⭐
- 고혈압, 당뇨 환자 식단 분석 시 유용
- 나트륨/당류 과다 섭취 경고에 활용

---

## 다운로드 상태

| 데이터베이스 | 상태 | 비고 |
|--------------|------|------|
| 전국통합식품영양성분정보 (원재료) | ⏳ API 키 필요 | 공공데이터포털 회원가입 후 Open API 신청 |
| 전국통합식품영양성분정보 (음식) | ⏳ API 키 필요 | 공공데이터포털 회원가입 후 Open API 신청 |
| 국가표준식품성분 DB 10.3 | ⏳ 수동 다운로드 필요 | koreanfood.rda.go.kr 방문 필요 |
| 식품안전나라 나트륨·당류 | ⏳ 웹사이트 확인 필요 | HTML 콘텐츠 크롤링 고려 |

## RAG 시스템 구축 우선순위

### 1순위: 전국통합식품영양성분정보 (음식) ⭐⭐⭐⭐⭐
**이유**: 사용자 입력 식단(음식명)과 직접 매칭 가능

**구축 방법**
1. 공공데이터포털(data.go.kr) 회원가입
2. Open API 활용 신청
3. API를 통해 전체 데이터 수집 (CSV/JSON)
4. Google Generative AI Embedding 생성
5. Redis Vector Store에 저장

**활용 시나리오**
```
사용자 입력: "김치찌개 1인분"
→ Vector Search: "김치찌개" 유사도 검색
→ 영양성분 반환: 칼로리 200kcal, 나트륨 1200mg, ...
→ Gemini 2.0 Flash에 컨텍스트 제공
→ 분석 리포트 생성
```

### 2순위: 국가표준식품성분 DB 10.3 ⭐⭐⭐⭐
**이유**: 가장 상세한 영양성분 정보 (130종)

**구축 방법**
1. koreanfood.rda.go.kr 방문
2. 공지사항에서 Excel/CSV 파일 다운로드
3. 파이썬으로 데이터 전처리 (HWP → CSV 변환 필요 시)
4. 1순위 데이터와 병합 (식품코드 기준)

### 3순위: 전국통합식품영양성분정보 (원재료) ⭐⭐⭐
**이유**: 가공식품 및 원재료 분석

**활용 시나리오**
- 식단 구성 시 대체 식품 추천
- 원재료 단위 영양소 계산

## 데이터 통합 전략

### Step 1: 데이터 수집
```bash
# 공공데이터포털 API 호출 예시 (Python)
import requests

api_key = "YOUR_API_KEY"
url = "https://api.odcloud.kr/api/15100070/v1/uddi:..."
params = {
    "serviceKey": api_key,
    "page": 1,
    "perPage": 1000
}
response = requests.get(url, params=params)
data = response.json()
```

### Step 2: 데이터 전처리
- 중복 제거 (식품코드 기준)
- 결측치 처리
- 단위 통일 (g, mg, μg)
- 한글 식품명 정규화

### Step 3: 임베딩 생성
```python
from langchain_google_genai import GoogleGenerativeAIEmbeddings

embeddings = GoogleGenerativeAIEmbeddings(
    model="models/text-embedding-004"
)

# 식품명 + 영양성분 요약을 텍스트로 변환
text = f"{food_name}: 칼로리 {calories}kcal, 단백질 {protein}g, ..."
vector = embeddings.embed_query(text)
```

### Step 4: Vector Store 저장
```python
from langchain_community.vectorstores import Redis

vectorstore = Redis.from_texts(
    texts=[food_description],
    embedding=embeddings,
    redis_url="redis://localhost:6379",
    index_name="food_nutrition"
)
```

## 다음 단계 액션 아이템

### 즉시 실행 가능
1. ✅ 공공데이터포털 회원가입
2. ✅ Open API 활용 신청 (2개)
   - 전국통합식품영양성분정보 (원재료)
   - 전국통합식품영양성분정보 (음식)
3. ✅ koreanfood.rda.go.kr 방문하여 Excel/CSV 다운로드

### 개발 작업
1. Python 스크립트 작성: API 호출 → CSV 저장
2. 데이터 전처리 파이프라인 구축
3. Spring AI + Redis Vector Store 통합
4. 식품명 검색 API 개발

## 예상 데이터 규모
- **음식 데이터**: 약 1만 건 추정
- **원재료 데이터**: 약 5만 건
- **농진청 DB**: 3,330건 (130종 영양소)
- **총 임베딩 벡터**: 약 6만 건

## 참고 링크
- [공공데이터포털](https://www.data.go.kr)
- [농식품올바로](http://koreanfood.rda.go.kr)
- [식품안전나라](https://foodsafetykorea.go.kr)
