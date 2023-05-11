package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Song;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.SongRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "Songs")
@RequestMapping("/api/songs")
@RestController
@Slf4j
public class SongsController extends ApiController {

    @Autowired
    SongRepository SongRepository;

    @ApiOperation(value = "List all songs")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Song> allSongs() {
        Iterable<Song> songs = songRepository.findAll();
        return songs;
    }

    @ApiOperation(value = "Get a single song")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Song getById(
            @ApiParam("id") @RequestParam Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Song.class, id));

        return song;
    }

    @ApiOperation(value = "Create a new song")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Song postSong(
            @ApiParam("artist") @RequestParam String artist,
            @ApiParam("album") @RequestParam String album,
            @ApiParam("year") @RequestParam int year
            )
            {

        Song song = new Song();
        song.setArtist(artist);
        song.setAlbum(album);
        song.setYear(year);

        Song savedSong = songRepository.save(song);

        return savedSong;
    }

    @ApiOperation(value = "Delete a song")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteSong(
            @ApiParam("id") @RequestParam Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Song.class, id));

        songRepository.delete(song);
        return genericMessage("Song with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single song")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Song updateSong(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Song incoming) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Song.class, id));

        song.setArtist(incoming.getArtist());
        song.setAlbum(incoming.getAlbum());
        song.setYear(incoming.getYear());

        songRepository.save(song);

        return song;
    }
}
