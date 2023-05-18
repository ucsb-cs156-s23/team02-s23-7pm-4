package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Song;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SongRepository extends CrudRepository<Song, Long> {

}