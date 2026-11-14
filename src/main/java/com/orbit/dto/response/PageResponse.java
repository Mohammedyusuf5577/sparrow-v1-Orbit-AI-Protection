package com.orbit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private PageMeta meta;

    public static <T> PageResponse<T> of(List<T> items, int page, int limit, long totalItems) {
        int totalPages = limit > 0 ? (int) Math.ceil((double) totalItems / limit) : 0;
        PageMeta meta = PageMeta.builder()
                .page(page)
                .limit(limit)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
        return PageResponse.<T>builder()
                .items(items)
                .meta(meta)
                .build();
    }
}
