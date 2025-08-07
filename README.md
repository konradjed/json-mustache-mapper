# JSON Mustache Mapper

A Java library for mapping JSON structures using Mustache templates with JSONPath support.

## Features
- Map source JSON to target JSON structure
- Mustache template processing
- JSONPath fragment extraction
- Array processing with `[*]` notation
- Multiple mapper types (MUSTACHE, COPY, TRANSFORM)

## Quick Start

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. Run the example:
   ```bash
   java -jar target/json-mustache-mapper-1.0.0-jar-with-dependencies.jar
   ```
3. Use in your code:
    ```java
    JsonMustacheGenerator generator = new JsonMustacheGenerator();
    
    Map<String, Object> config = Map.of(
    "client", Map.of(
    "Orders", Map.of(
    "mapperType", "MUSTACHE",
    "templateName", "ORDER_DETAIL",
    "jsonPath", "$.orders[*]"
    )
    )
    );
    
    String result = generator.mapJsonStructure(sourceJson, config);
    ```

## Configuration
Mapper Types

MUSTACHE: Process with Mustache template
COPY: Direct copy of JSON fragment
TRANSFORM: Custom transformations (extensible)

## JSONPath Examples

* `$.orders[*]` - All order items
* `$.user` - User object
* `$.orders[0]` - First order only
* `$.user.profile.preferences` - Nested object

## Dependencies

Jackson 2.15.2 (JSON processing)
Mustache Java 0.9.10 (templating)
JUnit 5.9.3 (testing)

## License
MIT License