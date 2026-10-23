# AI usage

## Summary

The whole solution was built with **Claude Code** (Anthropic's CLI agent, model Claude Opus 5.5), working directly in this repository. The AI read the task PDF, proposed decisions for the open questions, wrote the plan, the tests, the code and the documentation, and ran every build. I directed the work, made or approved every decision, reviewed each step and approved every commit.

How the work was organised:

1. **Requirements first, no code.** The AI listed the requirements and the open questions from the PDF. The open questions became [DECISIONS.md](DECISIONS.md): options for each question, then the AI's recommendations, which I reviewed and accepted (after challenging D17, thread safety).
2. **Plan as a checklist.** [PLAN.md](PLAN.md) split the work into steps, one commit per step, tests first. I reviewed the plan and added test cases before any code was written.
3. **Rules for the assistant.** [CLAUDE.md](CLAUDE.md) fixed the process (tests first and shown failing, `./mvnw verify` green, no commit without approval, nothing outside the plan without asking) and the architecture rules.
4. **Step by step, test first.** For every step the AI wrote the tests, ran them and showed them failing, implemented, ran `./mvnw verify` and asked before committing. When a new test passed immediately because the behaviour already existed, the AI temporarily broke the code on purpose to show the test could fail, then restored it.
5. **Review.** After the required features were done, I asked for a strict review of the whole repository. Its findings (below) were fixed in separate commits, each again test first.

### Where human review mattered

Several real bugs were found by reviewing the AI's output, including review in a separate Claude session, rather than accepting it:

- A team already playing in a *different* match needed its own exception (`TeamAlreadyPlayingException`), not "match already started".
- Match keys built by joining names could collide ("AB"/"CDE" vs "ABC"/"DE"); a test pins this.
- Swapped home/away teams must not update or finish a match.
- `strip()` does not remove no-break spaces (`Spain\u00A0`); whitespace *inside* names (`Costa\u00A0Rica` vs `Costa Rica`) let the same team play twice.
- The total score could overflow `int` and sort a match last.
- A rematch must count as the most recently started match; finishing a match must free both teams for other matches.
- `TeamName` equality should ignore case.

### Mistakes the AI made and corrected

Recorded for transparency, since they show what needed checking:

- Implemented `Match.toString` before writing its test, and added a test outside the plan; both flagged by the AI itself and handled.
- Wrote Unicode escapes in a test as invisible literal characters; converted back to `\uXXXX` escapes.
- Wrote a concurrency test whose readers stopped too early and whose worker errors were swallowed; fixed before the first run.
- Generated exception classes with a shell loop that mangled the Javadoc and messages; no test checked messages, so message assertions were added first and shown failing, then the files were rewritten.
- Hard-coded a thread count in a test helper; replaced with the variable.
- Did not notice that an incremental build hid the new compiler warnings; `clean verify` showed them.

Everything was verified by running it: tests, the build, `jshell` runs of the README examples, and the enforcer with an older JDK.

## Artifacts that guided the implementation

| Artifact | Role |
|---|---|
| Task PDF (not committed) | Requirements and example scenario |
| [DECISIONS.md](DECISIONS.md) | Every open question, the options considered, the decision and why |
| [PLAN.md](PLAN.md) | Test-first checklist, one commit per step; ticked off in the same commit as the work |
| [CLAUDE.md](CLAUDE.md) | Rules the assistant had to follow (process, tech, architecture) |
| Review findings | Strict review of the finished repository; summarised below |
| Git history | One commit per step or fix, in the order below |

### Review findings and how they were resolved

| ID | Finding | Commit |
|---|---|---|
| H1 | Invisibly different names (decomposed accents, zero-width space) let the same team play twice | `34d3346` |
| H2 | Invisible or control-character names were accepted | `34d3346` |
| M1 | `Match` did not validate its fields | `ac7d1d7` |
| M2 | `TeamName` could be created without normalisation | `c19f02e` |
| M3 | Documented behaviour without tests | `765aa9a` |
| M4 | Exceptions carried team names only in the message | `9063b58` |
| L1–L7 | `serialVersionUID`, Javadoc gaps, non-final exceptions, duplicate check, undocumented linear scan, no build enforcement, outdated test name | `bc9a7f2` |
| D1–D7 | Out-of-date or inaccurate statements in the documents | `41881fd` |

### Commit history

```
827a452 Add .gitignore for Maven, IntelliJ and macOS
f5c7872 Add draft of open design questions
14b52d6 Record design decisions
d4ce6c2 Add implementation plan
c4e1b96 Add Maven project skeleton
e0219f0 Add project rules for Claude Code
f10d2f4 Add starting a match
4d798a2 Add updating the score
b028fc8 Add finishing a match
6f874ff Order summary by total score and start order
61103d8 Make Scoreboard thread-safe
1c83a18 Add README
4fa711b Normalise whitespace inside team names
86ba8b1 Add finding a match by team          <- extra feature, distinct commit
34d3346 Normalise Unicode and reject control characters in team names
2c96743 Prevent total score overflow in summary ordering
ac7d1d7 Validate Match fields
c19f02e Normalise team names in the TeamName constructor
765aa9a Cover documented argument validation and concurrent lookups
9063b58 Expose team names on scoreboard exceptions
bc9a7f2 Tighten build checks and fix low-level review findings
41881fd Fix documentation review findings
```

## Prompt history

My prompts, verbatim and in order (typos included). Where the assistant asked a multiple-choice question, my answer is given in brackets. The assistant's replies are not reproduced; their results are the commits and documents above.

**Requirements and decisions**

1. read the pdf and list the requirements + things the task leaves open, do not write code yet
2. save open quetions to DECISIONS.md, each one gets a question and options, and Decision: TBD line
3. create .gitignore for maven, intelij, macos, and the pdf file, commit it as the frist commit
4. commit decisions.md now so we got the draft
5. go through decisions.md and give your suggestions with a very short descriptions, put it in a table
6. why did you choose B over C at D17?
7. the choices are ok, update decisions.md TBD lines and add line with short description below (as you did in the table above)
8. commit

**Plan**

9. make a plan based on pdf file and deccision.md, maven pom first, one commit per step but ask before commit, test first
10. comments to the plan: step 2: a team that's already playing → MatchAlreadyStartedException change to TeamAlreadyPlayingException (spain - poland but poland - brasil is live so no match is present but the team is playing), also test that "AB" vs "CDE" and "ABC vs "DE" are different matches (keybuild), step 3: add tests for swapped teams poland - spain can not be updated with spain - poland, step: 5 test immutable snapshot - summary before update. UPDATE the plan and show the changes
11. save the plan as PLAN.md as a checklist
12. yes also check that finishing with th swapped teams fails
13. commit the plan

**Implementation (Steps 1–8)**

14. yeah start step 1
15. add maven wrapper
16. commit step 1
17. proceed to step 2
18. before you commit, create CLAUDE.md file with the following: *(the rules now in [CLAUDE.md](CLAUDE.md))* [Answers: implementation class name InMemoryScoreboard; refactor within the Step 2 commit; keep the three extra tests; commit CLAUDE.md on its own first]
19. refactor TeamName, move it to package private file, remove parameterName arg, use strip() instead of trim(), add TeamNameTest with unicode space, ignore case, null/blank, verify
20. check the non breaking  spaces like  Spain\u00A0
21. commit step 2
22. proceed to step 3
23. commit step 3
24. proceed to step 4
25. add updateScore and finishMatch  tests with different case/white space (" poland" "SPAIN "), samePairingCanBeStartedAgainAfterFinishing covers the same pairing only, we need a test that: finishes poland - spain and starts poland-mexico and spain-germany
26. commit step 4
27. proceed to step 5
28. change start time to start order in getSummary java doc (no time is present), add rematch test in Summery: start mexico-canada, start spain-brazil,, finish mexico-canada, start mexico-canada again and mexico-should be first
29. commit step 5
30. ok step 6
31. commit step 6
32. start step 7
33. what about adding to thread-safety section, if needed - copy mateches inside the lock and sort outside it (safe, immutable records), bigger steos, read-write lock?
34. commit step 7
35. proceed to step 8
36. before commiting step 8, check TeamName for "Costa\u00A0Rica" and "Costa Rica" [Answers: collapse inner whitespace in both display and key; fix in a separate commit before Step 8]
37. commit it as "normalise whitespace inside team names" update CLAUDE.md, restore step 8 and rerun verify
38. commit step 8

**Hardening and review**

39. check the totalScore overflow [Answers: sum as long; own commit before Step 9]
40. do the strict code review of the whole repo, list findings, do not fix anything yet
41. fix H1 and H2
42. commit the h1 and h2 fixes, restore overflow fix
43. commit
44. proceed to m1-m4
45. commit and contiune with m2
46. override equals so new TeamName("Spain) and TeamName("spain) are the same
47. commit m2 and go to m3
48. commit and do m4
49. commit m4 and go to l1-l7, do the low level issues in one go
50. commit and do the documentation issues
51. just commit
52. do the step 9
