
package com.easy.controller.user;

import com.easy.pojo.dto.RoomDTO;
import com.easy.pojo.dto.group.AddGroup;
import com.easy.pojo.entity.Room;
import com.easy.pojo.model.ChatMessage;
import com.easy.result.Result;
import com.easy.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 音乐歌房控制器
 */
@RestController("userRoomController")
@RequiredArgsConstructor
@Tag(name = "C端-音乐歌房接口")
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;


    @GetMapping("/list")
    @Operation(summary = "获取活跃歌房列表")
    public Result<List<Room>> listActiveRooms() {
        return Result.success(roomService.listActiveRooms());
    }


    @PostMapping("/create")
    @Operation(summary = "创建歌房")
    public Result<Room> create(@RequestBody @Validated(AddGroup.class) RoomDTO roomDTO) {
        Room result = roomService.create(roomDTO);
        return Result.success(result);
    }

    @GetMapping("/chat")
    @Operation(summary = "获取最近20条聊天记录")
    public Result<List<ChatMessage>> chat(@RequestParam Long roomId) {
        return Result.success(roomService.listChatMessages(roomId));
    }
}
