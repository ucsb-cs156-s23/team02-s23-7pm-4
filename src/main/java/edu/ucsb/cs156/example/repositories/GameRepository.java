package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Game;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GameRepository extends CrudRepository<Game, Long> {
 
}