package com.orbit.chat.chat_app_backend.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    private String sender;
    private String content;
    private LocalDateTime timeStamp =  LocalDateTime.now();

}
