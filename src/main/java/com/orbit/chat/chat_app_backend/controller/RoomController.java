package com.orbit.chat.chat_app_backend.controller;

import com.orbit.chat.chat_app_backend.entities.Message;
import com.orbit.chat.chat_app_backend.entities.Room;
import com.orbit.chat.chat_app_backend.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    //create room
    @PostMapping("create")
    public ResponseEntity<?> createRoom(@RequestBody String roomId) {
        if (roomService.isRoomExist(roomId)){
            return ResponseEntity.badRequest().body("Room already exist"); //409
        } else {
            if(roomService.createRoom(roomId)){
                return ResponseEntity.status(HttpStatus.CREATED).body("Room created successfully"); //201
            } else  {
                return ResponseEntity.internalServerError().build();
            }
        }
    }

    //get room
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        return roomService.getRoom(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    //get messages of room
    @GetMapping("{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!roomService.isRoomExist(roomId)) {
            return ResponseEntity.notFound().build(); //404
        }

        List<Message> messages = roomService.getMessages(roomId, page, size);

        if (messages != null) {
            return ResponseEntity.ok(messages); //200
        }  else {
            return ResponseEntity.internalServerError().build(); //500
        }
    }

}
