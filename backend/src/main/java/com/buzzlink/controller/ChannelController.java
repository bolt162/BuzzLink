package com.buzzlink.controller;

import com.buzzlink.dto.ChannelDTO;
import com.buzzlink.entity.Channel;
import com.buzzlink.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for channel operations
 */
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChannelController {

    private final ChannelRepository channelRepository;

    /**
     * GET /api/channels - List all channels
     */
    @GetMapping
    public ResponseEntity<List<ChannelDTO>> getAllChannels() {
        List<Channel> channels = channelRepository.findAll();
        List<ChannelDTO> channelDTOs = channels.stream()
            .map(ChannelDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(channelDTOs);
    }

    /**
     * GET /api/channels/{id} - Get a specific channel
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChannelDTO> getChannel(@PathVariable Long id) {
        return channelRepository.findById(id)
            .map(channel -> ResponseEntity.ok(ChannelDTO.fromEntity(channel)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/channels - Create a new channel (for demo/testing)
     */
    @PostMapping
    public ResponseEntity<ChannelDTO> createChannel(@RequestBody CreateChannelRequest request) {
        Channel channel = new Channel();
        channel.setName(request.name());
        channel.setDescription(request.description());

        Channel savedChannel = channelRepository.save(channel);
        return ResponseEntity.ok(ChannelDTO.fromEntity(savedChannel));
    }

    /**
     * Request body for creating a channel
     */
    public record CreateChannelRequest(String name, String description) {}
}
