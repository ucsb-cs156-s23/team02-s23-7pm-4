package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Movie;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MovieRepository extends CrudRepository<Movie, Long> {
  
}