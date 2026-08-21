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
- **Data mutations preserve the selection by item, not by slot.** Inserting or removing a row *above* the selected item shifts the selection with it — the same item stays selected, only its `SelectionIndex` changes. Removing the selected item itself, or replacing the whole list via `Elements` / `ListData` / `ElementsFromString`, clears the selection (`SelectionIndex` → 0, `Selection` → empty).
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
| `Refresh` | `()` | *(Deprecated; not user-visible.)* Forces a redraw. |

### 3.4 Events

| Event | Signature | Expected behavior |
|---|---|---|
| `AfterPicking` | `()` | Raised after an element is chosen. The chosen element is available in `Selection` (updated before the event fires). Under `MultiSelect`, raised on every tap. |

### 3.5 Common inherited properties (framework-level)

These come from the shared visible-component framework, not from ListView itself. They are handled by the platform's base component layer, so they are **lower audit priority** — but listed here for completeness so the audit is exhaustive:

`Height`, `HeightPercent`, `Width`, `WidthPercent`, `Left`, `Top`, `Visible`.

---

## 4. Parity tracker

**What the Android column means.** Android is the **reference implementation**, and its ✅ is a *baseline* marker, not an independent test verdict: the contract text in §3 is derived from Android's shipping behavior and MIT's own reference docs, and every row here was extracted directly from the `@SimpleProperty` / `@SimpleFunction` / `@SimpleEvent` annotations in `ListView.java` — so the members provably exist and "missing method" cannot apply to Android by construction. ✅ therefore means *"this is the reference behavior,"* not *"proven bug-free."* Because the dynamic audit (§5) runs the **same** test app on both companions side by side, Android is exercised in the same pass at no extra cost; if Android is found to misbehave on a row, that Android cell is downgraded too — this table is not frozen at all-green.

The iOS column reflects the **static** check on `upstream/ucr` (member exists) plus known flags; it is **🔍 (to verify)** until the dynamic audit (§5) confirms behavior.

### 4.1 Properties

| Property | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| BackgroundColor | ✅ | 🔍 | — | |
| BounceEdgeEffect | ✅ | 🔍 | — | |
| DividerColor | ✅ | 🔍 | — | Proposal flagged a runtime failure; now present — confirm a line actually renders. |
| DividerThickness | ✅ | 🔍 | — | Confirm `0` hides the divider. |
| ElementColor | ✅ | 🔍 | — | |
| ElementCornerRadius | ✅ | 🔍 | — | |
| ElementMarginsWidth | ✅ | 🔍 | — | Confirm `> 0` hides the divider. |
| Elements | ✅ | 🔍 | (watch: wrong type) | Verify dictionaries render as rich rows, not plain text. |
| ElementsFromString | ✅ | 🔍 | — | |
| FontSize | ✅ | 🔍 | — | |
| FontSizeDetail | ✅ | 🔍 | — | |
| FontTypeface | ✅ | 🔍 | — | |
| FontTypefaceDetail | ✅ | 🔍 | — | |
| HintText | ✅ | 🔍 | — | |
| ImageHeight | ✅ | 🔍 | — | |
| ImageWidth | ✅ | 🔍 | — | |
| ListData | ✅ | 🔍 | — | Designer-only; verify rich rows render. |
| ListViewLayout | ✅ | 🔍 | — | Verify all row templates match Android. |
| Orientation | ✅ | 🔍 | — | Verify horizontal swipe browsing. |
| Selection | ✅ | 🔍 | — | |
| SelectionColor | ✅ | 🔍 | (watch: wrong logic) | Proposal flagged "not applied visually" — confirm highlight is visible. |
| SelectionDetailText | ✅ | 🔍 | — | |
| SelectionIndex | ✅ | 🔍 | (watch: wrong logic) | Verify index mapping is correct while a filter is active. |
| ShowFilterBar | ✅ | 🔍 | — | |
| TextAlignmentDetail | ✅ | 🔍 | — | |
| TextAlignmentMain | ✅ | 🔍 | — | |
| TextColor | ✅ | 🔍 | — | |
| TextColorDetail | ✅ | 🔍 | — | |
| **MultiSelect** | ✅ | 🟡 | — | Implemented for **both** platforms in **PR #4067** (base `ucr`, in review). Not yet merged to `ucr` — audit once it lands. |
| **SelectedItems** | ✅ | 🟡 | — | New property added by **PR #4067** (both platforms). Audit once merged. |

### 4.2 Methods

| Method | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| AddItem | ✅ | 🔍 | — | Recently fixed on iOS (refresh + array choice) — re-confirm. |
| AddItemAtIndex | ✅ | 🔍 | — | Confirm 1-based index + refresh. |
| AddItems | ✅ | 🔍 | — | |
| AddItemsAtIndex | ✅ | 🔍 | — | |
| CreateElement | ✅ | 🔍 | — | Verify returned dictionary is usable by `Elements`/`ListData`. |
| GetMainText | ✅ | 🔍 | — | |
| GetDetailText | ✅ | 🔍 | — | |
| GetImageName | ✅ | 🔍 | — | |
| RemoveItemAtIndex | ✅ | 🔍 | — | |
| Refresh (deprecated) | ✅ | 🔍 | — | |

### 4.3 Events

| Event | Android | iOS | Failure mode (if gap) | Notes / what to verify |
|---|:---:|:---:|---|---|
| AfterPicking | ✅ | 🔍 | — | Verify it fires with `Selection` already updated, incl. under `MultiSelect`. |

---

## 5. Audit procedure

To turn each 🔍 into ✅ / 🟡 / 🟠 / ❌:

1. Build one App Inventor test app with a ListView and the blocks needed to set every property, call every method, and read `Selection`/`SelectionIndex`/`SelectionDetailText` on `AfterPicking` (e.g. into a Label so the live value is visible).
2. Run the **same** app on both companions — Android emulator and iOS simulator — side by side.
3. For each row in §4, exercise the member and compare the iOS behavior against the contract line (§3) and the Android result.
4. Record the result in the iOS column with a one-line note. If it is not ✅, tag the failure mode (§2.2).
5. Fix each non-✅ row per its failure mode, re-run, and update the cell to ✅.

**Phase acceptance:** every row is ✅ (or has a documented, mentor-agreed exception), and this contract + tracker are reviewed and accepted by the mentors.

---

## 6. Notes

- **Only the members in this document are in scope for the dynamic audit.** If a member is missing here, it is missing from the audit — this list is the complete `ListView.java` public surface as of the current `ucr`.
- iOS/runtime changes are companion-affecting and target the **`ucr`** branch.
