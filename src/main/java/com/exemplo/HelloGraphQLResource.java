package com.exemplo;

import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import static com.exemplo.security.SecurityRoles.API_CLIENT;

@GraphQLApi
public class HelloGraphQLResource {

  @Query
  @Description("Say hello")
  @RolesAllowed(API_CLIENT)
  public String sayHello(@DefaultValue("World") String name) {
    return "Hello " + name;
  }
}