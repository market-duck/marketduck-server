package com.devgang.marketduck.api.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat-test")
public class ChatTestController {

    @GetMapping("/login")
    public String loginPage() {
        return "chat/login";
    }

    @GetMapping("/rooms")
    public String chatRoomsPage() {
        return "chat/rooms";
    }

    @GetMapping("/room")
    public String chatRoomPage() {
        return "chat/room";
    }
}