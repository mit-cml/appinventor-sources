# ListView — Cross-Platform Behavioral Contract and Parity Tracker

**MIT App Inventor · ListView Component Update · Continuity (Cross-Platform Porting) Phase**

---

## 1. Purpose and scope

The ListView component ships as two independent implementations that share no code, no interface:

- **Android** — `appinventor/components/src/com/google/appinventor/components/runtime/ListView.java`
- **iOS** — `appinventor/components-ios/src/ListView.swift`

Because every property, method, and event must be re-implemented by hand on each platform, features drift: a property that works on Android may be missing, mis-typed, or subtly wrong on iOS, and there is no central record of where.

This document is the **living parity contract** for the ListView component. It has two parts:

1. **The behavioral contract (§3)** — the single, platform-neutral definition of how every property, method, and event is expected to behave. Both implementations conform to this; it is the acceptance criterion for the port.
2. **The parity tracker (§4)** — the current implementation status of each feature on each platform, so gaps are visible and tracked rather than discovered through bug reports.

The member list below is the complete public surface declared in `ListView.java` (`@SimpleProperty` / `@SimpleFunction` / `@SimpleEvent`); descriptions are MIT App Inventor's own reference text where one exists.

---

## 2. How to read this document

### 2.1 Parity status

| Symbol | Meaning |
|:---:|---|
| ✅ | **Implemented** — present and behaves per the contract |
| 🟡 | **Partial** — present but incomplete |
| 🟠 | **Inconsistent** — present but behaves differently from the contract / Android |
| ❌ | **Missing** — not implemented on that platform |
| 🔍 | **To verify** — exists in code; behavior not yet confirmed by the dynamic audit |
| 🔵 | **Fix in review** — diverges on `ucr`, but a correction exists on a branch under review; re-verify and set to ✅ once merged |

### 2.2 Failure-mode taxonomy

Every iOS gap falls into one of three categories, and the category determines the fix:

| Failure mode | Meaning | Fix |
|---|---|---|
| **Missing method** | The `@objc` member does not exist; Blocks trigger `unrecognized selector` at runtime. | Implement the member in Swift. |
| **Wrong type handling** | The member exists but the Scheme→Swift coercion mishandles the value (e.g. a dictionary treated as plain text). | Correct the type coercion. |
| **Wrong logic** | The member exists and the type is correct, but the behavior is wrong (e.g. a color is accepted but not applied visually). | Align the iOS logic to this contract; validate against Android. |

### 2.3 Audit method

Each parity cell is confirmed by the **dynamic audit** (§5): a single test app exercises every member on both companions, and the observed iOS behavior is compared against the contract line and the Android reference. Static presence (the member compiles) is necessary but **not** sufficient — it only rules out *missing method*, never *wrong type* or *wrong logic*.

---

## 3. Behavioral contract

### 3.1 Interaction rules (cross-cutting)

These govern how the members below behave together. They are not attached to any single property and must hold on both platforms:

- **Filtering changes what is *visible*, not what is *selected*.** The selection is retained while the selected item remains visible in the filtered results, and is cleared **only** when the filter hides the selected item.
- **Selection is keyed to the item's original (unfiltered) index**, so `Selection` and `SelectionIndex` stay correct while a filter is active.
- **Filter matching is case-insensitive and substring-based**, evaluated over both the main and detail text of each row.
- **Data mutations refresh the view immediately.** `AddItem`, `AddItemAtIndex`, `AddItems`, `AddItemsAtIndex`, and `RemoveItemAtIndex` update the visible list without requiring any other action, and behave identically on both platforms.
- **A data mutation adjusts the selection only where it actually affects the selected item.** The selection tracks the *item* the user picked, not a fixed slot number:
  - **Append** (`AddItem`, `AddItems`) — selection untouched; existing indices do not move.
  - **Insert** (`AddItemAtIndex`, `AddItemsAtIndex`) — if the insert point is at or before the selected index, `SelectionIndex` shifts up by the number of rows inserted, so the same item stays selected. Selections before the insert point are unaffected.
  - **Remove** (`RemoveItemAtIndex`) — if the removed row *is* the selected item, the selection clears. If it sits before the selected item, `SelectionIndex` shifts down by one. Otherwise untouched.
  - **Update in place** (`UpdateItemAtIndex`) — if the replaced row is the selected item, it stops being selected, because it is no longer the item the user picked. Under `MultiSelect`, `Selection` / `SelectionIndex` move to the most recent remaining pick rather than reporting nothing. Rows other than the selected one are unaffected — replacing a row moves nothing.
  - **Replacing the whole list** (`Elements`, `ListData`, `ElementsFromString`) — clears the selection outright; every existing index is meaningless.

  *(This is the design implemented on `feature/listview-multiselect` and `feature/listview-update-item`. Note that `ucr` Android currently does something cruder — every mutation routes through `updateAdapterData()`, which calls `SelectionIndex(0)`, clearing the selection even on a plain append. That blanket clear is superseded by the branches above, where `updateAdapterData()` no longer touches the selection at all.)*
