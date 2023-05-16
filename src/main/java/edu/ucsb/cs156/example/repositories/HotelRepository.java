package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Hotel;

import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelRepository extends CrudRepository<Hotel, Long> {
 
}