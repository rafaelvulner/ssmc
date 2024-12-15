package com.ssmc.service.impl;

import com.ssmc.service.interfaces.WorkingStreamingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifyService implements WorkingStreamingService {

    @Value("${auth.client.id}")
    private String clientId;

    @Value("${auth.client.secret}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/callback";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getAccessToken(String code) {
        try {
            String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + credentials);

            String body = "grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectUri;

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode responseBody = mapper.readTree(response.getBody());
                return responseBody.get("access_token").asText();
            } else {
                throw new RuntimeException("Erro ao obter token: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Erro de cliente ao obter o token: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao obter o token: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPlayLists(String token) {
        return "";
    }

    @Override
    public String getPlaylistTracks(String token, String playlistId) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token de acesso inválido ou ausente.");
            }
            if (!isValidBase62(playlistId)) {
                throw new IllegalArgumentException("ID da playlist fornecido não está no formato base62.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> request = new HttpEntity<>(headers);

            String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Erro ao obter músicas da playlist: " + response.getStatusCode());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Erro de cliente ao obter músicas da playlist: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao obter músicas da playlist: " + e.getMessage(), e);
        }
    }

    private boolean isValidBase62(String input) {
        return input.matches("^[A-Za-z0-9]+$");
    }

    public List<String> getTracks(String token, String playlistId) {
        try {
            String responseJson = this.getPlaylistTracks(token, playlistId);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseJson);
            JsonNode itemsNode = rootNode.get("items");

            List<String> tracks = new ArrayList<>();
            for (JsonNode item : itemsNode) {
                JsonNode trackNode = item.get("track");
                String trackName = trackNode.get("name").asText();
                String artists = trackNode.get("artists").get(0).get("name").asText();

                tracks.add("Música: " + trackName + " | Artista: " + artists);
            }
            return tracks;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar músicas da playlist: " + e.getMessage(), e);
        }
    }
}