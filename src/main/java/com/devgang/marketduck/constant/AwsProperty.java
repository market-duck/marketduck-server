package com.devgang.marketduck.constant;

import lombok.Getter;

@Getter
public enum AwsProperty {

    // todo 디렉토리 이름 수정 필요
    STORAGE("storage/"),
    USER_IMAGE("user/image/"),
    FEED_IMAGE("feed/image/"),
    CHAT_IMAGE("chat/image/"),

    ZIP_DIR_NAME("zip/");

    private final String name;

    AwsProperty(String name) {
        this.name = name;
    }
}
