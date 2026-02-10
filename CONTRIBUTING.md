# Contributing to mjml-java

Thanks for contributing.

## Development setup

1. Fork and clone the repository.
2. Use Java 17+ and Maven 3.8+.
3. Run:

```bash
mvn clean verify
```

## Project modules

- `mjml-java-core`
- `mjml-java-resolvers`
- `mjml-java-spring`
- `mjml-java-bom`

## Pull request expectations

- Keep changes focused and small.
- Include tests for behavior changes.
- Update docs/Javadocs when public behavior changes.
- Keep compatibility in mind (especially output expectations and security behavior).

## Testing

Run all tests:

```bash
mvn clean verify
```

Run one module:

```bash
mvn -pl mjml-java-core test
```

## Commit guidance

- Use clear commit messages.
- Prefer one concern per commit.

## Reporting issues

Please use the issue templates and include:
- Reproduction input (MJML)
- Actual output/error
- Expected output/behavior
- Java and OS versions

## Additional docs

- Main documentation: https://jcputney.github.io/mjml-java/
- API and guides in `docs/docs/`
