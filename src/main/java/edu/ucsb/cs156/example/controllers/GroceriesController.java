package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Grocery;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.GroceryRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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


@Api(description = "Groceries")
@RequestMapping("/api/groceries")
@RestController
@Slf4j
public class GroceriesController extends ApiController {

    @Autowired
    GroceryRepository groceryRepository;

    @ApiOperation(value = "List all groceries")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Grocery> allGroceries() {
        Iterable<Grocery> groceries = groceryRepository.findAll();
        return groceries;
    }

    @ApiOperation(value = "Get a single grocery")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Grocery getById(
            @ApiParam("id") @RequestParam Long id) {
        Grocery grocery = groceryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Grocery.class, id));

        return grocery;
    }

    @ApiOperation(value = "Create a new grocery")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Grocery postGrocery(
            @ApiParam("name (ex: Banana)") @RequestParam String name,
            @ApiParam("price (ex: 5.99)") @RequestParam String price,
            @ApiParam("expiration (ex: 05-18-23)") @RequestParam String expiration)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters


        Grocery grocery = new Grocery();
        grocery.setName(name);
        grocery.setPrice(price);
        grocery.setExpiration(expiration);

        Grocery savedGrocery = groceryRepository.save(grocery);

        return savedGrocery;
    }

    @ApiOperation(value = "Delete a Grocery")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteGrocery(
            @ApiParam("id") @RequestParam Long id) {
        Grocery grocery = groceryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Grocery.class, id));

        groceryRepository.delete(grocery);
        return genericMessage("Grocery with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single grocery")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Grocery updateGrocery(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Grocery incoming) {

        Grocery grocery = groceryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Grocery.class, id));

        grocery.setName(incoming.getName());
        grocery.setPrice(incoming.getPrice());
        grocery.setExpiration(incoming.getExpiration());

        groceryRepository.save(grocery);

        return grocery;
    }
}
