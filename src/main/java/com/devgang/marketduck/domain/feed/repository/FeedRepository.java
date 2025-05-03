package com.devgang.marketduck.domain.feed.repository;

import com.devgang.marketduck.api.openapi.feed.dto.FeedDetailResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSearchDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSimpleResponseDto;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.image.entity.FeedImage;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FeedRepository {

    Page<FeedSimpleResponseDto> findAll(FeedSearchDto dto, Long userId);
    Page<FeedSimpleResponseDto> findAllForAdmin(FeedSearchDto dto);

    Page<FeedSimpleResponseDto> findAllByUserId(Long userId, int page);

    FeedDetailResponseDto findFeed(Long feedId);

    Feed findFeedById(Long feedId);

    Feed saveFeed(Feed feed);

    void deleteFeed(Long feedId);

    List<FeedImage> findAllFeedImages(Long feedId);

    FeedImage saveFeedImage(FeedImage feedImage);

    void deleteFeedImage(Long feedId, List<Integer> imageIndex);
}
