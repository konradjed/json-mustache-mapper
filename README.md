# JSON Mustache Mapper

A Java library for mapping JSON structures using Mustache templates with JSONPath support, including **multi-source template functionality**.

## Features
- Map source JSON to target JSON structure
- Mustache template processing with **multiple data sources**
- JSONPath fragment extraction
- Array processing with `[*]` notation
- Multiple mapper types (MUSTACHE, COPY, TRANSFORM)
- **NEW: Multi-source Mustache templates** - combine data from multiple JSONPath expressions

## Quick Start

1. **Build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the basic example:**
   ```bash
   java -jar target/json-mustache-mapper-1.0.0-jar-with-dependencies.jar
   ```

3. **Run the multi-source demo:**
   ```bash
   mvn exec:java -Dexec.mainClass="it.jedrzejewski.mustachemapper.MultiSourceDemo"
   ```

4. **Use in your code:**
    ```java
    JsonStructureMapper mapper = new JsonStructureMapper();
    
    // Single source (traditional)
    Map<String, Object> config = Map.of(
        "client", Map.of(
            "Orders", Map.of(
                "mapperType", "MUSTACHE",
                "templateName", "ORDER_DETAIL",
                "jsonPath", "$.orders[*]"
            )
        )
    );
    
    // Multi-source (NEW!)
    Map<String, Object> multiConfig = Map.of(
        "client", Map.of(
            "Orders", Map.of(
                "mapperType", "MUSTACHE",
                "templateName", "ORDER_DETAIL",
                "jsonPath", Arrays.asList("$.orders[*]", "$.user")
            )
        )
    );
    
    String result = mapper.transformJsonStructure(sourceJson, config);
    ```

## Multi-Source Templates (NEW!)

Combine data from multiple JSONPath expressions in a single template:

```json
{
    "Orders": {
        "mapperType": "MUSTACHE",
        "templateName": "ORDER_DETAIL",
        "jsonPath": ["$.orders[*]", "$.user"]
    }
}
```

**Template:**
```mustache
Order ID: {{orderId}}
Product: {{productName}}
Customer: {{source2.name}} ({{source2.email}})
```

- Primary data (`$.orders[*]`): Direct access with `{{property}}`
- Additional data (`$.user`): Access via `{{source2.property}}`

See [MULTI_SOURCE_MUSTACHE.md](MULTI_SOURCE_MUSTACHE.md) for complete documentation.

## Configuration
### Mapper Types

- **MUSTACHE**: Process with Mustache template (supports multi-source)
- **COPY**: Direct copy of JSON fragment
- **TRANSFORM**: Custom transformations (extensible)

### JSONPath Examples

- `$.orders[*]` - All order items
- `$.user` - User object  
- `$.orders[0]` - First order only
- `$.user.profile.preferences` - Nested object
- `["$.orders[*]", "$.user", "$.settings"]` - **Multi-source array**

## Dependencies

- Jackson 2.15.2 (JSON processing)
- Mustache Java 0.9.10 (templating)
- JUnit 5.9.3 (testing)

## License
MIT License