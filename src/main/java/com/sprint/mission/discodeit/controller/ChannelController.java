package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(method = RequestMethod.POST)
    public ChannelResponse create(@RequestParam String name, @RequestParam String description) {
        return channelService.create(name, description);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> findAll() {
        return channelService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Optional<ChannelResponse> findById(@PathVariable UUID id) {
        return channelService.findById(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ChannelResponse update(@PathVariable UUID id, @RequestParam String name, @RequestParam String description) {
        return channelService.update(id, name, description);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        channelService.delete(id);
    }
}