# holiday-api

## Overview

The project is a Spring Boot application that provides APIs to do the following:
- Given a country, return the last celebrated 3 holidays (date and name).
- Given a year and country codes, for each country return a number of public holidays not falling on weekends (sort in descending order).
- Given a year and 2 country codes, return the deduplicated list of dates celebrated in both countries (date + local names).

## How to run the project
1. Clone the repository:
   ```bash
   https://github.com/Rameswaree/holiday-api.git
   
2. Run the project using the following commands:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. Alternatively, you can run the project using the following command:
   ```bash
   docker-compose up
   ```
4. Test the APIs given in the Postman collection within the project using Postman.

## Additional validations built into the APIs
- The APIs are designed to handle invalid country codes, returning appropriate error messages.
- The APIs ensure that the year provided is valid and within the range available in Nager, any value not in range of 1975 to 2075 will return an error.
- When multiple country codes are provided and if one of the country codes is invalid, the API will return an error message as it is not ideal to return holidays for valid countries while ignoring the invalid ones.

## To-do (Beyond MVP functionality)
- ConcurrentMapCacheManager is used for caching (in-memory). Redis will be used to cache the results of the APIs for production use.
- Make the token dynamic for the APIs.
- Update the security configuration to use username and password from the application.properties file for multiple environments.