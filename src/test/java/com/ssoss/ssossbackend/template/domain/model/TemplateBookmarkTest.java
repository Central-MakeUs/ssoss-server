package com.ssoss.ssossbackend.template.domain.model;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemplateBookmark")
class TemplateBookmarkTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long TEMPLATE_ID = 2L;
    private static final Instant BOOKMARKED_AT = Instant.parse("2026-08-09T00:00:00Z");
    private static final Instant LATER = Instant.parse("2026-08-10T00:00:00Z");

    @Nested
    @DisplayName("bookmark")
    class Bookmark {

        @Test
        @DisplayName("북마크한 적 없으면 참을 돌려주고 북마크한 시각이 박힌다")
        void marksBookmarkedAt_whenNeverBookmarked() {
            TemplateBookmark bookmark = TemplateBookmark.create(MEMBER_ID, TEMPLATE_ID);

            assertThat(bookmark.bookmark(BOOKMARKED_AT)).isTrue();
            assertThat(bookmark.getBookmarkedAt()).isEqualTo(BOOKMARKED_AT);
        }

        @Test
        @DisplayName("이미 북마크했으면 거짓을 돌려주고 북마크한 시각이 그대로다")
        void keepsBookmarkedAt_whenAlreadyBookmarked() {
            TemplateBookmark bookmark = TemplateBookmark.create(MEMBER_ID, TEMPLATE_ID);
            bookmark.bookmark(BOOKMARKED_AT);

            assertThat(bookmark.bookmark(LATER)).isFalse();
            assertThat(bookmark.getBookmarkedAt()).isEqualTo(BOOKMARKED_AT);
        }
    }
}
