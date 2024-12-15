package com.ssmc.controller;

import com.ssmc.service.interfaces.WorkingStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpotifyController {

    private final WorkingStreamingService streamingService;

    public SpotifyController(WorkingStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @GetMapping("/callback")
    public ResponseEntity<List<String>> handleSpotifyCallback(@RequestParam("code") String code) {

        System.out.println("Authorization Code: " + code);

        String accessToken = streamingService.getAccessToken(code);
        String playLists = streamingService.getPlayLists(accessToken);
        List<String> playlistTracks = streamingService.getTracks(accessToken, "0hdCNdoUNEDhAfpfFK0ar8");


        return ResponseEntity.ok(playlistTracks);
    }

    @GetMapping("/playlist/{playlistId}/tracks")
    public ResponseEntity<String> getPlaylistTracks(@RequestParam("token") String token, @PathVariable("playlistId") String playlistId) {
        try {
            String tracks = streamingService.getPlaylistTracks(token, playlistId);
            return ResponseEntity.ok(tracks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
