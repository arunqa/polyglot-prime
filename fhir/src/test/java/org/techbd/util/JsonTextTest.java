package org.techbd.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JsonTextTest {

    private final JsonText jsonText = new JsonText();

    @Test
    void testValidUntypedJson() {
        String jsonString = """
        {
            "name": "John Doe",
            "age": 30
        }
        """;

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);
        
        assertThat(result).isInstanceOf(JsonText.JsonObjectResult.ValidUntypedResult.class);
        assertThat(result.isValid()).isTrue();
        assertThat(result.originalText()).isEqualTo(jsonString);
        
        Map<String, Object> jsonObject = ((JsonText.JsonObjectResult.ValidUntypedResult) result).jsonObject();
        assertThat(jsonObject).containsEntry("name", "John Doe")
                              .containsEntry("age", 30);
    }

    @Test
    void testValidTypedJson() {
        String jsonString = """
        {
            "$class": "org.techbd.util.JsonTextTest$SyntheticDynamicClassFixture",
            "name": "John Doe",
            "age": 30
        }
        """;

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);
        
        assertThat(result).isInstanceOf(JsonText.JsonObjectResult.ValidResult.class);
        assertThat(result.isValid()).isTrue();
        assertThat(result.originalText()).isEqualTo(jsonString);
        
        Map<String, Object> jsonObject = ((JsonText.JsonObjectResult.ValidResult<?>) result).jsonObject();
        assertThat(jsonObject).containsEntry("name", "John Doe")
                              .containsEntry("age", 30);
        
        Object instance = ((JsonText.JsonObjectResult.ValidResult<?>) result).instance();
        assertThat(instance).isInstanceOf(SyntheticDynamicClassFixture.class);
        
        SyntheticDynamicClassFixture myClassInstance = (SyntheticDynamicClassFixture) instance;
        assertThat(myClassInstance.getName()).isEqualTo("John Doe");
        assertThat(myClassInstance.getAge()).isEqualTo(30);
    }

    @Test
    void testClassNotFound() {
        String jsonString = """
        {
            "$class": "org.techbd.util.NonExistentClass",
            "name": "John Doe",
            "age": 30
        }
        """;

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);
        
        assertThat(result).isInstanceOf(JsonText.JsonObjectResult.ValidResultClassNotFound.class);
        assertThat(result.isValid()).isFalse();
        assertThat(result.originalText()).isEqualTo(jsonString);
        
        Map<String, Object> jsonObject = ((JsonText.JsonObjectResult.ValidResultClassNotFound) result).jsonObject();
        assertThat(jsonObject).containsEntry("name", "John Doe")
                              .containsEntry("age", 30);
        
        assertThat(((JsonText.JsonObjectResult.ValidResultClassNotFound) result).className())
                .isEqualTo("org.techbd.util.NonExistentClass");
    }

    @Test
    void testClassNotInstantiated() {
        String jsonString = """
        {
            "$class": "org.techbd.util.JsonTextTest$SyntheticDynamicClassFixtureWithImproperConstructor",
            "name": "John Doe"
        }
        """;

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);
        
        assertThat(result).isInstanceOf(JsonText.JsonObjectResult.ValidResultClassNotInstantiated.class);
        assertThat(result.isValid()).isFalse();
        assertThat(result.originalText()).isEqualTo(jsonString);
        
        Map<String, Object> jsonObject = ((JsonText.JsonObjectResult.ValidResultClassNotInstantiated) result).jsonObject();
        assertThat(jsonObject).containsEntry("name", "John Doe");
        
        assertThat(((JsonText.JsonObjectResult.ValidResultClassNotInstantiated) result).className())
                .isEqualTo("org.techbd.util.JsonTextTest$SyntheticDynamicClassFixtureWithImproperConstructor");
        
        Exception exception = ((JsonText.JsonObjectResult.ValidResultClassNotInstantiated) result).exception();
        assertThat(exception).isNotNull();
    }

    @Test
    void testInvalidJson() {
        String jsonString = "invalid json";

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);
        
        assertThat(result).isInstanceOf(JsonText.JsonObjectResult.InvalidResult.class);
        assertThat(result.isValid()).isFalse();
        assertThat(result.originalText()).isEqualTo(jsonString);
        
        List<Exception> exceptions = ((JsonText.JsonObjectResult.InvalidResult) result).exceptions();
        assertThat(exceptions).hasSize(1)
                              .first().isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class);
    }

    @Test
    void testSwitchMatcher() {
        String jsonString = """
        {
            "$class": "org.techbd.util.JsonTextTest$SyntheticDynamicClassFixture",
            "name": "John Doe",
            "age": 30
        }
        """;

        JsonText.JsonObjectResult result = jsonText.getJsonObject(jsonString);

        switch (result) {
            case JsonText.JsonObjectResult.ValidUntypedResult validUntypedResult -> {
                assertThat(validUntypedResult.isValid()).isTrue();
                assertThat(validUntypedResult.jsonObject()).containsEntry("name", "John Doe")
                                                           .containsEntry("age", 30);
            }
            case JsonText.JsonObjectResult.ValidResult<?> validResult -> {
                assertThat(validResult.isValid()).isTrue();
                assertThat(validResult.jsonObject()).containsEntry("name", "John Doe")
                                                    .containsEntry("age", 30);
                Object instance = validResult.instance();
                assertThat(instance).isInstanceOf(SyntheticDynamicClassFixture.class);
                SyntheticDynamicClassFixture myClassInstance = (SyntheticDynamicClassFixture) instance;
                assertThat(myClassInstance.getName()).isEqualTo("John Doe");
                assertThat(myClassInstance.getAge()).isEqualTo(30);
            }
            case JsonText.JsonObjectResult.ValidResultClassNotFound classNotFoundResult -> {
                fail("Class should be found, but it was not.");
            }
            case JsonText.JsonObjectResult.ValidResultClassNotInstantiated classNotInstantiatedResult -> {
                fail("Class should be instantiated, but it was not.");
            }
            case JsonText.JsonObjectResult.InvalidResult invalidResult -> {
                fail("JSON should be valid, but it was not.");
            }
        }
    }

    public static class SyntheticDynamicClassFixture {
        private final String name;
        private final int age;

        public SyntheticDynamicClassFixture(Map<String, Object> jsonMap, String originalText) {
            this.name = (String) jsonMap.get("name");
            this.age = (int) jsonMap.getOrDefault("age", 0); // Provide default value to handle missing "age"
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "SyntheticDynamicClassFixture{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    public static class SyntheticDynamicClassFixtureWithImproperConstructor {
        private final String name;
        private final int age;

        // this should really have two parameters, not just one
        public SyntheticDynamicClassFixtureWithImproperConstructor(Map<String, Object> jsonMap) {
            this.name = (String) jsonMap.get("name");
            this.age = (int) jsonMap.getOrDefault("age", 0); // Provide default value to handle missing "age"
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "SyntheticDynamicClassFixture{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
