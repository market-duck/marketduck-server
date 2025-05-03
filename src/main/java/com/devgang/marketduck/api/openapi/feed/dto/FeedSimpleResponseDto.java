package com.devgang.marketduck.api.openapi.feed.dto;

import com.devgang.marketduck.api.openapi.category.dto.CategoryResponseDto;
import com.devgang.marketduck.constant.CategoryType;
import com.devgang.marketduck.constant.FeedStatus;
import com.devgang.marketduck.constant.FeedType;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.image.entity.FeedImage;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class FeedSimpleResponseDto {

    private Long feedId; // ID 필드를 feedId로 변경

    private List<CategoryResponseDto> genreCategory;

    private List<CategoryResponseDto> goodsCategory;

    private String title;

    private BigDecimal price;

    private String content;

    private int viewCount;

    private int likeCount;

    private FeedStatus status;

    private FeedType feedType;

    private String mainImageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isLiked;

    // isLiked setter 명시적 추가
    public void setLiked(boolean liked) {
        this.isLiked = liked;
    }

    @QueryProjection
    public FeedSimpleResponseDto(Long feedId, List<CategoryResponseDto> genreCategory,
            List<CategoryResponseDto> goodsCategory, String title, BigDecimal price, String content, int viewCount,
            int likeCount, FeedStatus status, FeedType feedType, String mainImageUrl, LocalDateTime createdAt,
            LocalDateTime updatedAt, boolean isLiked) {
        this.feedId = feedId;
        this.genreCategory = genreCategory;
        this.goodsCategory = goodsCategory;
        this.title = title;
        this.price = price;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.status = status;
        this.feedType = feedType;
        this.mainImageUrl = mainImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLiked = isLiked;
    }

    public static FeedSimpleResponseDto of(Feed feed) {
        return FeedSimpleResponseDto.builder()
                .feedId(feed.getFeedId())
                .genreCategory(feed.getFeedGenreCategories().stream()
                        .map(category -> new CategoryResponseDto(category.getGenreCategory().getGenreCategoryId(),
                                category.getGenreCategory().getGenreCategoryName(), CategoryType.GENRE))
                        .toList())
                .goodsCategory(feed.getFeedGoodsCategories().stream()
                        .map(category -> new CategoryResponseDto(category.getGoodsCategory().getGoodsCategoryId(),
                                category.getGoodsCategory().getGoodsCategoryName(), CategoryType.GOODS))
                        .toList())
                .title(feed.getTitle())
                .price(feed.getPrice())
                .content(feed.getContent())
                .viewCount(feed.getViewCount())
                .likeCount(feed.getLikeCount())
                .status(feed.getFeedStatus())
                .feedType(feed.getFeedType())
                .mainImageUrl(feed.getFeedImages().stream().filter(FeedImage::isMain).map(FeedImage::getFileUrl)
                        .findFirst().orElse(""))
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .build();
    }

}
