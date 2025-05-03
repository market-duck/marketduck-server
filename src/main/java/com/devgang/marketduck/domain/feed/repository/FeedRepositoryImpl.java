package com.devgang.marketduck.domain.feed.repository;

import com.devgang.marketduck.api.openapi.feed.dto.FeedDetailResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSearchDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSimpleResponseDto;
import com.devgang.marketduck.constant.ErrorCode;
import com.devgang.marketduck.constant.FeedStatus;
import com.devgang.marketduck.constant.FeedType;
import com.devgang.marketduck.domain.category.entity.QFeedGenreCategory;
import com.devgang.marketduck.domain.category.entity.QFeedGoodsCategory;
import com.devgang.marketduck.domain.category.entity.QGenreCategory;
import com.devgang.marketduck.domain.category.entity.QGoodsCategory;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.feed.entity.QFeed;
import com.devgang.marketduck.domain.feed.entity.QFeedLike;
import com.devgang.marketduck.domain.image.entity.FeedImage;
import com.devgang.marketduck.domain.image.entity.QFeedImage;
import com.devgang.marketduck.domain.image.repository.FeedImageJpaRepository;
import com.devgang.marketduck.domain.user.entity.QUser;
import com.devgang.marketduck.exception.ServiceLogicException;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepository {

    private final JPAQueryFactory queryFactory;

    private final FeedJpaRepository feedJpaRepository;

    private final FeedImageJpaRepository feedImageJpaRepository;

    QFeed feed = QFeed.feed;
    QFeedImage feedImage = QFeedImage.feedImage;
    QFeedGenreCategory feedGenreCategory = QFeedGenreCategory.feedGenreCategory;
    QFeedGoodsCategory feedGoodsCategory = QFeedGoodsCategory.feedGoodsCategory;
    QUser user = QUser.user;
    QGenreCategory genre = QGenreCategory.genreCategory;
    QGoodsCategory goods = QGoodsCategory.goodsCategory;
    QFeedLike feedLike = QFeedLike.feedLike;

    @Override
    public Page<FeedSimpleResponseDto> findAll(FeedSearchDto dto, Long userId) {
        Pageable pageable = PageRequest.of(dto.getPage(), 10);

        JPAQuery<Feed> query = buildFeedQuery(dto, false); // includeDeleted = false

        long total = query.fetchCount();
        List<Feed> feeds = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // userId가 null이 아니면 좋아요 정보를 함께 조회
        List<FeedSimpleResponseDto> content;
        if (userId != null) {
            // 사용자가 좋아요한 피드 ID 목록 조회
            List<Long> likedFeedIds = queryFactory
                    .select(feedLike.feed.feedId)
                    .from(feedLike)
                    .where(feedLike.user.userId.eq(userId))
                    .fetch();

            // 피드마다 좋아요 여부 설정
            content = feeds.stream()
                    .map(feedEntity -> {
                        FeedSimpleResponseDto feedSimpleDto = FeedSimpleResponseDto.of(feedEntity);
                        feedSimpleDto.setLiked(likedFeedIds.contains(feedEntity.getFeedId()));
                        return feedSimpleDto;
                    })
                    .collect(Collectors.toList());
        } else {
            // 로그인하지 않은 사용자는 모든 피드에 대해 좋아요 false 설정
            content = feeds.stream()
                    .map(feedEntity -> {
                        FeedSimpleResponseDto feedSimpleDto = FeedSimpleResponseDto.of(feedEntity);
                        feedSimpleDto.setLiked(false);
                        return feedSimpleDto;
                    })
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<FeedSimpleResponseDto> findAllForAdmin(FeedSearchDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage(), 10);

        JPAQuery<Feed> query = buildFeedQuery(dto, true); // includeDeleted = true

        long total = query.fetchCount();
        List<Feed> feeds = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 관리자용 조회는 좋아요 상태를 false로 설정
        List<FeedSimpleResponseDto> content = feeds.stream()
                .map(feedEntity -> {
                    FeedSimpleResponseDto feedSimpleDto = FeedSimpleResponseDto.of(feedEntity);
                    feedSimpleDto.setLiked(false);
                    return feedSimpleDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    private JPAQuery<Feed> buildFeedQuery(FeedSearchDto dto, boolean includeDeleted) {
        List<Long> genreIds = dto.getGenreIds();
        List<Long> goodsIds = dto.getGoodsIds();
        String keyword = dto.getKeyword();
        FeedStatus status = dto.getStatus();
        FeedType type = dto.getFeedType();

        JPAQuery<Feed> query = queryFactory.select(feed)
                .from(feed)
                .leftJoin(feed.feedGenreCategories, feedGenreCategory)
                .leftJoin(feed.feedGoodsCategories, feedGoodsCategory)
                .leftJoin(feed.feedImages, feedImage)
                .leftJoin(feed.user, user);

        if (genreIds != null && !genreIds.isEmpty()) {
            query.where(
                    feedGenreCategory.genreCategory.genreCategoryId.in(genreIds));
        }
        if (goodsIds != null && !goodsIds.isEmpty()) {
            query.where(
                    feedGoodsCategory.goodsCategory.goodsCategoryId.in(goodsIds));
        }
        if (keyword != null && !keyword.isEmpty()) {
            query.where(
                    feed.title.containsIgnoreCase(keyword)
                            .or(feed.content.containsIgnoreCase(keyword))
                            .or(feedGoodsCategory.goodsCategory.goodsCategoryName.containsIgnoreCase(keyword))
                            .or(feedGenreCategory.genreCategory.genreCategoryName.containsIgnoreCase(keyword)));
        }
        if (status != null) {
            query.where(
                    feed.feedStatus.eq(status));
        }
        if (type != null) {
            query.where(
                    feed.feedType.eq(type));
        }

        if (!includeDeleted) {
            query.where(
                    feed.feedStatus.notIn(
                            FeedStatus.DELETED,
                            FeedStatus.DELETED_BY_ADMIN,
                            FeedStatus.STOPPED));
        }

        query.orderBy(feed.createdAt.desc());
        return query;
    }

    @Override
    public Page<FeedSimpleResponseDto> findAllByUserId(Long userId, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        JPAQuery<Feed> query = queryFactory.select(feed)
                .from(feed)
                .leftJoin(feed.feedGenreCategories, feedGenreCategory)
                .leftJoin(feed.feedGoodsCategories, feedGoodsCategory)
                .leftJoin(feed.feedImages, feedImage)
                .leftJoin(feed.user, user);
        query.where(feed.user.userId.eq(userId));
        query.where(
                feed.feedStatus.notIn(
                        FeedStatus.DELETED,
                        FeedStatus.DELETED_BY_ADMIN,
                        FeedStatus.STOPPED));
        query.orderBy(feed.createdAt.desc());
        long total = query.fetchCount();
        List<Feed> feeds = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 사용자가 좋아요한 피드 ID 목록 조회
        List<Long> likedFeedIds = queryFactory
                .select(feedLike.feed.feedId)
                .from(feedLike)
                .where(feedLike.user.userId.eq(userId))
                .fetch();

        // 피드마다 좋아요 여부 설정
        List<FeedSimpleResponseDto> content = feeds.stream()
                .map(feedEntity -> {
                    FeedSimpleResponseDto feedSimpleDto = FeedSimpleResponseDto.of(feedEntity);
                    feedSimpleDto.setLiked(likedFeedIds.contains(feedEntity.getFeedId()));
                    return feedSimpleDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public FeedDetailResponseDto findFeed(Long feedId) {

        JPAQuery<Feed> query = queryFactory.select(feed)
                .from(feed)
                .leftJoin(feed.feedGenreCategories, feedGenreCategory)
                .leftJoin(feed.feedGoodsCategories, feedGoodsCategory)
                .leftJoin(feed.feedImages, feedImage)
                .leftJoin(feed.user, user);
        query.where(feed.feedId.eq(feedId));
        Optional<Feed> findFeed = Optional.ofNullable(query.fetchOne());
        return findFeed.map(FeedDetailResponseDto::of).orElseThrow(
                () -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    @Override
    public Feed findFeedById(Long feedId) {
        JPAQuery<Feed> query = queryFactory.select(feed)
                .from(feed)
                .leftJoin(feed.feedGenreCategories, feedGenreCategory)
                .leftJoin(feed.feedGoodsCategories, feedGoodsCategory)
                .leftJoin(feed.feedImages, feedImage)
                .leftJoin(feed.user, user);
        query.where(feed.feedId.eq(feedId));
        Optional<Feed> findFeed = Optional.ofNullable(query.fetchOne());
        return findFeed.orElseThrow(
                () -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    @Override
    public Feed saveFeed(Feed feed) {
        return feedJpaRepository.save(feed);
    }

    @Override
    public void deleteFeed(Long feedId) {
        feedJpaRepository.deleteById(feedId);
    }

    @Override
    public List<FeedImage> findAllFeedImages(Long feedId) {
        return feedImageJpaRepository.findAllByFeed_FeedIdOrderByImageIndexAsc(feedId);
    }

    @Override
    public FeedImage saveFeedImage(FeedImage feedImage) {
        return feedImageJpaRepository.save(feedImage);
    }

    @Override
    public void deleteFeedImage(Long feedId, List<Integer> imageIndex) {
        feedImageJpaRepository.deleteAllByFeed_FeedIdAndImageIndexIn(feedId, imageIndex);

    }

}
