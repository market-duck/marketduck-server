package com.devgang.marketduck.api.openapi.feed.controller;

import com.devgang.marketduck.api.openapi.feed.dto.FeedDetailResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSearchDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSimpleResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.UserFeedSearchDto;
import com.devgang.marketduck.domain.feed.service.FeedService;
import com.devgang.marketduck.dto.PageResponseDto;
import com.devgang.marketduck.dto.ResponseDto;
import com.devgang.marketduck.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/open-api/feed")
@RequiredArgsConstructor
@Slf4j
public class FeedOpenApiController implements FeedOpenApiControllerIfs {

    private final FeedService feedService;

    @Override
    @GetMapping
    public ResponseEntity<PageResponseDto<List<FeedSimpleResponseDto>>> getFeedList(FeedSearchDto requestDto, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        Page<FeedSimpleResponseDto> response = feedService.findAllFeed(requestDto, userId);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<PageResponseDto<List<FeedSimpleResponseDto>>> getFeedListByUser(UserFeedSearchDto requestDto) {
        Page<FeedSimpleResponseDto> response = feedService.findAllByUserId(requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/detail/{feedId}")
    public ResponseEntity<ResponseDto<FeedDetailResponseDto>> getFeed(Long feedId) {
        FeedDetailResponseDto response = feedService.findFeedDetail(feedId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}