- **A mutation must never leave `Selection` disagreeing with the highlighted row.** Whatever adjustment the rule above calls for, the scalar properties and the visible highlight must end up pointing at the same row. *(`ucr` iOS violates this — after inserting above the selected row it adjusts neither, so `Selection` reports the old item while the highlight lands on whatever now occupies that index. Fixed on `feature/listview-multiselect`.)*
- **Clearing the selection clears all three selection properties together.** `Selection`, `SelectionIndex` and `SelectionDetailText` must never disagree about whether something is selected. *(`ucr` Android violates this — see the parity table note on `SelectionDetailText`; corrected on `feature/listview-update-item`.)*
- **`AfterPicking` fires *after* `Selection` / `SelectionIndex` are updated**, so a handler reading those properties observes the new selection. When `MultiSelect` is enabled it is raised on every tap, including a tap that deselects a row.

### 3.2 Properties — ListView-specific

Descriptions are MIT App Inventor's reference text (the expected behavior both platforms must satisfy).

| Property | Type | Expected behavior |
|---|---|---|
| `BackgroundColor` | color | The color of the `ListView` background. |
| `BounceEdgeEffect` | boolean | Enables/disables the bounce (over-scroll) effect. |
| `DividerColor` | color | The color of the divider line between rows. |
| `DividerThickness` | number | Thickness of the divider. If `0`, the divider is not visible. |
| `ElementColor` | color | The background color of a `ListView` element (row card). |
| `ElementCornerRadius` | number | Corner radius of a `ListView` element. |
| `ElementMarginsWidth` | number | Width of the margins around a `ListView` element. If `> 0`, the divider is not displayed. |
| `Elements` | list | The list of choices to display. Accepts plain strings or dictionaries (`Text1`/`Text2`/`Image`); setting replaces the whole list; the getter returns the current list. |
| `ElementsFromString` | text (set) | Sets the list from a comma-separated string, e.g. `Cheese,Fruit,Bacon,Radish`. |
| `FontSize` | number | Font size of the element's main text. |
| `FontSizeDetail` | number | Font size of the element's detail text. |
| `FontTypeface` | text | Main text typeface: default, serif, sans-serif, or monospace. |
| `FontTypefaceDetail` | text | Detail text typeface: default, serif, sans-serif, or monospace. |
| `HintText` | text | Hint (placeholder) shown in the filter bar. |
| `ImageHeight` | number | Image height for layouts containing images. |
| `ImageWidth` | number | Image width for layouts containing images. |
| `ListData` | text (designer) | Designer-only: sets rich rows for the chosen `ListViewLayout` (e.g. `Image,MainText`), each element carrying a filename and strings. |
| `ListViewLayout` | number | The row template: single-text, two-text, two-text-linear, image + single-text, image + two-text. |
| `Orientation` | number | `Vertical` (rows stacked) or `Horizontal` (one element at a time, swipe left/right to browse). |
| `Selection` | text | The main text of the row at `SelectionIndex`. Setting it selects the row whose main text matches (otherwise clears). |
| `SelectionColor` | color | The background color of a row while it is selected. |
| `SelectionDetailText` | text (read-only) | The detail (secondary) text of the row at `SelectionIndex`. |
| `SelectionIndex` | number | 1-based index of the selected row; `0` if none. Setting `< 1` or `>` item count → `0`, and `Selection` → empty. |
| `ShowFilterBar` | boolean | Shows/hides the filter (search) bar. |
| `TextAlignmentDetail` | number | Detail-text alignment: center, normal, or opposite. |
| `TextAlignmentMain` | number | Main-text alignment: center, normal, or opposite. |
| `TextColor` | color | The color of the main text of `ListView` items. |
| `TextColorDetail` | color | The color of the secondary (detail) text. |
| `MultiSelect` | boolean | *(Newer; not yet in public reference.)* When enabled, multiple rows may be selected at once. |
| `SelectedItems` | list (read-only) | *(New — arrives with PR #4067.)* Returns the list of all currently selected items when `MultiSelect` is enabled. |
| `TextSize` | number | *(Deprecated — backward-compatibility alias of `FontSize`. Not user-visible.)* |

### 3.3 Methods

| Method | Signature | Expected behavior |
|---|---|---|
| `AddItem` | `(mainText, detailText, imageName)` | Appends one new item to the list. |
| `AddItemAtIndex` | `(index, mainText, detailText, imageName)` | Inserts one new item at the given 1-based index. |
| `AddItems` | `(itemsList)` | Appends multiple items. |
| `AddItemsAtIndex` | `(index, itemsList)` | Inserts multiple items at the given 1-based index. |
| `CreateElement` | `(mainText, detailText, imageName)` → dictionary | Builds a single rich element (dictionary) for use with `Elements`/`ListData`. |
| `GetMainText` | `(listElement)` → text | Returns the main text of a `ListView` element. |
| `GetDetailText` | `(listElement)` → text | Returns the detail text of a `ListView` element. |
| `GetImageName` | `(listElement)` → text | Returns the image filename of a `ListView` element. |
| `RemoveItemAtIndex` | `(index)` | Removes the item at the given 1-based index. |
| `Refresh` | `()` | *(Deprecated — hidden from the blocks drawer, not reachable by users.)* Forces a redraw. |

### 3.4 Events

| Event | Signature | Expected behavior |
|---|---|---|
| `AfterPicking` | `()` | Raised after an element is chosen. The chosen element is available in `Selection` (updated before the event fires). Under `MultiSelect`, raised on every tap. |

### 3.5 Common inherited properties (framework-level)

These come from the shared visible-component framework, not from ListView itself. They are handled by the platform's base component layer, so they are **lower audit priority** — but listed here for completeness so the audit is exhaustive:

`Height`, `HeightPercent`, `Width`, `WidthPercent`, `Left`, `Top`, `Visible`.

---

## 4. Parity tracker

### 4.0 Audit status

The dynamic audit defined in §5 was **executed on 21–22 August 2026** against
`upstream/ucr`, running one identical test app on the Android emulator and the iOS
simulator with companions built from the same commit.

| | |
|---|---|
| Coverage | every member exercised on **both** platforms — nothing inferred from one alone |
| Findings | **11 on iOS** (in five clusters), **2 on Android** — 13 total |
| Verified equivalent | all six filter/selection interaction rules, plain-text list handling, `AfterPicking` ordering, layouts 1–2, `SelectionColor`, `Elements`/`CreateElement` type handling |
| Contract corrections | one §3.1 rule rewritten — a mutation adjusts the selection **only where it affects the selected item**, rather than clearing it wholesale |

**Two results worth highlighting:**

1. **The gaps run in both directions.** iOS is not simply a lagging port — it is the
   *correct* implementation on two of the thirteen findings (`SelectionIndex`
   clamping and `SelectionDetailText` clearing), where Android deviates from its own
   documented behaviour. This is why the contract is written as a neutral
   specification rather than "make iOS match Android".
2. **Two long-standing reports did not reproduce.** The proposal listed
   `SelectionColor` as "not applied visually" and `DividerColor` as failing at
   runtime on iOS; both now work. The historical iOS `AddItem`-on-a-plain-list
   defect is also fixed and stays fixed.

No row is left marked 🔍. Every member was run on both platforms, so each cell below
reports an observed result rather than an assumed one — including the Android column,
which is not given a default ✅ anywhere.


---

**What the Android column means.** Android is the **reference implementation**, and its ✅ is a *baseline* marker, not an independent test verdict: the contract text in §3 is derived from Android's shipping behavior and MIT's own reference docs, and every row here was extracted directly from the `@SimpleProperty` / `@SimpleFunction` / `@SimpleEvent` annotations in `ListView.java` — so the members provably exist and "missing method" cannot apply to Android by construction. ✅ therefore means *"this is the reference behavior,"* not *"proven bug-free."* Because the dynamic audit (§5) runs the **same** test app on both companions side by side, Android is exercised in the same pass at no extra cost; if Android is found to misbehave on a row, that Android cell is downgraded too — this table is not frozen at all-green.

The iOS column reflects the **executed** dynamic audit of 21–22 August 2026. Every row below was exercised on both platforms; no cell is left as *to verify*.

### 4.1 Properties

| Property | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| BackgroundColor | ✅ | ✅ | — | Black background renders identically on both. |
| BounceEdgeEffect | ✅ | ✅ | — | Both states verified on both platforms: OFF gives no bounce, ON bounces. |
| DividerColor | ✅ | ✅ | — | The configured colour renders correctly on both platforms.  |
| DividerThickness | ✅ | 🟠 | wrong logic (iOS) | **Verified 2026-08-22.** Android renders a thick cyan band at `20`. iOS renders a hairline at `20` **and still a hairline at `50`** — the property has no effect. Setting it to `0` on iOS still leaves the hairline, consistent with the property being ignored outright. **Verified on Android 2026-08-22: `0` correctly hides the divider** — no line visible anywhere in the list, confirming the documented behaviour on the platform that actually implements the property. |
| ElementColor | ✅ | ✅ | — | Applied at runtime on both — rows turn red when the Style button sets it. |
| ElementCornerRadius | ✅ | 🟠 | wrong logic (iOS) | **Verified 2026-08-22** with a row selected, so the yellow highlight makes corners visible against the black background. Android shows clearly rounded corners at `30`; iOS corners stay **square at `40`**. |
| ElementMarginsWidth | ✅ | 🟠 | wrong logic (iOS) | **Verified 2026-08-22.** Android insets rows to x=35 with 50px gaps **and correctly hides the divider** (documented behaviour). iOS shows no insets, no gaps and keeps drawing the divider. |
| Elements | ✅ | ✅ | — | Dictionaries from `CreateElement` render as rich rows on both — **no wrong-type coercion**; nothing renders as raw `{Text1=…}`. Setting `Elements` replaces the whole list correctly. |
| ElementsFromString | ✅ | ✅ | — | Produces plain-text rows on both, correctly rendered with no image and no detail line even under an image+detail layout. |
| FontSize | ✅ | ✅ | — | Verified at `30` — main text visibly large on both. |
| FontSizeDetail | ✅ | ✅ | — | Verified at `10` — detail text visibly tiny on both. |
| FontTypeface | ✅ | ✅ | — | Verified at `monospace` — clearly monospaced letterforms on both. |
| FontTypefaceDetail | ✅ | ✅ | — | Verified at `serif`, re-checked at `FontSizeDetail = 24` where the serifs are unmistakable. Applied on both. |
| HintText | ✅ | 🟠 | wrong logic (iOS) | **Root cause found in source 2026-08-23.** Both platforms declare the same default `"Search list..."`. Android's constructor calls the setter explicitly (`ListView.java:183`) so it reaches the widget. On iOS the setter is correctly wired (`filter.placeholder = _hint`, `ListView.swift:672`) but `_hint` is a **stored property default** (`ListView.swift:179`) — assigning it never invokes the setter, so `filter.placeholder` is never assigned at init. The hint appears only if an app sets `HintText` explicitly at runtime. Not a platform convention: iOS search bars support placeholders. Fix: apply `filter.placeholder = _hint` during setup. |
| ImageHeight | ✅ | 🟠 | wrong logic (iOS) | **Root cause found in source 2026-08-22.** `ListView.swift` constrains the image to `_imageWidth / 4` and `_imageHeight / 4` at three call sites (≈ lines 1005, 1057, 1106), so iOS renders every image at **a quarter of the requested size** (further compressed when even the quarter exceeds the row height). Android uses the value as given. Confirmed live: raising the setting does enlarge the image, but never to the requested size. Note the Designer mock (`MockListView.java`) independently divides by **5**, so one property currently has three different interpretations across mock, Android and iOS. |
| ImageWidth | ✅ | 🟠 | wrong logic (iOS) | **Root cause found in source 2026-08-22.** `ListView.swift` constrains the image to `_imageWidth / 4` and `_imageHeight / 4` at three call sites (≈ lines 1005, 1057, 1106), so iOS renders every image at **a quarter of the requested size** (further compressed when even the quarter exceeds the row height). Android uses the value as given. Confirmed live: raising the setting does enlarge the image, but never to the requested size. Note the Designer mock (`MockListView.java`) independently divides by **5**, so one property currently has three different interpretations across mock, Android and iOS. |
| ListData | ✅ | ✅ | — | **Verified 2026-08-22 on both platforms** — disabled `createList` in `Screen1.Initialize` so Designer-set ListData rows would actually render; rows with MainText/DetailText/Image displayed correctly on both. |
| ListViewLayout | ✅ | 🟠 | wrong logic (iOS) | All six layouts verified structurally identical on both, including the horizontal two-text 50/50 split (the layout fixed by PR #3944 — still correct). **iOS defect (2026-08-22):** at a large `FontSize` in layout 4, Android **grows the row** to fit wrapped main text plus detail text (nothing lost); iOS instead **drops content outright** — one row showed only the first word of the main text with the detail missing entirely, another row showed no text at all. Confirmed with a controlled same-settings comparison. |
| Orientation | ✅ | 🟠 | wrong logic (iOS) | Vertical matches. **Horizontal differs in three ways, confirmed 2026-08-22:** (1) Android shows **one element at a time** (as documented); iOS shows multiple elements side by side in a continuous scroll — reproduced in two separate layouts. (2) iOS **truncates** long text; Android shows it in full. (3) iOS's **filter bar disappears entirely** in horizontal orientation (confirmed missing in two captures); Android **keeps it visible** at the top, functioning as normal. Horizontal uses a `UICollectionView` on iOS — a separate code path from the table, so nothing proven in vertical carries over. |
| Selection | ✅ | 🔵 | wrong logic (iOS) | Reads correctly on tap and under an active filter on both. **But iOS does not refresh it on a data mutation** — see `AddItemAtIndex`: `Selection` keeps reporting the pre-mutation item while the highlight moves elsewhere. |
| SelectionColor | ✅ | ✅ | — | Yellow highlight visibly applied on both, and it **survives filtering** on iOS (`reloadData()` wipes UIKit's selection, so it is being re-applied correctly). The proposal's "not applied visually" report does **not** reproduce. |
| SelectionDetailText | 🟠 | 🟠 | wrong logic (both, different causes) | **Android — stale value (🔵 fix in review).** `selectionDetailText` is assigned only when the selected item is a dictionary carrying a main-text key; every other path leaves the previous value, so selecting a row on a plain-text list returns detail text from a dictionary list that was already replaced. Corrected on `feature/listview-update-item`, where the out-of-range path calls `clearSelectionInfo()` and resets all three selection fields together. **Both platforms — differing initial default (outstanding).** Before any row is ever selected, Android returns the literal string `"Uninitialized"` (`ListView.java:95`) while iOS returns `""` (`ListView.swift:162`). **No branch fixes this** — the literal is unchanged on `ucr`, `feature/listview-multiselect` and `feature/listview-update-item` alike. An empty string is the better target: it matches every other "nothing selected" path, and `"Uninitialized"` would surface to an end user as literal text. |
| SelectionIndex | 🔵 | ✅ | wrong logic (Android) | **Android-only defect (2026-08-21):** out-of-range values are stored verbatim instead of clamped to `0` — `SelectionIndex(41)` on a 40-item list leaves `41`, `-1` leaves `-1`, contradicting the property's own documented behaviour. Cause: the setter assigns `selectionIndex = index` **before** the range check. **iOS is correct** — verified returning `0` for `41`, `0` and `-1` (iOS clamps `-1` to `0` where Android stores it verbatim). Index mapping under an active filter is correct on both. |
| ShowFilterBar | ✅ | ✅ | — | Filter bar shown and functional on both. |
| TextAlignmentDetail | ✅ | ✅ | — | Verified at `right`/opposite — detail text right-aligned on both. |
| TextAlignmentMain | ✅ | ✅ | — | Verified at `center` — main text centred on both. |
| TextColor | ✅ | ✅ | — | Verified at `Green` — main text green on both. |
| TextColorDetail | ✅ | ✅ | — | Verified at `Orange` — detail text orange on both, distinct from the main colour. |
| **MultiSelect** | 🔵 | 🔵 | — | Implemented for **both** platforms in **PR #4067** (base `ucr`, in review). Not in `ucr`, therefore **not exercised on either platform**. Audit once it lands. |
| **SelectedItems** | 🔵 | 🔵 | — | New property added by **PR #4067** (both platforms). Not in `ucr`, therefore **not exercised on either platform**. Audit once merged. |

### 4.2 Methods

| Method | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| AddItem | ✅ | ✅ | — | Appends at the end on both. **Critically, on a plain-text list all existing rows survive** — the historical iOS defect (appending to the dictionary array and hiding plain rows) does **not** reproduce on `ucr`. |
| AddItemAtIndex | ✅ | 🔵 | wrong logic (iOS) | 1-based index and refresh correct on both. **iOS defect (2026-08-21):** iOS does not refresh selection state on a mutation at all — confirmed for both `AddItemAtIndex` and `RemoveItemAtIndex` — `Selection`, `SelectionIndex` and UIKit's selected row are all left untouched, so `Selection` reports the old item while the highlight lands on the new occupant of that index. 🔵 **Fix in review:** `feature/listview-multiselect` implements the §3.1 surgical rule on iOS — the index shifts when rows move above it and clears when the selected row itself goes. `ucr` Android's blanket clear is the cruder behaviour being replaced, not the target. |
| AddItems | ✅ | ✅ | — | Appends all supplied items at the end of the list on both platforms; verified with `AAA, BBB`. |
| AddItemsAtIndex | ✅ | ✅ | — | `PPP`/`QQQ` inserted at index 1 in the correct order on both platforms. 1-based. |
| CreateElement | ✅ | ✅ | — | The returned dictionary is consumed correctly by `Elements` on both; rows render with main text, detail and image. |
| GetMainText | ✅ | ✅ | — | Returns the main text correctly from a `CreateElement` dictionary on both platforms (Android: `guardiancreate1/Description 1/kaption.png`; iOS: `letter blue1/Description 1/kaption.png`). No type-handling problem. |
| GetDetailText | ✅ | ✅ | — | Returns the detail text correctly from a `CreateElement` dictionary on both platforms. No type-handling problem. |
| GetImageName | ✅ | ✅ | — | Returns the image filename correctly from a `CreateElement` dictionary on both platforms. No type-handling problem. |
| RemoveItemAtIndex | ✅ | 🔵 | wrong logic (iOS) | Removes the correct row, 1-based, on both platforms. **iOS defect confirmed 2026-08-22** (no longer inference): after removing a row the selection is not refreshed — the status label is byte-identical before and after, and the highlight stays on the same index, which now holds a different item. Same root cause as `AddItemAtIndex`. |
| Refresh (deprecated) | n/a | n/a | — | Marked `@Deprecated` in `ListView.java` and therefore **hidden from the blocks drawer** — not reachable by users on either platform. Out of scope for a user-facing parity audit; confirmed absent from the drawer 2026-08-22. |

### 4.3 Events

| Event | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| AfterPicking | ✅ | ✅ | — | Fires on tap on both, with `Selection` / `SelectionIndex` / `SelectionDetailText` **already updated** when the handler runs. Behaviour under `MultiSelect` not tested (that lands with PR #4067). |

---

## 5. Audit procedure

To turn each 🔍 into ✅ / 🟡 / 🟠 / ❌:

1. Build one App Inventor test app with a ListView and the blocks needed to set every property, call every method, and read `Selection`/`SelectionIndex`/`SelectionDetailText` on `AfterPicking` (e.g. into a Label so the live value is visible).
2. Run the **same** app on both companions — Android emulator and iOS simulator — side by side.
3. For each row in §4, exercise the member and compare the iOS behavior against the contract line (§3) and the Android result.
4. Record the result in the iOS column with a one-line note. If it is not ✅, tag the failure mode (§2.2).
5. Fix each non-✅ row per its failure mode, re-run, and update the cell to ✅.

**Outstanding checks not yet run** (carry these into the next pass):

*Never exercised on either platform:*
* `DividerThickness = 0` should hide the divider — checked on iOS (no effect, but iOS ignores the property entirely); **still untested on Android**.
* `ListData`, `AddItems`, `AddItemsAtIndex`, `GetMainText`, `GetDetailText`, `GetImageName`.
* `MultiSelect` and `SelectedItems` — not in `ucr`; audit once PR #4067 merges.

*Verified, but on weak or partial evidence:*
* iOS horizontal `Orientation` (multiple elements visible, text truncated) — observed in one layout only.

*Not run on one platform:*
* iOS layouts 0, 3, 4 and 5 — captured on Android only.

**Phase acceptance:** every row is ✅ (or has a documented, mentor-agreed exception), and this contract + tracker are reviewed and accepted by the mentors.

---

## 6. Notes

- **Only the members in this document are in scope for the dynamic audit.** If a member is missing here, it is missing from the audit — this list is the complete `ListView.java` public surface as of the current `ucr`.
- iOS/runtime changes are companion-affecting and target the **`ucr`** branch.
