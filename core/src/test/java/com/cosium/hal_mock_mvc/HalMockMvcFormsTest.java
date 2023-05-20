package com.cosium.hal_mock_mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jr.ob.JSON;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    assertThat(myController.resourceByName).containsKey("john");
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

    assertThat(myController.resourceByName).isEmpty();
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

    assertThat(myController.resourceByName.get("john").city).isEqualTo("paris");
  }

  @Test
  @DisplayName("PUT multipart")
  void test4() throws Exception {
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

    assertThat(templates).hasSize(2).map(Template::key).contains("default", "create");
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

  @Controller
  @RequestMapping("/HalMockMvcFormsTest")
  public static class MyController {

    private final Map<String, HalFormsResource> resourceByName = new HashMap<>();
    private final Map<String, MultipartFile> fileById = new HashMap<>();
    private final WebMvcLinkBuilderFactory linkBuilders;

    public MyController(WebMvcLinkBuilderFactory linkBuilders) {
      this.linkBuilders = linkBuilders;
    }

    private void reset() {
      resourceByName.clear();
      fileById.clear();
    }

    public void addResource(String name) {
      resourceByName.put(name, new HalFormsResource(name));
    }

    @GetMapping
    public ResponseEntity<?> list() {
      Link link =
          linkTo(methodOn(MyController.class).list())
              .withSelfRel()
              .andAffordance(VoidAffordance.create())
              .andAffordance(afford(methodOn(MyController.class).create(null)));

      List<EntityModel<HalFormsResource>> resources =
          this.resourceByName.values().stream()
              .map(HalFormsResource::toEntityModel)
              .collect(Collectors.toList());
      return ResponseEntity.ok(
          CollectionModel.of(
              resources, link, linkTo(methodOn(MyController.class).getFile(null)).withRel("file")));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCommand command) {
      resourceByName.put(command.name, new HalFormsResource(command.name));

      return ResponseEntity.created(
              linkTo(methodOn(MyController.class).findByName(command.name)).toUri())
          .build();
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> findByName(@PathVariable("name") String name) {
      return ResponseEntity.of(
          Optional.ofNullable(resourceByName.get(name)).map(HalFormsResource::toEntityModel));
    }

    @PutMapping("/{name}/city")
    public ResponseEntity<?> changeCity(
        @PathVariable("name") String name, @RequestBody ChangeCityCommand command) {
      HalFormsResource resource = resourceByName.get(name);
      if (resource == null) {
        return ResponseEntity.notFound().build();
      }
      resource.city = command.city;
      return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteByName(@PathVariable("name") String name) {
      resourceByName.remove(name);
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<?> getFile(@RequestParam("id") String id) {
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
  }

  private static class HalFormsResource {
    private final String name;
    private String city;

    HalFormsResource(String name) {
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

    public EntityModel<HalFormsResource> toEntityModel() {
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
