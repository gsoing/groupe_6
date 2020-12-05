package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.threeten.bp.OffsetDateTime;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * un verrou
 */
@Schema(description = "un verrou")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-12-05T07:54:55.839Z[GMT]")


public class Lock   {
  @JsonProperty("owner")
  private String owner = null;

  @JsonProperty("created")
  private OffsetDateTime created = null;

  public Lock owner(String owner) {
    this.owner = owner;
    return this;
  }

  /**
   * utilisateur propriétaire du verrou
   * @return owner
   **/
  @Schema(description = "utilisateur propriétaire du verrou")
  
    public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Lock created(OffsetDateTime created) {
    this.created = created;
    return this;
  }

  /**
   * date de la pose du verrou
   * @return created
   **/
  @Schema(description = "date de la pose du verrou")
  
    @Valid
    public OffsetDateTime getCreated() {
    return created;
  }

  public void setCreated(OffsetDateTime created) {
    this.created = created;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Lock lock = (Lock) o;
    return Objects.equals(this.owner, lock.owner) &&
        Objects.equals(this.created, lock.created);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, created);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Lock {\n");
    
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
