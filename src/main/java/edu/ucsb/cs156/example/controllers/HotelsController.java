package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Hotel;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HotelRepository;
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


@Api(description = "Hotels")
@RequestMapping("/api/hotels")
@RestController
@Slf4j
public class HotelsController extends ApiController {

    @Autowired
    HotelRepository hotelRepository;

    @ApiOperation(value = "List all hotels")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Hotel> allHotels() {
        Iterable<Hotel> hotels = hotelRepository.findAll();
        return hotels;
    }

    @ApiOperation(value = "Get a single hotel")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Hotel getById(
            @ApiParam("id") @RequestParam Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Hotel.class, id));

        return hotel;
    }

    @ApiOperation(value = "Create a new hotel")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Hotel postHotel(
        @ApiParam("name") @RequestParam String name,
        @ApiParam("address") @RequestParam String address,
        @ApiParam("description") @RequestParam String description
        )
        {

        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setDescription(description);


        Hotel savedHotel = hotelRepository.save(hotel);

        return savedHotel;
    }

    @ApiOperation(value = "Delete a hotel")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteHotel(
            @ApiParam("id") @RequestParam Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Hotel.class, id));

        hotelRepository.delete(hotel);
        return genericMessage("Hotel with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single hotel")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Hotel updateHotel(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Hotel incoming) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Hotel.class, id));


        hotel.setName(incoming.getName());  
        hotel.setAddress(incoming.getAddress());
        hotel.setDescription(incoming.getDescription());

        hotelRepository.save(hotel);

        return hotel;
    }
}
