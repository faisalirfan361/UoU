# Dtos

## Annotations

Since validation happens in core, DTOs are primarily annotated for OpenAPI doc generation.

- Use `@Schema(required = true)` for required fields instead of any others like `@NotNull`.
- Use `@Schema(nullable = true)` for nullable fields instead of any others like `@Nullable`.

Note that a field can be both required and nullable. Required means the key must exist in JSON,
while nullable means the value can actually be null.

Other annotations (like in `javax.validation`) can do some of the same things as `@Schema` but with
subtle and unexpected differences. Stick to `@Schema` for consistency.

### SchemaExt helper

There are a few custom extensions you can use in [SchemaExt](../../docs/SchemaExt.java) to make
things easier and reduce annotation boilerplate.
