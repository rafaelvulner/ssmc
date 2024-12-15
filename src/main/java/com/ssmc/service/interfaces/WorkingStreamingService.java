package com.ssmc.service.interfaces;

import java.util.List;

public interface WorkingStreamingService {

    String getAccessToken(String code);
    String getPlayLists(String token);
    String getPlaylistTracks(String token, String playlistId);
    List<String> getTracks(String token, String playlistId);
}
