# LLM 연동 방식 조사 (SSO-22)

조사일: 2026-07-19.
목적: Java 25 + Spring Boot 4 백엔드에서 소상공인 매장 홍보 콘텐츠(블로그·인스타그램·당근 비즈·스레드용 한국어 마케팅 문구)를 LLM으로 생성하기 위한 1차 사실 조사.
이 문서는 결정을 내리지 않고 사실과 근거만 수집한다. 추정치는 "추정"으로 표기했다.

## 1. Anthropic 공식 Java SDK 직접 사용 vs Spring AI

### Anthropic 공식 Java SDK (`com.anthropic:anthropic-java`)

최신 릴리스는 v2.49.0(2026-07-16)으로, 1.0을 한참 넘긴 안정(GA) 버전이며 2주에 1회 이상 꾸준히 릴리스되고 있다.
([GitHub Releases](https://github.com/anthropics/anthropic-sdk-java/releases))
Java 8 이상을 지원하는 순수 Java 라이브러리(OkHttp + Jackson 기반)라서 Spring Boot 버전과 직접적인 호환성 제약이 없고, Java 25에서도 그대로 쓸 수 있다.
공식 문서의 예제 코드 자체가 JDK 25 compact source file 기준으로 작성되어 있다.
([공식 Java SDK 문서](https://platform.claude.com/docs/en/api/sdks/java))
동기/비동기 클라이언트, SSE 스트리밍(`createStreaming` + `MessageAccumulator`), structured outputs, tool use, 배치 API까지 전부 지원한다.

주의할 점 하나: SDK는 Jackson 2 계열(기본 2.18.2, 최소 2.13.4)에 의존하는데 Spring Boot 4 / Spring AI 2.0은 Jackson 3로 넘어갔다.
Jackson 2와 3는 패키지·groupId가 달라 공존 자체는 가능하다는 게 일반적인 이해지만, 실제 프로젝트에서의 공존 검증은 필요하다 (추측 — 도입 시 확인 항목).

### Spring AI

Spring AI 2.0.0 GA는 2026-06-12에 릴리스됐고 Spring Boot 4.0/4.1 + Spring Framework 7.0을 전제로 설계됐다.
([Spring 공식 블로그](https://spring.io/blog/2026/06/12/spring-ai-2-0-0-GA-available-now/), [Getting Started 문서](https://docs.spring.io/spring-ai/reference/getting-started.html))
Spring Boot 3.x에 머무를 경우엔 1.1.x 브랜치(현재 1.1.8)를 써야 하고, Boot 4 프로젝트라면 2.0.x가 맞는 라인이다.
이 프로젝트는 이미 Spring Boot 4이므로 Spring AI를 쓴다면 2.0.x 하나로 정리된다.

Spring AI 2.0의 Anthropic 지원 수준은 다음과 같다.

- 2.0부터 Anthropic 연동이 자체 HTTP 구현을 버리고 **공식 Java SDK 기반 단일 구현**으로 통합됐다. 즉 Spring AI를 써도 내부적으로는 위의 공식 SDK가 돈다.
  ([Spring AI 2.0 GA 발표](https://spring.io/blog/2026/06/12/spring-ai-2-0-0-GA-available-now/))
- `spring-ai-starter-model-anthropic` 스타터로 자동 구성되고, `AnthropicChatOptions`로 모델·max-tokens·structured output 스키마·effort·adaptive thinking·프롬프트 캐싱·웹서치 툴·Skills(문서 생성)까지 노출한다.
  ([Spring AI Anthropic Chat 문서](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html))
- 도구 호출(ToolCallback), 스트리밍(Flux), 멀티모달(이미지·PDF), 레이트리밋 메타데이터 조회를 지원한다.
- 제한: Anthropic 모듈은 Amazon Bedrock·Vertex AI 백엔드를 지원하지 않는다 (직접 API 호출만 해당 없음).

정리하면 두 경로 모두 성숙도는 충분하다.
SDK 직접 사용은 Anthropic 신기능이 가장 빨리 닿고 의존성이 가장 얇으며, Spring AI는 Boot 자동 구성·프로퍼티 기반 설정·추후 모델 교체 추상화(ChatClient)를 얻는 대신 프레임워크 한 겹과 릴리스 랙(2.0 GA 시점 내장 Anthropic SDK는 2.17.0으로 최신보다 뒤처짐)이 생긴다.

## 2. Structured output (JSON 스키마 강제)

### Anthropic API 자체 지원

Structured outputs는 **GA(정식)** 기능으로, `output_config.format`에 JSON 스키마를 넘기면 constrained decoding으로 스키마에 맞는 JSON만 생성되도록 강제한다.
파싱 실패나 필드 누락에 대한 재시도 로직이 필요 없다는 게 핵심이다.
지원 모델: Opus 4.8/4.7/4.6, Sonnet 5/4.6/4.5, Haiku 4.5 등 현행 전 모델.
([Structured outputs 공식 문서](https://platform.claude.com/docs/en/build-with-claude/structured-outputs))

제약 사항: 모든 object에 `additionalProperties: false` 필수, 재귀 스키마·수치 제약(minimum/maximum)·문자열 길이 제약(minLength/maxLength) 미지원, 최초 요청 시 스키마(grammar) 컴파일 지연이 있고 이후 24시간 캐시된다.
제목·본문·해시태그 배열·사진 가이드 같은 채널별 필드 구조는 이 제약에 전혀 걸리지 않는 평범한 스키마다.

### SDK 직접 사용 경로

Java SDK는 `MessageCreateParams.builder().outputConfig(MyRecord.class)`로 Java 클래스/record에서 스키마를 자동 유도하고, 응답을 `StructuredMessage<MyRecord>`로 타입 파싱해 준다.
`@JsonPropertyDescription` 등 Jackson 어노테이션으로 필드 설명을 붙일 수 있다.
([Structured outputs 문서의 Java 예제](https://platform.claude.com/docs/en/build-with-claude/structured-outputs))

```java
record ChannelContent(String title, String body, List<String> hashtags, String photoGuide) {}

StructuredMessageCreateParams<ChannelContent> params = MessageCreateParams.builder()
    .model(Model.CLAUDE_SONNET_5)
    .maxTokens(4096L)
    .outputConfig(ChannelContent.class)
    .addUserMessage(prompt)
    .build();
```

### Spring AI 경로

Spring AI의 기본 `BeanOutputConverter` / `ChatClient.entity(Class)`는 프롬프트에 포맷 지시를 붙이는 **best-effort 방식이라 스키마 준수가 보장되지 않는다**고 문서가 명시한다.
([Structured Output Converter 문서](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html))
대신 2.0에서는 provider-native structured output을 켤 수 있다 — `.advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)`를 붙이거나 `AnthropicChatOptions.outputSchema(...)`에 JSON 스키마를 직접 지정하면 위와 동일한 API 레벨 강제가 적용된다.
Anthropic의 네이티브 스키마 강제는 claude-sonnet-4-6 이상 모델을 요구한다고 Spring AI 문서에 표기되어 있다.
([Spring AI Anthropic Chat 문서](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html))

결론적으로 두 경로 모두 채널별 결과 필드를 안정적으로 받을 수 있지만, Spring AI를 쓸 경우 기본 컨버터가 아니라 네이티브 structured output을 명시적으로 켜야 같은 보장을 얻는다.

## 3. 타임아웃·재시도 설정

### Java SDK (직접 사용 시)

([공식 Java SDK 문서 — Retries/Timeouts 섹션](https://platform.claude.com/docs/en/api/sdks/java) 기준)

- **타임아웃**: 기본 10분. 클라이언트 레벨 `AnthropicOkHttpClient.builder().timeout(Duration.ofSeconds(30))` 또는 요청별 `RequestOptions.builder().timeout(...)`로 오버라이드.
  비스트리밍 요청은 maxTokens에 따라 30초~10분 사이로 동적 산정되고, 스트리밍은 maxTokens 비례로 최대 60분까지 늘어난다.
- **재시도**: 기본 2회, 지수 백오프. 재시도 대상은 커넥션 오류·408·409·429·5xx뿐이며 400류는 재시도하지 않는다.
  `.maxRetries(n)`으로 조정하고, `client.withOptions(o -> o.maxRetries(...))`로 스코프 오버라이드도 가능하다.
- 타임아웃은 시도(try)별로 적용되므로 재시도까지 포함한 벽시계 시간은 최대 timeout × (maxRetries + 1)까지 갈 수 있다.

**서버 60초 생성 타임아웃 정책과의 조합**: 기본값(10분 × 3시도)을 그대로 두면 서버 정책과 어긋나므로 클라이언트 레벨에서 명시 설정이 필요하다.
예를 들어 60초 예산 안에 재시도 1회를 넣으려면 per-try 타임아웃 약 25초 + maxRetries 1, 재시도를 포기하면 per-try 55초 + maxRetries 0 같은 조합이 된다.
스트리밍(`createStreaming`)을 쓰면 첫 토큰 도착 이후로는 유휴 커넥션 타임아웃 위험이 줄어 60초 예산 관리가 쉬워진다 — SDK도 긴 요청엔 스트리밍을 권장한다.

### Spring AI (사용 시)

Anthropic 모듈이 프로퍼티로 같은 옵션을 노출한다: `spring.ai.anthropic.timeout`(문서 예시 기본 60s), `spring.ai.anthropic.max-retries`(기본 2).
내부가 공식 SDK이므로 의미는 위 SDK 설정과 동일하다.
([Spring AI Anthropic Chat 문서](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html))

429 응답에는 `retry-after` 헤더가 오고 SDK가 이를 존중해 재시도한다는 점, 그리고 60초 안에 429 백오프까지 소화하긴 어려우므로 429는 사용자에게 지연 안내로 처리하는 편이 현실적이라는 점도 설계 시 고려 대상이다.

## 4. 대략적 비용

### 모델별 단가 (2026-07 기준, [Models overview 공식 문서](https://platform.claude.com/docs/en/about-claude/models/overview))

| 모델 | 입력 $/MTok | 출력 $/MTok | 비고 |
|---|---|---|---|
| Claude Opus 4.8 | $5 | $25 | 최상위 실용 티어 |
| Claude Sonnet 5 | $3 | $15 | **2026-08-31까지 인트로 $2 / $10** |
| Claude Sonnet 4.6 | $3 | $15 | 이전 세대 Sonnet |
| Claude Haiku 4.5 | $1 | $5 | 최저가·최고 속도 |

배치 API 사용 시 토큰 단가 50% 할인이 있으나 비동기(최대 24시간)라 실시간 생성에는 부적합하다.

### 1건 생성의 토큰 규모 추정 (추정 — count_tokens API로 실측 필요)

한국어는 대략 1자당 1~2토큰으로 계산된다 (최신 토크나이저 기준, 공식 문서는 1M 토큰 ≈ 250만 유니코드 문자라는 평균치만 제공하며 한국어는 이보다 토큰 밀도가 높다).
이를 기준으로 한 건(한 채널, 본문 700~2,000자 + 제목·해시태그·사진 가이드 + JSON 구조)을 추정하면:

- 출력: 약 1,500~4,000 토큰
- 입력(시스템 프롬프트 + 매장 정보 + 채널 가이드): 약 1,000~3,000 토큰

### 1건당·월간 비용 감 (추정, 환율 1,400원/USD 가정)

| 모델 | 1건 비용 | 회원 1인 월 10건 | 원화 감 (월) |
|---|---|---|---|
| Sonnet 5 (인트로 $2/$10) | $0.005~0.046 | $0.05~0.46 | 약 70~650원 |
| Sonnet 5 / 4.6 (정가 $3/$15) | $0.026~0.069 | $0.26~0.69 | 약 360~970원 |
| Haiku 4.5 | $0.009~0.023 | $0.09~0.23 | 약 130~320원 |
| Opus 4.8 | $0.043~0.115 | $0.43~1.15 | 약 600~1,600원 |

즉 회원당 월 50크레딧(생성 10건) 기준으로 어느 티어를 골라도 회원 1인당 LLM 원가는 월 수백 원~1천 원대 수준이다.
프롬프트 캐싱(시스템 프롬프트·채널 가이드 캐시)을 적용하면 입력 비용은 캐시 히트 시 약 1/10로 더 내려간다.
주의: Sonnet 5·Opus 4.8은 이전 세대 대비 같은 텍스트에서 토큰이 약 30% 더 많이 잡히는 새 토크나이저를 쓰므로, 구세대 기준 실측치를 재사용하면 안 되고 대상 모델로 `count_tokens`를 다시 돌려야 한다.
([Models overview](https://platform.claude.com/docs/en/about-claude/models/overview) 각주)

## 5. 한국어 마케팅 문구 품질 관점의 모델 티어

공식 근거는 Anthropic의 다국어 벤치마크(번역판 MMLU, 영어=100% 상대 성능)가 유일하다.
([Multilingual support 공식 문서](https://platform.claude.com/docs/en/build-with-claude/multilingual-support))

- 한국어 상대 성능: Sonnet 4.5 **96.7%**, Haiku 4.5 **93.3%**, Opus 4.1 96.6%
- 영어 대비 하락 폭이 Sonnet 티어(3.3%p)보다 Haiku 티어(6.7%p)에서 두 배쯤 크다 — 한국어에서는 티어 간 격차가 영어보다 벌어진다.
- 공식 권장 사항: 대상 언어를 시스템 프롬프트에 명시하고, "네이티브처럼 관용적인 표현"을 지시하면 유창성이 개선된다.

다만 이 벤치는 지식 문제 풀이 기준이라 마케팅 문구 같은 창작·문체 품질을 직접 측정한 공식 자료는 찾지 못했다.
근거 있는 범위에서 말할 수 있는 것은 (1) 한국어에서 Haiku 티어의 품질 하락 폭이 상대적으로 크다는 점, (2) 비용 분석상 Sonnet 티어를 써도 회원당 월 원가가 수백 원 수준이라 티어를 낮출 경제적 압력이 작다는 점까지다.
따라서 후보 범위는 Sonnet 티어(현행 Sonnet 5, 인트로 단가 적용 중)를 기준선으로 두고 Haiku 4.5를 비용 최적 대안으로 비교하는 구도가 되며, 최종 선택은 실제 매장 데이터로 만든 한국어 마케팅 샘플을 두 모델에 돌려 보는 자체 평가로 결정해야 한다 (Anthropic도 사용 언어별 자체 테스트를 권장).

## 종합 관찰 (결정 아님)

- 어느 경로든 structured output으로 채널별 필드를 API 레벨에서 강제할 수 있고, 타임아웃·재시도도 서버 60초 정책에 맞게 조정 가능하다.
- 갈림길은 "Anthropic 신기능 속도 + 얇은 의존성"(SDK 직접) vs "Boot 자동 구성 + 모델 추상화"(Spring AI 2.0)이며, Spring AI 2.0도 내부는 공식 SDK라 기술적 하한은 같다.
- 비용은 어떤 티어든 크레딧 정책(월 50크레딧/10건) 대비 부담이 작아, 모델 선택은 비용보다 한국어 산출물 품질 평가가 지배 변수다.
