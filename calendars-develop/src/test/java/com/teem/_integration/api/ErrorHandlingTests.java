package com.UoU._integration.api;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

import com.UoU.app.exceptions.NotFoundStatusException;
import com.UoU.app.security.Authorize;
import com.UoU.app.security.CustomClaims;
import com.UoU.core.Noop;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.validation.ViolationException;
import com.UoU.infra.db.InvalidCursorException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class ErrorHandlingTests extends BaseApiIntegrationTest {
  @Override
  protected String getBasePath() {
    return ErrorHandlingTestsController.BASE_PATH;
  }

  @Test
  void invalidRoute_should404() {
    restAssured()
        .get("invalid-route")
        .then()
        .statusCode(404)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void notFoundExceptions_should404() {
    List.of(
        NotFoundStatusException.class.getName(),
        NotFoundException.class.getName(),
        NoDataFoundException.class.getName()
    ).forEach(exception -> restAssured()
        .get("throw/{exception}", exception)
        .then()
        .statusCode(404)
        .body("error", not(blankOrNullString())));
  }

  @Test
  void badDataExceptions_should400() {
    List.of(
        HttpMessageNotReadableException.class.getName(),
        ValidationException.class.getName(),
        IllegalOperationException.class.getName(),
        InvalidCursorException.class.getName()
    ).forEach(exception -> restAssured()
        .get("throw/{exception}", exception)
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString())));
  }

  @Test
  void violationException_should400WithViolations() {
    restAssured()
        .get("throw/{exception}", ViolationException.class.getName())
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()))
        .body("violations.field.size()", greaterThan(0));
  }

  @Test
  void constraintViolationException_should400WithViolations() {
    var invalidRequest = Map.of("name", "", "email", "invalid");

    restAssuredJson(invalidRequest)
        .post("validate")
        .then()
        .statusCode(400)
        .body("error", not(blankOrNullString()))
        .body("violations.field.size()", greaterThan(0));
  }

  @Test
  void illegalArgumentException_should500() {
    restAssured()
        .get("throw/{exception}", IllegalArgumentException.class.getName())
        .then()
        .statusCode(500)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void missingJwt_should401() {
    restAssured()
        .auth().none()
        .get()
        .then()
        .statusCode(401)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void missingJwtSub_should401() {
    restAssuredJson()
        .auth().oauth2(
            auth.createJwt(auth.buildClaimsWithFullAccess().subject(" ").build()))
        .get()
        .then()
        .statusCode(401)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void missingJwtOrgId_should401() {
    restAssuredJson()
        .auth().oauth2(auth.createJwt(
            auth.buildClaimsWithFullAccess().claim(CustomClaims.ORG_ID, " ").build()))
        .get()
        .then()
        .statusCode(401)
        .body("error", not(blankOrNullString()));
  }

  @Test
  void missingJwtScope_should403() {
    restAssuredJson()
        .auth().oauth2(auth.createJwtWithScope("invalid-test-scope"))
        .get("diagnostics-scope")
        .then()
        .statusCode(403)
        .body("error", not(blankOrNullString()));
  }
}

/**
 * Test controller that throws exceptions to test how they're handled.
 */
@RestController
@RequestMapping(ErrorHandlingTestsController.BASE_PATH)
class ErrorHandlingTestsController {
  public static final String BASE_PATH = "/test/error-handling";

  @Autowired Validator validator;

  @GetMapping("throw/{exception}")
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public String throwException(@PathVariable String exception) {
    val cls = Class.forName(exception);

    // Handle some special cases for exceptions that need special instantiation:
    if (cls == ViolationException.class) {
      throw ViolationException.forField("someField", "someField is invalid");
    }

    // Find a suitable ctor based on common exception ctors, and throw:
    throwIfCtor(cls); // parameterless
    throwIfCtor(cls, Pair.of(String.class, "message"));
    throwIfCtor(
        cls,
        Pair.of(String.class, "message"),
        Pair.of(Throwable.class, new RuntimeException()));

    return "Expected to throw, but couldn't find a suitable exception ctor.";
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  @SafeVarargs
  private static void throwIfCtor(Class cls, Pair<Class, Object>... params) {
    val paramTypes = Arrays.stream(params).map(x -> x.getLeft()).toArray(Class[]::new);
    Constructor<?> ctor;

    try {
      ctor = cls.getDeclaredConstructor(paramTypes);
    } catch (NoSuchMethodException ex) {
      return; // ctor not found, so don't throw.
    }

    val paramValues = Arrays.stream(params).map(x -> x.getRight()).toArray();
    throw (Throwable) ctor.newInstance(paramValues);
  }

  @PostMapping("validate")
  public void validate(@Valid @RequestBody TestThing testThing) {
    Noop.because("@Valid will cause TestThing to validate");
  }

  @Authorize.Diagnostics
  @GetMapping("diagnostics-scope")
  public String diagnosticsScope() {
    return "";
  }

  private record TestThing(
      @NotEmpty String name,
      @NotEmpty @Email String email) {
  }
}
