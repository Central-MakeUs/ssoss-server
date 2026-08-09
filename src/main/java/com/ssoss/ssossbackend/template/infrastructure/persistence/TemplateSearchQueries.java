package com.ssoss.ssossbackend.template.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.persistence.LikePattern;
import com.ssoss.ssossbackend.template.domain.model.Template;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

interface TemplateSearchQueries extends Repository<Template, Long> {

    String KEYWORD_MATCHES = "(title LIKE :pattern ESCAPE '" + LikePattern.ESCAPE + "'"
        + " OR description LIKE :pattern ESCAPE '" + LikePattern.ESCAPE + "'"
        + " OR body LIKE :pattern ESCAPE '" + LikePattern.ESCAPE + "')";

    String CATEGORY_MATCHES = "(:category IS NULL OR category = :category)";

    @Query("SELECT * FROM template WHERE " + KEYWORD_MATCHES + " AND " + CATEGORY_MATCHES
        + " ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<Template> search(String pattern, String category, int limit, long offset);

    @Query("SELECT COUNT(*) FROM template WHERE " + KEYWORD_MATCHES + " AND " + CATEGORY_MATCHES)
    long countMatches(String pattern, String category);
}
