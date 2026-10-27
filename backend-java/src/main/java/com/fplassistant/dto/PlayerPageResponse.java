package com.fplassistant.dto;

import java.util.List;

public class PlayerPageResponse {
    private List<PlayerSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PlayerPageResponse() {}

    public List<PlayerSummaryDto> getContent() { return content; }
    public void setContent(List<PlayerSummaryDto> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PlayerPageResponse r = new PlayerPageResponse();
        public Builder content(List<PlayerSummaryDto> v) { r.setContent(v); return this; }
        public Builder page(int v) { r.setPage(v); return this; }
        public Builder size(int v) { r.setSize(v); return this; }
        public Builder totalElements(long v) { r.setTotalElements(v); return this; }
        public Builder totalPages(int v) { r.setTotalPages(v); return this; }
        public Builder hasNext(boolean v) { r.setHasNext(v); return this; }
        public Builder hasPrevious(boolean v) { r.setHasPrevious(v); return this; }
        public PlayerPageResponse build() { return r; }
    }
}
