package com.eduscope.web.studentanalysis.dto;

import java.util.List;

/**
 * Demo Snapshot 생성을 위한 학생 Bulk 응답 DTO.
 * 기존 학생 검색/상세 DTO를 그대로 재사용한다.
 */
public class StudentSnapshotPageResponse {

    private final int offset;
    private final int limit;
    private final int returnedCount;
    private final int nextOffset;
    private final boolean hasMore;
    private final List<Item> items;

    public StudentSnapshotPageResponse(
            int offset,
            int limit,
            int returnedCount,
            int nextOffset,
            boolean hasMore,
            List<Item> items) {

        this.offset = offset;
        this.limit = limit;
        this.returnedCount = returnedCount;
        this.nextOffset = nextOffset;
        this.hasMore = hasMore;
        this.items = items;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getReturnedCount() {
        return returnedCount;
    }

    public int getNextOffset() {
        return nextOffset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {

        private final StudentSearchResponse search;
        private final StudentAnalysisResponse analysis;

        public Item(
                StudentSearchResponse search,
                StudentAnalysisResponse analysis) {

            this.search = search;
            this.analysis = analysis;
        }

        public StudentSearchResponse getSearch() {
            return search;
        }

        public StudentAnalysisResponse getAnalysis() {
            return analysis;
        }
    }
}
