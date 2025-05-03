package com.devgang.marketduck.api.openapi.feed.controller;


import com.devgang.marketduck.api.openapi.feed.dto.FeedDetailResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSearchDto;
import com.devgang.marketduck.api.openapi.feed.dto.FeedSimpleResponseDto;
import com.devgang.marketduck.api.openapi.feed.dto.UserFeedSearchDto;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "Feed Open API", description = "Feed 관련 Open API")
public interface FeedOpenApiControllerIfs {

    class FeedSimple extends PageResponseDto<List<FeedSimpleResponseDto>> { }
    class FeedDetail extends ResponseDto<FeedDetailResponseDto> { }


    @Operation(summary = "전체 Feed 정보 요청(회원,관리자)", description = "전체 Feed 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedSimple.class)
                    )
                    })
    })
    ResponseEntity<PageResponseDto<List<FeedSimpleResponseDto>>> getFeedList(
            @ModelAttribute FeedSearchDto requestDto,
            @Parameter(hidden = true) HttpServletRequest request
            );

    /*
     * 특정 회원의 Feed List 조회
     * */
    @Operation(summary = "특정 회원 Feed 정보 요청(회원,관리자)", description = "특정 회원 Feed 리스트 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedSimple.class)
                    )
                    })
    })
    ResponseEntity<PageResponseDto<List<FeedSimpleResponseDto>>> getFeedListByUser(
            @ModelAttribute UserFeedSearchDto requestDto
    );

    /*
    * Feed 상세 조회
    * */

    @Operation(summary = "특정 Feed 단건 정보 요청(회원,관리자)", description = "특정 Feed 단건 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = FeedDetail.class)
                    )
                    })
    })
    ResponseEntity<ResponseDto<FeedDetailResponseDto>> getFeed(
            @PathVariable @Parameter(description = "Feed 식별자", required = true) Long feedId
    );

}
