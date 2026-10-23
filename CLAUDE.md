# Project rules

## Process
- Follow DECISIONS.md; if something conflicts with it, ask me, do not decide.
- Work through PLAN.md one step at a time; tick the step off in the same commit.
- Tests first: show me the tests and their failures, then implement.
- ./mvnw verify must pass at the end of each step.
- Do not commit without my approval; one commit per step.
- Do not add anything that is not in the plan; ask first.

## Tech
- Java 21, no Lombok, no runtime dependencies.
- Tests: JUnit 5 + AssertJ.

## Architecture
- Package com.sportradar.scoreboard; exceptions in com.sportradar.scoreboard.exception.
- Public API: Scoreboard interface, Match record, exceptions, and a public final
  implementation class.
- Value types are immutable records; never return mutable internal state.
- Team-name normalisation (trim, ignore case) lives in one place; never build keys by
  joining strings.
- No repository/service layers.
- Javadoc on public API only.
