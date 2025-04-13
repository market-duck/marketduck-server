package com.devgang.marketduck.api.feed.controller;

import com.devgang.marketduck.annotation.UserSession;
import com.devgang.marketduck.api.feed.dto.FeedImagePatchDto;
import com.devgang.marketduck.api.openapi.feed.dto.*;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.dto.PageResponseDto;
import com.devgang.marketduck.domain.feed.dto.FeedLikeResponseDto;
import com.devgang.marketduck.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Feed API", description = "Feed 관련 API")
public interface FeedControllerIfs {

        class FeedSimple extends PageResponseDto<List<FeedSimpleResponseDto>> {
        }

        class FeedDetail extends ResponseDto<FeedDetailResponseDto> {
        }

        class FeedImage extends ResponseDto<FeedImageResponseDto> {
        }

        /*
         * Feed 등록
         */

        @Operation(summary = "전체 Feed 정보 요청(관리자 전용)", description = "전체 Feed 리스트 요청, 삭제된 데이터까지 모두 응답")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = FeedSimple.class))
                        })
        })
        ResponseEntity<PageResponseDto<List<FeedSimpleResponseDto>>> getFeedList(
                        @UserSession @Parameter(hidden = true) User user,
                        @ModelAttribute FeedSearchDto requestDto);

        @Operation(summary = "Feed 생성(회원, 관리자)", description = "Feed 생성 요청")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = FeedDetail.class))
                        })
        })
        ResponseEntity<ResponseDto<FeedDetailResponseDto>> postFeed(
                        @UserSession @Parameter(hidden = true) User user,
                        @RequestBody FeedPostRequestDto requestDto);

        /*
         * Feed 사진 등록
         */
        @Operation(summary = "특정 Feed 사진 등록(회원,관리자)", description = "특정 Feed 사진 등록, file Data는 배열 형태로 받음, Index(0) 데이터 메인 이미지로 설정 그외 Index 값 자동 할당, Image 저장 이름 uuid 자동 생성")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = FeedImage.class))
                        })
        })
        ResponseEntity<ResponseDto<FeedDetailResponseDto>> postFeedImage(
                        @PathVariable Long feedId,
                        @RequestPart("file") MultipartFile[] multipartFile,
                        @UserSession @Parameter(hidden = true) User user);

        /*
         * Feed 사진 수정
         */
        @Operation(summary = "특정 Feed 사진 삭제(회원,관리자)", description = "Feed Image 삭제 특정 FeedImage 의 해당 Index 데이터들만 삭제됨, 삭제 후 특정 피드의 이미지 리스트 인덱스 재정렬됨")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = FeedImage.class))
                        })
        })
        ResponseEntity<ResponseDto<FeedDetailResponseDto>> patchFeedImage(
                        @RequestBody FeedImagePatchDto request,
                        @UserSession @Parameter(hidden = true) User user);

        /*
         * Feed 수정
         */
        @Operation(summary = "Feed 수정(회원, 관리자)", description = "Feed 수정 요청")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = FeedDetail.class))
                        })
        })
        ResponseEntity<ResponseDto<FeedDetailResponseDto>> patchFeed(
                        @PathVariable Long feedId,
                        @UserSession @Parameter(hidden = true) User user,
                        @RequestBody FeedPatchRequestDto requestDto);

        /*
         * Feed 삭제
         */
        @Operation(summary = "Feed 삭제(관리자)", description = "Feed 삭제 요청, Feed와 연괸된 모든 정보 완전 삭제, 일반 삭제 상태 수정은 PATCH API 이용")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json")
                        })
        })
        ResponseEntity<ResponseDto> deleteFeed(
                        @UserSession @Parameter(hidden = true) User user,
                        @PathVariable Long feedId);

        /*
         * Feed 좋아요 추가
         */
        @Operation(summary = "Feed 좋아요 추가/취소(회원)", description = "Feed 좋아요 추가/취소 요청, 좋아요 추가시 좋아요 정보 반환, 좋아요 취소시 빈 객체 반환")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json")
                        })
        })
        ResponseEntity<ResponseDto<FeedLikeResponseDto>> postFeedLike(
                        @PathVariable Long feedId,
                        @UserSession @Parameter(hidden = true) User user);

        /*
         * Feed 좋아요 조회
         */
        @Operation(summary = "Feed 좋아요 전체 조회(회원)", description = "Feed 좋아요 전체 조회 요청")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                                        @Content(mediaType = "application/json")
                        })
        })
        ResponseEntity<PageResponseDto<List<FeedLikeResponseDto>>> getFeedLike(
                        @UserSession @Parameter(hidden = true) User user);
}
