package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Grocery;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GroceryRepository extends CrudRepository<Grocery, Long> {

}