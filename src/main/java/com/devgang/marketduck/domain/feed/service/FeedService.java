package com.devgang.marketduck.domain.feed.service;

import com.devgang.marketduck.api.category.dto.CategoryDto;
import com.devgang.marketduck.api.openapi.feed.dto.*;
import com.devgang.marketduck.constant.Authority;
import com.devgang.marketduck.constant.AwsProperty;
import com.devgang.marketduck.constant.ErrorCode;
import com.devgang.marketduck.constant.FeedStatus;
import com.devgang.marketduck.domain.category.entity.FeedGenreCategory;
import com.devgang.marketduck.domain.category.entity.FeedGoodsCategory;
import com.devgang.marketduck.domain.category.repository.CategoryRepository;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.feed.repository.FeedRepository;
import com.devgang.marketduck.domain.image.entity.FeedImage;
import com.devgang.marketduck.domain.image.repository.FeedImageJpaRepository;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.domain.user.service.UserService;
import com.devgang.marketduck.exception.ServiceLogicException;
import com.devgang.marketduck.file.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedService {

    private final FeedRepository feedRepository;

    private final FeedImageJpaRepository feedImageJpaRepository;

    private final CategoryRepository categoryRepository;

    private final UserService userService;

    private final FileService fileService;


    public Page<FeedSimpleResponseDto> findAllFeed(FeedSearchDto dto, String userId) {
        Long userIdLong = null;
        if (userId != null && !userId.isBlank()) {
            userIdLong = Long.parseLong(userId);
        }
        return feedRepository.findAll(dto, userIdLong);
    }

    public Page<FeedSimpleResponseDto> findAllFeedForAdmin(FeedSearchDto dto, User user) {
        return feedRepository.findAllForAdmin(dto);
    }


    public Page<FeedSimpleResponseDto> findAllByUserId(UserFeedSearchDto dto) {
        return feedRepository.findAllByUserId(dto.getUserId(), dto.getPage());
    }

    public FeedDetailResponseDto findFeedDetail(Long feedId) {
        Feed findFeed = feedRepository.findFeedById(feedId);
        findFeed.setViewCount(findFeed.getViewCount() + 1);
        return FeedDetailResponseDto.of(feedRepository.saveFeed(findFeed));
    }

    public FeedDetailResponseDto createFeed(FeedPostRequestDto dto, User user) {
        User findUser = userService.findUserByUserId(user.getUserId());
        Feed feed = Feed.create(dto, findUser);
        List<CategoryDto> genreCategories = dto.getGenreCategories();
        List<CategoryDto> goodsCategories = dto.getGoodsCategories();
        Feed saveFeed = feedRepository.saveFeed(feed);
        categoryRepository.findAllByGenreCategoryIdIn(genreCategories.stream().map(CategoryDto::getCategoryId).toList())
                        .forEach(genreCategory -> {
                            FeedGenreCategory feedGenreCategory = FeedGenreCategory.create(saveFeed, genreCategory);
                            categoryRepository.save(feedGenreCategory);
                        });
        categoryRepository.findAllByGoodsCategoryIdIn(goodsCategories.stream().map(CategoryDto::getCategoryId).toList())
                .forEach(goodsCategory -> {
                    FeedGoodsCategory feedGoodsCategory = FeedGoodsCategory.create(saveFeed, goodsCategory);
                    categoryRepository.save(feedGoodsCategory);
                });

        return FeedDetailResponseDto.of(saveFeed);
    }

    public FeedDetailResponseDto createFeedImage(User user, Long feedId, MultipartFile[] files) {
        // 이미지 개수 제한 검증
        List<FeedImage> findAllFeedImage = feedRepository.findAllFeedImages(feedId);
        if (findAllFeedImage.size() >= 10 || findAllFeedImage.size() + files.length > 10) {
            throw new ServiceLogicException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        // 사용자 권한 검증
        Feed findFeed = feedRepository.findFeedById(feedId);
        if (!findFeed.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        // 파일 업로드 및 이미지 생성
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            // 파일 타입 검증
            if (!fileService.isImageFile(file)) {
                throw new ServiceLogicException(ErrorCode.INVALID_FILE_TYPE, "이미지 파일만 업로드 가능합니다.");
            }

            String fileName = UUID.randomUUID().toString();
            String url = fileService.saveMultipartFileForAws(file, AwsProperty.FEED_IMAGE, fileName);

            int index = findAllFeedImage.size() + i;

            // FeedImage 생성
            FeedImage feedImage = FeedImage.builder()
                    .fileName(fileName)
                    .fileUrl(url)
                    .imageIndex(index)
                    .build();

            // 첫 번째 이미지를 메인으로 설정
            if (index == 0 && findAllFeedImage.isEmpty()) {
                feedImage.setMain(true);
            }

            // 연관 관계 설정 및 저장
            findFeed.addFeedImage(feedImage);
            feedImageJpaRepository.save(feedImage);
        }

        // Feed 저장 후 DTO 반환
        Feed feed = feedRepository.saveFeed(findFeed);
        return FeedDetailResponseDto.of(feed);
    }

    public FeedDetailResponseDto updateFeedImage(Long feedId, List<Integer> index, User user) {
        // 1. Feed 및 기존 이미지 조회
        Feed findFeed = feedRepository.findFeedById(feedId);
        Set<FeedImage> feedImages = findFeed.getFeedImages();

        // 2. 삭제할 이미지 미리 수집 후 처리
        List<FeedImage> imagesToRemove = feedImages.stream()
                .filter(feedImage -> index.contains(feedImage.getImageIndex()))
                .toList();  // ✅ 안전하게 목록을 먼저 수집

        // 3. 연관 관계에서 안전하게 제거
        imagesToRemove.forEach(feedImage -> {
            findFeed.getFeedImages().remove(feedImage);
            fileService.deleteAwsFile(feedImage.getFileName(), AwsProperty.FEED_IMAGE);
        });

        // 4. DB에서 삭제 처리
        feedImageJpaRepository.deleteAllByFeed_FeedIdAndImageIndexIn(feedId, index);

        // 5. 남은 이미지 인덱스 재설정
        List<FeedImage> remainingImages = feedImageJpaRepository.findAllByFeed_FeedIdOrderByImageIndexAsc(feedId);
        for (int i = 0; i < remainingImages.size(); i++) {
            FeedImage image = remainingImages.get(i);
            if (i == 0) {
                image.setMain(true);
            } else {
                image.setMain(false);
            }
            image.setImageIndex(i); // 새로운 인덱스 설정
            feedImageJpaRepository.save(image); // 변경 사항 저장
        }

        // 6. Feed 저장 후 DTO 반환
        Feed feed = feedRepository.saveFeed(findFeed);
        return FeedDetailResponseDto.of(feed);
    }

    public FeedDetailResponseDto updateFeed(Long feedId, FeedPatchRequestDto dto, User user) {
        // 1. Feed 조회
        Feed findFeed = feedRepository.findFeedById(feedId);

        // 2. 권한 검증
        if (!findFeed.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        // 3. Feed 정보 업데이트
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            findFeed.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            findFeed.setContent(dto.getContent());
        }
        if (dto.getPrice() != null) {
            findFeed.setPrice(dto.getPrice());
        }
        if (dto.getFeedStatus().equals(FeedStatus.STOPPED) || dto.getFeedStatus().equals(FeedStatus.DELETED_BY_ADMIN)) {
            if (user.getAuthority().equals(Authority.ADMIN)) {
                findFeed.setFeedStatus(dto.getFeedStatus());
            } else {
                throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            findFeed.setFeedStatus(dto.getFeedStatus());
        }

        // 4. 카테고리 업데이트
        if (dto.getGenreCategories() != null && !dto.getGenreCategories().isEmpty()) {
            // 기존 장르 카테고리 제거
            findFeed.getFeedGenreCategories().clear();
            categoryRepository.deleteAllFeedGenreCategoryByFeedId(feedId);

            // 새로운 장르 카테고리 추가
            categoryRepository.findAllByGenreCategoryIdIn(dto.getGenreCategories().stream().map(CategoryDto::getCategoryId).toList())
                    .forEach(genreCategory -> {
                        FeedGenreCategory feedGenreCategory = FeedGenreCategory.create(findFeed, genreCategory);
                        categoryRepository.save(feedGenreCategory);
                    });
        }

        if (dto.getGoodsCategories() != null && !dto.getGoodsCategories().isEmpty()) {
            // 기존 상품 카테고리 제거
            findFeed.getFeedGoodsCategories().clear();
            categoryRepository.deleteAllFeedGoodsCategoryByFeedId(feedId);

            // 새로운 상품 카테고리 추가
            categoryRepository.findAllByGoodsCategoryIdIn(dto.getGoodsCategories().stream().map(CategoryDto::getCategoryId).toList())
                    .forEach(goodsCategory -> {
                        FeedGoodsCategory feedGoodsCategory = FeedGoodsCategory.create(findFeed, goodsCategory);
                        categoryRepository.save(feedGoodsCategory);
                    });
        }

        // 5. Feed 저장 및 DTO 반환
        Feed updatedFeed = feedRepository.saveFeed(findFeed);
        return FeedDetailResponseDto.of(updatedFeed);
    }

    public void deleteFeed(Long feedId, User user) {
        // 1. Feed 조회
        Feed findFeed = feedRepository.findFeedById(feedId);

        // 2. 권한 검증
        if (!findFeed.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        // 3. 연관 이미지 삭제
        findFeed.getFeedImages().forEach(feedImage -> {
            fileService.deleteAwsFile(feedImage.getFileName(), AwsProperty.FEED_IMAGE);
        });
        feedImageJpaRepository.deleteAllByFeed_FeedId(feedId);

        // 4. 연관 카테고리 삭제
        findFeed.getFeedGoodsCategories().clear();
        findFeed.getFeedGenreCategories().clear();
        // 5. Feed 삭제
        feedRepository.deleteFeed(feedId);
    }

}
