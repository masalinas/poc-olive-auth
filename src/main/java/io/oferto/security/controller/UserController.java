package io.oferto.security.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.oferto.security.config.KeyCloakConfig;
import io.oferto.security.dto.LoginRequest;
import io.oferto.security.dto.LoginResponse;
import io.oferto.security.dto.UserRequest;
import io.oferto.security.service.LoginService;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;

@RestController
@RequestMapping("iam")
public class UserController {
	Logger log = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    LoginService loginService;
    
	@RequestMapping(value = "/{realm}/users", method = RequestMethod.GET)
    public List<UserRepresentation> getUsers(@PathVariable("realm") String realm) throws Exception {
        log.info("Executing getUsers");
                
    	UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();    	
    	
    	List<UserRepresentation> userRepresentations = usersResource.list();
    	    	
        return userRepresentations;
    }
	
	@RequestMapping(value = "/{realm}/users/{id}", method = RequestMethod.GET)
    public UserRepresentation getUse(@PathVariable("realm") String realm, @PathVariable("id") String id) throws Exception {
        log.info("Executing getUser By Id");
                
    	UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();    	
    	
    	List<UserRepresentation> userRepresentations = usersResource.list();

    	UserRepresentation userRepresentationById = null;
    	
    	for (final UserRepresentation userRepresentation : userRepresentations) { 
    		if (userRepresentation.getId().equals(id))
    			userRepresentationById = userRepresentation;
    		
    	};
    	
        return userRepresentationById;
    }
	
	@RequestMapping(value = "/{realm}/clients", method = RequestMethod.GET)
    public List<ClientRepresentation> getClients(@PathVariable("realm") String realm) throws Exception {
        log.info("Executing getClients");
                    	
    	ClientsResource clientsResource = KeyCloakConfig.getInstance().realm(realm).clients();
    	
    	List<ClientRepresentation> clientRepresentations = clientsResource.findAll();    	    
    	
        return clientRepresentations;
    }
	
	@RequestMapping(value = "/{realm}/users/{id}/roles", method = RequestMethod.GET)
    public Map<String, List<String>> getRoles(@PathVariable("realm") String realm, @PathVariable("id") String id) throws Exception {
        log.info("Executing getRoles");
                    	
    	UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();
    	    	
    	List<UserRepresentation> userRepresentations = usersResource.list();
    	
    	Map<String, List<String>> roles = null;
    	
    	for (final UserRepresentation userRepresentation : userRepresentations) { 
    		if (userRepresentation.getId().equals(id))
    			roles = userRepresentation.getClientRoles();
    		
    	};
    	
        return roles;
    }
	
	@RequestMapping(value = "/{realm}/users", method = RequestMethod.POST)
	public String createUser(@PathVariable("realm") String realm, @RequestBody UserRequest userRequest) {
		log.info("Executing createUser");
		
		UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();
		
	   	UserRepresentation user = new UserRepresentation();
	    user.setEnabled(userRequest.isEnabled());
	    user.setUsername(userRequest.getUsername());
	    user.setFirstName(userRequest.getFirstName());
	    user.setLastName(userRequest.getLastName());
	    user.setEmail(userRequest.getEmail());
	    
	    for (Entry<String, List<String>> entry : userRequest.getAttributes().entrySet()) {	    	
	    	user.setAttributes(Collections.singletonMap(entry.getKey(), entry.getValue()));
	    }
	    	         
	    Response response = usersResource.create(user);
	    
	    return CreatedResponseUtil.getCreatedId(response);	    
	}

	@RequestMapping(value = "/{realm}/users/{id}", method = RequestMethod.PUT)
	public void updateUser(@PathVariable("realm") String realm, @PathVariable("id") String id, @RequestBody UserRequest userRequest) {
		log.info("Executing updateUser");
		
		UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();
			 
		UserResource userResource = usersResource.get(id);
		
		UserRepresentation user = new UserRepresentation();
	    user.setEnabled(userRequest.isEnabled());
	    user.setUsername(userRequest.getUsername());
	    user.setFirstName(userRequest.getFirstName());
	    user.setLastName(userRequest.getLastName());
	    user.setEmail(userRequest.getEmail());
	    
	    for (Entry<String, List<String>> entry : userRequest.getAttributes().entrySet()) {	    	
	    	user.setAttributes(Collections.singletonMap(entry.getKey(), entry.getValue()));
	    }
	    
	    userResource.update(user);
	}
	
	@RequestMapping(value = "/{realm}/users/{id}", method = RequestMethod.DELETE)
	public int deleteUser(@PathVariable("realm") String realm, @PathVariable("id") String id) {
		log.info("Executing deleteUser");
		
		UsersResource usersResource = KeyCloakConfig.getInstance().realm(realm).users();
			    	         
	    Response response = usersResource.delete(id);
	    	 
	    return response.getStatus();
	}
	
    @PostMapping("/{realm}/login")
    public ResponseEntity<LoginResponse> login(@PathVariable("realm") String realm, @RequestBody LoginRequest loginRequest) throws Exception {
        log.info("Executing login");
                        
        ResponseEntity<LoginResponse> response = loginService.login(realm, loginRequest);

        return response;
    }
    
    @PostMapping("/{realm}/{refreshToken}/logout")
    public ResponseEntity<String> logout(@PathVariable("realm") String realm, @PathVariable("refreshToken") String refreshToken) throws Exception {
        log.info("Executing logout");
                        
        ResponseEntity<String> response = loginService.logout(realm, refreshToken);

        return response;
    }
}
