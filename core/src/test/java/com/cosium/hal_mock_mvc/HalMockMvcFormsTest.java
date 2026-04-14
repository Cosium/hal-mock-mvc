package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.jr.ob.JSON;

/**
 * @author Réda Housni Alaoui
 */
@HalMockMvcBootTest
class HalMockMvcFormsTest {

  @Inject private MyController myController;

  @Inject private MockMvc mockMvc;

  @BeforeEach
  void beforeEach() {
    myController.reset();
  }

  @Test
  @DisplayName("POST template")
  void test1() throws Exception {
    String json = JSON.std.composeString().startObject().put("name", "john").end().finish();

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("create")
        .submit(json)
        .andExpect(status().isCreated());

    assertThat(myController.personByName).containsKey("john");
  }

  @Test
  @DisplayName("DELETE template")
  void test2() throws Exception {
    myController.addResource("john");

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).findByName("john")).toUri())
        .build()
        .follow()
        .templates()
        .byKey("deleteByName")
        .submit()
        .andExpect(status().isNoContent());

    assertThat(myController.personByName).isEmpty();
  }

  @Test
  @DisplayName("PUT template on special target")
  void test3() throws Exception {
    myController.addResource("john");

    String command = JSON.std.composeString().startObject().put("city", "paris").end().finish();

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).findByName("john")).toUri())
        .build()
        .follow()
        .templates()
        .byKey("changeCity")
        .submit(command)
        .andExpect(status().isNoContent());

    assertThat(myController.personByName.get("john").city).isEqualTo("paris");
  }

  @Test
  @DisplayName("PUT multipart")
  void test4() throws Exception {
    myController.fileById.put("foo", new MockMultipartFile("file", new byte[0]));

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow(Hop.relation("file").withParameter("id", "foo"))
        .templates()
        .byKey("uploadFile")
        .multipart()
        .file("file", new byte[] {0})
        .submit()
        .andExpect(status().isNoContent());

    assertThat(myController.fileById.get("foo").getBytes()).isEqualTo(new byte[] {0});
  }

  @Test
  @DisplayName("List templates by key")
  void test5() throws Exception {
    Collection<Template> templates =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
            .build()
            .follow()
            .templates()
            .list();

    assertThat(templates).hasSize(3).map(Template::key).contains("post", "create", "addFile");
    assertThat(templates)
        .filteredOn(template -> "create".equals(template.key()))
        .map(Template::representation)
        .extracting(
            TemplateRepresentation::method,
            TemplateRepresentation::contentType,
            TemplateRepresentation::target)
        .contains(tuple("POST", "application/json", Optional.empty()));
  }

  @Test
  @DisplayName("POST template then GET created resource")
  void test6() throws Exception {
    String json = JSON.std.composeString().startObject().put("name", "john").end().finish();

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("create")
        .createAndShift(json)
        .follow()
        .get()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("john"));
  }

  @Test
  @DisplayName("POST multipart then GET created resource")
  void test7() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("addFile")
        .multipart()
        .file("file", new byte[] {0})
        .createAndShift()
        .follow()
        .get()
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST template with form")
  void test8() throws Exception {
    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("create")
        .createForm()
        .withString("name", "john")
        .submit()
        .andExpect(status().isCreated());

    assertThat(myController.personByName).containsKey("john");
  }

  @Test
  @DisplayName("PUT template then GET updated resource")
  void test9() throws Exception {

    HalMockMvc.builder(mockMvc)
        .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
        .build()
        .follow()
        .templates()
        .byKey("create")
        .createAndShift(JSON.std.composeString().startObject().put("name", "john").end().finish())
        .follow()
        .templates()
        .byKey("changeCity")
        .submitAndExpect204NoContent(
            JSON.std.composeString().startObject().put("city", "Casablanca").end().finish())
        .follow()
        .get()
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value("Casablanca"));
  }

  @Test
  @DisplayName("submitAndExpect204NoContent fails if the response status is not 204")
  void test10() throws Exception {

    Template template =
        HalMockMvc.builder(mockMvc)
            .baseUri(linkTo(methodOn(MyController.class).list()).toUri())
            .build()
            .follow()
            .templates()
            .byKey("create");

    String createCommand =
        JSON.std.composeString().startObject().put("name", "john").end().finish();

    assertThatThrownBy(() -> template.submitAndExpect204NoContent(createCommand))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("Status expected:<204> but was:<201>");
  }

  @Controller
  @RequestMapping("/HalMockMvcFormsTest")
  public static class MyController {

    private final Map<String, PersonResource> personByName = new HashMap<>();
    private final Map<String, MultipartFile> fileById = new HashMap<>();
    private final WebMvcLinkBuilderFactory linkBuilders;

    public MyController(WebMvcLinkBuilderFactory linkBuilders) {
      this.linkBuilders = linkBuilders;
    }

    private void reset() {
      personByName.clear();
      fileById.clear();
    }

    public void addResource(String name) {
      personByName.put(name, new PersonResource(name));
    }

    @GetMapping
    public ResponseEntity<?> list() {
      Link link =
          linkTo(methodOn(MyController.class).list())
              .withSelfRel()
              .andAffordance(VoidAffordance.create())
              .andAffordance(afford(methodOn(MyController.class).create(null)))
              .andAffordance(afford(methodOn(MyController.class).addFile(null)));

      List<EntityModel<PersonResource>> resources =
          this.personByName.values().stream()
              .map(PersonResource::toEntityModel)
              .collect(Collectors.toList());
      return ResponseEntity.ok(
          CollectionModel.of(
              resources, link, linkTo(methodOn(MyController.class).getFile(null)).withRel("file")));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCommand command) {
      personByName.put(command.name, new PersonResource(command.name));

      return ResponseEntity.created(
              linkTo(methodOn(MyController.class).findByName(command.name)).toUri())
          .build();
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> findByName(@PathVariable("name") String name) {
      return ResponseEntity.of(
          Optional.ofNullable(personByName.get(name)).map(PersonResource::toEntityModel));
    }

    @PutMapping("/{name}/city")
    public ResponseEntity<?> changeCity(
        @PathVariable("name") String name, @RequestBody ChangeCityCommand command) {
      PersonResource resource = personByName.get(name);
      if (resource == null) {
        return ResponseEntity.notFound().build();
      }
      resource.city = command.city;
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/{name}/city")
    public ResponseEntity<?> getCity(@PathVariable("name") String name) {
      PersonResource resource = personByName.get(name);
      if (resource == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(Map.of("value", resource.city));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteByName(@PathVariable("name") String name) {
      personByName.remove(name);
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<?> getFile(@PathVariable("id") String id) {
      if (!fileById.containsKey(id)) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(
          new RepresentationModel<>(
              linkBuilders
                  .linkTo(methodOn(MyController.class).getFile(id))
                  .withSelfRel()
                  .andAffordance(VoidAffordance.create())
                  .andAffordance(afford(methodOn(MyController.class).uploadFile(id, null)))));
    }

    @PutMapping(value = "/files/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFile(
        @PathVariable("id") String id, @RequestParam("file") MultipartFile file) {
      fileById.put(id, file);
      return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/files", consumes = "multipart/form-data")
    public ResponseEntity<?> addFile(@RequestParam("file") MultipartFile file) {
      String id = UUID.randomUUID().toString();
      fileById.put(id, file);
      return ResponseEntity.created(
              linkBuilders.linkTo(methodOn(MyController.class).getFile(id)).toUri())
          .build();
    }
  }

  private record FileRepresentation(@JsonProperty String id) {}

  private static class PersonResource {
    private final String name;
    private String city;

    PersonResource(String name) {
      this.name = name;
    }

    @JsonProperty
    public String name() {
      return name;
    }

    @JsonProperty
    public String city() {
      return city;
    }

    public EntityModel<PersonResource> toEntityModel() {
      Link selfRel =
          linkTo(methodOn(MyController.class).findByName(name))
              .withSelfRel()
              .andAffordance(VoidAffordance.create())
              .andAffordance(afford(methodOn(MyController.class).deleteByName(name)))
              .andAffordance(afford(methodOn(MyController.class).changeCity(name, null)));
      return EntityModel.of(this, selfRel);
    }
  }

  private static class CreateCommand {
    private String name;

    @JsonProperty
    public String getName() {
      return name;
    }

    @JsonProperty
    public void setName(String name) {
      this.name = name;
    }
  }

  private static class ChangeCityCommand {
    private String city;

    @JsonProperty
    public String getCity() {
      return city;
    }

    @JsonProperty
    public void setCity(String city) {
      this.city = city;
    }
  }

  @RequestMapping("/void")
  public abstract static class VoidAffordance {

    public static Affordance create() {
      return WebMvcLinkBuilder.afford(methodOn(VoidAffordance.class).post());
    }

    @PostMapping
    public ResponseEntity<?> post() {
      return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
  }
}
