package com.devgang.marketduck.api.feed.controller;


import com.devgang.marketduck.annotation.UserSession;
import com.devgang.marketduck.api.openapi.feed.dto.*;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.dto.PageResponseDto;
import com.devgang.marketduck.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Feed API", description = "Feed 관련 API")
public interface FeedControllerIfs {

    class FeedSimple extends PageResponseDto<List<FeedSimpleResponseDto>> { }
    class FeedDetail extends ResponseDto<FeedDetailResponseDto> { }
    class FeedImage extends ResponseDto<FeedImageResponseDto> { }

    /*
    * Feed 등록
    * */
    @Operation(summary = "Feed 생성(회원, 관리자)", description = "Feed 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedDetail.class)
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedDetailResponseDto>> postFeed(
            @UserSession @Parameter(hidden = true) User user,
            @RequestBody FeedPostRequestDto requestDto
            );
    /*
    * Feed 사진 등록
    * */
    @Operation(summary = "특정 Feed 사진 등록(회원,관리자)", description = "특정 Feed 사진 등록, file Data는 배열 형태로 받음, Index(0) 데이터 메인 이미지로 설정 그외 Index 값 자동 할당, Image 저장 이름 uuid 자동 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedImage.class)
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedImageResponseDto>> postFeedImage(
            @PathVariable Long feedId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );
    /*
     * Feed 사진 수정
     * */
    @Operation(summary = "특정 Feed 사진 수정(회원,관리자)", description = "Feed Image 수정 특정 FeedImage 의 해당 Index 데이터만 수정됨")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedImage.class)
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedImageResponseDto>> patchFeedImage(
            @PathVariable Long feedId,
            @PathVariable Long index,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user
    );

    /*
    * Feed 수정
    * */
    @Operation(summary = "Feed 수정(회원, 관리자)", description = "Feed 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedDetail.class)
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedDetailResponseDto>> patchFeed(
            @PathVariable Long feedId,
            @UserSession @Parameter(hidden = true) User user,
            @RequestBody FeedPatchRequestDto requestDto
    );
    /*
    * Feed 삭제
    * */
    @Operation(summary = "Feed 삭제(회원, 관리자)", description = "Feed 삭제 요청, Feed와 연괸된 모든 정보 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedDetailResponseDto>> deleteFeed(
            @UserSession @Parameter(hidden = true) User user,
            @PathVariable Long feedId
    );
}
