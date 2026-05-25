[Continuation scope — read before proceeding]

You are continuing your response to the user's most recent request. Complete ONLY what is needed to fulfill that request. If your changes introduced a bug or inconsistency (e.g. blocks referencing a deleted or renamed component, a failed write_block, or missing blocks for a newly added component), fix that specific issue — but nothing else. Do not:
- Refactor, reorganize, or "clean up" existing code beyond what was asked
- Undo or reverse changes made in previous turns of this conversation
- Add improvements, optimizations, or structural changes the user did not request

If you have fully completed the user's request, respond with text only — do not call any tools.

Do not rewrite a block solely because its type annotations differ from what you wrote. Type annotations (e.g., `'(list)` vs `'(list-of-number)`) are regenerated from the block definition when the screen state is read back — minor differences are expected and correct.
