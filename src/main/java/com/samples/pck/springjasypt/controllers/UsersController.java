package com.samples.pck.springjasypt.controllers;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.samples.pck.springjasypt.entity.Users;
import com.samples.pck.springjasypt.services.UsersService;

@RestController
public class UsersController {
	
	@Autowired
	UsersService userService;
	
	@GetMapping(value="/user/{id}", produces= {"application/json", "application/xml"})
	public @ResponseBody ResponseEntity<Users> getUserById(@PathVariable("id") Long userId){
		ResponseEntity<Users> response = null;
		Users user = null;
		try {
		
			user = userService.getUserById(userId);
		
		}catch(NoSuchElementException nsee) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such element found");
		}
		catch(Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please contact application administrator!");
		}
		
		response = new ResponseEntity<Users>(user, HttpStatus.OK);
		return response;		
	}
	
	@PostMapping(value = "/user", consumes = {"application/json", "application/xml"}, produces= {"application/json", "application/xml"})
	public @ResponseBody ResponseEntity<Users> addUser(@RequestBody Users user){
		ResponseEntity<Users> response = null;
		Users rUser = null;
		try {
		
			rUser = userService.addUser(user);
			
		}
		catch(IllegalAccessException iaex) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Incorrect access of illegal access to the resource!");
		}
		catch(Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please contact application administrator!");
		}
		
		response = new ResponseEntity<Users>(rUser, HttpStatus.OK);
		return response;		
	}
	
	@PutMapping(value = "/user/{id}", consumes= {"application/json", "application/xml"}, produces= {"application/json", "application/xml"})
	public @ResponseBody ResponseEntity<Users> updateUser(@PathVariable("id") Long userId, @RequestBody Users user){

		Users rUser = null;

		try {
			
			Users persistedUser = userService.getUserById(userId);
			
			if(!userId.equals(user.getUserId())) {
				throw new IllegalAccessException("Illegal access!!");
			}else if(!user.getUsername().equals(persistedUser.getUsername())) {
				throw new IllegalAccessException("Illegal access!!");
			}
			
			rUser = userService.updateUser(user);
			
		}catch(NoSuchElementException nsee) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such element found");
		}
		catch(IllegalAccessException iaex) {
			iaex.printStackTrace();
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Illegal access to the resource!!");
		}
		catch(Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Please contact application administrator!");
		}

		return new ResponseEntity<Users>(rUser, HttpStatus.OK);	
	}
	
}
