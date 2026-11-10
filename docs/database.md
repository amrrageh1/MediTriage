# Database setup status

The archive contains no backend and no complete application-compatible Oracle migration. `sample_schema.sql` preserves the original table definitions and alterations, but its columns differ from the Android API models. It omits the history summary and audit tables, ID generation, and backend mappings. Do not treat it as a deployable schema.

Before running the application, supply the Node.js backend that implements `app/src/main/java/com/example/meditriage/ApiService.kt`, plus its dependency manifest, configuration example, complete Oracle migrations and seed data.

Use a dedicated development schema instead of SYSTEM-owned application tables. Have the database administrator provision credentials outside Git, grant CREATE SESSION and only the object permissions the runtime requires, and apply bounded storage quotas. Keep schema migrations separate from the runtime database identity. Do not grant RESOURCE or UNLIMITED TABLESPACE to the runtime account by default.

The old SQL files contained a literal password. Rotate it wherever it was used, including reused credentials. Removing it from current files does not remove it from Git history, downloaded ZIPs, or forks. Review database access logs and coordinate history cleanup separately after rotation.
